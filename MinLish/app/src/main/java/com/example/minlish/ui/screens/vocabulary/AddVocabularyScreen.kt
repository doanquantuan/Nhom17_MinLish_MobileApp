package com.example.minlish.ui.screens.vocabulary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.R
import com.example.minlish.data.model.Vocabulary
import com.example.minlish.ui.components.*
import com.example.minlish.viewmodel.VocabularyViewModel

@Composable
fun AddVocabularyScreen(
    navController: NavController,
    setId: String,
    vocabularyId: String? = null,
    viewModel: VocabularyViewModel = viewModel()
) {
    val currentVocabulary by viewModel.currentVocabulary.collectAsState()
    val vocabList by viewModel.vocabularies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(setId) {
        viewModel.loadVocabularies(setId)
    }

    LaunchedEffect(vocabularyId) {
        if (vocabularyId != null) {
            viewModel.loadVocabularyById(vocabularyId)
        } else {
            viewModel.clearCurrentVocabulary()
        }
    }

    AddVocabularyContent(
        isLoading = isLoading,
        setId = setId,
        editingVocab = currentVocabulary,
        vocabList = vocabList,
        onBackClick = { navController.popBackStack() },
        onSaveVocab = { vocab ->
            if (vocabularyId == null) {
                viewModel.addVocabulary(vocab) { success ->
                    if (success) navController.popBackStack()
                }
            } else {
                viewModel.updateVocabulary(vocab) { success ->
                    if (success) navController.popBackStack()
                }
            }
        },
        onDeleteVocab = { vocab ->
            viewModel.deleteVocabulary(vocab) { success ->
                if (success) navController.popBackStack()
            }
        },
        onSpeak = { text -> viewModel.speak(text) }
    )
}

@Composable
fun AddVocabularyContent(
    isLoading: Boolean,
    setId: String,
    editingVocab: Vocabulary?,
    vocabList: List<Vocabulary>,
    onBackClick: () -> Unit,
    onSaveVocab: (Vocabulary) -> Unit,
    onDeleteVocab: (Vocabulary) -> Unit,
    onSpeak: (String) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var wordType by remember { mutableStateOf("Noun") }
    var pronunciation by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var collocation by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var wordError by remember { mutableStateOf<String?>(null) }
    var meaningError by remember { mutableStateOf<String?>(null) }
    
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateVocab by remember { mutableStateOf<Vocabulary?>(null) }

    val wordTypes = listOf("Noun", "Verb", "Adjective", "Adverb", "Preposition", "Conjunction", "Pronoun", "Interjection")

    val initialValues = remember(editingVocab) {
        if (editingVocab != null) {
            Triple(editingVocab.word, editingVocab.meaning, editingVocab.wordType)
        } else {
            Triple("", "", "Noun")
        }
    }

    LaunchedEffect(editingVocab) {
        editingVocab?.let {
            word = it.word
            wordType = it.wordType
            pronunciation = it.pronunciation
            meaning = it.meaning
            example = it.example
            collocation = it.collocation
            note = it.note
        }
    }

    val hasChanges = word != initialValues.first || 
                     meaning != initialValues.second || 
                     wordType != initialValues.third ||
                     pronunciation.isNotBlank() || 
                     example.isNotBlank() || 
                     collocation.isNotBlank() || 
                     note.isNotBlank()

    val handleBack = { if (hasChanges && !isLoading) showDiscardDialog = true else onBackClick() }
    BackHandler(enabled = hasChanges && !isLoading, onBack = handleBack)

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

    if (showDeleteDialog && editingVocab != null) {
        MinLishConfirmDialog(
            title = "Xóa từ vựng",
            message = "Bạn có chắc chắn muốn xóa từ '${editingVocab.word}' không?",
            confirmText = "Xóa",
            onConfirm = { onDeleteVocab(editingVocab) },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showDuplicateDialog && duplicateVocab != null) {
        MinLishConfirmDialog(
            title = "Từ vựng đã tồn tại",
            message = "Từ '${word}' đã có trong bộ từ này. Bạn có muốn cập nhật thông tin cho từ này không?",
            confirmText = "Cập nhật",
            onConfirm = {
                showDuplicateDialog = false
                onSaveVocab(duplicateVocab!!.copy(
                    word = word.trim(),
                    wordType = wordType,
                    pronunciation = pronunciation,
                    meaning = meaning,
                    example = example,
                    collocation = collocation,
                    note = note
                ))
            },
            onDismiss = { showDuplicateDialog = false }
        )
    }

    Scaffold(
        topBar = {
            MinLishTopAppBar(
                title = if (editingVocab == null) "Thêm từ mới" else "Chỉnh sửa từ vựng",
                onBack = handleBack,
                actions = {
                    if (editingVocab != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Thông tin từ vựng", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            MinLishTextField(
                value = word,
                onValueChange = { word = it; wordError = null },
                label = "Từ vựng *",
                error = wordError,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false
                )
            )

            MinLishDropdownField(
                selectedValue = wordType,
                onValueChange = { wordType = it },
                label = "Loại từ",
                options = wordTypes
            )

            MinLishTextField(
                value = pronunciation,
                onValueChange = { pronunciation = it },
                label = "Phát âm",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    autoCorrectEnabled = false
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onSpeak(word) }, enabled = word.isNotBlank()) {
                    Icon(painterResource(R.drawable.ic_volume_up), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nghe thử")
                }
            }

            MinLishTextField(
                value = meaning,
                onValueChange = { meaning = it; meaningError = null },
                label = "Nghĩa *",
                error = meaningError,
                minLines = 2
            )

            MinLishTextField(
                value = example,
                onValueChange = { example = it },
                label = "Ví dụ",
                minLines = 2
            )

            MinLishTextField(
                value = collocation,
                onValueChange = { collocation = it },
                label = "Collocation",
                minLines = 2
            )

            MinLishTextField(
                value = note,
                onValueChange = { note = it },
                label = "Ghi chú",
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            MinLishPrimaryButton(
                text = if (editingVocab == null) "Lưu từ vựng" else "Cập nhật",
                isLoading = isLoading,
                onClick = {
                    val (valid, wErr, mErr) = validateVocab(word, meaning)
                    wordError = wErr
                    meaningError = mErr
                    
                    if (valid) {
                        val existing = vocabList.find { 
                            it.word.trim().equals(word.trim(), ignoreCase = true) && 
                            it.id != (editingVocab?.id ?: "")
                        }
                        
                        if (existing != null) {
                            duplicateVocab = existing
                            showDuplicateDialog = true
                        } else {
                            val vocab = editingVocab?.copy(
                                word = word.trim(),
                                wordType = wordType,
                                pronunciation = pronunciation,
                                meaning = meaning,
                                example = example,
                                collocation = collocation,
                                note = note
                            ) ?: Vocabulary(
                                setId = setId,
                                word = word.trim(),
                                wordType = wordType,
                                pronunciation = pronunciation,
                                meaning = meaning,
                                example = example,
                                collocation = collocation,
                                note = note,
                                status = "Mới"
                            )
                            onSaveVocab(vocab)
                        }
                    }
                }
            )
        }
    }
}

private fun validateVocab(word: String, meaning: String): Triple<Boolean, String?, String?> {
    var wErr: String? = null
    var mErr: String? = null
    if (word.isBlank()) wErr = "Từ vựng không được để trống"
    if (meaning.isBlank()) mErr = "Nghĩa không được để trống"
    return Triple(wErr == null && mErr == null, wErr, mErr)
}
