package com.example.minlish.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.R
import com.example.minlish.viewmodel.AuthViewModel
import com.example.minlish.navigation.Routes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

val BeVietnamPro = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium)
)

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    val primaryPurple = Color(0xFF534AB7)
    val grayColor = Color(0xFF9E9E9E)
    val context = LocalContext.current

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("67533858089-7rg7mp6gjcpi43rehogvj22g0dm4tu6i.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    LaunchedEffect(authViewModel.toastMessage) {
        authViewModel.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            authViewModel.toastMessage = null
        }
    }

    LaunchedEffect(authViewModel.navigateToDashboard, authViewModel.navigateToOnboarding) {
        if (authViewModel.navigateToDashboard) {
            authViewModel.navigateToDashboard = false
            navController.navigate(Routes.Home.route) { popUpTo("login") { inclusive = true } }
        } else if (authViewModel.navigateToOnboarding) {
            authViewModel.navigateToOnboarding = false
            navController.navigate("onboarding") { popUpTo("login") { inclusive = true } }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { authViewModel.loginWithGoogle(it) }
        } catch (e: ApiException) {
            android.util.Log.e("MinLishError", "Lỗi đăng nhập Google: ", e)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(primaryPurple)) {
        if (authViewModel.showVerificationDialog) {
            AlertDialog(
                onDismissRequest = { authViewModel.dismissVerificationDialog() },
                title = { Text("Chưa xác thực email") },
                text = { Text("Tài khoản của bạn chưa được xác thực. Bạn có muốn gửi lại email xác thực không?") },
                confirmButton = {
                    TextButton(onClick = { authViewModel.resendVerificationEmail() }) {
                        Text("Gửi lại")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { authViewModel.dismissVerificationDialog() }) {
                        Text("Hủy")
                    }
                }
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1.2f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_minlish),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp).background(Color.White, shape = RoundedCornerShape(20.dp)).padding(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "MinLish", style = TextStyle(fontSize = 32.sp, fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold, color = Color.White))
            Text(text = "học từ vựng thông minh", style = TextStyle(fontSize = 15.sp, fontFamily = BeVietnamPro, color = Color(0xFFAFA9EC)))
        }

        Surface(
            modifier = Modifier.fillMaxWidth().weight(2f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
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
                        IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = image, contentDescription = null) }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple, unfocusedBorderColor = Color(0xFFE0E0E0), focusedLeadingIconColor = primaryPurple, unfocusedLeadingIconColor = grayColor, focusedTrailingIconColor = primaryPurple, unfocusedTrailingIconColor = grayColor),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        text = "Quên mật khẩu?",
                        color = primaryPurple,
                        fontFamily = BeVietnamPro,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showResetDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { authViewModel.login(email, password) },
                    enabled = !authViewModel.isLoading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                ) {
                    if (authViewModel.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Đăng nhập", fontSize = 16.sp, fontFamily = BeVietnamPro)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
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
                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text(text = "Khôi phục mật khẩu", fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text(text = "Nhập email tài khoản của bạn để nhận liên kết đặt lại mật khẩu mới.", fontFamily = BeVietnamPro, fontSize = 14.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = resetEmail,
                                    onValueChange = { resetEmail = it },
                                    placeholder = { Text("Nhập Email của bạn") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    authViewModel.resetPassword(resetEmail) {
                                        showResetDialog = false
                                        resetEmail = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                            ) {
                                Text("Gửi Email", fontFamily = BeVietnamPro)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetDialog = false }) {
                                Text("Hủy", color = Color.Gray, fontFamily = BeVietnamPro)
                            }
                        },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}
