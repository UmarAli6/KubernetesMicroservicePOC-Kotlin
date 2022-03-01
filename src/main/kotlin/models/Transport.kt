package models

import kotlinx.serialization.Serializable

@Serializable
data class Transport(
    val user: String,
    val newValue: NewValueTransport,
    val changeComment: String = ""
)

@Serializable
data class NewValueTransport(
    val jobValidityDuration: String,
    var modificationDate: String = "",
    val sms: Sms,
    val httpOta: HttpOta
)

@Serializable
data class NewValueTransportFalse(
    val jobValidityDuration: String,
    val modificationDate: String = "",
    val sms: Sms,
    val httpOta: HttpOtaFalse
)

@Serializable
data class Sms(
    val smOtaValidityDuration: String
)

@Serializable
data class HttpOta(
    val smHttpTrigEnabled: Boolean,
    val smHttpTrigValidityDuration: String,
    val smHttpTrigBlacklistedCardProfiles: List<String>
)

@Serializable
data class HttpOtaFalse(
    val smHttpTrigEnabled: Boolean,
    val smHttpTrigBlacklistedCardProfiles: List<String>
)