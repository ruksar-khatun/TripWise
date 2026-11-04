package com.tripwise.feature.destination

import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakeSavedDestinationRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import com.tripwise.core.testing.rules.collectInBackground
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DestinationDetailViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val bali = TestData.destination("bali")
    private val saved = FakeSavedDestinationRepository(listOf(bali))

    @Test fun `loading, then ready, and save toggles`() = runTest {
        val repo = FakeDestinationRepository(null)
        val vm = DestinationDetailViewModel("bali", repo, saved)
        collectInBackground(vm.uiState)
        assertEquals(DestinationDetailUiState.Loading, vm.uiState.value)

        repo.destinations.value = listOf(bali)
        assertEquals(DestinationDetailUiState.Ready(bali, isSaved = false), vm.uiState.value)

        vm.onAction(DestinationDetailAction.ToggleSave)
        assertEquals(DestinationDetailUiState.Ready(bali, isSaved = true), vm.uiState.value)
    }

    @Test fun `unknown id is NotFound`() = runTest {
        val vm = DestinationDetailViewModel("nope", FakeDestinationRepository(listOf(bali)), saved)
        collectInBackground(vm.uiState)
        assertEquals(DestinationDetailUiState.NotFound, vm.uiState.value)
    }
}
