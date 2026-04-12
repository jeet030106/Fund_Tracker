package com.example.fundtracker.ui.features.product_details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fundtracker.data.model.FundDetailsResponse
import com.example.fundtracker.data.model.NavData
import com.example.fundtracker.data.room.PortfolioEntity
import com.example.fundtracker.ui.features.AddToPortfolioSheet
import com.example.fundtracker.ui.utils.Resource
import com.example.fundtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    viewModel: ProductDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val portfolioResource by viewModel.portfolios.collectAsState()
    val savedInPortfolios by viewModel.savedPortfolios.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fund Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is Resource.Success) {
                        IconButton(onClick = {
                            if (isSaved) {
                                if (savedInPortfolios.size == 1) {
                                    viewModel.removeFromPortfolio(savedInPortfolios.first().id)
                                } else {
                                    showRemoveDialog = true
                                }
                            } else {
                                showAddSheet = true
                            }
                        }) {
                            Icon(
                                // Use painterResource for local drawable files
                                painter = painterResource(
                                    id = if (isSaved) R.drawable.icon_saved else R.drawable.icon_save
                                ),
                                contentDescription = "Save Toggle",
                                // Keeps the red color for saved state
                                tint = if (isSaved) Color.Red else LocalContentColor.current,
                                modifier = Modifier.size(24.dp) // Standard icon size
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            when (val result = uiState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> { /* Error UI as per original code */ }
                is Resource.Success -> {
                    ProductDetailsContent(result.data)

                    // SHEET FOR ADDING
                    if (showAddSheet) {
                        AddToPortfolioSheet(
                            portfolioResource = portfolioResource,
                            onDismiss = { showAddSheet = false },
                            // FIXED: Parameter name changed to onSelectPortfolios
                            onSelectPortfolios = { selectedList ->
                                selectedList.forEach { portfolio ->
                                    viewModel.saveToPortfolio(portfolio.id, result.data)
                                }
                            },
                            onCreateAndAdd = { viewModel.createAndSavePortfolio(it, result.data) }
                        )
                    }

                    // DIALOG FOR REMOVING (Multiple Portfolios Case)
                    if (showRemoveDialog) {
                        AlertDialog(
                            onDismissRequest = { showRemoveDialog = false },
                            title = { Text("Remove from Portfolio") },
                            text = {
                                Column {
                                    Text("This fund is in multiple portfolios. Select which one to remove it from:")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                        items(savedInPortfolios) { pf ->
                                            ListItem(
                                                headlineContent = { Text(pf.name) },
                                                modifier = Modifier.clickable {
                                                    viewModel.removeFromPortfolio(pf.id)
                                                    showRemoveDialog = false
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showRemoveDialog = false }) { Text("Cancel") }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Keep AddToPortfolioSheet, ProductDetailsContent, NavChart, etc. from original code

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPortfolioSheet(
    portfolioResource: Resource<List<PortfolioEntity>>,
    onDismiss: () -> Unit,
    onSelectPortfolio: (PortfolioEntity) -> Unit,
    onCreateAndAdd: (String) -> Unit
) {
    var isCreatingNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isCreatingNew) "Create Portfolio" else "Add to Portfolio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isCreatingNew) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Portfolio Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        onCreateAndAdd(newName)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = newName.isNotBlank()
                ) {
                    Text("Create & Add")
                }
            } else {
                when (portfolioResource) {
                    is Resource.Loading -> {
                        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is Resource.Error -> {
                        Text("Failed to load portfolios", color = Color.Red)
                        // Auto switch to creation mode if we can't load existing ones
                        LaunchedEffect(Unit) { isCreatingNew = true }
                    }
                    is Resource.Success -> {
                        val portfolios = portfolioResource.data
                        if (portfolios.isEmpty()) {
                            // If no portfolios exist, force creation view
                            LaunchedEffect(Unit) { isCreatingNew = true }
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                items(portfolios) { pf ->
                                    ListItem(
                                        headlineContent = { Text(pf.name) },
                                        leadingContent = { Icon(Icons.Default.Favorite, null, tint = Color(0xFF6200EE)) },
                                        modifier = Modifier.clickable {
                                            onSelectPortfolio(pf)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                            TextButton(
                                onClick = { isCreatingNew = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, null)
                                Text("Create New Portfolio")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailsContent(data: FundDetailsResponse) {
    val fund = data.meta
    val history = data.data

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(fund.schemeName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(fund.fundHouse, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "₹${history.firstOrNull()?.nav ?: "0.00"}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Current NAV", color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Performance (Last 30 Days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        NavChart(navHistory = history, modifier = Modifier.fillMaxWidth().height(220.dp))

        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FE))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoItem("Category", fund.schemeCategory)
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                InfoItem("Type", fund.schemeType)
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                InfoItem("Scheme Code", fund.schemeCode.toString())
            }
        }
    }
}

@Composable
fun NavChart(navHistory: List<NavData>, modifier: Modifier = Modifier) {
    val dataPoints = remember(navHistory) {
        navHistory.take(30).reversed().mapNotNull { it.nav.toFloatOrNull() }
    }
    if (dataPoints.isEmpty()) return

    val maxNav = dataPoints.maxOrNull() ?: 0f
    val minNav = dataPoints.minOrNull() ?: 0f
    val range = maxNav - minNav

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spaceX = width / (dataPoints.size - 1)

        val points = dataPoints.mapIndexed { index, nav ->
            val x = index * spaceX
            val normalizedY = if (range != 0f) (nav - minNav) / range else 0.5f
            Offset(x, height - (normalizedY * height))
        }

        val strokePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                points.forEach { lineTo(it.x, it.y) }
            }
        }

        val fillPath = android.graphics.Path(strokePath.asAndroidPath()).asComposePath().apply {
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        drawPath(
            fillPath,
            brush = Brush.verticalGradient(listOf(Color(0xFF6200EE).copy(alpha = 0.3f), Color.Transparent))
        )
        drawPath(
            strokePath,
            color = Color(0xFF6200EE),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}