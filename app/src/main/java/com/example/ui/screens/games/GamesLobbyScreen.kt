package com.example.ui.screens.games

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import kotlinx.coroutines.delay

// RAXI.WIN Theme Colors
private val LobbyBgDark = Color(0xFF1E0D14)
private val CardPlumDark = Color(0xFF2D1420)
private val CardBorderDark = Color(0xFF4A1F33)
private val SoftPeach = Color(0xFFFFE4E6)
private val DarkPeachCard = Color(0xFF381A28)
private val CoralRose = Color(0xFFF43F5E)
private val GoldenYellow = Color(0xFFFBBF24)

@Composable
fun GamesLobbyScreen(
    onNavigateToGame: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Lottery") }
    var currentBannerIndex by remember { mutableIntStateOf(0) }

    // Auto-cycling hero banner carousel
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentBannerIndex = (currentBannerIndex + 1) % 3
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LobbyBgDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. TOP APP BAR: RAXI.WIN LOGO & QUICK ACTIONS
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // RAXI.WIN Stylized Logo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFDC2626), Color(0xFFEA580C))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "RAXI",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ".WIN",
                            color = GoldenYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    // Top Action Icons (Sound, Download, Bell)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { Toast.makeText(context, "Sound enabled", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Sound",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { Toast.makeText(context, "App downloaded to desktop!", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { Toast.makeText(context, "No unread notifications", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. HERO PROMO BANNER CAROUSEL (RAXI.WIN EXACT)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("lobby_hero_carousel"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    when (currentBannerIndex) {
                                        0 -> Brush.horizontalGradient(
                                            listOf(Color(0xFF450A0A), Color(0xFF7F1D1D), Color(0xFF1C1917))
                                        )
                                        1 -> Brush.horizontalGradient(
                                            listOf(Color(0xFF1E1B4B), Color(0xFF4338CA), Color(0xFF312E81))
                                        )
                                        else -> Brush.horizontalGradient(
                                            listOf(Color(0xFF2E1065), Color(0xFF701A75), Color(0xFF0F172A))
                                        )
                                    }
                                )
                                .border(1.dp, GoldenYellow.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            when (currentBannerIndex) {
                                0 -> {
                                    // 5% Monthly 3 Day Recharge Bonus Banner
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.3f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "5%",
                                                    color = GoldenYellow,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 32.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFEF4444))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "3rd • 13th • 23rd",
                                                        color = Color.White,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "MONTHLY 3 DAY",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 17.sp
                                            )
                                            Text(
                                                text = "RECHARGE BONUS",
                                                color = GoldenYellow,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 17.sp
                                            )
                                        }

                                        // Rooster Mascot & Gold Coins
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(0.9f)
                                        ) {
                                            Text(text = "🐓💰", fontSize = 48.sp)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(GoldenYellow)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "+15% CASH",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                1 -> {
                                    // Official Channel Bonus Banner
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.3f)) {
                                            Text(
                                                text = "OFFICIAL CHANNEL",
                                                color = GoldenYellow,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "EXCLUSIVE BONUS",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "JOIN NOW @RAXI/GAME",
                                                color = Color(0xFF67E8F9),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "💃🎡", fontSize = 44.sp)
                                            Button(
                                                onClick = {
                                                    Toast.makeText(context, "Telegram channel opened!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text(
                                                    text = "JOIN NOW",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    // 24/7 VIP Instant Payouts Banner
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.3f)) {
                                            Text(
                                                text = "FASTEST WITHDRAWAL",
                                                color = GoldenYellow,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "INSTANT UPI PAYOUTS",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "24 Hours Live Banking • Zero Fees",
                                                color = NeonGreen,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(text = "⚡🚀", fontSize = 48.sp)
                                    }
                                }
                            }

                            // Carousel dots at bottom center
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (i in 0..2) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (currentBannerIndex == i) 14.dp else 6.dp, 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (currentBannerIndex == i) GoldenYellow else Color.White.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. MARQUEE ANNOUNCEMENT TICKER (RAXI.WIN EXACT)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2C131D))
                            .border(1.dp, Color(0xFF4A1F33), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "📢", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dear User, welcome to RAXI.WIN! RAXI.WIN is a brand-new premium entertainment platform with instant 24/7 deposits & payouts!",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFDC2626))
                                    .clickable {
                                        Toast.makeText(context, "Welcome to RAXI.WIN! Official Casino Platform.", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Detail",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 4. CATEGORY TABS (LOTTERY, POPULAR, MINI GAMES, SLOTS, SPORTS)
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tabs = listOf(
                        CategoryTabInfo("Lottery", "🎱", Color(0xFF06B6D4), Color(0xFFE0F7FA)),
                        CategoryTabInfo("Popular", "🏆", Color(0xFF3B82F6), Color(0xFFDBEAFE)),
                        CategoryTabInfo("Mini games", "🃏", Color(0xFFF43F5E), Color(0xFFFFE4E6)),
                        CategoryTabInfo("Slots", "🎰", Color(0xFFEF4444), Color(0xFFFEE2E2)),
                        CategoryTabInfo("Sports", "⚽", Color(0xFFF59E0B), Color(0xFFFEF3C7))
                    )

                    items(tabs) { tab ->
                        val isSelected = selectedCategory == tab.title
                        Box(
                            modifier = Modifier
                                .width(78.dp)
                                .height(86.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) tab.badgeColor else CardPlumDark
                                )
                                .border(
                                    width = 1.2.dp,
                                    color = if (isSelected) GoldenYellow else CardBorderDark,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCategory = tab.title }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .testTag("cat_tab_${tab.title}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = tab.emoji, fontSize = 30.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 5. SECTION 1: LOTTERY (WINGO, K3, 5D, TRX WINGO, MOTO RACING)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    SectionHeaderBar(
                        iconEmoji = "🎱",
                        title = "Lottery",
                        allCount = "ALL 5",
                        onPrevious = { },
                        onNext = { }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2-Column Grid of 5 Lottery Cards
                    val lotteryGames = listOf(
                        LotteryGameItem("WINGO", "🎱 1 8 9", Color(0xFFFFE4E6), Screen.GAME_WINGO),
                        LotteryGameItem("K3 LOTTERY", "🎲 ⚂ ⚅", Color(0xFFFFEDD5), Screen.GAME_WINGO),
                        LotteryGameItem("5D LOTTERY", "✨ 5 D", Color(0xFFFCE7F3), Screen.GAME_WINGO),
                        LotteryGameItem("TRX WINGO", "💎 TRX", Color(0xFFEDE9FE), Screen.GAME_WINGO),
                        LotteryGameItem("MOTO RACING", "🏍️ 7", Color(0xFFFEF3C7), Screen.GAME_CHICKEN_ROAD)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (row in lotteryGames.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (game in row) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        LotteryCardView(
                                            item = game,
                                            onClick = { onNavigateToGame(game.screen) }
                                        )
                                    }
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 6. PROMO STRIKE BANNER: ASSISTENTE DO DRAGÃO
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4C1D95), Color(0xFF581C87), Color(0xFF3B0764))
                                    )
                                )
                                .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Text(
                                        text = "ASSISTENTE DO DRAGÃO",
                                        color = GoldenYellow,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "5 STRIKE CONSECUTIVOS\nCOM O MESMO RESULTADO",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 15.sp
                                    )
                                }

                                Text(text = "🐉🔥", fontSize = 42.sp)
                            }
                        }
                    }
                }
            }

            // 7. SECTION 2: PLATFORM RECOMMENDATION (3-COLUMN GRID WITH LIVE RTP FOOTERS)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    SectionHeaderBar(
                        iconEmoji = "★",
                        title = "Platform recommendation",
                        allCount = "ALL 6",
                        onPrevious = { },
                        onNext = { }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val recommendedGames = listOf(
                        RtpGameCard("AVIATOR", "✈️", "97.87%", Color(0xFFDC2626), Screen.GAME_AVIATOR),
                        RtpGameCard("CRICKET", "🏏", "96.03%", Color(0xFF16A34A), Screen.GAME_WINGO),
                        RtpGameCard("MONEY COMING", "💵", "97.34%", Color(0xFF15803D), Screen.GAME_CRAZY_777),
                        RtpGameCard("BOOM", "💥", "97.55%", Color(0xFF0284C7), Screen.GAME_VORTEX_BOOM),
                        RtpGameCard("CHICKEN ROAD 2", "🐔", "97.01%", Color(0xFFEA580C), Screen.GAME_CHICKEN_ROAD),
                        RtpGameCard("ROULETTE", "🎡", "97.96%", Color(0xFFB91C1C), Screen.GAME_ROULETTE)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (row in recommendedGames.chunked(3)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (game in row) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        RtpGameCardView(
                                            game = game,
                                            onClick = { onNavigateToGame(game.screen) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. SECTION 3: POPULAR (SLOTS WITH LIVE RTP)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    SectionHeaderBar(
                        iconEmoji = "🔥",
                        title = "Popular",
                        allCount = "ALL 18",
                        onPrevious = { },
                        onNext = { }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val popularGames = listOf(
                        RtpGameCard("FORTUNE GARUDA", "🦅", "96.68%", Color(0xFFD97706), Screen.GAME_HOT_SPIN),
                        RtpGameCard("LUCKY JAGUAR", "🐆", "97.12%", Color(0xFFB45309), Screen.GAME_HOT_SPIN),
                        RtpGameCard("MONEY COMING", "💰", "97.34%", Color(0xFF15803D), Screen.GAME_CRAZY_777),
                        RtpGameCard("FORTUNE GEMS 2", "💎", "96.75%", Color(0xFF7C3AED), Screen.GAME_CRAZY_777),
                        RtpGameCard("FORTUNE GEMS 3", "👑", "97.01%", Color(0xFF9333EA), Screen.GAME_CRAZY_777),
                        RtpGameCard("HOT SPIN 777", "🔥", "97.61%", Color(0xFFDC2626), Screen.GAME_HOT_SPIN)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (row in popularGames.chunked(3)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (game in row) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        RtpGameCardView(
                                            game = game,
                                            onClick = { onNavigateToGame(game.screen) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // "View All" Pill Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { Toast.makeText(context, "Showing all 18 Popular games", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "View All",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 9. SECTION 4: MINI GAMES
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    SectionHeaderBar(
                        iconEmoji = "🎮",
                        title = "Mini games",
                        allCount = "ALL 134",
                        onPrevious = { },
                        onNext = { }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val miniGames = listOf(
                        RtpGameCard("VORTEX BOOM", "🌀", "98.10%", Color(0xFF0284C7), Screen.GAME_VORTEX_BOOM),
                        RtpGameCard("COIN FLIP", "🪙", "98.50%", Color(0xFFEAB308), Screen.GAME_COIN_FLIP),
                        RtpGameCard("CHICKEN ROAD", "🐔", "97.01%", Color(0xFF16A34A), Screen.GAME_CHICKEN_ROAD)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (game in miniGames) {
                            Box(modifier = Modifier.weight(1f)) {
                                RtpGameCardView(
                                    game = game,
                                    onClick = { onNavigateToGame(game.screen) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // FLOATING WIDGET 1: "Get ₹13 Add to Desktop" (Bottom Left Overlay)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 14.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFDC2626), Color(0xFFEA580C))
                    )
                )
                .clickable {
                    Toast.makeText(context, "₹13 Desktop Bonus Claimed!", Toast.LENGTH_SHORT).show()
                }
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("floating_desktop_bonus")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎁", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Get ₹13",
                        color = GoldenYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Add to Desktop",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // FLOATING WIDGET 2: Circle Telegram & Lucky Shortcut (Bottom Right Overlay)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 14.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF0284C7))
                .border(2.dp, Color.White, CircleShape)
                .clickable {
                    Toast.makeText(context, "Opening Official Telegram Support @RAXI/GAME", Toast.LENGTH_SHORT).show()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Telegram",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

data class CategoryTabInfo(val title: String, val emoji: String, val badgeColor: Color, val lightBg: Color)
data class LotteryGameItem(val title: String, val visual: String, val bgColor: Color, val screen: Screen)
data class RtpGameCard(val title: String, val emoji: String, val rtp: String, val cardBg: Color, val screen: Screen)

/**
 * Section Header Bar with Title, Count Pill, and Arrow Navigation (< >)
 */
@Composable
fun SectionHeaderBar(
    iconEmoji: String,
    title: String,
    allCount: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = iconEmoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF831843))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = allCount,
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Navigation Arrows (< >)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CardPlumDark)
                    .clickable { onPrevious() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CardPlumDark)
                    .clickable { onNext() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 2-Column Lottery Game Card (WINGO, K3, 5D, TRX, MOTO)
 */
@Composable
fun LotteryCardView(
    item: LotteryGameItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable { onClick() }
            .testTag("lottery_card_${item.title}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPeachCard),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.visual,
                fontSize = 32.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF230D17))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * 3-Column Game Card with Live RTP footer (AVIATOR, BOOM, MONEY COMING, SLOTS)
 */
@Composable
fun RtpGameCardView(
    game: RtpGameCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() }
            .testTag("rtp_card_${game.title}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPeachCard),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Game Visual & Title Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(game.cardBg.copy(alpha = 0.7f), DarkPeachCard)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = game.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = game.title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.5.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // RTP Footer Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF200B15))
                    .padding(vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RTP ",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = game.rtp,
                        color = NeonGreen,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
