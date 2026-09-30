package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TailorNavy

@Composable
fun ShopNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var shopNameInput by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Shop Name · دکان کا نام", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the name of your tailoring shop:")
                Text("اپنی درزی دکان کا نام تبدیل کریں:")
                OutlinedTextField(
                    value = shopNameInput,
                    onValueChange = { shopNameInput = it },
                    singleLine = true,
                    label = { Text("Shop Name") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(shopNameInput) },
                colors = ButtonDefaults.buttonColors(containerColor = TailorNavy)
            ) {
                Text("Save · محفوظ کریں")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel · منسوخ")
            }
        }
    )
}
