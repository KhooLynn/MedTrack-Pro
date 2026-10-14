package com.lynn.s34598901.medtrack.data.clinician

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.ai.client.generativeai.GenerativeModel
import com.lynn.s34598901.medtrack.BuildConfig
import com.lynn.s34598901.medtrack.data.MedtrackDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ViewModel for patient radar screens
 */
class PatientRadarRepository(private val applicationContext: Context) {
    private val database = MedtrackDatabase.getDatabase(applicationContext)
    private val clinicianDao = database.clinicianDao()

    // Create the Gemini model used to generate patient radar summaries.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.apiKey
    )

    // Check whether the device currently has an available network connection
    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(): Boolean{
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        // Return false if there is no active network
        val network = connectivityManager.activeNetwork ?: return false

        // Return false if the active network has no usable network capabilities
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    // Build the patient attention radar list for the clinician dashboard
    suspend fun getPatientAttentionRadar(): List<PatientRadarItem>{

        // Get all patients so each patient can be scored
        val patients = clinicianDao.getAllPatients()

        // Create one radar item for each patient and sort the highest priority first
        return patients.map { patient ->

            // Count how many medication this patient has
            val medicationCount = clinicianDao.getMedicationCountForPatient(patient.patientID)

            // Count how many symptoms this patient has
            val symptomCount = clinicianDao.getSymptomCountForPatient(patient.patientID)

            // Get the patient's average symptom severity
            val averageSeverity = clinicianDao.getAverageSymptomSeverityForPatient(patient.patientID)

            // Convert medication count into a score out of 30
            val medicationScore = calculateMedicationScore(medicationCount)

            // Convert symptom count into a score out of 30
            val symptomFrequencyScore = calculateSymptomFrequencyScore(symptomCount)

            // Convert average symptom severity into a score out of 40
            val severityScore = calculateSeverityScore(averageSeverity)

            // Add all three score parts into one overall priority score
            val concernPercentage = medicationScore + severityScore + symptomFrequencyScore

            // Priority score convert into a concern level label
            val concernLevel = when{
                concernPercentage >= 75 -> "Urgent"
                concernPercentage >= 55 -> "High"
                concernPercentage >= 30 -> "Moderate"
                else -> "Low"
            }


            // Create sort reason explaining why the patient received this concern level
            val reason = when{
                concernPercentage >= 75->
                    "This patient has a urgent review priority score and may need closer attention."

                severityScore >= 30 && medicationScore >= 18 ->
                    "High symptoms severity and multiple medications may need closer review."

                severityScore >= 30 ->
                    "Symptom severity is high and may need monitoring."

                medicationScore >= 18 ->
                    "This patient has several medications and may need routine review."

                symptomFrequencyScore >= 18 ->
                    "This patient has reported symptoms several times."

                concernPercentage >= 55 ->
                    "This patient has a high review priority score and may need attention."

                concernPercentage >= 30 ->
                    "This patient has a moderate review priority and should be monitored."

                else ->
                    "Current records show lower concern."
            }

            // Store all calculated radar values for this patient
            PatientRadarItem(
                patientId = patient.patientID,
                patientName = patient.name,
                medicationCount = medicationCount,
                symptomCount = symptomCount,
                averageSeverity = averageSeverity,
                medicationScore = medicationScore,
                severityScore = severityScore,
                symptomFrequencyScore = symptomFrequencyScore,
                concernPercentage = concernPercentage,
                concernLevel = concernLevel,
                reason = reason
            )
        }. sortedByDescending { it.concernPercentage }
    }

    // Concert medication count into a capped score out of 30
    private fun calculateMedicationScore(medicationCount: Int): Int{
        val score = medicationCount * 6
        return  score.coerceAtMost(30)
    }

    // Concert symptom count into a capped score out of 30
    private fun calculateSymptomFrequencyScore(symptomCount: Int): Int{
        val score = symptomCount * 6
        return score.coerceAtMost(30)
    }

    // Concert average symptom severity into a capped score out of 40
    private fun calculateSeverityScore(averageSeverity: Double): Int{

        // Keep the severity value between 0 and 10
        val score = averageSeverity.coerceIn(0.0, 10.0)
        return ((score / 10.0) * 40).toInt()
    }


    // Generate a short AI summary for the patient radar data
    suspend fun generateRadarSummary(
        radarItems: List<PatientRadarItem>
    ): RadarSummaryResult{
        return try {
            if(!isNetworkAvailable()){
                return RadarSummaryResult.NetworkUnavailable
            }

            // Convert the top radar items into readable text for the prompt
            val radarText = if(radarItems.isNotEmpty()) {
                radarItems.take(3).joinToString(separator = "\n") { item ->
                    "${item.patientName} (${item.patientId}) - ${item.concernLevel} concern, " +
                            "priority score ${item.concernPercentage}%, " +
                            "${item.medicationCount} medications, " +
                            "${item.symptomCount} symptoms, " +
                            "average severity %.2f.".format(item.averageSeverity)
                }
            } else{
                "No patient data available."
            }

            // Build the prompt for generating a clinician review-support summary
            val prompt = "This is a clinician review-support dashboard. The radar score is based on medication" +
                    " burden, symptom severity and symptom frequency. It is not a diagnosis and it only helps clinician decide" +
                    " which patient records may need review first. Do not simply repeat the numbers because clinician can already see the graph" +
                    " and the score. Your job is to explain what the pattern may mean and what should be reviewed next\n" +
                    "Top patient radar data: \n" +
                    "$radarText \n" +
                    "Generate a short analysis of the data.\n" +
                    "Requirement:\n" +
                    "1. Explain why they are higher priority using medication count, symptom count or average symptom severity\n" +
                    "2. Do not include any markdown symbol" +
                    "3. Use simple language\n" +
                    "4. Avoid diagnosis or medical advice"

            val response = withContext(Dispatchers.IO){
                generativeModel.generateContent(prompt)
            }

            val summary = response.text

            // Return the summary if Gemini return text
            if(!summary.isNullOrEmpty()){
                RadarSummaryResult.Success(summary)
            }else {
                RadarSummaryResult.Error
            }
        } catch (e: Exception){
            e.printStackTrace()
            RadarSummaryResult.Error
        }
    }

}

/**
 * Represent the result of generating the patient radar summary
 */
sealed class RadarSummaryResult {
    data class Success(val summary: String) : RadarSummaryResult()
    object NetworkUnavailable : RadarSummaryResult()
    object Error : RadarSummaryResult()
}