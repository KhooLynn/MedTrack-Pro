package com.lynn.s34598901.medtrack.data.medcoach

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lynn.s34598901.medtrack.data.medications.MedicationsRepository
import com.lynn.s34598901.medtrack.data.symptoms.SymptomsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the MedCoach screen
 */
class MedCoachViewModel(application: Application) : AndroidViewModel(application) {

    // Repository used for openFDA drug search, Gemini tips and saved tip history
    private val repository = MedCoachRepository(application)

    // Private mutable state for drug search results
    private val _uiState: MutableStateFlow<MedCoachUIState> = MutableStateFlow(MedCoachUIState.Initial)

    // Public read-only state observed by the MedCoach screen
    val uiState: StateFlow<MedCoachUIState> = _uiState.asStateFlow()

    // Private mutable state for the current patient's medication names
    private val _patientMedications: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    // Public read-only medication list observed by the MedCoach screen
    val patientMedications: StateFlow<List<String>> = _patientMedications.asStateFlow()

    // Repository used to load the patient's saved medications
    private val medicationsRepository = MedicationsRepository(application.applicationContext)

    // Private mutable state for AI medication tip generation
    private val _tipUIState: MutableStateFlow<MedCoachTipUIState> = MutableStateFlow(MedCoachTipUIState.Initial)

    // Public read-only state observed by the MedCoach tip card
    val tipUIState: StateFlow<MedCoachTipUIState> = _tipUIState.asStateFlow()

    // Private mutable state for the patient's previous AI tips
    private val _tipHistory: MutableStateFlow<List<MedCoachGenAITips>> = MutableStateFlow(emptyList())

    // Public read-only tip history observed by the MedCoach screen
    val tipHistory: StateFlow<List<MedCoachGenAITips>> = _tipHistory.asStateFlow()

    // Repository used to load the patient's saved symptoms
    private val symptomsRepository = SymptomsRepository(application.applicationContext)

    // Private mutable state for the current patient's symptom categories
    private val _patientSymptoms: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    // Public read-only symptom list used when generating medication tips
    val patientSymptoms: StateFlow<List<String>> = _patientSymptoms.asStateFlow()


    // Load the current patient's medication names from the medication clinicianRepository
    fun loadPatientMedications(patientID: String){
        viewModelScope.launch {
            medicationsRepository.getMedicationsByPatientID(patientID).collect { medications ->
                _patientMedications.value = medications.map { medication ->
                    medication.name
                }
            }
        }
    }

    // Load the patient's previously generated AI medication tips
    fun loadTipHistory(patientID: String){
        viewModelScope.launch {
            repository.getTipsByPatientID(patientID).collect { tips ->
                // Update the tip history state whenever the database tip list changes
                _tipHistory.value = tips
            }
        }
    }

    // Search drug information using the medication name entered or selected by the user
    fun searchDrug(drugName: String){

        // Stop the search if the drug name is blank
        if (drugName.isBlank()){
            _uiState.value = MedCoachUIState.Error(errorMessage = "Drug name cannot be blank")
            return
        }

        viewModelScope.launch {
            _uiState.value = MedCoachUIState.Loading

            when(val result = repository.searchDrug(drugName)){
                is DrugInfoResult.Success -> {
                    _uiState.value = MedCoachUIState.Success(drug = result.drug)
                }
                is DrugInfoResult.NotFound -> {
                    _uiState.value = MedCoachUIState.Error(errorMessage = "Drug not found. Please check the medication name.")
                }
                is DrugInfoResult.NetworkUnavailable -> {
                    _uiState.value = MedCoachUIState.Error(errorMessage = "Network unavailable. Please check your internet connection.")
                }
                is DrugInfoResult.Error -> {
                    _uiState.value = MedCoachUIState.Error(errorMessage = "Something went wrong. Please try again later.")
                }
            }
        }
    }

    // Generate a personalized AI medication tip for the current patient
    fun generateTip(patientID: String){
        if(patientID.isBlank()){
            _tipUIState.value = MedCoachTipUIState.Error(errorMessage = "Patient ID cannot be blank")
            return
        }

        viewModelScope.launch {
            _tipUIState.value = MedCoachTipUIState.Loading

            when (val result = repository.generateMedicationTip(
                patientID = patientID,
                medicationName = _patientMedications.value,
                symptomName = _patientSymptoms.value)){
                is TipResult.Success -> {
                    _tipUIState.value = MedCoachTipUIState.Success(content = result.content)
                }

                is TipResult.NetworkUnavailable -> {
                    _tipUIState.value = MedCoachTipUIState.Error(errorMessage = "Network unavailable. Please check your internet connection.")
                }

                is TipResult.Error -> {
                    _tipUIState.value = MedCoachTipUIState.Error(errorMessage = "Something went wrong. Please try again later.")
                }
            }
        }
    }

    // Load the current patient's symptom categories from the symptoms clinicianRepository
    fun loadPatientSymptoms(patientID: String) {
        viewModelScope.launch {
            symptomsRepository.getSymptomsByPatientID(patientID).collect { symptoms ->
                _patientSymptoms.value = symptoms.map { symptom ->
                    symptom.category
                }
            }
        }
    }

    /**
     * Factory used to create MedCoachViewModel with an Application parameter
     */
    class MedCoachViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MedCoachViewModel(application) as T
        }
    }
}