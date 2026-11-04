package com.tripwise.core.testing.fakes

import com.tripwise.core.common.network.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeNetworkMonitor(initiallyOnline: Boolean = true) : NetworkMonitor {
    private val online = MutableStateFlow(initiallyOnline)
    override val isOnline: StateFlow<Boolean> = online
    fun setOnline(value: Boolean) { online.value = value }
}
