package routes

import connection.*
import database.*
import models.Trace
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

private const val traceID = 3
private var message = ""
public const val insertTraceDefaultValueQuery = "INSERT INTO J_TRANSPORT VALUES (3, '{\n" +
        "  \"user\": \"Alex\",\n" +
        "  \"newValue\": {\n" +
        "    \"traceRateLimitPerContainerPerMinute\": \"10\",\n" +
        "    \"cardsToTrace\": {\n" +
        "      \"iccidList\": [],\n" +
        "      \"eidList\": []\n" +
        "    }\n" +
        "  }\n" +
        "}')"

fun Route.traceRouting() {
    checkDefaultValue(traceID)

    route("/api/v1/businessconfig/trace") {
        get(traceID)
        put {
            try {
                val fil = call.receive<Trace>()
                if (fil.newValue.modificationDate.isEmpty()) {
                    fil.newValue.modificationDate =
                        ZonedDateTime.now(ZoneId.of("Europe/Stockholm")).truncatedTo(ChronoUnit.SECONDS)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                    val stmt: Statement = conn!!.createStatement()
                    val rs: ResultSet = stmt.executeQuery(selectQuery + traceID)
                    if (!rs.next()) {
                        message = createMessage(emptyDBError, emptyDBErrorText, defaultUserAction)
                        if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                            logger.debug(message)
                        }
                        call.respondText(message, status = HttpStatusCode.NotFound)
                    } else {
                        val test = Gson()
                        val output: String = test.toJson(fil)
                        val statement: PreparedStatement = conn!!.prepareStatement(updateQuery + traceID)
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
                } else {
                    message = createMessage(badRequestError, modDateErrorText, modDateErrorUserAction)

                    if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                        logger.debug(message)
                    }
                    call.respondText(message, status = HttpStatusCode.BadRequest)
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

fun Application.registerTraceRoutes() {
    if (System.getenv("LOGLEVEL").toUpperCase() == "INFO") {
        logger.info("Registers Trace Routes")
    }
    routing {
        traceRouting()
    }
}