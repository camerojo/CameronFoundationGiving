package org.cameronfoundation.aba

/**
 * Columns in CSV - order of Enum matches order of columns, so ordinal gives index of column
 */
@Suppress("unused")
enum class CSVColumn {
    CHARITY,
    DATE,
    STATUS,
    AMOUNT,
    ACCOUNT_NAME,
    BSB,
    ACCOUNT_NUMBER,
    REFERENCE
}
