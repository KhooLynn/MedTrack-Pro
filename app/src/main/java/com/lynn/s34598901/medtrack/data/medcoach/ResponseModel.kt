package com.lynn.s34598901.medtrack.data.medcoach

/**
 * Represents the full response returned from the openFDA drug label API
 */
data class ResponseModel(

    // List of drug label results returned by the API
    val results: List<DrugResult>
)

/**
 * Represents one drug label result returned from openFDA
 */
data class DrugResult(

    // Purpose of the drug
    val purpose: List<String>?,

    // Warning information for the drug
    val warnings: List<String>?,

    // Dosage and administration instructions for the drug
    val dosage_and_administration: List<String>?,

    // Information about when the user should stop using the drug
    val stop_use: List<String>?
)
