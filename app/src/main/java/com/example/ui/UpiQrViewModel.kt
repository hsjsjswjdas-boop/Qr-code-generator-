package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UpiUtils
import com.example.data.model.QrHistoryItem
import com.example.data.model.SavedProfile
import com.example.data.model.UpiPayment
import com.example.data.qr.QrCodeGenerator
import com.example.data.storage.UpiStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UpiQrUiState(
    val vpa: String = "",
    val payeeName: String = "",
    val amount: String = "",
    val note: String = "",
    val vpaError: String? = null,
    val amountError: String? = null,
    val isBengali: Boolean = true,
    val qrColorHex: String = "#0E7A5F",
    val qrBitmap: Bitmap? = null,
    val posterBitmap: Bitmap? = null,
    val activeTab: Int = 0,
    val savedProfiles: List<SavedProfile> = emptyList(),
    val history: List<QrHistoryItem> = emptyList(),
    val isFullscreen: Boolean = false,
    val toastMessage: String? = null,
    val isSavingProfileDialogVisible: Boolean = false
) {
    val isValid: Boolean
        get() = vpa.isNotBlank() && vpaError == null && amountError == null && UpiPayment.isValidVpa(vpa)

    fun currentPayment(): UpiPayment {
        return UpiPayment(
            vpa = vpa.trim(),
            payeeName = payeeName.trim(),
            amount = amount.trim(),
            note = note.trim()
        )
    }
}

class UpiQrViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = UpiStorage(application)

    private val _uiState = MutableStateFlow(
        UpiQrUiState(
            isBengali = storage.getLanguage() == "bn",
            qrColorHex = storage.getQrColorHex(),
            savedProfiles = storage.getSavedProfiles(),
            history = storage.getHistory()
        )
    )
    val uiState: StateFlow<UpiQrUiState> = _uiState.asStateFlow()

    init {
        // Pre-fill with first saved profile for instant convenience if available
        val defaultProfile = _uiState.value.savedProfiles.firstOrNull()
        if (defaultProfile != null) {
            _uiState.update {
                it.copy(
                    vpa = defaultProfile.vpa,
                    payeeName = defaultProfile.payeeName
                )
            }
            regenerateQr()
        }
    }

    fun onVpaChanged(newVpa: String) {
        val trimmed = newVpa.trim()
        val error = if (trimmed.isNotBlank() && !UpiPayment.isValidVpa(trimmed)) {
            if (_uiState.value.isBengali) "সঠিক UPI আইডি দিন (যেমন: name@bank)" else "Enter a valid UPI ID (e.g. name@bank)"
        } else {
            null
        }

        _uiState.update {
            it.copy(vpa = newVpa, vpaError = error)
        }
        regenerateQr()
    }

    fun onPayeeNameChanged(newName: String) {
        if (newName.length <= 60) {
            _uiState.update { it.copy(payeeName = newName) }
            regenerateQr()
        }
    }

    fun onAmountChanged(newAmount: String) {
        val trimmed = newAmount.trim().replace(",", "")
        val error = if (trimmed.isNotBlank() && !UpiPayment.isValidAmount(trimmed)) {
            if (_uiState.value.isBengali) "সঠিক টাকার অঙ্ক দিন" else "Enter a valid amount"
        } else {
            null
        }

        _uiState.update {
            it.copy(amount = trimmed, amountError = error)
        }
        regenerateQr()
    }

    fun onQuickAmountSelected(amountStr: String) {
        val currentAmt = _uiState.value.amount
        val nextAmt = if (currentAmt == amountStr) "" else amountStr
        onAmountChanged(nextAmt)
    }

    fun onNoteChanged(newNote: String) {
        if (newNote.length <= 50) {
            _uiState.update { it.copy(note = newNote) }
            regenerateQr()
        }
    }

    fun setQrColor(hex: String) {
        storage.setQrColorHex(hex)
        _uiState.update { it.copy(qrColorHex = hex) }
        regenerateQr()
    }

    fun toggleLanguage() {
        val nextIsBengali = !_uiState.value.isBengali
        storage.setLanguage(if (nextIsBengali) "bn" else "en")
        _uiState.update { it.copy(isBengali = nextIsBengali) }
        regenerateQr()
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun clearAll() {
        _uiState.update {
            it.copy(
                vpa = "",
                payeeName = "",
                amount = "",
                note = "",
                vpaError = null,
                amountError = null,
                qrBitmap = null,
                posterBitmap = null,
                toastMessage = if (it.isBengali) "তথ্য মুছে ফেলা হয়েছে" else "Cleared"
            )
        }
    }

    fun copyPaymentLink(): String? {
        val payment = _uiState.value.currentPayment()
        val uri = payment.buildUri()
        if (uri.isBlank()) return null

        UpiUtils.copyToClipboard(getApplication(), uri, "UPI Payment Link")
        recordHistory(payment)
        _uiState.update {
            it.copy(
                toastMessage = if (it.isBengali) "পেমেন্ট লিংক ক্লিপবোর্ডে কপি করা হয়েছে" else "Payment link copied to clipboard"
            )
        }
        return uri
    }

    fun shareQrCode() {
        val state = _uiState.value
        val payment = state.currentPayment()
        if (payment.buildUri().isBlank()) return

        recordHistory(payment)
        viewModelScope.launch {
            val poster = withContext(Dispatchers.Default) {
                QrCodeGenerator.generatePosterBitmap(payment, state.isBengali)
            }
            if (poster != null) {
                UpiUtils.shareBitmap(getApplication(), poster, "UPI Payment QR - ${payment.payeeName}")
            }
        }
    }

    fun saveQrToGallery() {
        val state = _uiState.value
        val payment = state.currentPayment()
        if (payment.buildUri().isBlank()) return

        recordHistory(payment)
        viewModelScope.launch {
            val poster = withContext(Dispatchers.Default) {
                QrCodeGenerator.generatePosterBitmap(payment, state.isBengali)
            }
            if (poster != null) {
                val success = UpiUtils.saveBitmapToGallery(getApplication(), poster, payment.payeeName)
                _uiState.update {
                    it.copy(
                        toastMessage = if (success) {
                            if (it.isBengali) "QR কোড সফলভাবে গ্যালারিতে সেভ হয়েছে" else "QR poster saved to Gallery"
                        } else {
                            if (it.isBengali) "ছবি সেভ করতে ব্যর্থ হয়েছে" else "Failed to save image"
                        }
                    )
                }
            }
        }
    }

    fun testInUpiApp(): Boolean {
        val payment = _uiState.value.currentPayment()
        val uri = payment.buildUri()
        if (uri.isBlank()) return false

        recordHistory(payment)
        val launched = UpiUtils.launchUpiIntent(getApplication(), uri)
        if (!launched) {
            _uiState.update {
                it.copy(
                    toastMessage = if (it.isBengali) "কোনো UPI পেমেন্ট অ্যাপ পাওয়া যায়নি" else "No UPI payment app found"
                )
            }
        }
        return launched
    }

    fun showSaveProfileDialog(show: Boolean) {
        _uiState.update { it.copy(isSavingProfileDialogVisible = show) }
    }

    fun saveCurrentAsProfile(label: String) {
        val state = _uiState.value
        if (state.vpa.isBlank()) return

        val newProfile = SavedProfile(
            label = label.ifBlank { state.payeeName.ifBlank { state.vpa } },
            vpa = state.vpa.trim(),
            payeeName = state.payeeName.trim()
        )
        storage.addProfile(newProfile)
        _uiState.update {
            it.copy(
                savedProfiles = storage.getSavedProfiles(),
                isSavingProfileDialogVisible = false,
                toastMessage = if (it.isBengali) "প্রোফাইল সংরক্ষিত হয়েছে" else "Profile saved"
            )
        }
    }

    fun loadProfile(profile: SavedProfile) {
        _uiState.update {
            it.copy(
                vpa = profile.vpa,
                payeeName = profile.payeeName,
                activeTab = 0,
                toastMessage = if (it.isBengali) "${profile.label} লোড করা হয়েছে" else "Loaded ${profile.label}"
            )
        }
        regenerateQr()
    }

    fun deleteProfile(id: String) {
        storage.deleteProfile(id)
        _uiState.update {
            it.copy(
                savedProfiles = storage.getSavedProfiles(),
                toastMessage = if (it.isBengali) "প্রোফাইল মুছে ফেলা হয়েছে" else "Profile deleted"
            )
        }
    }

    fun loadHistoryItem(item: QrHistoryItem) {
        _uiState.update {
            it.copy(
                vpa = item.vpa,
                payeeName = item.payeeName,
                amount = item.amount,
                note = item.note,
                activeTab = 0,
                toastMessage = if (it.isBengali) "পূর্বের QR লোড করা হয়েছে" else "Loaded QR from history"
            )
        }
        regenerateQr()
    }

    fun clearHistory() {
        storage.clearHistory()
        _uiState.update {
            it.copy(
                history = emptyList(),
                toastMessage = if (it.isBengali) "ইতিহাস মুছে ফেলা হয়েছে" else "History cleared"
            )
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun recordHistory(payment: UpiPayment) {
        val item = QrHistoryItem(
            vpa = payment.vpa,
            payeeName = payment.payeeName,
            amount = payment.amount,
            note = payment.note
        )
        storage.addToHistory(item)
        _uiState.update { it.copy(history = storage.getHistory()) }
    }

    private fun regenerateQr() {
        val state = _uiState.value
        val payment = state.currentPayment()
        val upiUri = payment.buildUri()

        if (upiUri.isBlank() || !UpiPayment.isValidVpa(payment.vpa)) {
            _uiState.update { it.copy(qrBitmap = null) }
            return
        }

        val darkColor = try {
            Color.parseColor(state.qrColorHex)
        } catch (e: Exception) {
            Color.parseColor("#0E7A5F")
        }

        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = QrCodeGenerator.generateQrBitmap(
                content = upiUri,
                size = 720,
                darkColor = darkColor,
                includeCenterBadge = true
            )
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(qrBitmap = bitmap) }
            }
        }
    }
}
