package org.cameronfoundation.aba

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class TransactionCodeTest {

    @Test
    fun valueOfCode() {
        assertEquals(TransactionCode.EXTERNALLY_INITIATED_DEBIT, TransactionCode.valueOfCode("13"))

        try {
            TransactionCode.valueOfCode("123")
            fail("Should throw exception")
        } catch (e: Exception) {
            println(e)
            assertTrue(e is IllegalArgumentException)
        }
    }
}
