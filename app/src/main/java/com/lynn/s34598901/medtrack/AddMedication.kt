package com.lynn.s34598901.medtrack

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Calendar
import com.lynn.s34598901.medtrack.data.medications.Medication
import com.lynn.s34598901.medtrack.data.medications.MedicationsViewModel

/**
 * Screen for adding a new medication.
 * The user enters the details and then saves them.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    modifier: Modifier = Modifier,
    patientID: String,
    onMedicationSaved: () -> Unit,
    medicationsViewModel: MedicationsViewModel) {

    // Text fields values
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var medicationType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Get current time for the time picker
    val calendar = Calendar.getInstance()
    val timePicker = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    // Error states
    var medicationNameError by remember { mutableStateOf(false) }
    var frequencyError by remember { mutableStateOf(false) }
    var dosageError by remember { mutableStateOf(false) }
    var dosageFormatError by remember { mutableStateOf(false) }
    var medicationTypeError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Clear all the fields and resets error messages
    fun clearForm(){
        medicationName = ""
        dosage = ""
        frequency = ""
        medicationType = ""
        notes = ""

        medicationNameError = false
        frequencyError = false
        dosageError = false
        dosageFormatError = false
        medicationTypeError = false

        // Reset time to current time
        timePicker.hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        timePicker.minute = Calendar.getInstance().get(Calendar.MINUTE)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)}
    ) { innerPadding ->
        Surface(
            modifier = modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
            color = Color(0xFFF1F7F6)
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F7F6))
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Medication",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif)

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ){

                    // Medication name field
                    Text(text = "Medication Name",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        color = Color.DarkGray)

                    OutlinedTextField(
                        value = medicationName,
                        onValueChange = { medicationName = it },
                        placeholder = {Text("Enter medication name")},
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {

                            //Error message when the medication name is blank
                            if(medicationNameError){
                                Text(
                                    text = "Medication name cannot be blank",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Dosage field
                    Text(text = "Dosage",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        color = Color.DarkGray)

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it ; dosageError = false ; dosageFormatError = false },
                        placeholder = {Text("Enter dosage")},
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            when{

                                // Error message when the dosage is blank
                                dosageError ->{
                                    Text(
                                        text = "Dosage cannot be blank",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Error message when the dosage is in an invalid format
                                dosageFormatError -> {
                                    Text(
                                        text = "Invalid dosage format, please include unit (mg, ml, g)",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Drop down menu for frequency
                    Text(text = "Frequency",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        color = Color.DarkGray)
                    DropDownMenuFrequency(selectedFrequency = frequency, onFrequencySelected = {frequency = it ; frequencyError = false})

                    if(frequencyError){
                        Text(
                            text = "Please select a frequency",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 2.dp),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Time picker
                    Text(
                        text = "Time",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TimePicker(
                            state = timePicker,
                            modifier = Modifier.scale(0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Drop down menu for medication type
                    Text(text = "Medication Type",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        color = Color.DarkGray)
                    DropDownMenuType(selectedType = medicationType, onTypeSelected = {medicationType = it ; medicationTypeError = false})

                    // Error message when the medication type is not selected
                    if(medicationTypeError){
                        Text(
                            text = "Please select a medication type",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 2.dp),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Optional notes
                    Text(text = "Notes (Optional)",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        color = Color.DarkGray)

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        placeholder = {Text("Enter notes")},
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save button
                    Button(onClick = {
                        medicationNameError = medicationName.isBlank()
                        dosageError = dosage.isBlank()
                        frequencyError = frequency.isBlank()
                        medicationTypeError = medicationType.isBlank()
                        dosageFormatError = !isValidDosage(dosage) && dosage.isNotBlank()

                        scope.launch {
                            when {
                                medicationNameError || frequencyError || dosageError || dosageFormatError || medicationTypeError -> {
                                    snackbarHostState.showSnackbar("Please fill in all fields")
                                }
                                else -> {

                                    // Create a new medication object
                                    val medication = Medication(
                                        patientID = patientID,
                                        name = medicationName,
                                        dosage = dosage,
                                        frequency = frequency,
                                        time = String.format("%02d:%02d", timePicker.hour, timePicker.minute),
                                        type = medicationType,
                                        notes = notes)

                                    medicationsViewModel.insert(medication)

                                    // Clear form after saving
                                    clearForm()
                                    snackbarHostState.showSnackbar("Medication saved!")

                                    // Go back to Home Screen after saving
                                    onMedicationSaved()
                                }
                            }
                        }
                    },
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text(text = "Save Medication",
                            fontSize = 16.sp)
                    }

                    // Clear Button
                    Button(
                        onClick = {
                            clearForm()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Clear",
                            fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/**
 * Checks if the dosage is in a valid form.
 * E.g.: 5mg, 5ml,5g
 */
fun isValidDosage(dosage: String): Boolean{
    val dosageUnit = Regex("^\\d+(\\.\\d+)?(mg|ml|g)$", RegexOption.IGNORE_CASE)
    return dosageUnit.matches(dosage.trim())
}

/**
 * Dropdown menu for frequency
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenuFrequency(selectedFrequency: String, onFrequencySelected: (String) -> Unit){
    var expanded by remember { mutableStateOf(false) }

    val frequencyOptions = listOf("Once daily", "Twice daily", "Three times daily", "As needed")

    // This box manages opening and closing the dropdown
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedFrequency,

            // User cannot type here
            onValueChange = {},
            placeholder = { Text("Frequency") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ){
            frequencyOptions.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(choice) },
                    onClick = {
                        onFrequencySelected(choice)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Dropdown menu for medication type
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenuType(selectedType: String, onTypeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val typeOptions = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            placeholder = { Text("Medication Type") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            typeOptions.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(choice) },
                    onClick = {
                        onTypeSelected(choice)
                        expanded = false
                    }
                )
            }
        }
    }
}
