package org.yac.llamarangers.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.ui.login.LoginScreen

/**
 * Root screen that checks authentication state.
 * Ports iOS ContentView.
 */
@Composable
fun ContentScreen(
    authManager: AuthManager,
    onAuthenticated: @Composable () -> Unit
) {
    val isAuthenticated by authManager.isAuthenticated.collectAsState()

    if (isAuthenticated) {
        onAuthenticated()
    } else {
        LoginScreen()
    }
}
