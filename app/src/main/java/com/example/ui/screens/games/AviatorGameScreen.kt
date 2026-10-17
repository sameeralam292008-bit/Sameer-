package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CashFlowViewModel
import com.example.ui.theme.BgDark
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
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class AviatorState {
    WAITING_BETS, // Live countdown timer (e.g. 5s... 4s... 1s)
    FLYING,       // Jet in air, multiplier climbing
    CRASHED       // Flew away
}

data class LivePlayerBet(
    val name: String,
    val bet: Double,
    val cashOutMult: Double? = null
)

@Composable
fun AviatorGameScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val currentUser by viewModel.currentUser.collectAsState()
    val balance = currentUser?.balance ?: 0.0

    var gameState by remember { mutableStateOf(AviatorState.WAITING_BETS) }
    var countdownSeconds by remember { mutableIntStateOf(5) }
    var countdownProgress by remember { mutableDoubleStateOf(1.0) }

    var currentMultiplier by remember { mutableDoubleStateOf(1.00) }
    var betAmount by remember { mutableDoubleStateOf(50.0) }
    var isBetQueued by remember { mutableStateOf(false) } // Bet placed for next round
    var isBetActiveInFlight by remember { mutableStateOf(false) } // Currently flying with bet
    var cashedOutMultiplier by remember { mutableDoubleStateOf(0.0) }
    var messageText by remember { mutableStateOf("Place your bet for the next takeoff!") }

    val history = remember {
        mutableStateListOf(1.42, 2.85, 1.12, 5.40, 1.88, 3.10, 1.05, 12.45)
    }

    val simulatedPlayers = remember {
        mutableStateListOf(
            LivePlayerBet("Aryan K.", 200.0),
            LivePlayerBet("Vikram S.", 500.0),
            LivePlayerBet("Pooja M.", 100.0),
            LivePlayerBet("Rahul D.", 350.0),
            LivePlayerBet("Sneha R.", 150.0)
        )
    }

    val flightProgress = remember { Animatable(0f) }

    // REALISTIC CONTINUOUS ROUND LIFECYCLE WITH ROUND TIMER
    LaunchedEffect(gameState) {
        when (gameState) {
            AviatorState.WAITING_BETS -> {
                flightProgress.snapTo(0f)
                currentMultiplier = 1.00
                cashedOutMultiplier = 0.0

                // Reset simulated players for new round
                val sampleNames = listOf("Aman G.", "Rohan S.", "Karan M.", "Priya N.", "Deepak T.", "Ankit B.")
                simulatedPlayers.clear()
                sampleNames.shuffled().take(4).forEach { name ->
                    simulatedPlayers.add(LivePlayerBet(name, listOf(50.0, 100.0, 200.0, 500.0).random()))
                }

                messageText = if (isBetQueued) "Your ₹${betAmount.toInt()} bet is LOCKED IN for takeoff!" else "Place your bet before flight departs!"

                // 5-second countdown timer
                for (s in 5 downTo 1) {
                    countdownSeconds = s
                    countdownProgress = s / 5.0
                    delay(1000)
                }
                countdownSeconds = 0
                countdownProgress = 0.0

                // Transition to flight!
                isBetActiveInFlight = isBetQueued
                isBetQueued = false
                gameState = AviatorState.FLYING
            }

            AviatorState.FLYING -> {
                flightProgress.snapTo(0f)
                currentMultiplier = 1.00
                cashedOutMultiplier = 0.0
                messageText = "Jet took flight! Multiplier rising..."

                // Determine crash point
                val crashPoint = if (Random.nextFloat() < 0.20f) {
                    1.00 + Random.nextDouble(0.08, 0.45) // early crash 20%
                } else if (Random.nextFloat() < 0.70f) {
                    1.50 + Random.nextDouble(0.2, 3.8) // medium 50%
                } else {
                    4.00 + Random.nextDouble(1.0, 16.0) // high jackpot 30%
                }

                val startTime = System.currentTimeMillis()
                var flying = true

                while (flying) {
                    val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
                    val newMult = 1.00 + (elapsedSec * elapsedSec * 0.28) + (elapsedSec * 0.45)
                    currentMultiplier = (newMult * 100).toLong() / 100.0

                    flightProgress.animateTo(
                        targetValue = (elapsedSec / 8.0).toFloat().coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 60, easing = LinearEasing)
                    )

                    // Randomly simulate other players cashing out
                    if (Random.nextFloat() < 0.15f && currentMultiplier > 1.3) {
                        val uncashedIdx = simulatedPlayers.indexOfFirst { it.cashOutMult == null }
                        if (uncashedIdx != -1) {
                            val p = simulatedPlayers[uncashedIdx]
                            simulatedPlayers[uncashedIdx] = p.copy(cashOutMult = currentMultiplier)
                        }
                    }

                    if (currentMultiplier >= crashPoint) {
                        flying = false
                        gameState = AviatorState.CRASHED
                        val finalCrash = (crashPoint * 100).toLong() / 100.0
                        history.add(0, finalCrash)

                        if (isBetActiveInFlight && cashedOutMultiplier == 0.0) {
                            messageText = "FLEW AWAY at ${String.format("%.2f", finalCrash)}x! You lost ₹${betAmount.toInt()}"
                            viewModel.placeGameBet(
                                gameType = "AVIATOR",
                                betAmount = betAmount,
                                multiplier = 0.0,
                                isWin = false,
                                detail = "Flew away at ${String.format("%.2f", finalCrash)}x"
                            )
                        } else if (cashedOutMultiplier > 0.0) {
                            messageText = "FLEW AWAY at ${String.format("%.2f", finalCrash)}x (You won!)"
                        } else {
                            messageText = "FLEW AWAY at ${String.format("%.2f", finalCrash)}x"
                        }
                        isBetActiveInFlight = false
                    }
                    delay(60)
                }
            }

            AviatorState.CRASHED -> {
                // Show crashed state for 2.5 seconds, then auto-restart next countdown
                delay(2500)
                gameState = AviatorState.WAITING_BETS
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
    ) {
        // Top Game Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("aviator_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = NeonRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AVIATOR CRASH",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            // Balance Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "₹${String.format("%.2f", balance)}",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Live Round Timer Bar (Visible during WAITING_BETS)
        if (gameState == AviatorState.WAITING_BETS) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1B4B))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NEXT ROUND TAKEOFF IN 00:0${countdownSeconds}s",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Fast-forward skip wait button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .clickable {
                                isBetActiveInFlight = isBetQueued
                                isBetQueued = false
                                gameState = AviatorState.FLYING
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "LAUNCH NOW", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { countdownProgress.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NeonCyan,
                    trackColor = Color(0xFF312E81)
                )
            }
        }

        // Multiplier History Ribbon
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C101A))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(history) { mult ->
                val pillColor = if (mult >= 10.0) Color(0xFFC084FC) else if (mult >= 2.0) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(pillColor.copy(alpha = 0.15f))
                        .border(1.dp, pillColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${String.format("%.2f", mult)}x",
                        color = pillColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Flight Radar & Jet Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF130E1F), Color(0xFF0C0914))
                    )
                )
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Live Curve & Jet Drawing
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw radar grid lines
                for (i in 1..4) {
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(0f, h * i / 5f),
                        end = Offset(w, h * i / 5f),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(w * i / 5f, 0f),
                        end = Offset(w * i / 5f, h),
                        strokeWidth = 1f
                    )
                }

                if (gameState == AviatorState.FLYING) {
                    val p = flightProgress.value
                    val startX = 20.dp.toPx()
                    val startY = h - 20.dp.toPx()
                    val targetX = startX + (w - 70.dp.toPx()) * p
                    val targetY = startY - (h - 70.dp.toPx()) * (p * p)

                    // Trajectory Path
                    val curvePath = Path().apply {
                        moveTo(startX, startY)
                        quadraticTo(
                            (startX + targetX) / 2f,
                            startY,
                            targetX,
                            targetY
                        )
                    }

                    // Fill under curve
                    val fillPath = Path().apply {
                        addPath(curvePath)
                        lineTo(targetX, startY)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(NeonRed.copy(alpha = 0.35f), Color.Transparent),
                            startY = targetY,
                            endY = startY
                        )
                    )

                    // Glowing curve line
                    drawPath(
                        path = curvePath,
                        color = NeonRed,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Jet Icon / Circle at flight head
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = Offset(targetX, targetY)
                    )
                    drawCircle(
                        color = NeonRed,
                        radius = 12.dp.toPx(),
                        center = Offset(targetX, targetY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Central Multiplier Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (gameState == AviatorState.FLYING) {
                    Text(
                        text = "${String.format("%.2f", currentMultiplier)}x",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp,
                        letterSpacing = 1.sp
                    )
                    if (isBetActiveInFlight && cashedOutMultiplier == 0.0) {
                        Text(
                            text = "Win: ₹${String.format("%.2f", betAmount * currentMultiplier)}",
                            color = NeonGreen,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (gameState == AviatorState.CRASHED) {
                    Text(
                        text = "FLEW AWAY!",
                        color = NeonRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    )
                    Text(
                        text = "@ ${String.format("%.2f", history.firstOrNull() ?: 1.0)}x",
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                } else {
                    // WAITING_BETS with countdown display
                    Text(
                        text = "WAITING FOR TAKEOFF",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Takeoff in 00:0${countdownSeconds}s",
                        color = NeonCyan,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (isBetQueued) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(alpha = 0.2f))
                                .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "BET PLACED: ₹${betAmount.toInt()}",
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (cashedOutMultiplier > 0.0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonGreen)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CASHED OUT: ₹${String.format("%.2f", betAmount * cashedOutMultiplier)}",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Live Simulated Bets Ticker
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(simulatedPlayers) { p ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceDark)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = p.name, color = TextSecondary, fontSize = 10.5.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        if (p.cashOutMult != null) {
                            Text(text = "${String.format("%.2f", p.cashOutMult)}x", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                        } else {
                            Text(text = "₹${p.bet.toInt()}", color = GoldPrimary, fontSize = 10.5.sp)
                        }
                    }
                }
            }
        }

        // Status Message Banner
        Text(
            text = messageText,
            color = if (gameState == AviatorState.CRASHED) NeonRed else GoldPrimary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Bet Controls Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Bet Amount Quick Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Bet Amount", color = TextSecondary, fontSize = 13.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(20.0, 50.0, 100.0, 500.0).forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (betAmount == amt) GoldPrimary else SurfaceDark)
                                    .clickable { betAmount = amt }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "₹${amt.toInt()}",
                                    color = if (betAmount == amt) Color.Black else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount adjustment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "₹ ${betAmount.toInt()}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .clickable { betAmount = (betAmount / 2).coerceAtLeast(10.0) }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Text(text = "/2", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .clickable { betAmount = (betAmount * 2).coerceAtMost(10000.0) }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Text(text = "2X", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Place Bet / Cash Out / Queued Bet
                if (gameState == AviatorState.FLYING && isBetActiveInFlight && cashedOutMultiplier == 0.0) {
                    // Cash Out Button
                    Button(
                        onClick = {
                            val win = betAmount * currentMultiplier
                            cashedOutMultiplier = currentMultiplier
                            messageText = "WIN! Cashed out at ${String.format("%.2f", currentMultiplier)}x (+₹${String.format("%.2f", win)})"
                            viewModel.placeGameBet(
                                gameType = "AVIATOR",
                                betAmount = betAmount,
                                multiplier = currentMultiplier,
                                isWin = true,
                                detail = "Cashed out at ${String.format("%.2f", currentMultiplier)}x"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("aviator_cashout_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CASH OUT ₹${String.format("%.2f", betAmount * currentMultiplier)}",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                } else if (gameState == AviatorState.FLYING) {
                    Button(
                        onClick = {
                            // Queue bet for next round even while current round is flying
                            isBetQueued = !isBetQueued
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isBetQueued) NeonRed else GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBetQueued) "CANCEL NEXT ROUND BET" else "BET FOR NEXT ROUND (₹${betAmount.toInt()})",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.5.sp
                        )
                    }
                } else {
                    // WAITING_BETS or CRASHED state
                    Button(
                        onClick = {
                            isBetQueued = !isBetQueued
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("aviator_bet_start_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isBetQueued) NeonRed else GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBetQueued) "CANCEL BET (₹${betAmount.toInt()})" else "BET FOR TAKEOFF (₹${betAmount.toInt()})",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
