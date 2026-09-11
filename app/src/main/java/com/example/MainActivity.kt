package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.SafetyViewModel
import com.example.ui.screens.activity.ActivityLockerScreen
import com.example.ui.screens.demo.DemoCenterScreen
import com.example.ui.screens.family.FamilyProtectionScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.incident.IncidentResponseScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.payment.PaymentSafetyScreen
import com.example.ui.screens.scan.AudioAnalysisScreen
import com.example.ui.screens.scan.MessageAnalysisScreen
import com.example.ui.screens.scan.QrScannerScreen
import com.example.ui.screens.scan.ScanAnalysisScreen
import com.example.ui.screens.scan.ScanHubSheet
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.RakshaAITheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RakshaAITheme {
                val viewModel: SafetyViewModel = viewModel()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                var showScanHubSheet by remember { mutableStateOf(false) }

                val bottomNavRoutes = setOf("home", "activity_locker", "family_protection", "settings")
                val showBottomNav = currentRoute in bottomNavRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.testTag("nav_home")
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Folder, contentDescription = "Locker") },
                                    label = { Text("Locker") },
                                    selected = currentRoute == "activity_locker",
                                    onClick = {
                                        navController.navigate("activity_locker") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.testTag("nav_locker")
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.People, contentDescription = "Family") },
                                    label = { Text("Family") },
                                    selected = currentRoute == "family_protection",
                                    onClick = {
                                        navController.navigate("family_protection") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.testTag("nav_family")
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") },
                                    selected = currentRoute == "settings",
                                    onClick = {
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "home"
                        ) {
                            composable("onboarding") {
                                OnboardingScreen(
                                    viewModel = viewModel,
                                    onFinish = {
                                        navController.navigate("home") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("home") {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onOpenScanHub = { showScanHubSheet = true },
                                    onOpenLinkScan = {
                                        // Auto-load link demo or launch hub
                                        viewModel.startUrlAnalysis("https://sbi-yono-update-pan.cc/verify")
                                        navController.navigate("scan_analysis")
                                    },
                                    onOpenQrScan = { navController.navigate("qr_scanner") },
                                    onOpenMessageCheck = { navController.navigate("message_analysis") },
                                    onOpenAudioCheck = { navController.navigate("audio_analysis") },
                                    onOpenPaymentSafety = { navController.navigate("payment_safety") },
                                    onOpenEmergency = { navController.navigate("incident_response") },
                                    onOpenDemoCenter = { navController.navigate("demo_center") }
                                )
                            }

                            composable("scan_analysis") {
                                ScanAnalysisScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateEmergency = { navController.navigate("incident_response") }
                                )
                            }

                            composable("qr_scanner") {
                                QrScannerScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToAnalysis = { navController.navigate("scan_analysis") }
                                )
                            }

                            composable("message_analysis") {
                                MessageAnalysisScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToAnalysis = { navController.navigate("scan_analysis") }
                                )
                            }

                            composable("audio_analysis") {
                                AudioAnalysisScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToAnalysis = { navController.navigate("scan_analysis") }
                                )
                            }

                            composable("payment_safety") {
                                PaymentSafetyScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToEmergency = { navController.navigate("incident_response") }
                                )
                            }

                            composable("incident_response") {
                                IncidentResponseScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("family_protection") {
                                FamilyProtectionScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("activity_locker") {
                                ActivityLockerScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToDemoCenter = { navController.navigate("demo_center") }
                                )
                            }

                            composable("demo_center") {
                                DemoCenterScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onLaunchAnalysis = { navController.navigate("scan_analysis") }
                                )
                            }
                        }

                        // Bottom Sheet for "Scan Anything"
                        if (showScanHubSheet) {
                            ScanHubSheet(
                                viewModel = viewModel,
                                onDismiss = { showScanHubSheet = false },
                                onNavigateToAnalysis = { navController.navigate("scan_analysis") },
                                onNavigateToQr = { navController.navigate("qr_scanner") },
                                onNavigateToAudio = { navController.navigate("audio_analysis") },
                                onNavigateToPayment = { navController.navigate("payment_safety") },
                                onNavigateToMessage = { navController.navigate("message_analysis") }
                            )
                        }
                    }
                }
            }
        }
    }
}
