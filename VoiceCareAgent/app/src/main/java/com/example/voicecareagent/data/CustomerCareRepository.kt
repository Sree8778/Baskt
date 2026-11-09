package com.example.voicecareagent.data

import com.example.voicecareagent.model.CustomerCareTarget
import com.example.voicecareagent.model.ServiceCategory

object CustomerCareRepository {

    val defaultTargets: List<CustomerCareTarget> = listOf(
        CustomerCareTarget(
            id = "tgt_walmart",
            name = "Walmart Customer Care",
            phoneNumber = "1-800-925-6278",
            category = ServiceCategory.RETAIL_GROCERY,
            brandColorHex = 0xFF0071DC,
            defaultHoldTime = "~2 mins",
            verifiedSupportLine = true,
            promptGuidelines = "Walmart grocery order refunds, missing items, damaged delivery."
        ),
        CustomerCareTarget(
            id = "tgt_instacart",
            name = "Instacart Care",
            phoneNumber = "1-888-246-7822",
            category = ServiceCategory.FOOD_DELIVERY,
            brandColorHex = 0xFF43B02A,
            defaultHoldTime = "~3 mins",
            verifiedSupportLine = true,
            promptGuidelines = "Instacart grocery missing items, incorrect brand replacements, shopper fees."
        ),
        CustomerCareTarget(
            id = "tgt_ubereats",
            name = "Uber Eats Priority Support",
            phoneNumber = "1-800-253-9377",
            category = ServiceCategory.FOOD_DELIVERY,
            brandColorHex = 0xFF06C167,
            defaultHoldTime = "~3 mins",
            verifiedSupportLine = true,
            promptGuidelines = "Late food delivery, missing dishes, cold food, restaurant error."
        ),
        CustomerCareTarget(
            id = "tgt_target",
            name = "Target Guest Relations",
            phoneNumber = "1-800-440-0680",
            category = ServiceCategory.RETAIL_GROCERY,
            brandColorHex = 0xFFCC0000,
            defaultHoldTime = "~2 mins",
            verifiedSupportLine = true,
            promptGuidelines = "Target grocery pickup, drive-up order issues, damaged items."
        ),
        CustomerCareTarget(
            id = "tgt_costco",
            name = "Costco Member Service",
            phoneNumber = "1-800-774-2678",
            category = ServiceCategory.RETAIL_GROCERY,
            brandColorHex = 0xFF0060A9,
            defaultHoldTime = "~4 mins",
            verifiedSupportLine = true,
            promptGuidelines = "Costco wholesale delivery, membership refund guarantee."
        ),
        CustomerCareTarget(
            id = "tgt_xfinity",
            name = "Xfinity / Comcast Support",
            phoneNumber = "1-800-934-6489",
            category = ServiceCategory.UTILITIES_CABLE,
            brandColorHex = 0xFF76226C,
            defaultHoldTime = "~6 mins",
            verifiedSupportLine = true,
            promptGuidelines = "Internet outage credits, monthly bill discount renewal, equipment fees."
        ),
        CustomerCareTarget(
            id = "tgt_amazon",
            name = "Amazon Customer Service",
            phoneNumber = "1-888-280-4331",
            category = ServiceCategory.RETAIL_GROCERY,
            brandColorHex = 0xFFFF9900,
            defaultHoldTime = "~2 mins",
            verifiedSupportLine = true,
            promptGuidelines = "Amazon Fresh / Whole Foods delivery refund, missing packages."
        ),
        CustomerCareTarget(
            id = "tgt_planet_fitness",
            name = "Planet Fitness Member Care",
            phoneNumber = "1-844-880-7180",
            category = ServiceCategory.MEMBERSHIP_GYM,
            brandColorHex = 0xFF5C2D91,
            defaultHoldTime = "~4 mins",
            verifiedSupportLine = true,
            promptGuidelines = "Gym membership cancellation without in-person visit or certified letter."
        )
    )
}
