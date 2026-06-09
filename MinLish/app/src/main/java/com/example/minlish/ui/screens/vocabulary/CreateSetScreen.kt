package com.example.minlish.ui.screens.vocabulary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.data.model.VocabularySet
import com.example.minlish.ui.components.*
import com.example.minlish.viewmodel.VocabularyViewModel

@Composable
fun CreateSetScreen(
    navController: NavController,
    viewModel: VocabularyViewModel = viewModel(),
    setId: String? = null
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val vocabularySets by viewModel.vocabularySets.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVocabularySets()
    }

    val editingSet = remember(setId, vocabularySets) {
        if (setId != null) vocabularySets.find { it.id == setId } else null
    }

    val categories = remember(vocabularySets) {
        val defaultCategories = listOf("Chung", "IELTS", "TOEIC", "Giao tiếp", "Học thuật", "Cơ bản")
        val userCategories = vocabularySets.map { it.category.trim() }
        (defaultCategories + userCategories).distinctBy { it.lowercase() }.filter { it.isNotBlank() }
    }

    CreateSetContent(
        isLoading = isLoading,
        editingSet = editingSet,
        availableCategories = categories,
        isDuplicateName = { titleToCheck -> 
            vocabularySets.any { 
                it.title.trim().equals(titleToCheck.trim(), ignoreCase = true) && 
                it.id != (if (setId == "new") "" else (setId ?: ""))
            }
        },
        onBackClick = { navController.popBackStack() },
        onSaveSet = { title, description, category ->
            if (editingSet != null) {
                viewModel.updateVocabularySet(editingSet.copy(title = title, description = description, category = category)) { success ->
                    if (success) navController.popBackStack()
                }
            } else {
                viewModel.createVocabularySet(title, description, category) { success ->
                    if (success) navController.popBackStack()
                }
            }
        },
        onDeleteSet = { id ->
            viewModel.deleteVocabularySet(id) { success ->
                if (success) navController.popBackStack()
            }
        }
    )
}

@Composable
fun CreateSetContent(
    isLoading: Boolean,
    editingSet: VocabularySet?,
    availableCategories: List<String>,
    isDuplicateName: (String) -> Boolean,
    onBackClick: () -> Unit,
    onSaveSet: (String, String, String) -> Unit,
    onDeleteSet: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    
    var titleError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val initialValues = remember(editingSet) {
        Triple(editingSet?.title ?: "", editingSet?.description ?: "", editingSet?.category ?: "")
    }

    LaunchedEffect(editingSet) {
        editingSet?.let {
            title = it.title
            description = it.description
            selectedCategory = it.category
        }
    }

    val hasChanges = title != initialValues.first || 
                     description != initialValues.second || 
                     (selectedCategory != initialValues.third && selectedCategory.isNotBlank())

    val handleBack = { if (hasChanges) showDiscardDialog = true else onBackClick() }
    BackHandler(enabled = hasChanges, onBack = handleBack)

    // --- Dialogs ---
    if (showDiscardDialog) {
        MinLishConfirmDialog(
            title = "Hủy thay đổi?",
            message = "Bạn có những thay đổi chưa được lưu. Bạn có chắc chắn muốn thoát không?",
            confirmText = "Thoát",
            onConfirm = onBackClick,
            onDismiss = { showDiscardDialog = false }
        )
    }
    if (showDeleteDialog && editingSet != null) {
        MinLishConfirmDialog(
            title = "Xóa bộ từ",
            message = "Bạn có chắc chắn muốn xóa bộ từ '${editingSet.title}' không? Hành động này không thể hoàn tác.",
            confirmText = "Xóa",
            onConfirm = { onDeleteSet(editingSet.id) },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            MinLishTopAppBar(
                title = if (editingSet != null) "Chỉnh sửa bộ từ" else "Tạo bộ từ mới",
                onBack = handleBack,
                actions = {
                    if (editingSet != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Thông tin bộ từ", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            MinLishTextField(
                value = title,
                onValueChange = { title = it; titleError = null },
                label = "Tên bộ từ",
                error = titleError
            )

            MinLishTextField(
                value = description,
                onValueChange = { description = it },
                label = "Mô tả (không bắt buộc)",
                minLines = 3
            )

            MinLishDropdownField(
                selectedValue = selectedCategory,
                onValueChange = { selectedCategory = it; categoryError = null },
                label = "Chủ đề",
                options = availableCategories,
                error = categoryError
            )

            Spacer(modifier = Modifier.weight(1f))

            MinLishPrimaryButton(
                text = if (editingSet != null) "Cập nhật bộ từ" else "Tạo bộ từ",
                isLoading = isLoading,
                onClick = {
                    val (valid, tErr, cErr) = validateSet(title, selectedCategory, isDuplicateName)
                    titleError = tErr
                    categoryError = cErr
                    if (valid) onSaveSet(title.trim(), description.trim(), selectedCategory.trim())
                }
            )
        }
    }
}

private fun validateSet(title: String, category: String, isDuplicate: (String) -> Boolean): Triple<Boolean, String?, String?> {
    var tErr: String? = null
    var cErr: String? = null
    if (title.isBlank()) tErr = "Tên bộ từ không được để trống"
    else if (isDuplicate(title)) tErr = "Tên bộ từ này đã tồn tại"
    if (category.isBlank()) cErr = "Chủ đề không được để trống"
    return Triple(tErr == null && cErr == null, tErr, cErr)
}
