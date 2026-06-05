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
                    category = category,
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
                repository.updateSet(set.copy(updateAt = System.currentTimeMillis()))
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
                loadVocabularies(vocabulary.setId)
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
                loadVocabularies(vocabulary.setId)
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
                loadVocabularies(vocabulary.setId)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun cleanCsvField(value: String): String {
        return value
            .replace("\r", " ")
            .replace("\n", " ")
            .trim()
    }

    fun importCsv(
        uri: android.net.Uri,
        context: android.content.Context,
        targetSetId: String? = null,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Không thể mở file CSV")

                val reader = java.io.BufferedReader(
                    java.io.InputStreamReader(inputStream, Charsets.UTF_8)
                )

                val lines = reader.lineSequence()
                    .drop(1)
                    .filter { it.isNotBlank() }
                    .toList()

                reader.close()

                if (lines.isEmpty()) {
                    _isLoading.value = false
                    onComplete(false, "File CSV trống hoặc không có dữ liệu!")
                    return@launch
                }

                val db = FirebaseFirestore.getInstance()
                val finalSetId: String

                if (targetSetId == null) {

                    val userId = auth.currentUser?.uid ?: ""

                    val setDoc =
                        db.collection("vocabulary_sets").document()

                    finalSetId = setDoc.id

                    val currentDate =
                        SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date())

                    val newSet = VocabularySet(
                        id = finalSetId,
                        userId = userId,
                        title = "Imported CSV ($currentDate)",
                        description = "Dữ liệu được import tự động từ file CSV",
                        category = "Tất cả"
                    )

                    setDoc.set(newSet).await()

                } else {
                    finalSetId = targetSetId
                }

                // Fetch existing words in this set to check for duplicates
                val existingWords = vocabularyRepository.getWordsBySet(finalSetId)
                var successCount = 0
                var updatedCount = 0

                for (line in lines) {
                    val columns =
                        line.split(
                            ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()
                        ).map {
                            it.trim()
                                .removePrefix("\"")
                                .removeSuffix("\"")
                                .replace("\"\"", "\"")
                        }

                    if (columns.isNotEmpty() && columns[0].isNotBlank()) {
                        val wordText = cleanCsvField(columns.getOrElse(0) { "" })
                        
                        // Case-insensitive duplicate check
                        val existing = existingWords.find { it.word.equals(wordText, ignoreCase = true) }

                        val vocabData = Vocabulary(
                            id = existing?.id ?: "", // Use existing ID if it's an update
                            setId = finalSetId,
                            word = wordText,
                            meaning = cleanCsvField(columns.getOrElse(1) { "" }),
                            wordType = cleanCsvField(columns.getOrElse(2) { "" }),
                            pronunciation = cleanCsvField(columns.getOrElse(3) { "" }),
                            example = cleanCsvField(columns.getOrElse(4) { "" }),
                            collocation = cleanCsvField(columns.getOrElse(5) { "" }),
                            note = cleanCsvField(columns.getOrElse(6) { "" }),
                            status = existing?.status ?: "Mới"
                        )

                        if (existing != null) {
                            vocabularyRepository.updateWord(vocabData)
                            updatedCount++
                        } else {
                            vocabularyRepository.addWord(vocabData)
                            successCount++
                        }
                    }
                }

                if (targetSetId == null) {
                    loadVocabularySets()
                } else {
                    loadVocabularies(targetSetId)
                }

                _isLoading.value = false

                val message = if (updatedCount > 0) {
                    "Thành công! Đã thêm $successCount từ mới và cập nhật $updatedCount từ đã tồn tại."
                } else {
                    "Thành công! Đã thêm $successCount từ vựng."
                }
                onComplete(true, message)

            } catch (e: Exception) {

                _isLoading.value = false

                android.util.Log.e(
                    "MinLishError",
                    "Lỗi Import CSV",
                    e
                )

                onComplete(
                    false,
                    "Lỗi import: Vui lòng kiểm tra lại định dạng file CSV."
                )
            }
        }
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
                    "Word,Meaning,Type,Pronunciation,Example,Collocation,Note\n"
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
