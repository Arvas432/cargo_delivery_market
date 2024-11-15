package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import kotlinx.coroutines.*
import models.Tender

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres(embedded = true)
    val tenderService = TenderService(dbConnection)
    val deliveryService = DeliveryService(dbConnection)
    val cargoService = CargoService(dbConnection)
    routing {
        // Tenders
        route("/tenders") {
            post("/create") {
                val tender = call.receive<Tender>()
                val id = tenderService.create(tender)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                try {
                    val tender = tenderService.read(id)
                    call.respond(HttpStatusCode.OK, tender)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound, "Tender not found")
                }
            }

            put("/update/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val tender = call.receive<Tender>()
                tenderService.update(id, tender)
                call.respond(HttpStatusCode.OK, "Tender updated")
            }

            delete("/delete/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                tenderService.delete(id)
                call.respond(HttpStatusCode.OK, "Tender deleted")
            }
        }

        // Deliveries
        route("/deliveries") {
            post("/create") {
                val delivery = call.receive<Delivery>()
                val id = deliveryService.create(delivery)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            }

            get("/by-tender/{tenderId}") {
                val tenderId = call.parameters["tenderId"]?.toInt() ?: throw IllegalArgumentException("Invalid tender ID")
                val deliveries = deliveryService.getDeliveriesByTender(tenderId)
                call.respond(HttpStatusCode.OK, deliveries)
            }
        }

        // Cargo
        route("/cargo") {
            post("/create") {
                val cargo = call.receive<Cargo>()
                val id = cargoService.create(cargo)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            }

            get("/by-delivery/{deliveryId}") {
                val deliveryId = call.parameters["deliveryId"]?.toInt() ?: throw IllegalArgumentException("Invalid delivery ID")
                val cargoList = cargoService.getCargoByDelivery(deliveryId)
                call.respond(HttpStatusCode.OK, cargoList)
            }
        }

    }
}

/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */
fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    if (embedded) {
        return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "root", "")
    } else {
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()
        return DriverManager.getConnection(url, user, password)
    }
}
