package com.ciphersms.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ciphersms.app.presentation.ui.chat.ChatScreen
import com.ciphersms.app.presentation.ui.compose.ComposeMessageScreen
import com.ciphersms.app.presentation.ui.conversations.ConversationsScreen
import com.ciphersms.app.presentation.ui.lock.LockScreen
import com.ciphersms.app.presentation.ui.settings.SettingsScreen
import com.ciphersms.app.presentation.ui.vault.VaultScreen
import com.ciphersms.app.presentation.ui.theme.CipherColors
import com.ciphersms.app.presentation.ui.theme.CipherSMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CipherSMSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CipherColors.DeepBlack
                ) {
                    CipherNavHost()
                }
            }
        }
    }
}

object CipherRoutes {
    const val CONVERSATIONS = "conversations"
    const val CHAT = "chat/{threadId}/{address}"
    const val COMPOSE = "compose?address={address}"
    const val VAULT = "vault"
    const val SETTINGS = "settings"
    const val LOCK = "lock"
    const val SCHEDULED = "scheduled"
}

@Composable
fun CipherNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CipherRoutes.CONVERSATIONS
    ) {
        // Main conversations list
        composable(CipherRoutes.CONVERSATIONS) {
            ConversationsScreen(
                onConversationClick = { threadId, address ->
                    navController.navigate("chat/$threadId/$address")
                },
                onNewMessage = {
                    navController.navigate("compose?address=")
                },
                onVaultClick = {
                    navController.navigate(CipherRoutes.VAULT)
                },
                onSettingsClick = {
                    navController.navigate(CipherRoutes.SETTINGS)
                }
            )
        }

        // Chat screen
        composable(
            route = "chat/{threadId}/{address}",
            arguments = listOf(
                navArgument("threadId") { type = NavType.LongType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
            val address = backStackEntry.arguments?.getString("address") ?: ""
            ChatScreen(
                threadId = threadId,
                address = address,
                onBack = { navController.popBackStack() },
                onCallClick = { /* initiate call */ },
                onVideoCall = { /* initiate video */ }
            )
        }

        // Compose new message
        composable(
            route = "compose?address={address}",
            arguments = listOf(
                navArgument("address") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: ""
            ComposeMessageScreen(
                initialAddress = address,
                onBack = { navController.popBackStack() },
                onStartChat = { threadId, addr ->
                    navController.navigate("chat/$threadId/$addr") {
                        popUpTo("compose?address=$address") { inclusive = true }
                    }
                }
            )
        }

        // Vault
        composable(CipherRoutes.VAULT) {
            VaultScreen(
                onBack = { navController.popBackStack() },
                onConversationClick = { threadId, address ->
                    navController.navigate("chat/$threadId/$address")
                }
            )
        }

        // Settings
        composable(CipherRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Lock screen
        composable(CipherRoutes.LOCK) {
            LockScreen(
                onUnlocked = { navController.popBackStack() }
            )
        }
    }
}
