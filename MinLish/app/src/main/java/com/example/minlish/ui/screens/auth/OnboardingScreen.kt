package com.example.minlish.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minlish.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun OnboardingScreen(navController: NavController) {
    var currentStep by remember { mutableIntStateOf(1) }
    val primaryPurple = Color(0xFF534AB7)
    val context = LocalContext.current // Khai báo context để hiển thị Toast thông báo

    // Lưu thông tin lựa chọn của user
    var selectedGoal by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("") }
    var selectedWordsPerDay by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Thanh tiến trình
        Text(
            text = "BƯỚC $currentStep / 3",
            color = primaryPurple,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = BeVietnamPro
        )

        LinearProgressIndicator(
            progress = { currentStep / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(6.dp),
            color = primaryPurple,
            trackColor = Color(0xFFE0E0E0),
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hiển thị giao diện theo từng bước
        Box(modifier = Modifier.weight(1f)) {
            when (currentStep) {
                1 -> StepGoal(selectedGoal) { selectedGoal = it }
                2 -> StepLevel(selectedLevel) { selectedLevel = it }
                3 -> StepWordsPerDay(selectedWordsPerDay) { selectedWordsPerDay = it }
            }
        }

        // NÚT CHUYỂN TIẾP (ĐÃ THÊM LOGIC LƯU FIRESTORE)
        Button(
            onClick = {
                if (currentStep < 3) {
                    // RÀNG BUỘC: Bắt buộc chọn mới cho qua bước tiếp theo
                    if (currentStep == 1 && selectedGoal.isEmpty()) {
                        Toast.makeText(context, "Vui lòng chọn mục tiêu của bạn!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (currentStep == 2 && selectedLevel.isEmpty()) {
                        Toast.makeText(context, "Vui lòng chọn trình độ hiện tại!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    currentStep++
                } else {
                    if (selectedWordsPerDay == 0) {
                        Toast.makeText(context, "Vui lòng chọn mục tiêu mỗi ngày!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    // BƯỚC 3: ĐẨY DỮ LIỆU LÊN FIRESTORE
                    isLoading = true
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    if (userId != null) {
                        val db = FirebaseFirestore.getInstance()

                        // Đóng gói dữ liệu thành một Map
                        val userProfile = hashMapOf(
                            "goal" to selectedGoal,
                            "level" to selectedLevel,
                            "wordsPerDay" to selectedWordsPerDay,
                            "onboardingCompleted" to true
                        )

                        db.collection("users").document(userId)
                            .set(userProfile, SetOptions.merge())
                            .addOnSuccessListener {
                                isLoading = false
                                Toast.makeText(context, "Thiết lập thành công!", Toast.LENGTH_SHORT).show()
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        isLoading = false
                        Toast.makeText(context, "Lỗi: Chưa đăng nhập!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (currentStep == 3) "Bắt đầu học →" else "Tiếp tục →",
                    fontSize = 16.sp,
                    fontFamily = BeVietnamPro
                )
            }
        }
    }
}

// ... (CÁC HÀM StepIcon, StepGoal, StepLevel, StepWordsPerDay, OnboardingOptionCard GIỮ NGUYÊN NHƯ CŨ) ...

@Composable
fun StepIcon(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                color = Color(0xFFF3F2FA),
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun StepGoal(selected: String, onSelect: (String) -> Unit) {
    val goals = listOf("IELTS / TOEIC", "Giao tiếp hằng ngày", "Business English", "Tự học chung")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepIcon(iconRes = R.drawable.target)
        Text("Mục tiêu của bạn?", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
        Text("Chúng tôi sẽ cá nhân hóa lộ trình", color = Color.Gray, fontSize = 14.sp, fontFamily = BeVietnamPro)
        Spacer(modifier = Modifier.height(24.dp))
        goals.forEach { goal ->
            OnboardingOptionCard(text = goal, isSelected = selected == goal) { onSelect(goal) }
        }
    }
}

@Composable
fun StepLevel(selected: String, onSelect: (String) -> Unit) {
    val levels = listOf("A1-A2 (Sơ cấp)", "B1-B2 (Trung cấp)", "C1-C2 (Nâng cao)", "Chưa biết (Kiểm tra ngay)")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepIcon(iconRes = R.drawable.star)
        Text("Trình độ hiện tại?", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
        Text("Ước tính cũng được", color = Color.Gray, fontSize = 14.sp, fontFamily = BeVietnamPro)
        Spacer(modifier = Modifier.height(24.dp))
        levels.forEach { level ->
            OnboardingOptionCard(text = level, isSelected = selected == level) { onSelect(level) }
        }
    }
}

@Composable
fun StepWordsPerDay(selected: Int, onSelect: (Int) -> Unit) {
    val options = listOf(5, 10, 20, 30)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepIcon(iconRes = R.drawable.smile)
        Text("Mục tiêu mỗi ngày?", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
        Text("Chọn số từ bạn muốn học mỗi ngày", color = Color.Gray, fontSize = 14.sp, fontFamily = BeVietnamPro)
        Spacer(modifier = Modifier.height(24.dp))
        options.forEach { count ->
            OnboardingOptionCard(text = "$count từ / ngày", isSelected = selected == count) { onSelect(count) }
        }
    }
}

@Composable
fun OnboardingOptionCard(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val primaryPurple = Color(0xFF534AB7)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF3F2FA) else Color.White
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSelected) primaryPurple else Color(0xFFE0E0E0)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontSize = 16.sp,
            fontFamily = BeVietnamPro,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) primaryPurple else Color.Black,
            textAlign = TextAlign.Center
        )
    }
}