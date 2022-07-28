package org.cameronfoundation.aba

enum class TransactionCode(val code: String) {
    EXTERNALLY_INITIATED_DEBIT("13"),
    EXTERNALLY_INITIATED_CREDIT("50"),
    AUSTRALIAN_GOVERNMENT_SECURITY_INTEREST("51"),
    FAMILY_ALLOWANCE("52"),
    PAYROLL_PAYMENT("53"),
    PENSION_PAYMENT("54"),
    ALLOTMENT("55"),
    DIVIDEND("56"),
    DEBENTURE_OR_NOTE_INTEREST("57");

    companion object {
        fun valueOfCode(code: String): TransactionCode {
            val value = TransactionCode.values().find { tc -> tc.code == code }
            return value ?: throw IllegalArgumentException("$code is not a valid TransactionCode value")
        }
    }
}
