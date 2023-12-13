package de.mm20.launcher2.plugin.onedrive

import android.app.Activity
import android.os.Bundle
import org.koin.android.ext.android.inject

class SignInActivity: Activity() {
    private val client: MicrosoftApiClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client.login(this@SignInActivity)
        finish()
    }
}