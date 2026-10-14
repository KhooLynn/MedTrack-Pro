package com.lynn.s34598901.medtrack.data.symptoms

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * ViewModel for symptom related UI operations
 */
class SymptomsViewModel(context: Context): ViewModel(){
    val repository = SymptomsRepository(context)

    // Insert one symptom record using coroutine
    fun insert(symptom: Symptom){
        viewModelScope.launch {
            repository.insertSymptom(symptom)
        }
    }

    // Get all symptoms logged for one patient
    fun getSymptomsByPatientID(patientID: String) = repository.getSymptomsByPatientID(patientID)

    /**
     * Factory used to create SymptomsViewModel with a Context parameter
     */
    class SymptomsViewModelFactory(context: Context) : ViewModelProvider.Factory {
        private val context = context.applicationContext
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SymptomsViewModel(context) as T
    }
}