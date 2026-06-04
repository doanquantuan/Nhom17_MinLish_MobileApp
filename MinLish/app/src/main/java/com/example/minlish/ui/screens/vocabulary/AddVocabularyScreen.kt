package com.example.minlish.ui.screens.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.R
import com.example.minlish.data.model.Vocabulary
import com.example.minlish.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVocabularyScreen(
    navController: NavController,
    setId: String,
    vocabularyId: String? = null,
    viewModel: VocabularyViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentVocabulary by viewModel.currentVocabulary.collectAsState()

    var word by remember { mutableStateOf("") }
    var wordType by remember { mutableStateOf("Noun") }
    var pronunciation by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var collocation by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(vocabularyId) {
        if (vocabularyId != null) {
            viewModel.loadVocabularyById(vocabularyId)
        } else {
            viewModel.clearCurrentVocabulary()
            word = ""
            wordType = "Noun"
            pronunciation = ""
            meaning = ""
            description = ""
            example = ""
            collocation = ""
            note = ""
        }
    }

    LaunchedEffect(currentVocabulary) {
        currentVocabulary?.let { vocab ->
            if (vocabularyId != null && vocab.id == vocabularyId) {
                word = vocab.word
                wordType = vocab.wordType
                pronunciation = vocab.pronunciation
                meaning = vocab.meaning
                description = vocab.description
                example = vocab.example
                collocation = vocab.collocation
                note = vocab.note
            }
        }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val wordTypes = listOf("Noun", "Verb", "Adjective", "Adverb", "Preposition", "Conjunction", "Pronoun", "Interjection")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (vocabularyId == null) stringResource(R.string.add_new_word) else stringResource(R.string.edit_vocabulary),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (word.isNotBlank() && meaning.isNotBlank()) {
                            val vocab = currentVocabulary?.copy(
                                word = word,
                                wordType = wordType,
                                pronunciation = pronunciation,
                                meaning = meaning,
                                description = description,
                                example = example,
                                collocation = collocation,
                                note = note
                            ) ?: Vocabulary(
                                setId = setId,
                                word = word,
                                wordType = wordType,
                                pronunciation = pronunciation,
                                meaning = meaning,
                                description = description,
                                example = example,
                                collocation = collocation,
                                note = note,
                                status = "Mới"
                            )
                            
                            if (vocabularyId == null) {
                                viewModel.addVocabulary(vocab) { success ->
                                    if (success) navController.popBackStack()
                                }
                            } else {
                                viewModel.updateVocabulary(vocab) { success ->
                                    if (success) navController.popBackStack()
                                }
                            }
                        }
                    }) {
                        Text(
                            stringResource(R.string.save),
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF5145B1))
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (isLoading && vocabularyId != null && currentVocabulary == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF5145B1))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InputFieldLocal(
                    label = buildAnnotatedString {
                        append("Từ vựng ")
                        withStyle(SpanStyle(color = Color.Red)) { append("*") }
                    },
                    value = word,
                    onValueChange = { word = it },
                    placeholder = "",
                    singleLine = true
                )

                Column {
                    Text(
                        text = stringResource(R.string.word_type_label),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = wordType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF5145B1)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            wordTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        wordType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                InputFieldLocal(
                    label = buildAnnotatedString { append("Phát âm") },
                    value = pronunciation,
                    onValueChange = { pronunciation = it },
                    placeholder = "",
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.speak(word) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_volume_up),
                                contentDescription = "Speak",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                )

                InputFieldLocal(
                    label = buildAnnotatedString { append(stringResource(R.string.meaning_label)) },
                    value = meaning,
                    onValueChange = { meaning = it },
                    placeholder = "",
                    minLines = 3
                )

                InputFieldLocal(
                    label = buildAnnotatedString { append(stringResource(R.string.description_label)) },
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "",
                    minLines = 2
                )

                InputFieldLocal(
                    label = buildAnnotatedString { append(stringResource(R.string.example_label)) },
                    value = example,
                    onValueChange = { example = it },
                    placeholder = "",
                    minLines = 2
                )

                InputFieldLocal(
                    label = buildAnnotatedString { append("Collocation") },
                    value = collocation,
                    onValueChange = { collocation = it },
                    placeholder = "",
                    minLines = 2
                )

                InputFieldLocal(
                    label = buildAnnotatedString { append("Ghi chú") },
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "",
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (word.isNotBlank() && meaning.isNotBlank()) {
                            val vocab = currentVocabulary?.copy(
                                word = word,
                                wordType = wordType,
                                pronunciation = pronunciation,
                                meaning = meaning,
                                description = description,
                                example = example,
                                collocation = collocation,
                                note = note
                            ) ?: Vocabulary(
                                setId = setId,
                                word = word,
                                wordType = wordType,
                                pronunciation = pronunciation,
                                meaning = meaning,
                                description = description,
                                example = example,
                                collocation = collocation,
                                note = note,
                                status = "Mới"
                            )
                            
                            if (vocabularyId == null) {
                                viewModel.addVocabulary(vocab) { success ->
                                    if (success) navController.popBackStack()
                                }
                            } else {
                                viewModel.updateVocabulary(vocab) { success ->
                                    if (success) navController.popBackStack()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5145B1))
                ) {
                    Text(
                        if (vocabularyId == null) stringResource(R.string.save_vocabulary) else stringResource(R.string.save),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun InputFieldLocal(
    label: AnnotatedString,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    singleLine: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF5145B1),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            minLines = minLines,
            maxLines = if (singleLine) 1 else 5,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
            )
        )
    }
}
