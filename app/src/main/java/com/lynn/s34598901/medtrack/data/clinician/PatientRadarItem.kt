package com.lynn.s34598901.medtrack.data.clinician

/**
 * Store the calculated patient priority data for the Patient Radar screen
 */
data class PatientRadarItem(

    // PatientID
    val patientId: String,

    // Patient Name
    val patientName: String,

    // Number of medications recorded for this patient
    val medicationCount: Int,

    // Number of symptoms recorded for this patient
    val symptomCount: Int,

    // Average symptom severity recorded for this patient
    val averageSeverity: Double,

    // Score calculated from the patient's medication count
    val medicationScore: Int,

    // Score calculated from the patient's average symptom severity
    val severityScore: Int,

    // Score calculated from the patient's symptom count
    val symptomFrequencyScore: Int,

    // Overall priority score calculated from all radar score
    val concernPercentage: Int,

    // Concern label shown to the clinician
    val concernLevel: String,

    // Short explanation for why this patient received the concern level
    val reason: String
)
