package com.tripwise.core.domain.repository

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.SessionState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val session: StateFlow<SessionState>

    suspend fun signIn(email: String, password: String): Outcome<Unit>

    suspend fun signUp(displayName: String, email: String, password: String): Outcome<Unit>

    /** Always ends the local session, even when the server can't be reached. */
    suspend fun signOut()

    /** Deletes the account on the server (password re-confirmed), then ends the local session. */
    suspend fun deleteAccount(password: String): Outcome<Unit>
}
