package com.example.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.CashFlowViewModel
import com.example.ui.screens.account.AccountScreen

/**
 * Account Screen (Forwarder from ProfileScreen)
 */
@Composable
fun ProfileScreen(
    viewModel: CashFlowViewModel,
    modifier: Modifier = Modifier
) {
    AccountScreen(viewModel = viewModel, modifier = modifier)
}
