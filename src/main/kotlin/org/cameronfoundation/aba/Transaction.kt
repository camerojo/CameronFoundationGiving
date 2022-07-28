package org.cameronfoundation.aba

data class Transaction(
    val accountName: String,
    val accountNumber: String,

    //In cents
    val amount: Int,
    val bsb: String,
    val indicator: String = " ",
    val transactionCode: String,
    val reference: String = "",
    val remitter: String? = null,
    val taxWithholding: Int = 0,
)
