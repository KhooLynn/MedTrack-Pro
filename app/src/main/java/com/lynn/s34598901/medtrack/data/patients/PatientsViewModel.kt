package com.lynn.s34598901.medtrack.data.patients

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for patient account related UI operations.
 *
 * Screen data is exposed through StateFlow so composables only observe ViewModel state.
 * Database work still goes through PatientsRepository.
 */
class PatientsViewModel(context: Context): ViewModel(){
    val repository = PatientsRepository(context)

    private val _currentPatient = MutableStateFlow<Patient?>(null)
    val currentPatient: StateFlow<Patient?> = _currentPatient.asStateFlow()

    private val _currentPatientName = MutableStateFlow("")
    val currentPatientName: StateFlow<String> = _currentPatientName.asStateFlow()

    // Load the currently logged-in patient and expose it to the UI through StateFlow
    fun loadPatient(patientID: String){
        viewModelScope.launch {
            val patient = repository.getPatientByPatientID(patientID)
            _currentPatient.value = patient
            _currentPatientName.value = patient?.name ?: ""
        }
    }

    // Clear the patient state when the user logs out
    fun clearCurrentPatient(){
        _currentPatient.value = null
        _currentPatientName.value = ""
    }

    // Insert one patient record using a coroutine
    fun insert(patient: Patient){
        viewModelScope.launch {
            repository.insertPatient(patient)
        }
    }

    // Create a new patient account if the phone number is unique
    fun signUpNewPatient(
        fullName: String,
        phoneNumber: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ){
        viewModelScope.launch {

            // Check whether the phone number is not already used
            val isUnique = repository.countRepeatedPhoneNumbers(phoneNumber)

            if(!isUnique){

                // Return failure if the phone number already exists
                onResult(false, null)
            }else{

                // Generate a new patient ID and create a new patient record
                val newPatientID = repository.generateNewPatientID()
                val newPatient = Patient(
                    patientID = newPatientID,
                    phoneNumber = phoneNumber,
                    name = fullName,
                    password = password
                )

                repository.insertPatient(newPatient)

                // Return success with the generated patient ID
                onResult(true, newPatientID)
            }
        }
    }

    // Check the patient ID and password during login
    fun login(
        patientID: String,
        password: String,
        onResult: (String) -> Unit
    ){
        viewModelScope.launch {
            val loginResult = repository.login(patientID, password)
            onResult(loginResult)
        }
    }

    // Claim an existing patient account by checking patient ID and phone number
    fun claimAccount(
        patientID: String,
        phoneNumber: String,
        password: String,
        onResult: (String) -> Unit
    ){
        viewModelScope.launch {
            val claimResult = repository.claimAccount(patientID, phoneNumber, password)
            onResult(claimResult)
        }
    }

    // Keep these helper functions for one-time actions, but prefer loadPatient() for screen data.
    fun getPatientName(patientID: String, onResult: (String) -> Unit){
        viewModelScope.launch {
            val patient = repository.getPatientByPatientID(patientID)
            onResult(patient?.name ?: "")
        }
    }

    fun getPatient(patientID: String, onResult: (Patient?) -> Unit){
        viewModelScope.launch {
            val patient = repository.getPatientByPatientID(patientID)
            onResult(patient)
        }
    }

    /**
     * Factory used to create PatientsViewModel with a Context parameter
     */
    class PatientsViewModelFactory(context: Context) : ViewModelProvider.Factory {
        private val context = context.applicationContext
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PatientsViewModel(context) as T
    }
}
