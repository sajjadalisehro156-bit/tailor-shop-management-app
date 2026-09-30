package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.BilingualLabel
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.TailorNavy
import com.example.ui.theme.TailorTapeGold
import com.example.ui.viewmodel.OrderFormState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun NewOrderScreen(
    formState: OrderFormState,
    onUpdateForm: (OrderFormState.() -> OrderFormState) -> Unit,
    onSaveOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val popularDresses = listOf(
        "Shalwar Kameez · شلوار قمیض",
        "Kurta Pajama · کرتہ پاجامہ",
        "Waistcoat / Waskat · واسکٹ",
        "Pant Shirt · پینٹ شرٹ",
        "Sherwani · شیروانی",
        "Safari Suit · سفاری سوٹ",
        "Pent Coat · پینٹ کوٹ"
    )

    fun showDatePicker(initialDate: String, onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsed = sdf.parse(initialDate)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}

        val picker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selected)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        picker.show()
    }

    val total = formState.totalAmount.toDoubleOrNull() ?: 0.0
    val advance = formState.advanceAmount.toDoubleOrNull() ?: 0.0
    val balance = maxOf(0.0, total - advance)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (formState.editingOrderId != null) "Edit Order #${formState.orderNo}" else "New Order #${formState.orderNo}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (formState.editingOrderId != null) "آرڈر میں تبدیلی کریں" else "نیا آرڈر اور ناپ درج کریں",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TailorTapeGold.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Order #${formState.orderNo}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TailorNavy
                )
            }
        }

        // Error Banner
        formState.errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusOverdue.copy(alpha = 0.12f))
                    .border(1.dp, StatusOverdue, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = error,
                    color = StatusOverdue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // 1. Customer Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BilingualLabel("Customer Details", "گاہک کی معلومات")

                OutlinedTextField(
                    value = formState.customerName,
                    onValueChange = { onUpdateForm { copy(customerName = it) } },
                    label = { Text("Customer Name · گاہک کا نام *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name"),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formState.customerPhone,
                        onValueChange = { onUpdateForm { copy(customerPhone = it) } },
                        label = { Text("Phone · فون") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_customer_phone"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = formState.customerAddress,
                        onValueChange = { onUpdateForm { copy(customerAddress = it) } },
                        label = { Text("Address · پتہ") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // 2. Garment & Dates Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BilingualLabel("Garment & Scheduling", "سوٹ کی قسم اور تاریخ")

                OutlinedTextField(
                    value = formState.dressType,
                    onValueChange = { onUpdateForm { copy(dressType = it) } },
                    label = { Text("Dress Type · سوٹ کی قسم *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dress_type"),
                    shape = RoundedCornerShape(8.dp)
                )

                // Quick Dress suggestions chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularDresses.forEach { dress ->
                        SuggestionChip(
                            onClick = { onUpdateForm { copy(dressType = dress) } },
                            label = { Text(dress, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (formState.dressType == dress) TailorTapeGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = formState.karigar,
                    onValueChange = { onUpdateForm { copy(karigar = it) } },
                    label = { Text("Karigar / Craftsman · کاریگر کا نام") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Order Date
                    OutlinedTextField(
                        value = formState.orderDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Order Date · آرڈر تاریخ") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pick Date",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        showDatePicker(formState.orderDate) {
                                            onUpdateForm { copy(orderDate = it) }
                                        }
                                    }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showDatePicker(formState.orderDate) {
                                    onUpdateForm { copy(orderDate = it) }
                                }
                            },
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Delivery Date
                    OutlinedTextField(
                        value = formState.deliveryDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Delivery · ڈیلیوری تاریخ *") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pick Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        showDatePicker(formState.deliveryDate) {
                                            onUpdateForm { copy(deliveryDate = it) }
                                        }
                                    }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showDatePicker(formState.deliveryDate) {
                                    onUpdateForm { copy(deliveryDate = it) }
                                }
                            },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // 3. Billing & Pricing Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BilingualLabel("Payment & Billing", "رقم اور ایڈوانس")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formState.totalAmount,
                        onValueChange = { onUpdateForm { copy(totalAmount = it) } },
                        label = { Text("Total (Rs.) · کل رقم") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_total_amount"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = formState.advanceAmount,
                        onValueChange = { onUpdateForm { copy(advanceAmount = it) } },
                        label = { Text("Advance (Rs.) · ایڈوانس") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_advance_amount"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Balance summary strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (balance > 0) TailorNavy.copy(alpha = 0.08f) else Color(0xFF2F7A4F).copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remaining Balance · باقی رقم:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Rs. ${balance.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (balance > 0) StatusOverdue else Color(0xFF2F7A4F)
                    )
                }
            }
        }

        // 4. Measurements Card (ناپ)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BilingualLabel("Measurements (Inches)", "ناپ (انچ میں)")

                // Length & Shoulder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MeasurementField(
                        label = "Length · لمبائی",
                        value = formState.length,
                        onValueChange = { onUpdateForm { copy(length = it) } },
                        modifier = Modifier.weight(1f)
                    )
                    MeasurementField(
                        label = "Shoulder · کندھا",
                        value = formState.shoulder,
                        onValueChange = { onUpdateForm { copy(shoulder = it) } },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Chest & Waist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MeasurementField(
                        label = "Chest · چھاتی",
                        value = formState.chest,
                        onValueChange = { onUpdateForm { copy(chest = it) } },
                        modifier = Modifier.weight(1f)
                    )
                    MeasurementField(
                        label = "Waist · کمر",
                        value = formState.waist,
                        onValueChange = { onUpdateForm { copy(waist = it) } },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sleeve & Shalwar/Pant
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MeasurementField(
                        label = "Sleeve · آستین",
                        value = formState.sleeve,
                        onValueChange = { onUpdateForm { copy(sleeve = it) } },
                        modifier = Modifier.weight(1f)
                    )
                    MeasurementField(
                        label = "Shalwar / Pant · شلوار",
                        value = formState.shalwarLength,
                        onValueChange = { onUpdateForm { copy(shalwarLength = it) } },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Collar & Daman
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MeasurementField(
                        label = "Collar / Ban · کالر / بین",
                        value = formState.neckCollar,
                        onValueChange = { onUpdateForm { copy(neckCollar = it) } },
                        isNumeric = false,
                        modifier = Modifier.weight(1f)
                    )
                    MeasurementField(
                        label = "Daman · دامن (گول/چورس)",
                        value = formState.daman,
                        onValueChange = { onUpdateForm { copy(daman = it) } },
                        isNumeric = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Extra notes
                OutlinedTextField(
                    value = formState.extraNotes,
                    onValueChange = { onUpdateForm { copy(extraNotes = it) } },
                    label = { Text("Custom notes / Style instructions · دیگر ناپ و ہدایات") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Save Button
        Button(
            onClick = onSaveOrder,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_order_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = TailorNavy
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = TailorTapeGold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (formState.editingOrderId != null) "Update Order · آرڈر محفوظ کریں" else "Save Order · آرڈر محفوظ کریں",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun MeasurementField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isNumeric: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        keyboardOptions = if (isNumeric) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}
