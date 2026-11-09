package com.example.voicecareagent.model

import java.util.UUID

enum class CallMode(val label: String, val subtitle: String) {
    FRIENDS_AND_CONTACTS("Call Friends & Contacts", "AI calls friends or personal contacts to discuss any topic"),
    CUSTOMER_CARE("Customer Care & Businesses", "AI calls customer support to negotiate refunds, bills & cancellations")
}

data class FriendContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val relation: String = "Friend",
    val avatarColorHex: Long = 0xFF0A84FF
)

enum class ServiceCategory(val displayName: String, val defaultHoldEstimate: String) {
    RETAIL_GROCERY("Grocery & Retail", "~2 mins hold"),
    FOOD_DELIVERY("Food Delivery", "~3 mins hold"),
    TRAVEL_AIRLINES("Airlines & Travel", "~8 mins hold"),
    UTILITIES_CABLE("Internet & Cable", "~6 mins hold"),
    MEMBERSHIP_GYM("Gym & Subscriptions", "~4 mins hold"),
    CUSTOM("Custom Dial", "Direct Call")
}

data class CustomerCareTarget(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val category: ServiceCategory,
    val brandColorHex: Long = 0xFF0071DC,
    val defaultHoldTime: String = "~2 mins",
    val verifiedSupportLine: Boolean = true,
    val promptGuidelines: String = ""
)

enum class MissionType(val label: String, val defaultGoal: String) {
    MISSING_ITEMS_REFUND("Missing / Damaged Items", "Request refund or replacement for damaged/missing grocery or food items"),
    LATE_DELIVERY_COMPENSATION("Late Delivery Credit", "Request delivery fee refund or courtesy credit for excessive delivery delay"),
    CANCEL_SUBSCRIPTION("Cancel Membership", "Cancel gym or recurring subscription without paying cancellation fee"),
    BILL_NEGOTIATION("Lower Monthly Bill", "Negotiate promotional rate or bill reduction for internet/cable service"),
    DISPUTE_CHARGE("Dispute Incorrect Fee", "Request reversal of unauthorized or duplicate charge"),
    CUSTOM_REQUEST("Custom Request", "Talk to customer care according to custom instructions")
}

data class CallMission(
    val id: String = UUID.randomUUID().toString(),
    val callMode: CallMode = CallMode.FRIENDS_AND_CONTACTS,
    val target: CustomerCareTarget? = null,
    val friendContact: FriendContact? = null,
    val missionType: MissionType = MissionType.MISSING_ITEMS_REFUND,
    val personalTopic: String = "Check if free this Saturday around 7 PM for dinner at an Italian restaurant",
    val relationshipTone: String = "Warm & Casual",
    val orderOrAccountNumber: String = "#PV-7214",
    val customerName: String = "Sree Ram Varma",
    val customerPhone: String = "+1 (561) 555-0192",
    val customerEmail: String = "sreeram@example.com",
    val itemDetails: String = "Organic Eggs arrived crushed ($4.49) & Marketside Bread missing ($1.98)",
    val requestedAmount: Double = 6.47,
    val customNotes: String = ""
) {
    val calleeDisplayName: String
        get() = when (callMode) {
            CallMode.FRIENDS_AND_CONTACTS -> friendContact?.name ?: "Contact"
            CallMode.CUSTOMER_CARE -> target?.name ?: "Customer Care"
        }

    val calleePhoneNumber: String
        get() = when (callMode) {
            CallMode.FRIENDS_AND_CONTACTS -> friendContact?.phoneNumber ?: ""
            CallMode.CUSTOMER_CARE -> target?.phoneNumber ?: ""
        }
}

enum class CallStage(val displayTitle: String) {
    IDLE("Ready to Call"),
    DIALING("Dialing Contact..."),
    NAVIGATING_IVR("Navigating Automated Phone Menu..."),
    ON_HOLD("On Hold (Hold Music Detected)"),
    CONNECTED("Connected • Line Open"),
    CONVERSING("In Conversation on Topic..."),
    CONFIRMING_RESOLUTION("Wrapping up & Confirming Next Steps..."),
    COMPLETED("Call Completed • Summary Logged"),
    FAILED("Call Ended")
}

enum class TranscriptSpeaker {
    AI_AGENT,
    CONTACT_OR_REP,
    IVR_AUTOMATION,
    USER_WHISPER
}

data class TranscriptEntry(
    val id: String = UUID.randomUUID().toString(),
    val speaker: TranscriptSpeaker,
    val speakerLabel: String,
    val text: String,
    val timestamp: String
)

data class CallRecord(
    val id: String = UUID.randomUUID().toString(),
    val callMode: CallMode,
    val targetName: String,
    val phoneNumber: String,
    val topicOrMission: String,
    val durationSeconds: Int,
    val outcomeSummary: String,
    val refundSecured: Double = 0.0,
    val confirmationCode: String = "",
    val dateFormatted: String,
    val transcript: List<TranscriptEntry> = emptyList()
)
