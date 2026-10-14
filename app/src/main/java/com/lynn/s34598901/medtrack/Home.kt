package com.lynn.s34598901.medtrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lynn.s34598901.medtrack.data.medications.Medication
import com.lynn.s34598901.medtrack.data.medications.MedicationsViewModel
import java.util.Calendar
import androidx.compose.ui.platform.LocalLocale

/**
 * Shows the main Home screen for the logged in patient.
 *
 * This function:
 * 1. Displays the greeting message with the patient's name
 * 2. Patient ID
 * 2. Current date
 * 3. Medication progress summary
 * 4. List of medications with checkboxes
 * 5. Add Medication button
 * 6. Logout button
 *
 */

@Composable
fun HomeScreen(
    patientName: String,
    patientID: String,
    medications: List<Medication>,
    onAddMedicationClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    medicationsViewModel: MedicationsViewModel
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF1F7F6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Reset the medication taken status when the Home's screen is first opened.
            LaunchedEffect(Unit) {
                medicationsViewModel.resetTakenStatus()
            }

            // Get current date in string format (Monday, 12 March 2023)
            val dateTime = Calendar.getInstance().time
            val format = java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", LocalLocale.current.platformLocale)
            val currentDateTime = format.format(dateTime)

            // Count how many medications are marked as taken today
            val takenCount = medications.count { it.isTaken }

            Text(text = "Hello, $patientName",
                style = typography.headlineSmall)

            Text(text = "PatientID: $patientID",
                style = typography.bodyLarge,
                color = Color.Gray)

            Text(text = currentDateTime,
                style = typography.bodyMedium,
                color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            // Summary card showing daily medication progress
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "$takenCount of ${medications.size} medications taken today",
                    modifier = Modifier.padding(16.dp),
                    style = typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (medications.isEmpty()) {
                Text(text = "No medications scheduled.")
            } else {

                // Show all medications in a scrollable list and it will fill the remaining space in parent layout
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(medications.size) { index ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${medications[index].name} ${medications[index].dosage} ${medications[index].frequency} ${medications[index].time}",
                                    modifier = Modifier.weight(1f),
                                    style = typography.bodyLarge,

                                    // Strike through text if medication is marked as taken
                                    textDecoration = if (medications[index].isTaken) {
                                        TextDecoration.LineThrough
                                    } else {
                                        TextDecoration.None
                                    }
                                )

                                Checkbox(
                                    checked = medications[index].isTaken,

                                    // Toggle checkbox state when user clicks
                                    onCheckedChange = { checked ->
                                        medicationsViewModel.updateStatus(
                                            medication = medications[index],
                                            isTaken = checked
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button to open the Add Medication screen
            Button(
                onClick = onAddMedicationClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Add Medication",
                    fontSize = 16.sp)
            }

            //Button to log out the current user
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Logout",
                    fontSize = 16.sp)
            }
        }
    }
}
