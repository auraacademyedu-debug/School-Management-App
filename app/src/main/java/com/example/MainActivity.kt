package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainSchoolWorkspace
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.EduPulseTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object AppRoutes {
    const val ROUTE_LOGIN = "login"
    const val ROUTE_WORKSPACE = "workspace"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduPulseTheme {
                EduPulseAppRoot()
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun EduPulseAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val navController = rememberNavController()
    val startRoute = if (currentUser == null) AppRoutes.ROUTE_LOGIN else AppRoutes.ROUTE_WORKSPACE

    LaunchedEffect(currentUser?.uid) {
        val currentRoute = navController.currentDestination?.route
        if (currentUser != null && currentRoute != AppRoutes.ROUTE_WORKSPACE) {
            navController.navigate(AppRoutes.ROUTE_WORKSPACE) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else if (currentUser == null && currentRoute != null && currentRoute != AppRoutes.ROUTE_LOGIN) {
            navController.navigate(AppRoutes.ROUTE_LOGIN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(route = AppRoutes.ROUTE_LOGIN) {
            AuthScreen(
                onAuthSuccess = {
                    if (navController.currentDestination?.route != AppRoutes.ROUTE_WORKSPACE) {
                        navController.navigate(AppRoutes.ROUTE_WORKSPACE) {
                            popUpTo(AppRoutes.ROUTE_LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(route = AppRoutes.ROUTE_WORKSPACE) {
            val activeUser = currentUser
            if (activeUser != null) {
                MainSchoolWorkspace(
                    currentUserId = activeUser.uid,
                    userEmail = activeUser.email ?: "",
                    onSignedOut = {
                        navController.navigate(AppRoutes.ROUTE_LOGIN) {
                            popUpTo(AppRoutes.ROUTE_WORKSPACE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
