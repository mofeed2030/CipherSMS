package com.ciphersms.app.presentation.ui.chat

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.ciphersms.app.domain.model.*
import com.ciphersms.app.presentation.ui.theme.CipherColors
import com.ciphersms.app.presentation.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    threadId: Long,
    address: String,
    contactName: String? = null,
    viewModel: ChatViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCallClick: () -> Unit,
    onVideoCall: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.latestMessages.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CipherColors.DeepBlack)
    ) {
        // Digital grid background
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            val gridSize = 40f
            for (x in 0..size.width.toInt() step gridSize.toInt()) {
                drawLine(
                    color = CipherColors.NeonGreen,
                    start = androidx.compose.ui.geometry.Offset(x.toFloat(), 0f),
                    end = androidx.compose.ui.geometry.Offset(x.toFloat(), size.height),
                    strokeWidth = 0.5f
                )
            }
            for (y in 0..size.height.toInt() step gridSize.toInt()) {
                drawLine(
                    color = CipherColors.NeonGreen,
                    start = androidx.compose.ui.geometry.Offset(0f, y.toFloat()),
                    end = androidx.compose.ui.geometry.Offset(size.width, y.toFloat()),
                    strokeWidth = 0.5f
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Chat Header
            ChatHeader(
                name = contactName ?: address,
                address = address,
                isRCS = uiState.conversation?.isRCS == true,
                isEncrypted = uiState.conversation?.encryptionStatus == EncryptionStatus.ACTIVE,
                isStealthMode = uiState.isStealthMode,
                recipientTyping = uiState.recipientTyping,
                onBack = onBack,
                onCall = onCallClick,
                onVideoCall = onVideoCall,
                onSummarize = viewModel::summarizeConversation,
                onToggleStealth = viewModel::toggleStealthMode
            )

            // AI Summary Banner
            AnimatedVisibility(
                visible = uiState.aiSummary != null || uiState.isSummarizing,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SummaryBanner(
                    summary = uiState.aiSummary,
                    isLoading = uiState.isSummarizing
                )
            }

            // Messages List
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    MessageBubble(
                        message = message,
                        isBlurred = uiState.isBlurMode && !message.isMine,
                        onLongClick = { /* show message actions */ },
                        onTranslate = { viewModel.translateMessage(message.id) },
                        onEdit = { viewModel.editMessage(message.id, it) },
                        onDelete = { viewModel.deleteMessage(message.id) },
                        onSetDisappear = { minutes -> viewModel.setDisappearTimer(message.id, minutes) }
                    )
                }

                // Typing indicator
                if (uiState.recipientTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Smart Replies
            AnimatedVisibility(
                visible = uiState.smartReplies.isNotEmpty() && uiState.composeText.isEmpty(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                SmartRepliesRow(
                    replies = uiState.smartReplies,
                    onReplyClick = viewModel::useSmartReply
                )
            }

            // Schedule mode banner
            AnimatedVisibility(visible = uiState.isScheduleMode) {
                ScheduleBanner(
                    scheduledTime = uiState.scheduledTime,
                    onCancel = viewModel::toggleScheduleMode
                )
            }

            // Compose Bar
            ComposeBar(
                text = uiState.composeText,
                onTextChange = viewModel::onComposeTextChange,
                onSend = viewModel::sendMessage,
                onAttachmentClick = { /* open media picker */ },
                onCameraClick = { /* open camera */ },
                onVoiceNoteClick = { /* start recording */ },
                onScheduleClick = viewModel::toggleScheduleMode,
                isScheduled = uiState.isScheduleMode,
                replyTo = uiState.replyToMessage,
                onCancelReply = { viewModel.setReplyTo(null) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatHeader(
    name: String,
    address: String,
    isRCS: Boolean,
    isEncrypted: Boolean,
    isStealthMode: Boolean,
    recipientTyping: Boolean,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onVideoCall: () -> Unit,
    onSummarize: () -> Unit,
    onToggleStealth: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.5.dp, if (isRCS) CipherColors.NeonGreen else CipherColors.BorderBlack, CircleShape)
                            .clip(CircleShape)
                            .background(CipherColors.CardBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRCS) {
                            Box(Modifier.matchParentSize().blur(4.dp).background(CipherColors.NeonGreenGlow, CircleShape))
                        }
                        Text(
                            name.take(2).uppercase(),
                            color = CipherColors.NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = name,
                                color = CipherColors.OnSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            if (isEncrypted) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.Security,
                                    null,
                                    tint = CipherColors.NeonGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = if (recipientTyping) "typing..." else
                                if (isRCS) "[RCS]" else "[SMS]",
                            color = if (recipientTyping) CipherColors.NeonGreen else CipherColors.OnSurfaceDim,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, null, tint = CipherColors.NeonGreen)
                }
            },
            actions = {
                if (isStealthMode) {
                    Icon(
                        Icons.Filled.VisibilityOff,
                        null,
                        tint = CipherColors.NeonGreenDim,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                IconButton(onClick = onCall) {
                    Icon(Icons.Filled.Call, null, tint = CipherColors.NeonGreen)
                }
                IconButton(onClick = onSummarize) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = CipherColors.NeonGreenDim)
                }
                var showMore by remember { mutableStateOf(false) }
                IconButton(onClick = { showMore = true }) {
                    Icon(Icons.Filled.MoreVert, null, tint = CipherColors.OnSurfaceDim)
                }
                DropdownMenu(
                    expanded = showMore,
                    onDismissRequest = { showMore = false },
                    modifier = Modifier.background(CipherColors.CardBlack)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isStealthMode) "Exit Stealth" else "Stealth Mode", color = CipherColors.OnSurface) },
                        onClick = { onToggleStealth(); showMore = false },
                        leadingIcon = { Icon(Icons.Filled.VisibilityOff, null, tint = CipherColors.NeonGreen) }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CipherColors.SurfaceBlack
            )
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, CipherColors.NeonGreenFaint, Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isBlurred: Boolean,
    onLongClick: () -> Unit,
    onTranslate: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    onSetDisappear: (Int) -> Unit
) {
    val isMine = message.isMine

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        // Reply indicator
        if (message.replyToId != null) {
            Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .background(CipherColors.NeonGreenDim)
                )
                Text(
                    "  // reply",
                    color = CipherColors.OnSurfaceDim,
                    fontSize = 10.sp
                )
            }
        }

        Row(
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            val bubbleColor = if (isMine) CipherColors.OutgoingBubble else CipherColors.IncomingBubble
            val borderColor = if (isMine) CipherColors.NeonGreenFaint else CipherColors.BorderBlack

            Box(
                modifier = Modifier
                    .widthIn(min = 60.dp, max = 280.dp)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMine) 16.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 16.dp
                        )
                    )
                    .background(
                        color = bubbleColor,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMine) 16.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 16.dp
                        )
                    )
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongClick
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Glow on outgoing
                if (isMine) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(12.dp)
                            .background(CipherColors.NeonGreenTrace, RoundedCornerShape(16.dp))
                    )
                }

                Column {
                    // Body
                    if (isBlurred) {
                        Box(
                            modifier = Modifier
                                .blur(8.dp)
                                .background(CipherColors.NeonGreenFaint)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = message.body,
                                color = Color.Transparent,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text(
                            text = message.body,
                            color = if (isMine) CipherColors.OnSurface else CipherColors.OnSurface,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    // Translation
                    if (message.translation != null) {
                        Spacer(Modifier.height(4.dp))
                        Divider(color = CipherColors.NeonGreenFaint)
                        Text(
                            text = message.translation,
                            color = CipherColors.NeonGreenDim,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    // Edit indicator
                    if (message.isEdited) {
                        Text(
                            "edited",
                            color = CipherColors.OnSurfaceDim,
                            fontSize = 10.sp
                        )
                    }

                    // Time + Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.disappearsAt != null) {
                            Icon(
                                Icons.Filled.Timer,
                                null,
                                tint = CipherColors.WarningAmber,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                        }
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.date),
                            color = CipherColors.OnSurfaceDim,
                            fontSize = 10.sp
                        )
                        if (isMine) {
                            Spacer(Modifier.width(4.dp))
                            MessageStatusIcon(message.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIcon(status: MessageStatus) {
    val (icon, color) = when (status) {
        MessageStatus.SENDING -> Icons.Filled.Schedule to CipherColors.OnSurfaceDim
        MessageStatus.SENT -> Icons.Filled.Check to CipherColors.OnSurfaceDim
        MessageStatus.DELIVERED -> Icons.Filled.DoneAll to CipherColors.OnSurface
        MessageStatus.READ -> Icons.Filled.DoneAll to CipherColors.NeonGreen
        MessageStatus.FAILED -> Icons.Filled.Error to Color(0xFFFF0033)
        else -> Icons.Filled.Schedule to CipherColors.OnSurfaceDim
    }
    Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.padding(start = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(CipherColors.IncomingBubble, RoundedCornerShape(16.dp))
                .border(1.dp, CipherColors.BorderBlack, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(if (i == 0) alpha else if (i == 1) (1f - alpha).coerceIn(0.3f, 1f) else alpha)
                            .background(CipherColors.NeonGreen, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartRepliesRow(
    replies: List<String>,
    onReplyClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CipherColors.SurfaceBlack)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        replies.forEach { reply ->
            SuggestionChip(
                onClick = { onReplyClick(reply) },
                label = {
                    Text(reply, color = CipherColors.NeonGreen, fontSize = 13.sp)
                },
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = CipherColors.NeonGreenFaint
                ),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = CipherColors.NeonGreenTrace
                )
            )
        }
    }
}

@Composable
private fun SummaryBanner(summary: String?, isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CipherColors.CardBlack)
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(Color.Transparent, CipherColors.NeonGreenFaint, Color.Transparent)),
                shape = RectangleShape
            )
            .padding(12.dp)
    ) {
        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = CipherColors.NeonGreen,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("AI analyzing conversation...", color = CipherColors.NeonGreenDim, fontSize = 13.sp)
            }
        } else {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = CipherColors.NeonGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("// AI_SUMMARY", color = CipherColors.NeonGreenDim, fontSize = 10.sp, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(summary ?: "", color = CipherColors.OnSurface, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ScheduleBanner(scheduledTime: Date?, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A1A00))
            .border(1.dp, CipherColors.NeonGreenFaint)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Schedule, null, tint = CipherColors.NeonGreen, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (scheduledTime != null)
                "Scheduled: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(scheduledTime)}"
            else "Select schedule time",
            color = CipherColors.NeonGreen,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onCancel) {
            Text("Cancel", color = CipherColors.DangerRed, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit,
    onCameraClick: () -> Unit,
    onVoiceNoteClick: () -> Unit,
    onScheduleClick: () -> Unit,
    isScheduled: Boolean,
    replyTo: Message?,
    onCancelReply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CipherColors.SurfaceBlack)
    ) {
        // Top border
        Box(
            modifier = Modifier.fillMaxWidth().height(1.dp)
                .background(Brush.horizontalGradient(
                    listOf(Color.Transparent, CipherColors.NeonGreenFaint, Color.Transparent)
                ))
        )

        // Reply preview
        AnimatedVisibility(visible = replyTo != null) {
            replyTo?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.width(3.dp).height(32.dp).background(CipherColors.NeonGreen))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Reply to", color = CipherColors.NeonGreen, fontSize = 11.sp)
                        Text(
                            msg.body,
                            color = CipherColors.OnSurfaceDim,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onCancelReply, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, null, tint = CipherColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Attach
            IconButton(onClick = onAttachmentClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.AttachFile, null, tint = CipherColors.NeonGreenDim)
            }

            // Camera
            IconButton(onClick = onCameraClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.CameraAlt, null, tint = CipherColors.NeonGreenDim)
            }

            // Text Field
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "> type_message...",
                        color = CipherColors.OnSurfaceDim,
                        fontSize = 13.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CipherColors.NeonGreenFaint,
                    unfocusedBorderColor = CipherColors.BorderBlack,
                    focusedTextColor = CipherColors.OnSurface,
                    unfocusedTextColor = CipherColors.OnSurface,
                    cursorColor = CipherColors.NeonGreen,
                    focusedContainerColor = CipherColors.CardBlack,
                    unfocusedContainerColor = CipherColors.CardBlack
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 5,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
            )

            Spacer(Modifier.width(8.dp))

            if (text.isEmpty()) {
                // Voice note button
                IconButton(
                    onClick = onVoiceNoteClick,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        null,
                        tint = CipherColors.NeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                // Send button with glow
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clickable(onClick = onSend),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .blur(8.dp)
                            .background(CipherColors.NeonGreenGlow, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isScheduled) Color(0xFF004400) else CipherColors.NeonGreen,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isScheduled) Icons.Filled.Schedule else Icons.Filled.Send,
                            null,
                            tint = if (isScheduled) CipherColors.NeonGreen else CipherColors.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// Extension
private val CipherColors.WarningAmber: Color get() = Color(0xFFFFAA00)
private val CipherColors.DangerRed: Color get() = Color(0xFFFF0033)

@OptIn(ExperimentalFoundationApi::class)
private fun androidx.compose.foundation.layout.RowScope.combinedClickable(
    onClick: () -> Unit,
    onLongClick: () -> Unit
) = this
