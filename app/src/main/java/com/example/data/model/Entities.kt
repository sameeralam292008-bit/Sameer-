package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val phone: String,
    val name: String,
    val uid: String = "", // Distinct Unique Account UID for each registered user
    val balance: Double = 0.0,
    val referralCode: String = "CFPRO786",
    val referredBy: String = "",
    val referralEarnings: Double = 0.0,
    val totalReferrals: Int = 0,
    val bankAccount: String = "",
    val bankIfsc: String = "",
    val bankHolder: String = "",
    val bankName: String = "",
    val upiId: String = "",
    val vipLevel: Int = 1,
    val isVerified: Boolean = true
) {
    // Unique formatted UID for user display
    val displayUid: String
        get() = if (uid.isNotBlank()) uid else (10000000L + kotlin.math.abs(phone.hashCode()) % 90000000L).toString()
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userPhone: String,
    val type: String, // "DEPOSIT" or "WITHDRAWAL"
    val amount: Double,
    val method: String, // "UPI" or "BANK_TRANSFER"
    val targetUpi: String = "",
    val utrOrAccount: String, // 12-digit UTR reference for deposit; UPI ID / Bank A/C for withdrawal
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "bet_records")
data class BetRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userPhone: String,
    val gameType: String, // "AVIATOR", "CHICKEN_ROAD", "HOT_SPIN", "CRAZY_777", "VORTEX_BOOM", "COIN_FLIP"
    val betAmount: Double,
    val multiplier: Double,
    val winAmount: Double,
    val isWin: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val detail: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userPhone: String,
    val sender: String, // "USER" or "SUPPORT"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
