package com.lynn.s34598901.medtrack.data

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lynn.s34598901.medtrack.data.medications.Medication
import com.lynn.s34598901.medtrack.data.patients.Patient
import com.lynn.s34598901.medtrack.data.symptoms.Symptom

/**
 * Represent an old patient account saved in Shared Preferences
 */
data class PreviousUser(
    val patientID: String,
    val username: String,
    val phoneNumber: String,
    val password: String
)

/**
 * Represent an old medication record saved in Shared Preferences
 */
data class PreviousAddMedication(
    val patientID: String,
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val time: String,
    val medicationType: String,
    val notes: String
)

/**
 * Seeds the Room database with starting data from CSV files and also migrates older SharedPreferences data
 * into database
 */
class CsvDatabaseSeeder (
    private val context: Context,
    private val database: MedtrackDatabase
) {

    // Seed the database only once using a SharedPreferences flag
    suspend fun seedDatabase() {
        val sharedPreferences = context.getSharedPreferences("seed_prefs", Context.MODE_PRIVATE)
        val isSeeded = sharedPreferences.getBoolean("db_seeded_v10", false)

        if (!isSeeded) {

            // Insert starting patient records from the CSV file
            seedPatients()

            // Insert starting medication records from the CSV file
            seedMedications()

            // Insert starting symptom records from the CSV file
            seedSymptoms()

            // Move old signed up users from SharedPreferences into Room
            signUpFromSharedPreferences()

            // Move old added medications from SharedPreferences into Room
            addMedicationsFromSharedPreferences()

            // Mark the database aas seeded so this process does not repeat
            sharedPreferences.edit { putBoolean("db_seeded_v10", true) }
        }
    }

    // Load patient records from patients.csv and insert them into the database
    private suspend fun seedPatients() {
        val inputStream = context.assets.open("patients.csv")
        val reader = inputStream.bufferedReader()
        reader.useLines { lines ->

            // Skip the header row and read each patient line
            lines.drop(1).forEach { line ->
                val values = line.split(",")

                // Create a patient with no password
                val patient = Patient(
                    patientID = values[0].trim(),
                    phoneNumber = values[1].trim(),
                    name = values[2].trim(),
                    password = null
                )

                // Insert the patient into the database
                database.patientDao().insertPatient(patient)
            }
        }
    }

    // Load medication records from medications.csv and insert them into the database
    private suspend fun seedMedications() {
        val inputStream = context.assets.open("medications.csv")
        val reader = inputStream.bufferedReader()
        reader.useLines { lines ->

            lines.drop(1).forEach { line ->
                val values = line.split(",")

                // Create a medication record from the CSV columns
                val medication = Medication(
                    patientID = values[0].trim(),
                    name = values[1].trim(),
                    dosage = values[2].trim(),
                    frequency = values[3].trim(),
                    time = values[4].trim(),
                    type = values[5].trim(),
                    notes = values[6].trim()
                )

                // Insert the medication into the database
                database.medicationDao().insertMedication(medication)
            }
        }
    }

    // Load symptom records from symptoms.csv and insert them into the database
    private suspend fun seedSymptoms(){
        val inputStream = context.assets.open("symptoms.csv")
        val reader = inputStream.bufferedReader()

        reader.useLines { lines ->
            lines.drop(1).forEach { line ->

                // Skip empty CSV lines
                if (line.isBlank()) {
                    return@forEach
                }

                val values = line.split(",")

                // Skip invalid lines that do not have enough columns
                if (values.size < 5) {
                    return@forEach
                }

                // Create a symptom record and keep notes safe even if they contain commas
                val symptom = Symptom(
                    patientID = values[0].trim(),
                    category = values[1].trim(),
                    severity = values[2].trim().toInt(),
                    notes = values.subList(3, values.size - 1).joinToString(",").trim(),
                    dateTime = values[values.size - 1].trim()
                )

                database.symptomDao().insertSymptom(symptom)
            }
        }
    }

    // Move old user account data from SharedPreferences into the patient table
    private suspend fun signUpFromSharedPreferences(){
        val sharedPreferences = context.getSharedPreferences("MedTrackPreferences", Context.MODE_PRIVATE)
        val usersJson = sharedPreferences.getString("users", null) ?: return

        val gson = Gson()
        val type = object : TypeToken<MutableList<PreviousUser>>() {}.type
        val previousUsers: MutableList<PreviousUser> = gson.fromJson(usersJson, type) ?: mutableListOf()

        // Convert each previous user into a Patient entity
        for (user in previousUsers) {
            val patient = Patient(
                patientID = user.patientID,
                phoneNumber = user.phoneNumber,
                name = user.username,
                password = user.password
            )

            database.patientDao().insertPatient(patient)
        }
    }

    // Move old medication data from SharedPreferences into the medication table
    private suspend fun addMedicationsFromSharedPreferences(){
        val sharedPreferences = context.getSharedPreferences("MedTrackPreferences", Context.MODE_PRIVATE)
        val medicationsJson = sharedPreferences.getString("medications", null) ?: return

        val gson = Gson()
        val type = object : TypeToken<MutableList<PreviousAddMedication>>() {}.type
        val previousMedications: MutableList<PreviousAddMedication> = gson.fromJson(medicationsJson, type) ?: mutableListOf()

        // Convert each previous medication into a Medication entity
        for (medication in previousMedications) {
            val med = Medication(
                patientID = medication.patientID,
                name = medication.medicationName,
                dosage = medication.dosage,
                frequency = medication.frequency,
                time = medication.time,
                type = medication.medicationType,
                notes = medication.notes
            )

            database.medicationDao().insertMedication(med)
        }
    }
}