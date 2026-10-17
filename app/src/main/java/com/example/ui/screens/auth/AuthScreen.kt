package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CashFlowViewModel
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthMode {
    REGISTER_ONLINE,
    LOGIN
}

@Composable
fun AuthScreen(
    viewModel: CashFlowViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var authMode by remember { mutableStateOf(AuthMode.REGISTER_ONLINE) }

    // Form inputs
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("9876543210") }
    var referralInput by remember { mutableStateOf("") }
    var mobileTouched by remember { mutableStateOf(false) }

    var isSendingOtp by remember { mutableStateOf(false) }
    var isOtpSent by remember { mutableStateOf(false) }

    var otpCode by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var resendCooldown by remember { mutableIntStateOf(30) }

    // Validation logic for 10-digit Indian mobile number
    val cleanDigits = mobileNumber.filter { it.isDigit() }
    val isValidMobile = cleanDigits.length == 10 && (cleanDigits.startsWith("6") || cleanDigits.startsWith("7") || cleanDigits.startsWith("8") || cleanDigits.startsWith("9"))
    val isMobileLengthValid = cleanDigits.length == 10

    val validationErrorMessage = when {
        !mobileTouched -> null
        cleanDigits.isEmpty() -> "Mobile number cannot be empty"
        cleanDigits.length < 10 -> "Enter complete 10-digit mobile number (${cleanDigits.length}/10)"
        !isValidMobile -> "Mobile number must start with 6, 7, 8, or 9"
        else -> null
    }

    // OTP Resend Countdown Timer
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            resendCooldown = 30
            while (resendCooldown > 0) {
                delay(1000)
                resendCooldown--
            }
        }
    }

    fun handleSendOtp() {
        mobileTouched = true
        if (!isValidMobile) return

        keyboardController?.hide()
        isSendingOtp = true
        coroutineScope.launch {
            delay(500) // Simulated secure SMS dispatch
            isSendingOtp = false
            isOtpSent = true
            otpCode = "7860" // Pre-filled demo OTP for smooth testing
            otpError = null
        }
    }

    fun handleAuthSubmit() {
        if (otpCode.length < 4) {
            otpError = "Please enter the 4-digit verification code"
            return
        }

        isVerifying = true
        coroutineScope.launch {
            delay(500)
            if (otpCode == "7860" || otpCode.length == 4) {
                otpError = null
                if (authMode == AuthMode.REGISTER_ONLINE) {
                    // Register Online with Zero Initial Balance
                    viewModel.registerOnline(
                        name = fullName.ifBlank { "VIP Player" },
                        phone = cleanDigits,
                        referralCode = referralInput
                    )
                } else {
                    // Returning login
                    viewModel.loginWithPhone("+91 $cleanDigits", "VIP Member")
                }
            } else {
                otpError = "Invalid verification code. Use demo code 7860."
                isVerifying = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF080D1A), Color(0xFF10172A), BgDark)
                )
            )
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // App Crest / Logo
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(listOf(GoldPrimary, GoldSecondary))
                    )
                    .border(2.dp, Color(0xFFFFF9C4), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CashFlow",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PRO",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp
                )
            }

            Text(
                text = "PREMIER VIP GAMING & CASINO CLUB",
                color = NeonGreen,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Auth Mode Selection Switcher: REGISTER ONLINE vs LOGIN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isRegister = authMode == AuthMode.REGISTER_ONLINE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isRegister) GoldPrimary else Color.Transparent)
                        .clickable {
                            authMode = AuthMode.REGISTER_ONLINE
                            isOtpSent = false
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_register_online"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HowToReg,
                            contentDescription = null,
                            tint = if (isRegister) Color.Black else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REGISTER ONLINE",
                            color = if (isRegister) Color.Black else TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                val isLogin = authMode == AuthMode.LOGIN
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLogin) GoldPrimary else Color.Transparent)
                        .clickable {
                            authMode = AuthMode.LOGIN
                            isOtpSent = false
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            tint = if (isLogin) Color.Black else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LOGIN",
                            color = if (isLogin) Color.Black else TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (!isOtpSent) {
                        // === STAGE 1: Input Fields ===
                        Text(
                            text = if (authMode == AuthMode.REGISTER_ONLINE) "Register Online Account" else "Login to Your Account",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (authMode == AuthMode.REGISTER_ONLINE)
                                "Create a new player ID with ₹0 initial balance"
                            else
                                "Enter your registered mobile number to receive OTP",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // Zero Balance Policy Indicator for Register Online
                        if (authMode == AuthMode.REGISTER_ONLINE) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonGreen.copy(alpha = 0.12f))
                                    .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = NeonGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Starting Balance: ₹0.00 (Zero Balance)",
                                            color = NeonGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Deposit via Official QR in Wallet to start playing",
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Full Name Input Field (For Online Registration)
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name / Player Name") },
                                placeholder = { Text("e.g. Sameer Alam") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_register_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Mobile Number Input Field
                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }.take(10)
                                mobileNumber = digitsOnly
                                mobileTouched = true
                            },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("98765 43210") },
                            leadingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = if (isValidMobile) NeonGreen else GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+91",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(18.dp)
                                            .background(CardBorder)
                                    )
                                }
                            },
                            trailingIcon = {
                                if (cleanDigits.isNotEmpty()) {
                                    if (isValidMobile) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Valid",
                                            tint = NeonGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        IconButton(onClick = { mobileNumber = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = TextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            isError = validationErrorMessage != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (isValidMobile) handleSendOtp()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_mobile_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isValidMobile) NeonGreen else GoldPrimary,
                                unfocusedBorderColor = if (isValidMobile) NeonGreen.copy(alpha = 0.6f) else CardBorder,
                                errorBorderColor = NeonRed,
                                focusedLabelColor = GoldPrimary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Validation Helper / Error Text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (validationErrorMessage != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = NeonRed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = validationErrorMessage,
                                        color = NeonRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else if (isValidMobile) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = NeonGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Valid mobile number",
                                        color = NeonGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "10-digit Indian mobile",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = "${cleanDigits.length}/10",
                                color = if (isMobileLengthValid) NeonGreen else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Referral Code (Optional for Online Registration)
                        if (authMode == AuthMode.REGISTER_ONLINE) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = referralInput,
                                onValueChange = { referralInput = it.uppercase().take(12) },
                                label = { Text("Referral Code (Optional)") },
                                placeholder = { Text("e.g. CFPRO786") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_referral_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 'Send OTP' / Action Button
                        Button(
                            onClick = { handleSendOtp() },
                            enabled = isValidMobile && !isSendingOtp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_send_otp_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == AuthMode.REGISTER_ONLINE) NeonGreen else GoldPrimary,
                                disabledContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSendingOtp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SENDING SECURE OTP...",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = if (isValidMobile) Color.Black else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (authMode == AuthMode.REGISTER_ONLINE) "SEND OTP TO REGISTER" else "SEND OTP TO LOGIN",
                                    color = if (isValidMobile) Color.Black else TextMuted,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    } else {
                        // === STAGE 2: OTP Validation & Final Submission ===
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { isOtpSent = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Change Number",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (authMode == AuthMode.REGISTER_ONLINE) "Confirm Online Registration" else "Enter Verification Code",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "OTP sent to +91 $cleanDigits (Demo code: 7860)",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // OTP Code Input Field
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { input ->
                                otpCode = input.filter { it.isDigit() }.take(4)
                                otpError = null
                            },
                            label = { Text("4-Digit OTP") },
                            placeholder = { Text("7860") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (otpCode.length == 4) NeonGreen else GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (otpCode.length == 4) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Complete",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (otpCode.length == 4) handleAuthSubmit()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_otp_input"),
                            shape = RoundedCornerShape(12.dp),
                            isError = otpError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (otpCode.length == 4) NeonGreen else GoldPrimary,
                                unfocusedBorderColor = if (otpCode.length == 4) NeonGreen.copy(alpha = 0.5f) else CardBorder,
                                errorBorderColor = NeonRed,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        if (otpError != null) {
                            Text(
                                text = otpError!!,
                                color = NeonRed,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Use demo code: 7860",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (resendCooldown > 0) {
                                Text(
                                    text = "Resend in ${resendCooldown}s",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            } else {
                                Text(
                                    text = "Resend OTP",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.clickable {
                                        resendCooldown = 30
                                        otpCode = "7860"
                                        otpError = null
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Button
                        Button(
                            onClick = { handleAuthSubmit() },
                            enabled = otpCode.length >= 4 && !isVerifying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_verify_otp_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == AuthMode.REGISTER_ONLINE) NeonGreen else GoldPrimary,
                                disabledContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VERIFYING...",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp
                                )
                            } else {
                                Text(
                                    text = if (authMode == AuthMode.REGISTER_ONLINE)
                                        "REGISTER ONLINE (₹0 BALANCE)"
                                    else
                                        "VERIFY & LOGIN",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.5.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Instant Access Button (Started with strictly ₹0.00 zero balance)
            Button(
                onClick = {
                    viewModel.registerOnline(
                        name = "VIP Player",
                        phone = "9876543210",
                        referralCode = ""
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("auth_quick_demo_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = "⚡ Instant Quick Access (Zero Balance ₹0)",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Security & Disclaimers Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "18+ Only • Safe & Provably Fair • Instant UPI",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
