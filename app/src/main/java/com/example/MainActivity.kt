package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.OrderDetailDialog
import com.example.ui.components.PrintDocumentDialog
import com.example.ui.components.ShopNameDialog
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewOrderScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TailorNavy
import com.example.ui.theme.TailorTapeGold
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.TailorViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TailorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TailorApp(viewModel: TailorViewModel = viewModel()) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val shopName by viewModel.shopName.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()

    val filteredOrders by viewModel.filteredOrders.collectAsStateWithLifecycle()
    val orderSearchQuery by viewModel.orderSearchQuery.collectAsStateWithLifecycle()
    val orderStatusFilter by viewModel.orderStatusFilter.collectAsStateWithLifecycle()

    val filteredCustomers by viewModel.filteredCustomers.collectAsStateWithLifecycle()
    val customerSearchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()

    val selectedOrderDetail by viewModel.selectedOrderDetail.collectAsStateWithLifecycle()
    val printDocOrder by viewModel.printDocOrder.collectAsStateWithLifecycle()
    val printDocType by viewModel.printDocType.collectAsStateWithLifecycle()
    val showShopNameDialog by viewModel.showShopNameDialog.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    // Handle system Back button: if not on Home tab, return to Home; or if detail is open, close it
    BackHandler(enabled = selectedOrderDetail != null || currentTab != AppTab.HOME) {
        if (selectedOrderDetail != null) {
            viewModel.selectOrderDetail(null)
        } else if (currentTab != AppTab.HOME) {
            viewModel.setTab(AppTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Small tailor emblem icon
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TailorTapeGold)
                                    .border(1.dp, Color(0xFFD49A1B), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.tailor_icon_1790767451916),
                                    contentDescription = "Shop Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = shopName,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Tailor Shop Management · درزی کا نظام",
                                    fontSize = 11.sp,
                                    color = TailorTapeGold
                                )
                            }
                        }
                    },
                    actions = {
                        // Rename shop button (✎)
                        IconButton(
                            onClick = { viewModel.setShowShopNameDialog(true) },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .testTag("btn_rename_shop")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename Shop",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TailorNavy,
                        titleContentColor = Color.White
                    )
                )

                // Measuring tape decorative gold bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(TailorTapeGold)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 6.dp
            ) {
                NavTabItem(
                    label = "Home · ہوم",
                    icon = Icons.Default.Home,
                    selected = currentTab == AppTab.HOME,
                    onClick = { viewModel.setTab(AppTab.HOME) },
                    testTag = "nav_tab_home"
                )

                NavTabItem(
                    label = "Orders · آرڈر",
                    icon = Icons.Default.ReceiptLong,
                    selected = currentTab == AppTab.ORDERS,
                    onClick = { viewModel.setTab(AppTab.ORDERS) },
                    testTag = "nav_tab_orders"
                )

                NavTabItem(
                    label = "New · نیا",
                    icon = Icons.Default.Add,
                    selected = currentTab == AppTab.NEW_ORDER,
                    onClick = { viewModel.setTab(AppTab.NEW_ORDER) },
                    testTag = "nav_tab_new"
                )

                NavTabItem(
                    label = "Customers · گاہک",
                    icon = Icons.Default.Group,
                    selected = currentTab == AppTab.CUSTOMERS,
                    onClick = { viewModel.setTab(AppTab.CUSTOMERS) },
                    testTag = "nav_tab_customers"
                )
            }
        },
        floatingActionButton = {
            if (currentTab == AppTab.ORDERS) {
                FloatingActionButton(
                    onClick = { viewModel.setTab(AppTab.NEW_ORDER) },
                    containerColor = TailorNavy,
                    contentColor = TailorTapeGold,
                    modifier = Modifier.testTag("fab_new_order")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Order")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        stats = stats,
                        onOrderClick = { viewModel.selectOrderDetail(it) },
                        onNavigateTab = { viewModel.setTab(it) }
                    )
                }

                AppTab.ORDERS -> {
                    OrdersScreen(
                        orders = filteredOrders,
                        searchQuery = orderSearchQuery,
                        onSearchQueryChange = { viewModel.setOrderSearchQuery(it) },
                        statusFilter = orderStatusFilter,
                        onStatusFilterChange = { viewModel.setOrderStatusFilter(it) },
                        onOrderClick = { viewModel.selectOrderDetail(it) }
                    )
                }

                AppTab.NEW_ORDER -> {
                    NewOrderScreen(
                        formState = formState,
                        onUpdateForm = { viewModel.updateFormField(it) },
                        onSaveOrder = {
                            viewModel.saveCurrentOrder {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Order saved successfully · آرڈر محفوظ ہو گیا"
                                    )
                                }
                            }
                        }
                    )
                }

                AppTab.CUSTOMERS -> {
                    CustomersScreen(
                        customers = filteredCustomers,
                        searchQuery = customerSearchQuery,
                        onSearchQueryChange = { viewModel.setCustomerSearchQuery(it) },
                        onOrderClick = { viewModel.selectOrderDetail(it) }
                    )
                }
            }
        }
    }

    // Order Detail Modal / Sheet
    selectedOrderDetail?.let { orderWithCustomer ->
        OrderDetailDialog(
            orderWithCustomer = orderWithCustomer,
            onDismiss = { viewModel.selectOrderDetail(null) },
            onStatusChange = { orderId, newStatus ->
                viewModel.updateOrderStatus(orderId, newStatus)
            },
            onReceivePayment = { orderId, amount ->
                viewModel.receivePayment(orderId, amount)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Payment received: Rs. ${amount.toInt()} · رقم وصول ہو گئی")
                }
            },
            onEditOrder = { viewModel.editOrder(it) },
            onDeleteOrder = { orderId ->
                viewModel.deleteOrder(orderId)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Order deleted · آرڈر حذف کر دیا گیا")
                }
            },
            onPrintDoc = { item, type ->
                viewModel.openPrintDoc(item, type)
            }
        )
    }

    // Thermal Slip / Printable Dialog (Receipt, Order Slip, Karigar Sheet)
    printDocOrder?.let { orderWithCustomer ->
        PrintDocumentDialog(
            orderWithCustomer = orderWithCustomer,
            docType = printDocType,
            shopName = shopName,
            onDismiss = { viewModel.closePrintDoc() }
        )
    }

    // Rename Shop Dialog
    if (showShopNameDialog) {
        ShopNameDialog(
            currentName = shopName,
            onConfirm = { newName ->
                viewModel.updateShopName(newName)
            },
            onDismiss = {
                viewModel.setShowShopNameDialog(false)
            }
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavTabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = TailorNavy,
            selectedTextColor = TailorNavy,
            indicatorColor = TailorTapeGold.copy(alpha = 0.35f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag(testTag)
    )
}
