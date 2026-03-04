package com.ciphersms.app.presentation.ui.conversations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.ciphersms.app.domain.model.*
import com.ciphersms.app.presentation.ui.theme.CipherColors
import com.ciphersms.app.presentation.viewmodel.ConversationsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    viewModel: ConversationsViewModel = hiltViewModel(),
    onConversationClick: (Long, String) -> Unit,
    onNewMessage: () -> Unit,
    onVaultClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CipherColors.DeepBlack)
    ) {
        // Radar scan effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = CipherColors.NeonGreenGlow,
                start = Offset(0f, scanOffset % size.height),
                end = Offset(size.width, scanOffset % size.height),
                strokeWidth = 1f
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            CipherTopBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = viewModel::onSearchQueryChange,
                onClearSearch = viewModel::clearSearch,
                onVaultClick = onVaultClick,
                onSettingsClick = onSettingsClick,
                selectedCount = uiState.selectedConversations.size,
                onDeleteSelected = viewModel::deleteSelected,
                onArchiveSelected = viewModel::archiveSelected,
                onClearSelection = viewModel::clearSelection
            )

            // Search results or conversation list
            if (uiState.isSearching || uiState.searchResults.isNotEmpty()) {
                SearchResultsList(
                    results = uiState.searchResults,
                    isLoading = uiState.isSearching,
                    onResultClick = { /* navigate to message */ }
                )
            } else {
                ConversationList(
                    conversations = uiState.conversations,
                    selectedIds = uiState.selectedConversations,
                    onConversationClick = { conv ->
                        if (uiState.selectedConversations.isNotEmpty()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleSelection(conv.threadId)
                        } else {
                            onConversationClick(conv.threadId, conv.address)
                        }
                    },
                    onConversationLongClick = { conv ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleSelection(conv.threadId)
                    },
                    onPin = { conv -> viewModel.pinConversation(conv.threadId, !conv.isPinned) },
                    onArchive = { conv -> viewModel.archiveConversation(conv.threadId, true) },
                    onDelete = { conv -> viewModel.deleteConversation(conv.threadId) },
                    onVault = { conv -> viewModel.moveToVault(conv.threadId) }
                )
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onNewMessage,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = CipherColors.NeonGreen,
            contentColor = CipherColors.Black,
            shape = CircleShape
        ) {
            Box {
                Icon(Icons.Filled.Edit, contentDescription = "New Message")
                // Glow effect
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(8.dp)
                        .background(CipherColors.NeonGreenGlow, CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CipherTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onVaultClick: () -> Unit,
    onSettingsClick: () -> Unit,
    selectedCount: Int,
    onDeleteSelected: () -> Unit,
    onArchiveSelected: () -> Unit,
    onClearSelection: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = {
                if (selectedCount > 0) {
                    Text(
                        "$selectedCount selected",
                        color = CipherColors.NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Cipher logo/icon
                        Text(
                            text = "◈",
                            color = CipherColors.NeonGreen,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "CIPHER_SMS",
                            color = CipherColors.NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 3.sp
                        )
                    }
                }
            },
            actions = {
                if (selectedCount > 0) {
                    IconButton(onClick = onArchiveSelected) {
                        Icon(Icons.Filled.Archive, contentDescription = "Archive", tint = CipherColors.NeonGreen)
                    }
                    IconButton(onClick = onDeleteSelected) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = CipherColors.DangerRed)
                    }
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear", tint = CipherColors.OnSurface)
                    }
                } else {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = CipherColors.NeonGreen)
                    }
                    IconButton(onClick = onVaultClick) {
                        Icon(Icons.Filled.Lock, contentDescription = "Vault", tint = CipherColors.VaultPurple)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = CipherColors.OnSurfaceDim)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CipherColors.SurfaceBlack,
                titleContentColor = CipherColors.NeonGreen
            )
        )

        // Green accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            CipherColors.NeonGreen,
                            CipherColors.NeonGreenDim,
                            Color.Transparent
                        )
                    )
                )
        )

        // Search bar
        AnimatedVisibility(
            visible = isSearchActive,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        "> search_messages...",
                        color = CipherColors.OnSurfaceDim,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, null, tint = CipherColors.NeonGreen)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(Icons.Filled.Close, null, tint = CipherColors.OnSurfaceDim)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CipherColors.NeonGreen,
                    unfocusedBorderColor = CipherColors.BorderBlack,
                    focusedTextColor = CipherColors.OnSurface,
                    unfocusedTextColor = CipherColors.OnSurface,
                    cursorColor = CipherColors.NeonGreen,
                    focusedContainerColor = CipherColors.CardBlack,
                    unfocusedContainerColor = CipherColors.CardBlack
                ),
                singleLine = true,
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

@Composable
private fun ConversationList(
    conversations: List<Conversation>,
    selectedIds: Set<Long>,
    onConversationClick: (Conversation) -> Unit,
    onConversationLongClick: (Conversation) -> Unit,
    onPin: (Conversation) -> Unit,
    onArchive: (Conversation) -> Unit,
    onDelete: (Conversation) -> Unit,
    onVault: (Conversation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Pinned section header
        val pinned = conversations.filter { it.isPinned }
        val others = conversations.filter { !it.isPinned }

        if (pinned.isNotEmpty()) {
            item {
                SectionHeader("PINNED")
            }
            items(pinned, key = { it.threadId }) { conv ->
                ConversationItem(
                    conversation = conv,
                    isSelected = selectedIds.contains(conv.threadId),
                    onClick = { onConversationClick(conv) },
                    onLongClick = { onConversationLongClick(conv) },
                    onPin = { onPin(conv) },
                    onArchive = { onArchive(conv) },
                    onDelete = { onDelete(conv) },
                    onVault = { onVault(conv) }
                )
            }
            item { Divider(color = CipherColors.BorderBlack, thickness = 1.dp) }
            if (others.isNotEmpty()) {
                item { SectionHeader("MESSAGES") }
            }
        }

        items(others, key = { it.threadId }) { conv ->
            ConversationItem(
                conversation = conv,
                isSelected = selectedIds.contains(conv.threadId),
                onClick = { onConversationClick(conv) },
                onLongClick = { onConversationLongClick(conv) },
                onPin = { onPin(conv) },
                onArchive = { onArchive(conv) },
                onDelete = { onDelete(conv) },
                onVault = { onVault(conv) }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "// $title",
            color = CipherColors.NeonGreenDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(CipherColors.BorderBlack)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationItem(
    conversation: Conversation,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPin: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onVault: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) CipherColors.NeonGreenTrace else Color.Transparent,
        animationSpec = tween(200),
        label = "selection"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    showMenu = true
                }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .border(
                    width = if (conversation.isRCS) 2.dp else 1.dp,
                    color = if (conversation.isRCS) CipherColors.NeonGreen else CipherColors.BorderBlack,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(CipherColors.CardBlack),
            contentAlignment = Alignment.Center
        ) {
            // Glow for RCS
            if (conversation.isRCS) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(6.dp)
                        .background(CipherColors.NeonGreenGlow, CircleShape)
                )
            }
            val initials = (conversation.contactName ?: conversation.address)
                .take(2).uppercase()
            Text(
                text = initials,
                color = CipherColors.NeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name
                Text(
                    text = conversation.contactName ?: conversation.address,
                    color = if (conversation.unreadCount > 0) CipherColors.NeonGreen else CipherColors.OnSurface,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Time
                Text(
                    text = formatTime(conversation.date),
                    color = if (conversation.unreadCount > 0) CipherColors.NeonGreen else CipherColors.OnSurfaceDim,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icons row
                if (conversation.isLocked) {
                    Icon(Icons.Filled.Lock, null, tint = CipherColors.NeonGreenDim, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (conversation.isRCS) {
                    Text("⬡", color = CipherColors.NeonGreen, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (conversation.encryptionStatus == EncryptionStatus.ACTIVE) {
                    Icon(Icons.Filled.Security, null, tint = CipherColors.NeonGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Snippet
                Text(
                    text = conversation.snippet,
                    color = CipherColors.OnSurfaceDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )

                // Unread badge
                if (conversation.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(CipherColors.NeonGreen, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                            color = CipherColors.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Context menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(CipherColors.CardBlack)
        ) {
            DropdownMenuItem(
                text = { MenuText(if (conversation.isPinned) "Unpin" else "Pin") },
                onClick = { onPin(); showMenu = false },
                leadingIcon = { Icon(Icons.Filled.PushPin, null, tint = CipherColors.NeonGreen) }
            )
            DropdownMenuItem(
                text = { MenuText("Archive") },
                onClick = { onArchive(); showMenu = false },
                leadingIcon = { Icon(Icons.Filled.Archive, null, tint = CipherColors.NeonGreen) }
            )
            DropdownMenuItem(
                text = { MenuText("Move to Vault") },
                onClick = { onVault(); showMenu = false },
                leadingIcon = { Icon(Icons.Filled.Lock, null, tint = CipherColors.VaultPurple) }
            )
            Divider(color = CipherColors.BorderBlack)
            DropdownMenuItem(
                text = { MenuText("Delete", danger = true) },
                onClick = { onDelete(); showMenu = false },
                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = CipherColors.DangerRed) }
            )
        }
    }

    // Bottom divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 78.dp)
            .height(0.5.dp)
            .background(CipherColors.BorderBlack)
    )
}

@Composable
private fun MenuText(text: String, danger: Boolean = false) {
    Text(
        text = text,
        color = if (danger) CipherColors.DangerRed else CipherColors.OnSurface,
        fontSize = 14.sp
    )
}

@Composable
private fun SearchResultsList(
    results: List<com.ciphersms.app.domain.model.Message>,
    isLoading: Boolean,
    onResultClick: (Long) -> Unit
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CipherColors.NeonGreen)
        }
        return
    }

    LazyColumn {
        if (results.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "// no_results_found",
                        color = CipherColors.OnSurfaceDim,
                        fontSize = 14.sp
                    )
                }
            }
        }
        items(results) { message ->
            SearchResultItem(message, onClick = { onResultClick(message.id) })
        }
    }
}

@Composable
private fun SearchResultItem(
    message: com.ciphersms.app.domain.model.Message,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Icon(
            Icons.Filled.Message,
            null,
            tint = CipherColors.NeonGreenDim,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = message.address,
                color = CipherColors.NeonGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message.body,
                color = CipherColors.OnSurface,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTime(date: Date): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { time = date }
    return when {
        now.get(Calendar.DATE) == then.get(Calendar.DATE) ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        now.get(Calendar.WEEK_OF_YEAR) == then.get(Calendar.WEEK_OF_YEAR) ->
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else ->
            SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
    }
}

private val CipherColors.DangerRed: Color get() = Color(0xFFFF0033)
private val CipherColors.VaultPurple: Color get() = Color(0xFF8B00FF)
