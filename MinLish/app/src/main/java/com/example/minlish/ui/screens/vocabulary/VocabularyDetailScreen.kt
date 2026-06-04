package com.example.minlish.ui.screens.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.R
import com.example.minlish.navigation.Routes
import com.example.minlish.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyDetailScreen(
    navController: NavController,
    vocabularyId: String,
    modifier: Modifier = Modifier,
    viewModel: VocabularyViewModel = viewModel()
) {
    val vocab by viewModel.currentVocabulary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(vocabularyId) {
        viewModel.loadVocabularyById(vocabularyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.word_details), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (vocab != null) {
                        IconButton(onClick = {
                            navController.navigate(Routes.AddVocabulary.passArgs(vocab?.setId ?: "", vocab?.id))
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = {
                            vocab?.let {
                                viewModel.deleteVocabulary(it) { success ->
                                    if (success) navController.popBackStack()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF5145B1))
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (isLoading || vocab == null) {
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = vocab!!.word, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = "(${vocab!!.wordType})", fontSize = 18.sp, color = Color(0xFF5145B1), fontWeight = FontWeight.Medium)
                    }
                    IconButton(
                        onClick = { viewModel.speak(vocab!!.word) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_volume_up),
                            contentDescription = "Speak",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Black
                        )
                    }
                }

                DetailItem(label = stringResource(R.string.pronunciation_label), value = vocab!!.pronunciation)
                DetailItem(label = stringResource(R.string.meaning_label), value = vocab!!.meaning)
                DetailItem(label = stringResource(R.string.description_label), value = vocab!!.description)
                DetailItem(label = stringResource(R.string.example_label), value = vocab!!.example)
                DetailItem(label = stringResource(R.string.collocation_label), value = vocab!!.collocation)
                DetailItem(label = stringResource(R.string.note_label), value = vocab!!.note)
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    if (value.isNotBlank()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = label, fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = value,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp,
                    color = Color.Black,
                    lineHeight = 26.sp
                )
            }
        }
    }
}
