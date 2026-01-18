package com.habitforge.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // UI improvement: Converted to Material3 Scaffold with proper structure
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(hostState = scaffoldState.snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { androidx.compose.material3.Text("Profile") }
            )
        }
    ) { paddingValues ->
        // UI improvement: Improved spacing and visual consistency
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                // Profile Information Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // UI styling: Orange accent for section header
                        androidx.compose.material3.Text(
                            "Profile Information",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        androidx.compose.material3.OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { androidx.compose.material3.Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Language selection
                        androidx.compose.material3.OutlinedTextField(
                            value = languages.find { it.first == selectedLang }?.second ?: "Select Language",
                            onValueChange = {},
                            readOnly = true,
                            label = { androidx.compose.material3.Text("Language") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                androidx.compose.material3.IconButton(onClick = { expanded = true }) {
                                    androidx.compose.material3.Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Language"
                                    )
                                }
                            }
                        )
                        
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
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        androidx.compose.material3.OutlinedTextField(
                            value = avatarUrl,
                            onValueChange = { avatarUrl = it },
                            label = { androidx.compose.material3.Text("Avatar URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        // UI styling: Orange primary button
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.saveProfile(
                                    UserProfileEntity(
                                        name = name,
                                        email = email,
                                        avatarUrl = avatarUrl
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            androidx.compose.material3.Text(
                                "Save Profile",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            
            item {
                // Monthly Statistics Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // UI styling: Orange accent for section header
                        androidx.compose.material3.Text(
                            "Monthly Tracking",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MonthlyStatsList(uiState.monthlyStats)
                    }
                }
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
    // UI improvement: Enhanced statistics display with better visual design
    if (stats.isEmpty()) {
        androidx.compose.material3.Text(
            "No monthly data yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stats.forEach { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        stat.month,
                        style = MaterialTheme.typography.titleMedium
                    )
                    // UI styling: Orange accent for completion count
                    androidx.compose.material3.Text(
                        "Completed: ${stat.completedCount}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (stat != stats.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}
