package com.lynn.s34598901.medtrack.data.patients

import android.content.Context
import com.lynn.s34598901.medtrack.data.MedtrackDatabase

/**
 * Repository for patient account data operations
 */
class PatientsRepository(context: Context){
    private val patientDao = MedtrackDatabase.getDatabase(context).patientDao()

    // Insert one patient record into the database
    suspend fun insertPatient(patient: Patient){
        patientDao.insertPatient(patient)
    }

    // Return true if the phone number is not already used by another patient
    suspend fun countRepeatedPhoneNumbers(phoneNumber: String): Boolean {
        return patientDao.countRepeatedPhoneNumbers(phoneNumber) == 0
    }

    // Generate a new patient ID by finding the highest existing numeric patient ID
    suspend fun generateNewPatientID(): String {

        // Get all existing patient IDs from the database
        val patientIDs = patientDao.getAllPatientIDs()

        // Use 1000 as the starting number
        var maxID = 1000

        // Check each exiting patient ID to find the highest numeric value
        for (id in patientIDs){
            if (id.startsWith("P")) {
                val patientID = id.substring(1).toIntOrNull()
                if (patientID != null && patientID > maxID) {
                    maxID = patientID
                }
            }
        }

        // Return the next patient ID after the highest existing ID
        return "P${maxID + 1}"
    }

    // Check the patient ID and password during login
    suspend fun login(patientID: String, password: String): String {

        // Find the patient record using the entered patient ID
        val patient = patientDao.getPatientByPatientID(patientID)

        return if(patient == null){
            "account not found"
        }else if(patient.password == null) {
            "account not claimed"
        }else if(patient.password != password){
            "incorrect password"
        } else{
            patient.patientID
        }
    }

    // Claim an existing patient account by checking patient ID and phone number
    suspend fun claimAccount(patientID: String, phoneNumber: String, password: String): String{

        // Find the patient record or stop if the patient ID does not exist
        val patient = patientDao.getPatientByPatientID(patientID) ?: return "account not found"

        if (patient.password != null){
            return "already claimed"
        }
        if(patient.phoneNumber != phoneNumber) {
            return "incorrect phone number"
        }
        patientDao.updateNewPassword(patientID, password)
        return "success"
    }

    // Get one patient record using the patient ID
    suspend fun getPatientByPatientID(patientID: String): Patient? {
        return patientDao.getPatientByPatientID(patientID)
    }
}