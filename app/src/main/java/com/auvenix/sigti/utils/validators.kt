package com.auvenix.sigti.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Validators {

    enum class NameResult(private val msg: String) {
        Ok(""),
        TooShort("El nombre es muy corto"),
        InvalidChars("Contiene caracteres inválidos");
        fun message(): String = msg
    }

    enum class EmailResult(private val msg: String) {
        Ok(""),
        InvalidFormat("Formato de correo inválido");
        fun message(): String = msg
    }

    fun validateName(name: String): NameResult {
        if (name.trim().length < 2) return NameResult.TooShort
        val regex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ' ]+$".toRegex()
        if (!name.matches(regex)) return NameResult.InvalidChars
        return NameResult.Ok
    }

    fun validateEmail(email: String): EmailResult {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (!email.matches(emailRegex)) return EmailResult.InvalidFormat
        return EmailResult.Ok
    }

    fun isAtLeast18(fecha: String): Boolean {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = sdf.parse(fecha) ?: return false
            val dob = Calendar.getInstance().apply { time = birthDate }
            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))) {
                age--
            }
            return age >= 18
        } catch (e: Exception) {
            return false
        }
    }

    // ==========================================
    // LO NUEVO QUE ACABAMOS DE AGREGAR
    // ==========================================
    enum class AddressResult(private val msg: String) {
        Ok(""),
        Empty("La dirección es obligatoria"),
        TooShort("La dirección es muy corta (mínimo 10 caracteres)");
        fun message(): String = msg
    }

    fun validateAddress(address: String): AddressResult {
        val trimmed = address.trim()
        if (trimmed.isEmpty()) return AddressResult.Empty
        if (trimmed.length < 10) return AddressResult.TooShort
        return AddressResult.Ok
    }
}