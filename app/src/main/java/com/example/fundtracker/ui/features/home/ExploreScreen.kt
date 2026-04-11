package com.example.fundtracker.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fundtracker.data.model.FundMarketData
import com.example.fundtracker.data.model.FundSearchResult
import com.example.fundtracker.ui.theme.Primary

@Composable
fun ExploreScreen(
    onViewAllClick: (String) -> Unit,        // For specific categories
    onSearchClick: () -> Unit,              // For the search bar
    onGlobalViewAllClick: () -> Unit,       // NEW: For the header "View All"
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FE))) {
        // Pass the new global click handler to the header
        HeaderSection(onSearchClick = onSearchClick, onGlobalViewAll = onGlobalViewAllClick)

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF6200EE))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { FundCategory("Index Funds", state.indexFunds) { onViewAllClick("index") } }
            item { FundCategory("Bluechip Funds", state.bluechipFunds) { onViewAllClick("bluechip") } }
            item { FundCategory("Tax Saver (ELSS)", state.taxSaverFunds) { onViewAllClick("tax") } }
            item { FundCategory("Large Cap Funds", state.largeCapFunds) { onViewAllClick("large_cap") } }
        }
    }
}

@Composable
fun HeaderSection(onSearchClick: () -> Unit, onGlobalViewAll: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary) // Your theme's primary color
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MF Explorer",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Discover mutual funds by category",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // NEW: Global View All Button
            TextButton(onClick = onGlobalViewAll) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View All", color = Color.White, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar Surface
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.2f),
            onClick = onSearchClick
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search funds...", color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun FundCategory(
    title: String,
    funds: List<Pair<FundSearchResult, FundMarketData?>>,
    onViewAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onViewAll) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View All", color = Color(0xFF6200EE))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF6200EE)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            funds.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { pair ->
                        FundCard(pair.first, pair.second, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FundCard(
    fund: FundSearchResult,
    marketData: FundMarketData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(165.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(Color(0xFFF0E6FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = fund.schemeName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            Text("NAV", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text("₹${marketData?.currentNav ?: "---"}", fontWeight = FontWeight.Bold)

            marketData?.let { data ->
                val pillColor = if (data.isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                val textColor = if (data.isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                val sign = if (data.isPositive) "+" else ""

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = pillColor,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        text = "$sign${String.format("%.2f", data.dayChangePercent)}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}