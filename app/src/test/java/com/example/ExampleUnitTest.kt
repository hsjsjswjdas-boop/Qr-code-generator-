package com.example

import com.example.data.model.UpiPayment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testValidVpaValidation() {
        assertTrue(UpiPayment.isValidVpa("yourname@okaxis"))
        assertTrue(UpiPayment.isValidVpa("posterstudio@okhdfcbank"))
        assertTrue(UpiPayment.isValidVpa("user.name@ybl"))
        assertTrue(UpiPayment.isValidVpa("shop_123@paytm"))
        assertTrue(UpiPayment.isValidVpa("merchant-99@icici"))

        assertFalse(UpiPayment.isValidVpa("invalidvpa"))
        assertFalse(UpiPayment.isValidVpa("@okaxis"))
        assertFalse(UpiPayment.isValidVpa("name@"))
        assertFalse(UpiPayment.isValidVpa(""))
    }

    @Test
    fun testValidAmountValidation() {
        assertTrue(UpiPayment.isValidAmount(""))
        assertTrue(UpiPayment.isValidAmount("50"))
        assertTrue(UpiPayment.isValidAmount("120.50"))
        assertTrue(UpiPayment.isValidAmount("9999.99"))

        assertFalse(UpiPayment.isValidAmount("-50"))
        assertFalse(UpiPayment.isValidAmount("0"))
        assertFalse(UpiPayment.isValidAmount("abc"))
        assertFalse(UpiPayment.isValidAmount("12.345"))
    }

    @Test
    fun testUpiUriConstruction_withAllFields() {
        val payment = UpiPayment(
            vpa = "posterstudio@okaxis",
            payeeName = "Poster Studio",
            amount = "120",
            note = "Poster Order"
        )
        val uri = payment.buildUri()

        assertTrue(uri.startsWith("upi://pay?"))
        assertTrue(uri.contains("pa=posterstudio%40okaxis"))
        assertTrue(uri.contains("pn=Poster+Studio") || uri.contains("pn=Poster%20Studio"))
        assertTrue(uri.contains("am=120.00"))
        assertTrue(uri.contains("tn=Poster+Order") || uri.contains("tn=Poster%20Order"))
        assertTrue(uri.contains("cu=INR"))
    }

    @Test
    fun testUpiUriConstruction_withoutAmount() {
        val payment = UpiPayment(
            vpa = "myname@upi",
            payeeName = "My Name",
            amount = "",
            note = ""
        )
        val uri = payment.buildUri()

        assertTrue(uri.contains("pa=myname%40upi"))
        assertFalse(uri.contains("am="))
        assertFalse(uri.contains("tn="))
        assertTrue(uri.contains("cu=INR"))
    }
}
