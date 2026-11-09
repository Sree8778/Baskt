package com.example.voicecareagent.engine

import com.example.voicecareagent.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConversationStep(
    val stage: CallStage,
    val ivrText: String? = null,
    val otherPartySpeech: String? = null,
    val agentSpeech: String,
    val delayMs: Long = 2000L,
    val resolutionAmount: Double = 0.0,
    val confirmationCode: String = ""
)

object VoiceAgentConversationEngine {

    fun getCurrentTimestamp(): String =
        SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date())

    fun buildConversationScript(mission: CallMission): List<ConversationStep> {
        return when (mission.callMode) {
            CallMode.FRIENDS_AND_CONTACTS -> buildFriendsScript(mission)
            CallMode.CUSTOMER_CARE -> buildCustomerCareScript(mission)
        }
    }

    private fun buildFriendsScript(mission: CallMission): List<ConversationStep> {
        val steps = mutableListOf<ConversationStep>()
        val friendName = mission.friendContact?.name ?: "Friend"
        val topic = mission.personalTopic.ifBlank { "Catch up and discuss plans for this weekend" }
        val callerName = mission.customerName

        // 1. Dialing
        steps.add(
            ConversationStep(
                stage = CallStage.DIALING,
                agentSpeech = "Dialing $friendName at ${mission.calleePhoneNumber}...",
                delayMs = 1500L
            )
        )

        // 2. Friend Answers & Introduction
        steps.add(
            ConversationStep(
                stage = CallStage.CONNECTED,
                otherPartySpeech = "Hello?",
                agentSpeech = "Hi $friendName! This is $callerName's personal AI voice agent calling on his behalf. How are you doing today?",
                delayMs = 2600L
            )
        )

        // 3. Friend reaction & AI states the topic
        steps.add(
            ConversationStep(
                stage = CallStage.CONVERSING,
                otherPartySpeech = "Oh hey! An AI calling for $callerName? That's pretty cool! I'm doing well, what's going on?",
                agentSpeech = "$callerName asked me to call and talk to you about: $topic. He wanted to get your thoughts on it!",
                delayMs = 3000L
            )
        )

        // 4. Friend responds to the topic
        val friendResponse = when {
            topic.lowercase().contains("dinner") || topic.lowercase().contains("lunch") || topic.lowercase().contains("food") ->
                "Oh that sounds awesome! Saturday around 7 PM actually works really well for me. Where were you guys thinking of going?"
            topic.lowercase().contains("tuition") || topic.lowercase().contains("class") ->
                "Yes, that timing works! I have slots open on Tuesday and Thursday evenings from 6 PM to 7 PM. Does that work for the schedule?"
            topic.lowercase().contains("birthday") ->
                "Aww thank you so much! Tell $callerName I really appreciate him thinking of me. That made my day!"
            topic.lowercase().contains("resume") || topic.lowercase().contains("project") || topic.lowercase().contains("document") ->
                "Yes, I saw the documents come through earlier! They look solid. Tell $callerName I'll review the final section tonight."
            else ->
                "Got it! That sounds completely good to me. Tell $callerName I'm on board and we can definitely do that."
        }

        // 5. AI responds and clarifies details
        val agentFollowUp = when {
            topic.lowercase().contains("dinner") || topic.lowercase().contains("lunch") ->
                "He was thinking of that Italian spot on Palmetto Park, or wherever you prefer! Should I confirm with him that Saturday at 7 PM is locked in?"
            topic.lowercase().contains("tuition") || topic.lowercase().contains("class") ->
                "Tuesday and Thursday from 6 to 7 PM sounds ideal. I will relay those exact timings to $callerName right now so he can confirm."
            topic.lowercase().contains("birthday") ->
                "He'll be so glad to hear that! He also wanted to catch up soon in person whenever you have free time this week."
            else ->
                "Fantastic. $callerName also mentioned to let you know he's flexible if anything changes on your end."
        }

        steps.add(
            ConversationStep(
                stage = CallStage.CONVERSING,
                otherPartySpeech = friendResponse,
                agentSpeech = agentFollowUp,
                delayMs = 3200L
            )
        )

        // 6. Agreement & Confirmation
        steps.add(
            ConversationStep(
                stage = CallStage.CONFIRMING_RESOLUTION,
                otherPartySpeech = "Sounds great! Tell $callerName to text me the final details, and I'll see him then. Thanks for calling!",
                agentSpeech = "Will do $friendName! I've noted down all the details and $callerName will get the summary immediately. Have a wonderful rest of your day!",
                delayMs = 2800L
            )
        )

        // 7. Completed
        steps.add(
            ConversationStep(
                stage = CallStage.COMPLETED,
                agentSpeech = "Call with $friendName completed successfully. Topic discussed: '$topic'. Summary logged for $callerName.",
                delayMs = 1500L
            )
        )

        return steps
    }

    private fun buildCustomerCareScript(mission: CallMission): List<ConversationStep> {
        val target = mission.target ?: return emptyList()
        val steps = mutableListOf<ConversationStep>()

        // 1. Dialing & IVR
        steps.add(
            ConversationStep(
                stage = CallStage.DIALING,
                agentSpeech = "Connecting call to ${target.name} at ${target.phoneNumber}...",
                delayMs = 1500L
            )
        )

        steps.add(
            ConversationStep(
                stage = CallStage.NAVIGATING_IVR,
                ivrText = "Thank you for calling ${target.name}. For existing orders or delivery disputes, press 1. For general inquiries, press 2.",
                agentSpeech = "[AI navigating automated menu: Sending tone DTMF 1 for Existing Orders]",
                delayMs = 2500L
            )
        )

        // 2. On Hold / Music Detection
        steps.add(
            ConversationStep(
                stage = CallStage.ON_HOLD,
                ivrText = "[🎶 Gentle orchestral hold music playing • Hold time estimated: ${target.defaultHoldTime}]",
                agentSpeech = "Hold music detected. Keeping line open in background without disturbing you. I will alert you immediately when a live human agent answers.",
                delayMs = 3500L
            )
        )

        // 3. Human Rep Connects
        val repName = listOf("Sarah", "Marcus", "Jessica", "David").random()
        steps.add(
            ConversationStep(
                stage = CallStage.CONNECTED,
                otherPartySpeech = "Thank you for calling ${target.name} Priority Care, my name is $repName. Who do I have the pleasure of speaking with today?",
                agentSpeech = "Hello $repName! My name is VoiceCare, an authorized digital assistant calling on behalf of ${mission.customerName} regarding order ${mission.orderOrAccountNumber}.",
                delayMs = 2800L
            )
        )

        // 4. Identity & Verification
        steps.add(
            ConversationStep(
                stage = CallStage.CONVERSING,
                otherPartySpeech = "Hello VoiceCare. For security verification, could you please confirm the phone number and delivery address on the account?",
                agentSpeech = "Certainly. The verified phone number is ${mission.customerPhone} and the delivery address is 14401 S Military Trail, Boca Raton, FL 33431.",
                delayMs = 2600L
            )
        )

        // 5. Presenting the Problem
        steps.add(
            ConversationStep(
                stage = CallStage.CONVERSING,
                otherPartySpeech = "Thank you, I have verified the account and I see order ${mission.orderOrAccountNumber} right in front of me. How can I assist you with this order today?",
                agentSpeech = "Thank you $repName. When the delivery arrived, ${mission.itemDetails}. As a result, we are requesting a full refund of \$${String.format(Locale.US, "%.2f", mission.requestedAmount)} back to the original payment method.",
                delayMs = 3200L
            )
        )

        // 6. Handling Resistance / Counter Offer
        val lowOffer = mission.requestedAmount * 0.5
        steps.add(
            ConversationStep(
                stage = CallStage.CONVERSING,
                otherPartySpeech = "I am very sorry to hear about those damaged items! Our system currently allows me to issue a \$${String.format(Locale.US, "%.2f", lowOffer)} promotional store credit towards your next order.",
                agentSpeech = "We appreciate you checking that $repName, but because these items were completely unusable upon arrival, store credit does not cover the loss. Under your customer satisfaction policy, we kindly request the full \$${String.format(Locale.US, "%.2f", mission.requestedAmount)} directly credited back to the customer's card.",
                delayMs = 3400L
            )
        )

        // 7. Securing Full Refund
        val refCode = "REF-${(100000..999999).random()}"
        steps.add(
            ConversationStep(
                stage = CallStage.CONFIRMING_RESOLUTION,
                otherPartySpeech = "I completely understand and I appreciate your patience. I have applied a one-time supervisory override to issue the full refund of \$${String.format(Locale.US, "%.2f", mission.requestedAmount)} to the original card. You will see it reflected in 1 to 2 business days. The confirmation code is $refCode.",
                agentSpeech = "Thank you so much $repName! I have recorded confirmation reference $refCode for \$${String.format(Locale.US, "%.2f", mission.requestedAmount)}. We truly appreciate your professional help today. Have a wonderful rest of your day!",
                delayMs = 2800L,
                resolutionAmount = mission.requestedAmount,
                confirmationCode = refCode
            )
        )

        // 8. Completed
        steps.add(
            ConversationStep(
                stage = CallStage.COMPLETED,
                agentSpeech = "Call ended successfully. Full refund of \$${String.format(Locale.US, "%.2f", mission.requestedAmount)} secured under confirmation #$refCode.",
                delayMs = 1500L,
                resolutionAmount = mission.requestedAmount,
                confirmationCode = refCode
            )
        )

        return steps
    }

    /**
     * Dynamically generates an authentic contextual response to what the callee actually speaks on a real call.
     */
    fun generateDynamicFriendResponse(
        calleeText: String,
        mission: CallMission,
        turnNumber: Int,
        whisperGuidance: String? = null
    ): Pair<String, CallStage> {
        val lower = calleeText.lowercase().trim()
        val friendName = mission.friendContact?.name ?: mission.calleeDisplayName
        val callerName = mission.customerName
        val topic = mission.personalTopic

        val whisperAddon = if (!whisperGuidance.isNullOrBlank()) " Also, $callerName asked me to note: $whisperGuidance." else ""

        // 1. Inquiries about identity or caller
        if (lower.contains("who is this") || lower.contains("who are you") || lower.contains("what is this") || lower.contains("who's this")) {
            return Pair("This is $callerName's personal AI voice agent calling on his behalf! He wanted me to reach out to you directly about: $topic.$whisperAddon", CallStage.CONVERSING)
        }

        // 2. Callee is busy or cannot talk
        if (lower.contains("busy") || lower.contains("can't talk") || lower.contains("cant talk") || lower.contains("driving") || lower.contains("in a meeting") || lower.contains("call later") || lower.contains("call me back")) {
            return Pair("Understood completely! I won't hold you up. I will let $callerName know you are busy right now and have him follow up later. Have a great day!$whisperAddon", CallStage.COMPLETED)
        }

        // 3. Agreement / Positive Confirmation
        if (lower.contains("yes") || lower.contains("yeah") || lower.contains("sure") || lower.contains("works") || lower.contains("sounds good") || lower.contains("okay") || lower.contains("ok") || lower.contains("free") || lower.contains("can do")) {
            val response = when {
                topic.lowercase().contains("dinner") || topic.lowercase().contains("lunch") ->
                    "Awesome! I will confirm with $callerName that Saturday around 7 PM is locked in. He was thinking of that Italian restaurant, or he'll text you the details! Thank you $friendName!$whisperAddon"
                topic.lowercase().contains("tuition") || topic.lowercase().contains("class") ->
                    "Wonderful! I've noted down that the schedule works. I will inform $callerName right now so he has it on his calendar. Thanks!$whisperAddon"
                topic.lowercase().contains("birthday") ->
                    "He will be so happy to hear that! Thanks for catching up $friendName, hope you have a fantastic birthday!$whisperAddon"
                else ->
                    "Fantastic! I've recorded your confirmation on $topic. I will notify $callerName right away. Talk to you soon!$whisperAddon"
            }
            return Pair(response, CallStage.COMPLETED)
        }

        // 4. Declining / Not Free
        if (lower.contains("no") || lower.contains("can't") || lower.contains("cant") || lower.contains("not free") || lower.contains("won't work") || lower.contains("busy this weekend")) {
            return Pair("Got it, no problem! Is there another day or time that works better for you that I can pass along to $callerName?$whisperAddon", CallStage.CONVERSING)
        }

        // 5. Initial greeting or question: "Hello?" / "Hey" / "What's up"
        if (turnNumber <= 1 || lower.contains("hello") || lower.contains("hey") || lower.contains("hi") || lower.contains("what's up") || lower.contains("whats up")) {
            return Pair("Hey $friendName! $callerName asked me to call you to check: $topic. How does that sound to you?$whisperAddon", CallStage.CONVERSING)
        }

        // 6. General response addressing their statement
        return Pair("Got it! Regarding \"$calleeText\", I will update $callerName right away so you two are completely aligned. Thank you $friendName!$whisperAddon", CallStage.COMPLETED)
    }
}
