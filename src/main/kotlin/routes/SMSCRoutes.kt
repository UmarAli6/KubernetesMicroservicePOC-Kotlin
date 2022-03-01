package routes

import connection.*
import database.*
import models.SMSC
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.request.*
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.sql.*
import com.google.gson.Gson

private const val smscID = 2
private var message = ""
public const val insertSMSCDefaultValueQuery = "INSERT INTO J_TRANSPORT VALUES (2, '{\n" +
        " \"user\": \"Paul\",\n" +
        " \"newValue\": {\n" +
        " \"routeOn\": \"MSISDN\",\n" +
        " \"groups\": {\n" +
        " \"test-group\": {\n" +
        " \"routes\": [\n" +
        " {\n" +
        " \"pattern\": \"range\",\n" +
        " \"value\": \"1234..5678\"\n" +
        " },\n" +
        " {\n" +
        " \"pattern\": \"regex\",\n" +
        " \"value\": \"^999\"\n" +
        " }\n" +
        " ],\n" +
        " \"priority\": 0,\n" +
        " \"binds\": [\n" +
        " {\n" +
        " \"name\": \"awesome-bind\",\n" +
        " \"systemId\": \"csn\",\n" +
        " \"password\": \"myPass\",\n" +
        " \"windowSize\": 100,\n" +
        " \"type\": \"transceiver\",\n" +
        " \"systemType\": \"ota\",\n" +
        " \"countryCode\": 45,\n" +
        " \"destinationNumberFormat\": {\n" +
        " \"ton\": 2,\n" +
        " \"npi\": 1\n" +
        " },\n" +
        " \"source\": {\n" +
        " \"address\": \"557624804487\",\n" +
        " \"ton\": 1,\n" +
        " \"npi\": 1\n" +
        " },\n" +
        " \"address\": {\n" +
        " \"port\": 2775,\n" +
        " \"adress\": \"firstsmsc\"\n" +
        " }\n" +
        " },\n" +
        " {\n" +
        " \"name\": \"awesome-bind-2\",\n" +
        " \"systemId\": \"csn\",\n" +
        " \"password\": \"myPass\",\n" +
        " \"windowSize\": 100,\n" +
        " \"type\": \"transceiver\",\n" +
        " \"systemType\": \"ota\",\n" +
        " \"countryCode\": 45,\n" +
        " \"source\": {\n" +
        " \"address\": \"557624804487\",\n" +
        " \"ton\": 1,\n" +
        " \"npi\": 1\n" +
        " },\n" +
        " \"address\": {\n" +
        " \"port\": 2775,\n" +
        " \"adress\": \"secondsmsc\"\n" +
        " }\n" +
        " }\n" +
        " ]\n" +
        " },\n" +
        " \"default-group\": {\n" +
        " \"routes\": [\n" +
        " {\n" +
        " \"pattern\": \"range\",\n" +
        " \"value\": \"888..999\"\n" +
        " }\n" +
        " ],\n" +
        " \"priority\": 1,\n" +
        " \"default\": true,\n" +
        " \"binds\": [\n" +
        " {\n" +
        " \"name\": \"mediocre-bind\",\n" +
        " \"systemId\": \"csn\",\n" +
        " \"password\": \"myPass\",\n" +
        " \"windowSize\": 100,\n" +
        " \"type\": \"transceiver\",\n" +
        " \"systemType\": \"ota\",\n" +
        " \"countryCode\": 45,\n" +
        " \"source\": {\n" +
        " \"address\": \"557624804487\",\n" +
        " \"ton\": 1,\n" +
        " \"npi\": 1\n" +
        " },\n" +
        " \"address\": {\n" +
        " \"port\": 2775,\n" +
        " \"address\": \"thirdsmsc\"\n" +
        " }\n" +
        " }\n" +
        " ]\n" +
        " }\n" +
        " },\n" +
        " \"receivers\": [\n" +
        " {\n" +
        " \"name\": \"im-just-receiving\",\n" +
        " \"systemId\": \"csn\",\n" +
        " \"password\": \"myPass\",\n" +
        " \"type\": \"receiver\",\n" +
        " \"systemType\": \"ota\",\n" +
        " \"countryCode\": 45,\n" +
        " \"source\": {\n" +
        " \"address\": \"557624804487\",\n" +
        " \"ton\": 1,\n" +
        " \"npi\": 1\n" +
        " },\n" +
        " \"address\": {\n" +
        " \"port\": 2775,\n" +
        " \"address\": \"fourthsmsc\"\n" +
        " }\n" +
        " }\n" +
        " ]\n" +
        " },\n" +
        " \"changedComment\": \"Comment that describes what has changed\"\n" +
        "}')"

fun Route.smscRouting() {
    checkDefaultValue(smscID)

    route("/api/v1/businessconfig/smsc") {
        get(smscID)
        put {
            try {
                val fil = call.receive<SMSC>()
                if (!fil.newValue.modificationDate.isEmpty()) {
                    val text =
                        "You are not allowed to put modificationDate." +
                                "Server is ignoring this value "
                    if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                        logger.debug(text)
                    }
                }
                fil.newValue.modificationDate =
                    ZonedDateTime.now(ZoneId.of("Europe/Stockholm")).truncatedTo(ChronoUnit.SECONDS)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))

                val stmt: Statement = conn!!.createStatement()
                val rs: ResultSet = stmt.executeQuery(selectQuery + smscID)
                if (!rs.next()) {
                    message = createMessage(emptyDBError, emptyDBErrorText, defaultUserAction)
                    if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                        logger.debug(message)
                    }
                    call.respondText(message, status = HttpStatusCode.NotFound)
                } else {
                    val test = Gson()
                    val output: String = test.toJson(fil)
                    val statement: PreparedStatement = conn!!.prepareStatement(updateQuery + smscID)
                    statement.setObject(1, output)
                    val row: Int = statement.executeUpdate()
                    if (row > 0) {
                        if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                            logger.debug(updatedCorrectlyText)
                        }
                        call.respondText(updatedCorrectlyText, status = HttpStatusCode.OK)
                    } else {
                        message = createMessage(
                            notUpdatedCorrectlyError,
                            notUpdatedCorrectlyErrorText,
                            defaultUserAction
                        )

                        if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                            logger.debug(message)
                        }
                        call.respondText(message, status = HttpStatusCode.BadRequest)
                    }
                }
            } catch (ex: kotlinx.serialization.SerializationException) {
                message =
                    createMessage(ex.javaClass.canonicalName, serializingErrorText, defaultUserAction)
                logger.error(message)

                if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                    logger.debug(ex.stackTraceToString())
                }
                call.respondText(message, status = HttpStatusCode.BadRequest)
            } catch (ex: kotlinx.serialization.SerializationException) {
                message =
                    createMessage(ex.javaClass.canonicalName, serializingErrorText, defaultUserAction)
                logger.error(message)

                if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                    logger.debug(ex.stackTraceToString())
                }
                call.respondText(message, status = HttpStatusCode.BadRequest)
            } catch (ex: com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException) {
                message = createMessage(ex.javaClass.canonicalName, jsonValuesErrorText, jsonValuesErrorUserAction)
                logger.error(message)

                if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                    logger.debug(ex.stackTraceToString())
                }
                call.respondText(message, status = HttpStatusCode.BadRequest)
            } catch (ex: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException) {
                message =
                    createMessage(ex.javaClass.canonicalName, unknownValuesErrorText, unknownValuesErrorUserAction)
                logger.error(message)

                if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                    logger.debug(ex.stackTraceToString())
                }
                call.respondText(message, status = HttpStatusCode.BadRequest)
            } catch (ex: com.fasterxml.jackson.databind.exc.InvalidFormatException) {
                message =
                    createMessage(
                        ex.javaClass.canonicalName,
                        invalidJsonFormatErrorText,
                        invalidJsonFormatErrorUserAction
                    )
                logger.error(message)

                if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                    logger.debug(ex.stackTraceToString())
                }
                call.respondText(message, status = HttpStatusCode.BadRequest)
            } catch (ex: SQLException) {
                message = createMessage(ex.javaClass.canonicalName, dbErrorText, dbErrorUserAction)
                logger.error(message)

                if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                    logger.debug(ex.stackTraceToString())
                }
                call.respondText(message, status = HttpStatusCode.InternalServerError)

                if (conn == null) {
                    call.respondText(reconnectErrorText, status = HttpStatusCode.InternalServerError)
                    logger.error(reconnectErrorText)
                    reconnect()
                }
            } catch (ex: Exception) {
                message = createMessage(ex.javaClass.canonicalName, exceptionErrorText, defaultUserAction)
                logger.error(message)

                if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                    logger.debug(ex.stackTraceToString())
                }
                call.respondText(message, status = HttpStatusCode.InternalServerError)

                if (conn == null) {
                    call.respondText(reconnectErrorText, status = HttpStatusCode.InternalServerError)
                    logger.error(reconnectErrorText)
                    reconnect()
                }
            }
        }
    }
}

fun Application.registerSMSCRoutes() {
    if (System.getenv("LOGLEVEL").toUpperCase() == "INFO") {
        logger.info("Registers SMSC Routes")
    }
    routing {
        smscRouting()
    }
}