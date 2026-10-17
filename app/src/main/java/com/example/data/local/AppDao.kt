package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BetRecordEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    fun getUserByPhone(phone: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserSync(phone: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET balance = balance + :amount WHERE phone = :phone")
    suspend fun addBalance(phone: String, amount: Double)

    @Query("UPDATE users SET balance = balance - :amount WHERE phone = :phone")
    suspend fun deductBalance(phone: String, amount: Double)

    @Query("UPDATE users SET balance = :balance WHERE phone = :phone")
    suspend fun setBalance(phone: String, balance: Double)

    @Query("SELECT COUNT(*) FROM users WHERE phone = :phone")
    suspend fun userExists(phone: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE uid = :uid")
    suspend fun uidExists(uid: String): Int

    @Query("UPDATE users SET bankAccount = :account, bankIfsc = :ifsc, bankHolder = :holder, bankName = :bankName WHERE phone = :phone")
    suspend fun updateBankDetails(phone: String, account: String, ifsc: String, holder: String, bankName: String)

    @Query("UPDATE users SET upiId = :upiId WHERE phone = :phone")
    suspend fun updateUpiDetails(phone: String, upiId: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userPhone = :phone ORDER BY timestamp DESC")
    fun getTransactionsForUser(phone: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET status = :status, note = :note WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, note: String)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?
}

@Dao
interface BetRecordDao {
    @Query("SELECT * FROM bet_records WHERE userPhone = :phone ORDER BY timestamp DESC LIMIT 100")
    fun getBetsForUser(phone: String): Flow<List<BetRecordEntity>>

    @Query("SELECT * FROM bet_records WHERE userPhone = :phone AND gameType = :gameType ORDER BY timestamp DESC LIMIT 20")
    fun getRecentBetsForGame(phone: String, gameType: String): Flow<List<BetRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: BetRecordEntity): Long
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE userPhone = :phone ORDER BY timestamp ASC")
    fun getMessagesForUser(phone: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
}
