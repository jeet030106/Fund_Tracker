package com.example.fundtracker.ui.features

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPortfolioSheet(
    portfolios: List<PortfolioEntity>,
    onDismiss: () -> Unit,
    onSelectPortfolio: (PortfolioEntity) -> Unit,
    onCreateAndAdd: (String) -> Unit
) {
    var isCreatingNew by remember { mutableStateOf(portfolios.isEmpty()) }
    var newPortfolioName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = if (isCreatingNew) "Create Portfolio" else "Add to Portfolio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isCreatingNew) {
                // Create Mode
                OutlinedTextField(
                    value = newPortfolioName,
                    onValueChange = { newPortfolioName = it },
                    label = { Text("Portfolio Name (e.g., Retirement)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = { onCreateAndAdd(newPortfolioName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = newPortfolioName.isNotBlank()
                ) {
                    Text("Create & Add Fund")
                }

                if (portfolios.isNotEmpty()) {
                    TextButton(
                        onClick = { isCreatingNew = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Back to list")
                    }
                }
            } else {
                // List Mode
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(portfolios) { portfolio ->
                        ListItem(
                            headlineContent = { Text(portfolio.name) },
                            leadingContent = {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF6200EE))
                            },
                            modifier = Modifier.clickable { onSelectPortfolio(portfolio) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                TextButton(
                    onClick = { isCreatingNew = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create New Portfolio")
                }
            }
        }
    }
}