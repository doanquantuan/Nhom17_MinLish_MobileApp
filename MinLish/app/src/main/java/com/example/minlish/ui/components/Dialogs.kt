package com.example.minlish.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MinLishConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Xác nhận",
    dismissText: String = "Hủy",
    confirmColor: Color = Color.Red,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = confirmColor)) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = Color.Gray)
            }
        }
    )
}

@Composable
fun MinLishImportConflictDialog(
    duplicateCount: Int,
    onUpdateAll: () -> Unit,
    onOnlyNew: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Phát hiện trùng lặp", fontWeight = FontWeight.Bold) },
        text = { Text("Có $duplicateCount từ trong file đã tồn tại trong bộ từ này. Bạn muốn xử lý thế nào?") },
        confirmButton = {
            Button(
                onClick = onUpdateAll,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5145B1))
            ) {
                Text("Cập nhật tất cả")
            }
        },
        dismissButton = {
            TextButton(onClick = onOnlyNew) {
                Text("Chỉ thêm từ mới", color = Color(0xFF5145B1))
            }
        }
    )
}
