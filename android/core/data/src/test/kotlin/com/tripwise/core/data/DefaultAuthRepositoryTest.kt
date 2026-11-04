package com.tripwise.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.data.repository.DefaultAuthRepository
import com.tripwise.core.data.repository.LocalUserData
import com.tripwise.core.datastore.ProfileCache
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.SessionState
import com.tripwise.core.model.UserProfile
import com.tripwise.core.network.auth.AuthTokens
import com.tripwise.core.network.auth.SessionEvents
import com.tripwise.core.network.auth.TokenStore
import com.tripwise.core.network.datasource.AuthNetworkDataSource
import com.tripwise.core.network.dto.AuthResponse
import com.tripwise.core.network.dto.TokensDto
import com.tripwise.core.network.dto.UserDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private class FakeTokenStore(var tokens: AuthTokens? = null) : TokenStore {
    override suspend fun get() = tokens
    override suspend fun save(tokens: AuthTokens) { this.tokens = tokens }
    override suspend fun clear() { tokens = null }
}

private class FakeLocalData : LocalUserData {
    var clears = 0
    override suspend fun clear() { clears++ }
}

private class FakeAuthNetwork : AuthNetworkDataSource {
    var loginResult: Outcome<AuthResponse> = Outcome.Success(response("u1"))
    var meResult: Outcome<UserDto> = Outcome.Success(user("u1", "Fresh Name"))
    var logoutResult: Outcome<Unit> = Outcome.Success(Unit)
    var deleteResult: Outcome<Unit> = Outcome.Success(Unit)
    val loggedOut = mutableListOf<String>()

    override suspend fun register(email: String, password: String, displayName: String) = loginResult
    override suspend fun login(email: String, password: String) = loginResult
    override suspend fun logout(refreshToken: String): Outcome<Unit> { loggedOut += refreshToken; return logoutResult }
    override suspend fun me() = meResult
    override suspend fun deleteAccount(password: String) = deleteResult

    companion object {
        fun user(id: String, name: String = "Ann") = UserDto(id, "$id@example.com", name, "INR", "2026-01-01T00:00:00Z")
        fun response(id: String) = AuthResponse(user(id), TokensDto("access-$id", 900, "refresh-$id", "2026-02-01T00:00:00Z"))
    }
}

/**
 * DataStore does its file I/O on real background threads, so tests wait for the session state
 * itself instead of relying on virtual-time idling.
 */
private suspend fun DefaultAuthRepository.awaitSession(predicate: (SessionState) -> Boolean): SessionState =
    session.first(predicate)

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAuthRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private val tokens = FakeTokenStore()
    private val local = FakeLocalData()
    private val network = FakeAuthNetwork()
    private val events = SessionEvents()
    private val cache by lazy {
        ProfileCache(PreferenceDataStoreFactory.create(produceFile = { tmp.newFile("test.preferences_pb").also { it.delete() } }))
    }

    private fun TestScope.repo() = DefaultAuthRepository(network, tokens, cache, local, events, backgroundScope.let {
        kotlinx.coroutines.CoroutineScope(it.coroutineContext + UnconfinedTestDispatcher(testScheduler))
    })

    @Test fun `no stored session starts signed out`() = runTest {
        val repo = repo()
        assertEquals(SessionState.SignedOut(), repo.awaitSession { it != SessionState.Unknown })
    }

    @Test fun `stored session restores signed in, then refreshes profile from server`() = runTest {
        tokens.tokens = AuthTokens("a", "r")
        cache.save(UserProfile("u1", "Cached Name", "u1@example.com", null, CurrencyCode("INR")))
        val repo = repo()
        val state = repo.awaitSession { (it as? SessionState.SignedIn)?.user?.displayName == "Fresh Name" }
        assertEquals("Fresh Name", (state as SessionState.SignedIn).user.displayName)
    }

    @Test fun `offline start keeps the cached profile`() = runTest {
        tokens.tokens = AuthTokens("a", "r")
        cache.save(UserProfile("u1", "Cached Name", "u1@example.com", null, CurrencyCode("INR")))
        network.meResult = Outcome.Failure(AppError.Network)
        val repo = repo()
        val state = repo.awaitSession { it is SessionState.SignedIn }
        assertEquals("Cached Name", (state as SessionState.SignedIn).user.displayName)
    }

    @Test fun `sign in stores tokens, a different account wipes previous local data`() = runTest {
        val repo = repo()
        repo.awaitSession { it != SessionState.Unknown }
        val baseline = local.clears
        assertEquals(Outcome.Success(Unit), repo.signIn(" u1@example.com ", "pw"))
        assertEquals(AuthTokens("access-u1", "refresh-u1"), tokens.tokens)
        assertEquals(baseline + 1, local.clears) // no previous profile → treated as different
        repo.signOut()
        network.loginResult = Outcome.Success(FakeAuthNetwork.response("u2"))
        repo.signIn("u2@example.com", "pw")
        assertEquals("u2", (repo.session.value as SessionState.SignedIn).user.id)
    }

    @Test fun `failed sign in leaves the user signed out`() = runTest {
        network.loginResult = Outcome.Failure(AppError.InvalidCredentials)
        val repo = repo()
        repo.awaitSession { it != SessionState.Unknown }
        assertEquals(Outcome.Failure(AppError.InvalidCredentials), repo.signIn("x", "y"))
        assertEquals(SessionState.SignedOut(), repo.session.value)
        assertNull(tokens.tokens)
    }

    @Test fun `sign out revokes on server and clears everything even when offline`() = runTest {
        val repo = repo()
        repo.awaitSession { it != SessionState.Unknown }
        repo.signIn("u1@example.com", "pw")
        network.logoutResult = Outcome.Failure(AppError.Network)
        val before = local.clears
        repo.signOut()
        assertEquals(listOf("refresh-u1"), network.loggedOut)
        assertNull(tokens.tokens)
        assertNull(cache.get())
        assertEquals(before + 1, local.clears)
        assertEquals(SessionState.SignedOut(expired = false), repo.session.value)
    }

    @Test fun `server-side expiry ends the session with expired flag`() = runTest {
        val repo = repo()
        repo.awaitSession { it != SessionState.Unknown }
        repo.signIn("u1@example.com", "pw")
        events.notifyExpired()
        assertEquals(SessionState.SignedOut(expired = true), repo.awaitSession { it is SessionState.SignedOut })
    }

    @Test fun `account deletion only signs out when the server confirms`() = runTest {
        val repo = repo()
        repo.awaitSession { it != SessionState.Unknown }
        repo.signIn("u1@example.com", "pw")
        network.deleteResult = Outcome.Failure(AppError.InvalidCredentials)
        assertEquals(Outcome.Failure(AppError.InvalidCredentials), repo.deleteAccount("wrong"))
        assert(repo.session.value is SessionState.SignedIn)
        network.deleteResult = Outcome.Success(Unit)
        repo.deleteAccount("right")
        assertEquals(SessionState.SignedOut(), repo.session.value)
    }
}
