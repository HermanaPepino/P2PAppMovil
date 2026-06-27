package com.example.p2pappmovil.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.p2pappmovil.presentation.admin.AdminScreen
import com.example.p2pappmovil.presentation.admindetail.AdminDetailScreen
import com.example.p2pappmovil.presentation.admin.SupportTicketsScreen
import com.example.p2pappmovil.presentation.admin.SupportTicketDetailScreen
import com.example.p2pappmovil.presentation.dispute.DisputeScreen
import com.example.p2pappmovil.presentation.filters.FilterOffersScreen
import com.example.p2pappmovil.presentation.history.HistoryScreen
import com.example.p2pappmovil.presentation.login.LoginScreen
import com.example.p2pappmovil.presentation.marketplace.MarketplaceScreen
import com.example.p2pappmovil.presentation.notifications.NotificationsScreen
import com.example.p2pappmovil.presentation.operationdetail.OperationDetailScreen
import com.example.p2pappmovil.presentation.operationresume.OperationResumeScreen
import com.example.p2pappmovil.presentation.profile.ProfileScreen
import com.example.p2pappmovil.presentation.publishoffer.PublishOfferScreen
import com.example.p2pappmovil.presentation.push.PushInfoScreen
import com.example.p2pappmovil.presentation.rating.RatingScreen
import com.example.p2pappmovil.presentation.register.RegisterScreen
import com.example.p2pappmovil.presentation.splash.SplashWelcomeScreen
import com.example.p2pappmovil.presentation.startoperation.StartOperationScreen
import com.example.p2pappmovil.presentation.support.ChatSupportScreen
import com.example.p2pappmovil.presentation.support.SupportTicketHistoryScreen
import com.example.p2pappmovil.presentation.voucher.VoucherScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showFabRoutes = listOf("marketplace")

    Scaffold(
        floatingActionButton = {
            if (currentRoute in showFabRoutes) {
                FloatingActionButton(
                    onClick = { navController.navigate("chatSupport") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = "Soporte")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splashWelcome",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splashWelcome") {
                SplashWelcomeScreen(
                    onLoginClick = { navController.navigate("login") },
                    onRegisterClick = { navController.navigate("register") },
                    onAutoLoginSuccess = { rol ->
                        if (rol == "ADMIN") {
                            navController.navigate("admin") {
                                popUpTo("splashWelcome") { inclusive = true }
                            }
                        } else {
                            navController.navigate("marketplace") {
                                popUpTo("splashWelcome") { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onLoginClick = { navController.navigate("login") }
                )
            }

            composable("login") {
                LoginScreen(
                    onLoginSuccess = { 
                        navController.navigate("marketplace") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onAdminLoginSuccess = { 
                        navController.navigate("admin") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate("register") }
                )
            }

            composable("marketplace") {
                MarketplaceScreen(
                    navController = navController,
                    onFilterClick = { navController.navigate("filters") },
                    onPublishOfferClick = { navController.navigate("publishOffer") },
                    onOfferClick = { id -> navController.navigate("startOperation/$id") },
                    onNotificationsClick = { navController.navigate("notifications") },
                    onHistoryClick = { navController.navigate("history") },
                    onLogoutClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("splashWelcome") {
                            popUpTo("marketplace") { inclusive = true }
                        }
                    }
                )
            }

            composable("filters") {
                FilterOffersScreen(
                    onApplyFilters = { cGive, cRec, min, max, verified ->
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set("currencyGive", cGive)
                            set("currencyReceive", cRec)
                            set("minAmount", min)
                            set("maxAmount", max)
                            set("onlyVerified", verified)
                        }
                        navController.popBackStack()
                    },
                    onClearFilters = {
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set("currencyGive", "")
                            set("currencyReceive", "")
                            set("minAmount", null)
                            set("maxAmount", null)
                            set("onlyVerified", false)
                        }
                        navController.popBackStack()
                    },
                    onCloseClick = { navController.popBackStack() }
                )
            }

            composable("publishOffer") {
                PublishOfferScreen(
                    onPublishSuccess = { navController.navigate("marketplace") },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                ProfileScreen(
                    userId = userId,
                    onOperateClick = { navController.navigate("startOperation") },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("startOperation/{offerId}") { backStackEntry ->
                val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
                StartOperationScreen(
                    offerId = offerId,
                    onConfirmOperation = { txId -> navController.navigate("operationResume/$txId") },
                    onProfileClick = { userId -> navController.navigate("profile/$userId") },
                    onBackClick = { navController.navigate("marketplace") }
                )
            }

            composable("operationResume/{transactionId}") { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("transactionId") ?: ""
                OperationResumeScreen(
                    transactionId = txId,
                    onUploadVoucherClick = { navController.navigate("voucher/$txId") },
                    onBackClick = { navController.navigate("marketplace") }
                )
            }

            composable("voucher/{transactionId}") { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("transactionId") ?: ""
                VoucherScreen(
                    transactionId = txId,
                    onVoucherSent = { navController.navigate("history") },
                    onBackClick = { navController.navigate("operationResume/$txId") }
                )
            }

            composable("history") {
                HistoryScreen(
                    onOperationClick = { txId -> navController.navigate("operationDetail/$txId") },
                    onBackClick = { navController.navigate("marketplace") }
                )
            }

            composable("operationDetail/{transactionId}") { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("transactionId") ?: ""
                OperationDetailScreen(
                    transactionId = txId,
                    onRateUserClick = { uId -> navController.navigate("rating/$txId/$uId") },
                    onReportProblemClick = { navController.navigate("dispute/$txId") },
                    onBackClick = { navController.navigate("history") }
                )
            }

            composable("rating/{transactionId}/{userId}") { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("transactionId") ?: ""
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                RatingScreen(
                    transactionId = txId,
                    targetUserId = userId,
                    onRatingSent = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("dispute/{transactionId}") { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("transactionId") ?: ""
                DisputeScreen(
                    transactionId = txId,
                    onDisputeSent = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("notifications") {
                NotificationsScreen(
                    onBackClick = { navController.navigate("marketplace") },
                    onNotificationClick = { type, id ->
                        if (id != null) {
                            when (type) {
                                "TRANSACTION", "DISPUTE" -> navController.navigate("operationDetail/$id")
                                "SUPPORT_REPLY" -> navController.navigate("chatSupport")
                                else -> {}
                            }
                        }
                    }
                )
            }

            composable("pushInfo") {
                PushInfoScreen(
                    onBackClick = { navController.navigate("admin") }
                )
            }

            composable("admin") {
                AdminScreen(
                    onUserDetailClick = { txId -> navController.navigate("adminDetail/$txId") },
                    onDisputeDetailClick = { txId -> navController.navigate("adminDetail/$txId") },
                    onSupportRequestsClick = { navController.navigate("adminSupportTickets") },
                    onBackClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("admin") { inclusive = true }
                        }
                    }
                )
            }

            composable("adminDetail/{transactionId}") { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("transactionId") ?: ""
                AdminDetailScreen(
                    transactionId = txId,
                    onBackClick = { navController.navigate("admin") }
                )
            }

            // Rutas de Soporte (Mantenidas de master)
            composable("chatSupport") {
                ChatSupportScreen(
                    onBackClick = { navController.popBackStack() },
                    onHistoryClick = { navController.navigate("supportHistory") }
                )
            }

            composable("supportHistory") {
                SupportTicketHistoryScreen(
                    onTicketClick = { ticketId -> navController.navigate("adminSupportDetail/$ticketId") },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("adminSupportTickets") {
                SupportTicketsScreen(
                    onTicketClick = { ticketId -> navController.navigate("adminSupportDetail/$ticketId") },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("adminSupportDetail/{ticketId}") { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                SupportTicketDetailScreen(
                    ticketId = ticketId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}