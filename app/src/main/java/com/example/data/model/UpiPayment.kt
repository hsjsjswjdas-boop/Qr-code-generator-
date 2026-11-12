package com.example.data.model

import java.net.URLEncoder
import java.util.Locale

data class UpiPayment(
    val vpa: String = "",
    val payeeName: String = "",
    val amount: String = "",
    val note: String = "",
    val currency: String = "INR"
) {
    fun buildUri(): String {
        val trimmedVpa = vpa.trim()
        if (trimmedVpa.isBlank()) return ""

        val builder = java.lang.StringBuilder("upi://pay?")
        builder.append("pa=").append(URLEncoder.encode(trimmedVpa, "UTF-8"))

        val trimmedName = payeeName.trim()
        if (trimmedName.isNotBlank()) {
            builder.append("&pn=").append(URLEncoder.encode(trimmedName, "UTF-8"))
        }

        val trimmedAmount = amount.trim()
        val numAmount = trimmedAmount.toDoubleOrNull()
        if (numAmount != null && numAmount > 0) {
            val formattedAmount = String.format(Locale.US, "%.2f", numAmount)
            builder.append("&am=").append(formattedAmount)
        }

        val trimmedNote = note.trim()
        if (trimmedNote.isNotBlank()) {
            builder.append("&tn=").append(URLEncoder.encode(trimmedNote, "UTF-8"))
        }

        builder.append("&cu=INR")
        return builder.toString()
    }

    fun formattedAmount(): String? {
        val num = amount.trim().toDoubleOrNull() ?: return null
        return if (num > 0) String.format(Locale.US, "₹%.2f", num) else null
    }

    companion object {
        private val VPA_REGEX = Regex("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$")

        fun isValidVpa(vpa: String): Boolean {
            return VPA_REGEX.matches(vpa.trim())
        }

        fun isValidAmount(amountStr: String): Boolean {
            val trimmed = amountStr.trim()
            if (trimmed.isEmpty()) return true
            val num = trimmed.toDoubleOrNull() ?: return false
            if (num <= 0) return false
            // Check max 2 decimal places
            val parts = trimmed.split(".")
            return parts.size <= 2 && (parts.size == 1 || parts[1].length <= 2)
        }
    }
}
