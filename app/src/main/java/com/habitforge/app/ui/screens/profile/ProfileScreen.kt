package com.habitforge.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile ?: UserProfileEntity()
    var name by remember { mutableStateOf(TextFieldValue(profile.name)) }
    var email by remember { mutableStateOf(TextFieldValue(profile.email)) }
    var avatarUrl by remember { mutableStateOf(TextFieldValue(profile.avatarUrl)) }

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedLang by remember { mutableStateOf(Locale.getDefault().language) }
    val languages = listOf(
        "en" to "English",
        "fr" to "Français",
        "hi" to "हिन्दी"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Language selection
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = languages.find { it.first == selectedLang }?.second ?: "English",
                onValueChange = {},
                readOnly = true,
                label = { Text("Language") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languages.forEach { (code, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            selectedLang = code
                            expanded = false
                            LocaleHelper.applyLocale(context as android.app.Activity, code)
                        }
                    )
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
        Text("Monthly Tracking", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        MonthlyStatsList(uiState.monthlyStats)
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
