package com.homeservices.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeservices.app.data.Trade
import com.homeservices.app.data.api.dto.UserDto
import com.homeservices.app.navigation.Routes
import com.homeservices.app.ui.theme.*

data class DrawerMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val badge: String? = null
)

@Composable
fun AppDrawer(
    user: UserDto,
    onSignOut: () -> Unit,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onNavigateTrade: (Trade) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(310.dp),
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            // Profile Header Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Cream,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigate(Routes.PROFILE)
                        onCloseDrawer()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Brand),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ApplianceOrangeSoft
                        ) {
                            Text(
                                text = user.role.lowercase().replaceFirstChar(Char::uppercase),
                                style = MaterialTheme.typography.labelSmall,
                                color = Brand,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Outline)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MAIN NAVIGATION",
                style = MaterialTheme.typography.labelMedium,
                color = Muted,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // Core App Nav Items
            val mainItems = listOf(
                DrawerMenuItem(Routes.HOME, "Home Services", Icons.Rounded.Home),
                DrawerMenuItem(Routes.BOOKINGS, "My Bookings", Icons.Rounded.CalendarMonth),
                DrawerMenuItem(Routes.MESSAGES, "Messages", Icons.Rounded.ChatBubble)
            )

            mainItems.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item.title, fontWeight = FontWeight.Medium) },
                    selected = currentRoute == item.route,
                    onClick = {
                        onNavigate(item.route)
                        onCloseDrawer()
                    },
                    icon = { Icon(item.icon, contentDescription = null) },
                    badge = item.badge?.let { count ->
                        {
                            Surface(
                                shape = CircleShape,
                                color = Brand
                            ) {
                                Text(
                                    text = count,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Cream,
                        selectedIconColor = Brand,
                        selectedTextColor = Brand,
                        unselectedIconColor = Ink,
                        unselectedTextColor = Ink
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Outline)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BROWSE CATEGORIES",
                style = MaterialTheme.typography.labelMedium,
                color = Muted,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // Trades Nav Items
            Trade.entries.forEach { trade ->
                NavigationDrawerItem(
                    label = { Text(trade.plural, fontWeight = FontWeight.Normal) },
                    selected = currentRoute == "trade/${trade.name}",
                    onClick = {
                        onNavigateTrade(trade)
                        onCloseDrawer()
                    },
                    icon = { Icon(trade.icon, contentDescription = null, tint = trade.accent) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = trade.soft,
                        selectedTextColor = trade.accent,
                        unselectedTextColor = Ink
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(color = Outline)
            Spacer(modifier = Modifier.height(12.dp))

            // Settings & Sign Out
            NavigationDrawerItem(
                label = { Text("Settings") },
                selected = currentRoute == Routes.SETTINGS,
                onClick = {
                    onNavigate(Routes.SETTINGS)
                    onCloseDrawer()
                },
                icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Sign out", color = ElectricRed) },
                selected = false,
                onClick = onSignOut,
                icon = { Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null, tint = ElectricRed) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "HomeServices App v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
