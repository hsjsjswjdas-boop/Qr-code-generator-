package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FullscreenQrDialog
import com.example.ui.components.HistoryView
import com.example.ui.components.ProfilesView
import com.example.ui.components.QrActionsPanel
import com.example.ui.components.QrCodeDisplay
import com.example.ui.components.SaveProfileDialog
import com.example.ui.components.UpiInputForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiQrMainScreen(
    viewModel: UpiQrViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showColorMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissToast()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.isBengali) "UPI পেমেন্ট QR" else "UPI QR Maker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isBengali) "অফলাইন • তাত্ক্ষণিক পেমেন্ট লিঙ্ক" else "Instant & Offline QR Generator",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // QR Color Picker Button
                    Box {
                        IconButton(
                            onClick = { showColorMenu = true },
                            modifier = Modifier.testTag("btn_qr_color_picker")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Choose QR Color",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showColorMenu,
                            onDismissRequest = { showColorMenu = false }
                        ) {
                            val colorOptions = listOf(
                                Triple("#0E7A5F", "Emerald Green", "পান্না সবুজ"),
                                Triple("#14201B", "Classic Black", "কালো"),
                                Triple("#1A365D", "Navy Blue", "গাঢ় নীল"),
                                Triple("#44337A", "Royal Purple", "বেগুনী")
                            )

                            colorOptions.forEach { (hex, enName, bnName) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                                    .border(1.dp, Color.Gray, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(if (state.isBengali) bnName else enName)
                                            if (state.qrColorHex == hex) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setQrColor(hex)
                                        showColorMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Language Toggle Button (বাংলা / ENG)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.toggleLanguage() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("btn_language_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Toggle Language",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isBengali) "বাং | EN" else "EN | বাং",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Navigation Tabs
            TabRow(
                selectedTabIndex = state.activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = state.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text(if (state.isBengali) "QR বানান" else "Create QR") },
                    modifier = Modifier.testTag("tab_create_qr")
                )
                Tab(
                    selected = state.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text(if (state.isBengali) "প্রোফাইল" else "Presets") },
                    modifier = Modifier.testTag("tab_presets")
                )
                Tab(
                    selected = state.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text(if (state.isBengali) "ইতিহাস" else "History") },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            // Tab Content
            when (state.activeTab) {
                0 -> {
                    // QR Generator View (Adaptive for Phone & Tablet)
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWideScreen = maxWidth >= 680.dp

                        if (isWideScreen) {
                            // Two-column side-by-side layout (Tablet/Landscape)
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Left column: Form
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    LeadBanner(isBengali = state.isBengali)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    UpiInputForm(
                                        vpa = state.vpa,
                                        onVpaChanged = viewModel::onVpaChanged,
                                        vpaError = state.vpaError,
                                        payeeName = state.payeeName,
                                        onPayeeNameChanged = viewModel::onPayeeNameChanged,
                                        amount = state.amount,
                                        onAmountChanged = viewModel::onAmountChanged,
                                        amountError = state.amountError,
                                        onQuickAmountSelected = viewModel::onQuickAmountSelected,
                                        note = state.note,
                                        onNoteChanged = viewModel::onNoteChanged,
                                        isBengali = state.isBengali,
                                        onSaveProfileClick = { viewModel.showSaveProfileDialog(true) }
                                    )
                                }

                                // Right column: QR Display & Action buttons
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    QrCodeDisplay(
                                        payment = state.currentPayment(),
                                        qrBitmap = state.qrBitmap,
                                        isBengali = state.isBengali,
                                        onQrClick = { viewModel.setFullscreen(true) }
                                    )
                                    QrActionsPanel(
                                        isValid = state.isValid,
                                        isBengali = state.isBengali,
                                        onCopyLink = { viewModel.copyPaymentLink() },
                                        onShareQr = { viewModel.shareQrCode() },
                                        onSaveToGallery = { viewModel.saveQrToGallery() },
                                        onTestUpiApp = { viewModel.testInUpiApp() },
                                        onFullscreenClick = { viewModel.setFullscreen(true) },
                                        onClearAll = { viewModel.clearAll() }
                                    )
                                }
                            }
                        } else {
                            // Single column layout (Phones)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                LeadBanner(isBengali = state.isBengali)

                                UpiInputForm(
                                    vpa = state.vpa,
                                    onVpaChanged = viewModel::onVpaChanged,
                                    vpaError = state.vpaError,
                                    payeeName = state.payeeName,
                                    onPayeeNameChanged = viewModel::onPayeeNameChanged,
                                    amount = state.amount,
                                    onAmountChanged = viewModel::onAmountChanged,
                                    amountError = state.amountError,
                                    onQuickAmountSelected = viewModel::onQuickAmountSelected,
                                    note = state.note,
                                    onNoteChanged = viewModel::onNoteChanged,
                                    isBengali = state.isBengali,
                                    onSaveProfileClick = { viewModel.showSaveProfileDialog(true) }
                                )

                                QrCodeDisplay(
                                    payment = state.currentPayment(),
                                    qrBitmap = state.qrBitmap,
                                    isBengali = state.isBengali,
                                    onQrClick = { viewModel.setFullscreen(true) }
                                )

                                QrActionsPanel(
                                    isValid = state.isValid,
                                    isBengali = state.isBengali,
                                    onCopyLink = { viewModel.copyPaymentLink() },
                                    onShareQr = { viewModel.shareQrCode() },
                                    onSaveToGallery = { viewModel.saveQrToGallery() },
                                    onTestUpiApp = { viewModel.testInUpiApp() },
                                    onFullscreenClick = { viewModel.setFullscreen(true) },
                                    onClearAll = { viewModel.clearAll() }
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                1 -> {
                    // Presets / Saved Profiles Tab
                    ProfilesView(
                        profiles = state.savedProfiles,
                        isBengali = state.isBengali,
                        onSelectProfile = viewModel::loadProfile,
                        onDeleteProfile = viewModel::deleteProfile
                    )
                }
                2 -> {
                    // History Tab
                    HistoryView(
                        history = state.history,
                        isBengali = state.isBengali,
                        onSelectHistoryItem = viewModel::loadHistoryItem,
                        onClearHistory = viewModel::clearHistory
                    )
                }
            }
        }
    }

    // Fullscreen merchant counter modal
    AnimatedVisibility(
        visible = state.isFullscreen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        FullscreenQrDialog(
            payment = state.currentPayment(),
            qrBitmap = state.qrBitmap,
            isBengali = state.isBengali,
            onDismiss = { viewModel.setFullscreen(false) }
        )
    }

    // Save Preset Profile Dialog
    if (state.isSavingProfileDialogVisible) {
        SaveProfileDialog(
            initialLabel = state.payeeName.ifBlank { state.vpa },
            vpa = state.vpa,
            isBengali = state.isBengali,
            onConfirm = viewModel::saveCurrentAsProfile,
            onDismiss = { viewModel.showSaveProfileDialog(false) }
        )
    }
}

@Composable
private fun LeadBanner(isBengali: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isBengali) "UPI পেমেন্ট QR বানান" else "Create UPI Payment QR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isBengali)
                    "আপনার UPI আইডি আর টাকার অঙ্ক দিন। গ্রাহক যেকোনো UPI অ্যাপ (GPay, PhonePe, Paytm) দিয়ে স্ক্যান করলেই টাকা পাঠাতে পারবেন।"
                else
                    "Enter your UPI ID and amount. Customers can scan with any UPI app to transfer money directly into your bank account.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
