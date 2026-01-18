package com.habitforge.app.ui.screens.profile

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
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
    val profile = uiState.profile

    // Snackbar host state for Material3
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    
    // Track editable fields - initialize once and update only when profile changes
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var email by remember { mutableStateOf(profile?.email ?: "") }
    var selectedLang by remember { mutableStateOf(profile?.preferredLanguage ?: Locale.getDefault().language) }
    
    val languages = listOf(
        "en" to "English",
        "fr" to "Français",
        "hi" to "हिन्दी"
    )

    // Update fields only when profile actually changes (use profile.id as key to prevent unnecessary updates)
    LaunchedEffect(profile?.id) {
        profile?.let {
            name = it.name
            email = it.email
            // Only update language if it's different from current to avoid unnecessary locale changes
            if (it.preferredLanguage != selectedLang) {
                selectedLang = it.preferredLanguage
            }
        }
    }
    
    // Only apply locale when language actually changes, not on every profile update
    // Use a flag to prevent applying locale on initial load
    var isInitialLoad by remember { mutableStateOf(true) }
    LaunchedEffect(selectedLang) {
        if (isInitialLoad) {
            isInitialLoad = false
            return@LaunchedEffect
        }
        val currentLang = Locale.getDefault().language
        if (selectedLang != currentLang) {
            LocaleHelper.applyLocale(context as Activity, selectedLang)
        }
    }

    // Use Material3 Scaffold consistently
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { paddingValues ->
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
                        Text(
                            "Profile Information",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Language selection using ExposedDropdownMenuBox for Material 3
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = languages.find { it.first == selectedLang }?.second ?: "Select Language",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Language") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                }
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
                                            LocaleHelper.applyLocale(context as Activity, code)
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // UI styling: Orange primary button
                        Button(
                            onClick = {
                                viewModel.saveProfile(
                                    UserProfileEntity(
                                        name = name,
                                        email = email,
                                        avatarUrl = "", // Avatar URL no longer used
                                        preferredLanguage = selectedLang
                                    )
                                )
                                // Only apply locale if it changed
                                val currentLang = Locale.getDefault().language
                                if (selectedLang != currentLang) {
                                    LocaleHelper.applyLocale(context as Activity, selectedLang)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp), // Stabilize minimum height to prevent layout shifts
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // UI styling: Orange accent for section header
                        Text(
                            "Monthly Tracking",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Use key to prevent recomposition when stats haven't changed
                        key(uiState.monthlyStats.size) {
                            MonthlyStatsList(uiState.monthlyStats)
                        }
                    }
                }
            }
        }
    }

    // Show confirmation Snackbar when requested
    if (uiState.showConfirmation) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar("Profile saved!")
            viewModel.resetConfirmation()
        }
    }
}

@Composable
fun MonthlyStatsList(stats: List<MonthlyCompletionStat>) {
    // UI improvement: Enhanced statistics display with better visual design
    if (stats.isEmpty()) {
        Text(
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
                    Text(
                        stat.month,
                        style = MaterialTheme.typography.titleMedium
                    )
                    // UI styling: Orange accent for completion count
                    Text(
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
