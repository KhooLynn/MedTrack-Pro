package com.lynn.s34598901.medtrack.data.patients

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents one patient record
 */
@Entity(tableName = "patients")
data class Patient(

    // Unique patient ID used as the primary key
    @PrimaryKey
    val patientID: String,

    // Patient's phone number
    val phoneNumber: String,

    // Patient's name
    val name: String,

    // Patient's password which is null before the account is claimed
    val password: String?
)
