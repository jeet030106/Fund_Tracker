package com.example.fundtracker.ui.features.global_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalFundListScreen(
    viewModel: GlobalFundListViewModel = hiltViewModel(),
    onFundClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val funds by viewModel.filteredFunds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Mutual Funds", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // --- CATEGORY DROPDOWN ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                viewModel.setCategory(category)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- MAIN LIST CONTENT ---
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(funds) { fund ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = fund.schemeName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = { Text("Code: ${fund.schemeCode}") },
                            trailingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            },
                            modifier = Modifier.clickable { onFundClick(fund.schemeCode) }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }

                    if (!isLoading && funds.isEmpty()) {
                        item {
                            Text(
                                "No funds found.",
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}