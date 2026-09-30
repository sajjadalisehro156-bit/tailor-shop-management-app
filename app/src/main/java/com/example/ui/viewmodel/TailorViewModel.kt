package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CustomerWithOrders
import com.example.data.model.Order
import com.example.data.model.OrderWithCustomer
import com.example.data.repository.TailorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppTab {
    HOME,
    ORDERS,
    NEW_ORDER,
    CUSTOMERS
}

data class DashboardStats(
    val todayOrdersCount: Int = 0,
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val readyCount: Int = 0,
    val totalSales: Double = 0.0,
    val pendingPayments: Double = 0.0,
    val nextDeliveries: List<OrderWithCustomer> = emptyList()
)

data class OrderFormState(
    val editingOrderId: Long? = null,
    val orderNo: Int = 101,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val dressType: String = "Shalwar Kameez · شلوار قمیض",
    val karigar: String = "",
    val orderDate: String = "",
    val deliveryDate: String = "",
    val totalAmount: String = "",
    val advanceAmount: String = "",
    val length: String = "",
    val shoulder: String = "",
    val chest: String = "",
    val waist: String = "",
    val sleeve: String = "",
    val shalwarLength: String = "",
    val neckCollar: String = "",
    val daman: String = "",
    val cuff: String = "",
    val extraNotes: String = "",
    val errorMessage: String? = null
)

class TailorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TailorRepository
    val shopName: StateFlow<String>

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _orderSearchQuery = MutableStateFlow("")
    val orderSearchQuery: StateFlow<String> = _orderSearchQuery.asStateFlow()

    private val _orderStatusFilter = MutableStateFlow("all")
    val orderStatusFilter: StateFlow<String> = _orderStatusFilter.asStateFlow()

    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    private val _selectedOrderDetail = MutableStateFlow<OrderWithCustomer?>(null)
    val selectedOrderDetail: StateFlow<OrderWithCustomer?> = _selectedOrderDetail.asStateFlow()

    private val _printDocOrder = MutableStateFlow<OrderWithCustomer?>(null)
    val printDocOrder: StateFlow<OrderWithCustomer?> = _printDocOrder.asStateFlow()

    private val _printDocType = MutableStateFlow("r") // "r" = Receipt, "s" = Slip, "k" = Karigar
    val printDocType: StateFlow<String> = _printDocType.asStateFlow()

    private val _formState = MutableStateFlow(OrderFormState())
    val formState: StateFlow<OrderFormState> = _formState.asStateFlow()

    private val _showShopNameDialog = MutableStateFlow(false)
    val showShopNameDialog: StateFlow<Boolean> = _showShopNameDialog.asStateFlow()

    val rawOrders: StateFlow<List<OrderWithCustomer>>
    val rawCustomers: StateFlow<List<CustomerWithOrders>>

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = TailorRepository(db.tailorDao(), application)
        shopName = repository.shopNameFlow

        rawOrders = repository.allOrdersWithCustomer
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        rawCustomers = repository.allCustomersWithOrders
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        initNewForm()
    }

    // Filtered orders
    val filteredOrders: StateFlow<List<OrderWithCustomer>> = combine(
        rawOrders,
        _orderSearchQuery,
        _orderStatusFilter
    ) { orders, query, filter ->
        val q = query.trim().lowercase()
        orders.filter { item ->
            val statusMatches = filter == "all" || item.order.status == filter
            val queryMatches = q.isEmpty() ||
                item.order.orderNo.toString().contains(q) ||
                (item.customer?.name?.lowercase()?.contains(q) == true) ||
                (item.customer?.phone?.lowercase()?.contains(q) == true) ||
                item.order.dressType.lowercase().contains(q) ||
                item.order.karigar.lowercase().contains(q)

            statusMatches && queryMatches
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered customers
    val filteredCustomers: StateFlow<List<CustomerWithOrders>> = combine(
        rawCustomers,
        _customerSearchQuery
    ) { customers, query ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            customers
        } else {
            customers.filter { item ->
                item.customer.name.lowercase().contains(q) ||
                    item.customer.phone.lowercase().contains(q) ||
                    item.customer.address.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics
    val dashboardStats: StateFlow<DashboardStats> = rawOrders.combine(_currentTab) { orders, _ ->
        val todayStr = getCurrentDate()
        val todayOrders = orders.count { it.order.orderDate == todayStr }
        val pendingOrders = orders.filter { it.order.status != Order.STATUS_DONE }
        val readyOrders = orders.count { it.order.status == Order.STATUS_READY }
        val completedOrders = orders.count { it.order.status == Order.STATUS_DONE }
        val totalSales = orders.sumOf { it.order.totalAmount }
        val pendingPayments = pendingOrders.sumOf { it.order.remainingAmount }
        val nextDeliveries = pendingOrders
            .sortedBy { it.order.deliveryDate }
            .take(6)

        DashboardStats(
            todayOrdersCount = todayOrders,
            pendingCount = pendingOrders.size,
            completedCount = completedOrders,
            readyCount = readyOrders,
            totalSales = totalSales,
            pendingPayments = pendingPayments,
            nextDeliveries = nextDeliveries
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
        if (tab == AppTab.NEW_ORDER && _formState.value.editingOrderId == null) {
            initNewForm()
        }
    }

    fun setOrderSearchQuery(q: String) {
        _orderSearchQuery.value = q
    }

    fun setOrderStatusFilter(status: String) {
        _orderStatusFilter.value = status
    }

    fun setCustomerSearchQuery(q: String) {
        _customerSearchQuery.value = q
    }

    fun selectOrderDetail(orderWithCustomer: OrderWithCustomer?) {
        _selectedOrderDetail.value = orderWithCustomer
    }

    fun openPrintDoc(orderWithCustomer: OrderWithCustomer, docType: String) {
        _printDocOrder.value = orderWithCustomer
        _printDocType.value = docType
    }

    fun closePrintDoc() {
        _printDocOrder.value = null
    }

    fun setShowShopNameDialog(show: Boolean) {
        _showShopNameDialog.value = show
    }

    fun updateShopName(newName: String) {
        if (newName.isNotBlank()) {
            repository.updateShopName(newName.trim())
        }
        _showShopNameDialog.value = false
    }

    fun initNewForm() {
        viewModelScope.launch {
            val nextNo = repository.getNextOrderNumber()
            val todayStr = getCurrentDate()
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 3)
            val deliveryStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

            _formState.value = OrderFormState(
                editingOrderId = null,
                orderNo = nextNo,
                orderDate = todayStr,
                deliveryDate = deliveryStr,
                dressType = "Shalwar Kameez · شلوار قمیض"
            )
        }
    }

    fun editOrder(orderWithCustomer: OrderWithCustomer) {
        val o = orderWithCustomer.order
        val c = orderWithCustomer.customer
        _formState.value = OrderFormState(
            editingOrderId = o.id,
            orderNo = o.orderNo,
            customerName = c?.name ?: "",
            customerPhone = c?.phone ?: "",
            customerAddress = c?.address ?: "",
            dressType = o.dressType,
            karigar = o.karigar,
            orderDate = o.orderDate,
            deliveryDate = o.deliveryDate,
            totalAmount = if (o.totalAmount > 0) o.totalAmount.toInt().toString() else "",
            advanceAmount = if (o.advanceAmount > 0) o.advanceAmount.toInt().toString() else "",
            length = o.length,
            shoulder = o.shoulder,
            chest = o.chest,
            waist = o.waist,
            sleeve = o.sleeve,
            shalwarLength = o.shalwarLength,
            neckCollar = o.neckCollar,
            daman = o.daman,
            cuff = o.cuff,
            extraNotes = o.extraNotes
        )
        _selectedOrderDetail.value = null
        _currentTab.value = AppTab.NEW_ORDER
    }

    fun updateFormField(updater: OrderFormState.() -> OrderFormState) {
        _formState.value = _formState.value.updater().copy(errorMessage = null)
    }

    fun saveCurrentOrder(onSuccess: () -> Unit) {
        val s = _formState.value
        if (s.customerName.isBlank()) {
            _formState.value = s.copy(errorMessage = "Customer name is required · گاہک کا نام درج کریں")
            return
        }
        if (s.dressType.isBlank()) {
            _formState.value = s.copy(errorMessage = "Dress type is required · سوٹ کی قسم درج کریں")
            return
        }
        if (s.deliveryDate.isBlank()) {
            _formState.value = s.copy(errorMessage = "Delivery date is required · ڈیلیوری کی تاریخ منتخب کریں")
            return
        }

        viewModelScope.launch {
            val customerId = repository.findOrCreateCustomer(
                name = s.customerName,
                phone = s.customerPhone,
                address = s.customerAddress
            )

            val total = s.totalAmount.toDoubleOrNull() ?: 0.0
            val advance = s.advanceAmount.toDoubleOrNull() ?: 0.0

            val order = Order(
                id = s.editingOrderId ?: 0L,
                orderNo = s.orderNo,
                customerId = customerId,
                dressType = s.dressType.trim(),
                karigar = s.karigar.trim(),
                orderDate = s.orderDate.ifBlank { getCurrentDate() },
                deliveryDate = s.deliveryDate,
                totalAmount = total,
                advanceAmount = advance,
                status = if (s.editingOrderId != null) {
                    val existing = repository.getOrderById(s.editingOrderId)
                    existing?.order?.status ?: Order.STATUS_NEW
                } else {
                    Order.STATUS_NEW
                },
                length = s.length.trim(),
                shoulder = s.shoulder.trim(),
                chest = s.chest.trim(),
                waist = s.waist.trim(),
                sleeve = s.sleeve.trim(),
                shalwarLength = s.shalwarLength.trim(),
                neckCollar = s.neckCollar.trim(),
                daman = s.daman.trim(),
                cuff = s.cuff.trim(),
                extraNotes = s.extraNotes.trim()
            )

            repository.saveOrder(order)
            initNewForm()
            _currentTab.value = AppTab.ORDERS
            onSuccess()
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            val updated = repository.getOrderById(orderId)
            _selectedOrderDetail.value = updated
        }
    }

    fun receivePayment(orderId: Long, receivedAmount: Double) {
        viewModelScope.launch {
            val orderWithCust = repository.getOrderById(orderId) ?: return@launch
            val newAdvance = (orderWithCust.order.advanceAmount + receivedAmount)
                .coerceAtMost(orderWithCust.order.totalAmount)
            repository.updateOrderAdvance(orderId, newAdvance)
            _selectedOrderDetail.value = repository.getOrderById(orderId)
        }
    }

    fun deleteOrder(orderId: Long) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
            _selectedOrderDetail.value = null
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
