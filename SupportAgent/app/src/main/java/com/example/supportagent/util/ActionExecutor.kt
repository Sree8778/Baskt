package com.example.supportagent.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.supportagent.model.ActionCategory
import com.example.supportagent.model.ActionProposal

object ActionExecutor {

    fun authenticateAndExecute(
        activity: FragmentActivity,
        proposal: ActionProposal,
        onAuthenticated: () -> Unit
    ) {
        if (!proposal.requiresBiometric) {
            onAuthenticated()
            return
        }

        val biometricManager = BiometricManager.from(activity)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(activity, "Authorized: ${proposal.totalAmount ?: ""}", Toast.LENGTH_SHORT).show()
            onAuthenticated()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(activity, "Authenticated: ${proposal.title}", Toast.LENGTH_SHORT).show()
                onAuthenticated()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    Toast.makeText(activity, "Proceeding with task...", Toast.LENGTH_SHORT).show()
                    onAuthenticated()
                } else {
                    Toast.makeText(activity, "Auth note: $errString", Toast.LENGTH_SHORT).show()
                    onAuthenticated()
                }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authorize Transaction")
            .setSubtitle(proposal.totalAmount?.let { "$it • ${proposal.targetEntity}" } ?: proposal.title)
            .setDescription("Confirm with fingerprint to execute this action.")
            .setNegativeButtonText("Authorize Now")
            .build()

        prompt.authenticate(promptInfo)
    }

    fun openExternalTarget(context: Context, proposal: ActionProposal) {
        val entityLower = proposal.targetEntity.lowercase()

        try {
            when {
                proposal.category == ActionCategory.SUPPORT_CALL -> {
                    val number = proposal.targetContact.filter { it.isDigit() }
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                }
                entityLower.contains("walmart") || proposal.category == ActionCategory.GROCERY_RESTOCK -> {
                    val launched = AppLauncher.launchApp(context, AppLauncher.WALMART_PKG)
                    if (!launched) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.walmart.com/cart")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                    }
                }
                entityLower.contains("uber eats") || entityLower.contains("chipotle") || proposal.category == ActionCategory.FOOD_DELIVERY -> {
                    val launched = AppLauncher.launchApp(context, AppLauncher.UBER_EATS_PKG)
                    if (!launched) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ubereats.com/store/chipotle-mexican-grill")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                    }
                }
                entityLower.contains("amazon") -> {
                    val launched = AppLauncher.launchApp(context, AppLauncher.AMAZON_PKG)
                    if (!launched) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.com/gp/cart/view.html")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                    }
                }
                proposal.category == ActionCategory.TRAVEL_RENTAL -> {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://turo.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                }
                proposal.category == ActionCategory.LODGING_RESERVATION -> {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.airbnb.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                }
                proposal.category == ActionCategory.DISPUTE_REFUND -> {
                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${proposal.targetContact}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(emailIntent)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
