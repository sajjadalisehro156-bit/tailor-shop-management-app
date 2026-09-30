package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class OrderWithCustomer(
    @Embedded val order: Order,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer?
)

data class CustomerWithOrders(
    @Embedded val customer: Customer,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val orders: List<Order>
)
