package connection

import io.ktor.application.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.*
import java.text.NumberFormat
import kotlin.system.exitProcess

public var conn: Connection? = null
public var logger: Logger = LoggerFactory.getLogger(Application::class.java)
public val dbDriver: String = System.getenv("DB_DRIVER")
public val dbConnectionURL: String = System.getenv("DB_CONN_URL")
public val dbUsername: String = System.getenv("DB_USERNAME")
public val dbPassword: String = System.getenv("DB_PASSWORD")

public const val defaultUserAction =
    "The exact reason for the problem cannot be found, please contact the system administrator"
public const val badRequestError = "BAD_REQUEST_TO_DATABASE"

public const val emptyDBError = "DATABASE_EMPTY_ERROR"
public const val emptyDBErrorText = "No default config value found. The database is empty and cannot be updated"

public const val dbErrorText = "Database error"
public const val dbErrorUserAction =
    "Please check if the application can reach the database and is not blocked by any firewall. Also check if the database URL and login credentials are correct"

public const val notUpdatedCorrectlyError = "DATABASE_NOT_UPDATED_ERROR"
public const val notUpdatedCorrectlyErrorText = "Config not updated correctly. Malformed json"

public const val incorrectInsertTransportErrorText =
    "Client is trying to insert incorrect ISO 8601 duration at jobValidityDuration, smHttpTrigValidityDuration or smOtaValidityDuration"
public const val incorrectInsertTransportErrorUserAction = "Check if the values in the mentioned fields are correct"

public const val modDateErrorText = "Not allowed to put modificationDate"
public const val modDateErrorUserAction = "Not allowed to make a PUT with modificationDate. Try again after removing it"

public const val serializingErrorText = "Something went wrong when serializing JSON Object"

public const val jsonValuesErrorText = "Some crucial/important JSON values are missing"
public const val jsonValuesErrorUserAction = "Check the PUT payload and that it contains all the needed values"

public const val unknownValuesErrorText = "Not allowed to put unknown values"
public const val unknownValuesErrorUserAction = "Check the PUT payload and that it does not contain unknown values"

public const val invalidJsonFormatErrorText =
    "Not allowed to put a invalid format in JSON data. You have to put values with correct format correctly"
public const val invalidJsonFormatErrorUserAction = "Check the format of the PUT payload"

public const val exceptionErrorText = "Something went wrong"
public const val reconnectErrorText = "Trying to reconnect to the database..."

public const val updatedCorrectlyText = "Config updated correctly"
public const val receivingData = "Receiving data from database"
public const val insertingDefaultText = "Inserting default values"
public const val connectionEstablishedText = "Database connection established"

private var message = ""


fun connection() {
    try {
        Class.forName(dbDriver)
        conn = DriverManager.getConnection(dbConnectionURL, dbUsername, dbPassword)

        if (System.getenv("LOGLEVEL").toUpperCase() == "INFO") {
            logger.info(connectionEstablishedText)
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

fun connect() {
    connection()
}

fun reconnect() {
    val startTime: Long = System.currentTimeMillis()
    var elapsedTime = 0L
    while (elapsedTime < 5 * 60 * 1000) {
        elapsedTime = System.currentTimeMillis() - startTime
        connection()
        if (conn!!.isClosed) {
            return
        }
    }
    exitProcess(1)
}

fun memoryInfo() {
    val runtime: Runtime = Runtime.getRuntime()
    val format: NumberFormat = NumberFormat.getInstance()
    val maxMemory = runtime.maxMemory()
    val allocatedMemory = runtime.totalMemory()
    val freeMemory = runtime.freeMemory()
    val mb = 1024 * 1024
    val mega = " MB "

    logger.info("========================== Memory Info ==========================")
    logger.info("Free memory: " + format.format(freeMemory / mb) + mega)
    logger.info("Allocated memory: " + format.format(allocatedMemory / mb) + mega)
    logger.info("Max memory: " + format.format(maxMemory / mb) + mega)
    logger.info("Total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / mb) + mega)
    logger.info("=================================================================\n")
    logger.info("Starting the service")
}

fun createMessage(errorCode: String, errorMessage: String, userAction: String): String {
    return "Error code: $errorCode\nError message: $errorMessage\nUser action: $userAction"
}