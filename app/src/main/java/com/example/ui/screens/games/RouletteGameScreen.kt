package com.example.ui.screens.games

import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CashFlowViewModel
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// European Roulette Wheel Sequence (37 numbers: 0-36)
private val ROULETTE_NUMBERS = listOf(
    0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
    5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
)

private val RED_NUMBERS = setOf(
    1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
)

// Felt Colors matching Bucharest Auto-Roulette from video
private val FeltBackground = Color(0xFF03261A)
private val FeltDarker = Color(0xFF01140E)
private val GridBorderColor = Color(0xFF1B6B52)
private val RedCellColor = Color(0xFF6E221E)
private val BlackCellColor = Color(0xFF161719)
private val GreenZeroColor = Color(0xFF083827)
private val OutsideBetBg = Color(0xFF072F21)
private val ColumnBetText = Color(0xFF2DD4BF)

private val AVAILABLE_CHIPS = listOf(10.0, 25.0, 50.0, 100.0, 500.0, 1000.0, 5000.0)

enum class RouletteGamePhase {
    BETTING,       // 16s countdown with circular progress clock at top-left
    SPINNING,      // Wheel expands, ball spins around outer rim
    RESULT_POPUP   // Big [ 11 ] gold box appears over wheel, table spotlight shines on cell
}

@Composable
fun RouletteGameScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val currentUser by viewModel.currentUser.collectAsState()
    val balance = currentUser?.balance ?: 0.0
    val userName = currentUser?.name?.ifBlank { "Player" } ?: "Player"

    val coroutineScope = rememberCoroutineScope()
    val wheelRotation = remember { Animatable(0f) }
    val ballRotation = remember { Animatable(0f) }

    var gamePhase by remember { mutableStateOf(RouletteGamePhase.BETTING) }
    var countdownSeconds by remember { mutableIntStateOf(16) }
    var countdownProgress by remember { mutableDoubleStateOf(1.0) }

    var selectedChip by remember { mutableDoubleStateOf(10.0) }
    var showChipSelector by remember { mutableStateOf(false) }

    val placedBets = remember { mutableStateMapOf<String, Double>() }
    val betHistoryStack = remember { mutableStateListOf<Pair<String, Double>>() }
    var previousBets = remember { mutableMapOf<String, Double>() }

    var winningNumber by remember { mutableStateOf<Int?>(null) }
    var lastWinAmount by remember { mutableDoubleStateOf(0.0) }
    var chatTickerMessage by remember { mutableStateOf("Auto: Welcome to Bucharest Auto-Roulette!") }

    var forceSpinRequested by remember { mutableStateOf(false) }
    var lastRoundResultText by remember { mutableStateOf<String?>(null) }
    var lastRoundWinStatus by remember { mutableStateOf<Boolean?>(null) } // true = win, false = loss, null = no bet
    var lastRoundStaked by remember { mutableDoubleStateOf(0.0) }
    var lastRoundWon by remember { mutableDoubleStateOf(0.0) }
    var lastRoundWinningDesc by remember { mutableStateOf("") }

    // Initial sequence matching video at 00:00:
    // 16 11 16 25 14 29 33 6 4 20 34 18 28 12 11
    val recentNumbers = remember {
        mutableStateListOf(16, 11, 16, 25, 14, 29, 33, 6, 4, 20, 34, 18, 28, 12, 11)
    }

    val simulatedWinners = listOf(
        "₹76,321 taka-paka",
        "₹4,497 Sam",
        "₹3,600 Khushal",
        "₹2,723 Gooooo",
        "₹14,200 Rahul K.",
        "₹8,550 Vikram S.",
        "₹1,900 Pooja M."
    )

    val totalBetAmount = placedBets.values.sum()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    var currentTimestamp by remember { mutableStateOf(timeFormat.format(Date())) }

    // Live clock
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimestamp = timeFormat.format(Date())
        }
    }

    // Continuous idle wheel rotation while in BETTING phase
    LaunchedEffect(gamePhase) {
        if (gamePhase == RouletteGamePhase.BETTING) {
            while (gamePhase == RouletteGamePhase.BETTING) {
                wheelRotation.animateTo(
                    targetValue = wheelRotation.value + 40f,
                    animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
                )
            }
        }
    }

    fun addBet(betKey: String) {
        if (gamePhase != RouletteGamePhase.BETTING) return
        val currentTotal = placedBets.values.sum()
        if (currentTotal + selectedChip > balance) return

        val prev = placedBets[betKey] ?: 0.0
        placedBets[betKey] = prev + selectedChip
        betHistoryStack.add(Pair(betKey, selectedChip))
    }

    fun undoLastBet() {
        if (gamePhase != RouletteGamePhase.BETTING) return
        if (betHistoryStack.isNotEmpty()) {
            val (lastBetKey, lastAmt) = betHistoryStack.removeLast()
            val cur = placedBets[lastBetKey] ?: 0.0
            if (cur <= lastAmt) {
                placedBets.remove(lastBetKey)
            } else {
                placedBets[lastBetKey] = cur - lastAmt
            }
        }
    }

    fun doubleBets() {
        if (gamePhase != RouletteGamePhase.BETTING) return
        val currentTotal = placedBets.values.sum()
        if (currentTotal * 2 <= balance && currentTotal > 0) {
            val copy = HashMap(placedBets)
            placedBets.clear()
            copy.forEach { (k, v) ->
                placedBets[k] = v * 2
                betHistoryStack.add(Pair(k, v))
            }
        }
    }

    fun rebet() {
        if (gamePhase != RouletteGamePhase.BETTING) return
        if (previousBets.isNotEmpty()) {
            val req = previousBets.values.sum()
            if (req <= balance) {
                placedBets.clear()
                betHistoryStack.clear()
                placedBets.putAll(previousBets)
                previousBets.forEach { (k, v) -> betHistoryStack.add(Pair(k, v)) }
            }
        }
    }

    fun evaluateWin(resultNum: Int): Pair<Double, Boolean> {
        var totalWin = 0.0

        placedBets.forEach { (key, betAmt) ->
            when {
                key.startsWith("NUM_") -> {
                    val num = key.removePrefix("NUM_").toIntOrNull()
                    if (num == resultNum) totalWin += betAmt * 36.0
                }
                key == "RED" -> {
                    if (resultNum in RED_NUMBERS) totalWin += betAmt * 2.0
                }
                key == "BLACK" -> {
                    if (resultNum != 0 && resultNum !in RED_NUMBERS) totalWin += betAmt * 2.0
                }
                key == "EVEN" -> {
                    if (resultNum != 0 && resultNum % 2 == 0) totalWin += betAmt * 2.0
                }
                key == "ODD" -> {
                    if (resultNum % 2 == 1) totalWin += betAmt * 2.0
                }
                key == "LOW_1_18" -> {
                    if (resultNum in 1..18) totalWin += betAmt * 2.0
                }
                key == "HIGH_19_36" -> {
                    if (resultNum in 19..36) totalWin += betAmt * 2.0
                }
                key == "DOZEN_1" -> {
                    if (resultNum in 1..12) totalWin += betAmt * 3.0
                }
                key == "DOZEN_2" -> {
                    if (resultNum in 13..24) totalWin += betAmt * 3.0
                }
                key == "DOZEN_3" -> {
                    if (resultNum in 25..36) totalWin += betAmt * 3.0
                }
                key == "COL_1" -> {
                    if (resultNum != 0 && resultNum % 3 == 1) totalWin += betAmt * 3.0
                }
                key == "COL_2" -> {
                    if (resultNum != 0 && resultNum % 3 == 2) totalWin += betAmt * 3.0
                }
                key == "COL_3" -> {
                    if (resultNum != 0 && resultNum % 3 == 0) totalWin += betAmt * 3.0
                }
            }
        }

        return Pair(totalWin, totalWin > 0)
    }

    // REALISTIC CONTINUOUS ROUND LIFECYCLE (Matching video 00:00 - 00:26)
    LaunchedEffect(Unit) {
        while (true) {
            // PHASE 1: BETTING (14 seconds countdown)
            gamePhase = RouletteGamePhase.BETTING
            winningNumber = null
            lastRoundWinStatus = null
            lastRoundResultText = null
            chatTickerMessage = "Auto: Place your bets, please!"

            for (s in 14 downTo 1) {
                if (forceSpinRequested) break
                countdownSeconds = s
                countdownProgress = s / 14.0
                delay(1000)
            }
            forceSpinRequested = false
            countdownSeconds = 0
            countdownProgress = 0.0

            // PHASE 2: SPINNING (Wheel zooms in, ball spins fast)
            gamePhase = RouletteGamePhase.SPINNING
            chatTickerMessage = "Auto: No more bets! Wheel is spinning..."

            if (placedBets.isNotEmpty()) {
                previousBets = HashMap(placedBets)
            }

            // Target winning number (European roulette: 0-36)
            val targetNum = ROULETTE_NUMBERS.random()
            val targetIndex = ROULETTE_NUMBERS.indexOf(targetNum)
            val segmentAngle = 360f / ROULETTE_NUMBERS.size
            val totalWheelRotations = 360f * 6 + (360f - (targetIndex * segmentAngle))

            // Animate Wheel
            launch {
                wheelRotation.animateTo(
                    targetValue = wheelRotation.value + totalWheelRotations,
                    animationSpec = tween(durationMillis = 5200, easing = FastOutSlowInEasing)
                )
            }

            // Animate Ball in opposite direction
            launch {
                ballRotation.animateTo(
                    targetValue = ballRotation.value - (360f * 10 + (targetIndex * segmentAngle)),
                    animationSpec = tween(durationMillis = 5200, easing = FastOutSlowInEasing)
                )
            }

            delay(5300)

            // PHASE 3: RESULT POPUP (Huge badge over wheel, result announcement card, table spotlight)
            winningNumber = targetNum
            recentNumbers.add(0, targetNum)
            if (recentNumbers.size > 15) recentNumbers.removeLast()

            gamePhase = RouletteGamePhase.RESULT_POPUP

            val totalStaked = placedBets.values.sum()
            val (winAmount, isWin) = evaluateWin(targetNum)
            lastWinAmount = winAmount
            lastRoundStaked = totalStaked
            lastRoundWon = winAmount

            val colorName = when {
                targetNum == 0 -> "Green"
                targetNum in RED_NUMBERS -> "Red"
                else -> "Black"
            }
            val isEven = targetNum != 0 && targetNum % 2 == 0
            val isHigh = targetNum in 19..36
            val dozenStr = when (targetNum) {
                in 1..12 -> "1st Dozen (1-12)"
                in 13..24 -> "2nd Dozen (13-24)"
                in 25..36 -> "3rd Dozen (25-36)"
                else -> "Zero Pocket"
            }

            lastRoundWinningDesc = if (targetNum == 0) {
                "0 Green Zero Pocket"
            } else {
                "$colorName • ${if (isEven) "Even" else "Odd"} • ${if (isHigh) "High (19-36)" else "Low (1-18)"} • $dozenStr"
            }

            if (totalStaked > 0) {
                lastRoundWinStatus = isWin
                lastRoundResultText = if (isWin) {
                    "🎉 YOU WON ₹${String.format("%.2f", winAmount)}!"
                } else {
                    "❌ YOU LOST ₹${totalStaked.toInt()}"
                }

                val netMultiplier = winAmount / totalStaked
                viewModel.placeGameBet(
                    gameType = "ROULETTE",
                    betAmount = totalStaked,
                    multiplier = netMultiplier,
                    isWin = isWin,
                    detail = "Result: #$targetNum $colorName | Total Bet: ₹${totalStaked.toInt()}"
                )
            } else {
                lastRoundWinStatus = null
                lastRoundResultText = "Landed on #$targetNum $colorName"
            }

            val topWinner = simulatedWinners.random()
            chatTickerMessage = if (isWin && totalStaked > 0) {
                "🎉 WINNER! Result was #$targetNum $colorName! You won ₹${winAmount.toInt()}!"
            } else {
                "$topWinner • Result: #$targetNum $colorName"
            }

            // Keep result display for 5 seconds so player can see
            delay(5000)

            // Clear current bets for next round
            placedBets.clear()
            betHistoryStack.clear()
        }
    }

    // Dynamic Wheel Size based on Game Phase (Zooms in during spin, like video!)
    val animatedWheelSize by animateDpAsState(
        targetValue = when (gamePhase) {
            RouletteGamePhase.BETTING -> 195.dp
            RouletteGamePhase.SPINNING -> 255.dp
            RouletteGamePhase.RESULT_POPUP -> 255.dp
        },
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "wheelSize"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FeltBackground, FeltDarker)
                )
            )
            .statusBarsPadding()
    ) {
        // 1. TOP HISTORY RIBBON & CIRCULAR COUNTDOWN TIMER (Identical to video 00:00)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF070B0E))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Countdown Timer (Green ring with seconds, visible during BETTING)
            if (gamePhase == RouletteGamePhase.BETTING) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Circular Progress Ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 3.dp.toPx()
                        val diameter = size.minDimension - stroke
                        val topLeft = Offset(stroke / 2, stroke / 2)
                        val sweep = (countdownProgress * 360f).toFloat()

                        drawArc(
                            color = Color(0xFF14532D),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = androidx.compose.ui.geometry.Size(diameter, diameter),
                            style = Stroke(stroke)
                        )
                        drawArc(
                            color = if (countdownSeconds <= 4) NeonRed else NeonGreen,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = androidx.compose.ui.geometry.Size(diameter, diameter),
                            style = Stroke(stroke)
                        )
                    }

                    Text(
                        text = "$countdownSeconds",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp).testTag("roulette_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Winning Numbers History Ribbon (From video: 16 11 16 25 14 ...)
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(recentNumbers.indices.toList()) { idx ->
                    val num = recentNumbers[idx]
                    val isLatest = idx == 0
                    val textColor = when {
                        num == 0 -> Color(0xFF10B981)
                        num in RED_NUMBERS -> Color(0xFFEF4444)
                        else -> Color.White
                    }

                    if (isLatest) {
                        // White highlight box for latest winner (exact match to video)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = num.toString(),
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else {
                        Text(
                            text = num.toString(),
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right Action Icons (Star & Stats)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Favorites", tint = Color(0xFFCBD5E1), modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.BarChart, contentDescription = "Stats", tint = Color(0xFFCBD5E1), modifier = Modifier.size(17.dp))
                }
            }
        }

        // MAIN ARENA (SCROLLABLE TABLE + DYNAMIC WHEEL)
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. REALISTIC ROULETTE WHEEL WITH DYNAMIC CAMERA ZOOM & RESULT POPUP
                Box(
                    modifier = Modifier
                        .size(animatedWheelSize)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF38180C), Color(0xFF5A2814), Color(0xFF1E0C06))
                            )
                        )
                        .border(4.dp, Color(0xFF4A2311), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Brass Rim
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFB45309), Color(0xFFD97706), Color(0xFF78350F))
                            ),
                            radius = size.width / 2f,
                            style = Stroke(width = 6f)
                        )
                    }

                    // Rotating Pockets Ring with Numbers Drawn on Each Pocket!
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize(0.92f)
                            .rotate(wheelRotation.value)
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2
                        val sweepAngle = 360f / ROULETTE_NUMBERS.size

                        val textPaint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = (radius * 0.088f).coerceIn(22f, 38f)
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            isAntiAlias = true
                            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                        }

                        val zeroPaint = Paint().apply {
                            color = android.graphics.Color.rgb(255, 223, 77) // Bright Gold for Zero
                            textSize = (radius * 0.092f).coerceIn(24f, 40f)
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            isAntiAlias = true
                            setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
                        }

                        ROULETTE_NUMBERS.forEachIndexed { index, num ->
                            val startAngle = index * sweepAngle
                            val sectorColor = when {
                                num == 0 -> Color(0xFF064E3B)
                                num in RED_NUMBERS -> Color(0xFF881337)
                                else -> Color(0xFF111827)
                            }
                            drawArc(
                                color = sectorColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = size
                            )

                            // Silver/Brass pocket separator frets
                            val rad = Math.toRadians((startAngle).toDouble())
                            val endX = center.x + radius * cos(rad).toFloat()
                            val endY = center.y + radius * sin(rad).toFloat()
                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = center,
                                end = Offset(endX, endY),
                                strokeWidth = 1.5f
                            )

                            // Draw the number on the pocket!
                            val midAngle = startAngle + sweepAngle / 2f
                            val midRad = Math.toRadians(midAngle.toDouble())
                            val numDistance = radius * 0.81f
                            val numX = center.x + numDistance * cos(midRad).toFloat()
                            val numY = center.y + numDistance * sin(midRad).toFloat()

                            drawContext.canvas.nativeCanvas.apply {
                                save()
                                rotate(midAngle + 90f, numX, numY)
                                drawText(
                                    num.toString(),
                                    numX,
                                    numY + (textPaint.textSize * 0.35f),
                                    if (num == 0) zeroPaint else textPaint
                                )
                                restore()
                            }
                        }

                        // Inner Bowl Separation Ring
                        drawCircle(
                            color = Color(0xFFD97706),
                            radius = radius * 0.62f,
                            center = center,
                            style = Stroke(width = 3f)
                        )
                    }

                    // Center Silver/Chrome Spindle Turret
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.32f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.White, Color(0xFF94A3B8), Color(0xFF334155))
                                )
                            )
                            .border(2.dp, Color(0xFFE2E8F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val c = Offset(size.width / 2, size.height / 2)
                            drawLine(Color.White, Offset(c.x - 20f, c.y), Offset(c.x + 20f, c.y), strokeWidth = 4f)
                            drawLine(Color.White, Offset(c.x, c.y - 20f), Offset(c.x, c.y + 20f), strokeWidth = 4f)
                            drawCircle(Color(0xFFE2E8F0), radius = 8f, center = c)
                        }
                    }

                    // Ivory Roulette Ball (Spins around track, drops into pocket)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize(0.92f)
                            .rotate(ballRotation.value)
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val ballDistance = (size.width / 2f) * 0.74f
                        val ballCenter = Offset(center.x + ballDistance, center.y)

                        // Ball Shadow
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.5f),
                            radius = 6.5f,
                            center = Offset(ballCenter.x + 2f, ballCenter.y + 2f)
                        )
                        // Ball Body
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, Color(0xFFE2E8F0), Color(0xFF94A3B8)),
                                center = Offset(ballCenter.x - 1f, ballCenter.y - 1f),
                                radius = 5.5f
                            ),
                            radius = 5.5f,
                            center = ballCenter
                        )
                    }

                    // Gold Pointer Needle at Top of Wheel (Points down at current pocket)
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 2.dp)
                            .size(width = 16.dp, height = 18.dp)
                    ) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, size.height)
                            lineTo(0f, 0f)
                            lineTo(size.width, 0f)
                            close()
                        }
                        drawPath(path, color = Color(0xFFFFD700))
                    }

                    // 🏆 WINNING NUMBER POPUP BADGE (Identical to video at 00:18 - 00:20)
                    if (gamePhase == RouletteGamePhase.RESULT_POPUP && winningNumber != null) {
                        val winNum = winningNumber ?: 11
                        val isWinRed = winNum in RED_NUMBERS
                        val cardBg = when {
                            winNum == 0 -> Color(0xFF064E3B)
                            isWinRed -> Color(0xFF881337)
                            else -> Color(0xFF000000)
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 82.dp, height = 66.dp)
                                .shadow(16.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBg)
                                .border(2.5.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = winNum.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 38.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // LIVE BETTING TIMER BAR (With SPIN NOW Button!)
                AnimatedVisibility(visible = gamePhase == RouletteGamePhase.BETTING) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF071F15))
                            .border(1.dp, GridBorderColor, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏳ BETTING CLOSES: ",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "00:${if (countdownSeconds < 10) "0$countdownSeconds" else "$countdownSeconds"}s",
                                color = if (countdownSeconds <= 4) NeonRed else NeonGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { forceSpinRequested = true },
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("roulette_spin_now_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SPIN NOW 🎡",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                // WHEEL SPINNING STATUS BANNER
                AnimatedVisibility(visible = gamePhase == RouletteGamePhase.SPINNING) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E17)),
                        border = BorderStroke(1.dp, GoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WHEEL IS SPINNING... NO MORE BETS!",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // 📢 PROMINENT RESULT ANNOUNCEMENT CARD (Shows what landed and if you won/lost!)
                AnimatedVisibility(
                    visible = gamePhase == RouletteGamePhase.RESULT_POPUP && winningNumber != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (lastRoundWinStatus) {
                                true -> Color(0xFF0A301D) // Dark green
                                false -> Color(0xFF380E10) // Dark red
                                null -> Color(0xFF1E293B)  // Dark slate
                            }
                        ),
                        border = BorderStroke(
                            2.dp,
                            when (lastRoundWinStatus) {
                                true -> GoldPrimary
                                false -> NeonRed
                                null -> Color(0xFF64748B)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                winningNumber == 0 -> GreenZeroColor
                                                winningNumber!! in RED_NUMBERS -> RedCellColor
                                                else -> BlackCellColor
                                            }
                                        )
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${winningNumber ?: 0}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LANDED ON NUMBER #${winningNumber ?: 0}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastRoundWinningDesc,
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Outcome banner
                            when (lastRoundWinStatus) {
                                true -> {
                                    Text(
                                        text = "🎉 YOU WON! +₹${String.format("%.2f", lastRoundWon)}",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Total Bet: ₹${lastRoundStaked.toInt()} • Winnings credited to your balance!",
                                        color = NeonGreen,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                false -> {
                                    Text(
                                        text = "❌ YOU LOST ₹${lastRoundStaked.toInt()}",
                                        color = NeonRed,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Better luck on the next spin!",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 12.sp
                                    )
                                }
                                null -> {
                                    Text(
                                        text = "No bets placed this round",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Place your chips on numbers or colors to win up to 36X!",
                                        color = GoldPrimary,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. AUTHENTIC CASINO FELT BETTING GRID (Exact replica of Bucharest table)
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("roulette_betting_table"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FeltDarker),
                    border = BorderStroke(1.5.dp, GridBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                    ) {
                        // TOP ROW: ZERO (0)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.weight(0.32f))

                            // 0 Cell
                            Box(
                                modifier = Modifier
                                    .weight(0.68f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .background(GreenZeroColor)
                                    .border(
                                        width = if (winningNumber == 0) 2.5.dp else 1.dp,
                                        color = if (winningNumber == 0) GoldPrimary else GridBorderColor,
                                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    )
                                    .clickable(enabled = gamePhase == RouletteGamePhase.BETTING) { addBet("NUM_0") }
                                    .testTag("bet_cell_num_0"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "0",
                                    color = if (winningNumber == 0) GoldPrimary else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                placedBets["NUM_0"]?.let { ChipBadge(it) }

                                // Golden Spotlight on 0 if it wins
                                if (winningNumber == 0 && gamePhase == RouletteGamePhase.RESULT_POPUP) {
                                    WinningSpotlight()
                                }
                            }
                        }

                        // MAIN TABLE GRID: 12 ROWS
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // OUTSIDE BETS COLUMN (Leftmost) - 6 items of height 2 rows each
                            Column(modifier = Modifier.weight(0.16f)) {
                                OutsideBetBox("1-18", "1-18", heightDp = 76, isWon = winningNumber in 1..18 && winningNumber != null, placedBets["LOW_1_18"]) { addBet("LOW_1_18") }
                                OutsideBetBox("EVEN", "EVEN", heightDp = 76, isWon = winningNumber != null && winningNumber != 0 && winningNumber!! % 2 == 0, placedBets["EVEN"]) { addBet("EVEN") }
                                DiamondBetBox(isRed = true, heightDp = 76, isWon = winningNumber in RED_NUMBERS && winningNumber != null, placedBets["RED"]) { addBet("RED") }
                                DiamondBetBox(isRed = false, heightDp = 76, isWon = winningNumber != null && winningNumber != 0 && winningNumber !in RED_NUMBERS, placedBets["BLACK"]) { addBet("BLACK") }
                                OutsideBetBox("ODD", "ODD", heightDp = 76, isWon = winningNumber != null && winningNumber!! % 2 == 1, placedBets["ODD"]) { addBet("ODD") }
                                OutsideBetBox("19-36", "19-36", heightDp = 76, isWon = winningNumber in 19..36 && winningNumber != null, placedBets["HIGH_19_36"]) { addBet("HIGH_19_36") }
                            }

                            // DOZEN BETS COLUMN (Middle Left) - 3 items of height 4 rows each
                            Column(modifier = Modifier.weight(0.16f)) {
                                DozenBetBox("1ST 12", heightDp = 152, isWon = winningNumber in 1..12 && winningNumber != null, placedBets["DOZEN_1"]) { addBet("DOZEN_1") }
                                DozenBetBox("2ND 12", heightDp = 152, isWon = winningNumber in 13..24 && winningNumber != null, placedBets["DOZEN_2"]) { addBet("DOZEN_2") }
                                DozenBetBox("3RD 12", heightDp = 152, isWon = winningNumber in 25..36 && winningNumber != null, placedBets["DOZEN_3"]) { addBet("DOZEN_3") }
                            }

                            // NUMBERS GRID: 12 ROWS X 3 COLUMNS (Right)
                            Column(modifier = Modifier.weight(0.68f)) {
                                for (row in 0..11) {
                                    val n1 = row * 3 + 1
                                    val n2 = row * 3 + 2
                                    val n3 = row * 3 + 3
                                    Row(modifier = Modifier.fillMaxWidth().height(38.dp)) {
                                        NumberCellBox(n1, winningNumber == n1, placedBets["NUM_$n1"], showSpotlight = winningNumber == n1 && gamePhase == RouletteGamePhase.RESULT_POPUP, Modifier.weight(1f)) { addBet("NUM_$n1") }
                                        NumberCellBox(n2, winningNumber == n2, placedBets["NUM_$n2"], showSpotlight = winningNumber == n2 && gamePhase == RouletteGamePhase.RESULT_POPUP, Modifier.weight(1f)) { addBet("NUM_$n2") }
                                        NumberCellBox(n3, winningNumber == n3, placedBets["NUM_$n3"], showSpotlight = winningNumber == n3 && gamePhase == RouletteGamePhase.RESULT_POPUP, Modifier.weight(1f)) { addBet("NUM_$n3") }
                                    }
                                }
                            }
                        }

                        // BOTTOM ROW: 2 TO 1 COLUMN BETS
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.weight(0.32f))
                            Row(modifier = Modifier.weight(0.68f)) {
                                ColumnBetBox("COL_1", winningNumber != null && winningNumber != 0 && winningNumber!! % 3 == 1, placedBets["COL_1"], Modifier.weight(1f)) { addBet("COL_1") }
                                ColumnBetBox("COL_2", winningNumber != null && winningNumber != 0 && winningNumber!! % 3 == 2, placedBets["COL_2"], Modifier.weight(1f)) { addBet("COL_2") }
                                ColumnBetBox("COL_3", winningNumber != null && winningNumber != 0 && winningNumber!! % 3 == 0, placedBets["COL_3"], Modifier.weight(1f)) { addBet("COL_3") }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CHIP SELECTOR HORIZONTAL STRIP
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07120D)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GridBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AVAILABLE_CHIPS.forEach { chipVal ->
                            val isSelected = selectedChip == chipVal
                            CasinoChipButton(chipVal, isSelected) { selectedChip = chipVal }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. FLOATING RIGHT CONTROL BUTTONS (EXACTLY MATCHING VIDEO at 00:01 - 00:23)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // UNDO BUTTON (↶)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "UNDO", color = Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xD90F241C))
                            .border(1.dp, GridBorderColor, CircleShape)
                            .clickable(enabled = gamePhase == RouletteGamePhase.BETTING && betHistoryStack.isNotEmpty()) { undoLastBet() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (betHistoryStack.isNotEmpty()) Color.White else Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                }

                // CURRENT ACTIVE CHIP BUTTON (Shows active chip with dotted circular border)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(2.dp, GoldPrimary, CircleShape)
                        .clickable { showChipSelector = !showChipSelector },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedChip >= 1000) "${(selectedChip / 1000).toInt()}k" else "${selectedChip.toInt()}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                // DOUBLE BUTTON (x2)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "DOUBLE", color = Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xD90F241C))
                            .border(1.dp, GridBorderColor, CircleShape)
                            .clickable(enabled = gamePhase == RouletteGamePhase.BETTING && placedBets.isNotEmpty()) { doubleBets() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "x2", color = if (placedBets.isNotEmpty()) Color.White else Color(0xFF64748B), fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                // REBET / REPEAT (⟲)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xD90F241C))
                        .border(1.dp, GridBorderColor, CircleShape)
                        .clickable(enabled = gamePhase == RouletteGamePhase.BETTING && previousBets.isNotEmpty()) { rebet() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Rebet", tint = if (previousBets.isNotEmpty()) Color.White else Color(0xFF64748B), modifier = Modifier.size(18.dp))
                }

                // MENU (☰)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xD90F241C))
                        .border(1.dp, GridBorderColor, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // 5. BOTTOM STATUS HUD & WINNERS STREAM (Exact match to video at 00:04 & 00:19)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03160F))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Live Winner ticker (e.g. ₹76,321 taka-paka, ₹4,497 Sam ...)
            Text(
                text = chatTickerMessage,
                color = if (lastWinAmount > 0) NeonGreen else Color(0xFF4ADE80),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Stats and Table info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Total Bet & Balance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Total Bet ", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    Text(text = "₹${totalBetAmount.toInt()}", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 12.sp)

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(text = "Balance ", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    Text(text = "₹${String.format("%.2f", balance)}", color = GoldPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                // Right: Timestamp & Table title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "# $currentTimestamp", color = Color(0xFF64748B), fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Bucharest Auto-Roulette ", color = Color(0xFFE2E8F0), fontSize = 10.5.sp)
                    Text(text = "₹10–500,000", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPOSABLE CELLS & SPOTLIGHT FOR WINNING NUMBER
// -------------------------------------------------------------

@Composable
private fun WinningSpotlight() {
    // Golden Glowing Spotlight Pillar & Ring (Matching video at 00:18 - 00:20)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val c = Offset(size.width / 2, size.height / 2)
        // Outer glow circle
        drawCircle(
            color = Color(0xFFFBBF24).copy(alpha = 0.35f),
            radius = size.width / 2.2f,
            center = c
        )
        // Golden ring
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = size.width / 2.6f,
            center = c,
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}

@Composable
private fun NumberCellBox(
    number: Int,
    isWinningNumber: Boolean,
    placedBet: Double?,
    showSpotlight: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isRed = number in RED_NUMBERS
    val bgColor = if (isRed) RedCellColor else BlackCellColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .border(
                width = if (isWinningNumber) 2.dp else 1.dp,
                color = if (isWinningNumber) GoldPrimary else GridBorderColor
            )
            .clickable(onClick = onClick)
            .testTag("bet_cell_num_$number"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = if (isWinningNumber) GoldPrimary else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        placedBet?.let { ChipBadge(it) }

        if (showSpotlight) {
            WinningSpotlight()
        }
    }
}

@Composable
private fun OutsideBetBox(
    label: String,
    testTagKey: String,
    heightDp: Int,
    isWon: Boolean,
    placedBet: Double?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(OutsideBetBg)
            .border(1.dp, if (isWon) GoldPrimary else GridBorderColor)
            .clickable(onClick = onClick)
            .testTag("bet_cell_$testTagKey"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isWon) GoldPrimary else Color(0xFFCBD5E1),
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            modifier = Modifier.rotate(-90f)
        )
        placedBet?.let { ChipBadge(it) }
    }
}

@Composable
private fun DiamondBetBox(
    isRed: Boolean,
    heightDp: Int,
    isWon: Boolean,
    placedBet: Double?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(OutsideBetBg)
            .border(1.dp, if (isWon) GoldPrimary else GridBorderColor)
            .clickable(onClick = onClick)
            .testTag("bet_cell_diamond_${if (isRed) "red" else "black"}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp, 30.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w / 2f, 0f)
                lineTo(w, h / 2f)
                lineTo(w / 2f, h)
                lineTo(0f, h / 2f)
                close()
            }
            if (isRed) {
                drawPath(path = path, color = Color(0xFFDC2626))
            } else {
                drawPath(path = path, color = Color(0xFF1E293B))
                drawPath(path = path, color = Color.White.copy(alpha = 0.8f), style = Stroke(width = 1.5f))
            }
        }
        placedBet?.let { ChipBadge(it) }
    }
}

@Composable
private fun DozenBetBox(
    label: String,
    heightDp: Int,
    isWon: Boolean,
    placedBet: Double?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(OutsideBetBg)
            .border(1.dp, if (isWon) GoldPrimary else GridBorderColor)
            .clickable(onClick = onClick)
            .testTag("bet_cell_dozen_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isWon) GoldPrimary else Color(0xFFCBD5E1),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.rotate(-90f)
        )
        placedBet?.let { ChipBadge(it) }
    }
}

@Composable
private fun ColumnBetBox(
    colKey: String,
    isWon: Boolean,
    placedBet: Double?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .background(OutsideBetBg)
            .border(1.dp, if (isWon) GoldPrimary else GridBorderColor)
            .clickable(onClick = onClick)
            .testTag("bet_cell_$colKey"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "2 TO 1",
            color = if (isWon) GoldPrimary else ColumnBetText,
            fontWeight = FontWeight.Black,
            fontSize = 10.5.sp
        )
        placedBet?.let { ChipBadge(it) }
    }
}

@Composable
private fun ChipBadge(amount: Double) {
    val chipColor = when {
        amount >= 5000 -> Color(0xFF0F172A)
        amount >= 1000 -> Color(0xFFB45309)
        amount >= 500 -> Color(0xFF6B21A8)
        amount >= 100 -> Color(0xFF991B1B)
        amount >= 50 -> Color(0xFF1D4ED8)
        else -> Color(0xFF334155)
    }
    val borderColor = when {
        amount >= 5000 -> Color(0xFFF59E0B)
        amount >= 1000 -> Color(0xFFFCD34D)
        amount >= 500 -> Color(0xFFA78BFA)
        amount >= 100 -> Color(0xFFF87171)
        amount >= 50 -> Color(0xFF60A5FA)
        else -> Color(0xFFCBD5E1)
    }

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(chipColor)
            .border(1.2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (amount >= 1000) "${(amount / 1000).toInt()}k" else "${amount.toInt()}",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 8.5.sp
        )
    }
}

@Composable
private fun CasinoChipButton(
    chipVal: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val chipColor = when {
        chipVal >= 5000 -> Color(0xFF0F172A)
        chipVal >= 1000 -> Color(0xFFB45309)
        chipVal >= 500 -> Color(0xFF6B21A8)
        chipVal >= 100 -> Color(0xFF991B1B)
        chipVal >= 50 -> Color(0xFF1D4ED8)
        else -> Color(0xFF334155)
    }
    val borderColor = when {
        chipVal >= 5000 -> Color(0xFFF59E0B)
        chipVal >= 1000 -> Color(0xFFFCD34D)
        chipVal >= 500 -> Color(0xFFA78BFA)
        chipVal >= 100 -> Color(0xFFF87171)
        chipVal >= 50 -> Color(0xFF60A5FA)
        else -> Color(0xFFCBD5E1)
    }

    Box(
        modifier = Modifier
            .size(if (isSelected) 42.dp else 36.dp)
            .clip(CircleShape)
            .background(chipColor)
            .border(if (isSelected) 2.5.dp else 1.2.dp, if (isSelected) GoldPrimary else borderColor, CircleShape)
            .clickable(onClick = onClick)
            .testTag("chip_selector_${chipVal.toInt()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "₹${if (chipVal >= 1000) "${(chipVal / 1000).toInt()}k" else chipVal.toInt()}",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = if (isSelected) 10.5.sp else 9.sp
        )
    }
}
