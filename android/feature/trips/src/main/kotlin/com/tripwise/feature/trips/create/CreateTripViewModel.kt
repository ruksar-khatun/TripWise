package com.tripwise.feature.trips.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.UserRepository
import com.tripwise.core.model.CurrencyCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

enum class FieldError { REQUIRED, TRAVELLERS_RANGE, BUDGET_FORMAT }

data class CreateTripUiState(
    val title: String = "",
    val destination: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val travellers: Int = 2,
    val budget: String = "",
    val currency: CurrencyCode = CurrencyCode("INR"),
    val notes: String = "",
    /** Errors are only shown after the user tried to submit, to avoid shouting at an empty form. */
    val showErrors: Boolean = false,
) {
    val errors: Map<String, FieldError>
        get() = buildMap {
            if (title.isBlank()) put(FIELD_TITLE, FieldError.REQUIRED)
            if (destination.isBlank()) put(FIELD_DESTINATION, FieldError.REQUIRED)
            if (startDate == null || endDate == null) put(FIELD_DATES, FieldError.REQUIRED)
            if (travellers !in 1..MAX_TRAVELLERS) put(FIELD_TRAVELLERS, FieldError.TRAVELLERS_RANGE)
            if (budget.isNotBlank() && budget.toLongOrNull()?.takeIf { it >= 0 } == null) put(FIELD_BUDGET, FieldError.BUDGET_FORMAT)
        }

    val isValid: Boolean get() = errors.isEmpty()

    fun visibleError(field: String): FieldError? = if (showErrors) errors[field] else null

    companion object {
        const val FIELD_TITLE = "title"
        const val FIELD_DESTINATION = "destination"
        const val FIELD_DATES = "dates"
        const val FIELD_TRAVELLERS = "travellers"
        const val FIELD_BUDGET = "budget"
        const val MAX_TRAVELLERS = 20
    }
}

sealed interface CreateTripAction {
    data class TitleChanged(val value: String) : CreateTripAction
    data class DestinationChanged(val value: String) : CreateTripAction
    data class DatesChanged(val start: LocalDate?, val end: LocalDate?) : CreateTripAction
    data class TravellersChanged(val value: Int) : CreateTripAction
    data class BudgetChanged(val value: String) : CreateTripAction
    data class NotesChanged(val value: String) : CreateTripAction
    data object Submit : CreateTripAction
}

/**
 * Form state + validation for a new trip. Persisting the trip is part of the Trips phase;
 * in this UI phase Submit only validates (the screen explains that saving comes later).
 */
@HiltViewModel
class CreateTripViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    destinationRepository: DestinationRepository,
    userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()

    init {
        val destinationId: String? = savedStateHandle["destinationId"]
        viewModelScope.launch {
            val currency = userRepository.observeProfile().first().homeCurrency
            val destination = destinationId?.let { destinationRepository.observeDestination(it).firstOrNull() }
            _uiState.update { s ->
                s.copy(
                    currency = currency,
                    destination = destination?.name ?: s.destination,
                )
            }
        }
    }

    fun onAction(action: CreateTripAction) {
        _uiState.update { s ->
            when (action) {
                is CreateTripAction.TitleChanged -> s.copy(title = action.value.take(60))
                is CreateTripAction.DestinationChanged -> s.copy(destination = action.value.take(60))
                is CreateTripAction.DatesChanged -> s.copy(startDate = action.start, endDate = action.end)
                is CreateTripAction.TravellersChanged ->
                    s.copy(travellers = action.value.coerceIn(1, CreateTripUiState.MAX_TRAVELLERS))
                is CreateTripAction.BudgetChanged -> s.copy(budget = action.value.filter(Char::isDigit).take(12))
                is CreateTripAction.NotesChanged -> s.copy(notes = action.value.take(500))
                CreateTripAction.Submit -> s.copy(showErrors = true)
            }
        }
    }
}
