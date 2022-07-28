package org.cameronfoundation.aba

import mu.KotlinLogging
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val DESCRIPTIVE_TYPE = "0"
private const val DETAIL_TYPE = "1"
private const val BATCH_TYPE = "7"
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dMMYY")

/**
 * TODO JC Doc
 * @author John Cameron
 */
class Generator(val accountNumber: String, val bankName: String, val bsb: String,
                val description: String, val directEntryUserId: String,
                var includeAccountNumberInDescriptiveRecord: Boolean = true,

                //This is the default remitter used if remitter is not specified on a transaction
                val defaultRemitter: String,
                val userName: String) {

    var processingDate: LocalDate? = null

    private val log = KotlinLogging.logger{}

    fun generate(transactions: List<Transaction>): String {
        var abaString = ""
        var creditTotal = 0
        var debitTotal = 0

        validateDescriptiveRecord()
        abaString += buildDescriptiveRecord()

        for (transaction in transactions) {
            validateDetailRecord(transaction)
            abaString += buildDetailRecord(transaction)

            if (isDebit(transaction)) {
                debitTotal += transaction.amount
            } else {
                creditTotal += transaction.amount
            }
        }

        abaString += buildBatchControlRecord(debitTotal, creditTotal, transactions.size)

        return abaString
    }

    internal fun buildDescriptiveRecord(): String {
        // Record Type
        var line = DESCRIPTIVE_TYPE

        if (includeAccountNumberInDescriptiveRecord) {
            // BSB
            line += bsb

            // Account Number
            line += accountNumber.padStart(9)

            // Reserved - must be a single blank space
            line += " "
        } else {
            // Reserved - must be 17 blank spaces
            line += " ".repeat(17)
        }

        // Sequence Number
        line += "01"

        // Bank Name
        line += bankName

        // Reserved - must be seven blank spaces
        line += " ".repeat(7)

        // User Name
        line += userName.take(26).padEnd(26)

        // User ID
        line += directEntryUserId

        // File Description
        line += description.padEnd(12)

        // Processing Date
        if (processingDate == null) {
            processingDate = LocalDate.now()
        }
        line += LocalDateTime.now().format(dateFormatter)

        // Reserved - 40 blank spaces
        line += " ".repeat(40)

        line += "\n"

        return line
    }

    /**
     * Add a detail record for each transaction.
     */
    internal fun buildDetailRecord(transaction: Transaction): String {
        // Record Type
        var line = DETAIL_TYPE

        // BSB
        line += transaction.bsb

        // Account Number
        line += transaction.accountNumber.padStart(9)

        // Indicator
        line += transaction.indicator

        // Transaction Code
        line += transaction.transactionCode

        // Transaction Amount
        line += "${transaction.amount}".padStart(10, '0')

        // Account Name
        line += transaction.accountName.take(32).padEnd(32)

        // Lodgement Reference
        line += transaction.reference.take(18).padEnd(18)

        // Trace BSB - already validated
        line += bsb

        // Trace Account Number - already validated
        line += accountNumber.padStart(9)

        // Remitter Name - already validated
        val remitterWithDefault = transaction.remitter?: defaultRemitter
        line += remitterWithDefault.take(16).padEnd(16)

        // Withholding amount
        line += "${transaction.taxWithholding}".padStart(8, '0')

        line += "\n"

        return line
    }

    private fun buildBatchControlRecord(debitTotal: Int, creditTotal: Int, numberRecords:Int): String {
        var line = BATCH_TYPE

        // BSB
        line += "999-999"

        // Reserved - must be twelve blank spaces
        line += " ".repeat(12)

        // Batch Net Total
        line += "${kotlin.math.abs(creditTotal - debitTotal)}".padStart(10, '0')

        // Batch Credits Total
        line += "$creditTotal".padStart(10, '0')

        // Batch Debits Total
        line += "$debitTotal".padStart(10, '0')

        // Reserved - must be 24 blank spaces
        line += " ".repeat(24)

        // Number of records
        line += "$numberRecords".padStart(6, '0')

        // Reserved - must be 40 blank spaces
        line += " ".repeat(40)

        return line
    }

    /**
     * @throws Exception if validation fails
     */
    private fun validateDescriptiveRecord() {
        if (!validateBsb(bsb)) {
            throw Exception("Descriptive record bsb is invalid. Required format is 000-000.")
        }

        if (!Regex("""^[\d]{0,9}$""").matches(accountNumber)) {
            throw Exception("Descriptive record account number is invalid. Must be up to 9 digits only.")
        }

        if (!Regex("""^[A-Z]{3}$""").matches(bankName)) {
            throw Exception("Descriptive record bank name is invalid. Must be capital letter abbreviation of length 3.")
        }

        if (!Regex("""^[A-Za-z\s+]*$""").matches(userName)) {
            throw Exception("Descriptive record user name is invalid. Must be letters only and up to 26 characters long.")
        } else if (userName.length > 26) {
            log.warn { "Descriptive record userName will be truncated to 26 characters. $userName" }
        }

        if (!Regex("""^[\d]{6}$""").matches(directEntryUserId)) {
            throw Exception("Descriptive record direct entity user ID is invalid. Must be 6 digits long.")
        }

        if (!Regex("""^[A-Za-z\s]{0,12}$""").matches(description)) {
            throw Exception("Descriptive record description is invalid. Must be letters only and up to 12 characters long.")
        }
    }

    /**
     * @throws Exception if validation fails
     */
    internal fun validateDetailRecord(transaction: Transaction) {
        if (!validateBsb(transaction.bsb)) {
            throw Exception("Detail record bsb is invalid: ${transaction.bsb}. Required format is 000-000.")
        }

        if (!Regex("""^[\d]{0,9}${'$'}""").matches(transaction.accountNumber)) {
            throw Exception("Detail record account number is invalid. Must be up to 9 digits only.")
        }

        if (!Regex("""^N|W|X|Y| """).matches(transaction.indicator)) {
            throw Exception("Detail record transaction indicator is invalid. Must be one of N, W, X, Y or null.")
        }

        if (!Regex("""^[\d]{0,10}$""").matches("${transaction.amount}")) {
            throw Exception("Detail record amount is invalid. Must be expressed in cents, as an unsigned integer, no longer than 10 digits.")
        }

        if (transaction.accountName.length > 32) {
            log.warn { "Detail record account name will be truncated to 32 characters. ${transaction.accountName}" }
        }

        if (!Regex("""^[A-Za-z0-9\s+]*$""").matches(transaction.reference)) {
            throw Exception("Detail record reference is invalid: '${transaction.reference}'. Must be letters or numbers only and up to 18 characters long.")
        } else if (transaction.reference.length > 18) {
            log.warn { "Detail record reference will be truncated to 18 characters. ${transaction.reference}" }
        }


        if (transaction.remitter != null) {
            if (!Regex("""^[A-Za-z\s+]*$""").matches(transaction.remitter)) {
                throw Exception("Detail record remitter is invalid. Must be letters only and up to 16 characters long.")
            } else if (transaction.remitter.length > 16) {
                log.warn { "Detail record remitter will be truncated to 16 characters. ${transaction.remitter}" }
            }
        }

        if (!validateTransactionCode(transaction.transactionCode)) {
            throw Exception("Detail record transaction code invalid.")
        }
    }

    private fun isDebit(transaction: Transaction): Boolean {
        return (TransactionCode.valueOfCode(transaction.transactionCode)
        == TransactionCode.EXTERNALLY_INITIATED_DEBIT)
    }

    private fun validateBsb(bsb: String): Boolean {
        return Regex("""^[\d]{3}-[\d]{3}$""").matches(bsb)
    }

    private fun validateTransactionCode(code: String): Boolean {
        var validated = true
        try {
            TransactionCode.valueOfCode(code)
        } catch (e: Exception) {
            validated = false
        }
        return validated
    }
}
