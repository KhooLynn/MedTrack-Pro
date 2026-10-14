package com.lynn.s34598901.medtrack.data.medcoach

/**
 * Represents the UI state for searching drug information in MedCoach
 */
sealed interface MedCoachUIState {
    object Initial: MedCoachUIState

    object Loading: MedCoachUIState

    data class Success(val drug: DrugResult): MedCoachUIState

    data class Error(val errorMessage: String): MedCoachUIState
}