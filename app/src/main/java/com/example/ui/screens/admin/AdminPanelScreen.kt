package com.example.ui.screens.admin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.MerchantConfig
import com.example.ui.components.MerchantQrCodeCanvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CashFlowViewModel
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val pendingList by viewModel.pendingTransactions.collectAsState()
    val allList by viewModel.allTransactions.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val context = LocalContext.current
    val merchantUpiId by MerchantConfig.merchantUpiId.collectAsState()
    val merchantName by MerchantConfig.merchantName.collectAsState()
    var newUpiInput by remember(merchantUpiId) { mutableStateOf(merchantUpiId) }
    var newMerchantNameInput by remember(merchantName) { mutableStateOf(merchantName) }

    var activeTab by remember { mutableStateOf("PENDING") } // "PENDING", "ALL", "USERS", "QR_SETTINGS"
    var adjustPhone by remember { mutableStateOf("+91 98765 43210") }
    var adjustAmount by remember { mutableStateOf("500") }

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D17))
            .statusBarsPadding()
    ) {
        // Top Admin Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ADMIN APPROVAL PANEL",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pendingList.size} PENDING",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Filter Sub-Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1322))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "PENDING" to "Approvals (${pendingList.size})",
                "ALL" to "All Logs",
                "USERS" to "Credits",
                "QR_SETTINGS" to "Merchant QR"
            ).forEach { (tabKey, title) ->
                val isSel = activeTab == tabKey
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) GoldPrimary else CardDark)
                        .clickable { activeTab = tabKey }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("admin_tab_$tabKey")
                ) {
                    Text(
                        text = title,
                        color = if (isSel) Color.Black else TextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeTab == "PENDING") {
                if (pendingList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🎉", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "All caught up! No pending payments.",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "When users deposit via Merchant QR or request a withdrawal, they will show here for your approval.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(pendingList) { tx ->
                        val isDeposit = tx.type == "DEPOSIT"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_pending_item_${tx.id}"),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDeposit) NeonGreen.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isDeposit) NeonGreen.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isDeposit) "DEPOSIT REQUEST (UPI)" else "WITHDRAWAL REQUEST",
                                            color = if (isDeposit) NeonGreen else NeonCyan,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Text(
                                        text = "₹${String.format("%.2f", tx.amount)}",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Player Phone: ${tx.userPhone}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                Text(
                                    text = if (isDeposit) "UTR / Ref: ${tx.utrOrAccount}" else "Pay To: ${tx.utrOrAccount}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )

                                if (isDeposit) {
                                    Text(
                                        text = "Target UPI: ${tx.targetUpi}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = "Requested: ${dateFormat.format(Date(tx.timestamp))}",
                                    color = TextMuted,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Approve / Reject Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.rejectTransaction(tx.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("admin_reject_btn_${tx.id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.8f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "REJECT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.approveTransaction(tx.id) },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(44.dp)
                                            .testTag("admin_approve_btn_${tx.id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isDeposit) "APPROVE & CREDIT" else "APPROVE & PAY",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == "ALL") {
                items(allList) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${tx.type} • ₹${tx.amount.toInt()}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val statusColor = when (tx.status) {
                                        "APPROVED" -> NeonGreen
                                        "REJECTED" -> NeonRed
                                        else -> GoldPrimary
                                    }
                                    Text(
                                        text = tx.status,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "${tx.userPhone} • ${tx.utrOrAccount}",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = dateFormat.format(Date(tx.timestamp)),
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            } else if (activeTab == "USERS") {
                // USERS / BALANCE ADJUSTMENT
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Manual User Balance Adjustment",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = adjustPhone,
                                onValueChange = { adjustPhone = it },
                                label = { Text("User Phone Number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = adjustAmount,
                                onValueChange = { adjustAmount = it },
                                label = { Text("Amount (₹)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val amt = adjustAmount.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) viewModel.adjustUserBalance(adjustPhone, -amt)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.8f))
                                ) {
                                    Text("Deduct ₹$adjustAmount", color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        val amt = adjustAmount.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) viewModel.adjustUserBalance(adjustPhone, amt)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                                ) {
                                    Text("Add ₹$adjustAmount", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Registered Players (${allUsers.size})",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                items(allUsers) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${user.name} (${user.phone})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Ref: ${user.referralCode} • Ref Earn: ₹${user.referralEarnings}", color = TextMuted, fontSize = 11.sp)
                            }
                            Text(
                                text = "₹${String.format("%.2f", user.balance)}",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else if (activeTab == "QR_SETTINGS") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Merchant QR & UPI Gateway Settings",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Whatever UPI ID you save here instantly becomes the active payment QR for all deposits across the app.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Live QR Preview
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.clip(RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    MerchantQrCodeCanvas(
                                        upiId = newUpiInput.ifBlank { "merchant.pay@upi" },
                                        depositAmount = 500.0,
                                        merchantName = newMerchantNameInput.ifBlank { "CashFlow Pro" },
                                        sizeDp = 160
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = newUpiInput.ifBlank { "merchant.pay@upi" },
                                        color = Color(0xFF1E293B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = newUpiInput,
                                onValueChange = { newUpiInput = it.trim() },
                                label = { Text("Active Merchant UPI ID") },
                                placeholder = { Text("e.g. yourname@okhdfcbank or yourname@ybl") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_merchant_upi_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = newMerchantNameInput,
                                onValueChange = { newMerchantNameInput = it },
                                label = { Text("Merchant / Business Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_merchant_name_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (newUpiInput.isNotBlank()) {
                                        MerchantConfig.updateMerchantDetails(newUpiInput, newMerchantNameInput)
                                        Toast.makeText(context, "✅ New Merchant QR & UPI ID Applied!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Please enter a valid UPI ID", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_merchant_qr_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "SAVE & SET AS ACTIVE QR",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
