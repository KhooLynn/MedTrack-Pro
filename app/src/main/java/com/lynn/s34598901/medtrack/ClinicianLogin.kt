package com.lynn.s34598901.medtrack

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen used to validate clinician access key before showing the clinician dashboard.
 *
 */
@Composable
fun ClinicianLoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // Store the access key entered for clinician login
    var accessKey by remember { mutableStateOf("") }

    // Store the error message shown when the access key is incorrect
    var showError by remember { mutableStateOf("") }

    // Control whether the password is shown or not
    var showAccessKey by remember { mutableStateOf(false) }

    // Correct access key for clinician login
    val correctAccessKey = "dollar-entry-apples"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F7F6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top row with back button and screen title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF263238)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Clinician Login",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Centers the login card on the screen and shift it slightly upward
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                // Card containing the clinician access key form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Clinician access",
                            tint = Color(0xFF232F72)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Enter Clinician Access Key",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This area is for clinician review features only.",
                            style = typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = accessKey,
                            onValueChange = {
                                accessKey = it
                                showError = ""
                            },
                            placeholder = {
                                Text(
                                    text = "Enter access key",
                                    fontSize = 15.sp)
                            },
                            // Hide or show the access key depending on the eye icon state
                            visualTransformation = if (showAccessKey){
                                VisualTransformation.None
                            }else{
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {

                                        // Toggle the visibility of the password when the user clicks the eye icon
                                        showAccessKey = !showAccessKey
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (showAccessKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showAccessKey) "Hide password" else "Show password"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Show validation error only when the access key is incorrect
                        if(showError.isNotBlank()){
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = showError,
                                color = Color.Red,
                                style = typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if(accessKey.trim() == correctAccessKey){
                                    showError = ""
                                    onLoginSuccess()
                                }else{
                                    showError = "Invalid access key. Please try again."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF232F72),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onBackClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = "Back to Settings",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}