package com.example.minlish.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.minlish.R
import com.example.minlish.firebase.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val BeVietnamPro = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium)
)

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val primaryPurple = Color(0xFF534AB7)
    val grayColor = Color(0xFF9E9E9E)
    val context = LocalContext.current

    // Hàm dùng chung để kiểm tra Onboarding và chuyển trang
    fun checkOnboardingAndNavigate() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.getBoolean("onboardingCompleted") == true) {
                        navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                    } else {
                        navController.navigate("onboarding") { popUpTo("login") { inclusive = true } }
                    }
                }
        }
    }

    // CẤU HÌNH BỘ LAUNCHER ĐỂ MỞ BẢNG ĐĂNG NHẬP GOOGLE
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                AuthManager.loginWithGoogle(
                    idToken = idToken,
                    onSuccess = {
                        Toast.makeText(context, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
                        checkOnboardingAndNavigate() // Gọi hàm check để phân luồng người cũ/mới
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        } catch (e: ApiException) {
            android.util.Log.e("MinLishError", "Lỗi đăng nhập Google: ", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryPurple)
    ) {
        // --- PHẦN 1: HEADER MÀU TÍM ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_minlish),
                contentDescription = "Logo MinLish",
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "MinLish", style = TextStyle(fontSize = 32.sp, fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold, color = Color.White))
            Text(text = "học từ vựng thông minh", style = TextStyle(fontSize = 15.sp, fontFamily = BeVietnamPro, color = Color(0xFFAFA9EC)))
        }

        // --- PHẦN 2: FORM ĐĂNG NHẬP ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = grayColor, fontFamily = BeVietnamPro) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple, unfocusedBorderColor = Color(0xFFE0E0E0), focusedLeadingIconColor = primaryPurple, unfocusedLeadingIconColor = grayColor),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Mật khẩu", color = grayColor, fontFamily = BeVietnamPro) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple, unfocusedBorderColor = Color(0xFFE0E0E0), focusedLeadingIconColor = primaryPurple, unfocusedLeadingIconColor = grayColor, focusedTrailingIconColor = primaryPurple, unfocusedTrailingIconColor = grayColor),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        AuthManager.login(
                            email = email,
                            password = password,
                            onSuccess = {
                                // Lấy thông tin user hiện tại
                                val user = FirebaseAuth.getInstance().currentUser

                                // Kiểm tra xem cái cờ isEmailVerified đã thành true chưa
                                if (user != null && user.isEmailVerified) {
                                    // Nếu đã xác thực -> Cho vào app bình thường
                                    checkOnboardingAndNavigate()
                                } else {
                                    // Nếu CHƯA xác thực -> Báo lỗi và ép đăng xuất
                                    Toast.makeText(
                                        context,
                                        "Tài khoản chưa xác thực! Vui lòng kiểm tra Email của bạn.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    FirebaseAuth.getInstance().signOut()
                                }
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                ) {
                    Text("Đăng nhập", fontSize = 16.sp, fontFamily = BeVietnamPro)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // NÚT ĐĂNG NHẬP GOOGLE
                OutlinedButton(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id)) // Tự động sinh ra khi kết nối Firebase
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tiếp tục với Google", color = Color.Black, fontSize = 16.sp, fontFamily = BeVietnamPro)
                }

                Spacer(modifier = Modifier.weight(1f))

                Row {
                    Text(text = "Chưa có tài khoản? ", color = Color.Gray, fontFamily = BeVietnamPro)
                    Text(text = "Đăng ký", color = primaryPurple, fontWeight = FontWeight.Medium, fontFamily = BeVietnamPro, modifier = Modifier.clickable { navController.navigate("register") })
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}