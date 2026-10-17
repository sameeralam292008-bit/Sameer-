package com.example.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Payment QR Profiles corresponding to the 3 QRs uploaded by the user:
 * 1. PhonePe Official (Shahanwaz)
 * 2. PhonePe Official (Bank of India - 1335)
 * 3. VIP Gaming Golden Falcon QR
 */
enum class PaymentQrProfile(
    val id: String,
    val title: String,
    val merchantName: String,
    val upiId: String,
    val subtitle: String,
    val seed: Long
) {
    PHONEPE_SHAHANWAZ(
        id = "phonepe_shahanwaz",
        title = "PhonePe (Shahanwaz)",
        merchantName = "Shahanwaz",
        upiId = "shahanwaz@ybl",
        subtitle = "Accepted Here • Scan & Pay Using PhonePe App",
        seed = 0x50484F4E45504531L
    ),
    PHONEPE_BOI_1335(
        id = "phonepe_boi_1335",
        title = "PhonePe (Bank of India)",
        merchantName = "Bank Of India - 1335",
        upiId = "phonepe.boi1335@ibl",
        subtitle = "Bank Of In... - 1335",
        seed = 0x424F493133333532L
    ),
    VIP_GOLDEN_FALCON(
        id = "vip_golden_falcon",
        title = "VIP Golden Falcon",
        merchantName = "VIP Gaming Gateway",
        upiId = "vipgaming.pay@okaxis",
        subtitle = "VIP Fast Pay • Instant 24/7",
        seed = 0x56495046414C4333L
    )
}

/**
 * Global Merchant Configuration for UPI Payments & QR Code.
 * Automatically alternates / rotates between the 3 official QRs on each payment.
 */
object MerchantConfig {
    private var rotationCounter = 0

    // Current merchant UPI ID (defaults to active rotated QR)
    private val _merchantUpiId = MutableStateFlow("shahanwaz@ybl")
    val merchantUpiId = _merchantUpiId.asStateFlow()

    private val _merchantName = MutableStateFlow("Shahanwaz")
    val merchantName = _merchantName.asStateFlow()

    private val _supportPhone = MutableStateFlow("+91 98765 43210")
    val supportPhone = _supportPhone.asStateFlow()

    /**
     * Cycles to the next QR profile for every new payment initiated
     */
    fun getNextQrProfile(): PaymentQrProfile {
        val profiles = PaymentQrProfile.values()
        val profile = profiles[rotationCounter % profiles.size]
        rotationCounter++
        _merchantUpiId.value = profile.upiId
        _merchantName.value = profile.merchantName
        return profile
    }

    fun updateMerchantDetails(newUpiId: String, newName: String = "CashFlow Pro Official") {
        if (newUpiId.isNotBlank()) {
            _merchantUpiId.value = newUpiId.trim()
        }
        if (newName.isNotBlank()) {
            _merchantName.value = newName.trim()
        }
    }

    fun updateSupportPhone(newPhone: String) {
        if (newPhone.isNotBlank()) {
            _supportPhone.value = newPhone.trim()
        }
    }

    fun getUpiId(): String = _merchantUpiId.value
    fun getName(): String = _merchantName.value
    fun getPhone(): String = _supportPhone.value
}
