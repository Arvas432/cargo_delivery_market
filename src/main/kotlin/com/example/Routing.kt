package com.example

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.Tender

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hi")
        }
        route("/tenders") {
            post("/create") {
                val tender = call.receive<Tender>()

                call.respond(mapOf("message" to "Tender created", "tender" to tender))
            }

            delete("/delete/{id}") {
                val id = call.parameters["id"] ?: error("ID is required")
                // Логика удаления из БД
                call.respond(mapOf("message" to "Tender deleted", "id" to id))
            }

            post("/upload") {
                // Логика загрузки Excel
            }

            put("/update/{id}") {
                val id = call.parameters["id"] ?: error("ID is required")
                val tender = call.receive<Tender>()
                // Логика обновления в БД
                call.respond(mapOf("message" to "Tender updated", "id" to id))
            }

            post("/confirm/{id}") {
                val id = call.parameters["id"] ?: error("ID is required")
                // Логика подтверждения
                call.respond(mapOf("message" to "Tender confirmed", "id" to id))
            }
        }
    }
}
