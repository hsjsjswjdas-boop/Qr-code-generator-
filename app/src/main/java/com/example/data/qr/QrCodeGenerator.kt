package com.example.data.qr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.data.model.UpiPayment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeGenerator {

    /**
     * Generates a QR code bitmap from the given content string.
     */
    fun generateQrBitmap(
        content: String,
        size: Int = 800,
        darkColor: Int = Color.parseColor("#14201B"),
        lightColor: Int = Color.WHITE,
        includeCenterBadge: Boolean = true
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }

            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (matrix.get(x, y)) darkColor else lightColor
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

            if (includeCenterBadge) {
                drawCenterBadge(bitmap, darkColor)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawCenterBadge(bitmap: Bitmap, accentColor: Int): Bitmap {
        val canvas = Canvas(bitmap)
        val size = bitmap.width.toFloat()
        val center = size / 2f
        val badgeRadius = size * 0.11f

        // White circular background for badge
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, badgeRadius, bgPaint)

        // Accent outline border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = size * 0.012f
        }
        canvas.drawCircle(center, center, badgeRadius - borderPaint.strokeWidth / 2f, borderPaint)

        // Center Indian Rupee symbol ₹
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = badgeRadius * 1.3f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textY = center - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText("₹", center, textY, textPaint)

        return bitmap
    }

    /**
     * Generates a complete high-resolution payment poster card (suitable for sharing and printing).
     */
    fun generatePosterBitmap(payment: UpiPayment, isBengali: Boolean): Bitmap? {
        val upiUri = payment.buildUri()
        if (upiUri.isBlank()) return null

        val qrSize = 650
        val qrBitmap = generateQrBitmap(
            content = upiUri,
            size = qrSize,
            darkColor = Color.parseColor("#0E7A5F"),
            includeCenterBadge = true
        ) ?: return null

        val posterWidth = 900
        val posterHeight = 1350
        val poster = Bitmap.createBitmap(posterWidth, posterHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(poster)

        // Background
        canvas.drawColor(Color.parseColor("#F3F6F4"))

        // Top Header Banner
        val headerHeight = 220f
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0E7A5F")
        }
        canvas.drawRect(0f, 0f, posterWidth.toFloat(), headerHeight, headerPaint)

        // Header Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val headerTitle = if (isBengali) "UPI পেমেন্ট করুন" else "Scan & Pay via UPI"
        canvas.drawText(headerTitle, posterWidth / 2f, 100f, titlePaint)

        // Header Subtitle
        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D0EDE4")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val headerSub = if (isBengali) "যেকোনো UPI অ্যাপ দিয়ে স্ক্যান করুন" else "Accepting all UPI Apps"
        canvas.drawText(headerSub, posterWidth / 2f, 155f, subTitlePaint)

        // White Card Background for QR
        val cardMargin = 50f
        val cardTop = 180f
        val cardBottom = posterHeight - 110f
        val cardRect = RectF(cardMargin, cardTop, posterWidth - cardMargin, cardBottom)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        // Draw QR Code centered in card
        val qrLeft = (posterWidth - qrSize) / 2f
        val qrTop = cardTop + 50f
        canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)

        var curY = qrTop + qrSize + 45f

        // Payee Name
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#14201B")
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val displayName = if (payment.payeeName.isNotBlank()) payment.payeeName.trim() else if (isBengali) "মার্চেন্ট" else "Merchant"
        canvas.drawText(displayName, posterWidth / 2f, curY, namePaint)

        curY += 45f

        // UPI VPA Badge
        val vpaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5D6B64")
            textSize = 30f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(payment.vpa.trim(), posterWidth / 2f, curY, vpaPaint)

        curY += 55f

        // Amount if specified
        val amt = payment.formattedAmount()
        if (amt != null) {
            val amtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0E7A5F")
                textSize = 58f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(amt, posterWidth / 2f, curY, amtPaint)
            curY += 45f
        } else {
            val amtHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#7B8B83")
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            val hintText = if (isBengali) "(টাকার অঙ্ক স্ক্যান করার পর দিন)" else "(Enter amount after scanning)"
            canvas.drawText(hintText, posterWidth / 2f, curY, amtHintPaint)
            curY += 40f
        }

        // Note if specified
        if (payment.note.isNotBlank()) {
            val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#5D6B64")
                textSize = 26f
                textAlign = Paint.Align.CENTER
            }
            val noteLabel = if (isBengali) "নোট: ${payment.note.trim()}" else "Note: ${payment.note.trim()}"
            canvas.drawText(noteLabel, posterWidth / 2f, curY, notePaint)
        }

        // Bottom Footer Bar
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4C6358")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val supportedApps = "GPay • PhonePe • Paytm • BHIM • Cred • Any Bank App"
        canvas.drawText(supportedApps, posterWidth / 2f, posterHeight - 50f, footerPaint)

        return poster
    }
}
