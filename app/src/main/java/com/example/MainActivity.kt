package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpecimenBankScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VerifyScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SignatureViewModel

enum class AppScreen {
    HOME,
    VERIFY,
    RESULT,
    HISTORY,
    SPECIMENS,
    SETTINGS
}

data class NavItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val signatureViewModel: SignatureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SignatureApp(viewModel = signatureViewModel)
            }
        }
    }
}

@Composable
fun SignatureApp(viewModel: SignatureViewModel) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var previousScreen by remember { mutableStateOf(AppScreen.HOME) }

    fun navigateTo(screen: AppScreen) {
        previousScreen = currentScreen
        currentScreen = screen
    }

    if (showSplash) {
        SplashScreen(
            onAnimationFinished = { showSplash = false }
        )
    } else {
        // BackHandler support
        BackHandler(enabled = currentScreen != AppScreen.HOME) {
            currentScreen = when (currentScreen) {
                AppScreen.RESULT -> AppScreen.VERIFY
                AppScreen.SETTINGS -> previousScreen
                else -> AppScreen.HOME
            }
        }

        val navItems = listOf(
            NavItem(AppScreen.HOME, "Beranda", Icons.Default.Home, "nav_home"),
            NavItem(AppScreen.VERIFY, "Verifikasi", Icons.Default.VerifiedUser, "nav_verify"),
            NavItem(AppScreen.HISTORY, "Riwayat", Icons.Default.History, "nav_history"),
            NavItem(AppScreen.SPECIMENS, "Spesimen", Icons.Default.FolderShared, "nav_specimens"),
            NavItem(AppScreen.SETTINGS, "Pengaturan", Icons.Default.Settings, "nav_settings")
        )

        val showBottomBar = currentScreen in listOf(
            AppScreen.HOME,
            AppScreen.VERIFY,
            AppScreen.HISTORY,
            AppScreen.SPECIMENS,
            AppScreen.SETTINGS
        )

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightDark),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        NavigationBar(
                            containerColor = com.example.ui.theme.MidnightCardBg,
                            contentColor = Color.White,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                .border(
                                    1.dp,
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(Color(0x4000E5FF), Color(0x2038BDF8), Color(0x4000E5FF))
                                    ),
                                    androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                                )
                                .testTag("main_navigation_bar")
                        ) {
                            navItems.forEach { item ->
                                val selected = currentScreen == item.screen
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(item.screen) },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MidnightDark,
                                        selectedTextColor = CyanAccent,
                                        indicatorColor = CyanAccent,
                                        unselectedIconColor = Color(0xFF64748B),
                                        unselectedTextColor = Color(0xFF64748B)
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Crossfade(
                targetState = currentScreen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToVerify = { navigateTo(AppScreen.VERIFY) },
                            onNavigateToHistory = { navigateTo(AppScreen.HISTORY) },
                            onNavigateToSpecimens = { navigateTo(AppScreen.SPECIMENS) },
                            onNavigateToSettings = { navigateTo(AppScreen.SETTINGS) },
                            onReplaySplash = { showSplash = true },
                            onSelectRecord = { record ->
                                navigateTo(AppScreen.HISTORY)
                            }
                        )
                    }

                    AppScreen.VERIFY -> {
                        VerifyScreen(
                            viewModel = viewModel,
                            onVerificationCompleted = {
                                navigateTo(AppScreen.RESULT)
                            }
                        )
                    }

                    AppScreen.RESULT -> {
                        ResultScreen(
                            viewModel = viewModel,
                            onBackToHome = {
                                viewModel.resetVerification()
                                navigateTo(AppScreen.HOME)
                            },
                            onVerifyAnother = {
                                viewModel.resetVerification()
                                navigateTo(AppScreen.VERIFY)
                            }
                        )
                    }

                    AppScreen.HISTORY -> {
                        HistoryScreen(viewModel = viewModel)
                    }

                    AppScreen.SPECIMENS -> {
                        SpecimenBankScreen(
                            viewModel = viewModel,
                            onUseSpecimenForVerify = { specimen ->
                                viewModel.selectReferenceSpecimen(specimen)
                                navigateTo(AppScreen.VERIFY)
                            }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = {
                                navigateTo(previousScreen)
                            },
                            onReplaySplash = { showSplash = true }
                        )
                    }
                }
            }
        }
    }
}
