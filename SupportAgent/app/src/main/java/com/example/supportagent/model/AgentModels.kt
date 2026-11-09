package com.example.supportagent.model

import java.util.UUID

enum class Sender {
    USER,
    AGENT
}

enum class ActionCategory {
    SUPPORT_CALL,
    FOOD_DELIVERY,
    GROCERY_RESTOCK,
    TRAVEL_RENTAL,
    LODGING_RESERVATION,
    DISPUTE_REFUND
}

enum class ActionIconType {
    PHONE,
    RESTAURANT,
    GROCERY,
    CAR,
    HOTEL,
    SHIELD
}

enum class ActionStatus {
    PROPOSED,
    EXECUTING,
    REQUIRES_USER_DECISION,
    COMPLETED,
    CANCELLED,
    FAILED
}

enum class StepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ALERT
}

data class MissionStep(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val detail: String = "",
    val status: StepStatus = StepStatus.PENDING
)

data class TranscriptEntry(
    val speaker: String,
    val text: String,
    val timestamp: String
)

data class NegotiationOffer(
    val id: String = UUID.randomUUID().toString(),
    val company: String,
    val originalPrice: String,
    val offeredPrice: String,
    val duration: String,
    val details: String
)

data class MissionExecution(
    val id: String = UUID.randomUUID().toString(),
    val companyName: String,
    val contactTarget: String,
    val currentStepIndex: Int = 0,
    val steps: List<MissionStep> = emptyList(),
    val liveTranscript: List<TranscriptEntry> = emptyList(),
    val isHolding: Boolean = false,
    val pendingOffer: NegotiationOffer? = null,
    val resultSummary: String? = null
)

data class ActionProposal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: ActionCategory,
    val categoryBadge: String,
    val targetEntity: String,
    val targetContact: String,
    val estimatedTime: String,
    val strategySteps: List<String>,
    val guardrails: String,
    val actionButtonText: String,
    val actionIcon: ActionIconType,
    val requiresBiometric: Boolean = false,
    val totalAmount: String? = null,
    val cartItems: List<String> = emptyList(),
    val status: ActionStatus = ActionStatus.PROPOSED,
    val execution: MissionExecution? = null
)

data class AgentMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val timestamp: String,
    val actionProposal: ActionProposal? = null,
    val groceryDeck: GroceryCartDeck? = null
)
