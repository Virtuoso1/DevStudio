package com.homeservices.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homeservices.app.data.AppViewModel
import com.homeservices.app.data.ProData
import com.homeservices.app.data.Trade
import com.homeservices.app.ui.components.AppDrawer
import com.homeservices.app.ui.screens.*
import com.homeservices.app.ui.theme.*
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val BOOKINGS = "bookings"
    const val MESSAGES = "messages"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val TRADE = "trade/{trade}"
    const val PRO = "pro/{id}"
    const val CHAT = "chat/{id}"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val Tabs = listOf(
    Tab(Routes.HOME, "Home", Icons.Rounded.Home),
    Tab(Routes.BOOKINGS, "Bookings", Icons.Rounded.CalendarMonth),
    Tab(Routes.MESSAGES, "Messages", Icons.Rounded.ChatBubble),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeServicesApp(vm: AppViewModel = viewModel()) {
    val isAuthenticated by vm.isAuthenticated.collectAsStateWithLifecycle()
    val currentUser by vm.currentUser.collectAsStateWithLifecycle()
    val authError by vm.authError.collectAsStateWithLifecycle()
    val authLoading by vm.authLoading.collectAsStateWithLifecycle()
    val darkMode by vm.darkMode.collectAsStateWithLifecycle()

    val nav = rememberNavController()

    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = isAuthenticated && Tabs.any { it.route == route }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val bookings by vm.bookings.collectAsStateWithLifecycle()
    val chats by vm.chats.collectAsStateWithLifecycle()
    val diagnosis by vm.diagnosis.collectAsStateWithLifecycle()
    val diagnosing by vm.diagnosing.collectAsStateWithLifecycle()

    fun selectTab(target: String) {
        nav.navigate(target) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBar,
        drawerContent = {
            if (isAuthenticated) {
                AppDrawer(
                    currentRoute = route,
                    onNavigate = { target ->
                        nav.navigate(target) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateTrade = { trade ->
                        nav.navigate("trade/${trade.name}")
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Cream,
            topBar = {
                if (showBar) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "HomeServices",
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Rounded.Menu, contentDescription = "Open Navigation Menu", tint = Ink)
                            }
                        },
                        actions = {
                            IconButton(onClick = { nav.navigate(Routes.PROFILE) }) {
                                Icon(Icons.Rounded.Person, contentDescription = "User Profile", tint = Ink)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Cream
                        )
                    )
                }
            },
            bottomBar = {
                if (showBar) {
                    Column {
                        HorizontalDivider(color = Outline)
                        NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                            Tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = route == tab.route,
                                    onClick = { selectTab(tab.route) },
                                    icon = { Icon(tab.icon, contentDescription = null) },
                                    label = { Text(tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Ink,
                                        selectedTextColor = Ink,
                                        indicatorColor = ApplianceOrangeSoft,
                                        unselectedIconColor = Muted,
                                        unselectedTextColor = Muted,
                                    ),
                                )
                            }
                        }
                    }
                }
            },
        ) { inner ->
            NavHost(
                navController = nav,
                startDestination = if (isAuthenticated) Routes.HOME else Routes.LOGIN,
                modifier = Modifier
                    .padding(inner)
                    .consumeWindowInsets(inner),
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLogin = { name, email, pass ->
                            vm.login(name, email, pass) {
                                nav.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToRegister = { nav.navigate(Routes.REGISTER) { launchSingleTop = true } },
                        isLoading = authLoading,
                        errorMessage = authError
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onRegister = { name, email, phone, pass, role, trade, bio, yearsExp ->
                            vm.register(name, email, phone, pass, role, trade, bio, yearsExp) {
                                nav.navigate(Routes.HOME) {
                                    popUpTo(Routes.REGISTER) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToLogin = { nav.popBackStack() },
                        isLoading = authLoading,
                        errorMessage = authError
                    )
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        userName = currentUser?.name ?: "User",
                        diagnosing = diagnosing,
                        diagnosis = diagnosis,
                        onSearchSubmit = vm::diagnose,
                        onClearDiagnosis = vm::clearDiagnosis,
                        onTrade = { nav.navigate("trade/${it.name}") },
                        onPro = { nav.navigate("pro/${it.id}") },
                    )
                }

                composable(
                    Routes.TRADE,
                    arguments = listOf(navArgument("trade") { type = NavType.StringType }),
                ) { entry ->
                    val trade = entry.arguments?.getString("trade")
                        ?.let { name -> Trade.entries.firstOrNull { it.name == name } }
                    if (trade != null) {
                        TradeListScreen(
                            trade = trade,
                            pros = ProData.byTrade(trade),
                            onBack = { nav.popBackStack() },
                            onPro = { nav.navigate("pro/${it.id}") },
                        )
                    }
                }

                composable(
                    Routes.PRO,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val pro = entry.arguments?.getString("id")?.let(ProData::byId)
                    if (pro != null) {
                        ProProfileScreen(
                            pro = pro,
                            onBack = { nav.popBackStack() },
                            onMessage = {
                                vm.openChat(pro.id)
                                nav.navigate("chat/${pro.id}")
                            },
                            onConfirmBooking = { date, time, note -> vm.book(pro, date, time, note) },
                            onViewBookings = {
                                nav.navigate(Routes.BOOKINGS) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }

                composable(
                    Routes.CHAT,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val pro = entry.arguments?.getString("id")?.let(ProData::byId)
                    if (pro != null) {
                        LaunchedEffect(pro.id) { vm.openChat(pro.id) }
                        ChatScreen(
                            pro = pro,
                            messages = chats[pro.id].orEmpty(),
                            onBack = { nav.popBackStack() },
                            onSend = { vm.send(pro.id, it) },
                        )
                    }
                }

                composable(Routes.BOOKINGS) {
                    BookingsScreen(
                        bookings = bookings,
                        onCancel = vm::cancelBooking,
                        onFindPro = { selectTab(Routes.HOME) },
                    )
                }

                composable(Routes.MESSAGES) {
                    MessagesScreen(
                        chats = chats,
                        onOpen = { nav.navigate("chat/${it.id}") },
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        darkMode = darkMode,
                        onDarkModeChanged = vm::setDarkMode,
                        onBack = { nav.popBackStack() }
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        user = currentUser,
                        onLogout = {
                            vm.logout()
                            try {
                                nav.navigate(Routes.LOGIN) {
                                    popUpTo(nav.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (_: Exception) {
                                nav.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onBack = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}
