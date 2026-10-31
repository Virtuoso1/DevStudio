package com.homeservices.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.homeservices.app.data.Pro
import com.homeservices.app.data.ProData
import com.homeservices.app.data.Trade
import com.homeservices.app.ui.components.EmptyState
import com.homeservices.app.ui.components.ProCard
import com.homeservices.app.ui.components.SectionTitle
import com.homeservices.app.ui.theme.Brand
import com.homeservices.app.ui.theme.Muted
import com.homeservices.app.ui.theme.Outline
import java.time.LocalTime

private fun greeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
fun HomeScreen(
    userName: String,
    onTrade: (Trade) -> Unit,
    onPro: (Pro) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) { ProData.search(query) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(greeting(), style = MaterialTheme.typography.bodyMedium, color = Muted)
                Text(
                    "What needs fixing,\n$userName?",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search a service or professional", color = Muted) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Muted) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Brand,
                    unfocusedBorderColor = Outline,
                ),
            )
        }

        if (query.isBlank()) {
            item { SectionTitle("Browse by trade") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Trade.entries.forEach { trade ->
                        TradeTile(trade, onClick = { onTrade(trade) }, modifier = Modifier.weight(1f))
                    }
                }
            }
            item { SectionTitle("Book again near you") }
            items(ProData.bookAgain, key = { it.id }) { pro ->
                ProCard(pro, onClick = { onPro(pro) })
            }
        } else if (results.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = "No matches",
                    message = "Try a trade like \"plumber\" or a name.",
                )
            }
        } else {
            item { SectionTitle("${results.size} result${if (results.size == 1) "" else "s"}") }
            items(results, key = { it.id }) { pro ->
                ProCard(pro, onClick = { onPro(pro) })
            }
        }
    }
}

@Composable
private fun TradeTile(trade: Trade, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Outline),
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(trade.accent)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(trade.soft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(trade.icon, contentDescription = null, tint = trade.accent, modifier = Modifier.size(24.dp))
                }
                Text(trade.tileLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
