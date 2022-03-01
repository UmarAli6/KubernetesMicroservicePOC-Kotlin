package models

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class SMSC(
    val user: String,
    val newValue: NewValueSMSC,
    val changeComment: String = ""
)

@Serializable
data class NewValueSMSC(
    var modificationDate: String = "",
    val routeOn: String,
    val groups: Groups,
    val receivers: List<Receiver>
)

@Serializable
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class Groups(
    @JsonProperty("test-group")
    @SerializedName("test-group")
    var test_group: TestGroup,

    @JsonProperty("default-group")
    @SerializedName("default-group")
    var default_group: DefaultGroup
)


@Serializable
data class Receiver(
    val name: String,
    val systemId: String,
    val password: String,
    val type: String,
    val systemType: String,
    val countryCode: Int,
    val source: Source,
    val address: Address
)

@Serializable
data class TestGroup(
    val routes: List<Routes>,
    val priority: Int,
    val binds: List<Binds>
)

@Serializable
data class DefaultGroup(
    val routes: List<Routes>,
    val priority: Int,
    val default: Boolean,
    val binds: List<Binds>
)

@Serializable
data class Routes(
    val pattern: String,
    val value: String
)

@Serializable
data class Binds(
    val name: String,
    val systemId: String,
    val password: String,
    val windowSize: Int,
    val type: String,
    val systemType: String,
    val countryCode: Int,
    val destinationNumberFormat: DestinationNumberFormat? = null,
    val source: Source,
    val address: Address
)

@Serializable
data class DestinationNumberFormat(
    val ton: Int,
    val npi: Int
)

@Serializable
data class Source(
    val address: String,
    val ton: Int,
    val npi: Int
)

@Serializable
data class Address(
    val port: Long,
    val address: String
)