package com.newoether.agora.ui.chat.bottombar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.TransferableContent
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.newoether.agora.R
import com.newoether.agora.api.util.ContextUsage
import com.newoether.agora.data.CustomProviderConfig
import com.newoether.agora.data.modelDisplayName
import com.newoether.agora.data.providerDisplayName
import com.newoether.agora.model.ContextBudget
import com.newoether.agora.model.apiModelName
import com.newoether.agora.model.profile.ModelCapabilities
import com.newoether.agora.model.profile.ModelProfileRegistry
import com.newoether.agora.ui.chat.PdfPageSelectDialog
import com.newoether.agora.ui.chat.VideoSliceDialog
import com.newoether.agora.ui.common.LocalAgoraHaptics
import com.newoether.agora.ui.common.OpenAiServiceTierControlPanel
import com.newoether.agora.ui.common.ThinkingControlPanel
import com.newoether.agora.ui.common.openAiServiceTierShortLabel
import com.newoether.agora.ui.common.thinkingControlShortLabel
import com.newoether.agora.ui.components.DialogWindowEdgeToEdge
import com.newoether.agora.ui.motion.LocalAgoraMotionPolicy
import com.newoether.agora.ui.motion.MotionAwareCircularProgressIndicator as CircularProgressIndicator
import com.newoether.agora.ui.motion.MotionAwareModalBottomSheet as ModalBottomSheet
import com.newoether.agora.ui.theme.ChatType
import com.newoether.agora.util.noOpBringIntoView
import com.newoether.agora.viewmodel.QueuedSend
import com.newoether.agora.viewmodel.SendAcceptance
import kotlinx.coroutines.launch

internal val CHAT_BOTTOM_BAR_OUTER_RADIUS = 28.dp
internal val CHAT_BOTTOM_BAR_OUTER_SHAPE = RoundedCornerShape(CHAT_BOTTOM_BAR_OUTER_RADIUS)
internal val CHAT_DROPDOWN_MENU_SHAPE = RoundedCornerShape(16.dp)

internal fun contextUsageExceedsCompactThreshold(
    contextUsage: ContextUsage,
    thresholdPercent: Int,
): Boolean = contextUsage.exceedsCompactThreshold(thresholdPercent)

internal fun contextUsageExceedsCompactThreshold(
    estimatedTokens: Int,
    tokenBudget: Int,
    thresholdPercent: Int,
): Boolean = ContextUsage(estimatedTokens, tokenBudget).exceedsCompactThreshold(thresholdPercent)

internal fun contextUsageAtCapacity(contextUsage: ContextUsage): Boolean =
    contextUsage.isAtCapacity()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatBottomBar(
    onSendMessage: suspend (
        String,
        List<com.newoether.agora.model.SelectedAttachment>,
        suspend () -> Unit,
    ) -> SendAcceptance?,
    onStopGeneration: () -> Unit = {},
    isLoading: Boolean,
    isCompacting: Boolean = false,
    isSwitching: Boolean = false,
    enabledModels: Set<String>,
    selectedModel: String,
    modelAliases: Map<String, String> = emptyMap(),
    customProviders: List<CustomProviderConfig> = emptyList(),
    codeExecutionEnabled: Boolean = false,
    googleSearchEnabled: Boolean = false,
    openAiWebSearchAvailable: Boolean = false,
    openAiWebSearchEnabled: Boolean = false,
    capabilities: ModelCapabilities = remember(selectedModel) { ModelProfileRegistry.resolve(selectedModel).capabilities },
    thinkingEnabled: Boolean = true,
    thinkingLevel: String = "medium",
    thinkingBudgetEnabled: Boolean = false,
    thinkingBudgetTokens: Int = 4096,
    openAiServiceTierAvailable: Boolean = false,
    openAiServiceTierEnabled: Boolean = false,
    openAiServiceTier: String = "auto",
    webSearchEnabled: Boolean = false,
    shellEnabled: Boolean = false,
    onCodeExecutionToggle: (Boolean) -> Unit = {},
    onGoogleSearchToggle: (Boolean) -> Unit = {},
    onOpenAiWebSearchToggle: (Boolean) -> Unit = {},
    onThinkingToggle: (Boolean) -> Unit = {},
    onThinkingLevelChange: (String) -> Unit = {},
    onThinkingBudgetEnabledChange: (Boolean) -> Unit = {},
    onThinkingBudgetTokensChange: (Int) -> Unit = {},
    onOpenAiServiceTierToggle: (Boolean) -> Unit = {},
    onOpenAiServiceTierChange: (String) -> Unit = {},
    onWebSearchToggle: (Boolean) -> Unit = {},
    onShellToggle: (Boolean) -> Unit = {},
    onModelSelect: (String) -> Unit,
    onImageClick: (String) -> Unit = {},
    onAllMediaClick: ((urls: List<String>, index: Int) -> Unit)? = null,
    onFileContentClick: ((fileName: String, content: String) -> Unit)? = null,
    onPdfPagesClick: ((pages: List<String>, startIndex: Int) -> Unit)? = null,
    onPdfPreviewSelect: ((pages: List<String>, startIndex: Int) -> Unit)? = null,
    onPdfViewerClosed: (() -> Unit)? = null,
    pdfViewerSelection: Set<Int> = emptySet(),
    onTogglePdfSelection: ((Int) -> Unit)? = null,
    onInitPdfSelection: ((Set<Int>) -> Unit)? = null,
    fullScreenViewerUrls: List<String>? = null,
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState = rememberSaveable(saver = TextFieldState.Saver) { TextFieldState() },
    composerState: ChatComposerState = rememberChatComposerState(),
    focusRequester: FocusRequester = FocusRequester(),
    onInputFocusChanged: (Boolean) -> Unit = {},
    isExpanded: Boolean = false,
    isExpandAnimating: Boolean = false,
    onCollapse: () -> Unit = {},
    onExpand: () -> Unit = {},
    showWebSearch: Boolean = true,
    showShell: Boolean = true,
    onAdvancedClick: () -> Unit = {},
    compactDefaultModel: String? = null,
    compactDefaultPrompt: String = "",
    contextUsage: ContextUsage = ContextUsage(0, ContextBudget.DEFAULT_TOKENS, 0, false),
    contextCompactThresholdPercent: Int = 90,
    canCompact: Boolean = false,
    onCompactClick: () -> Unit = {},
    queuedSends: List<QueuedSend> = emptyList(),
    onRemoveQueuedSend: (String) -> Unit = {},
    isStopping: Boolean = false,
) {
    val motionPolicy = LocalAgoraMotionPolicy.current
    val allowSpatialTransitions = motionPolicy.allowSpatialTransitions
    val scrollState = rememberScrollState()
    BackHandler(enabled = isExpanded) { onCollapse() }
    val isModelValid = selectedModel.isNotBlank() && enabledModels.contains(selectedModel)

    // No-op bring-into-view to prevent auto-scrolling on text field focus

    val composer = composerState

    // Draft persistence lives in ChatApp, keyed by conversation id (the id must be captured at
    // edit time, not at debounce-fire time — see the draft effect there).

    val context = LocalContext.current
    val haptics = LocalAgoraHaptics.current
    val clipboardImageReceiver = remember(context, composer) {
        object : ReceiveContentListener {
            override fun onReceive(
                transferableContent: TransferableContent,
            ): TransferableContent? {
                val imageUris = mutableListOf<android.net.Uri>()
                val advertisesImages = transferableContent.hasMediaType(MediaType.Image)
                val remaining = transferableContent.consume { item ->
                    val uri = item.uri ?: return@consume false
                    val resolvedMime = context.contentResolver.getType(uri)
                    val isImage = resolvedMime?.startsWith("image/") == true ||
                        (resolvedMime == null && advertisesImages)
                    if (isImage) imageUris += uri
                    isImage
                }
                if (imageUris.isNotEmpty()) {
                    composer.onPickImages(imageUris)
                }
                return remaining
            }
        }
    }
    var showThinkingSheet by rememberSaveable { mutableStateOf(false) }
    var showOpenAiServiceTierSheet by rememberSaveable { mutableStateOf(false) }
    val composerOcclusionColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    val composerOcclusionShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
    )

    // Restore PDF dialog after viewer closes
    LaunchedEffect(fullScreenViewerUrls) {
        if (fullScreenViewerUrls == null && composer.pdfDialogHiddenForPreview && composer.pendingPdfUri != null) {
            composer.showPdfPageDialog = true
            composer.pdfDialogHiddenForPreview = false
        }
    }
    LaunchedEffect(openAiServiceTierAvailable) {
        if (!openAiServiceTierAvailable) showOpenAiServiceTierSheet = false
    }
    val photoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> composer.onPickImages(uris) }
    val videoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> composer.onPickVideos(uris) }
    val fileLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents()
    ) { uris -> composer.onPickFiles(uris, onInitPdfSelection) }
    val activityLaunchScope = rememberCoroutineScope()
    var pendingCameraPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraPermissionPath by rememberSaveable { mutableStateOf<String?>(null) }
    var internalCameraPath by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { captured ->
        pendingCameraPath?.let { privatePath ->
            composer.completeCameraCapture(privatePath, captured)
        }
        pendingCameraPath = null
    }
    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val privatePath = pendingCameraPermissionPath
        pendingCameraPermissionPath = null
        if (granted && privatePath != null) {
            internalCameraPath = privatePath
        } else if (privatePath != null) {
            composer.completeCameraCapture(privatePath, captured = false)
            composer.reportCameraPreparationFailure()
        }
    }

    fun launchInternalCamera(privatePath: String) {
        if (
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            internalCameraPath = privatePath
        } else {
            pendingCameraPermissionPath = privatePath
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxWidth().then(if (isExpanded) Modifier.fillMaxHeight() else Modifier).padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 12.dp)) {
        Column(modifier = Modifier.fillMaxWidth().then(if (isExpanded) Modifier.fillMaxHeight() else Modifier)) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = EnterTransition.None,
                exit = if (allowSpatialTransitions) {
                    shrinkVertically(tween(250)) + fadeOut(tween(250))
                } else {
                    fadeOut(tween(250))
                },
            ) {
                Spacer(modifier = Modifier.height(44.dp))
            }

            ComposerStatusColumn(
                queuedSends = queuedSends,
                onRemoveQueuedSend = onRemoveQueuedSend,
                modifier = Modifier.zIndex(0f),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isExpanded) Modifier.weight(1f) else Modifier)
                    .then(
                        if (allowSpatialTransitions) {
                            Modifier.animateContentSize(
                                animationSpec = tween(durationMillis = 400),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clip(composerOcclusionShape)
                    .background(composerOcclusionColor)
                    .zIndex(1f),
            ) {
        // Also shown while expanded: hiding it there meant a full-screen composer gave no sign
        // that attachments were about to be sent.
        if (composer.selectedAttachments.isNotEmpty()) {
            AttachmentPreviewRow(
                composer = composer,
                onAllMediaClick = onAllMediaClick,
                onFileContentClick = onFileContentClick,
                onPdfPagesClick = onPdfPagesClick,
            )
        }

        Box(modifier = Modifier.fillMaxWidth().then(if (isExpanded) Modifier.weight(1f) else Modifier).noOpBringIntoView()) {
            TextField(
                state = textFieldState,
                scrollState = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isExpanded) Modifier.fillMaxHeight() else Modifier)
                    .contentReceiver(clipboardImageReceiver)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onInputFocusChanged(focusState.isFocused)
                    }
                    .verticalScrollbar(scrollState, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                placeholder = {
                    Text(
                        stringResource(R.string.ask_agora),
                        style = ChatType.input,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                enabled = true,
                lineLimits = TextFieldLineLimits.MultiLine(1, if (isExpanded) Int.MAX_VALUE else 6),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = ChatType.input.copy(color = MaterialTheme.colorScheme.onSurface)
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn(tween(250)),
                exit = ExitTransition.None,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                val elevatedSurface = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                IconButton(onClick = { if (!isExpandAnimating) onExpand() }, modifier = Modifier.padding(end = 4.dp, top = 4.dp).size(40.dp).background(Brush.radialGradient(listOf(elevatedSurface, elevatedSurface.copy(alpha = 0.5f), Color.Transparent)), CircleShape)) { Icon(painter = androidx.compose.ui.res.painterResource(id = R.drawable.expand_all_24px), contentDescription = stringResource(R.string.expand), modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)) }
            }
        }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, start = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(48.dp).background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp), RoundedCornerShape(100)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                AttachmentAddMenu(
                    onCamera = {
                        activityLaunchScope.launch {
                            val target = composer.createCameraCaptureTarget()
                            if (target == null) {
                                composer.reportCameraPreparationFailure()
                                return@launch
                            }
                            if (canLaunchSystemImageCapture(context)) {
                                pendingCameraPath = target.privatePath
                                runCatching { cameraLauncher.launch(target.uri) }
                                    .onFailure {
                                        pendingCameraPath = null
                                        launchInternalCamera(target.privatePath)
                                    }
                            } else {
                                launchInternalCamera(target.privatePath)
                            }
                        }
                    },
                    onPhotos = {
                        photoLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts
                                    .PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    onVideos = {
                        videoLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts
                                    .PickVisualMedia.VideoOnly,
                            ),
                        )
                    },
                    onFiles = { fileLauncher.launch("*/*") },
                )
                var activeMenu by remember { mutableStateOf<String?>(null) }
                var lastModelDismissTime by remember { mutableLongStateOf(0L) }
                var lastContextDismissTime by remember { mutableLongStateOf(0L) }
                var lastToolsDismissTime by remember { mutableLongStateOf(0L) }

                val selectedProvider = providerDisplayName(
                    com.newoether.agora.model.ModelId.parse(selectedModel).providerName,
                    customProviders,
                )
                val displayText = when {
                    isModelValid -> modelDisplayName(selectedModel, modelAliases, customProviders)
                    enabledModels.isNotEmpty() -> stringResource(R.string.select_model)
                    else -> stringResource(R.string.no_model_selected)
                }
                
                ExposedDropdownMenuBox(
                    expanded = activeMenu == "model",
                    onExpandedChange = { }
                ) {
                    TextButton(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (activeMenu == "model") {
                                activeMenu = null
                            } else if (now - lastModelDismissTime > 200) {
                                activeMenu = "model"
                            }
                        },
                        modifier = Modifier.height(38.dp).widthIn(max = 160.dp).menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text(
                            displayText,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isModelValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    
                    ExposedDropdownMenu(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        expanded = activeMenu == "model", 
                        onDismissRequest = { 
                            if (activeMenu == "model") {
                                activeMenu = null
                                lastModelDismissTime = System.currentTimeMillis()
                            }
                        },
                        matchTextFieldWidth = false,
                        shape = CHAT_DROPDOWN_MENU_SHAPE,
                    ) {
                        if (enabledModels.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.models_no_models)) },
                                onClick = {
                                    activeMenu = null
                                    lastModelDismissTime = 0L // Reset to allow immediate re-open
                                },
                                enabled = false
                            )
                        } else {
                            // Grouped by provider, then alphabetical. enabledModels is a Set whose
                            // iteration order is insertion order (i.e. whenever each model was
                            // enabled), which scrambles providers together in the picker.
                            val sortedModels = remember(enabledModels, customProviders) {
                                enabledModels.sortedWith(
                                    compareBy(
                                        {
                                            providerDisplayName(
                                                com.newoether.agora.model.ModelId.parse(it).providerName,
                                                customProviders,
                                            ).lowercase()
                                        },
                                        { com.newoether.agora.model.ModelId.parse(it).apiModelName.lowercase() },
                                    )
                                )
                            }
                            sortedModels.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Text(modelDisplayName(model, modelAliases, customProviders))
                                    },
                                    onClick = {
                                        haptics.selection()
                                        onModelSelect(model)
                                        activeMenu = null
                                        lastModelDismissTime = 0L
                                    }
                                )
                            }
                        }
                    }
                }
                
                val contextProgressColor = if (
                    contextUsage.exceedsCompactThreshold(contextCompactThresholdPercent)
                ) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
                val contextProgressTarget = contextUsage.progress
                val contextProgress by animateFloatAsState(
                    targetValue = contextProgressTarget,
                    animationSpec = if (motionPolicy.allowContinuousMotion) {
                        tween(durationMillis = 400)
                    } else {
                        snap()
                    },
                    label = "contextProgress",
                )
                ExposedDropdownMenuBox(
                    expanded = activeMenu == "context",
                    onExpandedChange = { },
                ) {
                    IconButton(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (activeMenu == "context") {
                                activeMenu = null
                            } else if (now - lastContextDismissTime > 200) {
                                activeMenu = "context"
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true,
                            ),
                    ) {
                        CircularProgressIndicator(
                            progress = { contextProgress },
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = contextProgressColor,
                        )
                    }
                    ExposedDropdownMenu(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        expanded = activeMenu == "context",
                        onDismissRequest = {
                            if (activeMenu == "context") {
                                activeMenu = null
                                lastContextDismissTime = System.currentTimeMillis()
                            }
                        },
                        matchTextFieldWidth = false,
                        shape = CHAT_DROPDOWN_MENU_SHAPE,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.context_title),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            CircularProgressIndicator(
                                progress = { contextProgress },
                                modifier = Modifier.size(36.dp).align(Alignment.CenterHorizontally),
                                strokeWidth = 4.dp,
                                color = contextProgressColor,
                            )
                            Text(
                                text = stringResource(
                                    R.string.context_usage_messages,
                                    ContextBudget.compactLabel(contextUsage.estimatedTokenCount),
                                    ContextBudget.compactLabel(contextUsage.tokenBudget),
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                ChatComposerToolsMenu(
                    expanded = activeMenu == "tools",
                    onExpandedChange = { expanded ->
                        val now = System.currentTimeMillis()
                        if (!expanded) {
                            activeMenu = null
                            lastToolsDismissTime = now
                        } else if (now - lastToolsDismissTime > 200) {
                            activeMenu = "tools"
                        }
                    },
                    onDismissRequest = {
                        if (activeMenu == "tools") {
                            activeMenu = null
                            lastToolsDismissTime = System.currentTimeMillis()
                        }
                    },
                    isModelValid = isModelValid,
                    capabilities = capabilities,
                    thinkingEnabled = thinkingEnabled,
                    thinkingLevel = thinkingLevel,
                    thinkingBudgetEnabled = thinkingBudgetEnabled,
                    thinkingBudgetTokens = thinkingBudgetTokens,
                    onThinkingToggle = onThinkingToggle,
                    onOpenThinkingSheet = { showThinkingSheet = true },
                    codeExecutionEnabled = codeExecutionEnabled,
                    onCodeExecutionToggle = onCodeExecutionToggle,
                    googleSearchEnabled = googleSearchEnabled,
                    onGoogleSearchToggle = onGoogleSearchToggle,
                    openAiWebSearchAvailable = openAiWebSearchAvailable,
                    openAiWebSearchEnabled = openAiWebSearchEnabled,
                    onOpenAiWebSearchToggle = onOpenAiWebSearchToggle,
                    openAiServiceTierAvailable = openAiServiceTierAvailable,
                    openAiServiceTierEnabled = openAiServiceTierEnabled,
                    openAiServiceTier = openAiServiceTier,
                    onOpenAiServiceTierToggle = onOpenAiServiceTierToggle,
                    onOpenOpenAiServiceTierSheet = { showOpenAiServiceTierSheet = true },
                    showWebSearch = showWebSearch,
                    webSearchEnabled = webSearchEnabled,
                    onWebSearchToggle = onWebSearchToggle,
                    showShell = showShell,
                    shellEnabled = shellEnabled,
                    onShellToggle = onShellToggle,
                    canCompact = canCompact,
                    isCompacting = isCompacting,
                    onCompactClick = onCompactClick,
                    onAdvancedClick = onAdvancedClick,
                )
            }
            ComposerSendButton(
                textFieldState = textFieldState,
                composer = composer,
                isLoading = isLoading,
                isSwitching = isSwitching,
                isStopping = isStopping,
                isModelValid = isModelValid,
                onSendMessage = onSendMessage,
                onStopGeneration = onStopGeneration,
                onCollapse = onCollapse,
            )
        }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 4.dp, top = 4.dp)
        ) {
            val elevatedSurface = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            IconButton(
                onClick = { if (!isExpandAnimating) onCollapse() },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.radialGradient(
                            listOf<Color>(
                                elevatedSurface,
                                elevatedSurface.copy(alpha = 0.5f),
                                Color.Transparent,
                            ),
                        ),
                        CircleShape,
                    ),
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.collapse_all_24px),
                    contentDescription = stringResource(R.string.collapse),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                )
            }
        }
    }

    ChatBottomBarOverlayHost(
        showThinkingSheet = showThinkingSheet,
        onDismissThinkingSheet = { showThinkingSheet = false },
        thinkingEnabled = thinkingEnabled,
        thinkingLevel = thinkingLevel,
        thinkingBudgetEnabled = thinkingBudgetEnabled,
        thinkingBudgetTokens = thinkingBudgetTokens,
        onThinkingToggle = onThinkingToggle,
        onThinkingLevelChange = onThinkingLevelChange,
        onThinkingBudgetEnabledChange = onThinkingBudgetEnabledChange,
        onThinkingBudgetTokensChange = onThinkingBudgetTokensChange,
        selectedModel = selectedModel,
        customProviders = customProviders,
        showOpenAiServiceTierSheet = showOpenAiServiceTierSheet,
        openAiServiceTierAvailable = openAiServiceTierAvailable,
        onDismissOpenAiServiceTierSheet = { showOpenAiServiceTierSheet = false },
        openAiServiceTierEnabled = openAiServiceTierEnabled,
        openAiServiceTier = openAiServiceTier,
        onOpenAiServiceTierToggle = onOpenAiServiceTierToggle,
        onOpenAiServiceTierChange = onOpenAiServiceTierChange,
        internalCameraPath = internalCameraPath,
        onInternalCameraPathChange = { internalCameraPath = it },
        composer = composer,
        pdfViewerSelection = pdfViewerSelection,
        onTogglePdfSelection = onTogglePdfSelection,
        onPdfPreviewSelect = onPdfPreviewSelect,
        onInitPdfSelection = onInitPdfSelection,
    )

}
