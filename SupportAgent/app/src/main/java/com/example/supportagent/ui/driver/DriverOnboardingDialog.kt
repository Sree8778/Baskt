package com.example.supportagent.ui.driver

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supportagent.model.DriverProfile

@Composable
fun DriverOnboardingDialog(
    isDarkTheme: Boolean,
    onCompleteOnboarding: (DriverProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Personal & ID, 2: Vehicle, 3: Direct Deposit, 4: Subscription

    // Form states
    var fullName by remember { mutableStateOf("Alex Rivera") }
    var phone by remember { mutableStateOf("(561) 555-0192") }
    var licenseNumber by remember { mutableStateOf("FL-R450-781-92") }
    var vehicleModel by remember { mutableStateOf("Toyota RAV4 (Silver)") }
    var licensePlate by remember { mutableStateOf("FL 74A-B98") }
    var insurancePolicy by remember { mutableStateOf("State Farm #SF-881920") }
    var payoutCard by remember { mutableStateOf("•••• 4242 (Chase Visa)") }
    var isSubscribed by remember { mutableStateOf(true) }

    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = cardBg,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D2818))
                                .border(1.dp, Color(0xFF30D158), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color(0xFF30D158),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Driver Registration",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Text(
                                text = "Step $step of 4 • KYC & Verification",
                                fontSize = 11.sp,
                                color = Color(0xFF30D158)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = subText, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4-Step Progress Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..4) {
                        val active = i <= step
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(if (active) Color(0xFF30D158) else if (isDarkTheme) Color(0xFF2C2C2E) else Color(0xFFD1D1D6))
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                when (step) {
                    1 -> {
                        // Step 1: Personal & Driver's License KYC
                        Text("Government ID & Driver's License", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        Text("Required for Checkr identity and background screening.", fontSize = 11.sp, color = subText)

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Legal Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            label = { Text("Driver's License # (State of FL)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkTheme) Color(0xFF1B1D2C) else Color(0xFFF2F2F7))
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Checkr Background Check", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                    Text("Instant automated MVR & identity check passed.", fontSize = 10.sp, color = Color(0xFF30D158))
                                }
                            }
                        }
                    }

                    2 -> {
                        // Step 2: Vehicle & Insurance
                        Text("Vehicle & Active Insurance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        Text("Delivery vehicles must be 2005 or newer with full coverage.", fontSize = 11.sp, color = subText)

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = vehicleModel,
                            onValueChange = { vehicleModel = it },
                            label = { Text("Vehicle Make & Model") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = licensePlate,
                            onValueChange = { licensePlate = it },
                            label = { Text("License Plate Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = insurancePolicy,
                            onValueChange = { insurancePolicy = it },
                            label = { Text("Auto Insurance Policy") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    3 -> {
                        // Step 3: Direct Deposit Payout Setup
                        Text("Instant Payout & Direct Deposit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        Text("Stripe Express connected account for $0 fee instant cash-outs.", fontSize = 11.sp, color = subText)

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color(0xFF0E1A14) else Color(0xFFE8F8EE)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF30D158).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Connected Debit Card", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF30D158))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(payoutCard, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF30D158))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Transfer Speed: Instant (< 60 sec) • Fee: $0.00", fontSize = 10.sp, color = subText)
                            }
                        }
                    }

                    4 -> {
                        // Step 4: Zero-Commission Subscription Activation
                        Text("Provi Driver Subscription Plan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        Text("Our business model: No cuts from your trips. Only a flat monthly membership.", fontSize = 11.sp, color = subText)

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color(0xFF1B1D2C) else Color(0xFFF2F2F7)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFF30D158), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Provi Pro Driver Tier", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                    Text("$19.99/mo", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF30D158))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val benefits = listOf(
                                    "0% platform commission on all orders",
                                    "Keep 100% of the $4.99 delivery fee",
                                    "Keep 100% of customer tips",
                                    "Free instant payouts via Stripe Express"
                                )
                                benefits.forEach { b ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(b, fontSize = 11.sp, color = primaryText)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDarkTheme) Color(0xFF0E1A14) else Color(0xFFE8F8EE))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 On Uber Eats or DoorDash, 40 deliveries cost you ~$140 in platform fees. On Provi, you pay just $19.99 flat.",
                                fontSize = 10.sp,
                                color = primaryText,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 4) {
                        step++
                    } else {
                        val newProfile = DriverProfile(
                            id = "drv_live_reg",
                            fullName = fullName,
                            phone = phone,
                            driversLicenseNumber = licenseNumber,
                            licensePlate = licensePlate,
                            rating = 5.0,
                            vehicleModel = vehicleModel,
                            isOnline = true,
                            verificationStatus = com.example.supportagent.model.VerificationStatus.VERIFIED,
                            subscriptionTier = com.example.supportagent.model.DriverSubscriptionTier.ZERO_COMMISSION_PRO,
                            todayEarnings = 0.0,
                            weeklyEarnings = 0.0,
                            completedTrips = 0
                        )
                        onCompleteOnboarding(newProfile)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (step == 4) Color(0xFF30D158) else Color(0xFF0A84FF)
                )
            ) {
                Text(
                    text = if (step == 4) "Activate Membership & Start" else "Next Step ➔",
                    fontWeight = FontWeight.Bold,
                    color = if (step == 4) Color.Black else Color.White
                )
            }
        },
        dismissButton = {
            if (step > 1) {
                TextButton(onClick = { step-- }) {
                    Text("Back", color = subText)
                }
            }
        }
    )
}
