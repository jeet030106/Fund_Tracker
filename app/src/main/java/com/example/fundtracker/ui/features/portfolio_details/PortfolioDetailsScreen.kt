package com.example.fundtracker.ui.features.portfolio_details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fundtracker.ui.features.portfolio_list.PortfolioViewModel
import com.example.fundtracker.ui.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioDetailsScreen(
    portfolioId: Long,
    portfolioName: String,
    viewModel: PortfolioViewModel = hiltViewModel(),
    onFundClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    // Collecting the Resource state
    val portfolioState by viewModel.selectedPortfolio.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    DisposableEffect(portfolioId) {
        onDispose {
            viewModel.resetSyncFlag()
        }
    }

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
                    // This loader only shows for background API syncing
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
        // Handle the Resource states explicitly
        when (val state = portfolioState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(text = state.message, color = Color.Gray, textAlign = TextAlign.Center)
                    TextButton(onClick = onBack) { Text("Go Back") }
                }
            }

            is Resource.Success -> {
                val portfolioWithFunds = state.data

                if (portfolioWithFunds.funds.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
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
                                        color = Color(0xFF4CAF50) // Green color for NAV
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
}