package com.example.fundtracker.ui.features.product_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fundtracker.ui.utils.Resource

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
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            when (val result = state) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(result.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
                is Resource.Success -> {
                    val fund = result.data.meta
                    val history = result.data.data

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(fund.schemeName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(fund.fundHouse, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(24.dp))

                        // NAV Section
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("₹${history.firstOrNull()?.nav ?: "0.00"}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Current NAV", color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Placeholder for Chart
                        Card(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FE))
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("Interactive Chart Component Here", color = Color.LightGray)
                                // In a real app, use a LineChart here
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Stats Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoItem("Category", fund.schemeCategory)
                            InfoItem("Type", fund.schemeType)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}