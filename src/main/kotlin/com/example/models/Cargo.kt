package models

import kotlinx.serialization.Serializable

@Serializable
data class Cargo(
    val cargoId: String,
    val cargoType: String,
    val nettWeight: Double
)
