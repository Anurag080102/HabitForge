package com.habitforge.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
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
    var name by remember { mutableStateOf(TextFieldValue(profile.name)) }
    var email by remember { mutableStateOf(TextFieldValue(profile.email)) }
    var avatarUrl by remember { mutableStateOf(TextFieldValue(profile.avatarUrl)) }
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
                Text("Profile", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Language selection
                Spacer(Modifier.height(8.dp))
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { (code, label) ->
                        DropdownMenuItem(
                            onClick = {
                                selectedLang = code
                                expanded = false
                                LocaleHelper.applyLocale(context as android.app.Activity, code)
                            }
                        ) {
                            Text(label)
                        }
                    }
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = avatarUrl,
                    onValueChange = { avatarUrl = it },
                    label = { Text("Avatar URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    viewModel.saveProfile(
                        UserProfileEntity(
                            name = name.text,
                            email = email.text,
                            avatarUrl = avatarUrl.text
                        )
                    )
                }) {
                    Text("Save Profile")
                }
                Spacer(Modifier.height(24.dp))
                Text("Monthly Tracking", style = MaterialTheme.typography.subtitle1)
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
        Text("No monthly data yet.")
    } else {
        Column(Modifier.fillMaxWidth()) {
            stats.forEach { stat ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stat.month)
                    Text("Completed: ${stat.completedCount}")
                }
            }
        }
    }
}
