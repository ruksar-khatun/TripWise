package com.tripwise.feature.saved

import com.tripwise.core.common.result.LoadState
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeSavedDestinationRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class SavedViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val bali = TestData.destination("bali", name = "Bali")
    private val agra = TestData.destination("agra", name = "Agra")
    private val repo = FakeSavedDestinationRepository(
        listOf(bali, agra),
        mapOf("bali" to Instant.parse("2026-01-02T00:00:00Z"), "agra" to Instant.parse("2026-01-01T00:00:00Z")),
    )

    private fun SavedUiState.ids() = (items as LoadState.Ready).data.map { it.destination.id }

    @Test fun `sorts by recent then by name`() = runTest {
        val vm = SavedViewModel(repo)
        backgroundScope.launch { vm.uiState.collect {} }
        assertEquals(listOf("bali", "agra"), vm.uiState.value.ids())
        vm.onAction(SavedAction.SortChanged(SavedSort.NAME))
        assertEquals(listOf("agra", "bali"), vm.uiState.value.ids())
    }

    @Test fun `remove then undo restores the item`() = runTest {
        val vm = SavedViewModel(repo)
        backgroundScope.launch { vm.uiState.collect {} }
        val item = (vm.uiState.value.items as LoadState.Ready).data.first { it.destination.id == "agra" }
        vm.onAction(SavedAction.Remove(item))
        assertEquals(listOf("bali"), vm.uiState.value.ids())
        assertEquals("agra", vm.uiState.value.lastRemoved?.destination?.id)
        vm.onAction(SavedAction.UndoRemove)
        assertEquals(setOf("bali", "agra"), vm.uiState.value.ids().toSet())
        assertEquals(null, vm.uiState.value.lastRemoved)
    }

    @Test fun `dismissing the snackbar keeps the removal`() = runTest {
        val vm = SavedViewModel(repo)
        backgroundScope.launch { vm.uiState.collect {} }
        val item = (vm.uiState.value.items as LoadState.Ready).data.first()
        vm.onAction(SavedAction.Remove(item))
        vm.onAction(SavedAction.UndoDismissed)
        assertEquals(1, vm.uiState.value.ids().size)
        assertEquals(null, vm.uiState.value.lastRemoved)
    }
}
