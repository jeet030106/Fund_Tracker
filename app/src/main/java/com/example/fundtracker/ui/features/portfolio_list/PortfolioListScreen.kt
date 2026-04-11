package com.example.fundtracker.ui.features.portfolio_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioListScreen(
    viewModel: PortfolioViewModel = hiltViewModel(),
    onPortfolioClick: (Long, String) -> Unit
) {
    val portfolios by viewModel.allPortfolios.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Portfolios") }) }
    ) { padding ->
        if (portfolios.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No portfolios yet. Save a fund to get started!", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(portfolios) { pf ->
                    ListItem(
                        headlineContent = { Text(pf.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("View saved funds") },
                        leadingContent = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF6200EE)) },
                        trailingContent = { Icon(Icons.Default.PlayArrow, null) },
                        modifier = Modifier.clickable { onPortfolioClick(pf.id, pf.name) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}