package com.example.myapplication1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication1.notification.SleepReminderScheduler
import com.example.myapplication1.ui.LogScreen
import com.example.myapplication1.ui.StatsScreen
import com.example.myapplication1.ui.theme.MyApplication1Theme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                SleepReminderScheduler.schedule(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionAndSchedule()

        setContent {
            MyApplication1Theme {
                val navController = rememberNavController()
                val sleepViewModel: SleepViewModel = viewModel()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_bedtime),
                                        contentDescription = "Log",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("Log") },
                                selected = currentRoute == "log",
                                onClick = {
                                    navController.navigate("log") {
                                        popUpTo("log") { inclusive = true }
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_bar_chart),
                                        contentDescription = "Stats",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("Stats") },
                                selected = currentRoute == "stats",
                                onClick = {
                                    navController.navigate("stats") {
                                        popUpTo("log")
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "log",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("log") { LogScreen(viewModel = sleepViewModel) }
                        composable("stats") { StatsScreen(viewModel = sleepViewModel) }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                SleepReminderScheduler.schedule(this)
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            SleepReminderScheduler.schedule(this)
        }
    }
}
