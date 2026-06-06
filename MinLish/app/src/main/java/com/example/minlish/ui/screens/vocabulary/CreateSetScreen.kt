package com.example.minlish.ui.screens.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSetScreen(
    navController: NavController,
    viewModel: VocabularyViewModel = viewModel(),
    setId: String? = null
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val vocabularySets by viewModel.vocabularySets.collectAsState()

    val editingSet = remember(setId, vocabularySets) {
        if (setId != null) vocabularySets.find { it.id == setId } else null
    }

    CreateSetContent(
        isLoading = isLoading,
        editingSet = editingSet,
        onBackClick = { navController.popBackStack() },
        onCreateSet = { title, description, category ->
            viewModel.createVocabularySet(title, description, category) { success ->
                if (success) {
                    navController.popBackStack()
                }
            }
        },
        onUpdateSet = { updatedSet ->
            viewModel.updateVocabularySet(updatedSet) { success ->
                if (success) {
                    navController.popBackStack()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSetContent(
    isLoading: Boolean,
    editingSet: com.example.minlish.data.model.VocabularySet? = null,
    onBackClick: () -> Unit,
    onCreateSet: (String, String, String) -> Unit,
    onUpdateSet: (com.example.minlish.data.model.VocabularySet) -> Unit = {}
) {
    val categories = listOf("General", "IELTS")

    var title by remember(editingSet) { mutableStateOf(editingSet?.title ?: "") }
    var description by remember(editingSet) { mutableStateOf(editingSet?.description ?: "") }
    var selectedCategory by remember(editingSet) { mutableStateOf(editingSet?.category ?: categories[0]) }
    var expanded by remember { mutableStateOf(false) }

    val isEditMode = editingSet != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Chỉnh sửa bộ từ" else "Tạo bộ từ mới", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF5145B1))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Thông tin bộ từ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tên bộ từ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả (không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text
                )
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Chủ đề") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color(0xFF5145B1)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        if (isEditMode && editingSet != null) {
                            onUpdateSet(editingSet.copy(
                                title = title,
                                description = description,
                                category = selectedCategory.ifBlank { "Chung" }
                            ))
                        } else {
                            onCreateSet(title, description, selectedCategory.ifBlank { "Chung" })
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5145B1)),
                enabled = title.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isEditMode) "Cập nhật bộ từ" else "Tạo bộ từ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}