package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.material.icons.filled.AccountBalanceWallet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // UI states
    var showSignUp by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()) {
            // Top Background section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Blue600)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp, bottom = 64.dp, start = 32.dp, end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.AccountBalanceWallet,
                        contentDescription = "App Logo",
                        modifier = Modifier.size(80.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Money Flow Manager",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Main Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-32).dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = Slate900
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AnimatedContent(
                    targetState = isOtpSent,
                    label = "login_state_transition"
                ) { otpSent ->
                    if (!otpSent) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (showSignUp) "Create your account" else "Welcome back",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        
                            AnimatedVisibility(visible = showSignUp) {
                                Column {
                                    OutlinedTextField(
                                        value = fullName,
                                        onValueChange = { fullName = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Full Name") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Blue600,
                                            unfocusedBorderColor = Slate300,
                                            focusedTextColor = Slate900,
                                            unfocusedTextColor = Slate900,
                                            focusedLabelColor = Blue600,
                                            unfocusedLabelColor = Slate500,
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            
                            OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it.filter { char -> char.isDigit() }.take(10) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            prefix = { Text("+91 ", color = Slate900) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue600,
                                unfocusedBorderColor = Slate300,
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedLabelColor = Blue600,
                                unfocusedLabelColor = Slate500,
                                focusedPrefixColor = Slate900,
                                unfocusedPrefixColor = Slate900
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                if (phoneNumber.length >= 10) {
                                    coroutineScope.launch {
                                        isLoading = true
                                        delay(1000)
                                        isLoading = false
                                        isOtpSent = true
                                        android.widget.Toast.makeText(context, "OTP Sent: 123456", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = phoneNumber.length >= 10 && !isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue600,
                                contentColor = Color.White,
                                disabledContainerColor = Slate200,
                                disabledContentColor = Slate400
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                            } else {
                                Text(if (showSignUp) "Sign Up" else "Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (showSignUp) "Already have an account?" else "Don't have an account?",
                                color = Slate600,
                                fontSize = 14.sp
                            )
                            TextButton(onClick = { showSignUp = !showSignUp }) {
                                Text(text = if (showSignUp) "Login" else "Sign Up", color = Blue600, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // OTP Verification UI
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "OTP Verification",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We have sent a 6-digit OTP to \n+91 $phoneNumber",
                            color = Slate600,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it.filter { char -> char.isDigit() }.take(6) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("6-Digit OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue600,
                                unfocusedBorderColor = Slate300,
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedLabelColor = Blue600,
                                unfocusedLabelColor = Slate500,
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                if (otpCode.length == 6) {
                                    coroutineScope.launch {
                                        isLoading = true
                                        delay(1000)
                                        isLoading = false
                                        onLoginSuccess()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = otpCode.length == 6 && !isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue600,
                                contentColor = Color.White,
                                disabledContainerColor = Slate200,
                                disabledContentColor = Slate400
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                            } else {
                                Text("Verify & Proceed", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextButton(onClick = { isOtpSent = false; otpCode = "" }) {
                            Text("Change Number", color = Blue600, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Developed By Rudees Digital",
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                textAlign = TextAlign.Center,
                color = Slate500,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
}
