package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kotlinx.coroutines.launch
import kotlin.random.Random

val CRAZY_MAIN_SYMBOLS = listOf("777", "77", "7", "BAR3", "BAR2", "BAR")
val CRAZY_BONUS_REEL = listOf("2X", "5X", "10X", "RESPIN", "+₹200", "1X")

@Composable
fun Crazy777GameScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    var r1 by remember { mutableStateOf("777") }
    var r2 by remember { mutableStateOf("777") }
    var r3 by remember { mutableStateOf("777") }
    var r4Bonus by remember { mutableStateOf("10X") }

    var isSpinning by remember { mutableStateOf(false) }
    var betAmount by remember { mutableDoubleStateOf(50.0) }
    var wonAmount by remember { mutableDoubleStateOf(0.0) }
    var statusText by remember { mutableStateOf("Spin the 3+1 reels for Crazy 777 Jackpot!") }
    var isAutoSpinActive by remember { mutableStateOf(false) }
    var autoSpinCountdown by remember { mutableIntStateOf(3) }
    var jackpotCountdown by remember { mutableIntStateOf(18) }

    // Real-time progressive jackpot drop timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            jackpotCountdown = if (jackpotCountdown > 1) jackpotCountdown - 1 else 25
        }
    }

    fun spinCrazy() {
        if (isSpinning) return
        isSpinning = true
        wonAmount = 0.0
        statusText = "Reels rolling..."

        scope.launch {
            for (i in 0..16) {
                r1 = CRAZY_MAIN_SYMBOLS.random()
                r2 = CRAZY_MAIN_SYMBOLS.random()
                r3 = CRAZY_MAIN_SYMBOLS.random()
                r4Bonus = CRAZY_BONUS_REEL.random()
                delay(60)
            }

            // Weighted outcome
            val pickSym = {
                val rnd = Random.nextFloat()
                when {
                    rnd < 0.04f -> "777"
                    rnd < 0.12f -> "77"
                    rnd < 0.25f -> "7"
                    rnd < 0.45f -> "BAR3"
                    rnd < 0.70f -> "BAR2"
                    else -> "BAR"
                }
            }

            r1 = pickSym()
            delay(100)
            r2 = pickSym()
            delay(120)
            r3 = pickSym()
            delay(140)

            val bonusPick = if (Random.nextFloat() < 0.15f) "10X" else if (Random.nextFloat() < 0.35f) "5X" else if (Random.nextFloat() < 0.60f) "2X" else "1X"
            r4Bonus = bonusPick

            // Calculate Base Multiplier
            val isMatch = (r1 == r2 && r2 == r3)
            val isAllSevens = (r1.startsWith("7") && r2.startsWith("7") && r3.startsWith("7"))

            val baseMult = when {
                isMatch && r1 == "777" -> 100.0
                isMatch && r1 == "77" -> 40.0
                isMatch && r1 == "7" -> 20.0
                isAllSevens -> 10.0
                isMatch && r1 == "BAR3" -> 8.0
                isMatch && r1 == "BAR2" -> 5.0
                isMatch && r1 == "BAR" -> 3.0
                (r1 == r2 || r2 == r3) -> 1.5
                else -> 0.0
            }

            val bonusFactor = when (r4Bonus) {
                "10X" -> 10.0
                "5X" -> 5.0
                "2X" -> 2.0
                else -> 1.0
            }

            val totalMult = baseMult * bonusFactor
            val win = betAmount * totalMult
            wonAmount = win

            if (totalMult > 0.0) {
                statusText = "CRAZY 777 WIN! $r1 $r2 $r3 with $r4Bonus bonus (+₹$win)!"
                viewModel.placeGameBet(
                    gameType = "CRAZY_777",
                    betAmount = betAmount,
                    multiplier = totalMult,
                    isWin = true,
                    detail = "$r1 $r2 $r3 + $r4Bonus"
                )
            } else {
                statusText = "Missed: $r1 $r2 $r3. Lost ₹$betAmount"
                viewModel.placeGameBet(
                    gameType = "CRAZY_777",
                    betAmount = betAmount,
                    multiplier = 0.0,
                    isWin = false,
                    detail = "$r1 $r2 $r3"
                )
            }

            isSpinning = false
        }
    }

    // Auto-spin timer cycle
    LaunchedEffect(isAutoSpinActive, isSpinning) {
        if (isAutoSpinActive && !isSpinning) {
            autoSpinCountdown = 3
            while (autoSpinCountdown > 0 && isAutoSpinActive && !isSpinning) {
                delay(1000)
                if (isAutoSpinActive && !isSpinning) autoSpinCountdown--
            }
            if (isAutoSpinActive && !isSpinning) {
                spinCrazy()
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
            IconButton(onClick = onBack, modifier = Modifier.testTag("crazy777_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎰", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CRAZY 777",
                    color = NeonRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldPrimary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "1000X MAX",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }

        // Macau Slot Cabinet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(14.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E0A12), Color(0xFF140508))
                    )
                )
                .border(2.dp, GoldPrimary, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top marquee
                Text(
                    text = "★ CRAZY 777 JACKPOT REEL ★",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progressive Jackpot Countdown Timer Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3B0712))
                        .border(1.dp, NeonRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = NeonRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "JACKPOT DROP IN:", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Text(
                            text = "00:${if (jackpotCountdown < 10) "0" else ""}${jackpotCountdown}s (₹2,84,500)",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Reels Row: 3 Main + 1 Bonus Reel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF090406))
                        .border(1.5.dp, CardBorder, RoundedCornerShape(14.dp))
                        .padding(vertical = 20.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 3 Regular Reels
                    listOf(r1, r2, r3).forEach { sym ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CardDark)
                                .border(1.dp, NeonRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val symColor = if (sym.contains("7")) NeonRed else GoldPrimary
                            Text(
                                text = sym,
                                color = symColor,
                                fontWeight = FontWeight.Black,
                                fontSize = if (sym.length > 3) 14.sp else 22.sp
                            )
                        }
                    }

                    // Separator
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(60.dp)
                            .background(GoldPrimary.copy(alpha = 0.6f))
                    )

                    // 4th Special Bonus Reel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.verticalGradient(listOf(GoldPrimary, Color(0xFFE65100))))
                            .border(1.5.dp, Color.White, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "BONUS",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = r4Bonus,
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (wonAmount > 0.0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonGreen)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "JACKPOT WIN: ₹${String.format("%.2f", wonAmount)}!",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                } else {
                    Text(
                        text = "Paytable: 777-777-777 (100X) • 77 (40X) • 7 (20X) • BAR (8X)",
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }
            }
        }

        // Status Banner
        Text(
            text = statusText,
            color = if (wonAmount > 0.0) NeonGreen else GoldPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Bet Controls
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
                                .background(if (betAmount == amt) GoldPrimary else SurfaceDark)
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
                    // Auto-Play with countdown timer button
                    Button(
                        onClick = { isAutoSpinActive = !isAutoSpinActive },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAutoSpinActive) NeonGreen else CardDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = if (isAutoSpinActive) Color.Black else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAutoSpinActive) "AUTO (00:0${autoSpinCountdown}s)" else "AUTO SPIN",
                            color = if (isAutoSpinActive) Color.Black else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Spin Button
                    Button(
                        onClick = { spinCrazy() },
                        enabled = !isSpinning,
                        modifier = Modifier
                            .weight(1.8f)
                            .height(52.dp)
                            .testTag("crazy777_spin_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSpinning) "SPINNING..." else "SPIN (₹${betAmount.toInt()})",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.5.sp
                        )
                    }
                }
            }
        }
    }
}
