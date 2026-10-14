package com.lynn.s34598901.medtrack

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lynn.s34598901.medtrack.data.patients.PatientsViewModel

/**
 * Shows the login form for the user.
 *
 * The user needs to enter a patientID and password.
 * If the patientID and the password matched, the patient ID is saved in SharedPreferences and the user is
 * taken to the Home screen.
 *
 */

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String) -> Unit,
    patientsViewModel: PatientsViewModel
) {

    // Store the current patientID input
    var patientID by remember { mutableStateOf("") }

    // Store the current password input
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Error State
    var patientIDBlank by remember { mutableStateOf(false) }
    var passwordBlank by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        color = Color(0xFFF1F7F6)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                /* AI Acknowledgement
                * The logo used in this MedTrack project was created with the assistance of
                * generative AI. I selected and integrated the final version into the application
                * myself.*/
                painter = painterResource(id = R.drawable.medtrack_logo),
                contentDescription = "Medtrack Logo",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Welcome heading
            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )

            // Small subtitle below the heading
            Text(
                text = "Log in to continue to MedTrack",
                fontSize = 18.sp,
                color = Color.Gray,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Card-like container that holds the login form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(20.dp)
                    ).padding(20.dp)
            ) {
                // Login title
                Text(
                    text = "Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif)

                Spacer(modifier = Modifier.height(16.dp))


                // Input field for patientID
                OutlinedTextField(
                    value = patientID,
                    onValueChange = { patientID = it ; patientIDBlank = false },
                    label = { Text("PatientID") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {

                        // Error message if the patientID is blank
                        if (patientIDBlank){
                            Text(
                                text = "PatientID cannot be blank",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input field for password, hidden for privacy (masked input)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it ; passwordBlank = false},
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {

                        // Error message if the password is blank
                        if (passwordBlank){
                            Text(
                                text = "Password cannot be blank",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {

                        // Check if the fields are blank
                        patientIDBlank = patientID.isBlank()
                        passwordBlank = password.isBlank()


                        // Check that both fields are filled before logging in
                        if (patientIDBlank || passwordBlank){
                           Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
                        }else{

                            // Validate the login details using the ViewModel
                            patientsViewModel.login(patientID, password){ loginResult ->
                                when (loginResult){

                                    // Show error when the patientID does not exist
                                    "account not found" -> {
                                        Toast.makeText(context, "No account found with this patientID", Toast.LENGTH_SHORT).show()
                                    }

                                    // Show error when the password is incorrect
                                    "incorrect password" -> {
                                        Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                                    }

                                    // Show error when the account is not claimed
                                    "account not claimed" -> {
                                        Toast.makeText(context, "This account is not claimed", Toast.LENGTH_SHORT).show()
                                    }

                                    // Login successful
                                    else -> {
                                        onLoginSuccess(loginResult)
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text(text = "Login",
                        fontSize = 16.sp)
                }
            }

        }


    }
}



