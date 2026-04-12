package com.example.fundtracker.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fundtracker.data.room.PortfolioEntity
import com.example.fundtracker.ui.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPortfolioSheet(
    portfolioResource: Resource<List<PortfolioEntity>>,
    onDismiss: () -> Unit,
    // Updated to handle multiple selections
    onSelectPortfolios: (List<PortfolioEntity>) -> Unit,
    onCreateAndAdd: (String) -> Unit
) {
    var isCreatingNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    // Track selected portfolios locally in the sheet
    val selectedPortfolios = remember { mutableStateListOf<PortfolioEntity>() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCreatingNew) "Create New Portfolio" else "Add to Portfolios",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Show "Add" button only if we have selections and aren't in "Create" mode
                if (!isCreatingNew && selectedPortfolios.isNotEmpty()) {
                    TextButton(onClick = {
                        onSelectPortfolios(selectedPortfolios.toList())
                        onDismiss()
                    }) {
                        Text("Add (${selectedPortfolios.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isCreatingNew) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Portfolio Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { onCreateAndAdd(newName); onDismiss() },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = newName.isNotBlank()
                ) { Text("Create & Add Fund") }

                TextButton(
                    onClick = { isCreatingNew = false },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }

            } else {
                when (portfolioResource) {
                    is Resource.Loading -> {
                        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is Resource.Error -> {
                        Text("Error loading portfolios", color = Color.Red)
                        LaunchedEffect(Unit) { isCreatingNew = true }
                    }
                    is Resource.Success -> {
                        val portfolios = portfolioResource.data
                        if (portfolios.isEmpty()) {
                            LaunchedEffect(Unit) { isCreatingNew = true }
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                                items(portfolios) { pf ->
                                    val isSelected = selectedPortfolios.contains(pf)

                                    ListItem(
                                        headlineContent = { Text(pf.name) },
                                        leadingContent = {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Add,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                            )
                                        },
                                        trailingContent = {
                                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF6200EE).copy(alpha = 0.5f))
                                        },
                                        modifier = Modifier.clickable {
                                            if (isSelected) selectedPortfolios.remove(pf)
                                            else selectedPortfolios.add(pf)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isCreatingNew) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    TextButton(
                        onClick = { isCreatingNew = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Create New Portfolio")
                    }
                }
            }
        }
    }
}