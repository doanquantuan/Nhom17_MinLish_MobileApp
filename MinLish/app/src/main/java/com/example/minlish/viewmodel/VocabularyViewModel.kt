package com.example.minlish.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlish.data.model.Vocabulary
import com.example.minlish.data.model.VocabularySet
import com.example.minlish.data.repository.VocabularyRepository
import com.example.minlish.data.repository.VocabularySetRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VocabularyViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val repository = VocabularySetRepository(FirebaseFirestore.getInstance())
    private val auth = FirebaseAuth.getInstance()

    private val _vocabularySets = MutableStateFlow<List<VocabularySet>>(emptyList())
    val vocabularySets: StateFlow<List<VocabularySet>> = _vocabularySets

    private val _setWordCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val setWordCounts: StateFlow<Map<String, Int>> = _setWordCounts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _vocabularies = MutableStateFlow<List<Vocabulary>>(emptyList())
    val vocabularies: StateFlow<List<Vocabulary>> = _vocabularies

    private val _currentVocabulary = MutableStateFlow<Vocabulary?>(null)
    val currentVocabulary: StateFlow<Vocabulary?> = _currentVocabulary

    private val vocabularyRepository = VocabularyRepository(FirebaseFirestore.getInstance())

    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)

    init {
        loadVocabularySets()
        tts = TextToSpeech(application, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            _isTtsReady.value = true
        }
    }

    fun speak(text: String) {
        if (_isTtsReady.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    private val _currentSetTitle = MutableStateFlow("")
    val currentSetTitle: StateFlow<String> = _currentSetTitle

    fun loadVocabularies(setId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _vocabularies.value = vocabularyRepository.getWordsBySet(setId)
                // Also load set title if sets are already loaded, otherwise default
                val title = _vocabularySets.value.find { it.id == setId }?.title
                if (title != null) {
                    _currentSetTitle.value = title
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadVocabularyById(wordId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentVocabulary.value = vocabularyRepository.getWordById(wordId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCurrentVocabulary() {
        _currentVocabulary.value = null
    }

    fun loadVocabularySets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid

                val sets = if (userId != null) {
                    repository.getSetsByUserId(userId)
                } else {
                    repository.getAllSets()
                }
                _vocabularySets.value = sets

                // Fetch counts for each set
                val counts = mutableMapOf<String, Int>()
                sets.forEach { set ->
                    counts[set.id] = vocabularyRepository.getWordCountBySet(set.id)
                }
                _setWordCounts.value = counts

            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun normalizeCategory(category: String): String {
        return category.trim().lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun createVocabularySet(title: String, description: String, category: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: ""
                android.util.Log.d("VocabVM", "Creating set for user: $userId")
                val newSet = VocabularySet(
                    userId = userId,
                    title = title,
                    description = description,
                    category = normalizeCategory(category),
                    createdAt = System.currentTimeMillis(),
                    updateAt = System.currentTimeMillis()
                )
                repository.createSet(newSet)
                android.util.Log.d("VocabVM", "Set created successfully")
                loadVocabularySets()
                onComplete(true)
            } catch (e: Exception) {
                android.util.Log.e("VocabVM", "Error creating set", e)
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateVocabularySet(set: VocabularySet, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateSet(set.copy(
                    category = normalizeCategory(set.category),
                    updateAt = System.currentTimeMillis()
                ))
                loadVocabularySets()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteVocabularySet(setId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteSet(setId)
                loadVocabularySets()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addVocabulary(vocabulary: Vocabulary, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vocabularyRepository.addWord(vocabulary)
                updateSetMetadata(vocabulary.setId) // Cập nhật ngày và tiến độ bộ từ
                loadVocabularies(vocabulary.setId)
                loadVocabularySets()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateVocabulary(vocabulary: Vocabulary, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vocabularyRepository.updateWord(vocabulary)
                updateSetMetadata(vocabulary.setId)
                loadVocabularies(vocabulary.setId)
                loadVocabularySets()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteVocabulary(vocabulary: Vocabulary, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vocabularyRepository.deleteWord(vocabulary.id)
                updateSetMetadata(vocabulary.setId)
                loadVocabularies(vocabulary.setId)
                loadVocabularySets()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cập nhật thời gian và tính toán lại tiến độ cho bộ từ
     */
    private suspend fun updateSetMetadata(setId: String) {
        try {
            val total = vocabularyRepository.getWordCountBySet(setId)
            val learned = vocabularyRepository.getLearnedCountBySet(setId)
            val progress = if (total > 0) (learned * 100 / total) else 0
            
            val currentSet = repository.getSetById(setId)
            if (currentSet != null) {
                repository.updateSet(currentSet.copy(
                    progress = progress,
                    updateAt = System.currentTimeMillis()
                ))
            }
        } catch (e: Exception) {
            android.util.Log.e("VocabVM", "Error updating set metadata", e)
        }
    }

    private fun cleanCsvField(value: String): String {
        return value
            .replace("\r", " ")
            .replace("\n", " ")
            .trim()
    }

    private val _pendingNewWords = MutableStateFlow<List<Vocabulary>>(emptyList())
    private val _pendingDuplicateWords = MutableStateFlow<List<Vocabulary>>(emptyList())
    private val _showImportConflictDialog = MutableStateFlow(false)
    val showImportConflictDialog: StateFlow<Boolean> = _showImportConflictDialog
    val duplicateCount: StateFlow<Int> = _pendingDuplicateWords.map { it.size }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun dismissImportDialog() {
        _showImportConflictDialog.value = false
    }

    /**
     * Bước 1: Đọc file CSV và phân loại từ (Mới vs Trùng)
     */
    fun processCsvFile(
        uri: android.net.Uri,
        context: android.content.Context,
        targetSetId: String,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Không thể mở file CSV")

                val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream, Charsets.UTF_8))
                val lines = reader.lineSequence().drop(1).filter { it.isNotBlank() }.toList()
                reader.close()

                if (lines.isEmpty()) {
                    onError("File CSV trống!")
                    return@launch
                }

                val existingWords = vocabularyRepository.getWordsBySet(targetSetId)
                val newWords = mutableListOf<Vocabulary>()
                val duplicateWords = mutableListOf<Vocabulary>()

                for (line in lines) {
                    val columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                        .map { it.trim().removePrefix("\"").removeSuffix("\"").replace("\"\"", "\"") }

                    if (columns.isNotEmpty() && columns[0].isNotBlank()) {
                        val wordText = cleanCsvField(columns.getOrElse(0) { "" })
                        val existing = existingWords.find { it.word.equals(wordText, ignoreCase = true) }

                        val vocab = Vocabulary(
                            id = existing?.id ?: "",
                            setId = targetSetId,
                            word = wordText,
                            meaning = cleanCsvField(columns.getOrElse(1) { "" }),
                            wordType = cleanCsvField(columns.getOrElse(2) { "" }),
                            pronunciation = cleanCsvField(columns.getOrElse(3) { "" }),
                            example = cleanCsvField(columns.getOrElse(5) { "" }),
                            collocation = cleanCsvField(columns.getOrElse(6) { "" }),
                            note = cleanCsvField(columns.getOrElse(7) { "" }),
                            status = existing?.status ?: "Mới"
                        )

                        if (existing != null) duplicateWords.add(vocab)
                        else newWords.add(vocab)
                    }
                }
                
                if (duplicateWords.isNotEmpty()) {
                    _pendingNewWords.value = newWords
                    _pendingDuplicateWords.value = duplicateWords
                    _showImportConflictDialog.value = true
                } else {
                    performImport(newWords, emptyList(), false) { _, _ -> }
                }
            } catch (e: Exception) {
                onError("Lỗi đọc file: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmImport(shouldUpdateExisting: Boolean, onComplete: (Boolean, String) -> Unit) {
        val newWords = _pendingNewWords.value
        val duplicateWords = _pendingDuplicateWords.value
        _showImportConflictDialog.value = false
        performImport(newWords, duplicateWords, shouldUpdateExisting, onComplete)
    }

    /**
     * Bước 2: Thực hiện lưu vào Database dựa trên lựa chọn của người dùng
     */
    private fun performImport(
        newWords: List<Vocabulary>,
        duplicateWords: List<Vocabulary>,
        shouldUpdateExisting: Boolean,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Luôn thêm từ mới
                newWords.forEach { vocabularyRepository.addWord(it) }

                // Cập nhật từ trùng nếu người dùng chọn "Cập nhật tất cả"
                if (shouldUpdateExisting) {
                    duplicateWords.forEach { vocabularyRepository.updateWord(it) }
                }

                val setId = (newWords.getOrNull(0) ?: duplicateWords.getOrNull(0))?.setId
                if (setId != null) loadVocabularies(setId)

                val total = newWords.size + (if (shouldUpdateExisting) duplicateWords.size else 0)
                onComplete(true, "Đã nhập thành công $total từ vựng.")
            } catch (e: Exception) {
                onComplete(false, "Lỗi khi lưu dữ liệu: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importCsv(
        uri: android.net.Uri,
        context: android.content.Context,
        targetSetId: String? = null,
        onComplete: (Boolean, String) -> Unit
    ) {
        // Giữ lại hàm cũ để không làm lỗi các phần khác, 
        // nhưng chúng ta sẽ chủ yếu dùng 2 hàm mới ở trên.
    }

    fun exportCsv(
        setId: String,
        uri: android.net.Uri,
        context: android.content.Context,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {

            _isLoading.value = true

            try {

                val vocabList =
                    vocabularyRepository.getWordsBySet(setId)

                val setInfo =
                    _vocabularySets.value.find {
                        it.id == setId
                    }

                val title =
                    setInfo?.title ?: "Vocabulary_Export"

                if (vocabList.isEmpty()) {

                    _isLoading.value = false

                    onComplete(
                        false,
                        "Bộ từ này đang trống, không có gì để export!"
                    )

                    return@launch
                }

                val csvContent = StringBuilder()

                csvContent.append(
                    "Word,Meaning,Type,Pronunciation,Description,Example,Collocation,Note\n"
                )

                for (v in vocabList) {

                    val row = listOf(
                        v.word,
                        v.meaning,
                        v.wordType,
                        v.pronunciation,
                        v.example,
                        v.collocation,
                        v.note
                    ).joinToString(",") { field ->

                        val cleanField =
                            cleanCsvField(field)

                        "\"${cleanField.replace("\"", "\"\"")}\""
                    }

                    csvContent
                        .append(row)
                        .append("\n")
                }

                val outputStream =
                    context.contentResolver.openOutputStream(uri)
                        ?: throw Exception("Không thể tạo file CSV")

                outputStream.use {

                    it.write(
                        byteArrayOf(
                            0xEF.toByte(),
                            0xBB.toByte(),
                            0xBF.toByte()
                        )
                    )

                    it.write(
                        csvContent.toString()
                            .toByteArray(Charsets.UTF_8)
                    )

                    it.flush()
                }

                _isLoading.value = false

                onComplete(
                    true,
                    "Đã export thành công bộ từ '$title'!"
                )

            } catch (e: Exception) {

                _isLoading.value = false

                android.util.Log.e(
                    "MinLishError",
                    "Lỗi Export CSV",
                    e
                )

                onComplete(
                    false,
                    "Lỗi export: ${e.localizedMessage}"
                )
            }
        }
    }
}