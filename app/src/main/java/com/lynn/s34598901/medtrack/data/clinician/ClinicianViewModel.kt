package com.lynn.s34598901.medtrack.data.clinician

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for clinician dashboard
 */
class ClinicianViewModel (application: Application) : ViewModel(){
    private val clinicianRepository = ClinicianRepository(application)
    private val patientRadarRepository = PatientRadarRepository(application)

    // Aggregate statistics shown on the clinician dashboard
    private val _stats = MutableStateFlow(Clinician())
    val stats: StateFlow<Clinician> = _stats.asStateFlow()

    // UI state for clinician dashboard GenAI insights
    private val _insightUIState = MutableStateFlow<ClinicianUIState>(ClinicianUIState.Initial)
    val insightUIState: StateFlow<ClinicianUIState> = _insightUIState.asStateFlow()

    // Patient radar list shown on the Patient Radar screen
    private val _radarItems = MutableStateFlow<List<PatientRadarItem>>(emptyList())
    val radarItems: StateFlow<List<PatientRadarItem>> = _radarItems.asStateFlow()

    // UI state for the Patient Radar GenAI summary
    private val _radarSummaryState = MutableStateFlow<PatientRadarUIState>(PatientRadarUIState.Initial)
    val radarSummaryState: StateFlow<PatientRadarUIState> = _radarSummaryState.asStateFlow()

    // Load aggregate clinician dashboard statistics from the clinicianRepository
    fun loadStats(){
        viewModelScope.launch {
            val patientCount = clinicianRepository.getPatientCount()
            val averageMedicationCount = clinicianRepository.getAverageMedicationCount()
            val mostCommonSymptomCategory = clinicianRepository.getMostCommonSymptomCategory()
            val averageSymptomSeverity = clinicianRepository.getAverageSymptomSeverity()

            _stats.value = Clinician(
                patientCount = patientCount,
                averageMedicationCount = averageMedicationCount,
                mostCommonSymptomCategory = mostCommonSymptomCategory,
                averageSymptomSeverity = averageSymptomSeverity
            )
        }
    }

    // Generate GenAI insights for the clinician dashboard
    fun generateInsights(){
        viewModelScope.launch {
            _insightUIState.value = ClinicianUIState.Loading

            when(val result = clinicianRepository.generateInsights(_stats.value)){
                is InsightsResult.Success -> {
                    _insightUIState.value = ClinicianUIState.Success(content = result.insights)
                }
                is InsightsResult.NetworkUnavailable -> {
                    _insightUIState.value = ClinicianUIState.Error(errorMessage = "Network unavailable. Please check your internet connection.")
                }
                is InsightsResult.Error -> {
                    _insightUIState.value = ClinicianUIState.Error(errorMessage = "Something went wrong. Please try again later.")
                }
            }
        }
    }

    // Load patient radar priority data from the patientRadarRepository
    fun loadPatientRadar(){
        viewModelScope.launch {
            _radarItems.value = patientRadarRepository.getPatientAttentionRadar()
        }
    }

    // Generate a GenAI summary for the patient radar data
    fun generateRadarSummary(){
        viewModelScope.launch {
            _radarSummaryState.value = PatientRadarUIState.Loading

            when(val result = patientRadarRepository.generateRadarSummary(_radarItems.value)){
                is RadarSummaryResult.Success -> {
                    _radarSummaryState.value = PatientRadarUIState.Success(content = result.summary)
                }
                RadarSummaryResult.NetworkUnavailable -> {
                    _radarSummaryState.value = PatientRadarUIState.Error(errorMessage = "Network unavailable. Please check your internet connection.")
                }

                RadarSummaryResult.Error -> {
                    _radarSummaryState.value = PatientRadarUIState.Error(errorMessage = "Something went wrong. Please try again later.")
                }
            }
        }
    }

    /**
     * Factory used to create ClinicianViewModel with an Application parameter
     */
    class ClinicianViewModelFactory(private val application: Application): ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClinicianViewModel(application) as T
        }
    }
}