package com.example.fundtracker.ui.features

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    portfolioResource: Resource<List<PortfolioEntity>>, // Updated parameter type
    onDismiss: () -> Unit,
    onSelectPortfolio: (PortfolioEntity) -> Unit,
    onCreateAndAdd: (String) -> Unit
) {
    var isCreatingNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                text = if (isCreatingNew) "Create New Portfolio" else "Add to Portfolio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
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
            } else {
                // Handle Resource State inside the Sheet
                when (portfolioResource) {
                    is Resource.Loading -> {
                        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is Resource.Error -> {
                        Text("Error loading portfolios", color = Color.Red)
                        isCreatingNew = true // Fallback to creation
                    }
                    is Resource.Success -> {
                        val portfolios = portfolioResource.data
                        if (portfolios.isEmpty()) {
                            // Force creation if none exist
                            isCreatingNew = true
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                items(portfolios) { pf ->
                                    ListItem(
                                        headlineContent = { Text(pf.name) },
                                        leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF6200EE)) },
                                        modifier = Modifier.clickable { onSelectPortfolio(pf); onDismiss() }
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isCreatingNew) {
                    TextButton(
                        onClick = { isCreatingNew = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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