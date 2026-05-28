package com.example.minlish.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val primaryPurple = Color(0xFF534AB7)
    val context = LocalContext.current

    LaunchedEffect(authViewModel.toastMessage) {
        authViewModel.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.toastMessage = null
        }
    }

    LaunchedEffect(authViewModel.registerSuccess) {
        if (authViewModel.registerSuccess) {
            authViewModel.registerSuccess = false
            navController.navigate("login") { popUpTo("register") { inclusive = true } }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(primaryPurple)) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Đăng ký", color = Color.White, fontSize = 32.sp, fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold)
            Text(text = "Tạo tài khoản mới để bắt đầu", color = Color(0xCCFFFFFF), fontSize = 14.sp, fontFamily = BeVietnamPro)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().weight(2.5f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email", fontFamily = BeVietnamPro) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Mật khẩu", fontFamily = BeVietnamPro) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Nhập lại mật khẩu", fontFamily = BeVietnamPro) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { authViewModel.register(email, password, confirmPassword) },
                    enabled = !authViewModel.isLoading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                ) {
                    if (authViewModel.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Đăng ký", fontSize = 16.sp, fontFamily = BeVietnamPro)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.padding(top = 16.dp)) {
                    Text(text = "Đã có tài khoản? ", color = Color.Gray, fontFamily = BeVietnamPro)
                    Text(text = "Đăng nhập", color = primaryPurple, fontWeight = FontWeight.Medium, fontFamily = BeVietnamPro, modifier = Modifier.clickable { navController.popBackStack() })
                }
            }
        }
    }
}