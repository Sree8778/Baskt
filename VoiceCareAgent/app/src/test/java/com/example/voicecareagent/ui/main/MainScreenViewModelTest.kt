package com.example.voicecareagent.ui.main

import com.example.voicecareagent.data.CustomerCareRepository
import com.example.voicecareagent.engine.VoiceAgentConversationEngine
import com.example.voicecareagent.model.CallMission
import com.example.voicecareagent.model.CallMode
import com.example.voicecareagent.model.MissionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceAgentConversationEngineTest {

    @Test
    fun buildNegotiationScript_generatesCompleteLifecycle() {
        val target = CustomerCareRepository.defaultTargets.first()
        val mission = CallMission(
            callMode = CallMode.CUSTOMER_CARE,
            target = target,
            missionType = MissionType.MISSING_ITEMS_REFUND,
            orderOrAccountNumber = "#PV-7214",
            requestedAmount = 6.47
        )

        val script = VoiceAgentConversationEngine.buildConversationScript(mission)

        assertTrue(script.isNotEmpty())
        assertEquals("Walmart Customer Care", mission.target?.name)
        val finalStep = script.last()
        assertEquals(6.47, finalStep.resolutionAmount, 0.001)
        assertTrue(finalStep.confirmationCode.startsWith("REF-"))
    }
}
