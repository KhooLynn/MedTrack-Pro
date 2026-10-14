package com.lynn.s34598901.medtrack.data.clinician

import androidx.room.Dao
import androidx.room.Query
import com.lynn.s34598901.medtrack.data.patients.Patient

/**
 * Data access object for clinician dashboard statistics and patient radar data.
 * This DAO reads existing patient, medication and symptom records to calculate summary values.
 */
@Dao
interface ClinicianDao {

    // Count the total number of patients stored in the database
    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int

    // Calculate the average number of medications per patient, including patients with no medications
    @Query("SELECT COALESCE(AVG(medicationCount), 0.0) FROM(SELECT p.patientID, COUNT(m.patientID) AS medicationCount FROM patients p LEFT JOIN medications m ON p.patientID = m.patientID GROUP BY p.patientID)")
    suspend fun getAverageMedicationCount(): Double

    // Find the most common symptom category across all patients
    @Query("SELECT category FROM symptoms GROUP BY category ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostCommonSymptomCategory(): String?

    // Calculate the average symptom severity across all patients, including patients with no symptoms
    @Query("SELECT COALESCE(AVG(severity), 0.0) FROM symptoms")
    suspend fun getAverageSymptomSeverity(): Double

    // Get all patients from the database
    @Query("SELECT * FROM patients")
    suspend fun getAllPatients(): List<Patient>

    // Count how many medications are recorded for one patient.
    @Query("SELECT COUNT(*) FROM medications WHERE patientID = :patientId")
    suspend fun getMedicationCountForPatient(patientId: String): Int

    // Count how many symptoms are recorded for one patient
    @Query("SELECT COUNT(*) FROM symptoms WHERE patientID = :patientId")
    suspend fun getSymptomCountForPatient(patientId: String): Int

    // Calculate the average symptom severity for one patient
    @Query("SELECT AVG(severity) FROM symptoms WHERE patientID = :patientId")
    suspend fun getAverageSymptomSeverityForPatient(patientId: String): Double
}