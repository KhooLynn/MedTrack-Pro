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
 * Repository for clinician dashboard data and GenAI.
 */
class ClinicianRepository (private val applicationContext: Context){

    // Get the Room database instance for the app
    private val database = MedtrackDatabase.getDatabase(applicationContext)

    // Get the DAO used for clinician statistics and patient radar queries
    private val clinicianDao = database.clinicianDao()

    // Get the total number of patients in the database
    suspend fun getPatientCount(): Int {
        return clinicianDao.getPatientCount()
    }

    // Get the average number of medications recorded per patient
    suspend fun getAverageMedicationCount(): Double {
        return clinicianDao.getAverageMedicationCount()
    }

    // Get the most commonly recorded symptom category
    suspend fun getMostCommonSymptomCategory(): String {
        return clinicianDao.getMostCommonSymptomCategory()?: "No symptoms recorded"
    }

    // Get the average symptom severity across all symptom records
    suspend fun getAverageSymptomSeverity(): Double {
        return clinicianDao.getAverageSymptomSeverity()
    }

    // Create the Gemini model used to generate clinician insights
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

    // Generate three AI insights from the clinician dashboard statistics
    suspend fun generateInsights(stats: Clinician): InsightsResult{
        return try {

            // Stop the request early if there is no internet connection
            if(!isNetworkAvailable()){
                return InsightsResult.NetworkUnavailable
            }

            // Build the prompt using the current clinician dashboard statistics
            val prompt = "You are a clinician and please returns 3 interesting patterns or " +
                    "observations about the data and display these insights in a clear, structured list.\n" +
                    "Requirements: \n" +
                    "1. Do not include introduction\n" +
                    "2. Do not bold any word\n" +
                    "3. Return only three bullet points\n" +
                    "4. Straight to the point\n" +
                    "5. Do not include any markdown symbols\n" +
                    "Patient data summary: \n" +
                    "Total number of patients: ${stats.patientCount}\n" +
                    "Average number of medications per patient: ${stats.averageMedicationCount}\n" +
                    "Most common symptom category: ${stats.mostCommonSymptomCategory}\n" +
                    "Average symptom severity: ${stats.averageSymptomSeverity}\n"


            // Send the prompt to Gemini on the IO dispatcher
            val response = withContext(Dispatchers.IO){
                generativeModel.generateContent(prompt)
            }

            // Read the text returned by Gemini
            val insights = response.text

            // Convert the returned text into a maximum of three insight lines
            if(!insights.isNullOrEmpty()){
                val insights = insights
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .take(3)
                InsightsResult.Success(insights)
            }else {
                InsightsResult.Error
            }
        } catch (e: Exception){

            // Print the exception for debugging and return a safe error state
            e.printStackTrace()
            InsightsResult.Error
        }
    }

}

/**
 * Represents the result of generating clinician dashboard insights
 */
sealed class InsightsResult{
    data class Success(val insights: List<String>): InsightsResult()
    object NetworkUnavailable: InsightsResult()
    object Error: InsightsResult()
}
