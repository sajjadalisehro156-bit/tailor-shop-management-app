package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNo: Int,
    val customerId: Long,
    val dressType: String,
    val karigar: String = "",
    val orderDate: String,
    val deliveryDate: String,
    val totalAmount: Double = 0.0,
    val advanceAmount: Double = 0.0,
    val status: String = STATUS_NEW, // "new", "sew", "ready", "done"
    
    // Measurements (ناپ)
    val length: String = "",       // لمبائی
    val shoulder: String = "",     // کندھا
    val chest: String = "",        // چھاتی
    val waist: String = "",        // کمر
    val sleeve: String = "",       // آستین
    val shalwarLength: String = "", // شلوار / پینٹ لمبائی
    val neckCollar: String = "",   // بین / کالر
    val daman: String = "",        // دامن (گول / چورس)
    val cuff: String = "",         // کف
    val extraNotes: String = "",   // دیگر ناپ و ہدایات
    
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Double
        get() = maxOf(0.0, totalAmount - advanceAmount)

    val isPaid: Boolean
        get() = remainingAmount <= 0.0

    val paymentStatus: String
        get() = when {
            remainingAmount <= 0.0 -> "Paid · ادا"
            advanceAmount > 0.0 -> "Partial · کچھ ادا"
            else -> "Unpaid · باقی"
        }

    companion object {
        const val STATUS_NEW = "new"       // New · نیا
        const val STATUS_SEW = "sew"       // Stitching · سلائی جاری
        const val STATUS_READY = "ready"   // Ready · تیار
        const val STATUS_DONE = "done"     // Delivered · مکمل / ڈیلیور
    }
}
