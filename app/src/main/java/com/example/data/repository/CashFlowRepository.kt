package com.example.data.repository

import com.example.data.local.BetRecordDao
import com.example.data.local.ChatMessageDao
import com.example.data.local.TransactionDao
import com.example.data.local.UserDao
import com.example.data.model.BetRecordEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CashFlowRepository(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val betRecordDao: BetRecordDao,
    private val chatMessageDao: ChatMessageDao
) {
    fun getUser(phone: String): Flow<UserEntity?> = userDao.getUserByPhone(phone)
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun generateDistinctUid(): String {
        while (true) {
            val candidate = (10000000L..99999999L).random().toString()
            if (userDao.uidExists(candidate) == 0) {
                return candidate
            }
        }
    }

    suspend fun getOrCreateUser(phone: String, name: String = "VIP Player"): UserEntity {
        val existing = userDao.getUserSync(phone)
        if (existing != null) {
            if (existing.uid.isBlank()) {
                val newUid = generateDistinctUid()
                val updated = existing.copy(uid = newUid)
                userDao.updateUser(updated)
                return updated
            }
            return existing
        }

        val uniqueUid = generateDistinctUid()
        val newUser = UserEntity(
            phone = phone,
            name = name,
            uid = uniqueUid,
            balance = 0.0, // Initial zero balance as requested
            referralCode = "CFPRO" + phone.takeLast(4),
            referralEarnings = 0.0,
            totalReferrals = 0,
            bankAccount = "",
            bankIfsc = "",
            bankHolder = "",
            bankName = "",
            upiId = ""
        )
        userDao.insertUser(newUser)

        // Add a welcoming support message
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                userPhone = phone,
                sender = "SUPPORT",
                text = "Welcome to CashFlow Pro! 🎰 Your account has been registered with UID: $uniqueUid and ₹0 balance. To start playing Aviator, Chicken Road, Hot Spin, Crazy 777, Vortex Boom & Coin Flip, scan the Official QR in Wallet to deposit."
            )
        )

        return newUser
    }

    suspend fun registerOnlineUser(
        phone: String,
        name: String,
        referralCode: String = ""
    ): Result<UserEntity> {
        val existing = userDao.getUserSync(phone)
        if (existing != null) {
            return Result.failure(Exception("Mobile number already registered. Please login instead."))
        }

        val uniqueUid = generateDistinctUid()
        val newUser = UserEntity(
            phone = phone,
            name = name.ifBlank { "VIP Player" },
            uid = uniqueUid,
            balance = 0.0, // Strictly Zero Balance as requested!
            referralCode = "CFPRO" + phone.takeLast(4),
            referredBy = referralCode,
            referralEarnings = 0.0,
            totalReferrals = 0,
            bankAccount = "",
            bankIfsc = "",
            bankHolder = "",
            bankName = "",
            upiId = ""
        )
        userDao.insertUser(newUser)

        chatMessageDao.insertMessage(
            ChatMessageEntity(
                userPhone = phone,
                sender = "SUPPORT",
                text = "Welcome ${newUser.name} to CashFlow Pro! 🎰 Your online registration is complete with UID: $uniqueUid and ₹0 balance. Please deposit via Official QR in Wallet to start playing."
            )
        )

        return Result.success(newUser)
    }

    suspend fun resetUserBalanceToZero(phone: String) {
        userDao.setBalance(phone, 0.0)
    }

    suspend fun updateBankDetails(phone: String, account: String, ifsc: String, holder: String, bankName: String) {
        userDao.updateBankDetails(phone, account, ifsc, holder, bankName)
    }

    suspend fun updateUpiDetails(phone: String, upiId: String) {
        userDao.updateUpiDetails(phone, upiId)
    }

    // Transactions
    fun getUserTransactions(phone: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForUser(phone)

    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    fun getPendingTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getPendingTransactions()

    suspend fun requestDeposit(phone: String, amount: Double, utr: String): Long {
        return submitAndMatchDeposit(phone, amount, utr, isAutoApproved = false)
    }

    suspend fun submitAndMatchDeposit(phone: String, amount: Double, utr: String, isAutoApproved: Boolean): Long {
        val status = if (isAutoApproved) "APPROVED" else "PENDING"
        val note = if (isAutoApproved) "Auto-Verified: Amount ₹${amount.toInt()} matched with UTR $utr" else "UPI Deposit via Merchant QR"
        val transaction = TransactionEntity(
            userPhone = phone,
            type = "DEPOSIT",
            amount = amount,
            method = "UPI",
            targetUpi = com.example.data.model.MerchantConfig.getUpiId(),
            utrOrAccount = utr,
            status = status,
            note = note
        )
        val id = transactionDao.insertTransaction(transaction)
        if (isAutoApproved) {
            userDao.addBalance(phone, amount)
        }
        return id
    }

    suspend fun requestWithdrawal(phone: String, amount: Double, method: String, destination: String): Result<Long> {
        val user = userDao.getUserSync(phone) ?: return Result.failure(Exception("User not found"))
        if (user.balance < amount) {
            return Result.failure(Exception("Insufficient wallet balance. You have ₹${user.balance}"))
        }
        if (amount < 200.0) {
            return Result.failure(Exception("Minimum withdrawal is ₹200.00"))
        }

        // Deduct from user balance temporarily while pending
        userDao.deductBalance(phone, amount)

        val transaction = TransactionEntity(
            userPhone = phone,
            type = "WITHDRAWAL",
            amount = amount,
            method = method,
            targetUpi = "",
            utrOrAccount = destination,
            status = "PENDING",
            note = "Withdrawal to $destination ($method)"
        )
        val id = transactionDao.insertTransaction(transaction)
        return Result.success(id)
    }

    // Admin controls
    suspend fun approveTransaction(id: Long, adminNote: String = "Approved by Admin") {
        val tx = transactionDao.getTransactionById(id) ?: return
        if (tx.status != "PENDING") return

        if (tx.type == "DEPOSIT") {
            // Credit the deposit amount to user's wallet balance
            userDao.addBalance(tx.userPhone, tx.amount)
        }
        transactionDao.updateStatus(id, "APPROVED", adminNote)
    }

    suspend fun rejectTransaction(id: Long, reason: String = "Verification failed") {
        val tx = transactionDao.getTransactionById(id) ?: return
        if (tx.status != "PENDING") return

        if (tx.type == "WITHDRAWAL") {
            // Refund the deducted amount back to user's balance
            userDao.addBalance(tx.userPhone, tx.amount)
        }
        transactionDao.updateStatus(id, "REJECTED", reason)
    }

    suspend fun adjustUserBalance(phone: String, amountDelta: Double) {
        if (amountDelta > 0) {
            userDao.addBalance(phone, amountDelta)
        } else {
            userDao.deductBalance(phone, -amountDelta)
        }
    }

    // Bets & Games
    fun getUserBets(phone: String): Flow<List<BetRecordEntity>> =
        betRecordDao.getBetsForUser(phone)

    fun getRecentBetsForGame(phone: String, gameType: String): Flow<List<BetRecordEntity>> =
        betRecordDao.getRecentBetsForGame(phone, gameType)

    suspend fun placeBetAndSettle(
        phone: String,
        gameType: String,
        betAmount: Double,
        multiplier: Double,
        isWin: Boolean,
        detail: String
    ): Result<Double> {
        val user = userDao.getUserSync(phone) ?: return Result.failure(Exception("User not found"))
        if (user.balance < betAmount) {
            return Result.failure(Exception("Insufficient balance to place bet of ₹$betAmount"))
        }

        val winAmount = if (isWin) betAmount * multiplier else 0.0
        val netChange = winAmount - betAmount

        if (netChange > 0) {
            userDao.addBalance(phone, netChange)
        } else if (netChange < 0) {
            userDao.deductBalance(phone, -netChange)
        }

        betRecordDao.insertBet(
            BetRecordEntity(
                userPhone = phone,
                gameType = gameType,
                betAmount = betAmount,
                multiplier = multiplier,
                winAmount = winAmount,
                isWin = isWin,
                detail = detail
            )
        )

        val updated = userDao.getUserSync(phone)
        return Result.success(updated?.balance ?: 0.0)
    }

    // Referral
    suspend fun claimReferralBonus(phone: String): Double {
        val user = userDao.getUserSync(phone) ?: return 0.0
        val earnings = user.referralEarnings
        if (earnings > 0) {
            userDao.addBalance(phone, earnings)
            userDao.updateUser(user.copy(referralEarnings = 0.0))
        }
        return earnings
    }

    // Chat Support
    fun getChatMessages(phone: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getMessagesForUser(phone)

    suspend fun sendUserMessage(phone: String, messageText: String) {
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                userPhone = phone,
                sender = "USER",
                text = messageText
            )
        )

        // Intelligent auto-response system based on keywords
        val lower = messageText.lowercase()
        val reply = when {
            "deposit" in lower || "utr" in lower || "upi" in lower -> {
                "For deposits: Please scan the Official Merchant QR Code in the Wallet tab to complete payment, then enter the 12-digit UTR/Ref number in the Wallet > Deposit tab. Your payment will match and credit immediately."
            }
            "withdraw" in lower || "bank" in lower -> {
                "Withdrawals are processed directly to your linked Bank Account or UPI ID. Go to Wallet > Withdraw. Minimum withdrawal is ₹200."
            }
            "aviator" in lower || "game" in lower -> {
                "In Aviator, cash out before the jet flies away! Chicken Road allows you to step through lanes with multiplying rewards. Hot Spin & Crazy 777 give instant reel jackpots."
            }
            "admin" in lower -> {
                "You can access the Admin Panel directly from Settings > Admin Panel to test manual approval of deposits & withdrawals."
            }
            else -> {
                "Thank you for contacting CashFlow Pro Customer Care. An agent is reviewing your message. How else can we assist your gaming experience today?"
            }
        }

        chatMessageDao.insertMessage(
            ChatMessageEntity(
                userPhone = phone,
                sender = "SUPPORT",
                text = reply
            )
        )
    }
}
