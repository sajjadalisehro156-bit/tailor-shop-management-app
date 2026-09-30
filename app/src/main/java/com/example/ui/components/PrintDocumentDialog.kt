package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OrderWithCustomer
import com.example.ui.theme.TailorNavy
import com.example.ui.theme.TailorTapeGold

@Composable
fun PrintDocumentDialog(
    orderWithCustomer: OrderWithCustomer,
    docType: String, // "r" = Receipt, "s" = Slip, "k" = Karigar
    shopName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val o = orderWithCustomer.order
    val c = orderWithCustomer.customer
    val scrollState = rememberScrollState()

    val title = when (docType) {
        "r" -> "RECEIPT · رسید"
        "s" -> "ORDER SLIP · آرڈر سلپ"
        else -> "KARIGAR SHEET · کاریگر شیٹ"
    }

    // Build plain text for sharing via WhatsApp/SMS
    fun generateShareText(): String {
        val sb = StringBuilder()
        sb.appendLine("✂ $shopName")
        sb.appendLine("========================")
        sb.appendLine(title)
        sb.appendLine("------------------------")
        sb.appendLine("Order #: ${o.orderNo}")
        sb.appendLine("Date: ${o.orderDate}")
        sb.appendLine("Customer: ${c?.name ?: "Customer"}")
        if (docType == "k") {
            sb.appendLine("Karigar: ${o.karigar.ifBlank { "Not assigned" }}")
        } else if (!c?.phone.isNullOrBlank()) {
            sb.appendLine("Phone: ${c?.phone}")
        }
        sb.appendLine("Dress: ${o.dressType}")
        sb.appendLine("Delivery: ${o.deliveryDate}")

        if (docType != "r") {
            sb.appendLine("------------------------")
            sb.appendLine("MEASUREMENTS / ناپ:")
            if (o.length.isNotBlank()) sb.appendLine("Length (لمبائی): ${o.length}\"")
            if (o.shoulder.isNotBlank()) sb.appendLine("Shoulder (کندھا): ${o.shoulder}\"")
            if (o.chest.isNotBlank()) sb.appendLine("Chest (چھاتی): ${o.chest}\"")
            if (o.waist.isNotBlank()) sb.appendLine("Waist (کمر): ${o.waist}\"")
            if (o.sleeve.isNotBlank()) sb.appendLine("Sleeve (آستین): ${o.sleeve}\"")
            if (o.shalwarLength.isNotBlank()) sb.appendLine("Shalwar/Pant: ${o.shalwarLength}\"")
            if (o.neckCollar.isNotBlank()) sb.appendLine("Collar (کالر): ${o.neckCollar}")
            if (o.daman.isNotBlank()) sb.appendLine("Daman (دامن): ${o.daman}")
            if (o.extraNotes.isNotBlank()) sb.appendLine("Notes: ${o.extraNotes}")
        }

        if (docType != "k") {
            sb.appendLine("------------------------")
            sb.appendLine("Total: ${formatCurrency(o.totalAmount)}")
            sb.appendLine("Advance: ${formatCurrency(o.advanceAmount)}")
            sb.appendLine("Remaining Due: ${formatCurrency(o.remainingAmount)}")
            sb.appendLine("Status: ${o.paymentStatus}")
        }

        sb.appendLine("========================")
        if (docType == "r") {
            sb.appendLine("Shukriya! Thank you for your order · شکریہ")
        }
        return sb.toString()
    }

    // Android PrintManager integration via simple HTML
    fun printDocument() {
        val webView = WebView(context)
        val htmlContent = """
            <html>
            <head>
                <style>
                    body { font-family: monospace; font-size: 13px; margin: 10px; color: #000; width: 280px; }
                    .center { text-align: center; }
                    .title { font-size: 15px; font-weight: bold; margin: 4px 0; }
                    .dashed { border-top: 1px dashed #000; margin: 6px 0; }
                    .row { display: flex; justify-content: space-between; margin: 3px 0; }
                    .bold { font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="center">
                    <div class="title">$shopName</div>
                    <div>$title</div>
                </div>
                <div class="dashed"></div>
                <div class="row"><span>Order #:</span><span class="bold">#${o.orderNo}</span></div>
                <div class="row"><span>Date:</span><span>${o.orderDate}</span></div>
                <div class="row"><span>Customer:</span><span class="bold">${c?.name ?: ""}</span></div>
                ${if (docType == "k") "<div class='row'><span>Karigar:</span><span>${o.karigar}</span></div>" else "<div class='row'><span>Phone:</span><span>${c?.phone ?: ""}</span></div>"}
                <div class="row"><span>Dress:</span><span>${o.dressType}</span></div>
                <div class="row"><span>Delivery:</span><span class="bold">${o.deliveryDate}</span></div>
                
                ${if (docType != "r") """
                    <div class="dashed"></div>
                    <div class="center bold">MEASUREMENTS / ناپ</div>
                    ${if (o.length.isNotBlank()) "<div class='row'><span>Length:</span><span>${o.length}\"</span></div>" else ""}
                    ${if (o.shoulder.isNotBlank()) "<div class='row'><span>Shoulder:</span><span>${o.shoulder}\"</span></div>" else ""}
                    ${if (o.chest.isNotBlank()) "<div class='row'><span>Chest:</span><span>${o.chest}\"</span></div>" else ""}
                    ${if (o.waist.isNotBlank()) "<div class='row'><span>Waist:</span><span>${o.waist}\"</span></div>" else ""}
                    ${if (o.sleeve.isNotBlank()) "<div class='row'><span>Sleeve:</span><span>${o.sleeve}\"</span></div>" else ""}
                    ${if (o.shalwarLength.isNotBlank()) "<div class='row'><span>Shalwar:</span><span>${o.shalwarLength}\"</span></div>" else ""}
                    ${if (o.neckCollar.isNotBlank()) "<div class='row'><span>Collar:</span><span>${o.neckCollar}</span></div>" else ""}
                    ${if (o.daman.isNotBlank()) "<div class='row'><span>Daman:</span><span>${o.daman}</span></div>" else ""}
                    ${if (o.extraNotes.isNotBlank()) "<div>Notes: ${o.extraNotes}</div>" else ""}
                """ else ""}

                ${if (docType != "k") """
                    <div class="dashed"></div>
                    <div class="row"><span>Total:</span><span class="bold">${formatCurrency(o.totalAmount)}</span></div>
                    <div class="row"><span>Advance:</span><span>${formatCurrency(o.advanceAmount)}</span></div>
                    <div class="row"><span>Remaining:</span><span class="bold">${formatCurrency(o.remainingAmount)}</span></div>
                    <div class="row"><span>Payment:</span><span>${o.paymentStatus}</span></div>
                """ else ""}

                <div class="dashed"></div>
                <div class="center" style="margin-top: 8px;">
                    ${if (docType == "r") "Shukriya · شکریہ" else "Craftsmanship Guaranteed"}
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Tailor_$title")
                printManager.print("Tailor_Order_${o.orderNo}", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                // Dialog header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                    }
                }

                // Thermal Slip Card (58mm style preview)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFCCC5B5), RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "✂ $shopName",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1D2740)
                        )
                        Text(
                            text = title,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF555D75)
                        )

                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - - - - - -",
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Gray
                        )

                        SlipRow("Order #:", "#${o.orderNo}")
                        SlipRow("Date:", o.orderDate)
                        SlipRow("Customer:", c?.name ?: "")
                        if (docType == "k") {
                            SlipRow("Karigar:", o.karigar.ifBlank { "—" })
                        } else if (!c?.phone.isNullOrBlank()) {
                            SlipRow("Phone:", c?.phone ?: "")
                        }
                        SlipRow("Dress:", o.dressType)
                        SlipRow("Delivery:", o.deliveryDate)

                        if (docType != "r") {
                            Text(
                                text = "- - - - - - - - - - - - - - - - - - - - - - - -",
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Gray
                            )
                            Text(
                                text = "MEASUREMENTS / ناپ",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF1D2740)
                            )
                            if (o.length.isNotBlank()) SlipRow("Length:", "${o.length}\"")
                            if (o.shoulder.isNotBlank()) SlipRow("Shoulder:", "${o.shoulder}\"")
                            if (o.chest.isNotBlank()) SlipRow("Chest:", "${o.chest}\"")
                            if (o.waist.isNotBlank()) SlipRow("Waist:", "${o.waist}\"")
                            if (o.sleeve.isNotBlank()) SlipRow("Sleeve:", "${o.sleeve}\"")
                            if (o.shalwarLength.isNotBlank()) SlipRow("Shalwar:", "${o.shalwarLength}\"")
                            if (o.neckCollar.isNotBlank()) SlipRow("Collar:", o.neckCollar)
                            if (o.daman.isNotBlank()) SlipRow("Daman:", o.daman)
                            if (o.extraNotes.isNotBlank()) SlipRow("Notes:", o.extraNotes)
                        }

                        if (docType != "k") {
                            Text(
                                text = "- - - - - - - - - - - - - - - - - - - - - - - -",
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Gray
                            )
                            SlipRow("Total:", formatCurrency(o.totalAmount))
                            SlipRow("Advance:", formatCurrency(o.advanceAmount))
                            SlipRow("Remaining:", formatCurrency(o.remainingAmount))
                            SlipRow("Payment:", o.paymentStatus)
                        }

                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - - - - - -",
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Gray
                        )

                        if (docType == "r") {
                            Text(
                                text = "Shukriya · شکریہ",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF1D2740)
                            )
                        }
                    }
                }

                // Share & Print Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val text = generateShareText()
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "$shopName - $title")
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Slip via"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TailorNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_share_slip")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TailorTapeGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share · شیئر")
                    }

                    Button(
                        onClick = { printDocument() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F7A4F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_print_slip")
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print · پرنٹ")
                    }
                }
            }
        }
    }
}

@Composable
private fun SlipRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color(0xFF555D75)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF1D2740)
        )
    }
}
