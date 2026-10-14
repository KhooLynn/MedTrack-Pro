package com.lynn.s34598901.medtrack.data.clinician

/**
 * Represent the UI state for generating clinician dashboard insights
 */
sealed interface ClinicianUIState {

    // Initial state before the clinician generates any insight
    object Initial : ClinicianUIState

    // Loading state while the GenAI insight is being generated
    object Loading : ClinicianUIState

    // Success state containing the generated insight lines
    data class Success(val content: List<String>) : ClinicianUIState

    // Error state containing the message to show when generation fails
    data class Error(val errorMessage: String) : ClinicianUIState
}