package com.lynn.s34598901.medtrack.data.medcoach

/**
 * Represents the UI state for generating MedCoach AI medication tips
 */
sealed interface MedCoachTipUIState {

    object Initial : MedCoachTipUIState

    object Loading : MedCoachTipUIState

    data class Success(val content: String) : MedCoachTipUIState

    data class Error(val errorMessage: String) : MedCoachTipUIState
}