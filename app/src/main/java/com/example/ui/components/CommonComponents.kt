package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderWithCustomer
import com.example.ui.theme.StatusDelivered
import com.example.ui.theme.StatusDeliveredBg
import com.example.ui.theme.StatusNew
import com.example.ui.theme.StatusNewBg
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusReady
import com.example.ui.theme.StatusReadyBg
import com.example.ui.theme.StatusStitching
import com.example.ui.theme.StatusStitchingBg
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "Rs. ${formatter.format(amount.toInt())}"
}

fun isDateOverdue(deliveryDate: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val delivery = sdf.parse(deliveryDate) ?: return false
        val today = sdf.parse(sdf.format(Date())) ?: return false
        delivery.before(today)
    } catch (e: Exception) {
        false
    }
}

@Composable
fun BilingualLabel(
    english: String,
    urdu: String,
    modifier: Modifier = Modifier,
    isUrduSmall: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = english,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = urdu,
            fontStyle = FontStyle.Italic,
            fontSize = if (isUrduSmall) 12.sp else 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (label, urduLabel, textColor, bgColor) = when (status) {
        Order.STATUS_SEW -> Quad("Stitching", "سلائی جاری", StatusStitching, StatusStitchingBg)
        Order.STATUS_READY -> Quad("Ready", "تیار", Color(0xFF523B00), StatusReadyBg)
        Order.STATUS_DONE -> Quad("Delivered", "مکمل", StatusDelivered, StatusDeliveredBg)
        else -> Quad("New", "نیا", StatusNew, StatusNewBg)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = urduLabel,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.85f)
            )
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Composable
fun OrderCard(
    orderWithCustomer: OrderWithCustomer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val o = orderWithCustomer.order
    val c = orderWithCustomer.customer
    val overdue = o.status != Order.STATUS_DONE && isDateOverdue(o.deliveryDate)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("order_card_${o.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (overdue) StatusOverdue.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${o.orderNo}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = " · ${c?.name ?: "Customer"}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = o.dressType,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (overdue) StatusOverdue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (overdue) "${o.deliveryDate} (Overdue · تاخیر)" else o.deliveryDate,
                        fontSize = 12.sp,
                        fontWeight = if (overdue) FontWeight.Bold else FontWeight.Normal,
                        color = if (overdue) StatusOverdue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = o.status)
                Spacer(modifier = Modifier.height(6.dp))
                if (o.isPaid) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusDelivered,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Paid · ادا",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusDelivered
                        )
                    }
                } else {
                    Text(
                        text = "Due: ${formatCurrency(o.remainingAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (o.advanceAmount > 0) MaterialTheme.colorScheme.primary else StatusOverdue
                    )
                }
            }
        }
    }
}
