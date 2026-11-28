package com.homeservices.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.homeservices.app.data.Conversation
import com.homeservices.app.data.ServicesSession
import com.homeservices.app.data.Pro
import com.homeservices.app.data.ChatMessage
import com.homeservices.app.data.api.dto.AuthResponse
import com.homeservices.app.data.api.dto.ReviewDto
import kotlinx.coroutines.delay
import com.homeservices.app.data.Trade
import com.homeservices.app.ui.components.AppDrawer
import com.homeservices.app.ui.screens.*
import com.homeservices.app.ui.theme.*
import kotlinx.coroutines.launch


object Routes {
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
    val session by vm.session.collectAsStateWithLifecycle()
    val busy by vm.authBusy.collectAsStateWithLifecycle()
    val error by vm.authError.collectAsStateWithLifecycle()
    val current = session
    if (current == null) {
        AuthScreen(busy, error, vm::login, vm::register, vm::clearAuthError)
    } else {
        key(current.token) {
            SignedInApp(current, vm::signOut, vm::expireSession)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignedInApp(session: AuthResponse, onSignOut: () -> Unit, onExpired: () -> Unit) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = Tabs.any { it.route == route }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val services = remember(session) { ServicesSession(session, scope, onExpired) }
    val bookings by services.bookings.collectAsStateWithLifecycle()
    val conversations by services.conversations.collectAsStateWithLifecycle()
    val repeatProviders by services.bookAgain.collectAsStateWithLifecycle()
    val error by services.error.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var openedConversation by remember { mutableStateOf<Conversation?>(null) }

    LaunchedEffect(error) {
        error?.let {
            snackbar.showSnackbar(it, withDismissAction = true)
            services.clearError()
        }
    }

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
            AppDrawer(
                user = session.user,
                onSignOut = onSignOut,
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
    ) {
        Scaffold(
            containerColor = Cream,
            snackbarHost = { SnackbarHost(snackbar) },
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
                startDestination = Routes.HOME,
                modifier = Modifier
                    .padding(inner)
                    .consumeWindowInsets(inner),
            ) {
                composable(Routes.HOME) {
                    var query by rememberSaveable { mutableStateOf("") }
                    var results by remember { mutableStateOf<List<Pro>?>(null) }
                    var searching by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { services.loadBookAgain() }
                    LaunchedEffect(query) {
                        results = null
                        searching = query.isNotBlank()
                        if (searching) {
                            delay(250)
                            val trade = Trade.entries.firstOrNull {
                                query.equals(it.name, true) || query.equals(it.label, true) || query.equals(it.plural, true)
                            }
                            results = services.request { services.repository.providers(trade, query.takeIf { trade == null }) }
                            searching = false
                        }
                    }
                    HomeScreen(
                        userName = session.user.name,
                        repeatProviders = repeatProviders,
                        searchResults = results,
                        searching = searching,
                        onSearch = { query = it },
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
                        var providers by remember { mutableStateOf<List<Pro>?>(null) }
                        LaunchedEffect(trade) {
                            providers = services.request { services.repository.providers(trade) }.orEmpty()
                        }
                        val list = providers
                        if (list == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else TradeListScreen(
                            trade = trade,
                            pros = list,
                            onBack = { nav.popBackStack() },
                            onPro = { nav.navigate("pro/${it.id}") },
                        )
                    }
                }

                composable(
                    Routes.PRO,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id") ?: return@composable
                    var provider by remember { mutableStateOf<Pro?>(null) }
                    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
                    var loading by remember { mutableStateOf(true) }
                    var openingChat by remember { mutableStateOf(false) }
                    val screenScope = rememberCoroutineScope()
                    LaunchedEffect(id) {
                        provider = services.request { services.repository.provider(id) }
                        loading = false
                        if (provider != null) reviews = services.request { services.repository.reviews(id) }.orEmpty()
                    }
                    val pro = provider
                    if (pro != null) {
                        ProProfileScreen(
                            pro = pro,
                            canBook = !services.isProvider,
                            reviews = reviews,
                            onBack = { nav.popBackStack() },
                            onMessage = {
                                if (!openingChat) {
                                    openingChat = true
                                    screenScope.launch {
                                        try {
                                            services.startChat(pro)?.let {
                                                openedConversation = it
                                                nav.navigate("chat/${it.id}")
                                            }
                                        } finally {
                                            openingChat = false
                                        }
                                    }
                                }
                            },
                            onConfirmBooking = { date, time, note -> services.book(pro, date, time, note) },
                            onViewBookings = {
                                nav.navigate(Routes.BOOKINGS) {
                                    popUpTo(Routes.HOME)
                                    launchSingleTop = true
                                }
                            },
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (loading) CircularProgressIndicator()
                            else TextButton(onClick = { nav.popBackStack() }) { Text("Back to providers") }
                        }
                    }
                }

                composable(
                    Routes.CHAT,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id") ?: return@composable
                    val conversation = openedConversation?.takeIf { it.id == id }
                        ?: conversations.firstOrNull { it.id == id }
                    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
                    var loaded by remember { mutableStateOf(false) }
                    LaunchedEffect(id) {
                        if (conversation == null) services.loadConversations()
                        var next = services.messages(id)
                        loaded = true
                        while (next != null) {
                            messages = (messages + next).distinctBy { it.id }
                            delay(3_000)
                            next = services.messages(id)
                        }
                    }
                    if (conversation != null) {
                        ChatScreen(
                            conversation = conversation,
                            messages = messages,
                            onBack = { nav.popBackStack() },
                            onSend = { text ->
                                val sent = services.send(id, text)
                                if (sent != null) messages = (messages + sent).distinctBy { it.id }
                                sent != null
                            },
                        )
                    } else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (!loaded) CircularProgressIndicator()
                        else TextButton(onClick = { nav.popBackStack() }) { Text("Back to messages") }
                    }
                }

                composable(Routes.BOOKINGS) {
                    LaunchedEffect(Unit) { services.loadBookings() }
                    BookingsScreen(
                        bookings = bookings,
                        isProvider = services.isProvider,
                        onStatus = services::updateBooking,
                        onReview = services::review,
                        onFindPro = { selectTab(Routes.HOME) },
                    )
                }

                composable(Routes.MESSAGES) {
                    LaunchedEffect(Unit) { services.loadConversations() }
                    MessagesScreen(
                        conversations = conversations,
                        onOpen = {
                            openedConversation = it
                            nav.navigate("chat/${it.id}")
                        },
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { nav.popBackStack() },
                        onProfile = { nav.navigate(Routes.PROFILE) },
                        onSignOut = onSignOut,
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        user = session.user,
                        onSignOut = onSignOut,
                        onBack = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}
