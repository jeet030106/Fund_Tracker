package com.example.fundtracker.ui.features.portfolio_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fundtracker.ui.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioListScreen(
    viewModel: PortfolioViewModel = hiltViewModel(),
    onExploreFundsClick: () -> Unit,
    onPortfolioClick: (Long, String) -> Unit
) {
    // Collect the Resource state instead of a raw list
    val portfolioState by viewModel.allPortfolios.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Portfolios") }) }
    ) { padding ->
        // Handle the Resource states
        when (val state = portfolioState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6200EE))
                }
            }

            is Resource.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Text(text = state.message, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
            }

            is Resource.Success -> {
                val portfolios = state.data
                if (portfolios.isEmpty()) {
                    EmptyPortfolioState(
                        onExploreFundsClick = onExploreFundsClick,
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                        items(portfolios) { pf ->
                            ListItem(
                                headlineContent = { Text(pf.name, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text("View saved funds") },
                                leadingContent = {
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF6200EE))
                                },
                                trailingContent = { Icon(Icons.Default.DateRange, null) },
                                modifier = Modifier.clickable { onPortfolioClick(pf.id, pf.name) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPortfolioState(
    onExploreFundsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .background(Color(0xFFE8EAF6), shape = androidx.compose.foundation.shape.CircleShape)
                .padding(24.dp),
            tint = Color(0xFF6200EE)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your Portfolio is Empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Save your first mutual fund to create a personalized watchlist.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onExploreFundsClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Explore Mutual Funds", fontWeight = FontWeight.SemiBold)
        }
    }
}