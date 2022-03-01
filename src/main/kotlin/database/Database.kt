package database

import connection.*
import models.NewValueTransportFalse
import routes.*
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import com.google.gson.Gson

public const val selectQuery = "SELECT * FROM J_TRANSPORT WHERE ID = "
public const val selectNewValueQuery = "SELECT PO.PO_DOCUMENT.newValue FROM J_TRANSPORT PO WHERE ID = "
public const val updateQuery = "UPDATE J_TRANSPORT SET J_TRANSPORT.PO_DOCUMENT = ? WHERE ID = "

private var message = ""

fun checkDefaultValue(id: Int) {
    try {
        val stmt: Statement = conn!!.createStatement()
        val rs: ResultSet = stmt.executeQuery(selectQuery + id)
        if (!rs.next()) {
            when (id) {
                1 -> stmt.executeQuery(insertTransportDefaultValueQuery)
                2 -> stmt.executeQuery(insertSMSCDefaultValueQuery)
                3 -> stmt.executeQuery(insertTraceDefaultValueQuery)
                4 -> stmt.executeQuery(insertCardownersDefaultValueQuery)
            }
            if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                logger.debug(insertingDefaultText)
            }
        }
    } catch (ex: SQLException) {
        message = createMessage(ex.javaClass.canonicalName, dbErrorText, dbErrorUserAction)
        logger.error(message)

        if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
            logger.debug(ex.stackTraceToString())
        }

        if (conn == null) {
            logger.error(reconnectErrorText)
            reconnect()
        }
    } catch (ex: Exception) {
        message = createMessage(ex.javaClass.canonicalName, exceptionErrorText, defaultUserAction)
        logger.error(message)

        if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
            logger.debug(ex.stackTraceToString())
        }

        if (conn == null) {
            logger.error(reconnectErrorText)
            reconnect()
        }
    }
}

fun Route.get(id: Int) {
    get {
        try {
            val stmt: Statement = conn!!.createStatement()
            val rs: ResultSet = stmt.executeQuery(selectNewValueQuery + id)
            var blo: String? = null
            val arrayList = ArrayList<String>()
            while (rs.next()) {
                blo = rs.getString("newValue")
                if (id == 1) {
                    val convertedObject: NewValueTransportFalse =
                        Gson().fromJson(blo, NewValueTransportFalse::class.java)
                    if (!convertedObject.httpOta.smHttpTrigEnabled) {
                        blo = Gson().toJson(convertedObject)
                    }
                }
                arrayList.add(blo)
            }
            if (blo != null) {
                call.respond(arrayList)
                if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                    logger.debug(receivingData)
                }
            } else {
                message = createMessage(emptyDBError, emptyDBErrorText, defaultUserAction)

                if (System.getenv("LOGLEVEL").toUpperCase() == "DEBUG") {
                    logger.debug(message)
                }

                call.respondText(message, status = HttpStatusCode.NotFound)
            }
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