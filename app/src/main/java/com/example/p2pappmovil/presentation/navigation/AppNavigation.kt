package com.example.p2pappmovil.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.p2pappmovil.presentation.admin.AdminScreen
import com.example.p2pappmovil.presentation.admindetail.AdminDetailScreen
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
import com.example.p2pappmovil.presentation.voucher.VoucherScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splashWelcome",
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
                onFilterClick = { navController.navigate("filters") },
                onPublishOfferClick = { navController.navigate("publishOffer") },
                onOfferClick = { navController.navigate("startOperation") },
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
                onApplyFilters = { navController.popBackStack() },
                onClearFilters = { /* No navega, limpia localmente */ },
                onCloseClick = { navController.popBackStack() }
            )
        }

        composable("publishOffer") {
            PublishOfferScreen(
                onPublishSuccess = { navController.navigate("marketplace") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                onOperateClick = { navController.navigate("startOperation") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("startOperation") {
            StartOperationScreen(
                onConfirmOperation = { navController.navigate("operationResume") },
                onProfileClick = { navController.navigate("profile") },
                onBackClick = { navController.navigate("marketplace") }
            )
        }

        composable("operationResume") {
            OperationResumeScreen(
                onUploadVoucherClick = { navController.navigate("voucher") },
                onBackClick = { navController.navigate("startOperation") }
            )
        }

        composable("voucher") {
            VoucherScreen(
                onVoucherSent = { navController.navigate("history") },
                onBackClick = { navController.navigate("operationResume") }
            )
        }

        composable("history") {
            HistoryScreen(
                onOperationClick = { navController.navigate("operationDetail") },
                onBackClick = { navController.navigate("marketplace") }
            )
        }

        composable("operationDetail") {
            OperationDetailScreen(
                onRateUserClick = { navController.navigate("rating") },
                onReportProblemClick = { navController.navigate("dispute") },
                onBackClick = { navController.navigate("history") }
            )
        }

        composable("rating") {
            RatingScreen(
                onRatingSent = { navController.navigate("operationDetail") },
                onBackClick = { navController.navigate("operationDetail") }
            )
        }

        composable("dispute") {
            DisputeScreen(
                onDisputeSent = { navController.navigate("history") },
                onBackClick = { navController.navigate("operationDetail") }
            )
        }

        composable("notifications") {
            NotificationsScreen(
                onBackClick = { navController.navigate("marketplace") },
                onNotificationClick = { navController.navigate("operationDetail") }
            )
        }

        composable("pushInfo") {
            PushInfoScreen(
                onBackClick = { navController.navigate("admin") }
            )
        }

        composable("admin") {
            AdminScreen(
                onUserDetailClick = { navController.navigate("adminDetail") },
                onDisputeDetailClick = { navController.navigate("adminDetail") },
                onBackClick = { 
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("admin") { inclusive = true }
                    }
                }
            )
        }

        composable("adminDetail") {
            AdminDetailScreen(
                onBackClick = { navController.navigate("admin") },
                onBlockUserClick = { /* Acción simulada */ },
                onResolveDisputeClick = { /* Acción simulada */ }
            )
        }
    }
}
