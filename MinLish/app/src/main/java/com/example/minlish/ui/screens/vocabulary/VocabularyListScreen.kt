package com.example.minlish.ui.screens.vocabulary

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.R
import com.example.minlish.data.model.Vocabulary
import com.example.minlish.ui.screens.auth.BeVietnamPro
import com.example.minlish.navigation.Routes
import com.example.minlish.ui.components.*
import com.example.minlish.viewmodel.VocabularyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VocabularyListScreen(
    navController: NavController,
    setId: String,
    viewModel: VocabularyViewModel = viewModel()
) {
    val vocabularies by viewModel.vocabularies.collectAsState()
    val setTitle by viewModel.currentSetTitle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showImportConflictDialog by viewModel.showImportConflictDialog.collectAsState()
    val duplicateCount by viewModel.duplicateCount.collectAsState()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.processCsvFile(it, context, setId) { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() } }
    }

    if (showImportConflictDialog) {
        MinLishImportConflictDialog(
            duplicateCount = duplicateCount,
            onUpdateAll = {
                viewModel.confirmImport(true) { _, msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
            },
            onOnlyNew = {
                viewModel.confirmImport(false) { _, msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
            },
            onDismiss = { viewModel.dismissImportDialog() }
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { viewModel.exportCsv(setId, it, context) { _, msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() } }
    }

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(setId) {
        viewModel.loadVocabularies(setId)
    }

    Scaffold(
        topBar = {
            MinLishLargeHeader(
                title = setTitle.ifEmpty { "Danh sách từ" },
                onBack = { navController.popBackStack() },
                height = 100.dp,
                actions = {
                    IconButton(onClick = { importLauncher.launch("text/*") }) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                    }
                    IconButton(onClick = {
                        val fileName = if (setTitle.isNotEmpty()) "${setTitle.replace(" ", "_")}.csv" else "vocab_export.csv"
                        exportLauncher.launch(fileName)
                    }) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White)
        ) {
            if (vocabularies.isNotEmpty()) {
                item {
                    val learned = vocabularies.count { it.status == "Thuộc" }
                    val needsReview = vocabularies.count { it.status == "Ôn lại" }
                    val newWords = vocabularies.count { it.status == "Mới" }
                    
                    StatsSection(newWords = newWords, needsReview = needsReview, learned = learned, modifier = Modifier.padding(top = 24.dp))
                }
            }

            item {
                SearchAndAddRow(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onAddClick = { navController.navigate(Routes.AddVocabulary.passArgs(setId, null)) },
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF5145B1))
                    }
                }
            } else {
                val filteredVocabs = vocabularies.filter {
                    it.word.contains(searchQuery, ignoreCase = true) ||
                    it.meaning.contains(searchQuery, ignoreCase = true)
                }
                items(filteredVocabs) { vocab ->
                    VocabularyItem(
                        vocab = vocab,
                        onPronounceClick = { viewModel.speak(vocab.word) },
                        onClick = { navController.navigate(Routes.VocabularyDetail.passId(vocab.id)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 1.dp, color = Color(0xFFF0F0F0))
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun StatsSection(newWords: Int, needsReview: Int, learned: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VocabStatCard(count = newWords, label = "Từ mới", color = Color(0xFFE8EAF6), textColor = Color(0xFF3F51B5), modifier = Modifier.weight(1f))
        VocabStatCard(count = needsReview, label = "Cần ôn", color = Color(0xFFFFF3E0), textColor = Color(0xFFEF6C00), modifier = Modifier.weight(1f))
        VocabStatCard(count = learned, label = "Đã thuộc", color = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
    }
}

@Composable
fun VocabStatCard(count: Int, label: String, color: Color, textColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = count.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = label, fontSize = 14.sp, color = textColor.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun SearchAndAddRow(query: String, onQueryChange: (String) -> Unit, onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MinLishTextField(
            value = query,
            onValueChange = onQueryChange,
            label = "",
            placeholder = "Tìm từ...",
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onAddClick,
            modifier = Modifier.width(100.dp).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5145B1))
        ) {
            Text("Thêm", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VocabularyItem(vocab: Vocabulary, onPronounceClick: () -> Unit, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = vocab.word, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (vocab.wordType.isNotBlank()) {
                        Text(text = "(${vocab.wordType}) ", fontSize = 16.sp, color = Color(0xFF5145B1), fontWeight = FontWeight.Medium)
                    }
                    Text(text = vocab.meaning, fontSize = 18.sp, color = Color.Gray)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusTag(status = vocab.status)
                if (vocab.status != "Mới") {
                    Text(
                        text = formatNextReviewTime(vocab.nextReview),
                        fontSize = 11.sp,
                        color = if (vocab.nextReview <= System.currentTimeMillis()) Color(0xFFEF6C00) else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(onClick = onPronounceClick) {
                Icon(painterResource(R.drawable.ic_volume_up), contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp))
            }
        }
    }
}

fun formatNextReviewTime(nextReview: Long): String {
    val now = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
    val diff = nextReview - now
    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
    return when {
        days < 0 -> "Cần ôn ngay"
        days == 0 -> "Hôm nay"
        days == 1 -> "Ngày mai"
        days < 30 -> "$days ngày nữa"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(nextReview))
    }
}

@Composable
fun StatusTag(status: String) {
    val (color, textColor) = when (status) {
        "Thuộc" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Ôn lại" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        else -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
    }
    Surface(color = color, shape = RoundedCornerShape(8.dp)) {
        Text(text = status, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}
