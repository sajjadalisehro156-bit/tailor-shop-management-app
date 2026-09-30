package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Order
import com.example.data.model.OrderWithCustomer
import com.example.ui.theme.StatusDelivered
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.TailorNavy
import com.example.ui.theme.TailorTapeGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderDetailDialog(
    orderWithCustomer: OrderWithCustomer,
    onDismiss: () -> Unit,
    onStatusChange: (Long, String) -> Unit,
    onReceivePayment: (Long, Double) -> Unit,
    onEditOrder: (OrderWithCustomer) -> Unit,
    onDeleteOrder: (Long) -> Unit,
    onPrintDoc: (OrderWithCustomer, String) -> Unit
) {
    val context = LocalContext.current
    val o = orderWithCustomer.order
    val c = orderWithCustomer.customer
    val scrollState = rememberScrollState()

    var showReceivePaymentDialog by remember { mutableStateOf(false) }
    var receiveAmountInput by remember { mutableStateOf(o.remainingAmount.toInt().toString()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with Order # and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "#${o.orderNo} · ${c?.name ?: "Customer"}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "آرڈر کی مکمل تفصیلات",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                // Customer Info Row
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DetailRow("Customer · گاہک", c?.name ?: "—")

                        if (!c?.phone.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Phone · فون",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${c?.phone}")
                                            }
                                            context.startActivity(intent)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        tint = TailorNavy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = c?.phone ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TailorNavy
                                    )
                                }
                            }
                        }

                        if (!c?.address.isNullOrBlank()) {
                            DetailRow("Address · پتہ", c?.address ?: "")
                        }
                    }
                }

                // Garment & Timing
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DetailRow("Dress · سوٹ کی قسم", o.dressType)
                        if (o.karigar.isNotBlank()) {
                            DetailRow("Karigar · کاریگر", o.karigar)
                        }
                        DetailRow("Order Date · آرڈر تاریخ", o.orderDate)
                        DetailRow("Delivery Date · ڈیلیوری", o.deliveryDate)
                    }
                }

                // Billing & Payments
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DetailRow("Total Amount · کل رقم", formatCurrency(o.totalAmount))
                        DetailRow("Advance Paid · ایڈوانس", formatCurrency(o.advanceAmount))
                        DetailRow(
                            label = "Remaining Due · باقی رقم",
                            value = formatCurrency(o.remainingAmount),
                            isHighlighted = o.remainingAmount > 0,
                            highlightColor = if (o.remainingAmount > 0) StatusOverdue else StatusDelivered
                        )
                        DetailRow("Status · حالت ادائیگی", o.paymentStatus)
                    }
                }

                // Measurements (ناپ)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Measurements · ناپ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        if (o.length.isNotBlank()) DetailRow("Length · لمبائی", "${o.length}\"")
                        if (o.shoulder.isNotBlank()) DetailRow("Shoulder · کندھا", "${o.shoulder}\"")
                        if (o.chest.isNotBlank()) DetailRow("Chest · چھاتی", "${o.chest}\"")
                        if (o.waist.isNotBlank()) DetailRow("Waist · کمر", "${o.waist}\"")
                        if (o.sleeve.isNotBlank()) DetailRow("Sleeve · آستین", "${o.sleeve}\"")
                        if (o.shalwarLength.isNotBlank()) DetailRow("Shalwar · شلوار", "${o.shalwarLength}\"")
                        if (o.neckCollar.isNotBlank()) DetailRow("Collar · کالر", o.neckCollar)
                        if (o.daman.isNotBlank()) DetailRow("Daman · دامن", o.daman)
                        if (o.cuff.isNotBlank()) DetailRow("Cuff · کف", o.cuff)
                        if (o.extraNotes.isNotBlank()) DetailRow("Notes · ہدایات", o.extraNotes)
                    }
                }

                // Job Status Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Job Status · کام کی حالت",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val statuses = listOf(
                        Pair(Order.STATUS_NEW, "New · نیا"),
                        Pair(Order.STATUS_SEW, "Stitching · سلائی"),
                        Pair(Order.STATUS_READY, "Ready · تیار"),
                        Pair(Order.STATUS_DONE, "Delivered · مکمل")
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statuses.forEach { (st, label) ->
                            val selected = o.status == st
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) TailorNavy else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) TailorTapeGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onStatusChange(o.id, st) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Receive Payment Button (if balance due)
                    if (o.remainingAmount > 0) {
                        Button(
                            onClick = {
                                receiveAmountInput = o.remainingAmount.toInt().toString()
                                showReceivePaymentDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F7A4F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_receive_payment")
                        ) {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Receive Payment · رقم وصول کریں (${formatCurrency(o.remainingAmount)})")
                        }
                    }

                    // Print / Share Document Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onPrintDoc(orderWithCustomer, "r") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🧾 Receipt\nرسید", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onPrintDoc(orderWithCustomer, "s") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🏷 Slip\nآرڈر سلپ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onPrintDoc(orderWithCustomer, "k") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("✂ Karigar\nکاریگر شیٹ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Edit & Delete row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onEditOrder(orderWithCustomer) },
                            colors = ButtonDefaults.buttonColors(containerColor = TailorNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit · تبدیلی")
                        }

                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusOverdue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Receive Payment Dialog
    if (showReceivePaymentDialog) {
        AlertDialog(
            onDismissRequest = { showReceivePaymentDialog = false },
            title = { Text("Receive Payment · رقم وصول", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total remaining balance is ${formatCurrency(o.remainingAmount)}.")
                    Text("کل باقی رقم: ${formatCurrency(o.remainingAmount)}")
                    OutlinedTextField(
                        value = receiveAmountInput,
                        onValueChange = { receiveAmountInput = it },
                        label = { Text("Amount Received (Rs.) · وصول رقم") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = receiveAmountInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onReceivePayment(o.id, amount)
                        }
                        showReceivePaymentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F7A4F))
                ) {
                    Text("Confirm · تصدیق")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReceivePaymentDialog = false }) {
                    Text("Cancel · منسوخ")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Order #${o.orderNo}?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this order? This action cannot be undone.\nکیا آپ واقعی یہ آرڈر حذف کرنا چاہتے ہیں؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteOrder(o.id)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOverdue)
                ) {
                    Text("Delete · حذف کریں")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel · منسوخ")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    highlightColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = if (isHighlighted) 14.sp else 13.sp,
            color = if (isHighlighted) highlightColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
