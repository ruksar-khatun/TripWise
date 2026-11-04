package com.tripwise.core.common.network

import kotlinx.coroutines.flow.Flow

/** Reports connectivity. Implemented on Android in :core:data; faked in :core:testing. */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
