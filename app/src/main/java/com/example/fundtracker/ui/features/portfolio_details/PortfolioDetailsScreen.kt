package com.example.fundtracker.ui.features.portfolio_details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fundtracker.ui.features.portfolio_list.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioDetailsScreen(
    portfolioId: Long,
    portfolioName: String,
    viewModel: PortfolioViewModel = hiltViewModel(),
    onFundClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val data by viewModel.selectedPortfolio.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // NEW: Reset the sync flag when the user leaves the screen
    DisposableEffect(portfolioId) {
        onDispose {
            viewModel.resetSyncFlag()
        }
    }

    // Load data when screen opens
    LaunchedEffect(portfolioId) {
        viewModel.loadPortfolioDetails(portfolioId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(portfolioName) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                    }
                }
            )
        }
    ) { padding ->
        data?.let { portfolioWithFunds ->
            if (portfolioWithFunds.funds.isEmpty()) {
                // Optional: Handle empty state
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No funds in this portfolio", color = Color.Gray)
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(portfolioWithFunds.funds) { fund ->
                        ListItem(
                            headlineContent = { Text(fund.schemeName, maxLines = 1) },
                            supportingContent = { Text(fund.category) },
                            trailingContent = {
                                Text(
                                    "₹${fund.lastNav}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.clickable { onFundClick(fund.schemeCode) }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}