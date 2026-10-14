package com.lynn.s34598901.medtrack.data.medcoach

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lynn.s34598901.medtrack.data.patients.Patient

/**
 * Represents a saved AI-generated medication tips for a patient
 */
@Entity(tableName = "medcoach_genai_tips", foreignKeys = [ForeignKey(entity = Patient::class,
    parentColumns = ["patientID"], childColumns = ["patientId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("patientId")])
data class MedCoachGenAITips(

    // Unique ID for each generated tip record
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Patient ID linked to the patient who received this tip
    val patientId: String,

    // AI generated medication tip content
    val content: String,

    // Time when the tip was created, stored in milliseconds
    val timestamp: Long = System.currentTimeMillis()
)
