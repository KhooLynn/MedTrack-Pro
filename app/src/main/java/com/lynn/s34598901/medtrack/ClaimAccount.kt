package com.lynn.s34598901.medtrack

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lynn.s34598901.medtrack.data.patients.PatientsViewModel
import kotlinx.coroutines.launch

/**
 * Shows the claim account screen for the user who already exist in the database but do not have password
 * on the account.
 *
 * This function let the user:
 * 1. Enter their patientID
 * 2. Enter their phone number
 * 3. Enter a password
 * 4. Confirm their password
 * 5. Claim the account
 *
 * It also checks all the input are filled and the patient ID and phone number are correct before claiming the account.
 */
@Composable
fun ClaimAccountScreen(
    modifier: Modifier = Modifier,
    patientsViewModel: PatientsViewModel,
    onClaimSuccess: () -> Unit,
    onBackToLogin: () -> Unit
){
    // Store the current patientID input
    var patientID by remember { mutableStateOf("") }

    // Store the current patient's phone number input
    var phoneNumber by remember { mutableStateOf("") }

    // Store the current password input and also confirm password input
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Error State
    var patientIDBlank by remember { mutableStateOf(false) }
    var phoneNumberBlank by remember { mutableStateOf(false) }
    var passwordBlank by remember { mutableStateOf(false) }
    var passwordMatchError by remember { mutableStateOf(false) }

    // Control snackbars
    val snackbarHostState = remember { SnackbarHostState() }

    // Create and keep a coroutine helper for this screen to show snackbars
    val scope = rememberCoroutineScope()

    Scaffold(
        // Snackbar message has somewhere to appear
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)}
    ){ innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            color = Color(0xFFF1F7F6)
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F7F6))
                    .padding(horizontal = 20.dp)
                    .padding(top = 40.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    /* AI Acknowledgement
                    * The logo used in this MedTrack project was created with the assistance of
                    * generative AI. I selected and integrated the final version into the application
                    * myself.*/
                    painter = painterResource(id = R.drawable.medtrack_logo),
                    contentDescription = "Medtrack Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Claim account heading
                Text(
                    text = "Claim Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                )

                // Small subtitle below the heading
                Text(
                    text = "Set your password to activate your account",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Card-like container that holds the claim account form
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
                ){
                    // Claim account title
                    Text(
                        text = "Claim Account",
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input field for phone number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it ; phoneNumberBlank = false },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {

                            // Error message if the phone number is blank
                            if (phoneNumberBlank){
                                Text(
                                    text = "Phone number cannot be blank",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input field for password, hidden for privacy (masked input)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it ; passwordBlank = false },
                        label = { Text("Password") },
                        placeholder = {Text("Enter your password")},
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {

                            // Error message when the password is blank
                            if(passwordBlank){
                                Text(
                                    text = "Min 8 chars, include a letter and number.",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input field for confirm password, hidden for privacy (masked input)
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; passwordMatchError = false },
                        label = { Text("Confirm Password") },
                        placeholder = {Text("Confirm your password")},
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {

                            // Error message when the passwords do not match
                            if(passwordMatchError){
                                Text(
                                    text = "Passwords do not match or cannot be blank.",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Claim Account Button
                    Button(
                        onClick = {

                            // Check whether the patientID and phone number are blank
                            patientIDBlank = patientID.isBlank()
                            phoneNumberBlank = phoneNumber.isBlank()

                            // Check whether the password follows the required password rule
                            passwordBlank = !isValidPassword(password)

                            // Check whether the confirm password matches the password
                            passwordMatchError = confirmPassword != password || confirmPassword.isBlank()

                            // Combine all field validation results into one value
                            val errorInFields = patientIDBlank || phoneNumberBlank || passwordBlank || passwordMatchError

                            // If all fields are valid, claim the account
                            if(!errorInFields){
                                patientsViewModel.claimAccount(
                                    patientID = patientID,
                                    phoneNumber = phoneNumber,
                                    password = password
                                ){claimResult ->
                                    when(claimResult){

                                        // Show error when the patientID does not exist
                                        "account not found" -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("No account found with this patientID")
                                            }
                                        }

                                        // Show error when the phone number is incorrect
                                        "incorrect phone number" -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Incorrect phone number")
                                            }
                                        }

                                        // Show error when the account is already claimed
                                        "already claimed" -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("This account is already claimed, please proceed to login")
                                            }
                                        }

                                        // Claim successful and move to Login screen
                                        else -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Claim successful!")
                                                onClaimSuccess()
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5F8B))
                    ){
                        Text(
                            text = "Claim Account",
                            fontSize = 16.sp
                        )
                    }

                    // Return to the Login Screen without claiming the account
                    Button(
                        onClick = {
                            onBackToLogin()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Back to Login",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}


