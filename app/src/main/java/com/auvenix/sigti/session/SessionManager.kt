package com.auvenix.sigti.session

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ====== TOKEN ======
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    // ====== "RECUÉRDAME" PENDIENTE (hasta verificar correo) ======
    fun savePendingRemember(email: String, password: String) {
        prefs.edit()
            .putString(KEY_PENDING_EMAIL, email)
            .putString(KEY_PENDING_PASSWORD, password)
            .apply()
    }

    fun clearPendingRemember() {
        prefs.edit()
            .remove(KEY_PENDING_EMAIL)
            .remove(KEY_PENDING_PASSWORD)
            .apply()
    }

    // Pasa de pendiente -> permanente (se llama cuando verifica correo)
    fun promotePendingToRemembered() {
        val email = prefs.getString(KEY_PENDING_EMAIL, null)
        val pass = prefs.getString(KEY_PENDING_PASSWORD, null)

        if (!email.isNullOrBlank() && !pass.isNullOrBlank()) {
            prefs.edit()
                .putString(KEY_REMEMBER_EMAIL, email)
                .putString(KEY_REMEMBER_PASSWORD, pass)
                .apply()
        }
        clearPendingRemember()
    }

    // ====== "RECUÉRDAME" PERMANENTE (ya verificado) ======
    fun getRememberedEmail(): String? = prefs.getString(KEY_REMEMBER_EMAIL, null)
    fun getRememberedPassword(): String? = prefs.getString(KEY_REMEMBER_PASSWORD, null)

    fun clearRememberedCredentials() {
        prefs.edit()
            .remove(KEY_REMEMBER_EMAIL)
            .remove(KEY_REMEMBER_PASSWORD)
            .apply()
    }

    // ====== LIMPIEZA GENERAL ======
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "sigti_session"

        private const val KEY_TOKEN = "token"

        private const val KEY_PENDING_EMAIL = "pending_email"
        private const val KEY_PENDING_PASSWORD = "pending_password"

        private const val KEY_REMEMBER_EMAIL = "remember_email"
        private const val KEY_REMEMBER_PASSWORD = "remember_password"
    }
}