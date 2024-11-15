package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import models.Tender
import java.sql.Connection
import java.sql.Statement


@Serializable
data class Delivery(
    val deliveryId: String,
    val tenderId: Int,
    val startDate: String,
    val endDate: String
)

@Serializable
data class Cargo(
    val cargoId: String,
    val deliveryId: Int,
    val cargoType: String,
    val nettWeight: Double
)

class DeliveryService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_DELIVERIES =
            """
            CREATE TABLE IF NOT EXISTS deliveries (
                id SERIAL PRIMARY KEY,
                delivery_id VARCHAR(255) NOT NULL,
                tender_id INT NOT NULL REFERENCES tenders(id) ON DELETE CASCADE,
                start_date TIMESTAMP NOT NULL,
                end_date TIMESTAMP NOT NULL
            );
            """
        private const val INSERT_DELIVERY =
            "INSERT INTO deliveries (delivery_id, tender_id, start_date, end_date) VALUES (?, ?, ?, ?)"
        private const val SELECT_DELIVERIES_BY_TENDER =
            "SELECT * FROM deliveries WHERE tender_id = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_DELIVERIES)
    }

    suspend fun create(delivery: Delivery): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_DELIVERY, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, delivery.deliveryId)
        statement.setInt(2, delivery.tenderId)
        statement.setString(3, delivery.startDate)
        statement.setString(4, delivery.endDate)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted delivery")
        }
    }

    suspend fun getDeliveriesByTender(tenderId: Int): List<Delivery> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_DELIVERIES_BY_TENDER)
        statement.setInt(1, tenderId)
        val resultSet = statement.executeQuery()

        val deliveries = mutableListOf<Delivery>()
        while (resultSet.next()) {
            deliveries.add(
                Delivery(
                    deliveryId = resultSet.getString("delivery_id"),
                    tenderId = resultSet.getInt("tender_id"),
                    startDate = resultSet.getTimestamp("start_date").toString(),
                    endDate = resultSet.getTimestamp("end_date").toString()
                )
            )
        }
        deliveries
    }
}

class CargoService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_CARGO =
            """
            CREATE TABLE IF NOT EXISTS cargo (
                id SERIAL PRIMARY KEY,
                cargo_id VARCHAR(255) NOT NULL,
                delivery_id INT NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
                cargo_type VARCHAR(255) NOT NULL,
                nett_weight DECIMAL(10, 2) NOT NULL
            );
            """
        private const val INSERT_CARGO =
            "INSERT INTO cargo (cargo_id, delivery_id, cargo_type, nett_weight) VALUES (?, ?, ?, ?)"
        private const val SELECT_CARGO_BY_DELIVERY =
            "SELECT * FROM cargo WHERE delivery_id = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_CARGO)
    }

    suspend fun create(cargo: Cargo): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_CARGO, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, cargo.cargoId)
        statement.setInt(2, cargo.deliveryId)
        statement.setString(3, cargo.cargoType)
        statement.setDouble(4, cargo.nettWeight)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted cargo")
        }
    }

    suspend fun getCargoByDelivery(deliveryId: Int): List<Cargo> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CARGO_BY_DELIVERY)
        statement.setInt(1, deliveryId)
        val resultSet = statement.executeQuery()

        val cargoList = mutableListOf<Cargo>()
        while (resultSet.next()) {
            cargoList.add(
                Cargo(
                    cargoId = resultSet.getString("cargo_id"),
                    deliveryId = resultSet.getInt("delivery_id"),
                    cargoType = resultSet.getString("cargo_type"),
                    nettWeight = resultSet.getDouble("nett_weight")
                )
            )
        }
        cargoList
    }
}
class TenderService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_TENDERS =
            """
            CREATE TABLE IF NOT EXISTS tenders (
                id SERIAL PRIMARY KEY,
                longterm_contract_id VARCHAR(255) NOT NULL,
                title VARCHAR(255) NOT NULL,
                description TEXT NOT NULL,
                start_date TIMESTAMP NOT NULL,
                end_date TIMESTAMP NOT NULL,
                employer_account_id INT NOT NULL,
                contractor_account_id INT NOT NULL,
                price DECIMAL(10, 2) NOT NULL,
                status VARCHAR(50) DEFAULT 'pending'
            );
            """
        private const val SELECT_TENDER_BY_ID = "SELECT * FROM tenders WHERE id = ?"
        private const val INSERT_TENDER =
            "INSERT INTO tenders (longterm_contract_id, title, description, start_date, end_date, employer_account_id, contractor_account_id, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        private const val UPDATE_TENDER =
            "UPDATE tenders SET longterm_contract_id = ?, title = ?, description = ?, start_date = ?, end_date = ?, employer_account_id = ?, contractor_account_id = ?, price = ? WHERE id = ?"
        private const val DELETE_TENDER = "DELETE FROM tenders WHERE id = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_TENDERS)
    }

    // Create a new tender
    suspend fun create(tender: Tender): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_TENDER, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, tender.longtermContractId)
        statement.setString(2, tender.title)
        statement.setString(3, tender.description)
        statement.setString(4, tender.startDate)
        statement.setString(5, tender.endDate)
        statement.setInt(6, tender.employerAccountId)
        statement.setInt(7, tender.contractorAccountId)
        statement.setDouble(8, tender.price)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted tender")
        }
    }

    // Read a tender by ID
    suspend fun read(id: Int): Tender = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_TENDER_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Tender(
                longtermContractId = resultSet.getString("longterm_contract_id"),
                title = resultSet.getString("title"),
                description = resultSet.getString("description"),
                startDate = resultSet.getTimestamp("start_date").toString(),
                endDate = resultSet.getTimestamp("end_date").toString(),
                employerAccountId = resultSet.getInt("employer_account_id"),
                contractorAccountId = resultSet.getInt("contractor_account_id"),
                price = resultSet.getDouble("price")
            )
        } else {
            throw Exception("Record not found")
        }
    }

    // Update a tender by ID
    suspend fun update(id: Int, tender: Tender) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_TENDER)
        statement.setString(1, tender.longtermContractId)
        statement.setString(2, tender.title)
        statement.setString(3, tender.description)
        statement.setString(4, tender.startDate)
        statement.setString(5, tender.endDate)
        statement.setInt(6, tender.employerAccountId)
        statement.setInt(7, tender.contractorAccountId)
        statement.setDouble(8, tender.price)
        statement.setInt(9, id)
        statement.executeUpdate()
    }

    // Delete a tender by ID
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_TENDER)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}