package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.CashFlowViewModel
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Color palettes matching RAXI.WIN WinGo in video
private val WinGoBg = Color(0xFF1D0E14)
private val CardBgDark = Color(0xFF29151D)
private val GreenColor = Color(0xFF10B981)
private val VioletColor = Color(0xFF8B5CF6)
private val RedColor = Color(0xFFEF4444)
private val BigColor = Color(0xFFF59E0B)
private val SmallColor = Color(0xFF3B82F6)

data class WinGoHistoryItem(
    val period: String,
    val number: Int,
    val isBig: Boolean,
    val isGreen: Boolean,
    val isViolet: Boolean,
    val isRed: Boolean
)

data class WinGoUserBet(
    val intervalLabel: String,
    val period: String,
    val targetType: String, // "GREEN", "VIOLET", "RED", "BIG", "SMALL", "NUM_X"
    val targetLabel: String,
    val amount: Double,
    var resultNumber: Int? = null,
    var wonAmount: Double = 0.0,
    var isWin: Boolean? = null
)

data class WinModalInfo(
    val intervalLabel: String,
    val period: String,
    val winningNum: Int,
    val wonAmount: Double,
    val targetLabel: String
)

enum class WinGoInterval(val label: String, val seconds: Int) {
    SEC_30("WinGo 30Sec", 30),
    MIN_1("WinGo 1 Min", 60),
    MIN_3("WinGo 3 Min", 180),
    MIN_5("WinGo 5 Min", 300)
}

class WinGoIntervalState(
    val interval: WinGoInterval,
    initialRemaining: Int,
    initialPeriodCounter: Long,
    initialHistory: List<WinGoHistoryItem>,
    val intervalPrefix: String
) {
    var remainingSeconds by mutableIntStateOf(initialRemaining)
    var periodCounter by mutableLongStateOf(initialPeriodCounter)
    val historyList = mutableStateListOf<WinGoHistoryItem>().apply { addAll(initialHistory) }
    val activeBets = mutableStateListOf<WinGoUserBet>()

    fun getCurrentPeriodStr(dateFormat: SimpleDateFormat): String {
        val dateStr = dateFormat.format(Date())
        return "${dateStr}${intervalPrefix}" + String.format(Locale.US, "%05d", periodCounter)
    }
}

fun createRealTimeWinGoState(interval: WinGoInterval, dateFormat: SimpleDateFormat): WinGoIntervalState {
    val cal = java.util.Calendar.getInstance()
    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = cal.get(java.util.Calendar.MINUTE)
    val second = cal.get(java.util.Calendar.SECOND)
    val totalSecondsToday = hour * 3600 + minute * 60 + second

    // Prefix exactly matching video: 1000 for 30s, 1001 for 1m, 1003 for 3m, 1005 for 5m
    val prefix = when (interval) {
        WinGoInterval.SEC_30 -> "10005"
        WinGoInterval.MIN_1 -> "10015"
        WinGoInterval.MIN_3 -> "10035"
        WinGoInterval.MIN_5 -> "10055"
    }

    val basePeriod = 1500L + (totalSecondsToday / interval.seconds)
    val remainingSec = interval.seconds - (totalSecondsToday % interval.seconds)
    val dateStr = dateFormat.format(Date())

    // Generate 500 history records (50 pages of 10 items) for browsing back and forth!
    val history = (1..500).map { i ->
        val pastRound = basePeriod - i
        val safeRound = if (pastRound > 0) pastRound else (50000 + pastRound)
        val periodStr = "${dateStr}${prefix}" + String.format(Locale.US, "%04d", safeRound % 10000)
        
        // Realistic distribution
        val hash = kotlin.math.abs((periodStr.hashCode() * 37 + i * 19 + interval.seconds * 11))
        val num = hash % 10
        WinGoHistoryItem(
            period = periodStr,
            number = num,
            isBig = num >= 5,
            isGreen = num in listOf(1, 3, 5, 7, 9),
            isViolet = num in listOf(0, 5),
            isRed = num in listOf(0, 2, 4, 6, 8)
        )
    }

    return WinGoIntervalState(
        interval = interval,
        initialRemaining = remainingSec,
        initialPeriodCounter = basePeriod,
        initialHistory = history,
        intervalPrefix = prefix
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinGoGameScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val currentUser by viewModel.currentUser.collectAsState()
    val balance = currentUser?.balance ?: 0.0

    val dateFormat = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()) }

    var selectedInterval by remember { mutableStateOf(WinGoInterval.SEC_30) }

    // 4 Independent real-time state engines for each interval that change period every round!
    val state30s = remember { createRealTimeWinGoState(WinGoInterval.SEC_30, dateFormat) }
    val state1m = remember { createRealTimeWinGoState(WinGoInterval.MIN_1, dateFormat) }
    val state3m = remember { createRealTimeWinGoState(WinGoInterval.MIN_3, dateFormat) }
    val state5m = remember { createRealTimeWinGoState(WinGoInterval.MIN_5, dateFormat) }

    // Active state corresponding to currently selected interval tab
    val activeState = when (selectedInterval) {
        WinGoInterval.SEC_30 -> state30s
        WinGoInterval.MIN_1 -> state1m
        WinGoInterval.MIN_3 -> state3m
        WinGoInterval.MIN_5 -> state5m
    }

    val userBetsList = remember { mutableStateListOf<WinGoUserBet>() }
    var winningPopupData by remember { mutableStateOf<WinModalInfo?>(null) }

    // Betting Modal State
    var selectedBetType by remember { mutableStateOf<String?>(null) }
    var selectedBetLabel by remember { mutableStateOf("") }
    var selectedBetColor by remember { mutableStateOf(GreenColor) }
    var baseBalanceChip by remember { mutableDoubleStateOf(1.0) }
    var quantityCount by remember { mutableIntStateOf(1) }
    var selectedMultiplier by remember { mutableIntStateOf(1) }
    var agreePreSale by remember { mutableStateOf(true) }

    var activeTab by remember { mutableStateOf("GAME_HISTORY") } // "GAME_HISTORY", "CHART", "MY_HISTORY"

    // Pagination states for all tabs (aage piche dekh sake)
    var historyPage by remember { mutableIntStateOf(1) }
    var chartPage by remember { mutableIntStateOf(1) }
    var myHistoryPage by remember { mutableIntStateOf(1) }

    fun processRoundComplete(state: WinGoIntervalState) {
        val winningNum = Random.nextInt(0, 10)
        val isBig = winningNum >= 5
        val isGreen = winningNum in listOf(1, 3, 5, 7, 9)
        val isViolet = winningNum in listOf(0, 5)
        val isRed = winningNum in listOf(0, 2, 4, 6, 8)
        val periodStr = state.getCurrentPeriodStr(dateFormat)

        val newPeriodItem = WinGoHistoryItem(
            period = periodStr,
            number = winningNum,
            isBig = isBig,
            isGreen = isGreen,
            isViolet = isViolet,
            isRed = isRed
        )
        // Automatically drop at top (auto par girta rahe)
        state.historyList.add(0, newPeriodItem)
        if (state.historyList.size > 1000) state.historyList.removeLast()

        var totalRoundWin = 0.0
        var winningBetSummary = ""
        var hadWin = false

        // Evaluate Active Bets for this specific interval
        state.activeBets.forEach { bet ->
            var win = false
            var multiplier = 0.0

            when (bet.targetType) {
                "GREEN" -> {
                    if (winningNum in listOf(1, 3, 7, 9)) {
                        win = true
                        multiplier = 2.0
                    } else if (winningNum == 5) {
                        win = true
                        multiplier = 1.5
                    }
                }
                "VIOLET" -> {
                    if (winningNum in listOf(0, 5)) {
                        win = true
                        multiplier = 4.5
                    }
                }
                "RED" -> {
                    if (winningNum in listOf(2, 4, 6, 8)) {
                        win = true
                        multiplier = 2.0
                    } else if (winningNum == 0) {
                        win = true
                        multiplier = 1.5
                    }
                }
                "BIG" -> {
                    if (winningNum >= 5) {
                        win = true
                        multiplier = 2.0
                    }
                }
                "SMALL" -> {
                    if (winningNum <= 4) {
                        win = true
                        multiplier = 2.0
                    }
                }
                else -> {
                    if (bet.targetType.startsWith("NUM_")) {
                        val targetN = bet.targetType.removePrefix("NUM_").toIntOrNull()
                        if (targetN == winningNum) {
                            win = true
                            multiplier = 9.0
                        }
                    }
                }
            }

            val winAmount = if (win) bet.amount * multiplier else 0.0
            bet.resultNumber = winningNum
            bet.wonAmount = winAmount
            bet.isWin = win
            userBetsList.add(0, bet)

            if (win) {
                hadWin = true
                totalRoundWin += winAmount
                winningBetSummary = bet.targetLabel
            }

            viewModel.placeGameBet(
                gameType = "WINGO",
                betAmount = bet.amount,
                multiplier = multiplier,
                isWin = win,
                detail = "[${state.interval.label}] Period: ${bet.period} | Bet: ${bet.targetLabel} | Result: $winningNum"
            )
        }

        if (hadWin && selectedInterval == state.interval) {
            winningPopupData = WinModalInfo(
                intervalLabel = state.interval.label,
                period = periodStr,
                winningNum = winningNum,
                wonAmount = totalRoundWin,
                targetLabel = winningBetSummary
            )
        }

        state.activeBets.clear()
        state.periodCounter++
        state.remainingSeconds = state.interval.seconds
    }

    // 4 Parallel Background Coroutine Engines for Each Interval (24/7 continuous auto drop)
    LaunchedEffect(Unit) {
        launch {
            while (true) {
                delay(1000)
                if (state30s.remainingSeconds > 0) {
                    state30s.remainingSeconds--
                } else {
                    processRoundComplete(state30s)
                }
            }
        }
        launch {
            while (true) {
                delay(1000)
                if (state1m.remainingSeconds > 0) {
                    state1m.remainingSeconds--
                } else {
                    processRoundComplete(state1m)
                }
            }
        }
        launch {
            while (true) {
                delay(1000)
                if (state3m.remainingSeconds > 0) {
                    state3m.remainingSeconds--
                } else {
                    processRoundComplete(state3m)
                }
            }
        }
        launch {
            while (true) {
                delay(1000)
                if (state5m.remainingSeconds > 0) {
                    state5m.remainingSeconds--
                } else {
                    processRoundComplete(state5m)
                }
            }
        }
    }

    val isLockPeriod = activeState.remainingSeconds <= 5 // Last 5 seconds lock

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WinGoBg)
            .statusBarsPadding()
    ) {
        // 1. TOP HEADER (Back button, RAXI.WIN Brand Logo, Audio/Help Icons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF241018))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp).testTag("wingo_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // RAXI.WIN Brand badge (exact style from video)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3B1522))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RAXI",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = ".WIN",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Audio", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Help", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 2. WALLET BALANCE CARD (Identical to video)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2C1620)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF4A2536))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${String.format(Locale.US, "%.2f", balance)}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color(0xFFE2E8F0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Wallet balance",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Withdraw & Deposit buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFE11D48))
                                    .clickable { onBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Withdraw", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF10B981))
                                    .clickable { onBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Deposit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                        }
                    }
                }
            }

            // Announcement Ticker (from video)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2B141E))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = RedColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dear User, welcome to RAXI.WIN! RAXI.WIN is a brand-new premium entertainment platform...",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE11D48))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Detail", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. TIME INTERVAL TABS (30Sec, 1 Min, 3 Min, 5 Min)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WinGoInterval.entries.forEach { interval ->
                        val isSelected = selectedInterval == interval
                        val intState = when (interval) {
                            WinGoInterval.SEC_30 -> state30s
                            WinGoInterval.MIN_1 -> state1m
                            WinGoInterval.MIN_3 -> state3m
                            WinGoInterval.MIN_5 -> state5m
                        }
                        val rem = intState.remainingSeconds
                        val remMin = rem / 60
                        val remSec = rem % 60
                        val remText = if (remMin > 0) String.format(Locale.US, "%02d:%02d", remMin, remSec) else "${rem}s"

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedInterval = interval
                                    historyPage = 1
                                    chartPage = 1
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF6B1D36) else Color(0xFF28131D)
                            ),
                            border = BorderStroke(1.dp, if (isSelected) RedColor else Color(0xFF451E2E))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 7.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🕒",
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = interval.label.replace("WinGo ", ""),
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = remText,
                                    color = if (isSelected) Color(0xFFFCD34D) else Color(0xFF64748B),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. LIVE TICKET AREA (Peach/Salmon gradient ticket, history balls, flip clock countdown)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF421C2A)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF68283F))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Game title & Past 5 balls
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedInterval.label,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "How to play",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 5 History Result Balls
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                activeState.historyList.take(5).forEach { hItem ->
                                    MiniHistoryBall(hItem.number)
                                }
                            }
                        }

                        // Right: Time Remaining Flip Clock & Period ID
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Time remaining",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val minutes = activeState.remainingSeconds / 60
                            val seconds = activeState.remainingSeconds % 60
                            val mStr = String.format(Locale.US, "%02d", minutes)
                            val sStr = String.format(Locale.US, "%02d", seconds)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                DigitBox(mStr[0])
                                DigitBox(mStr[1])
                                Text(text = ":", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                DigitBox(sStr[0])
                                DigitBox(sStr[1])
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Period: ",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = activeState.getCurrentPeriodStr(dateFormat),
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // 5. BETTING BOARD & GIANT 5-SECOND COUNTDOWN OVERLAY
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF26121B))
                            .border(1.dp, Color(0xFF451E2E), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        // Color Buttons Row: GREEN, VIOLET, RED
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ColorBetButton(
                                label = "Green",
                                color = GreenColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                selectedBetType = "GREEN"
                                selectedBetLabel = "Green"
                                selectedBetColor = GreenColor
                            }

                            ColorBetButton(
                                label = "Violet",
                                color = VioletColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                selectedBetType = "VIOLET"
                                selectedBetLabel = "Violet"
                                selectedBetColor = VioletColor
                            }

                            ColorBetButton(
                                label = "Red",
                                color = RedColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                selectedBetType = "RED"
                                selectedBetLabel = "Red"
                                selectedBetColor = RedColor
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Number Balls (0 to 9 in 2 rows of 5 balls)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Row 1: 0, 1, 2, 3, 4
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (num in 0..4) {
                                    NumberBallButton(num) {
                                        selectedBetType = "NUM_$num"
                                        selectedBetLabel = "$num"
                                        selectedBetColor = getNumberBallColor(num)
                                    }
                                }
                            }

                            // Row 2: 5, 6, 7, 8, 9
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (num in 5..9) {
                                    NumberBallButton(num) {
                                        selectedBetType = "NUM_$num"
                                        selectedBetLabel = "$num"
                                        selectedBetColor = getNumberBallColor(num)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Multipliers Row: Random, X1, X5, X10, X20, X50, X100
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF3B1826))
                                    .clickable {
                                        val randNum = Random.nextInt(0, 10)
                                        selectedBetType = "NUM_$randNum"
                                        selectedBetLabel = "$randNum"
                                        selectedBetColor = getNumberBallColor(randNum)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Random", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }

                            listOf(1, 5, 10, 20, 50, 100).forEach { mult ->
                                val isSel = selectedMultiplier == mult
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) GreenColor else Color(0xFF3B1826))
                                        .clickable { selectedMultiplier = mult },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "X$mult",
                                        color = if (isSel) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Big and Small Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(BigColor)
                                    .clickable {
                                        selectedBetType = "BIG"
                                        selectedBetLabel = "Big"
                                        selectedBetColor = BigColor
                                    }
                                    .testTag("wingo_btn_big"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Big", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(SmallColor)
                                    .clickable {
                                        selectedBetType = "SMALL"
                                        selectedBetLabel = "Small"
                                        selectedBetColor = SmallColor
                                    }
                                    .testTag("wingo_btn_small"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Small", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }
                    }

                    // 💥 GIANT 5-SECOND COUNTDOWN OVERLAY (from video 00:33 - 00:38)
                    if (isLockPeriod) {
                        val sStr = String.format(Locale.US, "%02d", activeState.remainingSeconds)
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.78f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                GiantCountdownCard(sStr[0])
                                GiantCountdownCard(sStr[1])
                            }
                        }
                    }
                }
            }

            // 6. HISTORY TABS (Game history, Chart, My history)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        "GAME_HISTORY" to "Game history",
                        "CHART" to "Chart",
                        "MY_HISTORY" to "My history"
                    ).forEach { (key, label) ->
                        val isSel = activeTab == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) RedColor else Color(0xFF28131D))
                                .clickable { activeTab = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // TAB 1: GAME HISTORY WITH PAGINATION (1/50, 2/50, 3/50...)
            // -------------------------------------------------------------
            if (activeTab == "GAME_HISTORY") {
                val itemsPerPage = 10
                val totalHistoryPages = maxOf(1, (activeState.historyList.size + itemsPerPage - 1) / itemsPerPage)
                val safeHistoryPage = historyPage.coerceIn(1, totalHistoryPages)
                val startIdx = (safeHistoryPage - 1) * itemsPerPage
                val currentHistoryItems = activeState.historyList.drop(startIdx).take(itemsPerPage)

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF28131D)),
                        border = BorderStroke(1.dp, Color(0xFF451E2E))
                    ) {
                        Column {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF381827))
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Period", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
                                Text(text = "Number", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(text = "Big Small", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(text = "Color", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }

                            // Table Rows (browsable back and forth!)
                            currentHistoryItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 12.dp)
                                        .border(0.5.dp, Color(0xFF381827), RoundedCornerShape(0.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.period,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1.8f)
                                    )

                                    // Number with color
                                    val numColor = when {
                                        item.isViolet -> VioletColor
                                        item.isGreen -> GreenColor
                                        else -> RedColor
                                    }
                                    Text(
                                        text = item.number.toString(),
                                        color = numColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = if (item.isBig) "Big" else "Small",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )

                                    // Color Dot (Green, Red, or two-tone)
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        item.isViolet && item.isGreen -> Brush.horizontalGradient(listOf(GreenColor, VioletColor))
                                                        item.isViolet && item.isRed -> Brush.horizontalGradient(listOf(RedColor, VioletColor))
                                                        item.isGreen -> Brush.linearGradient(listOf(GreenColor, GreenColor))
                                                        else -> Brush.linearGradient(listOf(RedColor, RedColor))
                                                    }
                                                )
                                        )
                                    }
                                }
                            }

                            // Pagination Row (< 1/50 >)
                            WinGoPaginationRow(
                                currentPage = safeHistoryPage,
                                totalPages = totalHistoryPages,
                                onPageChange = { historyPage = it }
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // TAB 2: CHART WITH STATISTICS & TREND GRAPH (00:20 in video)
            // -------------------------------------------------------------
            else if (activeTab == "CHART") {
                val last100 = activeState.historyList.take(100)
                val itemsPerPage = 10
                val totalChartPages = maxOf(1, (activeState.historyList.size + itemsPerPage - 1) / itemsPerPage)
                val safeChartPage = chartPage.coerceIn(1, totalChartPages)
                val startIdx = (safeChartPage - 1) * itemsPerPage
                val currentChartItems = activeState.historyList.drop(startIdx).take(itemsPerPage)

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF28131D)),
                        border = BorderStroke(1.dp, Color(0xFF451E2E))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Section 1: Statistic (last 100 Periods)
                            Text(
                                text = "Statistic (last 100 Periods)",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            // Header Row for 0 to 9 numbers
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF381827))
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Winning Numbers",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(2.2f)
                                )
                                (0..9).forEach { num ->
                                    val col = getNumberBallColor(num)
                                    Text(
                                        text = "$num",
                                        color = col,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Stat Row: Missing
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Missing", color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.weight(2.2f))
                                (0..9).forEach { digit ->
                                    val miss = last100.indexOfFirst { it.number == digit }.let { if (it == -1) 100 else it }
                                    Text(
                                        text = "$miss",
                                        color = if (miss == 0) NeonGreen else Color(0xFFCBD5E1),
                                        fontSize = 10.sp,
                                        fontWeight = if (miss == 0) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Stat Row: Avg missing
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Avg missing", color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.weight(2.2f))
                                (0..9).forEach { digit ->
                                    val count = last100.count { it.number == digit }
                                    val avg = if (count > 0) 100 / count else 100
                                    Text(
                                        text = "$avg",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Stat Row: Frequency
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Frequency", color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.weight(2.2f))
                                (0..9).forEach { digit ->
                                    val freq = last100.count { it.number == digit }
                                    Text(
                                        text = "$freq",
                                        color = if (freq >= 12) NeonGreen else Color(0xFFCBD5E1),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Stat Row: Max consecutive
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Max consecutive", color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.weight(2.2f))
                                (0..9).forEach { digit ->
                                    var maxStreak = 0
                                    var curStreak = 0
                                    last100.forEach { item ->
                                        if (item.number == digit) {
                                            curStreak++
                                            if (curStreak > maxStreak) maxStreak = curStreak
                                        } else {
                                            curStreak = 0
                                        }
                                    }
                                    Text(
                                        text = "$maxStreak",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Section 2: Trend Chart Rows with Line Chart Connecting Points
                            Text(
                                text = "Trend Chart (${activeState.interval.label})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            // Chart Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF381827))
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Period",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.8f)
                                )
                                Text(
                                    text = "Number",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(3.5f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "B/S",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Chart Table with interactive visual line overlay
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    currentChartItems.forEach { cItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Period text
                                            Text(
                                                text = cItem.period,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.weight(1.8f)
                                            )

                                            // 10 Number Circles (0 to 9)
                                            Row(
                                                modifier = Modifier.weight(3.5f),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                for (d in 0..9) {
                                                    val isWinning = cItem.number == d
                                                    val color = getNumberBallColor(d)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(19.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isWinning) color else Color(0xFF381827).copy(alpha = 0.6f)
                                                            )
                                                            .border(
                                                                width = if (isWinning) 1.dp else 0.5.dp,
                                                                color = if (isWinning) Color.White else Color(0xFF451E2E),
                                                                shape = CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "$d",
                                                            color = if (isWinning) Color.White else Color(0xFF64748B),
                                                            fontSize = 9.sp,
                                                            fontWeight = if (isWinning) FontWeight.Black else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }

                                            // Big/Small badge
                                            Box(
                                                modifier = Modifier
                                                    .weight(0.7f)
                                                    .padding(start = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(if (cItem.isBig) BigColor else SmallColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = if (cItem.isBig) "B" else "S",
                                                        color = Color.White,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Pagination Row (< 1/50 >) for Chart
                            WinGoPaginationRow(
                                currentPage = safeChartPage,
                                totalPages = totalChartPages,
                                onPageChange = { chartPage = it }
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // TAB 3: MY HISTORY (Personal Bet Records)
            // -------------------------------------------------------------
            else if (activeTab == "MY_HISTORY") {
                if (userBetsList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF28131D))
                        ) {
                            Text(
                                text = "No bets placed yet. Tap any color or number to start!",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.5.sp,
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val itemsPerPage = 10
                    val totalMyPages = maxOf(1, (userBetsList.size + itemsPerPage - 1) / itemsPerPage)
                    val safeMyPage = myHistoryPage.coerceIn(1, totalMyPages)
                    val startIdx = (safeMyPage - 1) * itemsPerPage
                    val currentMyBets = userBetsList.drop(startIdx).take(itemsPerPage)

                    items(currentMyBets) { bet ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF28131D)),
                            border = BorderStroke(1.dp, Color(0xFF451E2E))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "${bet.intervalLabel} | Period: ${bet.period}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text(text = "Selection: ${bet.targetLabel}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Bet: ₹${bet.amount.toInt()}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (bet.isWin == true) {
                                        Text(text = "+₹${String.format(Locale.US, "%.2f", bet.wonAmount)}", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                        Text(text = "WIN", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    } else if (bet.isWin == false) {
                                        Text(text = "-₹${bet.amount.toInt()}", color = NeonRed, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                        Text(text = "LOSE", color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    } else {
                                        Text(text = "WAITING RESULT", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (totalMyPages > 1) {
                        item {
                            WinGoPaginationRow(
                                currentPage = safeMyPage,
                                totalPages = totalMyPages,
                                onPageChange = { myHistoryPage = it }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // 7. BETTING BOTTOM SHEET MODAL (Exact match to video)
    if (selectedBetType != null) {
        val totalAmount = baseBalanceChip * quantityCount * selectedMultiplier

        ModalBottomSheet(
            onDismissRequest = { selectedBetType = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF2C1420),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(selectedBetColor)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedInterval.label,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Select $selectedBetLabel",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Balance Selection (1, 10, 100, 1000)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Balance", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1.0, 10.0, 100.0, 1000.0).forEach { chip ->
                                val isSel = baseBalanceChip == chip
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) selectedBetColor else Color(0xFF381827))
                                        .clickable { baseBalanceChip = chip }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${chip.toInt()}",
                                        color = if (isSel) Color.White else Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quantity Counter (- 1 +)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Quantity", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(selectedBetColor)
                                    .clickable { if (quantityCount > 1) quantityCount-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "-", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }

                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(34.dp)
                                    .background(Color(0xFF381827)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "$quantityCount", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(selectedBetColor)
                                    .clickable { quantityCount++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multipliers Row: X1, X5, X10, X20, X50, X100
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 5, 10, 20, 50, 100).forEach { mult ->
                            val isSel = selectedMultiplier == mult
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) selectedBetColor else Color(0xFF451E2E))
                                    .clickable { selectedMultiplier = mult },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "X$mult",
                                    color = if (isSel) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pre-sale Agreement
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { agreePreSale = !agreePreSale }
                    ) {
                        Checkbox(
                            checked = agreePreSale,
                            onCheckedChange = { agreePreSale = it },
                            colors = CheckboxDefaults.colors(checkedColor = selectedBetColor)
                        )
                        Text(text = "I agree (Pre-sale rules)", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Footer Buttons: Cancel vs Total Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { selectedBetType = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381827)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (balance >= totalAmount && agreePreSale && !isLockPeriod) {
                                    val newBet = WinGoUserBet(
                                        intervalLabel = activeState.interval.label,
                                        period = activeState.getCurrentPeriodStr(dateFormat),
                                        targetType = selectedBetType!!,
                                        targetLabel = selectedBetLabel,
                                        amount = totalAmount
                                    )
                                    activeState.activeBets.add(newBet)
                                    selectedBetType = null
                                }
                            },
                            enabled = balance >= totalAmount && agreePreSale && !isLockPeriod,
                            modifier = Modifier
                                .weight(1.8f)
                                .height(46.dp)
                                .testTag("wingo_confirm_bet_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = selectedBetColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Total amount ₹${String.format(Locale.US, "%.2f", totalAmount)}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // 8. WIN CELEBRATION MODAL POPUP
    if (winningPopupData != null) {
        val winData = winningPopupData!!
        Dialog(onDismissRequest = { winningPopupData = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1420)),
                border = BorderStroke(2.dp, GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 CONGRATULATIONS! 🎉",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = winData.intervalLabel,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Period: ${winData.period}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Winning Number: ",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MiniHistoryBall(winData.winningNum)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF381827))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "You Bet: ${winData.targetLabel}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+₹${String.format(Locale.US, "%.2f", winData.wonAmount)}",
                                color = NeonGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { winningPopupData = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RedColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(text = "CONTINUE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER COMPONENTS FOR WINGO UI
// -------------------------------------------------------------

@Composable
private fun WinGoPaginationRow(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev button [<]
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (currentPage > 1) Color(0xFFE11D48) else Color(0xFF381827))
                .border(1.dp, if (currentPage > 1) Color(0xFFF43F5E) else Color(0xFF451E2E), RoundedCornerShape(8.dp))
                .clickable(enabled = currentPage > 1) { onPageChange(currentPage - 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Page",
                tint = if (currentPage > 1) Color.White else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        // Page indicator "1/50"
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF241018))
                .border(1.dp, Color(0xFF451E2E), RoundedCornerShape(6.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$currentPage/$totalPages",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        // Next button [>]
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (currentPage < totalPages) Color(0xFFE11D48) else Color(0xFF381827))
                .border(1.dp, if (currentPage < totalPages) Color(0xFFF43F5E) else Color(0xFF451E2E), RoundedCornerShape(8.dp))
                .clickable(enabled = currentPage < totalPages) { onPageChange(currentPage + 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Page",
                tint = if (currentPage < totalPages) Color.White else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ColorBetButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable(onClick = onClick)
            .testTag("wingo_color_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}

@Composable
private fun NumberBallButton(
    number: Int,
    onClick: () -> Unit
) {
    val isRed = number in listOf(2, 4, 6, 8)
    val isGreen = number in listOf(1, 3, 7, 9)
    val isZero = number == 0
    val isFive = number == 5

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
                when {
                    isZero -> Brush.horizontalGradient(listOf(RedColor, VioletColor))
                    isFive -> Brush.horizontalGradient(listOf(GreenColor, VioletColor))
                    isGreen -> Brush.radialGradient(listOf(Color(0xFF34D399), GreenColor, Color(0xFF065F46)))
                    else -> Brush.radialGradient(listOf(Color(0xFFF87171), RedColor, Color(0xFF991B1B)))
                }
            )
            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            .clickable(onClick = onClick)
            .testTag("wingo_ball_$number"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
        )
    }
}

private fun getNumberBallColor(number: Int): Color {
    return when (number) {
        0 -> VioletColor
        5 -> VioletColor
        1, 3, 7, 9 -> GreenColor
        else -> RedColor
    }
}

@Composable
private fun MiniHistoryBall(number: Int) {
    val isZero = number == 0
    val isFive = number == 5
    val isGreen = number in listOf(1, 3, 7, 9)

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                when {
                    isZero -> Brush.horizontalGradient(listOf(RedColor, VioletColor))
                    isFive -> Brush.horizontalGradient(listOf(GreenColor, VioletColor))
                    isGreen -> Brush.linearGradient(listOf(GreenColor, GreenColor))
                    else -> Brush.linearGradient(listOf(RedColor, RedColor))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$number", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun DigitBox(char: Char) {
    Box(
        modifier = Modifier
            .size(width = 18.dp, height = 24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF28101C))
            .border(0.5.dp, Color(0xFF6B2840), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$char",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun GiantCountdownCard(char: Char) {
    // 💥 Giant Red Flip-Card (from video at 00:33 - 00:38)
    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFDC2626), Color(0xFF991B1B))
                )
            )
            .border(2.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$char",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 90.sp,
            textAlign = TextAlign.Center
        )
    }
}
