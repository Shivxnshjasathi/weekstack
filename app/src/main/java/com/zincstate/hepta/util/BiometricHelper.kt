package com.zincstate.hepta.util

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object AppPreferences {
    private const val PREFS_NAME = "hepta_app_prefs"
    private const val KEY_VAULT_ENABLED = "vault_enabled"
    private const val KEY_SELECTED_THEME = "selected_theme"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isVaultEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_VAULT_ENABLED, false)
    }

    fun setVaultEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_VAULT_ENABLED, enabled).apply()
    }

    fun getTheme(context: Context): com.zincstate.hepta.ui.theme.ZenTheme {
        val themeName = prefs(context).getString(KEY_SELECTED_THEME, com.zincstate.hepta.ui.theme.ZenTheme.OBSIDIAN.name)
        return try {
            com.zincstate.hepta.ui.theme.ZenTheme.valueOf(themeName ?: com.zincstate.hepta.ui.theme.ZenTheme.OBSIDIAN.name)
        } catch (e: Exception) {
            com.zincstate.hepta.ui.theme.ZenTheme.OBSIDIAN
        }
    }

    fun setTheme(context: Context, theme: com.zincstate.hepta.ui.theme.ZenTheme) {
        prefs(context).edit().putString(KEY_SELECTED_THEME, theme.name).apply()
    }
}

object BiometricHelper {

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // If user cancels or uses fallback, still allow entry
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    onError(errString.toString())
                } else {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't close — let user retry
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("HEPTA VAULT")
            .setSubtitle("Authenticate to unlock your workspace")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
