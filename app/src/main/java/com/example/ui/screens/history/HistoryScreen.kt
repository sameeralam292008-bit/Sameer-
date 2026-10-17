package com.example.ui.screens.history

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
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
fun HistoryScreen(
    viewModel: CashFlowViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.userTransactions.collectAsState()
    val bets by viewModel.userBets.collectAsState()

    var selectedSection by remember { mutableIntStateOf(0) } // 0: Transactions (Deposits/Withdrawals), 1: Game Bets
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "APPROVED", "REJECTED", "DEPOSIT", "WITHDRAWAL"

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    val filteredTransactions = remember(transactions, statusFilter) {
        when (statusFilter) {
            "PENDING" -> transactions.filter { it.status == "PENDING" }
            "APPROVED" -> transactions.filter { it.status == "APPROVED" }
            "REJECTED" -> transactions.filter { it.status == "REJECTED" }
            "DEPOSIT" -> transactions.filter { it.type == "DEPOSIT" }
            "WITHDRAWAL" -> transactions.filter { it.type == "WITHDRAWAL" }
            else -> transactions
        }
    }

    val totalDepositsSum = transactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" }.sumOf { it.amount }
    val totalWithdrawalsSum = transactions.filter { it.type == "WITHDRAWAL" && it.status == "APPROVED" }.sumOf { it.amount }
    val pendingCount = transactions.count { it.status == "PENDING" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Main Category Navigation Tabs
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = SurfaceDark,
                contentColor = GoldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = GoldPrimary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = if (selectedSection == 0) GoldPrimary else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRANSACTION HISTORY (${transactions.size})",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSection == 0) GoldPrimary else TextMuted
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_transaction_history")
                )

                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = if (selectedSection == 1) GoldPrimary else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GAME BETS (${bets.size})",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSection == 1) GoldPrimary else TextMuted
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_game_bets_history")
                )
            }
        }

        if (selectedSection == 0) {
            // === TRANSACTION HISTORY (DEPOSIT & WITHDRAWAL REQUESTS) ===
            item {
                // Summary Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Approved Deposits", color = TextMuted, fontSize = 10.5.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${String.format("%.2f", totalDepositsSum)}",
                                color = NeonGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Paid Withdrawals", color = TextMuted, fontSize = 10.5.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${String.format("%.2f", totalWithdrawalsSum)}",
                                color = NeonCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(0.9f),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Pending", color = TextMuted, fontSize = 10.5.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$pendingCount Requests",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // Status Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "All (${transactions.size})",
                        "PENDING" to "⏳ Pending (${transactions.count { it.status == "PENDING" }})",
                        "APPROVED" to "✅ Approved (${transactions.count { it.status == "APPROVED" }})",
                        "REJECTED" to "❌ Rejected (${transactions.count { it.status == "REJECTED" }})",
                        "DEPOSIT" to "Deposits",
                        "WITHDRAWAL" to "Withdrawals"
                    )

                    items(filters) { (key, label) ->
                        val isSelected = statusFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GoldPrimary else CardDark)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldPrimary else CardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { statusFilter = key }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("filter_tx_$key")
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            // List of Filtered Transactions
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No transactions found",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Submit a deposit via Official QR Code or request a withdrawal in the Wallet tab to see records here.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions) { tx ->
                    TransactionItemCard(tx = tx, dateFormat = dateFormat)
                }
            }
        } else {
            // === GAME BETS SECTION ===
            if (bets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎰", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No game bet records yet",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Play Aviator, Chicken Road, Hot Spin, Crazy 777, Vortex Boom, or Coin Flip to see win streaks.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(bets) { bet ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val emoji = when (bet.gameType) {
                                        "AVIATOR" -> "✈️"
                                        "CHICKEN_ROAD" -> "🐔"
                                        "HOT_SPIN" -> "🔥"
                                        "CRAZY_777" -> "🎰"
                                        "VORTEX_BOOM" -> "🌀"
                                        else -> "🪙"
                                    }
                                    Text(
                                        text = "$emoji ${bet.gameType.replace('_', ' ')}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (bet.isWin) NeonGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (bet.isWin) "${String.format("%.2f", bet.multiplier)}X WIN" else "LOST",
                                            color = if (bet.isWin) NeonGreen else NeonRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Text(
                                    text = bet.detail,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Text(
                                    text = dateFormat.format(Date(bet.timestamp)),
                                    color = TextMuted,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (bet.isWin) "+₹${String.format("%.2f", bet.winAmount)}" else "-₹${String.format("%.2f", bet.betAmount)}",
                                    color = if (bet.isWin) NeonGreen else NeonRed,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Bet: ₹${bet.betAmount.toInt()}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TransactionItemCard(
    tx: TransactionEntity,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    val isDeposit = tx.type == "DEPOSIT"

    // Status Styling: Pending (Amber), Approved (Green), Rejected (Red)
    val (statusLabel, statusBg, statusColor, statusIcon) = when (tx.status) {
        "APPROVED" -> Quadruple("APPROVED", NeonGreen.copy(alpha = 0.15f), NeonGreen, Icons.Default.CheckCircle)
        "REJECTED" -> Quadruple("REJECTED", NeonRed.copy(alpha = 0.15f), NeonRed, Icons.Default.ErrorOutline)
        else -> Quadruple("PENDING", GoldPrimary.copy(alpha = 0.15f), GoldPrimary, Icons.Default.HourglassEmpty)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tx_card_${tx.id}"),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (tx.status == "PENDING") GoldPrimary.copy(alpha = 0.4f) else CardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type icon + Title + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDeposit) NeonGreen.copy(alpha = 0.18f) else NeonCyan.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isDeposit) NeonGreen else NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isDeposit) "UPI Deposit" else "Withdrawal Request",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isDeposit) "Via: Official Merchant QR" else "Method: ${tx.method}",
                            color = TextMuted,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Amount
                Text(
                    text = "₹${String.format("%.2f", tx.amount)}",
                    color = if (isDeposit) NeonGreen else GoldPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reference Detail Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDeposit) "UTR: ${tx.utrOrAccount}" else "Account: ${tx.utrOrAccount}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Formatted Timestamp + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(tx.timestamp)),
                    color = TextMuted,
                    fontSize = 11.sp
                )

                // Status Badge (Pending / Approved / Rejected)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("status_badge_${tx.status.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Optional note
            if (tx.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${tx.note}",
                    color = TextMuted,
                    fontSize = 10.5.sp
                )
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
