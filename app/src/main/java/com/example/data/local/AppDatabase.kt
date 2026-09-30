package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Customer
import com.example.data.model.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [Customer::class, Order::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tailorDao(): TailorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tailor_manager.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedInitialData(database.tailorDao())
                    }
                }
            }

            private suspend fun seedInitialData(dao: TailorDao) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cal = Calendar.getInstance()
                val today = dateFormat.format(cal.time)

                cal.add(Calendar.DAY_OF_YEAR, 2)
                val inTwoDays = dateFormat.format(cal.time)

                cal.add(Calendar.DAY_OF_YEAR, 3)
                val inFiveDays = dateFormat.format(cal.time)

                cal.add(Calendar.DAY_OF_YEAR, -6)
                val pastDate = dateFormat.format(cal.time)

                // Sample Customer 1
                val c1Id = dao.insertCustomer(
                    Customer(
                        name = "Muhammad Usman",
                        phone = "03001234567",
                        address = "Main Bazar, Shop 14, Lahore"
                    )
                )

                // Sample Customer 2
                val c2Id = dao.insertCustomer(
                    Customer(
                        name = "Haji Abdul Rasheed",
                        phone = "03219876543",
                        address = "Gulberg III, Street 5"
                    )
                )

                // Sample Customer 3
                val c3Id = dao.insertCustomer(
                    Customer(
                        name = "Tariq Mehmood",
                        phone = "03455551234",
                        address = "Model Town, Block B"
                    )
                )

                // Sample Orders
                dao.insertOrder(
                    Order(
                        orderNo = 101,
                        customerId = c1Id,
                        dressType = "Shalwar Kameez · شلوار قمیض",
                        karigar = "Master Aslam",
                        orderDate = today,
                        deliveryDate = inTwoDays,
                        totalAmount = 2500.0,
                        advanceAmount = 1000.0,
                        status = Order.STATUS_SEW,
                        length = "40.5",
                        shoulder = "18",
                        chest = "42",
                        waist = "38",
                        sleeve = "24",
                        shalwarLength = "39",
                        neckCollar = "15.5 Sherwani Collar",
                        daman = "Round · گول دامن",
                        cuff = "2.5 inch",
                        extraNotes = "Front pocket + hidden mobile pocket, soft collar"
                    )
                )

                dao.insertOrder(
                    Order(
                        orderNo = 102,
                        customerId = c2Id,
                        dressType = "Waistcoat / Waskat · واسکٹ",
                        karigar = "Rashid Bhai",
                        orderDate = today,
                        deliveryDate = inFiveDays,
                        totalAmount = 3500.0,
                        advanceAmount = 3500.0,
                        status = Order.STATUS_READY,
                        length = "28",
                        shoulder = "17.5",
                        chest = "44",
                        waist = "42",
                        sleeve = "",
                        shalwarLength = "",
                        neckCollar = "V-Neck Ban",
                        daman = "Straight · چورس",
                        cuff = "",
                        extraNotes = "Golden fancy buttons, dark blue raw silk fabric"
                    )
                )

                dao.insertOrder(
                    Order(
                        orderNo = 103,
                        customerId = c3Id,
                        dressType = "Kurta Pajama · کرتہ پاجامہ",
                        karigar = "Master Aslam",
                        orderDate = pastDate,
                        deliveryDate = today,
                        totalAmount = 2200.0,
                        advanceAmount = 500.0,
                        status = Order.STATUS_NEW,
                        length = "38",
                        shoulder = "17",
                        chest = "40",
                        waist = "36",
                        sleeve = "23.5",
                        shalwarLength = "38",
                        neckCollar = "Shirt Collar",
                        daman = "Straight",
                        cuff = "Normal",
                        extraNotes = "White cotton fabric, double stitch on sides"
                    )
                )
            }
        }
    }
}
