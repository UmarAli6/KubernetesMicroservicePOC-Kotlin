package models

import kotlinx.serialization.Serializable

@Serializable
data class Cardowners(
    val user: String,
    val newValue: NewValueCardowners,
    val changeComment: String = ""
)

@Serializable
data class NewValueCardowners(
    var modificationDate: String = "",
    val owners: List<Owners>,
)

@Serializable
data class Owners(
    val iccidList: List<String>,
    val tenant: String
)