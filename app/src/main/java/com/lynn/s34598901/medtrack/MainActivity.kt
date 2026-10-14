package com.lynn.s34598901.medtrack

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlignVerticalBottom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lynn.s34598901.medtrack.ui.theme.MedtrackTheme
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.lynn.s34598901.medtrack.data.CsvDatabaseSeeder
import com.lynn.s34598901.medtrack.data.MedtrackDatabase
import com.lynn.s34598901.medtrack.data.clinician.ClinicianViewModel
import com.lynn.s34598901.medtrack.data.medcoach.MedCoachViewModel
import com.lynn.s34598901.medtrack.data.medications.MedicationsViewModel
import com.lynn.s34598901.medtrack.data.patients.Patient
import com.lynn.s34598901.medtrack.data.patients.PatientsViewModel
import com.lynn.s34598901.medtrack.data.symptoms.SymptomsViewModel
import kotlinx.coroutines.launch

/**
 * Stores all route names used in the app navigation. These route values are used by the NavHost to
 * decided which screen should be shown.
 *
 */

object MedTrackRoutes {
    const val Welcome = "welcome"
    const val Login = "login"
    const val SignUp = "signup"
    const val Home = "home"
    const val Symptoms = "symptoms"
    const val AddMedication = "add_medication"
    const val ClaimAccount = "claim_account"
    const val Settings = "settings"
    const val MedCoach = "med_coach"
    const val Clinician = "clinician"
    const val PatientRadar = "patient_radar"
    const val ClinicianLogin = "clinician_login"
}

// Main Screen of the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get the app database and send it with initial CSV data when the app starts
        val database = MedtrackDatabase.getDatabase(applicationContext)

        lifecycleScope.launch {
            val seeder = CsvDatabaseSeeder(applicationContext, database)
            seeder.seedDatabase()
        }

        // Show the welcome screen when there is no active session
        setContent {
            MedtrackTheme {
                MedTrackAppRoute()
            }
        }
    }
}

/**
 * Main navigation controller for the whole app.
 *
 * This function:
 * 1. Checks whether a user is already logged in
 * 2. Decides the first screen to show based on whether the user is logged in or not
 * 3. Handles navigation between all the screens in the app
 * 4. Shows the bottom bar only on the Home and Symptoms screens
 *
 * If a patient ID already exists in SharedPreferences, the app will show the Home screen.
 * Otherwise, the app will show the Welcome screen.
 *
 */

@Composable
fun MedTrackAppRoute(){
    val context = LocalContext.current

    // Create the ViewModels needed for the app screens
    val patientsViewModel: PatientsViewModel = viewModel(
        factory = PatientsViewModel.PatientsViewModelFactory(context)
    )

    val medicationsViewModel: MedicationsViewModel = viewModel(
        factory = MedicationsViewModel.MedicationsViewModelFactory(context)
    )

    val symptomsViewModel: SymptomsViewModel = viewModel(
        factory = SymptomsViewModel.SymptomsViewModelFactory(context)
    )

    val medCoachViewModel: MedCoachViewModel = viewModel(
        factory = MedCoachViewModel.MedCoachViewModelFactory(context.applicationContext as Application)
    )

    val clinicianViewModel: ClinicianViewModel = viewModel(
        factory = ClinicianViewModel.ClinicianViewModelFactory(context.applicationContext as Application)
    )

    // Open the app's saved storage named "MedTrackPreferences"
    val sharedPreferences = context.getSharedPreferences("MedTrackPreferences", Context.MODE_PRIVATE)

    // Read the saved logged in patient ID from it
    val loggedInPatientID = sharedPreferences.getString("logged_in_patient_id", "")

    // Decide which screen should open first
    val startDestination = if(!loggedInPatientID.isNullOrEmpty()){
        MedTrackRoutes.Home
    }else{
        MedTrackRoutes.Welcome
    }

    // Create the navigation controller
    val navController = rememberNavController()

    // Find what screen the user is on now
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    // Get the route name of the screen and it will stop and return null if the screen is not found
    val activeRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        // Decide which bottom navigation bar should be shown for the current screen
        bottomBar = {

            // Screens for patient's side of the app
            val patientRoutes = listOf(MedTrackRoutes.Home, MedTrackRoutes.Symptoms, MedTrackRoutes.MedCoach, MedTrackRoutes.Settings)

            // Screens for clinician's side of the app
            val clinicianRoutes = listOf(MedTrackRoutes.Clinician, MedTrackRoutes.PatientRadar)

            // Check the current route and show the correct bottom bar
            when (activeRoute){
                in patientRoutes -> {
                    MedTrackBottomBar(navController = navController)
                }
                in clinicianRoutes -> {
                    ClinicianBottomBar(navController = navController)
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ){

            // Welcome Screen
            composable(MedTrackRoutes.Welcome){
                WelcomeScreen(
                    onLoginClick = {
                        navController.navigate(MedTrackRoutes.Login)
                    },
                    onSignUpClick = {
                        navController.navigate(MedTrackRoutes.SignUp)
                    },
                    onClaimAccountClick = {
                        navController.navigate(MedTrackRoutes.ClaimAccount)
                    }
                )
            }

            // Login Screen
            composable(MedTrackRoutes.Login){
                LoginScreen(
                    patientsViewModel = patientsViewModel,
                    onLoginSuccess = { patientID ->

                        // Saved logged in patient ID so the session can be remembered
                        sharedPreferences.edit { putString("logged_in_patient_id", patientID) }

                        // Move to Home and remove Welcome from back stack because it is no longer useful
                        navController.navigate(MedTrackRoutes.Home){
                            popUpTo(MedTrackRoutes.Welcome){inclusive = true}

                            // if the user is already on that screen, it will not be added to the back stack again
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Sign Up Screen
            composable (MedTrackRoutes.SignUp){
                SignUpScreen(
                    patientsViewModel = patientsViewModel,
                    onSignUpSuccess = {

                        // After successful sign up, move to Login Screen and remove SignUp from back stack
                        navController.navigate(MedTrackRoutes.Login){
                            popUpTo(MedTrackRoutes.SignUp){inclusive = true}
                            launchSingleTop = true
                        }
                    },
                    onBackToLogin = {

                        // Go back to Login Screen and remove SignUp from back stack
                        navController.navigate(MedTrackRoutes.Login){
                            popUpTo(MedTrackRoutes.SignUp){ inclusive = true}
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Home Screen
            composable(MedTrackRoutes.Home){

                // Get the logged-in patient's ID from SharedPreferences
                val currentPatientID = sharedPreferences.getString("logged_in_patient_id", "") ?: ""


                // When the patientID changes, load the matching patient's name from the database
                LaunchedEffect(currentPatientID) {
                    patientsViewModel.loadPatient(currentPatientID)
                }

                val patientName by patientsViewModel.currentPatientName.collectAsState()

                // Get all medications that belong to the logged-in patient
                val medications by medicationsViewModel.getMedicationsByPatientID(currentPatientID).collectAsState(emptyList())

                HomeScreen(
                    patientName = patientName,
                    patientID = currentPatientID,
                    medications = medications,
                    medicationsViewModel = medicationsViewModel,
                    onAddMedicationClick = {

                        // Open Add Medication screen
                        navController.navigate(MedTrackRoutes.AddMedication)
                    },
                    onLogoutClick = {

                        // Remove the saved session when user logs out
                        sharedPreferences.edit { remove("logged_in_patient_id") }

                        // Go back to Welcome screen  and clear the back stack
                        navController.navigate(MedTrackRoutes.Welcome){

                            // clear the back stack from the very beginning including the start destination screen to reset navigation history
                            popUpTo(navController.graph.findStartDestination().id){ inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Symptoms screen
            composable(MedTrackRoutes.Symptoms){
                val currentPatientID = sharedPreferences.getString("logged_in_patient_id", "") ?: ""

                // Open Symptoms screen using the current patient ID
                SymptomScreen(patientID = currentPatientID, symptomsViewModel = symptomsViewModel)
            }

            // Add Medication screen
            composable(MedTrackRoutes.AddMedication){
                val currentPatientID = sharedPreferences.getString("logged_in_patient_id", "") ?: ""

                AddMedicationScreen(
                    patientID = currentPatientID,
                    medicationsViewModel = medicationsViewModel,
                    onMedicationSaved = {

                        // After saving, move back to Home screen and remove AddMedication from back stack
                        navController.navigate(MedTrackRoutes.Home){
                            popUpTo(MedTrackRoutes.Home){inclusive = true}
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Claim Account Screen
            composable (MedTrackRoutes.ClaimAccount){
                ClaimAccountScreen(
                    patientsViewModel = patientsViewModel,

                    // After the account is claimed successfully, go to the Login screen
                    onClaimSuccess = {
                        navController.navigate(MedTrackRoutes.Login){
                            popUpTo(MedTrackRoutes.ClaimAccount){inclusive = true}
                            launchSingleTop = true
                        }
                    },

                    // If the user chooses to go back, return to the Login screen
                    onBackToLogin = {
                        navController.navigate(MedTrackRoutes.Login){
                            popUpTo(MedTrackRoutes.ClaimAccount){inclusive = true}
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Settings screen
            composable (MedTrackRoutes.Settings){
                val currentPatientID = sharedPreferences.getString("logged_in_patient_id", "") ?: ""

                LaunchedEffect(currentPatientID) {
                    patientsViewModel.loadPatient(currentPatientID)
                }

                val patientDetails by patientsViewModel.currentPatient.collectAsState()

                SettingsScreen(
                    patient = patientDetails,

                    // Open the clinician login section from the Settings screen
                    onClinicianLogin = {
                        navController.navigate(MedTrackRoutes.ClinicianLogin)
                    },

                    // Logout the current patient
                    onLogoutClick = {
                        sharedPreferences.edit { remove("logged_in_patient_id") }
                        patientsViewModel.clearCurrentPatient()

                        // Go back to the Welcome screen and clear the navigation history.
                        navController.navigate(MedTrackRoutes.Welcome){
                            popUpTo(navController.graph.findStartDestination().id){inclusive = true}
                            launchSingleTop = true
                        }
                    }
                )
            }

            // MedCoach Screen
            composable(MedTrackRoutes.MedCoach){
                val currentPatientID = sharedPreferences.getString("logged_in_patient_id", "") ?: ""

                LaunchedEffect(currentPatientID) {
                    patientsViewModel.loadPatient(currentPatientID)
                }
                val patientDetails by patientsViewModel.currentPatient.collectAsState()

                MedCoachScreen(
                    patient = patientDetails,
                    medCoachViewModel = medCoachViewModel
                )
            }

            //Clinician Screen
            composable(MedTrackRoutes.Clinician){
                ClinicianScreen(
                    clinicianViewModel = clinicianViewModel,

                    // Return from the clinician section back to the Settings screen
                    onLogoutClick = {
                        navController.navigate(MedTrackRoutes.Settings){
                            popUpTo(MedTrackRoutes.Settings){ inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Patient Radar Screen
            composable(MedTrackRoutes.PatientRadar) {
                PatientRadarScreen(clinicianViewModel = clinicianViewModel)
            }

            // Clinician Login Screen
            composable(MedTrackRoutes.ClinicianLogin){
                ClinicianLoginScreen(
                    onBackClick = {
                        navController.navigate(MedTrackRoutes.Settings){
                            launchSingleTop = true
                        }
                    },
                    onLoginSuccess = {
                        navController.navigate(MedTrackRoutes.Clinician){
                            popUpTo(MedTrackRoutes.ClinicianLogin){inclusive = true}
                        }
                    }
                )
            }
        }
    }
}

/**
 * Bottom navigation bar between Home Screen, Symptom Screen, MedCoach Screen and Settings Screen.
 *
 */

@Composable
fun MedTrackBottomBar(navController: NavHostController) {
    BottomAppBar(
        modifier = Modifier.height(60.dp),
        containerColor = Color(0xFFF1F7F6)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ){

            // Home button
            IconButton(
                onClick = {

                    // Navigate back to Home and clear AddMedication from back stack
                    navController.navigate(MedTrackRoutes.Home) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            ) {
                Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(30.dp))
            }

            // Symptoms button
            IconButton(
                onClick = {

                    // Navigate to Symptoms and restore previous state if exists
                    navController.navigate(MedTrackRoutes.Symptoms) {

                        // Remove the screen but remember how it was (e.g. scroll position)
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                Icon(Icons.Filled.Info, contentDescription = "Symptoms", modifier = Modifier.size(30.dp))
            }

            // MedCoach button
            IconButton(
                onClick = {
                    navController.navigate(MedTrackRoutes.MedCoach){

                        // Remove the screen but remember how it was (e.g. scroll position)
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ){
                Icon(Icons.Default.Search, contentDescription = "MedCoach", modifier = Modifier.size(30.dp))
            }

            // Settings button
            IconButton(
                onClick = {
                    navController.navigate(MedTrackRoutes.Settings){

                        // Remove the screen but remember how it was (e.g. scroll position)
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ){
                Icon(Icons.Filled.Settings, contentDescription = "Settings", modifier = Modifier.size(30.dp))
            }
        }
    }
}

/**
 * Bottom navigation bar between Clinician Screen and Patient Radar Screen.
 */
@Composable
fun ClinicianBottomBar(navController: NavHostController) {
    BottomAppBar(
        modifier = Modifier.height(60.dp),
        containerColor = Color(0xFFF1F7F6)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ){

            // Clinician Dashboard button
            IconButton(
                onClick = {
                    navController.navigate(MedTrackRoutes.Clinician){
                        launchSingleTop = true
                    }
                }
            )  {
                Icon(Icons.Filled.Info, contentDescription = "Clinician", modifier = Modifier.size(30.dp))
            }

            // Patient Radar button
            IconButton(
                onClick = {
                    navController.navigate(MedTrackRoutes.PatientRadar){
                        launchSingleTop = true
                    }
                }
            ){
                Icon(Icons.Filled.AlignVerticalBottom, contentDescription = "Patient Radar", modifier = Modifier.size(30.dp))
            }
        }
    }
}


/**
 * Shows the welcome screen of the MedTrack app.
 *
 * This screen includes:
 * 1. The logo and name of the app.
 * 2. A disclaimer about the app's purpose.
 * 3. A link to the Monash Health Clinic website.
 * 4. Author's name and student ID
 * 5. Login and Sign Up buttons.
 *
 */
@Composable
fun WelcomeScreen(modifier: Modifier = Modifier, onLoginClick: () -> Unit = {}, onSignUpClick: () -> Unit = {}, onClaimAccountClick: () -> Unit = {}) {
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
        ){
            // The logo of the app shown at the top of the welcome screen
            androidx.compose.foundation.Image(
                /* AI Acknowledgement
                * The logo used in this MedTrack project was created with the assistance of
                * generative AI. I selected and integrated the final version into the application
                * myself.*/
                painter = painterResource(id = R.drawable.medtrack_logo),
                contentDescription = "Medtrack Logo",
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name of the app
            Text(
                text = "MedTrack",
                fontSize = 45.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Disclaimer box to remind the users the purpose of this app
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFEDEDED),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(color = Color.LightGray, width = 1.dp, shape = RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ){
                Text(
                    text = "Disclaimer: This app is for tracking purposes only and does not replace professional medical advice.",
                    color = Color(0xFFE65100),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clickable link to the Monash Health Clinic website for medical consultation
            Text(

                // Build a string that contain normal text and clickable text
                buildAnnotatedString {
                    append("For medical consultation, visit ")

                    // Link to Monash Health Clinic website
                    withLink(

                        // Url that will be opened when the text is clicked
                        LinkAnnotation.Url(
                            url = "https://monashhealth.org/",

                            // The category style for link
                            styles = TextLinkStyles(

                                // The specific design of the clickable text
                                style = SpanStyle(
                                    color = Color.Blue,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        )
                    ) {
                        append("Monash Health Clinic")
                    }
                },
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Author's name and student ID
            Text(
                text = "Khoo Lynn (34598901)",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Navigate to Log in Screen using the Login Button
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text(
                text = "Login",
                fontSize = 16.sp)}

            // Navigate to Claim Account Screen using the Claim Account Button
            Button(
                onClick = onClaimAccountClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text(
                    text = "Claim Account",
                    fontSize = 16.sp)
            }

            // Navigate to Sign Up Screen using the Sign-Up Button
            Button(
                onClick = onSignUpClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5F8B))
            ) { Text(
                text = "Sign Up",
                fontSize = 16.sp)}
        }
    }
}