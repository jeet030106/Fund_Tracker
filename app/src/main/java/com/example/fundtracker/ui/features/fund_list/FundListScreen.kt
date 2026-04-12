package com.example.fundtracker.ui.features.fund_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fundtracker.data.model.FundMarketData
import com.example.fundtracker.data.model.FundSearchResult
import com.example.fundtracker.ui.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundListScreen(
    title: String,
    viewModel: FundListViewModel = hiltViewModel(),
    onFundClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(start = padding.calculateStartPadding(
            LayoutDirection.Ltr),
            end = padding.calculateEndPadding(LayoutDirection.Ltr),
            top = 4.dp, // Manually set a smaller top gap
            bottom = padding.calculateBottomPadding()).background(Color(0xFFF8F9FE))) {
            when (val result = state) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(result.data) { (fund, marketData) ->
                            FullWidthFundCard(
                                fund = fund,
                                marketData = marketData,
                                onClick = { onFundClick(fund.schemeCode) }
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    Text(result.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun FullWidthFundCard(
    fund: FundSearchResult,
    marketData: FundMarketData?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFF0E6FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color(0xFF6200EE))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Code
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fund.schemeName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Code: ${fund.schemeCode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            // Price Section
            Column(horizontalAlignment = Alignment.End) {
                if (marketData != null) {
                    Text(
                        text = "₹${marketData.currentNav}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    // Show a tiny loader while the background fetch happens
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.LightGray
                    )
                }
            }

        }
    }
}