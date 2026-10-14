package com.lynn.s34598901.medtrack.data.patients

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO for patient records
 */
@Dao
interface PatientDao {

    // Insert one patient record and replace it if the same patientID already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    // Count how many patients already use the give phone number
    @Query("SELECT COUNT(*) FROM patients WHERE phoneNumber = :phoneNumber")
    suspend fun countRepeatedPhoneNumbers(phoneNumber: String): Int

    // Get all patient IDs currently stored in the database
    @Query("SELECT patientID FROM patients")
    suspend fun getAllPatientIDs(): List<String>

    // Get one patient records using the patient ID
    @Query("SELECT * FROM patients WHERE patientID = :patientID")
    suspend fun getPatientByPatientID(patientID: String): Patient?

    // Update the password for a patient after the account is claimed
    @Query("UPDATE patients SET password = :newPassword WHERE patientID = :patientID")
    suspend fun updateNewPassword(patientID: String, newPassword: String)
}