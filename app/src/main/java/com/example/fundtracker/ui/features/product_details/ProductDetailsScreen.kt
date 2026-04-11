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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fundtracker.data.model.FundDetailsResponse
import com.example.fundtracker.data.model.NavData
import com.example.fundtracker.data.room.PortfolioEntity
import com.example.fundtracker.ui.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    viewModel: ProductDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val portfolios by viewModel.portfolios.collectAsState()
    var showSheet by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { showSheet = true }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Save to Portfolio")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color.White)) {
            when (val result = state) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(
                    text = result.message ?: "Unknown Error",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
                is Resource.Success -> {
                    ProductDetailsContent(result.data)

                    if (showSheet) {
                        AddToPortfolioSheet(
                            portfolios = portfolios,
                            onDismiss = { showSheet = false },
                            onSelectPortfolio = { viewModel.saveToPortfolio(it.id, result.data) },
                            onCreateAndAdd = { viewModel.createAndSavePortfolio(it, result.data) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPortfolioSheet(
    portfolios: List<PortfolioEntity>,
    onDismiss: () -> Unit,
    onSelectPortfolio: (PortfolioEntity) -> Unit,
    onCreateAndAdd: (String) -> Unit
) {
    var isCreatingNew by remember { mutableStateOf(portfolios.isEmpty()) }
    var newName by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 24.dp)) {
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
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onCreateAndAdd(newName); onDismiss() },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = newName.isNotBlank()
                ) { Text("Create & Add") }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(portfolios) { pf ->
                        ListItem(
                            headlineContent = { Text(pf.name) },
                            leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF6200EE)) },
                            modifier = Modifier.clickable { onSelectPortfolio(pf); onDismiss() }
                        )
                    }
                }
                TextButton(onClick = { isCreatingNew = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Create New Portfolio")
                }
            }
        }
    }
}

@Composable
fun ProductDetailsContent(data: FundDetailsResponse) {
    val fund = data.meta
    val history = data.data

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(fund.schemeName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(fund.fundHouse, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text("₹${history.firstOrNull()?.nav ?: "0.00"}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Current NAV", color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Performance (Last 30 Days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        NavChart(navHistory = history, modifier = Modifier.fillMaxWidth().height(220.dp))

        Spacer(modifier = Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FE))) {
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

        drawPath(fillPath, brush = Brush.verticalGradient(listOf(Color(0xFF6200EE).copy(alpha = 0.3f), Color.Transparent)))
        drawPath(strokePath, color = Color(0xFF6200EE), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}