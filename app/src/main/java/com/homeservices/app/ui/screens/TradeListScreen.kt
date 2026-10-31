package com.homeservices.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.homeservices.app.data.Pro
import com.homeservices.app.data.Trade
import com.homeservices.app.ui.components.BackButton
import com.homeservices.app.ui.components.ProCard
import com.homeservices.app.ui.theme.Muted

@Composable
fun TradeListScreen(
    trade: Trade,
    pros: List<Pro>,
    onBack: () -> Unit,
    onPro: (Pro) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        BackButton(onClick = onBack, modifier = Modifier.padding(start = 20.dp, top = 12.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Text("${trade.plural} Near You", style = MaterialTheme.typography.headlineMedium)
                    Box(
                        Modifier
                            .padding(top = 8.dp, bottom = 6.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(trade.accent)
                    )
                    Text("${pros.size} listed", style = MaterialTheme.typography.bodyMedium, color = Muted)
                }
            }
            items(pros, key = { it.id }) { pro ->
                ProCard(pro, onClick = { onPro(pro) })
            }
        }
    }
}
