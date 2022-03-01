import com.fasterxml.jackson.databind.SerializationFeature
import connection.*
import routes.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    install(Routing) {
        connect()
    }
    if (System.getenv("LOGLEVEL").toUpperCase() == "INFO") {
        memoryInfo()
    }

    registerTransportRoutes()
    registerSMSCRoutes()
    registerTraceRoutes()
    registerCardownersRoutes()
}