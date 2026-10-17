package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.random.Random

@Composable
fun CoinFlipGameScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    var selectedSide by remember { mutableStateOf("HEADS") } // "HEADS" or "TAILS"
    var currentCoinFace by remember { mutableStateOf("HEADS") }
    var isFlipping by remember { mutableStateOf(false) }
    var winStreak by remember { mutableIntStateOf(0) }
    var betAmount by remember { mutableDoubleStateOf(50.0) }
    var statusText by remember { mutableStateOf("Pick Heads or Tails and start your winning streak!") }
    var flipTimerSec by remember { mutableIntStateOf(8) }
    var timerActive by remember { mutableStateOf(true) }

    val rotationY = remember { Animatable(0f) }

    val streakMultipliers = listOf(1.95, 3.80, 7.40, 14.50, 28.00, 55.00)
    val currentMultiplier = if (winStreak > 0) streakMultipliers.getOrElse(winStreak - 1) { 55.0 } else 1.0

    fun flipCoin() {
        if (isFlipping) return
        isFlipping = true
        statusText = "Coin is in the air..."

        scope.launch {
            // Animate spin
            rotationY.snapTo(0f)
            rotationY.animateTo(
                targetValue = 1800f, // 5 full rotations
                animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
            )

            val outcome = if (Random.nextBoolean()) "HEADS" else "TAILS"
            currentCoinFace = outcome

            if (outcome == selectedSide) {
                // Won!
                winStreak++
                val mult = streakMultipliers.getOrElse(winStreak - 1) { 55.0 }
                val currentWin = betAmount * mult
                statusText = "CORRECT! It landed on $outcome! Streak: $winStreak (${mult}x = ₹${String.format("%.2f", currentWin)}). Double or Cash Out?"
            } else {
                // Lost!
                val lostStreak = winStreak
                winStreak = 0
                statusText = "MISSED! Landed on $outcome. Lost ₹$betAmount"
                viewModel.placeGameBet(
                    gameType = "COIN_FLIP",
                    betAmount = betAmount,
                    multiplier = 0.0,
                    isWin = false,
                    detail = "Flipped $outcome vs picked $selectedSide (ended streak of $lostStreak)"
                )
            }

            isFlipping = false
            flipTimerSec = 8
        }
    }

    // Continuous Live Round Countdown Timer
    LaunchedEffect(isFlipping, winStreak, timerActive) {
        if (!isFlipping && winStreak == 0 && timerActive) {
            while (flipTimerSec > 0 && !isFlipping && winStreak == 0 && timerActive) {
                delay(1000)
                if (!isFlipping && winStreak == 0 && timerActive) {
                    flipTimerSec--
                }
            }
            if (!isFlipping && winStreak == 0 && timerActive) {
                flipCoin()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("coin_flip_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🪙", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "COIN FLIP STREAK",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldPrimary.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "STREAK: $winStreak",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Live Round Countdown Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E1B4B))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (winStreak > 0) "🔥 ACTIVE STREAK: x$currentMultiplier (Tap Double or Cash Out)" else "⏳ NEXT FLIP IN 00:0${flipTimerSec}s",
                        color = if (winStreak > 0) NeonGreen else NeonCyan,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (winStreak == 0 && !isFlipping) {
                    LinearProgressIndicator(
                        progress = { flipTimerSec / 8f },
                        modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = NeonCyan,
                        trackColor = Color(0xFF312E81)
                    )
                }
            }
        }

        // 3D Coin Arena
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(14.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E170A), Color(0xFF0F0C05))
                    )
                )
                .border(1.5.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Streak Multiplier Ribbon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    streakMultipliers.forEachIndexed { idx, mult ->
                        val reached = winStreak > idx
                        val isNext = winStreak == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (reached) NeonGreen else if (isNext) GoldPrimary else SurfaceDark)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${mult}x",
                                color = if (reached || isNext) Color.Black else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Rotating 3D Coin
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer {
                            this.rotationY = rotationY.value
                            cameraDistance = 12 * density
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFE082), GoldPrimary, Color(0xFFFF8F00))
                            )
                        )
                        .border(4.dp, Color(0xFFFFF9C4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentCoinFace == "HEADS") "🦅" else "👑",
                            fontSize = 54.sp
                        )
                        Text(
                            text = currentCoinFace,
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (winStreak > 0) {
                    val currentWin = betAmount * currentMultiplier
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonGreen)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "STREAK POT: ₹${String.format("%.2f", currentWin)} (${currentMultiplier}x)",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Status text
        Text(
            text = statusText,
            color = if (winStreak > 0) NeonGreen else GoldPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Bet Controls & Choices
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Choice: HEADS or TAILS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("HEADS" to "🦅 HEADS", "TAILS" to "👑 TAILS").forEach { (side, label) ->
                        val isSelected = selectedSide == side
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GoldPrimary else SurfaceDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) GoldPrimary else CardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = !isFlipping) { selectedSide = side },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bet chips (if streak == 0)
                if (winStreak == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(20.0, 50.0, 100.0, 500.0).forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (betAmount == amt) GoldPrimary else SurfaceDark)
                                    .clickable(enabled = !isFlipping) { betAmount = amt }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹${amt.toInt()}",
                                    color = if (betAmount == amt) Color.Black else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { flipCoin() },
                        enabled = !isFlipping,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("coin_flip_action_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isFlipping) "FLIPPING..." else "FLIP NOW (00:0${flipTimerSec}s) • ₹${betAmount.toInt()}",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    // Win streak is active: Option to CONTINUE or CASH OUT
                    val currentWin = betAmount * currentMultiplier
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val finalWin = betAmount * currentMultiplier
                                winStreak = 0
                                statusText = "WIN! Cashed out ₹${String.format("%.2f", finalWin)}!"
                                viewModel.placeGameBet(
                                    gameType = "COIN_FLIP",
                                    betAmount = betAmount,
                                    multiplier = currentMultiplier,
                                    isWin = true,
                                    detail = "Cashed out at ${currentMultiplier}x"
                                )
                            },
                            enabled = !isFlipping,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("coin_flip_cashout_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "CASH OUT ₹${String.format("%.2f", currentWin)}",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { flipCoin() },
                            enabled = !isFlipping,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("coin_flip_double_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "DOUBLE UP!",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
