package com.tripwise.core.testing.fakes

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.repository.AuthRepository
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.SessionState
import com.tripwise.core.model.UserProfile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(initial: SessionState = SessionState.SignedOut()) : AuthRepository {
    override val session = MutableStateFlow(initial)

    /** Result for the next sign-in / sign-up / delete call; Success signs the user in. */
    var nextResult: Outcome<Unit> = Outcome.Success(Unit)
    /** Set to hold calls open (to observe loading state). */
    var gate: CompletableDeferred<Unit>? = null
    val calls = mutableListOf<String>()

    private fun user(name: String, email: String) = UserProfile("u1", name, email, null, CurrencyCode("INR"))

    override suspend fun signIn(email: String, password: String): Outcome<Unit> {
        calls += "signIn:$email"
        gate?.await()
        return nextResult.also { if (it is Outcome.Success) session.value = SessionState.SignedIn(user("User", email)) }
    }

    override suspend fun signUp(displayName: String, email: String, password: String): Outcome<Unit> {
        calls += "signUp:$email"
        gate?.await()
        return nextResult.also { if (it is Outcome.Success) session.value = SessionState.SignedIn(user(displayName, email)) }
    }

    override suspend fun signOut() {
        calls += "signOut"
        session.value = SessionState.SignedOut()
    }

    override suspend fun continueAsGuest() {
        calls += "guest"
        session.value = SessionState.SignedIn(com.tripwise.core.model.guestProfile())
    }

    override suspend fun leaveGuestMode() {
        calls += "leaveGuest"
        session.value = SessionState.SignedOut()
    }

    override suspend fun deleteAccount(password: String): Outcome<Unit> {
        calls += "delete"
        return nextResult.also { if (it is Outcome.Success) session.value = SessionState.SignedOut() }
    }
}
