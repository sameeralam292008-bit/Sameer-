package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CashFlowDatabase
import com.example.data.model.BetRecordEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.repository.CashFlowRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    AUTH,
    DASHBOARD,
    GAME_AVIATOR,
    GAME_CHICKEN_ROAD,
    GAME_HOT_SPIN,
    GAME_CRAZY_777,
    GAME_VORTEX_BOOM,
    GAME_COIN_FLIP,
    GAME_ROULETTE,
    GAME_WINGO,
    SUPPORT_CHAT,
    ADMIN_PANEL
}

enum class DashboardTab {
    GAMES,
    WALLET,
    HISTORY,
    ACCOUNT,
    SETTINGS;

    companion object {
        val PROFILE get() = ACCOUNT
    }
}

class CashFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CashFlowDatabase.getInstance(application)
    private val repository = CashFlowRepository(
        db.userDao(),
        db.transactionDao(),
        db.betRecordDao(),
        db.chatMessageDao()
    )

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(DashboardTab.GAMES)
    val currentTab: StateFlow<DashboardTab> = _currentTab.asStateFlow()

    private val _currentPhone = MutableStateFlow("+91 98765 43210")
    val currentPhone: StateFlow<String> = _currentPhone.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val currentUser: StateFlow<UserEntity?> = _currentPhone.flatMapLatest { phone ->
        if (phone.isNotBlank()) repository.getUser(phone) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userTransactions: StateFlow<List<TransactionEntity>> = _currentPhone.flatMapLatest { phone ->
        if (phone.isNotBlank()) repository.getUserTransactions(phone) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userBets: StateFlow<List<BetRecordEntity>> = _currentPhone.flatMapLatest { phone ->
        if (phone.isNotBlank()) repository.getUserBets(phone) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<TransactionEntity>> = repository.getPendingTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = _currentPhone.flatMapLatest { phone ->
        if (phone.isNotBlank()) repository.getChatMessages(phone) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize default demo user
        viewModelScope.launch {
            repository.getOrCreateUser(_currentPhone.value, "VIP Gamer")
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectTab(tab: DashboardTab) {
        _currentTab.value = tab
        _currentScreen.value = Screen.DASHBOARD
    }

    fun loginWithPhone(phone: String, name: String = "VIP Player") {
        viewModelScope.launch {
            val formatted = if (phone.startsWith("+91")) phone else "+91 $phone"
            _currentPhone.value = formatted
            _isLoggedIn.value = true
            repository.getOrCreateUser(formatted, name)
            _currentScreen.value = Screen.DASHBOARD
            _toastEvent.emit("Login successful! Welcome to CashFlow Pro")
        }
    }

    fun registerOnline(name: String, phone: String, referralCode: String = "") {
        viewModelScope.launch {
            val formatted = if (phone.startsWith("+91")) phone else "+91 $phone"
            val result = repository.registerOnlineUser(formatted, name, referralCode)
            result.onSuccess { user ->
                _currentPhone.value = formatted
                _isLoggedIn.value = true
                _currentScreen.value = Screen.DASHBOARD
                _toastEvent.emit("Registration successful! Account UID: ${user.displayUid} (₹0 Balance).")
            }.onFailure {
                // If already registered, still log them in seamlessly
                _currentPhone.value = formatted
                _isLoggedIn.value = true
                _currentScreen.value = Screen.DASHBOARD
                _toastEvent.emit("Account verified! Logged in with $formatted")
            }
        }
    }

    fun resetBalanceToZero() {
        viewModelScope.launch {
            repository.resetUserBalanceToZero(_currentPhone.value)
            _toastEvent.emit("Wallet balance set to ₹0.00")
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentScreen.value = Screen.AUTH
    }

    fun submitDeposit(amount: Double, utr: String) {
        viewModelScope.launch {
            val cleanUtr = utr.trim().filter { it.isDigit() }
            if (cleanUtr.length != 12) {
                _toastEvent.emit("Please enter complete 12-digit UPI UTR number (${cleanUtr.length}/12)")
                return@launch
            }
            if (amount < 100.0 || amount > 50000.0) {
                _toastEvent.emit("Deposit amount must be between ₹100 and ₹50,000")
                return@launch
            }

            // Payment Amount & 12-digit UTR matching validation
            val isMatched = cleanUtr.length == 12 && amount in 100.0..50000.0
            repository.submitAndMatchDeposit(_currentPhone.value, amount, cleanUtr, isAutoApproved = isMatched)

            if (isMatched) {
                _toastEvent.emit("✅ Payment Matched! ₹${amount.toInt()} credited to your wallet balance!")
            } else {
                _toastEvent.emit("Deposit of ₹${amount.toInt()} submitted! Sent to Admin for review.")
            }
        }
    }

    fun submitWithdrawal(amount: Double, method: String, destination: String) {
        viewModelScope.launch {
            if (destination.isBlank()) {
                _toastEvent.emit("Please enter your Bank or UPI withdrawal details")
                return@launch
            }
            val result = repository.requestWithdrawal(_currentPhone.value, amount, method, destination)
            result.onSuccess {
                _toastEvent.emit("Withdrawal of ₹$amount requested! Under verification.")
            }.onFailure { err ->
                _toastEvent.emit(err.message ?: "Withdrawal failed")
            }
        }
    }

    fun updateBankDetails(account: String, ifsc: String, holder: String, bankName: String) {
        viewModelScope.launch {
            repository.updateBankDetails(_currentPhone.value, account, ifsc, holder, bankName)
            _toastEvent.emit("Bank account details saved successfully")
        }
    }

    fun updateUpiDetails(upiId: String) {
        viewModelScope.launch {
            repository.updateUpiDetails(_currentPhone.value, upiId)
            _toastEvent.emit("UPI ID saved successfully")
        }
    }

    fun claimReferralBonus() {
        viewModelScope.launch {
            val claimed = repository.claimReferralBonus(_currentPhone.value)
            if (claimed > 0) {
                _toastEvent.emit("Claimed ₹$claimed commission to your wallet balance!")
            } else {
                _toastEvent.emit("No pending referral commission to claim")
            }
        }
    }

    fun sendSupportMessage(text: String) {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                repository.sendUserMessage(_currentPhone.value, text)
            }
        }
    }

    fun approveTransaction(id: Long) {
        viewModelScope.launch {
            repository.approveTransaction(id)
            _toastEvent.emit("Transaction #$id Approved!")
        }
    }

    fun rejectTransaction(id: Long) {
        viewModelScope.launch {
            repository.rejectTransaction(id)
            _toastEvent.emit("Transaction #$id Rejected.")
        }
    }

    fun adjustUserBalance(phone: String, delta: Double) {
        viewModelScope.launch {
            repository.adjustUserBalance(phone, delta)
            _toastEvent.emit("User balance adjusted by ₹$delta")
        }
    }

    fun placeGameBet(
        gameType: String,
        betAmount: Double,
        multiplier: Double,
        isWin: Boolean,
        detail: String,
        onResult: (Boolean, Double) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null || user.balance < betAmount) {
                _toastEvent.emit("Insufficient balance! Please deposit to play.")
                onResult(false, 0.0)
                return@launch
            }
            val res = repository.placeBetAndSettle(
                phone = _currentPhone.value,
                gameType = gameType,
                betAmount = betAmount,
                multiplier = multiplier,
                isWin = isWin,
                detail = detail
            )
            res.onSuccess { newBal ->
                onResult(true, newBal)
            }.onFailure { err ->
                _toastEvent.emit(err.message ?: "Bet error")
                onResult(false, 0.0)
            }
        }
    }
}
