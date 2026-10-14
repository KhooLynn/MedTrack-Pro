package com.lynn.s34598901.medtrack

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lynn.s34598901.medtrack.data.symptoms.Symptom
import com.lynn.s34598901.medtrack.data.symptoms.SymptomsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Screen for logging symptoms.
 *
 * This function:
 * 1. Let user choose a symptom category
 * 2. Pick a severity level
 * 3. Add notes
 * 4. Pick a date and time that the symptom occur
 * 5. Save the symptom
 * 6. View previous symptom records
 *
 * It also checks that required fields are filled in before saving.
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomScreen(
    patientID: String,
    modifier: Modifier = Modifier,
    symptomsViewModel: SymptomsViewModel){

    // Stores the selected symptom category
    var symptomsCategory by remember { mutableStateOf("") }

    // Stores the slider value for severity
    var severityRating by remember { mutableFloatStateOf(1f) }

    // Stores user notes
    var notes by remember { mutableStateOf("") }

    // Maximum number of characters for notes
    val maxChar = 200

    val datePicker = rememberDatePickerState()

    // Get current time for the time picker
    val calendar = Calendar.getInstance()
    val timePicker = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    val context = LocalContext.current

    // Load symptom history for the current patient
    val symptoms by symptomsViewModel.getSymptomsByPatientID(patientID).collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Error states
    var categoryError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var futureDateTimeError by remember { mutableStateOf(false) }

    // Change slider colour based on severity
    val sliderDynamicColor = when {
        severityRating <= 3 -> Color.Green
        severityRating <= 6 -> Color.Yellow
        else -> Color.Red
    }

    // Clear all the fields and resets error messages
    fun clearForm(){
        symptomsCategory = ""
        severityRating = 1f
        notes = ""

        // Reset date
        datePicker.selectedDateMillis = null

        categoryError = false
        dateError = false
        futureDateTimeError = false

        // Reset time to current time
        timePicker.hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        timePicker.minute = Calendar.getInstance().get(Calendar.MINUTE)
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)}
    ) {innerPadding ->
        Surface(
            modifier = modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
            color = Color(0xFFF1F7F6)
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F7F6))
                    .verticalScroll(rememberScrollState())
                    .imePadding() // Prevents the keyboard from covering the content when typing
                    .navigationBarsPadding() // Add padding above the bottom navigation bar
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Symptoms",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif)

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ){
                    Text(text = "Log Symptoms",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dropdown for symptom category
                    Text(text = "Symptoms Category",
                        fontSize = 14.sp,
                        color = Color.DarkGray)

                    DropDownMenuSymptoms(selectedType = symptomsCategory, onTypeSelected = {symptomsCategory = it ; categoryError = false})

                    // Show inline error if user does not choose a category
                    if (categoryError) {
                        Text(
                            text = "Please select a symptom category",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Slider for severity from 1 to 10
                    Text(text = "Severity: ${severityRating.toInt()} / 10",
                        fontSize = 14.sp,
                        color = Color.DarkGray)

                    Slider(
                        value = severityRating,
                        onValueChange = { severityRating = it },
                        valueRange = 1f..10f,
                        colors = SliderDefaults.colors(
                            thumbColor = sliderDynamicColor,
                            activeTrackColor = sliderDynamicColor
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notes field with character count
                    Text(text = "Additional notes (${notes.length} / 200)",
                        fontSize = 14.sp,
                        color = Color.DarkGray)

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { if (it.length <= maxChar) notes = it },
                        label = { Text("Notes") },
                        placeholder = {Text("Enter notes")},
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date and time section
                    Text(text = "When did the symptoms occur?",
                        fontSize = 14.sp,
                        color = Color.DarkGray)

                    Text("Date",
                        fontSize = 14.sp,
                        color = Color.DarkGray)
                    DatePicker(state = datePicker)

                    // Show error if date is not selected
                    if (dateError){
                        Text(
                            text = "Please select a date",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Time",
                        fontSize = 14.sp,
                        color = Color.DarkGray)

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TimePicker(state = timePicker,
                            modifier = Modifier.scale(0.9f))
                    }

                    // Show error if selected date and time is in the future
                    if(futureDateTimeError){
                        Text(
                            text = "Future date and time is not allowed.",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Save button checks the form first
                    Button(onClick = {
                        categoryError = symptomsCategory.isBlank()
                        dateError = datePicker.selectedDateMillis == null

                        // Reset error state if date is selected
                        futureDateTimeError = false

                        val pickedDateMillis = datePicker.selectedDateMillis

                        var formattedDateTime = ""

                        // Only check when date is selected
                        if (pickedDateMillis != null) {

                            // Create a Calendar object to store the complete chosen date and time
                            val pickedDateTime = Calendar.getInstance().apply {

                                // Set the Calendar date using the date picked by the user
                                timeInMillis = pickedDateMillis

                                // Set the Calendar hour using the value picked by the user
                                set(Calendar.HOUR_OF_DAY, timePicker.hour)

                                // Set the Calendar minute using the value picked by the user
                                set(Calendar.MINUTE, timePicker.minute)

                                // Set the Calendar second and millisecond to 0
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)

                            }

                            // Get the current date and time
                            val rightNow = Calendar.getInstance()

                            // Check if the chosen date and time is in the future if yes show an error
                            futureDateTimeError = pickedDateTime.timeInMillis > rightNow.timeInMillis

                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            formattedDateTime = formatter.format((pickedDateTime.time))
                        }

                        val isRequired = symptomsCategory.isNotBlank() && datePicker.selectedDateMillis != null && !futureDateTimeError

                        scope.launch {
                            if(isRequired){

                                // Create a new symptom object
                                val symptom = Symptom(
                                    patientID = patientID,
                                    category = symptomsCategory,
                                    severity = severityRating.toInt(),
                                    notes = notes,
                                    dateTime = formattedDateTime
                                )

                                symptomsViewModel.insert(symptom)

                                clearForm()
                                snackbarHostState.showSnackbar("Symptoms saved!")
                            }else{
                                snackbarHostState.showSnackbar("Please fill in all fields")
                            }
                        }
                    },
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text(text = "Save Symptom",
                            fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ){
                    Text(text = "Symptom History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Create a fixed space for the symptom history area so the section remain in same height whether it is empty or has item
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)){

                        // Show message if there is no symptom history
                        if (symptoms.isEmpty()){
                            Text(text = "No symptoms logged yet.")
                        }else {

                            // Show previous symptom records in a scrollable list
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(symptoms.size) { index ->
                                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = severityColor(symptoms[index].severity)
                                        )) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(text = "${symptoms[index].category} | ${symptoms[index].dateTime} | ${symptoms[index].severity} | ${symptoms[index].notes}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

/**
 * Dropdown menu for symptom category.
 *
 * User can choose one symptom type from a fixed list.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenuSymptoms(selectedType: String, onTypeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    // List of category choices shown in the dropdown
    val symptomsCategoryOptions = listOf("Pain", "Nausea", "Dizziness", "Fatigue", "Headache", "Skin Reaction", "Other")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            label = { Text("Symptom Category") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            symptomsCategoryOptions.forEach { choice ->
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


/**
 * Gives a colour based on severity.
 *
 * 1 - 3: Green
 * 4 - 6: Yellow
 * 7 - 10: Red
 *
 */

fun severityColor(severity: Int): Color{
    return when(severity){
        in 1..3 -> Color.Green
        in 4..6 -> Color.Yellow
        else -> Color.Red
    }
}
