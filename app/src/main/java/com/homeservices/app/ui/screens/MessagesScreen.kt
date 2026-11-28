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
import com.homeservices.app.data.Conversation
import com.homeservices.app.ui.components.Avatar
import com.homeservices.app.ui.components.EmptyState
import com.homeservices.app.ui.theme.Muted
import com.homeservices.app.ui.theme.Outline

@Composable
fun MessagesScreen(
    conversations: List<Conversation>,
    onOpen: (Conversation) -> Unit,
) {

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
                items(conversations, key = { it.id }) { conversation ->
                    val last = conversation.last
                    Surface(
                        onClick = { onOpen(conversation) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        border = BorderStroke(1.dp, Outline),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            conversation.trade?.let { Avatar(conversation.initials, it) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(conversation.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    last?.let { (if (it.fromMe) "You: " else "") + it.text } ?: "No messages yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(last?.time.orEmpty(), style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                }
            }
        }
    }
}
