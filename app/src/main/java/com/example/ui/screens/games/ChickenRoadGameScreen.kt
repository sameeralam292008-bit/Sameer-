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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

data class RoadStep(
    val stepIndex: Int,
    val multiplier: Double,
    val trapLane: Int // 0, 1, or 2
)

@Composable
fun ChickenRoadGameScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val stepMultipliers = listOf(1.25, 1.65, 2.20, 3.10, 4.80, 7.50, 15.00)
    var currentStep by remember { mutableIntStateOf(0) }
    var isGameRunning by remember { mutableStateOf(false) }
    var betAmount by remember { mutableDoubleStateOf(50.0) }
    var isGameOver by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Cross the road to multiply your bet!") }

    val steps = remember {
        mutableStateListOf<RoadStep>().apply {
            stepMultipliers.forEachIndexed { i, m ->
                add(RoadStep(i, m, Random.nextInt(3)))
            }
        }
    }

    // List of chosen lanes per step
    val chosenLanes = remember { mutableStateListOf<Int>() }

    fun startNewGame() {
        steps.clear()
        stepMultipliers.forEachIndexed { i, m ->
            steps.add(RoadStep(i, m, Random.nextInt(3)))
        }
        chosenLanes.clear()
        currentStep = 0
        isGameOver = false
        isGameRunning = true
        statusMessage = "Pick a safe lane (1, 2, or 3) to cross Step 1!"
    }

    fun handleLaneClick(stepIdx: Int, lane: Int) {
        if (!isGameRunning || isGameOver || stepIdx != currentStep) return

        chosenLanes.add(lane)
        val step = steps[stepIdx]

        if (lane == step.trapLane) {
            // Hit trap!
            isGameOver = true
            isGameRunning = false
            statusMessage = "CRASH! 💥 The chicken hit a trap on Step ${stepIdx + 1}. You lost ₹$betAmount"
            viewModel.placeGameBet(
                gameType = "CHICKEN_ROAD",
                betAmount = betAmount,
                multiplier = 0.0,
                isWin = false,
                detail = "Trapped on step ${stepIdx + 1}"
            )
        } else {
            // Safe step!
            val mult = step.multiplier
            currentStep++
            if (currentStep >= stepMultipliers.size) {
                // Reached the other side jackpot!
                isGameOver = true
                isGameRunning = false
                val win = betAmount * mult
                statusMessage = "🎉 JACKPOT! Reached the golden farm at ${mult}x (+₹$win)!"
                viewModel.placeGameBet(
                    gameType = "CHICKEN_ROAD",
                    betAmount = betAmount,
                    multiplier = mult,
                    isWin = true,
                    detail = "Cleared all 7 steps at ${mult}x"
                )
            } else {
                statusMessage = "Safe! Current win: ₹${String.format("%.2f", betAmount * mult)} (${mult}x). Next or Cash Out?"
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
            IconButton(onClick = onBack, modifier = Modifier.testTag("chicken_road_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🐔", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CHICKEN ROAD",
                    color = TextPrimary,
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
                    text = "UP TO 15X",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Road Stepping Arena
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F1524))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                reverseLayout = true, // Start from bottom step 1 to top step 7
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(steps.size) { index ->
                    val step = steps[index]
                    val isCurrent = isGameRunning && currentStep == index
                    val isPassed = currentStep > index
                    val chosen = chosenLanes.getOrNull(index)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurrent) Color(0xFF1E2840) else CardDark)
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) GoldPrimary else CardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Multiplier Pill
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isPassed) NeonGreen else if (isCurrent) GoldPrimary else SurfaceDark)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${step.multiplier}x",
                                color = if (isPassed || isCurrent) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // 3 Lanes
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (lane in 0..2) {
                                val isSelectedLane = chosen == lane
                                val isRevealedTrap = (isGameOver && lane == step.trapLane && (index <= currentStep))
                                val isSafeChosen = isPassed && isSelectedLane

                                val tileBg = when {
                                    isRevealedTrap -> NeonRed.copy(alpha = 0.3f)
                                    isSafeChosen -> NeonGreen.copy(alpha = 0.3f)
                                    isCurrent -> SurfaceDark
                                    else -> Color(0xFF0B101B)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(tileBg)
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrent) GoldPrimary.copy(alpha = 0.5f) else CardBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable(enabled = isCurrent) {
                                            handleLaneClick(index, lane)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isRevealedTrap) {
                                        Text(text = "💥", fontSize = 18.sp)
                                    } else if (isSafeChosen) {
                                        Text(text = "🌽", fontSize = 18.sp)
                                    } else if (isCurrent) {
                                        Text(text = "?", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    } else {
                                        Text(text = "·", color = TextMuted, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Status Message
        Text(
            text = statusMessage,
            color = if (isGameOver && !isGameRunning && currentStep < stepMultipliers.size) NeonRed else GoldPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Control Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (isGameRunning && currentStep > 0) {
                    val currentMult = stepMultipliers[currentStep - 1]
                    val winAmount = betAmount * currentMult

                    Button(
                        onClick = {
                            isGameRunning = false
                            isGameOver = true
                            statusMessage = "WIN! Cashed out ₹${String.format("%.2f", winAmount)} at ${currentMult}x!"
                            viewModel.placeGameBet(
                                gameType = "CHICKEN_ROAD",
                                betAmount = betAmount,
                                multiplier = currentMult,
                                isWin = true,
                                detail = "Cashed out at step $currentStep (${currentMult}x)"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("chicken_cashout_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CASH OUT ₹${String.format("%.2f", winAmount)}",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                } else if (!isGameRunning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(20.0, 50.0, 100.0, 200.0).forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (betAmount == amt) GoldPrimary else SurfaceDark)
                                    .clickable { betAmount = amt }
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { startNewGame() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("chicken_start_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "START CHICKEN ROAD (₹${betAmount.toInt()})",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    // Game running at step 0
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "TAP LANE 1, 2, OR 3 TO ADVANCE",
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
