package com.lynn.s34598901.medtrack.data.symptoms

import android.content.Context
import com.lynn.s34598901.medtrack.data.MedtrackDatabase

/**
 * Repository for symptom data operations
 */
class SymptomsRepository(context: Context){

    // Get the symptom DAO from the Room database
    private val symptomDao = MedtrackDatabase.getDatabase(context).symptomDao()

    // Insert one symptom record into the database
    suspend fun insertSymptom(symptom: Symptom){
        symptomDao.insertSymptom(symptom)
    }

    // Get all symptoms logged for one patient
    fun getSymptomsByPatientID(patientID: String) = symptomDao.getSymptomsByPatientID(patientID)
}