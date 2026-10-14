package com.lynn.s34598901.medtrack

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lynn.s34598901.medtrack.data.patients.PatientsViewModel
import kotlinx.coroutines.launch

/**
 * Sign Up screen for creating a new account.
 *
 * This function let the user:
 * 1. Enter their full name
 * 2. Enter their phone number
 * 3. Enter a password
 * 4. Confirm their password
 *
 * It also checks all the input are filled and the phone number is unique before saving the new user.
 *
 */

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    patientsViewModel: PatientsViewModel) {

    // Text field values entered by the user
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Error states
    var fullNameError by remember { mutableStateOf(false) }
    var phoneNumberError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var phoneNumberUniquenessError by remember { mutableStateOf(false) }

    // Control snackbars
    val snackbarHostState = remember { SnackbarHostState() }

    // Create and keep a coroutine helper for this screen to show snackbars
    val scope = rememberCoroutineScope()

    // Store the new patient ID returned after signing up
    var newPatientID by remember {mutableStateOf<String?>(null)}

    // Control the visibility of the new patient ID dialog
    var showNewPatientID by remember {mutableStateOf(false)}

    Scaffold(

        // Snackbar message has somewhere to appear
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)}
    ) { innerPadding ->
        Surface(
            modifier = modifier.fillMaxSize()
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

                // App logo shown at the top of the screen
                androidx.compose.foundation.Image(
                    /* AI Acknowledgement
                    * The logo used in this MedTrack project was created with the assistance of
                    * generative AI. I selected and integrated the final version into the application
                    * myself.*/
                    painter = painterResource(id = R.drawable.medtrack_logo),
                    contentDescription = "Medtrack Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Welcome heading
                Text(
                    text = "Welcome to MedTrack",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                )

                // Small subtitle below the heading
                Text(
                    text = "Sign in to create a new account",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(20.dp))

                // White card area for the sign up form
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

                    // Sign up title inside the form
                    Text(text = "Sign Up",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full name field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; fullNameError = false },
                        label = { Text("Full Name") },
                        placeholder = {Text("Enter your full name")},
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {

                            // Error message when the full name is blank
                            if(fullNameError){
                                Text(
                                    text = "Full name cannot be blank",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Phone number field
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it  ; phoneNumberError = false ; phoneNumberUniquenessError = false },
                        label = { Text("Phone Number") },
                        placeholder = {Text("Enter your phone number")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {

                            // Error message when the phone number is blank
                            if (phoneNumberError){
                                Text(
                                    text = "Must start with 04 and be 10 digits",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Error message when the phone number is not unique
                            if (phoneNumberUniquenessError){
                                Text(
                                    text = "Phone number already exists.",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8. dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it ; passwordError = false },
                        label = { Text("Password") },
                        placeholder = {Text("Enter your password")},
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {

                            // Error message when the password is blank
                            if(passwordError){
                                Text(
                                    text = "Min 8 chars, include a letter and number.",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8. dp))

                    // Confirm password field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmPasswordError = false },
                        label = { Text("Confirm Password") },
                        placeholder = {Text("Confirm your password")},
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {

                            // Error message when the passwords do not match
                            if(confirmPasswordError){
                                Text(
                                    text = "Passwords do not match or cannot be blank.",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sign Up Button
                    Button(
                        onClick = {

                            // Check whether the full name field is blank
                            fullNameError = fullName.isBlank()

                            // Validate the phone number and password
                            phoneNumberError = !isValidPhoneNumber(phoneNumber)
                            passwordError = !isValidPassword(password)

                            // Check whether the passwords match
                            confirmPasswordError = confirmPassword != password || confirmPassword.isBlank()

                            val errorInFields = fullNameError || phoneNumberError || passwordError || confirmPasswordError

                            // Only save if all fields are valid
                            if (!errorInFields){
                                patientsViewModel.signUpNewPatient(
                                    fullName = fullName,
                                    phoneNumber = phoneNumber,
                                    password = password
                                ){success, patientID ->

                                    // Show success message and store the new patient ID
                                    if (success){
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Sign up successful!")
                                            newPatientID = patientID
                                            showNewPatientID = true
                                        }
                                    }else{
                                        scope.launch {
                                            phoneNumberUniquenessError = true
                                        }
                                    }

                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5F8B))
                    ) { Text(
                        text = "Sign Up",
                        fontSize = 16.sp)}

                    Spacer(modifier = Modifier.height(10.dp))

                    // Link to Login screen for users who already have an account
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(text = "Already have an account? ")
                        Text(
                            text = "Login",
                            color = Color.Blue,
                            modifier = Modifier.clickable {
                                onBackToLogin()
                            }
                        )
                    }
                }
            }
        }
    }

    // Show a dialog containing the new patientID after successful sign up
    if (showNewPatientID && newPatientID != null){
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(text = "New Account Created!",
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    style = typography.bodyLarge,
                    textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(text = buildAnnotatedString {
                        append("Your PatientID is: ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp)){
                            append(newPatientID)
                        }
                        append("\nPlease remember it for login purpose.")
                    },
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(
                        onClick = {

                            // Return to the Login screen after the user has seen the new patientID
                            onSignUpSuccess()
                        }
                    ){
                        Text(
                            text = "Go to Login",
                            fontSize = 16.sp)
                    }
                }
            }
        )
    }
}

/**
 * Check whether the password is valid.
 *
 * The password must:
 * 1. At least 8 characters long
 * 2. Contain at least one letter
 * 3. Contain at least one number
 *
 */

fun isValidPassword(password: String): Boolean {
    val containLetter = password.any { it.isLetter() }
    val containNumber = password.any { it.isDigit() }
    val passwordLength = password.length >= 8

    return containLetter && containNumber && passwordLength
}

/**
 * Check whether the phone number is valid.
 *
 * The phone number must:
 * 1. Start with 04
 * 2. Contain 10 digits
 * 3. Contain only numbers
 *
 */
fun isValidPhoneNumber(phoneNumber: String): Boolean {
    val numberStartWith = phoneNumber.startsWith("04")
    val numberLength = phoneNumber.length == 10
    val numberContainNumber = phoneNumber.all { it.isDigit() }

    return numberStartWith && numberLength && numberContainNumber
}
