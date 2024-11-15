package models

import kotlinx.serialization.Serializable

@Serializable
data class Tender(
    val longtermContractId: String,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val employerAccountId: Int,
    val contractorAccountId: Int,
    val price: Double
)

