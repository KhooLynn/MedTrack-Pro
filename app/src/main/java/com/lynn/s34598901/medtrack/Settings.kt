package com.lynn.s34598901.medtrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lynn.s34598901.medtrack.data.patients.Patient

/**
 * Screen for settings.
 *
 * This function:
 * 1. Displays the patient's information
 * 2. Allows the user to log in as a clinician
 * 3. Allows the user to log out
 */
@Composable
fun SettingsScreen(
    patient: Patient?,
    onClinicianLogin: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF1F7F6)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Displays the patient's account information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Account Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Displays the patient's information
                    Text(text = "Name: ${patient?.name ?: ""}")
                    Text(text = "Phone Number: ${patient?.phoneNumber ?: ""}")
                    Text(text = "Patient ID: ${patient?.patientID ?: ""}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Open the clinician login dialog
            Button(
                onClick = onClinicianLogin,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Clinician Login",
                    fontSize = 16.sp
                )
            }

            // Logout
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Logout",
                    fontSize = 16.sp
                )
            }
        }
    }
}