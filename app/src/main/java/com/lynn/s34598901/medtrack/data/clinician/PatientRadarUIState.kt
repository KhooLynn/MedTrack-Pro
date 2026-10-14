package com.lynn.s34598901.medtrack.data.clinician

/**
 * Represents the UI state for generating the Patient Radar summary
 */
sealed interface PatientRadarUIState {
    object Initial : PatientRadarUIState
    object Loading : PatientRadarUIState
    data class Success(val content: String) : PatientRadarUIState
    data class Error(val errorMessage: String) : PatientRadarUIState
}