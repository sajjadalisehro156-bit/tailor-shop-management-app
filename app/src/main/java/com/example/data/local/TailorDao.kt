package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Customer
import com.example.data.model.CustomerWithOrders
import com.example.data.model.Order
import com.example.data.model.OrderWithCustomer
import kotlinx.coroutines.flow.Flow

@Dao
interface TailorDao {

    // Orders
    @Transaction
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllOrdersWithCustomerFlow(): Flow<List<OrderWithCustomer>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderWithCustomerById(id: Long): OrderWithCustomer?

    @Query("SELECT MAX(orderNo) FROM orders")
    suspend fun getMaxOrderNumber(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE orders SET advanceAmount = :advance WHERE id = :orderId")
    suspend fun updateOrderAdvance(orderId: Long, advance: Double)

    // Customers
    @Transaction
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersWithOrdersFlow(): Flow<List<CustomerWithOrders>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun findCustomerByPhone(phone: String): Customer?

    @Query("SELECT * FROM customers WHERE name = :name LIMIT 1")
    suspend fun findCustomerByName(name: String): Customer?

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomers(): List<Customer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)
}
