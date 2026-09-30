package com.example

import com.example.data.model.Order
import com.example.ui.components.formatCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testOrderRemainingBalanceCalculation() {
        val order = Order(
            orderNo = 101,
            customerId = 1L,
            dressType = "Shalwar Kameez",
            orderDate = "2026-09-30",
            deliveryDate = "2026-10-05",
            totalAmount = 3000.0,
            advanceAmount = 1000.0
        )

        assertEquals(2000.0, order.remainingAmount, 0.001)
        assertFalse(order.isPaid)
        assertTrue(order.paymentStatus.contains("Partial"))
    }

    @Test
    fun testFullyPaidOrder() {
        val order = Order(
            orderNo = 102,
            customerId = 2L,
            dressType = "Waistcoat",
            orderDate = "2026-09-30",
            deliveryDate = "2026-10-05",
            totalAmount = 2500.0,
            advanceAmount = 2500.0
        )

        assertEquals(0.0, order.remainingAmount, 0.001)
        assertTrue(order.isPaid)
        assertTrue(order.paymentStatus.contains("Paid"))
    }

    @Test
    fun testFormatCurrency() {
        assertEquals("Rs. 2,500", formatCurrency(2500.0))
        assertEquals("Rs. 10,000", formatCurrency(10000.0))
        assertEquals("Rs. 0", formatCurrency(0.0))
    }
}
