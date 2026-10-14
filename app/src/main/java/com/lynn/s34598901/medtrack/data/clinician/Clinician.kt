package com.lynn.s34598901.medtrack.data.clinician

/**
 * Stores calculated summary statistics for the clinician dashboard.
 *
 * THis class is not a database table because the values are calculated from patient, medication and
 * symptom records when needed
 */
data class Clinician(

    // Total number of patients in the database
    val patientCount: Int = 0,

    // Average number of medications recorded per patient
    val averageMedicationCount: Double = 0.0,

    // Symptom category that appears most frequent across all symptom records
    val mostCommonSymptomCategory: String = "No symptoms recorded",

    // Average symptom severity across all symptom records
    val averageSymptomSeverity: Double = 0.0
)
