package com.example.minlish.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.minlish.firebase.AuthManager
import androidx.compose.foundation.clickable
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val primaryPurple = Color(0xFF534AB7)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryPurple)
    ) {
        // --- HEADER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Đăng ký",
                 color = Color.White,
                 fontSize = 32.sp,
                 fontFamily = BeVietnamPro,
                 fontWeight = FontWeight.Bold)

            Text(text = "Tạo tài khoản mới để bắt đầu",
                 color = Color(0xCCFFFFFF),
                 fontSize = 14.sp,
                 fontFamily = BeVietnamPro)
        }

        // --- FORM ĐĂNG KÝ ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2.5f), // Tăng weight lên chút để form dài hơn
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()), // Thêm cuộn để không bị che khuất khi bàn phím hiện
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email", fontFamily = BeVietnamPro) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // Mật khẩu
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Mật khẩu", fontFamily = BeVietnamPro) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // Nhập lại mật khẩu
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Nhập lại mật khẩu", fontFamily = BeVietnamPro) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                Spacer(modifier = Modifier.height(32.dp))

                // Nút Đăng ký
                Button(
                    onClick = {

                        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {

                            Toast.makeText(
                                context,
                                "Vui lòng nhập đầy đủ thông tin",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        if (password != confirmPassword) {

                            Toast.makeText(
                                context,
                                "Mật khẩu không khớp",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        AuthManager.register(
                            email = email,
                            password = password,

                            onSuccess = {
                                FirebaseAuth.getInstance().signOut()
                                // SỬA CÂU THÔNG BÁO Ở ĐÂY
                                Toast.makeText(
                                    context,
                                    "Đăng ký thành công! Vui lòng kiểm tra hộp thư email để xác thực.",
                                    Toast.LENGTH_LONG // Đổi thành LONG để hiện lâu hơn một chút
                                ).show()

                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },

                            onError = {
                                Toast.makeText(
                                    context,
                                    it,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                ) {
                    Text("Đăng ký", fontSize = 16.sp, fontFamily = BeVietnamPro)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chuyển về Đăng nhập
                Row(modifier = Modifier.padding(top = 16.dp)) {
                    Text(text = "Đã có tài khoản? ",
                        color = Color.Gray,
                        fontFamily = BeVietnamPro)

                    Text(
                        text = "Đăng nhập",
                        color = primaryPurple,
                        fontWeight = FontWeight.Medium,
                        fontFamily = BeVietnamPro,
                        modifier = Modifier.clickable {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(navController = rememberNavController())
}