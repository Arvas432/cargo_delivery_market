package models

import kotlinx.serialization.Serializable

@Serializable
data class Delivery(
    val deliveryId: String,
    val startDate: String,
    val endDate: String,
    val cargo: List<Cargo>
)
