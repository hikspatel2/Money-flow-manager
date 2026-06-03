package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MpinScreen(
    mode: MpinMode,
    onSuccess: (String) -> Unit,
    onForgotMpin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmPhase by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    
    val pinLength = 4
    val currentInput = if (isConfirmPhase) confirmPin else pin
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 80.dp, bottom = 48.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (mode) {
                    MpinMode.CREATE -> if (isConfirmPhase) "Confirm MPIN" else "Create MPIN"
                    MpinMode.UNLOCK -> "Enter MPIN"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (mode) {
                    MpinMode.CREATE -> "Set a 4-digit security PIN."
                    MpinMode.UNLOCK -> "Enter your security PIN to unlock."
                },
                color = Slate600,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until pinLength) {
                    val isFilled = i < currentInput.length
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) Blue600 else Slate200)
                    )
                }
            }
            
            if (errorText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorText, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
        }
        
        // Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )
            
            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (key in row) {
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(72.dp))
                        } else if (key == "DEL") {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        errorText = ""
                                        if (isConfirmPhase && confirmPin.isNotEmpty()) {
                                            confirmPin = confirmPin.dropLast(1)
                                        } else if (!isConfirmPhase && pin.isNotEmpty()) {
                                            pin = pin.dropLast(1)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Slate700)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Slate100)
                                    .clickable {
                                        errorText = ""
                                        if (isConfirmPhase) {
                                            if (confirmPin.length < pinLength) {
                                                confirmPin += key
                                                if (confirmPin.length == pinLength) {
                                                    if (pin == confirmPin) {
                                                        onSuccess(pin)
                                                    } else {
                                                        errorText = "PINs do not match"
                                                        confirmPin = ""
                                                    }
                                                }
                                            }
                                        } else {
                                            if (pin.length < pinLength) {
                                                pin += key
                                                if (pin.length == pinLength) {
                                                    if (mode == MpinMode.CREATE) {
                                                        isConfirmPhase = true
                                                    } else {
                                                        onSuccess(pin)
                                                        pin = "" // clear for retry in case it was wrong
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = key, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            }
                        }
                    }
                }
            }
        }
        
        if (mode == MpinMode.UNLOCK && onForgotMpin != null) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onForgotMpin) {
                Text("Forgot MPIN?", color = Blue600, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

enum class MpinMode {
    CREATE, UNLOCK
}
