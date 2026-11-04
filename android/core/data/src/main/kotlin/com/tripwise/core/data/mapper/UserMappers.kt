package com.tripwise.core.data.mapper

import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.UserProfile
import com.tripwise.core.network.dto.UserDto

internal fun UserDto.toModel() = UserProfile(
    id = id,
    displayName = displayName,
    email = email,
    photo = null,
    homeCurrency = CurrencyCode(homeCurrency),
)
