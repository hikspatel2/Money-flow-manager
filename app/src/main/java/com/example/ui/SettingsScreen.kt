package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CashbookCategory
import com.example.ui.theme.*

enum class SettingSubScreen {
    NONE,
    MANAGE_LEDGERS,
    ABOUT_US,
    TERMS,
    PRIVACY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CashbookViewModel,
    onNavigateToRecurring: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSubScreen by remember { mutableStateOf(SettingSubScreen.NONE) }

    when (currentSubScreen) {
        SettingSubScreen.NONE -> MainSettingsContent(
            viewModel = viewModel,
            onNavigateToRecurring = onNavigateToRecurring,
            onNavigateToSubScreen = { currentSubScreen = it },
            onLogout = onLogout,
            modifier = modifier
        )
        SettingSubScreen.MANAGE_LEDGERS -> ManageLedgersScreen(
            viewModel = viewModel,
            onBack = { currentSubScreen = SettingSubScreen.NONE },
            modifier = modifier
        )
        SettingSubScreen.ABOUT_US -> AboutUsScreen(
            onBack = { currentSubScreen = SettingSubScreen.NONE },
            modifier = modifier
        )
        SettingSubScreen.TERMS -> TermsOfServiceScreen(
            onBack = { currentSubScreen = SettingSubScreen.NONE },
            modifier = modifier
        )
        SettingSubScreen.PRIVACY -> PrivacyPolicyScreen(
            onBack = { currentSubScreen = SettingSubScreen.NONE },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsContent(
    viewModel: CashbookViewModel,
    onNavigateToRecurring: () -> Unit,
    onNavigateToSubScreen: (SettingSubScreen) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()

    // Profile local form inputs
    var username by remember { mutableStateOf("") }
    var bizName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val readOnlyEmail = "hikspatel2@gmail.com" // Simulated session context metadata

    // Synchronize form states on first load
    LaunchedEffect(userProfile) {
        userProfile?.let {
            username = it.name
            bizName = it.businessName
            phone = it.phone
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DesignBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Appbar Header styled cohesively
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DesignBackground)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings icon",
                tint = Blue600,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings & Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Option 1: User Profile customization card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "ORGANIZATION PROFILE DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue600,
                        letterSpacing = 0.5.sp
                    )

                    // Business Name field input
                    Column {
                        Text("Business Name / Title", fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = bizName,
                            onValueChange = { bizName = it },
                            placeholder = { Text("Enter business banner name", color = Slate400) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_biz_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue600,
                                unfocusedBorderColor = Slate200,
                                cursorColor = Blue600
                            )
                        )
                    }

                    // User Name field input
                    Column {
                        Text("User Owner Name", fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            placeholder = { Text("Enter your name", color = Slate400) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_username_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue600,
                                unfocusedBorderColor = Slate200,
                                cursorColor = Blue600
                            )
                        )
                    }

                    // Phone number field input
                    Column {
                        Text("Phone Number", fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = { Text("Contact phone", color = Slate400) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_phone_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue600,
                                unfocusedBorderColor = Slate200,
                                cursorColor = Blue600
                            )
                        )
                    }

                    // email read only block
                    Column {
                        Text("Email Address (Read-only)", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = readOnlyEmail,
                            onValueChange = {},
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_email_readonly"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = Slate100,
                                disabledTextColor = Slate400,
                                disabledContainerColor = Slate50
                            )
                        )
                    }

                    Button(
                        onClick = {
                            if (bizName.isBlank() || username.isBlank()) {
                                Toast.makeText(context, "Business and User owner name cannot be blank.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.upsertUserProfile(
                                email = readOnlyEmail,
                                name = username.trim(),
                                businessName = bizName.trim(),
                                phone = phone.trim()
                            )
                            Toast.makeText(context, "Enterprise profile updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save profile", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Profile Changes", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Option 2: Manage Cashbooks ledgers/categories segment
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSubScreen(SettingSubScreen.MANAGE_LEDGERS) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Blue100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Manage Ledgers",
                            tint = Blue600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Manage Ledgers",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Create and organize your cashbooks",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to ledgers list",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Option 2.5: Manage Recurring Transactions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRecurring() }
                    .testTag("btn_navigate_recurring"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Blue100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Recurring icon",
                            tint = Blue600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recurring Transactions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Setup automated repeating credit and debit schedules",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to recurring screen",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Option 3: Branded About Us Premium Seal InfoCard & Legal Pages
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White), // Light card for Legal
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "ABOUT & LEGAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 0.5.sp
                    )
                    
                    ListItemRow(icon = Icons.Default.Info, text = "About Us") {
                        onNavigateToSubScreen(SettingSubScreen.ABOUT_US)
                    }
                    HorizontalDivider(color = Slate100)
                    ListItemRow(icon = Icons.Default.Description, text = "Terms of Service") {
                        onNavigateToSubScreen(SettingSubScreen.TERMS)
                    }
                    HorizontalDivider(color = Slate100)
                    ListItemRow(icon = Icons.Default.PrivacyTip, text = "Privacy Policy") {
                        onNavigateToSubScreen(SettingSubScreen.PRIVACY)
                    }
                    HorizontalDivider(color = Slate100)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = "App Version", tint = Slate500, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("App Version", fontSize = 15.sp, color = Slate800, fontWeight = FontWeight.Medium)
                        }
                        Text("v1.0.3", fontSize = 14.sp, color = Slate500, fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            // Support Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900), // charcoal dark card
                border = BorderStroke(1.dp, Color(0xFF020617))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = "Branding Seal", tint = Color(0xFFF59E0B), modifier = Modifier.size(36.dp))

                    Text(
                        text = "Rudees Digital",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Providing highly scalable, client-focused ledger products. Experience seamless business statements auditing with Money Flow Manager.",
                        fontSize = 11.sp,
                        color = Slate400,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Link Website
                        Column(
                            modifier = Modifier
                                .clickable {
                                    val url = "https://www.rudeesdigital.dev"
                                    val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    try {
                                        context.startActivity(urlIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Website: www.rudeesdigital.dev", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Language, contentDescription = "Web logo", tint = Color(0xFF93C5FD), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Website", fontSize = 10.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                        }

                        // Mail support info Link website
                        Column(
                            modifier = Modifier
                                .clickable {
                                    val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:info@rudeesdigital.dev")
                                        putExtra(Intent.EXTRA_SUBJECT, "Money Flow Manager Support inquiry")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    try {
                                        context.startActivity(mailIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Support: info@rudeesdigital.dev", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Email support", tint = Color(0xFF93C5FD), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Support Email", fontSize = 10.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Simulated Exit system button
            Button(
                onClick = {
                    onLogout()
                    Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Rose600)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Exit log logo")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Account Session", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ListItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = text, tint = Slate500, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontSize = 15.sp, color = Slate800, fontWeight = FontWeight.Medium)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Arrow right", tint = Slate400, modifier = Modifier.size(20.dp))
    }
}
