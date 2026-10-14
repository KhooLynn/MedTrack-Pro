package com.lynn.s34598901.medtrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lynn.s34598901.medtrack.data.medcoach.DrugResult
import com.lynn.s34598901.medtrack.data.medcoach.MedCoachGenAITips
import com.lynn.s34598901.medtrack.data.medcoach.MedCoachTipUIState
import com.lynn.s34598901.medtrack.data.medcoach.MedCoachUIState
import com.lynn.s34598901.medtrack.data.medcoach.MedCoachViewModel
import com.lynn.s34598901.medtrack.data.patients.Patient

/**
 * Screen for MedCoach.
 *
 * This function:
 * 1. Allows the user to search for a medication
 * 2. Allows the user to generate medication tips
 * 3. Allows the user to view previous medication tips
 *
 */
@Composable
fun MedCoachScreen(
    patient: Patient?,
    medCoachViewModel: MedCoachViewModel,
    modifier: Modifier = Modifier
){
    // Store the name of the drug entered by the user
    var drugName by remember { mutableStateOf("") }

    // Collect the current drug search state from the view model
    val uiState by medCoachViewModel.uiState.collectAsState()

    // Collect the patient's medication history from the view model
    val patientMedications by medCoachViewModel.patientMedications.collectAsState()

    // Collect the current tips state from the view model
    val tipUIState by medCoachViewModel.tipUIState.collectAsState()

    // Collect the previous generated AI tips history from the view model
    val tipHistory by medCoachViewModel.tipHistory.collectAsState()

    // Control whether the tip dialog is shown
    var showDialog by remember { mutableStateOf(false) }

    // Load all the data when the patient ID changes
    LaunchedEffect(patient?.patientID){
        patient?.patientID?.let { patientID ->
            medCoachViewModel.loadPatientMedications(patientID)
            medCoachViewModel.loadPatientSymptoms(patientID)
            medCoachViewModel.loadTipHistory(patientID)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = Color(0xFFF1F7F6)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F7F6))
                .padding(horizontal = 20.dp)
                .padding(top = 15.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "MedCoach",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Card for searching drug information from the typed or selected medication name
            DrugInfoCard(
                drugName = drugName,
                onDrugNameChange = { drugName = it },
                medicationNames = patientMedications,
                onMedicationSelected = { selectedMedication ->
                    drugName = selectedMedication
                },
                onSearchClick = {
                    medCoachViewModel.searchDrug(drugName)
                },
                uiState = uiState
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Card for generating and viewing AI medication tips
            GenAiCard(
                patientID = patient?.patientID ?: "",
                tipUIState = tipUIState,
                tipHistory = tipHistory,
                showDialog = showDialog,
                onGenerateTipClick = {
                    medCoachViewModel.generateTip(patient?.patientID ?: "")
                },
                onShowAllTipsClick = {
                    showDialog = true
                },
                onDismissDialog = {
                    showDialog = false
                }
            )
        }
    }
}

/**
 * Show the drug information card.
 *
 * The user can type a medication name, choose from their own medication, search drug information and
 * view the search result state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugInfoCard(
    drugName: String,
    onDrugNameChange: (String) -> Unit,
    medicationNames: List<String>,
    onMedicationSelected: (String) -> Unit,
    onSearchClick: () -> Unit,
    uiState: MedCoachUIState,
){
    // Control whether the dropdown menu is expanded
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(16.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ){
            Text(
                text = "Drug Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = drugName,
                onValueChange = onDrugNameChange,
                label = { Text("Drug Name") },
                placeholder = { Text("Search for a medication") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Show medication taken by patient in dropdown menu only when the patient has taken medication
            if(medicationNames.isNotEmpty()){
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = drugName,
                        onValueChange = onDrugNameChange,
                        label = { Text("Your medications") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ){
                        medicationNames.forEach { medicationName ->
                            DropdownMenuItem(
                                text = { Text(medicationName) },
                                onClick = {

                                    // Use the selected medication name as the search text
                                    onMedicationSelected(medicationName)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search for drug information using the current medication name
            Button(
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = "Search",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Area that displays the current drug search state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.TopStart
            ){
                when (uiState){

                    // Show instruction text before the user clicks the button
                    MedCoachUIState.Initial -> {
                        Text(
                            text = "Search for a medication to check the drug information.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }

                    // Show loading indicator while the drug is being searched
                    MedCoachUIState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Show the drug search result
                    is MedCoachUIState.Success -> {
                        ShowDrugResult(
                            drug = uiState.drug
                        )
                    }

                    // Show error message if the drug search fails
                    is MedCoachUIState.Error -> {
                        Text(
                            text = "Error: ${uiState.errorMessage}"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays the drug information returned from the search result.
 * The result can include purpose, warnings, dosage and administration, and stop use information.
 */
@Composable
fun ShowDrugResult(
    drug: DrugResult
){
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        item {
            Text(
                text = "Purpose: ",
                fontWeight = FontWeight.Bold
            )

            // Show drug purpose if available, otherwise show "Not Available"
            Text(
                text = drug.purpose?.firstOrNull() ?: "Not Available"
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Warnings: ",
                fontWeight = FontWeight.Bold
            )

            // Show drug warnings if available, otherwise show "Not Available"
            Text(
                text = drug.warnings?.firstOrNull() ?: "Not Available"
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Dosage and administration: ",
                fontWeight = FontWeight.Bold
            )

            // Show drug dosage and administration if available, otherwise show "Not Available"
            Text(
                text = drug.dosage_and_administration?.firstOrNull() ?: "Not Available"
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Stop use: ",
                fontWeight = FontWeight.Bold
            )

            // Show drug stop use if available, otherwise show "Not Available"
            Text(
                text = drug.stop_use?.firstOrNull() ?: "Not Available"
            )
        }
    }
}

/**
 * Show the AI generated medication tips.
 *
 * This card lets the patient generate a new medication tip and open a dialog containing
 * previous generated tips.
 */
@Composable
fun GenAiCard(
    patientID: String,
    tipUIState: MedCoachTipUIState,
    tipHistory: List<MedCoachGenAITips>,
    showDialog: Boolean,
    onGenerateTipClick: () -> Unit,
    onShowAllTipsClick: () -> Unit,
    onDismissDialog: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(16.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Generate Medication Tips",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Generate a new medication tip
            Button(
                onClick = onGenerateTipClick,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = "Generate Tips",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Show all previous medication tips
            Button(
                onClick = onShowAllTipsClick,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = "Show All Tips",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Area that displays the AI generated tips
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.TopStart
            ){
                when(tipUIState){

                    // Show instruction text before the user clicks the button
                    MedCoachTipUIState.Initial -> {
                        Text(
                            text = "Generate a tip to help you stay on top of your medication schedule.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }

                    // Show loading indicator while the AI is generating tips
                    MedCoachTipUIState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Show the AI generated tips
                    is MedCoachTipUIState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item{
                                Text(
                                    text = tipUIState.content
                                )
                            }
                        }
                    }

                    // Show error message if the AI fails to generate tips
                    is MedCoachTipUIState.Error -> {
                        Text(
                            text = "Error: ${tipUIState.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    // Show the previous medication tips
    if (showDialog){
        TipDialog(
            tips = tipHistory,
            onDismissDialog = onDismissDialog
        )
    }
}

/**
 * Dialog to show the previous medication tips.
 */
@Composable
fun TipDialog(
  tips: List<MedCoachGenAITips>,
  onDismissDialog: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismissDialog,
        title = { Text(text = "Previous MedCoach Tips") },
        text = {

            // Show an empty message if there are no previous tips
            if (tips.isEmpty()){
                Text(text = "No tips have been generated yet.")
            }else{

                // Show the previous tips
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ){
                    items(tips.size){ index ->

                        // Display the tip number in reverse order
                        Text(text = "Tip ${tips.size - index}",
                            fontWeight = FontWeight.Bold)

                        // Display the tip content
                        Text(text = tips[index].content)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissDialog
            ){
                Text(text = "Close")
            }
        }
    )
}