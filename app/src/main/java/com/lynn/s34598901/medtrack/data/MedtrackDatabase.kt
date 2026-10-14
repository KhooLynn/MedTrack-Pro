package com.lynn.s34598901.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lynn.s34598901.medtrack.data.clinician.ClinicianDao
import com.lynn.s34598901.medtrack.data.medcoach.MedCoachGenAITips
import com.lynn.s34598901.medtrack.data.medcoach.MedCoachTipsDao
import com.lynn.s34598901.medtrack.data.medications.Medication
import com.lynn.s34598901.medtrack.data.medications.MedicationDao
import com.lynn.s34598901.medtrack.data.patients.Patient
import com.lynn.s34598901.medtrack.data.patients.PatientDao
import com.lynn.s34598901.medtrack.data.symptoms.Symptom
import com.lynn.s34598901.medtrack.data.symptoms.SymptomDao

/**
 * Main Room database for the MedTrack app
 */
@Database(entities = [Patient::class, Medication::class, Symptom::class, MedCoachGenAITips::class], version = 7, exportSchema = false)
abstract class MedtrackDatabase: RoomDatabase(){
    // Provides access to patient database operations
    abstract fun patientDao(): PatientDao

    // Provides access to medication database operations
    abstract fun medicationDao(): MedicationDao

    // Provides access to symptom database operations
    abstract fun symptomDao(): SymptomDao

    // Provides access to MedCoach AI tip database operations
    abstract fun medCoachTipsDao(): MedCoachTipsDao

    // Provides access to clinician dashboard and patient radar queries
    abstract fun clinicianDao(): ClinicianDao

    companion object{

        // Keeps the database instance visible across threads
        @Volatile
        private var Instance: MedtrackDatabase? = null

        // Return the existing database instance or create it if needed
        fun getDatabase(context: Context): MedtrackDatabase{
            return Instance ?: synchronized(this){

                // Build the Room database using the application database name
                Room.databaseBuilder(context, MedtrackDatabase::class.java, "medtrack_database")

                    // Recreate the database if the schema version changes without a migration
                    .fallbackToDestructiveMigration(true)
                    .build()

                    // Save the created database instance for future reuse
                    .also { Instance = it }
            }
        }
    }
}