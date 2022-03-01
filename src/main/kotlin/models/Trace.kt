package models

import kotlinx.serialization.Serializable

@Serializable
data class Trace(
    val user: String,
    val newValue: NewValueTrace,
    val changeComment: String = ""
)

@Serializable
data class NewValueTrace(
    var modificationDate: String = "",
    val traceRateLimitPerContainerPerMinute: Int,
    val cardsToTrace: CardsToTrace,
)

@Serializable
data class CardsToTrace(
    val iccidList: List<String>,
    val eidList: List<String>
)