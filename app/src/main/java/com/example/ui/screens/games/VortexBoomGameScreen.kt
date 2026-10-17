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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dangerous
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
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

data class VortexTile(
    val index: Int,
    val isBomb: Boolean,
    var isRevealed: Boolean = false
)

@Composable
fun VortexBoomGameScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var bombCount by remember { mutableIntStateOf(3) }
    var betAmount by remember { mutableDoubleStateOf(50.0) }
    var isGameRunning by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var revealedGemsCount by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf("Find cosmic gems and avoid the Vortex Mines!") }

    val tiles = remember {
        mutableStateListOf<VortexTile>().apply {
            for (i in 0 until 25) add(VortexTile(i, false))
        }
    }

    // Dynamic multiplier based on revealed gems & bomb count
    val currentMultiplier = remember(revealedGemsCount, bombCount) {
        if (revealedGemsCount == 0) 1.0
        else {
            var mult = 1.0
            val safeCount = 25 - bombCount
            for (i in 0 until revealedGemsCount) {
                val stepMult = (safeCount - i).toDouble() / (25 - i).toDouble()
                mult /= stepMult
            }
            (mult * 0.96 * 100).toLong() / 100.0 // 4% house edge
        }
    }

    fun startVortexGame() {
        val bombIndices = mutableSetOf<Int>()
        while (bombIndices.size < bombCount) {
            bombIndices.add(Random.nextInt(25))
        }

        tiles.clear()
        for (i in 0 until 25) {
            tiles.add(VortexTile(index = i, isBomb = bombIndices.contains(i), isRevealed = false))
        }

        revealedGemsCount = 0
        isGameOver = false
        isGameRunning = true
        statusText = "Pick any tile! $bombCount Vortex Mines are hidden."
    }

    fun handleTileClick(tile: VortexTile) {
        if (!isGameRunning || isGameOver || tile.isRevealed) return

        val idx = tile.index
        tiles[idx] = tile.copy(isRevealed = true)

        if (tile.isBomb) {
            // Hit Bomb!
            isGameOver = true
            isGameRunning = false
            // Reveal all bombs
            for (i in 0 until 25) {
                if (tiles[i].isBomb) {
                    tiles[i] = tiles[i].copy(isRevealed = true)
                }
            }
            statusText = "BOOM! 💥 You hit a Vortex Mine! Lost ₹$betAmount"
            viewModel.placeGameBet(
                gameType = "VORTEX_BOOM",
                betAmount = betAmount,
                multiplier = 0.0,
                isWin = false,
                detail = "Exploded at $revealedGemsCount gems"
            )
        } else {
            // Found Gem!
            revealedGemsCount++
            val winSoFar = betAmount * currentMultiplier
            statusText = "Cosmic Gem found! Current: ${String.format("%.2f", currentMultiplier)}x (₹${String.format("%.2f", winSoFar)}). Tap more or Cash Out!"
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
            IconButton(onClick = onBack, modifier = Modifier.testTag("vortex_boom_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌀", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VORTEX BOOM",
                    color = NeonCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonCyan.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$bombCount MINES",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // 5x5 Mines Grid Arena
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(14.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0C1322))
                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tiles.size) { index ->
                    val tile = tiles[index]
                    val isRev = tile.isRevealed

                    val tileBg = when {
                        isRev && tile.isBomb -> NeonRed.copy(alpha = 0.3f)
                        isRev && !tile.isBomb -> NeonCyan.copy(alpha = 0.25f)
                        isGameRunning -> CardDark
                        else -> Color(0xFF141C2E)
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tileBg)
                            .border(
                                width = 1.dp,
                                color = if (isRev && !tile.isBomb) NeonCyan else CardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = isGameRunning && !isGameOver && !isRev) {
                                handleTileClick(tile)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRev && tile.isBomb) {
                            Text(text = "💣", fontSize = 24.sp)
                        } else if (isRev && !tile.isBomb) {
                            Text(text = "💎", fontSize = 24.sp)
                        } else {
                            Text(text = "🌀", color = TextMuted.copy(alpha = 0.4f), fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Status banner
        Text(
            text = statusText,
            color = if (isGameOver && !isGameRunning && revealedGemsCount == 0) NeonRed else GoldPrimary,
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
                if (isGameRunning && revealedGemsCount > 0) {
                    val currentWin = betAmount * currentMultiplier
                    Button(
                        onClick = {
                            isGameRunning = false
                            isGameOver = true
                            statusText = "WIN! Cashed out ₹${String.format("%.2f", currentWin)} at ${String.format("%.2f", currentMultiplier)}x!"
                            viewModel.placeGameBet(
                                gameType = "VORTEX_BOOM",
                                betAmount = betAmount,
                                multiplier = currentMultiplier,
                                isWin = true,
                                detail = "Cashed out with $revealedGemsCount gems"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("vortex_cashout_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CASH OUT ₹${String.format("%.2f", currentWin)} (${String.format("%.2f", currentMultiplier)}x)",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                } else if (!isGameRunning) {
                    // Mine Count Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Mines: ", color = TextSecondary, fontSize = 12.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 3, 5, 10).forEach { count ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (bombCount == count) NeonCyan else SurfaceDark)
                                        .clickable { bombCount = count }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        color = if (bombCount == count) Color.Black else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bet Chips
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
                                    .clickable { betAmount = amt }
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
                        onClick = { startVortexGame() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("vortex_start_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "START VORTEX BOOM (₹${betAmount.toInt()})",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                } else {
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
                            text = "TAP A TILE TO UNCOVER GEMS",
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
