package com.example.supportagent.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object AppLauncher {

    const val WALMART_PKG = "com.walmart.android"
    const val UBER_EATS_PKG = "com.ubercab.eats"
    const val AMAZON_PKG = "com.amazon.mShop.android.shopping"
    const val COSTCO_PKG = "com.costco.app.android"
    const val PUBLIX_PKG = "com.publix.main"

    fun launchApp(context: Context, packageName: String): Boolean {
        val pm = context.packageManager

        // 1. Standard launch intent
        var intent = pm.getLaunchIntentForPackage(packageName)

        // 2. Explicit known launcher class fallback
        if (intent == null) {
            val knownClass = when (packageName) {
                WALMART_PKG -> "com.walmart.glass.integration.splash.SplashActivity"
                UBER_EATS_PKG -> "com.ubercab.eats.core.activity.LauncherActivity"
                AMAZON_PKG -> "com.amazon.mShop.home.HomeActivity"
                else -> null
            }
            if (knownClass != null) {
                intent = Intent().apply {
                    setClassName(packageName, knownClass)
                }
            }
        }

        // 3. Fallback category launcher intent
        if (intent == null) {
            intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
            }
        }

        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            context.startActivity(intent)
            Log.d("AppLauncher", "Successfully launched native package: $packageName")
            true
        } catch (e: Exception) {
            Log.e("AppLauncher", "Could not launch native app $packageName: ${e.message}")
            false
        }
    }

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
