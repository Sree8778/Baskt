package com.example.supportagent.engine

import com.example.supportagent.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object SupportAgentEngine {

    fun getCurrentTimestamp(): String =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

    fun processPrompt(query: String): AgentMessage {
        val lower = query.lowercase()

        return when {
            // 1. Food Delivery (Uber Eats / Chipotle)
            lower.contains("uber eats") || lower.contains("chipotle") || lower.contains("food") || lower.contains("dinner") || lower.contains("lunch") -> {
                createUberEatsProposal()
            }
            // 2. Groceries (Walmart / Costco)
            lower.contains("walmart") || lower.contains("costco") || lower.contains("grocery") || lower.contains("milk") || lower.contains("eggs") -> {
                createGroceryProposal()
            }
            // 3. Car Rental (Turo / Hertz)
            lower.contains("car rental") || lower.contains("rental") || lower.contains("turo") || lower.contains("hertz") || lower.contains("suv") -> {
                createCarRentalProposal()
            }
            // 4. Lodging (Airbnb / Cabin)
            lower.contains("airbnb") || lower.contains("hotel") || lower.contains("cabin") || lower.contains("stay") -> {
                createLodgingProposal()
            }
            // 5. Gym Cancellation (Planet Fitness)
            lower.contains("gym") || lower.contains("planet fitness") -> {
                createGymCancellationProposal()
            }
            // 6. Internet / Cable Bill (Comcast / Xfinity)
            lower.contains("comcast") || lower.contains("xfinity") || lower.contains("internet") || lower.contains("cable") -> {
                createComcastProposal()
            }
            // 7. Disputes & Charges
            lower.contains("dispute") || lower.contains("refund") || lower.contains("charge") -> {
                createDisputeProposal(query)
            }
            else -> {
                createGenericProposal(query)
            }
        }
    }

    private fun createUberEatsProposal(): AgentMessage {
        val proposal = ActionProposal(
            title = "Assemble Chipotle Cart on Uber Eats",
            category = ActionCategory.FOOD_DELIVERY,
            categoryBadge = "FOOD DELIVERY • UBER EATS",
            targetEntity = "Chipotle Mexican Grill",
            targetContact = "In-App Ghost Session",
            estimatedTime = "Instant Cart Sync",
            strategySteps = listOf(
                "Launch Uber Eats app via Ghost Touch",
                "Navigate to nearest open Chipotle Mexican Grill",
                "Assemble Chicken Burrito Bowl (Brown rice, black beans, fajita veggies, salsa)",
                "Tap 'Add to order' & open Cart for your review"
            ),
            guardrails = "Strict dietary profile: brown rice, black beans, salsa. Add to cart only.",
            actionButtonText = "🌯 Add Chipotle to Cart",
            actionIcon = ActionIconType.RESTAURANT,
            requiresBiometric = false,
            totalAmount = null,
            cartItems = listOf("Chicken Burrito Bowl")
        )

        return AgentMessage(
            sender = Sender.AGENT,
            text = "I prepared your Chipotle order for Uber Eats: Chicken Burrito Bowl with brown rice, black beans, fajita veggies, and fresh salsa.\n\nTap below and Ghost Touch will open Uber Eats, configure the bowl, and place it directly in your cart for review.",
            timestamp = getCurrentTimestamp(),
            actionProposal = proposal
        )
    }

    private fun createGroceryProposal(): AgentMessage {
        val proposal = ActionProposal(
            title = "Put Weekly Groceries in Walmart Cart",
            category = ActionCategory.GROCERY_RESTOCK,
            categoryBadge = "GROCERY RESTOCK • WALMART",
            targetEntity = "Walmart Supercenter",
            targetContact = "Walmart In-App Session",
            estimatedTime = "Instant Cart Sync",
            strategySteps = listOf(
                "Launch Walmart app via Ghost Touch",
                "Search & add Great Value Whole Milk (1 Gal) to cart",
                "Search & add Organic Grade-A Eggs (18ct) to cart",
                "Search & add Honeycrisp Apples & Whole Wheat Bread to cart",
                "Open Cart view ready for your review"
            ),
            guardrails = "Organic eggs, store-brand staples. Add to cart only without placing order.",
            actionButtonText = "🛒 Put Items in Walmart Cart",
            actionIcon = ActionIconType.GROCERY,
            requiresBiometric = false,
            totalAmount = null,
            cartItems = listOf("Whole Milk", "Organic Eggs", "Honeycrisp Apples", "Whole Wheat Bread")
        )

        return AgentMessage(
            sender = Sender.AGENT,
            text = "I prepared your grocery list for Walmart: Great Value Whole Milk, Organic Grade-A Eggs, Honeycrisp Apples, and Whole Wheat Bread.\n\nTap below and Ghost Touch will search each item and add them directly into your Walmart cart for review.",
            timestamp = getCurrentTimestamp(),
            actionProposal = proposal
        )
    }

    private fun createCarRentalProposal(): AgentMessage {
        val proposal = ActionProposal(
            title = "2023 Subaru Outback AWD",
            category = ActionCategory.TRAVEL_RENTAL,
            categoryBadge = "TRAVEL • CAR RENTAL",
            targetEntity = "Turo (Denver International Airport)",
            targetContact = "Instant Vehicle Reservation",
            estimatedTime = "Nov 14 - 17 (3 Days)",
            strategySteps = listOf(
                "Aggregated 24 vehicles across Kayak, Hertz, Enterprise, and Turo",
                "Calculated true all-inclusive cost (Base rate + mandatory airport concession fees + CDW insurance)",
                "Selected Turo 2023 Subaru Outback AWD (5.0★ host, unlimited mileage, free curbside terminal dropoff)",
                "Includes 100% free cancellation up to 24 hours prior to trip"
            ),
            guardrails = "AWD mandatory. Unlimited miles. All-inclusive rate under $60/day.",
            actionButtonText = "🚗 Reserve Subaru Outback ($162.40)",
            actionIcon = ActionIconType.CAR,
            requiresBiometric = true,
            totalAmount = "$162.40"
        )

        return AgentMessage(
            sender = Sender.AGENT,
            text = "I compared 24 rental listings for your Denver trip.\n\nWhile Hertz advertised \$32/day, airport concession surcharges and insurance inflated the true cost to \$218. Turo's 2023 Subaru Outback AWD is the best true value at \$162.40 total (\$42/day net) with unlimited mileage and curbside terminal delivery.",
            timestamp = getCurrentTimestamp(),
            actionProposal = proposal
        )
    }

    private fun createLodgingProposal(): AgentMessage {
        val proposal = ActionProposal(
            title = "Mountain View Cabin Retreat",
            category = ActionCategory.LODGING_RESERVATION,
            categoryBadge = "LODGING • AIRBNB SUPERHOST",
            targetEntity = "Airbnb Premier Property",
            targetContact = "Airbnb Instant Booking",
            estimatedTime = "Nov 14 - 17 (3 Nights)",
            strategySteps = listOf(
                "Filtered top-rated cabins (4.9★+ with panoramic mountain view and dedicated workstation)",
                "Computed all-inclusive cost including host cleaning fee and local occupancy tax",
                "Confirmed free cancellation window through November 10",
                "Pre-filled guest credentials and lockbox check-in protocol"
            ),
            guardrails = "Superhost rating > 4.9. Dedicated parking and high-speed Wi-Fi required.",
            actionButtonText = "🏡 Book Mountain Cabin ($485.00)",
            actionIcon = ActionIconType.HOTEL,
            requiresBiometric = true,
            totalAmount = "$485.00"
        )

        return AgentMessage(
            sender = Sender.AGENT,
            text = "Found an exceptional 4.94★ Superhost cabin with panoramic mountain views. The true total for 3 nights is \$485.00 with cleaning and taxes fully accounted for.\n\nCancellation is 100% refundable until November 10.",
            timestamp = getCurrentTimestamp(),
            actionProposal = proposal
        )
    }

    private fun createGymCancellationProposal(): AgentMessage {
        val proposal = ActionProposal(
            title = "Cancel Planet Fitness Membership",
            category = ActionCategory.SUPPORT_CALL,
            categoryBadge = "TELEPHONY PROXY • CANCELLATION",
            targetEntity = "Planet Fitness Billing Desk",
            targetContact = "1-844-880-7180",
            estimatedTime = "3 - 5 mins",
            strategySteps = listOf(
                "Dial billing & cancellation support line",
                "Navigate IVR tree (Speech-to-DTMF: Select 2 - Billing & Cancellations)",
                "Hold queue monitoring with hold music VAD classification",
                "State cancellation intent and decline retention pause offers",
                "Obtain official cancellation receipt code and email confirmation"
            ),
            guardrails = "Insist on cancellation. Reject contracts. Zero cancellation fees.",
            actionButtonText = "📞 Call & Cancel Planet Fitness",
            actionIcon = ActionIconType.PHONE,
            requiresBiometric = false
        )

        return AgentMessage(
            sender = Sender.AGENT,
            text = "Planet Fitness requires speaking to their billing line or an in-person visit to cancel.\n\nI can call them, navigate the automated menu, hold quietly in the queue, reject their retention pitch, and secure your official cancellation code.",
            timestamp = getCurrentTimestamp(),
            actionProposal = proposal
        )
    }

    private fun createComcastProposal(): AgentMessage {
        val proposal = ActionProposal(
            title = "Negotiate Comcast Internet Rate",
            category = ActionCategory.SUPPORT_CALL,
            categoryBadge = "TELEPHONY PROXY • BILL NEGOTIATION",
            targetEntity = "Xfinity Customer Retention",
            targetContact = "1-800-934-6489",
            estimatedTime = "5 - 8 mins",
            strategySteps = listOf(
                "Dial retention department line and bypass virtual assistant",
                "Hold until senior customer loyalty specialist connects",
                "Cite competitor fiber pricing ($45/mo promotional rate)",
                "Negotiate discount of at least $30/mo on existing speed tier",
                "Escalate counter-offer to you for one-tap sign-off"
            ),
            guardrails = "Target $45-$55/mo. Do not reduce download speed. No contract commitments.",
            actionButtonText = "💬 Negotiate Comcast Rate Down",
            actionIcon = ActionIconType.PHONE,
            requiresBiometric = false
        )

        return AgentMessage(
            sender = Sender.AGENT,
            text = "Comcast raised your rate to \$95/mo. Their retention department has discretionary credits available for customers citing local fiber alternatives.\n\nI can call retention, negotiate for their \$49/mo rate, and alert you if an offer is received.",
            timestamp = getCurrentTimestamp(),
            actionProposal = proposal
        )
    }

    private fun createDisputeProposal(query: String): AgentMessage {
        val proposal = ActionProposal(
            title = "Dispute Unauthorized Charge & Demand Refund",
            category = ActionCategory.DISPUTE_REFUND,
            categoryBadge = "BILLING DISPUTE • FORMAL NOTICE",
            targetEntity = "Merchant Billing Resolutions",
            targetContact = "billing-resolution@merchant.com",
            estimatedTime = "Instant Dispatch",
            strategySteps = listOf(
                "Compile charge metadata, transaction ID, and timestamp",
                "Generate formal dispute citation under Fair Credit Billing regulations",
                "Demand chargeback credit within 5 business days",
                "Archive dispatch record for card issuer evidence"
            ),
            guardrails = "Demand 100% full credit refund.",
            actionButtonText = "🛡️ File Dispute & Demand Refund",
            actionIcon = ActionIconType.SHIELD,
            requiresBiometric = false
        )

        return AgentMessage(
            sender = Sender.AGENT,
            text = "I prepared a formal legal dispute notice citing unauthorized billing. Dispatching this immediately creates an evidentiary paper trail for your bank to guarantee full reimbursement.",
            timestamp = getCurrentTimestamp(),
            actionProposal = proposal
        )
    }

    private fun createGenericProposal(query: String): AgentMessage {
        val proposal = ActionProposal(
            title = "Execute Task: $query",
            category = ActionCategory.SUPPORT_CALL,
            categoryBadge = "AUTONOMOUS DELEGATION",
            targetEntity = "Support Desk",
            targetContact = "Direct Hotline",
            estimatedTime = "2 - 4 mins",
            strategySteps = listOf(
                "Connect to service channel",
                "Present task requirements",
                "Confirm resolution and save receipt"
            ),
            guardrails = "Prioritize user preference. Verify terms.",
            actionButtonText = "⚡ Execute Task For Me",
            actionIcon = ActionIconType.PHONE,
            requiresBiometric = false
        )

        return AgentMessage(
            sender = Sender.AGENT,
            text = "I reviewed your request and formulated a step-by-step execution plan so you don't have to spend time doing it manually.",
            timestamp = getCurrentTimestamp(),
            actionProposal = proposal
        )
    }

    fun executeMission(proposal: ActionProposal): Flow<ActionProposal> = flow {
        val initialSteps = proposal.strategySteps.mapIndexed { index, desc ->
            MissionStep(
                description = desc,
                status = if (index == 0) StepStatus.IN_PROGRESS else StepStatus.PENDING
            )
        }

        var execution = MissionExecution(
            companyName = proposal.targetEntity,
            contactTarget = proposal.targetContact,
            currentStepIndex = 0,
            steps = initialSteps,
            liveTranscript = listOf(
                TranscriptEntry("System", "Initiating mission for ${proposal.targetEntity}...", getCurrentTimestamp())
            ),
            isHolding = false
        )

        var currentProposal = proposal.copy(
            status = ActionStatus.EXECUTING,
            execution = execution
        )
        emit(currentProposal)

        // Step 1: Connecting / Launching
        delay(1400)
        execution = execution.copy(
            steps = execution.steps.mapIndexed { i, s ->
                when (i) {
                    0 -> s.copy(status = StepStatus.COMPLETED)
                    1 -> s.copy(status = StepStatus.IN_PROGRESS)
                    else -> s
                }
            },
            currentStepIndex = 1,
            liveTranscript = execution.liveTranscript + TranscriptEntry(
                "System",
                if (proposal.category == ActionCategory.SUPPORT_CALL)
                    "Call established. IVR Automated Menu answering: 'Welcome to ${proposal.targetEntity}'"
                else
                    "Target session active. Assembling parameters and verified credentials...",
                getCurrentTimestamp()
            )
        )
        currentProposal = currentProposal.copy(execution = execution)
        emit(currentProposal)

        // Step 2: Navigating / Assembling
        delay(1800)
        val isCall = proposal.category == ActionCategory.SUPPORT_CALL
        execution = execution.copy(
            steps = execution.steps.mapIndexed { i, s ->
                when (i) {
                    1 -> s.copy(status = StepStatus.COMPLETED)
                    2 -> s.copy(status = StepStatus.IN_PROGRESS)
                    else -> s
                }
            },
            currentStepIndex = 2,
            isHolding = isCall,
            liveTranscript = execution.liveTranscript + listOf(
                if (isCall)
                    TranscriptEntry("Voice Proxy", "Transmitting DTMF tone '2' (Cancellations & Billing)", getCurrentTimestamp())
                else
                    TranscriptEntry("Agent Proxy", "Cart synchronized: All items verified in stock with promotional credit applied.", getCurrentTimestamp()),
                TranscriptEntry("System", if (isCall) "Placed in queue. Hold music active 🎵. Background monitor listening..." else "Calculating final taxes and locking delivery slot...", getCurrentTimestamp())
            )
        )
        currentProposal = currentProposal.copy(execution = execution)
        emit(currentProposal)

        // Step 3: Resolution / Negotiation
        delay(2200)
        if (isCall) {
            execution = execution.copy(
                isHolding = false,
                steps = execution.steps.mapIndexed { i, s ->
                    when (i) {
                        2 -> s.copy(status = StepStatus.COMPLETED)
                        3 -> s.copy(status = StepStatus.IN_PROGRESS)
                        else -> s
                    }
                },
                currentStepIndex = 3,
                liveTranscript = execution.liveTranscript + listOf(
                    TranscriptEntry("System", "Human voice detected. Hold music ended.", getCurrentTimestamp()),
                    TranscriptEntry("Rep (Sarah)", "Thank you for calling ${proposal.targetEntity}. My name is Sarah. How can I help you?", getCurrentTimestamp()),
                    TranscriptEntry("Voice Proxy", "Hello Sarah, I am calling on behalf of the account holder to complete an immediate cancellation today.", getCurrentTimestamp())
                )
            )
            currentProposal = currentProposal.copy(execution = execution)
            emit(currentProposal)

            // Step 4: Retention Offer Prompt
            delay(2200)
            val offer = NegotiationOffer(
                company = proposal.targetEntity,
                originalPrice = "$49.99/mo",
                offeredPrice = "$14.99/mo",
                duration = "Next 6 months",
                details = "50% Loyalty Retention Credit applied with no extension of contract."
            )

            execution = execution.copy(
                steps = execution.steps.mapIndexed { i, s ->
                    when (i) {
                        3 -> s.copy(status = StepStatus.COMPLETED)
                        4 -> s.copy(status = StepStatus.ALERT, detail = "Retention offer received from representative.")
                        else -> s
                    }
                },
                currentStepIndex = 4,
                pendingOffer = offer,
                liveTranscript = execution.liveTranscript + listOf(
                    TranscriptEntry("Rep (Sarah)", "Before processing that, I can apply a special retention discount to drop your rate to $14.99/mo for the next 6 months. Would you consider that?", getCurrentTimestamp()),
                    TranscriptEntry("Voice Proxy", "Please hold for a moment while I confirm this offer with the account holder.", getCurrentTimestamp())
                )
            )

            currentProposal = currentProposal.copy(
                status = ActionStatus.REQUIRES_USER_DECISION,
                execution = execution
            )
            emit(currentProposal)
        } else {
            // Instant Commerce & Travel Completion
            delay(1600)
            val confirmationCode = when (proposal.category) {
                ActionCategory.FOOD_DELIVERY -> "#UBER-CHIP-77319"
                ActionCategory.GROCERY_RESTOCK -> "#WMT-DELIV-44910"
                ActionCategory.TRAVEL_RENTAL -> "#TURO-RES-99281"
                ActionCategory.LODGING_RESERVATION -> "#ABNB-CABIN-33812"
                else -> "#CONF-55102"
            }

            val isCartAction = proposal.category == ActionCategory.FOOD_DELIVERY || proposal.category == ActionCategory.GROCERY_RESTOCK
            val summaryText = if (isCartAction) {
                "Items placed in ${proposal.targetEntity} cart! Ready for your review."
            } else {
                "${proposal.title} confirmed successfully! Reference Code: $confirmationCode"
            }

            val finalTranscript = if (isCartAction) {
                listOf(
                    TranscriptEntry("Ghost Touch", "Items added to cart successfully.", getCurrentTimestamp()),
                    TranscriptEntry("System", "Shopping cart assembled. Tap below to view your cart in ${proposal.targetEntity}.", getCurrentTimestamp())
                )
            } else {
                listOf(
                    TranscriptEntry("Merchant Gateway", "Reservation authorized. Reference: $confirmationCode", getCurrentTimestamp()),
                    TranscriptEntry("System", "Receipt archived to your Proof Vault. Reference: $confirmationCode", getCurrentTimestamp())
                )
            }

            execution = execution.copy(
                steps = execution.steps.map { it.copy(status = StepStatus.COMPLETED) },
                currentStepIndex = execution.steps.size - 1,
                resultSummary = summaryText,
                liveTranscript = execution.liveTranscript + finalTranscript
            )

            currentProposal = currentProposal.copy(
                status = ActionStatus.COMPLETED,
                execution = execution
            )
            emit(currentProposal)
        }
    }

    fun resolveOffer(proposal: ActionProposal, acceptOffer: Boolean): Flow<ActionProposal> = flow {
        val exec = proposal.execution ?: return@flow
        val offer = exec.pendingOffer

        val newTranscript = if (acceptOffer) {
            exec.liveTranscript + listOf(
                TranscriptEntry("Voice Proxy", "Thank you Sarah. The account holder has accepted the promotional rate of ${offer?.offeredPrice} for ${offer?.duration}.", getCurrentTimestamp()),
                TranscriptEntry("Rep (Sarah)", "Great! The promotional rate is now active on your account. Confirmation: #SAV-88392.", getCurrentTimestamp())
            )
        } else {
            exec.liveTranscript + listOf(
                TranscriptEntry("Voice Proxy", "The account holder has declined the retention rate and requires full cancellation of the account today.", getCurrentTimestamp()),
                TranscriptEntry("Rep (Sarah)", "Understood. The account is now closed effective immediately with zero fees. Confirmation: #CAN-44910.", getCurrentTimestamp())
            )
        }

        val completedSteps = exec.steps.map { it.copy(status = StepStatus.COMPLETED) }
        val summary = if (acceptOffer) {
            "Rate negotiated down to ${offer?.offeredPrice} (Saved ~\$210/year). Confirmation: #SAV-88392"
        } else {
            "Account fully cancelled with zero fees. Confirmation: #CAN-44910"
        }

        val updatedExecution = exec.copy(
            pendingOffer = null,
            steps = completedSteps,
            liveTranscript = newTranscript,
            resultSummary = summary
        )

        emit(
            proposal.copy(
                status = ActionStatus.COMPLETED,
                execution = updatedExecution
            )
        )
    }
}
