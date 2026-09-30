package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.TailorDao
import com.example.data.model.Customer
import com.example.data.model.CustomerWithOrders
import com.example.data.model.Order
import com.example.data.model.OrderWithCustomer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TailorRepository(
    private val dao: TailorDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tailor_prefs", Context.MODE_PRIVATE)

    private val _shopNameFlow = MutableStateFlow(
        prefs.getString("shop_name", "Tailor Manager · ٹیلر منیجر") ?: "Tailor Manager · ٹیلر منیجر"
    )
    val shopNameFlow = _shopNameFlow.asStateFlow()

    fun updateShopName(newName: String) {
        prefs.edit().putString("shop_name", newName).apply()
        _shopNameFlow.value = newName
    }

    // Orders Flow
    val allOrdersWithCustomer: Flow<List<OrderWithCustomer>> =
        dao.getAllOrdersWithCustomerFlow()

    // Customers Flow
    val allCustomersWithOrders: Flow<List<CustomerWithOrders>> =
        dao.getAllCustomersWithOrdersFlow()

    suspend fun getNextOrderNumber(): Int {
        val max = dao.getMaxOrderNumber() ?: 100
        return max + 1
    }

    suspend fun getOrderById(id: Long): OrderWithCustomer? =
        dao.getOrderWithCustomerById(id)

    suspend fun findOrCreateCustomer(name: String, phone: String, address: String): Long {
        val trimmedPhone = phone.trim()
        val trimmedName = name.trim()

        val existing = if (trimmedPhone.isNotEmpty()) {
            dao.findCustomerByPhone(trimmedPhone)
        } else {
            dao.findCustomerByName(trimmedName)
        }

        return if (existing != null) {
            // Update customer info if changed
            dao.updateCustomer(
                existing.copy(
                    name = trimmedName.ifEmpty { existing.name },
                    phone = trimmedPhone.ifEmpty { existing.phone },
                    address = address.trim().ifEmpty { existing.address }
                )
            )
            existing.id
        } else {
            dao.insertCustomer(
                Customer(
                    name = trimmedName,
                    phone = trimmedPhone,
                    address = address.trim()
                )
            )
        }
    }

    suspend fun saveOrder(order: Order): Long {
        return if (order.id == 0L) {
            dao.insertOrder(order)
        } else {
            dao.updateOrder(order)
            order.id
        }
    }

    suspend fun updateOrderStatus(orderId: Long, status: String) {
        dao.updateOrderStatus(orderId, status)
    }

    suspend fun updateOrderAdvance(orderId: Long, advance: Double) {
        dao.updateOrderAdvance(orderId, advance)
    }

    suspend fun deleteOrder(orderId: Long) {
        dao.deleteOrderById(orderId)
    }

    suspend fun updateCustomer(customer: Customer) {
        dao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        dao.deleteCustomer(customer)
    }
}
