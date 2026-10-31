package com.homeservices.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.homeservices.app.data.ChatMessage
import com.homeservices.app.data.Pro
import com.homeservices.app.data.ProData
import com.homeservices.app.ui.components.Avatar
import com.homeservices.app.ui.components.EmptyState
import com.homeservices.app.ui.theme.Muted
import com.homeservices.app.ui.theme.Outline

@Composable
fun MessagesScreen(
    chats: Map<String, List<ChatMessage>>,
    onOpen: (Pro) -> Unit,
) {
    val conversations = chats.entries.toList().reversed().mapNotNull { (id, msgs) ->
        ProData.byId(id)?.let { Triple(it, msgs.last(), msgs.size) }
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Messages",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
        )
        if (conversations.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.ChatBubble,
                title = "No conversations yet",
                message = "Tap Message on a professional's profile to ask a question before you book.",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(conversations, key = { it.first.id }) { (pro, last, _) ->
                    Surface(
                        onClick = { onOpen(pro) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        border = BorderStroke(1.dp, Outline),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(pro.initials, pro.trade)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(pro.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    (if (last.fromMe) "You: " else "") + last.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(last.time, style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                }
            }
        }
    }
}
