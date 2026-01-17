package com.habitforge.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.habitforge.app.util.LocaleHelper
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitforge.app.data.local.entity.UserProfileEntity
import com.habitforge.app.data.local.entity.MonthlyCompletionStat
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile ?: UserProfileEntity()
    var name by remember { mutableStateOf(profile.name) }
    var email by remember { mutableStateOf(profile.email) }
    var avatarUrl by remember { mutableStateOf(profile.avatarUrl) }
    val scaffoldState = rememberScaffoldState()

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedLang by remember { mutableStateOf<String>(Locale.getDefault().language) }
    val languages = listOf(
        "en" to "English",
        "fr" to "Français",
        "hi" to "हिन्दी"
    )

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(hostState = scaffoldState.snackbarHostState) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text("Profile", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { androidx.compose.material3.Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Language selection
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { (code, label) ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { androidx.compose.material3.Text(label) },
                            onClick = {
                                selectedLang = code
                                expanded = false
                                LocaleHelper.applyLocale(context as android.app.Activity, code)
                            }
                        )
                    }
                }
                androidx.compose.material3.OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { androidx.compose.material3.Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.material3.OutlinedTextField(
                    value = avatarUrl,
                    onValueChange = { avatarUrl = it },
                    label = { androidx.compose.material3.Text("Avatar URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.Button(onClick = {
                    viewModel.saveProfile(
                        UserProfileEntity(
                            name = name,
                            email = email,
                            avatarUrl = avatarUrl
                        )
                    )
                }) {
                    androidx.compose.material3.Text("Save Profile")
                }
                Spacer(Modifier.height(24.dp))
                androidx.compose.material3.Text("Monthly Tracking", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                MonthlyStatsList(uiState.monthlyStats)
            }
        }
    }
    // Show confirmation Snackbar
    if (uiState.showConfirmation) {
        LaunchedEffect(scaffoldState.snackbarHostState) {
            scaffoldState.snackbarHostState.showSnackbar("Profile saved!")
            viewModel.resetConfirmation()
        }
    }
}

@Composable
fun MonthlyStatsList(stats: List<MonthlyCompletionStat>) {
    if (stats.isEmpty()) {
        androidx.compose.material3.Text("No monthly data yet.")
    } else {
        Column(Modifier.fillMaxWidth()) {
            stats.forEach { stat ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    androidx.compose.material3.Text(stat.month)
                    androidx.compose.material3.Text("Completed: ${stat.completedCount}")
                }
            }
        }
    }
}
