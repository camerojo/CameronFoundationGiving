package org.cameronfoundation.aba

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class GeneratorTest {

    @BeforeEach
    internal fun setUp() {
    }

    @Test
    fun `buildDetailRecord$TestKotlin`() {

        val bsbRegex = """^[\d]{3}-[\d]{3}$""".toRegex()
        assertTrue(bsbRegex.matches("123-456"))

        val generator = Generator(
            bsb = "063-142",
            accountNumber = "10419362",
            bankName = "CBA",
            userName = "The Cameron Family Foundation",
            defaultRemitter = "CameronFoundation",
            directEntryUserId = "301500",
            description = "Donations"
        )

        var transaction = Transaction(
            accountName = "The Cameron Family Foundation",
            accountNumber = "10419362",
            amount = 45000000,
            bsb = "063-142",
            indicator = "N",
            transactionCode = TransactionCode.EXTERNALLY_INITIATED_DEBIT.code,
            reference = "Donations",
        )

        var abaDetail = generator.buildDetailRecord(transaction)

        println(abaDetail)

        transaction = Transaction(
            accountName = "Murdoch Childrens Research Institute",
            accountNumber = "123456",
            amount = 45000000,
            bsb = "123-456",
            indicator = "N",
//            remitter = "CameronFoundation",
            transactionCode = TransactionCode.EXTERNALLY_INITIATED_CREDIT.code,
            reference = "HOL CameronFoundation",
        )

        abaDetail = generator.buildDetailRecord(transaction)

        println(abaDetail)
    }

    @Test
    fun `validateDetailRecord$TestKotlin`() {

        val generator = Generator(
            bsb = "123-123",
            accountNumber = "12345678",
            bankName = "CBA",
            userName = "Some name",
            defaultRemitter = "From some guy",
            directEntryUserId = "999999",
            description = "Payroll"
        )

        val transaction = Transaction(
            accountName = "Murdoch Childrens Research Institute",
            accountNumber = "123456",
            amount = 12345,
            bsb = "123-456",
            indicator = "N",
            transactionCode = TransactionCode.EXTERNALLY_INITIATED_CREDIT.code,
            reference = "HOL CameronFoundation",
            remitter = "CameronFoundation",
        )

        generator.validateDetailRecord(transaction)
    }

    @Test
    fun `buildDescriptiveRecord$TestKotlin`() {

        val generator = Generator(
            bsb = "123-123",
            accountNumber = "12345678",
            bankName = "CBA",
            userName = "Some name",
            defaultRemitter = "From some guy",
            directEntryUserId = "999999",
            description = "Payroll"
        )
        val abaDescriptive = generator.buildDescriptiveRecord()

        println(abaDescriptive)
    }
}
