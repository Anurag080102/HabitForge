package com.habitforge.app.ui.screens.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habitforge.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitforge.app.data.remote.firebase.CommunityPost
import com.habitforge.app.util.MilestoneType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigateToShare: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.community_title)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToShare) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_milestone))
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_posts),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.posts, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        onLike = { viewModel.likePost(post.id) }
                    )
                }
            }
        }

        // Error snackbar
        uiState.errorMessage?.let { error ->
            // Show error
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onLike: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val milestoneEmoji = when (post.milestoneType) {
        MilestoneType.STREAK.value -> "🔥"
        MilestoneType.COMPLETION.value -> "✅"
        MilestoneType.JOURNAL.value -> "📔"
        else -> "🎉"
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = post.odge,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dateFormat.format(Date(post.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Milestone info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = milestoneEmoji,
                    style = MaterialTheme.typography.headlineSmall
                )
                Column {
                    Text(
                        text = "${post.milestoneValue} day streak!",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = post.habitName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Message
            if (post.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Like button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isLiked = !isLiked
                    if (!isLiked) return@IconButton
                    onLike()
                }) {
                    Icon(
                        if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${post.likes + if (isLiked) 1 else 0}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
