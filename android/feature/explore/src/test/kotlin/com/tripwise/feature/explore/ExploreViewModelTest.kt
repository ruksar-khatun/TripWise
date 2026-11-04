package com.tripwise.feature.explore

import androidx.lifecycle.SavedStateHandle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.domain.usecase.ObserveSaveableDestinationsUseCase
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakeSavedDestinationRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val bali = TestData.destination("bali", country = "Indonesia", interests = setOf(TravelInterest.BEACHES))
    private val kyoto = TestData.destination("kyoto", country = "Japan", interests = setOf(TravelInterest.CULTURE))
    private val repo = FakeDestinationRepository(listOf(bali, kyoto))
    private val saved = FakeSavedDestinationRepository(listOf(bali, kyoto))

    private fun vm(args: Map<String, Any?> = emptyMap()) =
        ExploreViewModel(SavedStateHandle(args), repo, saved, ObserveSaveableDestinationsUseCase(saved))

    private fun ExploreUiState.ids() = (results as LoadState.Ready).data.map { it.destination.id }

    @Test fun `initial interest comes from the route`() = runTest {
        val vm = vm(mapOf("interest" to "CULTURE"))
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()
        assertEquals(TravelInterest.CULTURE, vm.uiState.value.interest)
        assertEquals(listOf("kyoto"), vm.uiState.value.ids())
    }

    @Test fun `search is debounced and matches country`() = runTest {
        val vm = vm()
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()
        vm.onAction(ExploreAction.QueryChanged("indo"))
        advanceTimeBy(SEARCH_DEBOUNCE_MS - 50)
        runCurrent()
        assertEquals(listOf("bali", "kyoto"), vm.uiState.value.ids()) // not yet applied
        advanceTimeBy(100)
        runCurrent()
        assertEquals(listOf("bali"), vm.uiState.value.ids())
    }

    @Test fun `clear filters resets query and interest`() = runTest {
        val vm = vm(mapOf("interest" to "BEACHES", "query" to "zzz"))
        backgroundScope.launch { vm.uiState.collect {} }
        advanceTimeBy(SEARCH_DEBOUNCE_MS + 1); runCurrent()
        assertEquals(emptyList<String>(), vm.uiState.value.ids())
        vm.onAction(ExploreAction.ClearFilters)
        runCurrent()
        assertEquals(listOf("bali", "kyoto"), vm.uiState.value.ids())
        assertEquals(null, vm.uiState.value.interest)
    }

    @Test fun `toggle save reflects in results`() = runTest {
        val vm = vm()
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()
        vm.onAction(ExploreAction.ToggleSave("kyoto", true))
        runCurrent()
        assertEquals(true, (vm.uiState.value.results as LoadState.Ready).data.first { it.destination.id == "kyoto" }.isSaved)
    }
}
