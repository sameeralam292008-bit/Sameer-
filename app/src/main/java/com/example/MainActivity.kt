package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.CashFlowViewModel
import com.example.ui.Screen
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.games.AviatorGameScreen
import com.example.ui.screens.games.ChickenRoadGameScreen
import com.example.ui.screens.games.CoinFlipGameScreen
import com.example.ui.screens.games.Crazy777GameScreen
import com.example.ui.screens.games.HotSpinGameScreen
import com.example.ui.screens.games.RouletteGameScreen
import com.example.ui.screens.games.VortexBoomGameScreen
import com.example.ui.screens.games.WinGoGameScreen
import com.example.ui.screens.support.SupportChatScreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CashFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Toast listener
                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collect { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    CashFlowApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun CashFlowApp(viewModel: CashFlowViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    if (!isLoggedIn) {
        AuthScreen(viewModel = viewModel)
    } else {
        when (currentScreen) {
            Screen.AUTH -> {
                AuthScreen(viewModel = viewModel)
            }
            Screen.DASHBOARD -> {
                DashboardScreen(viewModel = viewModel)
            }
            Screen.GAME_AVIATOR -> {
                AviatorGameScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
            Screen.GAME_CHICKEN_ROAD -> {
                ChickenRoadGameScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
            Screen.GAME_HOT_SPIN -> {
                HotSpinGameScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
            Screen.GAME_CRAZY_777 -> {
                Crazy777GameScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
            Screen.GAME_VORTEX_BOOM -> {
                VortexBoomGameScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
            Screen.GAME_COIN_FLIP -> {
                CoinFlipGameScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
            Screen.GAME_ROULETTE -> {
                RouletteGameScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
            Screen.GAME_WINGO -> {
                WinGoGameScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
            Screen.SUPPORT_CHAT -> {
                SupportChatScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
            Screen.ADMIN_PANEL -> {
                AdminPanelScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                )
            }
        }
    }
}
