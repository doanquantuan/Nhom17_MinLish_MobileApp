package com.example.minlish.ui.screens.vocabulary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.minlish.data.model.VocabularySet
import com.example.minlish.navigation.Routes
import com.example.minlish.ui.components.*
import com.example.minlish.viewmodel.VocabularyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VocabularySetScreen(
    navController: NavController,
    viewModel: VocabularyViewModel = viewModel()
) {
    val vocabularySets by viewModel.vocabularySets.collectAsState()
    val setWordCounts by viewModel.setWordCounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVocabularySets()
    }

    VocabularySetScreenContent(
        vocabularySets = vocabularySets,
        setWordCounts = setWordCounts,
        isLoading = isLoading,
        onAddSetClick = { navController.navigate(Routes.CreateSet.passSetId()) },
        onDeleteSet = { setId -> viewModel.deleteVocabularySet(setId) },
        onEditSet = { set -> navController.navigate(Routes.CreateSet.passSetId(set.id)) },
        onSetClick = { setId ->
            navController.navigate(Routes.VocabularyList.passSetId(setId))
        },
        bottomBar = {}
    )
}

@Composable
fun VocabularySetScreenContent(
    vocabularySets: List<VocabularySet>,
    setWordCounts: Map<String, Int>,
    isLoading: Boolean,
    onAddSetClick: () -> Unit,
    onDeleteSet: (String) -> Unit,
    onEditSet: (VocabularySet) -> Unit,
    onSetClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var setToDelete by remember { mutableStateOf<VocabularySet?>(null) }

    val dynamicCategories = remember(vocabularySets) {
        listOf("Tất cả") + vocabularySets.map { it.category }.distinct().sorted()
    }
    var selectedCategory by remember { mutableStateOf("Tất cả") }

    val filteredSets = vocabularySets.filter {
        (selectedCategory == "Tất cả" || it.category == selectedCategory) &&
                it.title.contains(searchQuery, ignoreCase = true)
    }

    if (setToDelete != null) {
        MinLishConfirmDialog(
            title = "Xóa bộ từ",
            message = "Bạn có chắc chắn muốn xóa bộ từ '${setToDelete!!.title}' không? Hành động này không thể hoàn tác.",
            confirmText = "Xóa",
            onConfirm = {
                onDeleteSet(setToDelete!!.id)
                setToDelete = null
            },
            onDismiss = { setToDelete = null }
        )
    }

    Scaffold(
        topBar = { MinLishLargeHeader(title = "Bộ từ của tôi") },
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSetClick,
                containerColor = Color(0xFF5145B1),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Set")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                MinLishTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "",
                    placeholder = "Tìm bộ từ...",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dynamicCategories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8EAF6),
                                selectedLabelColor = Color(0xFF5145B1),
                                containerColor = Color.White,
                                labelColor = Color.Gray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.LightGray,
                                borderWidth = 1.dp,
                                selectedBorderColor = Color.Transparent,
                                selectedBorderWidth = 0.dp
                            )
                        )
                    }
                }
            }

            if (isLoading && filteredSets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF5145B1))
                    }
                }
            } else if (filteredSets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Không tìm thấy bộ từ nào", color = Color.Gray)
                    }
                }
            } else {
                items(filteredSets) { set ->
                    VocabularySetCard(
                        set = set,
                        wordCount = setWordCounts[set.id] ?: 0,
                        onDelete = { setToDelete = set },
                        onEdit = { onEditSet(it) },
                        onClick = { onSetClick(set.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun VocabularySetCard(
    set: VocabularySet,
    wordCount: Int,
    onDelete: () -> Unit,
    onEdit: (VocabularySet) -> Unit,
    onClick: () -> Unit
) {
    val primaryColor = Color(0xFF5145B1)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val lastUpdated = sdf.format(Date(set.updateAt))
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.dp, color = Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = set.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$wordCount từ · cập nhật $lastUpdated", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }

                Box {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = null) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Chỉnh sửa") },
                            onClick = { showMenu = false; onEdit(set) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa", color = Color.Red) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(color = primaryColor.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = set.category,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = primaryColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Tiến độ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(text = "${set.progress}%", style = MaterialTheme.typography.bodyMedium, color = primaryColor, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { set.progress / 100f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = primaryColor,
                trackColor = Color(0xFFEEEEEE),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}
