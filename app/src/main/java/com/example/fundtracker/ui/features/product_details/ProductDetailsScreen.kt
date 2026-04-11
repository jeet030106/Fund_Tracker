package com.example.fundtracker.ui.features.product_details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.fundtracker.data.model.NavData
import com.example.fundtracker.ui.utils.Resource
import kotlin.collections.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    viewModel: ProductDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

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
                    IconButton(onClick = { /* Add to Watchlist Logic */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Watchlist")
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
                    val fund = result.data.meta
                    val history = result.data.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = fund.schemeName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fund.fundHouse,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // NAV Section
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${history.firstOrNull()?.nav ?: "0.00"}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Current NAV",
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Real Integrated Chart
                        Text(
                            text = "Performance (Last 30 Days)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        NavChart(
                            navHistory = history,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Stats Grid
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
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun NavChart(
    navHistory: List<NavData>,
    modifier: Modifier = Modifier
) {
    // Process data: Take 30 points, reverse for L-to-R chronological order
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
            // Normalize Y so the chart isn't a flat line at the top
            val normalizedY = if (range != 0f) (nav - minNav) / range else 0.5f
            val y = height - (normalizedY * height)
            Offset(x, y)
        }

        val strokePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                points.forEach { lineTo(it.x, it.y) }
            }
        }

        // 1. Draw Gradient Fill
        val fillPath = android.graphics.Path(strokePath.asAndroidPath()).asComposePath()
        fillPath.lineTo(points.last().x, height)
        fillPath.lineTo(points.first().x, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF6200EE).copy(alpha = 0.3f), Color.Transparent)
            )
        )

        // 2. Draw the Line
        drawPath(
            path = strokePath,
            color = Color(0xFF6200EE),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}