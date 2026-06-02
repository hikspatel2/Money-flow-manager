package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DesignBackground
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@Composable
fun AboutUsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    LegalPageLayout(
        title = "About Us",
        onBack = onBack,
        modifier = modifier
    ) {
        HeaderTitle("Welcome to Money Flow Manager")
        Paragraph("Money Flow Manager by Rudees Digital is a high-performance ledger and business statement auditing tool. Our application empowers you to effortlessly track, manage, and understand your cash flows without requiring an internet connection.")
        Spacer(modifier = Modifier.height(16.dp))
        HeaderTitle("Our Mission")
        Paragraph("We strive to provide intuitive and robust financial tools so you can focus on building your enterprise rather than balancing spreadsheets. We prioritize offline-first capabilities, privacy, and seamless user experiences.")
        Spacer(modifier = Modifier.height(16.dp))
        HeaderTitle("Contact Us")
        Paragraph("Have questions? Reach out to us at info@rudeesdigital.dev or visit our website at www.rudeesdigital.dev.")
    }
}

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    LegalPageLayout(
        title = "Terms of Service",
        onBack = onBack,
        modifier = modifier
    ) {
        HeaderTitle("1. Acceptance of Terms")
        Paragraph("By using Money Flow Manager, you agree to these Terms of Service. If you do not agree, do not use the application.")
        Spacer(modifier = Modifier.height(16.dp))
        HeaderTitle("2. Offline Data and Sync")
        Paragraph("Currently, Money Flow Manager operates primarily offline, utilizing a local SQLite (Room) database. You are entirely responsible for creating backups of your data by using the provided export features (Excel/CSV/PDF). We are not liable for data loss occurred due to app uninstallation or device failure.")
        Spacer(modifier = Modifier.height(16.dp))
        HeaderTitle("3. Limitations of Use")
        Paragraph("This software is provided 'as is'. You may not decompile, reverse engineer, or unlawfully distribute this application. The auditing insights provided by the application are approximate and should not completely substitute professional certified accounting services.")
        Spacer(modifier = Modifier.height(16.dp))
        HeaderTitle("4. Changes to Terms")
        Paragraph("We reserve the right to modify these terms at any given time. If substantial changes are made, we may notify you through in-app alerts.")
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    LegalPageLayout(
        title = "Privacy Policy",
        onBack = onBack,
        modifier = modifier
    ) {
        HeaderTitle("Your Privacy Matters")
        Paragraph("At Rudees Digital, we take your privacy seriously. This Privacy Policy details how Money Flow Manager handles your data.")
        Spacer(modifier = Modifier.height(16.dp))
        HeaderTitle("Local Data Storage")
        Paragraph("All your financial transactions, ledgers, party names, and business metadata are stored strictly on your local device. We do not transmit or sync your transaction records to our cloud servers.")
        Spacer(modifier = Modifier.height(16.dp))
        HeaderTitle("Exporting Data")
        Paragraph("When you export data to PDF or Excel, files are generated locally on your device and are subject to your Android device's file storage permissions.")
        Spacer(modifier = Modifier.height(16.dp))
        HeaderTitle("Usage Data & Analytics")
        Paragraph("We do not collect identifiable personal or business transaction analytics. Any crash reports collected via standard Play Store mechanisms are anonymized and used exclusively to fix bugs and improve performance.")
    }
}

@Composable
fun LegalPageLayout(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DesignBackground)
    ) {
        // Appbar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DesignBackground)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(26.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Slate900
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            content = content
        )
    }
}

@Composable
private fun HeaderTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Slate900,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun Paragraph(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Slate600,
        lineHeight = 22.sp
    )
}
