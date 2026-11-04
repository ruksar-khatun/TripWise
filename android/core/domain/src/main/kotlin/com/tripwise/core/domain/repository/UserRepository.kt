package com.tripwise.core.domain.repository

import com.tripwise.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeProfile(): Flow<UserProfile>
}
