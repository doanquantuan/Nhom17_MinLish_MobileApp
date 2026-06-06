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
import com.example.minlish.viewmodel.VocabularyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularySetScreen(
    navController: NavController,
    viewModel: VocabularyViewModel = viewModel()
) {
    val vocabularySets by viewModel.vocabularySets.collectAsState()
    val setWordCounts by viewModel.setWordCounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        android.util.Log.d("VocabSetScreen", "Refreshing vocabulary sets")
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
        bottomBar = { MinLishBottomNavigation(navController) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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

    // Dynamic categories
    val dynamicCategories = remember(vocabularySets) {
        listOf("Tất cả") + vocabularySets.map { it.category }.distinct().sorted()
    }
    var selectedCategory by remember { mutableStateOf("Tất cả") }

    val filteredSets = vocabularySets.filter {
        (selectedCategory == "Tất cả" || it.category == selectedCategory) &&
                it.title.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF5145B1)),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = "Bộ từ của tôi",
                    modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
        },
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
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = { Text("Tìm bộ từ...", color = Color.Gray) },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF1F1F1),
                        focusedContainerColor = Color(0xFFF1F1F1),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF5145B1)
                    ),
                    singleLine = true
                )
            }

            // Dynamic Category Chips
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF5145B1))
                    }
                }
            } else if (filteredSets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không tìm thấy bộ từ nào", color = Color.Gray)
                    }
                }
            } else {
                // Vocabulary Set List
                items(filteredSets) { set ->
                    VocabularySetCard(
                        set = set,
                        wordCount = setWordCounts[set.id] ?: 0,
                        onDelete = { onDeleteSet(set.id) },
                        onEdit = { onEditSet(it) },
                        onClick = { onSetClick(set.id) }
                    )
                }

                // Extra padding for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
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
    val progressColor = when (set.category) {
        "IELTS" -> Color(0xFF5145B1)
        "Business" -> Color(0xFF006D3C)
        "Travel" -> Color(0xFFE48C07)
        else -> Color(0xFF5145B1)
    }

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
                    Text(
                        text = set.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$wordCount từ · cập nhật $lastUpdated",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Chỉnh sửa") },
                            onClick = {
                                showMenu = false
                                onEdit(set)
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = progressColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = set.category,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = progressColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiến độ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "${set.progress}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = progressColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { set.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = progressColor,
                trackColor = Color(0xFFEEEEEE),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

@Composable
fun MinLishBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem("Trang chủ", Icons.Default.Home, "dashboard/0"),
            BottomNavItem("Bộ từ", Icons.AutoMirrored.Filled.MenuBook, "dashboard/1"),
            BottomNavItem("Học", Icons.Default.Description, "dashboard/2"),
            BottomNavItem("Thống kê", Icons.Default.BarChart, "dashboard/3"),
            BottomNavItem("Cá nhân", Icons.Default.Person, "dashboard/4")
        )

        items.forEach { item ->
            val isSelected = currentRoute == item.route ||
                    (item.route == "dashboard/1" && currentRoute == Routes.VocabularySet.route)

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title, fontSize = 10.sp) },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF5145B1),
                    selectedTextColor = Color(0xFF5145B1),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)
