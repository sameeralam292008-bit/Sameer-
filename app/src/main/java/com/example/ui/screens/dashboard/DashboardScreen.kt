package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.CashFlowViewModel
import com.example.ui.DashboardTab
import com.example.ui.Screen
import com.example.ui.components.CashFlowTopBar
import com.example.ui.components.CustomBottomNav
import com.example.ui.screens.account.AccountScreen
import com.example.ui.screens.games.GamesLobbyScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.theme.BgDark

@Composable
fun DashboardScreen(
    viewModel: CashFlowViewModel
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        topBar = {
            CashFlowTopBar(
                balance = currentUser?.balance ?: 0.0,
                onDepositClick = {
                    viewModel.selectTab(DashboardTab.WALLET)
                },
                onSupportClick = {
                    viewModel.navigateTo(Screen.SUPPORT_CHAT)
                },
                onAdminClick = {
                    viewModel.navigateTo(Screen.ADMIN_PANEL)
                }
            )
        },
        bottomBar = {
            CustomBottomNav(
                selectedTab = currentTab,
                onTabSelected = { tab -> viewModel.selectTab(tab) }
            )
        },
        containerColor = BgDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                DashboardTab.GAMES -> {
                    GamesLobbyScreen(
                        onNavigateToGame = { screen -> viewModel.navigateTo(screen) }
                    )
                }
                DashboardTab.WALLET -> {
                    WalletScreen(viewModel = viewModel)
                }
                DashboardTab.HISTORY -> {
                    HistoryScreen(viewModel = viewModel)
                }
                DashboardTab.ACCOUNT -> {
                    AccountScreen(viewModel = viewModel)
                }
                DashboardTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAdmin = { viewModel.navigateTo(Screen.ADMIN_PANEL) },
                        onNavigateToSupport = { viewModel.navigateTo(Screen.SUPPORT_CHAT) }
                    )
                }
            }
        }
    }
}
