package org.yac.llamarangers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.yac.llamarangers.data.repository.ZoneRepository
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.ui.navigation.AppNavigation
import org.yac.llamarangers.ui.theme.LlamaRangersTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var zoneRepository: ZoneRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LlamaRangersTheme {
                AppNavigation(
                    authManager = authManager,
                    zoneRepository = zoneRepository
                )
            }
        }
    }
}
