package com.example.voicecareagent.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicecareagent.data.ContactsRepository
import com.example.voicecareagent.data.CustomerCareRepository
import com.example.voicecareagent.model.*
import com.example.voicecareagent.telephony.ActiveCallState
import com.example.voicecareagent.telephony.CallVoiceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val callMode: CallMode = CallMode.FRIENDS_AND_CONTACTS,
    val contacts: List<FriendContact> = ContactsRepository.defaultContacts,
    val selectedContact: FriendContact = ContactsRepository.defaultContacts.first(),
    val contactSearchQuery: String = "",
    val isContactSearchExpanded: Boolean = false,
    val personalTopic: String = ContactsRepository.presetTopics.first(),
    val relationshipTone: String = "Warm & Casual",
    val presetTopics: List<String> = ContactsRepository.presetTopics,

    // Customer Care mode states
    val targets: List<CustomerCareTarget> = CustomerCareRepository.defaultTargets,
    val selectedTarget: CustomerCareTarget = CustomerCareRepository.defaultTargets.first(),
    val missionType: MissionType = MissionType.MISSING_ITEMS_REFUND,
    val orderNumber: String = "#PV-7214",
    val customerName: String = "Sree Ram Varma",
    val customerPhone: String = "+1 (561) 555-0192",
    val customerAddress: String = "14401 S Military Trail, Boca Raton, FL 33431",
    val itemDetails: String = "Organic Eggs arrived crushed ($4.49) & Marketside Bread missing ($1.98)",
    val requestedAmount: Double = 6.47,
    val totalRefundsRecovered: Double = 142.50,

    val showCallSummaryDialog: Boolean = false,
    val showCustomTargetDialog: Boolean = false,
    val showCustomContactDialog: Boolean = false,
    val showContactPickerDialog: Boolean = false,
    val phonebookContactsForPicker: List<FriendContact> = emptyList(),
    val pickerSearchQuery: String = "",
    val pendingPickerSelection: Set<String> = emptySet(),
    val isPickerLoading: Boolean = false,

    val pastCallRecords: List<CallRecord> = listOf(
        CallRecord(
            callMode = CallMode.FRIENDS_AND_CONTACTS,
            targetName = "Alex (Test Friend)",
            phoneNumber = "+1 (555) 014-9922",
            topicOrMission = "Coordinate dinner and weekend plans",
            durationSeconds = 120,
            outcomeSummary = "Agreed on Saturday 7 PM at Italian restaurant",
            dateFormatted = "Today • 11:40 AM"
        ),
        CallRecord(
            callMode = CallMode.CUSTOMER_CARE,
            targetName = "Walmart Customer Care",
            phoneNumber = "1-800-925-6278",
            topicOrMission = "Missing Items Refund",
            durationSeconds = 195,
            outcomeSummary = "Full refund approved for crushed eggs and missing bread",
            refundSecured = 6.47,
            confirmationCode = "REF-918274",
            dateFormatted = "Yesterday • 2:15 PM"
        )
    )
) {
    val filteredContacts: List<FriendContact>
        get() {
            val q = contactSearchQuery.trim()
            if (q.isBlank()) return contacts
            val cleanQ = q.replace("[^0-9+]".toRegex(), "")
            return contacts.filter { contact ->
                contact.name.contains(q, ignoreCase = true) ||
                (cleanQ.isNotBlank() && contact.phoneNumber.replace("[^0-9+]".toRegex(), "").contains(cleanQ))
            }
        }

    val filteredPickerContacts: List<FriendContact>
        get() {
            val q = pickerSearchQuery.trim()
            if (q.isBlank()) return phonebookContactsForPicker
            val cleanQ = q.replace("[^0-9+]".toRegex(), "")
            return phonebookContactsForPicker.filter {
                it.name.contains(q, ignoreCase = true) ||
                (cleanQ.isNotBlank() && it.phoneNumber.replace("[^0-9+]".toRegex(), "").contains(cleanQ))
            }
        }
}

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    val voiceEngine = CallVoiceEngine(application)
    val callState: StateFlow<ActiveCallState> = voiceEngine.callState

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadDeviceContacts()
    }

    fun switchCallMode(mode: CallMode) {
        _uiState.update { it.copy(callMode = mode) }
    }

    fun selectContact(contact: FriendContact) {
        _uiState.update { it.copy(selectedContact = contact) }
    }

    fun setPersonalTopic(topic: String) {
        _uiState.update { it.copy(personalTopic = topic) }
    }

    fun setRelationshipTone(tone: String) {
        _uiState.update { it.copy(relationshipTone = tone) }
    }

    fun selectTarget(target: CustomerCareTarget) {
        _uiState.update { it.copy(selectedTarget = target) }
    }

    fun selectMissionType(type: MissionType) {
        val (defaultItems, defaultAmount) = when (type) {
            MissionType.MISSING_ITEMS_REFUND -> Pair("Organic Eggs arrived crushed ($4.49) & Marketside Bread missing ($1.98)", 6.47)
            MissionType.LATE_DELIVERY_COMPENSATION -> Pair("Delivery arrived 65 minutes past the guaranteed window", 9.99)
            MissionType.CANCEL_SUBSCRIPTION -> Pair("Cancel recurring subscription without early termination fee", 0.00)
            MissionType.BILL_NEGOTIATION -> Pair("Negotiate lower promotional monthly rate for standard internet", 25.00)
            MissionType.DISPUTE_CHARGE -> Pair("Duplicate delivery surcharge of $8.50 billed twice", 8.50)
            MissionType.CUSTOM_REQUEST -> Pair("Inquire regarding pending replacement and account credit", 5.00)
        }
        _uiState.update {
            it.copy(
                missionType = type,
                itemDetails = defaultItems,
                requestedAmount = defaultAmount
            )
        }
    }

    fun updateOrderNumber(orderNum: String) {
        _uiState.update { it.copy(orderNumber = orderNum) }
    }

    fun updateItemDetails(details: String) {
        _uiState.update { it.copy(itemDetails = details) }
    }

    fun updateRequestedAmount(amount: Double) {
        _uiState.update { it.copy(requestedAmount = amount) }
    }

    fun initiateCall() {
        val state = _uiState.value
        val mission = if (state.callMode == CallMode.FRIENDS_AND_CONTACTS) {
            CallMission(
                callMode = CallMode.FRIENDS_AND_CONTACTS,
                friendContact = state.selectedContact,
                personalTopic = state.personalTopic,
                relationshipTone = state.relationshipTone,
                customerName = state.customerName,
                customerPhone = state.customerPhone
            )
        } else {
            CallMission(
                callMode = CallMode.CUSTOMER_CARE,
                target = state.selectedTarget,
                missionType = state.missionType,
                orderOrAccountNumber = state.orderNumber,
                customerName = state.customerName,
                customerPhone = state.customerPhone,
                itemDetails = state.itemDetails,
                requestedAmount = state.requestedAmount
            )
        }
        voiceEngine.startCallMission(mission)
    }

    fun whisperToAgent(whisper: String) {
        voiceEngine.whisperToAgent(whisper)
    }

    fun toggleTakeover() {
        voiceEngine.toggleTakeover()
    }

    fun toggleMute() {
        voiceEngine.toggleMute()
    }

    fun openSystemDialer() {
        voiceEngine.openSystemDialer()
    }

    fun sendCalleeSpeech(speechText: String) {
        voiceEngine.handleCalleeSpeech(speechText)
    }

    fun endCall() {
        val currentCall = callState.value
        voiceEngine.stopCall()

        val record = CallRecord(
            callMode = currentCall.mission?.callMode ?: _uiState.value.callMode,
            targetName = currentCall.mission?.calleeDisplayName ?: "Contact",
            phoneNumber = currentCall.mission?.calleePhoneNumber ?: "",
            topicOrMission = if (currentCall.mission?.callMode == CallMode.FRIENDS_AND_CONTACTS) {
                currentCall.mission.personalTopic
            } else {
                currentCall.mission?.missionType?.label ?: "Resolution"
            },
            durationSeconds = currentCall.durationSeconds,
            outcomeSummary = if (currentCall.mission?.callMode == CallMode.FRIENDS_AND_CONTACTS) {
                "Call completed on '${currentCall.mission.personalTopic}'"
            } else {
                "Full refund approved under ${currentCall.confirmationCode}"
            },
            refundSecured = currentCall.resolutionAmount,
            confirmationCode = currentCall.confirmationCode,
            dateFormatted = "Just now",
            transcript = currentCall.transcript
        )

        _uiState.update {
            it.copy(
                pastCallRecords = listOf(record) + it.pastCallRecords,
                totalRefundsRecovered = if (currentCall.resolutionAmount > 0.0) it.totalRefundsRecovered + currentCall.resolutionAmount else it.totalRefundsRecovered,
                showCallSummaryDialog = true
            )
        }
    }

    fun dismissSummary() {
        _uiState.update { it.copy(showCallSummaryDialog = false) }
        voiceEngine.clearCall()
    }

    fun openCustomContactDialog() {
        _uiState.update { it.copy(showCustomContactDialog = true) }
    }

    fun closeCustomContactDialog() {
        _uiState.update { it.copy(showCustomContactDialog = false) }
    }

    fun addCustomContact(name: String, phone: String, relation: String) {
        if (name.isBlank() || phone.isBlank()) return
        val newContact = FriendContact(
            name = name.trim(),
            phoneNumber = phone.trim(),
            relation = relation.ifBlank { "Contact" },
            avatarColorHex = 0xFF5856D6
        )
        _uiState.update {
            it.copy(
                contacts = listOf(newContact) + it.contacts,
                selectedContact = newContact,
                showCustomContactDialog = false
            )
        }
    }

    fun setContactSearchQuery(query: String) {
        _uiState.update { it.copy(contactSearchQuery = query) }
    }

    fun clearContactSearch() {
        _uiState.update { it.copy(contactSearchQuery = "") }
    }

    fun toggleContactSearchExpanded(expanded: Boolean? = null) {
        _uiState.update { it.copy(isContactSearchExpanded = expanded ?: !it.isContactSearchExpanded) }
    }

    fun loadDeviceContacts() {
        // Safe Mode: default to the safe test allowlist to protect real people from unintended calls
        val safeContacts = ContactsRepository.loadSafeTestContacts(getApplication())
        _uiState.update { current ->
            current.copy(
                contacts = safeContacts,
                selectedContact = safeContacts.firstOrNull() ?: current.selectedContact
            )
        }
    }

    fun openContactPicker() {
        _uiState.update { it.copy(showContactPickerDialog = true, isPickerLoading = true, pickerSearchQuery = "") }
        viewModelScope.launch(Dispatchers.IO) {
            val directory = ContactsRepository.loadDeviceDirectoryForPicker(getApplication())
            _uiState.update { it.copy(phonebookContactsForPicker = directory, isPickerLoading = false) }
        }
    }

    fun closeContactPicker() {
        _uiState.update { it.copy(showContactPickerDialog = false, pendingPickerSelection = emptySet(), pickerSearchQuery = "") }
    }

    fun setPickerSearchQuery(query: String) {
        _uiState.update { it.copy(pickerSearchQuery = query) }
    }

    fun togglePickerContactSelection(contactId: String) {
        _uiState.update { current ->
            val updated = if (current.pendingPickerSelection.contains(contactId)) {
                current.pendingPickerSelection - contactId
            } else {
                current.pendingPickerSelection + contactId
            }
            current.copy(pendingPickerSelection = updated)
        }
    }

    fun confirmPickerSelection() {
        val current = _uiState.value
        val toAdd = current.phonebookContactsForPicker.filter { current.pendingPickerSelection.contains(it.id) }
        if (toAdd.isNotEmpty()) {
            val existingPhones = current.contacts.map { it.phoneNumber.replace("[^0-9+]".toRegex(), "") }.toSet()
            val newContacts = toAdd.filter { !existingPhones.contains(it.phoneNumber.replace("[^0-9+]".toRegex(), "")) }
            val updatedList = newContacts + current.contacts
            _uiState.update {
                it.copy(
                    contacts = updatedList,
                    selectedContact = newContacts.firstOrNull() ?: it.selectedContact,
                    showContactPickerDialog = false,
                    pendingPickerSelection = emptySet(),
                    pickerSearchQuery = ""
                )
            }
        } else {
            closeContactPicker()
        }
    }

    fun removeContactFromAllowlist(contact: FriendContact) {
        _uiState.update { current ->
            val updated = current.contacts.filter { it.id != contact.id }
            val fallback = updated.firstOrNull() ?: ContactsRepository.defaultContacts.first()
            current.copy(
                contacts = if (updated.isNotEmpty()) updated else ContactsRepository.defaultContacts,
                selectedContact = if (current.selectedContact.id == contact.id) fallback else current.selectedContact
            )
        }
    }

    fun openCustomTargetDialog() {
        _uiState.update { it.copy(showCustomTargetDialog = true) }
    }

    fun closeCustomTargetDialog() {
        _uiState.update { it.copy(showCustomTargetDialog = false) }
    }

    fun addCustomTarget(name: String, phone: String) {
        if (name.isBlank() || phone.isBlank()) return
        val custom = CustomerCareTarget(
            name = name.trim(),
            phoneNumber = phone.trim(),
            category = ServiceCategory.CUSTOM,
            brandColorHex = 0xFF5E5CE6,
            defaultHoldTime = "Direct Call",
            verifiedSupportLine = false
        )
        _uiState.update {
            it.copy(
                targets = listOf(custom) + it.targets,
                selectedTarget = custom,
                showCustomTargetDialog = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.stopCall()
    }
}
