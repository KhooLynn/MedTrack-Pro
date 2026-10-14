package com.lynn.s34598901.medtrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lynn.s34598901.medtrack.data.clinician.ClinicianUIState
import com.lynn.s34598901.medtrack.data.clinician.ClinicianViewModel

/**
 * Screen for clinician dashboard.
 *
 * This function:
 * 1. Displays the aggregate statistics
 * 2. Allows the clinician to generate insights about the data
 * 3. Allows the clinician to log out
 *
 */
@Composable
fun ClinicianScreen(
    clinicianViewModel: ClinicianViewModel,
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit = {}
){
    val stats by clinicianViewModel.stats.collectAsState()
    val insightUIState by clinicianViewModel.insightUIState.collectAsState()

    // Load the latest aggregate statistics when the screen first opens.
    LaunchedEffect(Unit) {
        clinicianViewModel.loadStats()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF1F7F6)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            Text(
                text = "Clinical Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display the aggregate statistics across all patient data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ){
                    Text(
                        text = "Aggregate Statistics",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // Total number of patients in the database
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                                append("Total patients: ")
                            }
                            append("${stats.patientCount}")
                        }
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Average number of medications per patient
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                                append("Average medication count: ")
                            }
                            append("%.2f".format(stats.averageMedicationCount))
                        }
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Most common symptom category
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                                append("Most common symptom category: ")
                            }
                            append(stats.mostCommonSymptomCategory)
                        }
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Average symptom severity
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                                append("Average symptom severity: ")
                            }
                            append("%.2f".format(stats.averageSymptomSeverity))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card that lets the clinician generate insights about the data
            GenAiInsightCard(
                insightsState = insightUIState,
                onFindPatterns = { clinicianViewModel.generateInsights() },
                onLogoutClick = onLogoutClick
            )
        }
    }
}

/**
 * Show the insights generated by the AI.
 */
@Composable
fun GenAiInsightCard(
    insightsState: ClinicianUIState,
    onFindPatterns: () -> Unit,
    onLogoutClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(435.dp),
        shape = RoundedCornerShape(16.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Generate Patterns About the Data",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Start AI insights generation when clicked
            Button(
                onClick = onFindPatterns,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = "Find Patterns",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Area that displays the AI insights generated
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.TopStart
            ){
                when (insightsState){
                    ClinicianUIState.Initial ->{
                        Text(

                            // Show instruction text before the user clicks the button
                            text = "Generate 3 interesting patterns or observations about the data.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }

                    // Show loading indicator while the AI is generating insights
                    ClinicianUIState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Show the AI insights generated in Lazy Column
                    is ClinicianUIState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item{
                                insightsState.content.forEachIndexed { index, insight ->
                                    Text(text = "${index + 1}. $insight")

                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }

                    // Show error message if the AI fails to generate insights
                    is ClinicianUIState.Error -> {
                        Text(
                            text = "Error: ${insightsState.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Logout button
    Button(
        onClick = onLogoutClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Logout",
            fontSize = 16.sp
        )
    }
}