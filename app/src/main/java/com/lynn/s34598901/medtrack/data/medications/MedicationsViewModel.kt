package com.lynn.s34598901.medtrack.data.medications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for medication related UI operations
 */
class MedicationsViewModel(context: Context): ViewModel() {
    val repository = MedicationsRepository(context)

    // Insert one medication record using coroutine
    fun insert(medication: Medication){
        viewModelScope.launch {
            repository.insertMedication(medication)
        }
    }


    //Get all medications that belong to that one patient
    fun getMedicationsByPatientID(patientID: String): Flow<List<Medication>> = repository.getMedicationsByPatientID(patientID)

    // Update whether a medication is marked as taken today
    fun updateStatus(medication: Medication, isTaken: Boolean){
        viewModelScope.launch {

            // Format today's date so the app know when the medication was marked
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

            // Update the selected medication using its composite primary key values
            repository.updateStatus(
                medicationId = medication.medicationId,
                isTaken = isTaken,
                dateTaken = today
            )
        }
    }

    // Reset medication taken status if it was marked on a previous date
    fun resetTakenStatus(){
        viewModelScope.launch {
            // Format today's date so old taken records can be reset
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

            // Reset medications that were not marked as taken today
            repository.resetTakenStatus(today)
        }
    }

    /**
     * Factory used to create MedicationsViewModel with a Context parameter
     */
    class MedicationsViewModelFactory(context: Context) : ViewModelProvider.Factory {
        private val context = context.applicationContext
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MedicationsViewModel(context) as T
    }
}