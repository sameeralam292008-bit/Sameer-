package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CashFlowViewModel
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class SlotSymbol(val icon: String, val name: String, val weight: Int)

val HOT_SYMBOLS = listOf(
    SlotSymbol("🔥", "Hot 7", 5),
    SlotSymbol("💎", "Diamond", 8),
    SlotSymbol("🔔", "Bell", 12),
    SlotSymbol("🍀", "Clover", 16),
    SlotSymbol("🍒", "Cherry", 24),
    SlotSymbol("🪙", "Coin", 35)
)

@Composable
fun HotSpinGameScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    var reel1 by remember { mutableStateOf("🔥") }
    var reel2 by remember { mutableStateOf("💎") }
    var reel3 by remember { mutableStateOf("🔥") }
    var isSpinning by remember { mutableStateOf(false) }
    var betAmount by remember { mutableDoubleStateOf(50.0) }
    var lastWin by remember { mutableDoubleStateOf(0.0) }
    var bonusMultiplier by remember { mutableStateOf("1X") }
    var statusText by remember { mutableStateOf("Spin the flaming reels to win up to 50X!") }

    var isAutoSpinActive by remember { mutableStateOf(false) }
    var autoSpinCountdown by remember { mutableIntStateOf(4) }
    var jackpotDropTimer by remember { mutableIntStateOf(52) }

    fun pickSymbol(): String {
        val total = HOT_SYMBOLS.sumOf { it.weight }
        var rnd = Random.nextInt(total)
        for (sym in HOT_SYMBOLS) {
            if (rnd < sym.weight) return sym.icon
            rnd -= sym.weight
        }
        return "🍒"
    }

    fun spinReels() {
        if (isSpinning) return
        isSpinning = true
        lastWin = 0.0
        bonusMultiplier = "1X"
        statusText = "Reels are blazing..."

        scope.launch {
            // Rapid reel cycle
            for (i in 0..18) {
                reel1 = HOT_SYMBOLS.random().icon
                reel2 = HOT_SYMBOLS.random().icon
                reel3 = HOT_SYMBOLS.random().icon
                delay(65)
            }

            // Final symbols
            val f1 = pickSymbol()
            val f2 = pickSymbol()
            val f3 = pickSymbol()
            reel1 = f1
            delay(120)
            reel2 = f2
            delay(150)
            reel3 = f3

            // Calculate payout
            val isThreeMatch = (f1 == f2 && f2 == f3)
            val isTwoMatch = (f1 == f2 || f2 == f3 || f1 == f3)

            val baseMult = when {
                isThreeMatch && f1 == "🔥" -> 50.0
                isThreeMatch && f1 == "💎" -> 25.0
                isThreeMatch && f1 == "🔔" -> 12.0
                isThreeMatch && f1 == "🍀" -> 8.0
                isThreeMatch && f1 == "🍒" -> 5.0
                isThreeMatch && f1 == "🪙" -> 4.0
                isTwoMatch && (f1 == "🔥" || f2 == "🔥") -> 2.5
                isTwoMatch -> 1.5
                else -> 0.0
            }

            // Random Hot Wheel Multiplier trigger (25% chance)
            val wheelTrigger = Random.nextFloat() < 0.25f && baseMult > 0.0
            val wheelBonus = if (wheelTrigger) listOf(2, 3, 5).random() else 1
            if (wheelTrigger) {
                bonusMultiplier = "${wheelBonus}X"
            }

            val finalMult = baseMult * wheelBonus
            val winAmount = betAmount * finalMult
            lastWin = winAmount

            if (finalMult > 0.0) {
                statusText = "MEGA WIN! $f1 $f2 $f3 paid ${finalMult}x (+₹$winAmount)!"
                viewModel.placeGameBet(
                    gameType = "HOT_SPIN",
                    betAmount = betAmount,
                    multiplier = finalMult,
                    isWin = true,
                    detail = "Hit $f1 $f2 $f3 ($bonusMultiplier wheel)"
                )
            } else {
                statusText = "Try again! $f1 $f2 $f3. Lost ₹$betAmount"
                viewModel.placeGameBet(
                    gameType = "HOT_SPIN",
                    betAmount = betAmount,
                    multiplier = 0.0,
                    isWin = false,
                    detail = "Missed: $f1 $f2 $f3"
                )
            }

            isSpinning = false
        }
    }

    // Continuous Live Table & Auto-Spin Timer Loop
    LaunchedEffect(isAutoSpinActive, isSpinning) {
        while (true) {
            delay(1000)
            if (jackpotDropTimer > 1) {
                jackpotDropTimer--
            } else {
                jackpotDropTimer = 60
            }

            if (isAutoSpinActive && !isSpinning) {
                if (autoSpinCountdown > 1) {
                    autoSpinCountdown--
                } else {
                    autoSpinCountdown = 4
                    spinReels()
                }
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
            IconButton(onClick = onBack, modifier = Modifier.testTag("hot_spin_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = NeonOrange,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HOT SPIN VEGAS",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonOrange.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "WHEEL BONUS",
                    color = NeonOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Live Table Timer & Jackpot Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF2C1304), Color(0xFF1E0A02), Color(0xFF2C1304))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
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
                        tint = NeonOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAutoSpinActive) "AUTO SPIN: 00:0${autoSpinCountdown}s" else "LIVE TABLE ACTIVE",
                        color = if (isAutoSpinActive) NeonOrange else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "JACKPOT DROP: ",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "00:${if (jackpotDropTimer < 10) "0$jackpotDropTimer" else "$jackpotDropTimer"}s",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Slot Machine Cabinet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF22111E), Color(0xFF100A15))
                    )
                )
                .border(2.dp, NeonOrange.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldPrimary)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "HOT WHEEL 50X",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // The 3 Reels Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF09070D))
                        .border(1.5.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(reel1, reel2, reel3).forEachIndexed { idx, symbol ->
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardDark)
                                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = symbol,
                                fontSize = 38.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Multiplier & Win indicator
                if (lastWin > 0.0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonGreen)
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "WIN: ₹${String.format("%.2f", lastWin)}! ($bonusMultiplier BONUS)",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDark)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥 7-7-7 = 50X", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "💎 = 25X", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Status text
        Text(
            text = statusText,
            color = if (lastWin > 0.0) NeonGreen else GoldPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Bet Controls & Spin Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(20.0, 50.0, 100.0, 500.0).forEach { amt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (betAmount == amt) NeonOrange else SurfaceDark)
                                .clickable(enabled = !isSpinning) { betAmount = amt }
                                .padding(vertical = 8.dp),
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Auto Play Toggle Button
                    Button(
                        onClick = {
                            isAutoSpinActive = !isAutoSpinActive
                            if (isAutoSpinActive) autoSpinCountdown = 3
                        },
                        modifier = Modifier
                            .weight(0.42f)
                            .height(52.dp)
                            .testTag("hot_spin_auto_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAutoSpinActive) NeonRed else SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = if (isAutoSpinActive) Color.White else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAutoSpinActive) "STOP" else "AUTO",
                            color = if (isAutoSpinActive) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Main Spin Button
                    Button(
                        onClick = { spinReels() },
                        enabled = !isSpinning,
                        modifier = Modifier
                            .weight(0.58f)
                            .height(52.dp)
                            .testTag("hot_spin_spin_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSpinning) "SPINNING..." else "SPIN (₹${betAmount.toInt()})",
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
