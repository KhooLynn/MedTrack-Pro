package com.lynn.s34598901.medtrack.data.medcoach

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.lynn.s34598901.medtrack.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.lynn.s34598901.medtrack.data.MedtrackDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Repository for MedCoach drug information and AI medication tips
 */

class MedCoachRepository (private val applicationContext: Context){

    // Create Retrofit for calling the openFDA API
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.fda.gov/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val database = MedtrackDatabase.getDatabase(applicationContext)
    private val medCoachTipDao = database.medCoachTipsDao()

    // Create the API service used for openFDA drug label searches
    private val apiService = retrofit.create(MedCoachApiService::class.java)

    // Create the Gemini model used to generate medication tips
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.apiKey
    )

    /**
     * Retrofit API interface for searching drug label information from openFDA
     */
    interface MedCoachApiService {
        @GET("drug/label.json")

        // Search openFDA drug labels using the given query and result limit
        suspend fun searchDrug(
            @Query("search") query: String,
            @Query("limit") limit: Int = 1
        ): Response<ResponseModel>
    }

    // Searches drug information from openFDA using the generic name first then brand name
    suspend fun searchDrug(drugName: String): DrugInfoResult {
        return try {
            if(!isNetworkAvailable()){
                return DrugInfoResult.NetworkUnavailable
            }

            // Clean the drug name before placing it into the openFDA search query
            val formattedDrugName = drugName.trim().replace("\"", "")

            // Search the drug by generic name in the openFDA drug label data
            val genericSearch = "openfda.generic_name:\"$formattedDrugName\""

            // Send the openFDA request on the IO dispatcher
            val genericResponse = withContext(Dispatchers.IO){
                apiService.searchDrug(
                    query = genericSearch,
                    limit = 1
                )
            }

            // Get the first generic name result if the response is successful
            val genericDrug = if(genericResponse.isSuccessful){
                genericResponse.body()?.results?.firstOrNull()
            }else{
                null
            }

            // Return the generic name result if one is found
            if(genericDrug != null){
                return DrugInfoResult.Success(genericDrug)
            }

            // Search the drug by brand name if the generic name search does not have a result
            val brandSearch = "openfda.brand_name:\"$formattedDrugName\""

            // Send the brand name search request on the IO dispatcher
            val brandResponse = withContext(Dispatchers.IO){
                apiService.searchDrug(
                    query = brandSearch,
                    limit = 1
                )
            }

            // Get the first brand name result if the response is successful
            val brandDrug = if(brandResponse.isSuccessful){
                brandResponse.body()?.results?.firstOrNull()
            }else{
                null
            }

            // Return the brand name result if one is found
            if(brandDrug != null){
                return DrugInfoResult.Success(brandDrug)
            }

            // Return not found if both generic name and brand name searches fail
            DrugInfoResult.NotFound

        } catch (e: Exception){
            e.printStackTrace()
            DrugInfoResult.Error
        }
    }

    // Check the internet connection of the device
    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(): Boolean{
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    // Generates a personalized AI medication tip for a patient
    suspend fun generateMedicationTip(
        patientID: String,
        medicationName: List<String>,
        symptomName: List<String>): TipResult{
        return try{
            if(!isNetworkAvailable()){
                return TipResult.NetworkUnavailable
            }

            // Convert the patient's medication list into readable text for the prompt
            val medicationInfo = if(medicationName.isNotEmpty()){
                medicationName.joinToString(", ")
            }else{
                "No saved medications"
            }

            // Convert the patient's symptom list into readable text for the prompt
            val symptomInfo = if (symptomName.isNotEmpty()) {
                symptomName.joinToString(separator = ", ")
            } else {
                "No recent symptoms recorded"
            }

            // Build the prompt for generating a short personalized medication tip
            val prompt = "Generate a short encouraging message to help someone stay on top of their medication schedule.\n" +
                    "Patient medications: $medicationInfo \n" +
                    "Recent symptoms: $symptomInfo \n" +
                    "Make it personalised"

            val response = withContext(Dispatchers.IO){
                generativeModel.generateContent(prompt)
            }

            val generateTip = response.text

            if(!generateTip.isNullOrEmpty()){
                val tip = MedCoachGenAITips(

                    // Create a saved tip record for the current patient
                    patientId = patientID,
                    content = generateTip
                )

                medCoachTipDao.insertTip(tip)

                TipResult.Success(generateTip)
            }else{
                TipResult.Error
            }
        } catch (e: Exception){
            e.printStackTrace()
            TipResult.Error
        }
    }

    // Get all saved AI medication tips for one patient
    fun getTipsByPatientID(patientID: String): Flow<List<MedCoachGenAITips>> {
        return medCoachTipDao.getTipsForPatient(patientID)
    }
}


/**
 * Represents the result of searching drug information from openFDA
 */
sealed class DrugInfoResult{
    data class Success(val drug: DrugResult): DrugInfoResult()
    object NotFound: DrugInfoResult()
    object NetworkUnavailable: DrugInfoResult()
    object Error: DrugInfoResult()
}

/**
 * Represents the result of searching drug information from openFDA
 */
sealed class TipResult{
    data class Success(val content: String): TipResult()
    object NetworkUnavailable: TipResult()
    object Error: TipResult()
}