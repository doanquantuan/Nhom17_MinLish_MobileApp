package com.example.minlish.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.ui.screens.auth.BeVietnamPro
import com.example.minlish.viewmodel.AuthViewModel
import com.example.minlish.viewmodel.VocabularyViewModel

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel = viewModel(), vocabViewModel: VocabularyViewModel = viewModel()) {
    val primaryPurple = Color(0xFF534AB7)
    val lightGrayBg = Color(0xFFF5F5F5)
    var isReminderEnabled by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val csvPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            vocabViewModel.importCsv(uri, context) { _, message ->
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    // Gọi tải dữ liệu profile qua ViewModel khi mở màn hình
    LaunchedEffect(Unit) {
        authViewModel.loadUserProfile()
    }

    Column(modifier = Modifier.fillMaxSize().background(lightGrayBg)) {
        // --- PHẦN 1: HEADER ---
        Column(
            modifier = Modifier.fillMaxWidth().background(color = primaryPurple).padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val avatarText = authViewModel.userName.split(" ").take(2).joinToString("") { it.take(1).uppercase() }
            Box(
                modifier = Modifier.size(90.dp).background(Color(0xFFE8E7F5), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // FIX: Dùng ifEmpty cho code gọn và chuẩn Kotlin
                Text(text = avatarText.ifEmpty { "MK" }, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryPurple, fontFamily = BeVietnamPro)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (authViewModel.isEditingName) {
                OutlinedTextField(
                    value = authViewModel.userName,
                    onValueChange = { authViewModel.userName = it },
                    trailingIcon = {
                        IconButton(onClick = { authViewModel.updateName(authViewModel.userName) }) {
                            Icon(Icons.Default.Check, contentDescription = "Lưu", tint = Color.White)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(alpha = 0.5f)),
                    singleLine = true,
                    modifier = Modifier.padding(horizontal = 32.dp).height(56.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { authViewModel.isEditingName = true }
                ) {
                    Text(text = authViewModel.userName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = BeVietnamPro)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa tên", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = authViewModel.userEmail, fontSize = 14.sp, color = Color(0xFFAFA9EC), fontFamily = BeVietnamPro)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProfileTag(text = authViewModel.userLevel) }
        }

        // --- PHẦN 2: CÀI ĐẶT ---
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column {
                Text(text = "Cài đặt học", color = Color.Gray, fontSize = 14.sp, fontFamily = BeVietnamPro, modifier = Modifier.padding(bottom = 8.dp))

                // FIX LỖI ELEVATION: Bỏ luôn chữ định danh, chỉ truyền số 2.dp
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        SettingRow(label = "Từ mới mỗi ngày", value = authViewModel.userWordsPerDay.toString())
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Nhắc nhở hàng ngày", fontSize = 16.sp, fontFamily = BeVietnamPro, color = Color.DarkGray)
                            Switch(checked = isReminderEnabled, onCheckedChange = { isReminderEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryPurple))
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                        SettingRow(label = "Giờ nhắc", value = "20:00")
                    }
                }
            }

            Column {
                Text(text = "Dữ liệu", color = Color.Gray, fontSize = 14.sp, fontFamily = BeVietnamPro, modifier = Modifier.padding(bottom = 8.dp))

                Button(
                    onClick = { csvPickerLauncher.launch("*/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    // FIX LỖI ELEVATION: Truyền số 2.dp trực tiếp
                    elevation = ButtonDefaults.buttonElevation(2.dp)
                ) {
                    Text(text = $$"Import CSV", color = Color.Black, fontSize = 16.sp, fontFamily = BeVietnamPro, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lưu ý: Chỉ hỗ trợ định dạng file .csv\nThứ tự các cột: Từ vựng, Nghĩa, Loại từ, Phát âm, Ví dụ, Collocation, Ghi chú",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = BeVietnamPro,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    lineHeight = 18.sp // Tăng khoảng cách dòng cho dễ đọc
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { showLogoutDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Đăng xuất", color = Color(0xFFD32F2F), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro, textAlign = TextAlign.Center)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Xác nhận đăng xuất", fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng MinLish không?", fontFamily = BeVietnamPro) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout {
                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        }
                    }
                ) { Text("Đăng xuất", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy", color = Color.Gray, fontFamily = BeVietnamPro) } },
            containerColor = Color.White, shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ProfileTag(text: String) {
    Surface(
        color = Color(0x26FFFFFF),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = BeVietnamPro,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, fontFamily = BeVietnamPro, color = Color.DarkGray)
        Text(text = value, fontSize = 16.sp, fontFamily = BeVietnamPro, color = Color(0xFF534AB7), fontWeight = FontWeight.Bold)
    }
}