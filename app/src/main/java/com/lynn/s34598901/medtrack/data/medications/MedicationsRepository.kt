package com.lynn.s34598901.medtrack.data.medications

import android.content.Context
import com.lynn.s34598901.medtrack.data.MedtrackDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Repository for medication data operations
 */
class MedicationsRepository(context : Context){

    // Get the medication DAO from the Room database
    private val medicationDao = MedtrackDatabase.getDatabase(context).medicationDao()

    // Insert one medication record into the database
    suspend fun insertMedication(medication: Medication){
        medicationDao.insertMedication(medication)
    }

    // Get all medications that belong to one patient
    fun getMedicationsByPatientID(patientID: String): Flow<List<Medication>> = medicationDao.getMedicationsByPatientID(patientID)

    // Update the taken status and taken date for one medication
    suspend fun updateStatus(
        medicationId: Int,
        isTaken: Boolean,
        dateTaken: String
    ){
        medicationDao.updateStatus(
            medicationId = medicationId,
            isTaken = isTaken,
            dateTaken = dateTaken
        )
    }

    // Reset medication taken status for records not marked today
    suspend fun resetTakenStatus(today: String){
        medicationDao.resetTakenStatus(today)
    }
}