package com.auvenix.sigti.session

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun saveRememberedEmail(email: String) {
        prefs.edit().putString(KEY_REMEMBER_EMAIL, email).apply()
    }

    fun getRememberedEmail(): String? = prefs.getString(KEY_REMEMBER_EMAIL, null)

    fun clearRememberedEmail() {
        prefs.edit().remove(KEY_REMEMBER_EMAIL).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME       = "sigti_session"
        private const val KEY_TOKEN        = "token"
        private const val KEY_REMEMBER_EMAIL = "remember_email"
    }
}