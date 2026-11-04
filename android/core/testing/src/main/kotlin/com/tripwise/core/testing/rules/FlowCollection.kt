package com.tripwise.core.testing.rules

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Subscribes to [flow] for the rest of the test so `stateIn(WhileSubscribed)` upstreams start,
 * collecting eagerly (unconfined) so `uiState.value` is up to date right after each action.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.collectInBackground(flow: Flow<T>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
}
