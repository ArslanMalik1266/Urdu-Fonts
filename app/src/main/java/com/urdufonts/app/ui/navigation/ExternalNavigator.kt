package com.urdufonts.app.ui.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class ExternalNavigator(private val context: Context) {

    fun openEmailSupport(email: String = "hello@webscare.com") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Support - Urdu Fonts App")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No email client installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWebPage(url: String) {
        if (url.isBlank()) {
            Toast.makeText(context, "Link not configured yet", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPlayStoreForRating() {
        val packageName = context.packageName
        val marketUri = Uri.parse("market://details?id=$packageName")
        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        }
        try {
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webUri = "https://play.google.com/store/apps/details?id=$packageName"
            openWebPage(webUri)
        }
    }

    fun openOrInstallApp(packageName: String = "com.webscare.urducanvas") {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                openPlayStoreForPackage(packageName)
            }
        } catch (e: Exception) {
            openPlayStoreForPackage(packageName)
        }
    }

    fun openPlayStoreForPackage(packageName: String) {
        val marketUri = Uri.parse("market://details?id=$packageName")
        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }
        try {
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webUri = "https://play.google.com/store/apps/details?id=$packageName"
            openWebPage(webUri)
        }
    }
}

