package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UpiPayment
import com.example.data.qr.QrCodeGenerator
import com.example.data.storage.UpiStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("UPI QR Maker", appName)
    }

    @Test
    fun `test qr generation with valid upi payment`() {
        val payment = UpiPayment(
            vpa = "posterstudio@okaxis",
            payeeName = "Poster Studio",
            amount = "120"
        )
        val bitmap = QrCodeGenerator.generateQrBitmap(payment.buildUri(), size = 400)
        assertNotNull(bitmap)
        assertEquals(400, bitmap?.width)
        assertEquals(400, bitmap?.height)
    }

    @Test
    fun `test local storage profiles initialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = UpiStorage(context)
        val profiles = storage.getSavedProfiles()
        assertTrue(profiles.isNotEmpty())
    }
}
