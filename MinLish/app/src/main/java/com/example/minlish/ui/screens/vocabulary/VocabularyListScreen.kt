package com.example.minlish.ui.screens.vocabulary

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.minlish.navigation.Routes
import com.example.minlish.R
import com.example.minlish.data.model.Vocabulary
import com.example.minlish.ui.theme.MinLishTheme
import com.example.minlish.viewmodel.CsvExporter
import com.example.minlish.viewmodel.VocabularyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun VocabularyListScreen(
    navController: NavController,
    setId: String,
    viewModel: VocabularyViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val vocabList by viewModel.vocabularies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val setTitle by viewModel.currentSetTitle.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(vocabList, searchQuery) {
        vocabList.filter { it.word.contains(searchQuery, ignoreCase = true) || it.meaning.contains(searchQuery, ignoreCase = true) }
    }

    val total = vocabList.size
    val review = vocabList.count { it.status == "Ôn lại" }
    val learned = vocabList.count { it.status == "Thuộc" }

    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importCsv(it, context, setId) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            viewModel.exportCsv(setId, it, context) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(setId) {
        viewModel.loadVocabularies(setId)
    }

    Scaffold(
        topBar = {
            VocabularyHeader(
                title = setTitle.ifEmpty { "Vocabulary List" },
                onBackClick = { navController.popBackStack() },
                onExportClick = {
                    val fileName = if (setTitle.isNotEmpty()) "${setTitle.replace(" ", "_")}.csv" else "vocab_export.csv"
                    exportLauncher.launch(fileName)
                },
                onImportClick = {
                    importLauncher.launch("text/*")
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                StatsSection(
                    total = total,
                    review = review,
                    learned = learned,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                SearchAndAddRow(
                    searchText = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onAddClick = { navController.navigate(Routes.AddVocabulary.passArgs(setId)) },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (isLoading && vocabList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF5145B1))
                    }
                }
            } else if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "Không có từ vựng nào" else "Không tìm thấy từ vựng",
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                    }
                }
            } else {
                items(filteredList) { vocab ->
                    VocabularyItem(
                        vocab = vocab,
                        onPronounceClick = { viewModel.speak(vocab.word) },
                        onClick = {
                            navController.navigate(Routes.VocabularyDetail.passId(vocab.id))
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 0.5.dp,
                        color = Color(0xFFEEEEEE)
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}



@Composable
fun VocabularyHeader(
    title: String,
    onBackClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color(0xFF5145B1))
            .padding(start = 12.dp, end = 12.dp, bottom = 24.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 38.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onImportClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Import CSV",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(
                onClick = onExportClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Export CSV",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun StatsSection(
    total: Int,
    review: Int,
    learned: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            count = total,
            label = stringResource(R.string.total_words),
            containerColor = Color(0xFFEEF0FF),
            contentColor = Color(0xFF5145B1),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            count = review,
            label = stringResource(R.string.needs_review),
            containerColor = Color(0xFFFFF1E6),
            contentColor = Color(0xFFE48C07),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            count = learned,
            label = stringResource(R.string.learned),
            containerColor = Color(0xFFE6F4EA),
            contentColor = Color(0xFF006D3C),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    count: Int,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = count.toString(),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                fontSize = 15.sp,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SearchAndAddRow(
    searchText: String,
    onSearchChange: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            placeholder = { 
                Text(
                    stringResource(R.string.search_word), 
                    color = Color.Gray,
                    fontSize = 18.sp
                ) 
            },
            modifier = Modifier
                .weight(1f)
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF5145B1)
            ),
            singleLine = true
        )

        Button(
            onClick = onAddClick,
            modifier = Modifier
                .height(58.dp)
                .width(115.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5145B1))
        ) {
            Text(
                stringResource(R.string.add), 
                color = Color.White, 
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun VocabularyItem(
    vocab: Vocabulary,
    onPronounceClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vocab.word,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (vocab.wordType.isNotBlank()) {
                        Text(
                            text = "(${vocab.wordType}) ",
                            fontSize = 16.sp,
                            color = Color(0xFF5145B1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = vocab.meaning,
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    StatusTag(status = vocab.status)
                    if (vocab.status != "Mới") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatNextReviewTime(vocab.nextReview),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                IconButton(
                    onClick = onPronounceClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_volume_up),
                        contentDescription = "Pronounce",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

private fun formatNextReviewTime(nextReview: Long): String {
    val now = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val reviewDate = Calendar.getInstance().apply {
        timeInMillis = nextReview
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val diff = reviewDate - now
    val days = (diff / (1000 * 60 * 60 * 24)).toInt()

    return when {
        days < 0 -> "Cần ôn ngay"
        days == 0 -> "Hôm nay"
        days == 1 -> "Ngày mai"
        days < 30 -> "$days ngày nữa"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(nextReview))
        }
    }
}

@Composable
fun StatusTag(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, textRes) = when (status) {
        "Ôn lại" -> Triple(Color(0xFFFFF1E6), Color(0xFFE48C07), R.string.review_tag)
        "Thuộc" -> Triple(Color(0xFFE6F4EA), Color(0xFF006D3C), R.string.learned_tag)
        else -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), R.string.new_tag)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(textRes),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VocabularyItemPreview() {
    val mockVocab = Vocabulary(
        word = "Architecture",
        wordType = "Noun",
        meaning = "Kiến trúc",
        status = "Thuộc",
        nextReview = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000) // 2 days later
    )
    MinLishTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            VocabularyItem(
                vocab = mockVocab,
                onPronounceClick = {},
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VocabularyItemDuePreview() {
    val mockVocab = Vocabulary(
        word = "Exception",
        wordType = "Noun",
        meaning = "Ngoại lệ",
        status = "Ôn lại",
        nextReview = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000) // Yesterday
    )
    MinLishTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            VocabularyItem(
                vocab = mockVocab,
                onPronounceClick = {},
                onClick = {}
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun VocabularyListScreenPreview() {
//    MinLishTheme {
//        VocabularyListScreen(
//            navController = rememberNavController(),
//            setId = "1"
//        )
//    }
//}
