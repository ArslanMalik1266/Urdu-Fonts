package com.webscare.urdufonts.ui.navigation

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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
        }
    }
}
