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
import java.util.*

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
                val newSet = VocabularySet(
                    userId = userId,
                    title = title,
                    description = description,
                    category = category,
                    createdAt = System.currentTimeMillis(),
                    updateAt = System.currentTimeMillis()
                )
                repository.createSet(newSet)
                loadVocabularySets()
                onComplete(true)
            } catch (e: Exception) {
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
}
