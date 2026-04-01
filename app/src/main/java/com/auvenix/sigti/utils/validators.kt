package com.auvenix.sigti.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Validators {

    private val nameRegex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")
    private val addressRegex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9#.,\\- ]+$")

    private val allowedEmailDomains = setOf(
        "gmail.com",
        "hotmail.com",
        "outlook.com",
        "icloud.com",
        "yahoo.com",
        "live.com",
        "msn.com"
    )

    enum class NameResult(private val msg: String) {
        Ok(""),
        Empty("Este campo es obligatorio"),
        TooShort("Debe tener al menos 2 letras"),
        TooLong("Excede la longitud permitida"),
        InvalidChars("Solo se permiten letras y espacios"),
        NeedsVowelAndConsonant("Debe parecer un nombre real"),
        RepeatedChars("No se permiten secuencias repetidas sin sentido"),
        RandomText("Ingresa un nombre válido, no texto aleatorio");

        fun message(): String = msg
    }

    enum class EmailResult(private val msg: String) {
        Ok(""),
        Empty("El correo es obligatorio"),
        InvalidFormat("Ingresa un correo con formato válido"),
        InvalidPrefix("La parte antes de @ no es válida"),
        RandomPrefix("La parte antes de @ parece texto aleatorio"),
        InvalidDomain("Solo se aceptan correos de Gmail, Hotmail, Outlook, iCloud, Yahoo, Live o MSN");

        fun message(): String = msg
    }

    enum class AddressResult(private val msg: String) {
        Ok(""),
        Empty("La dirección es obligatoria"),
        TooShort("La dirección es demasiado corta"),
        TooLong("La dirección es demasiado larga"),
        InvalidChars("La dirección contiene caracteres no válidos"),
        RandomText("Ingresa una dirección válida");

        fun message(): String = msg
    }

    fun validateName(name: String, maxLength: Int = 20): NameResult {
        val trimmed = normalizeSpaces(name)
        if (trimmed.isEmpty()) return NameResult.Empty
        if (trimmed.length < 2) return NameResult.TooShort
        if (trimmed.length > maxLength) return NameResult.TooLong
        if (!trimmed.matches(nameRegex)) return NameResult.InvalidChars

        val compact = trimmed.replace(" ", "")
        if (compact.length < 2) return NameResult.TooShort
        if (!containsVowel(compact) || !containsConsonant(compact)) {
            return NameResult.NeedsVowelAndConsonant
        }
        if (hasAbsurdRepetition(compact)) return NameResult.RepeatedChars
        if (looksRandomWord(compact)) return NameResult.RandomText

        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.any { it.length < 2 }) return NameResult.TooShort
        if (words.any { !containsVowel(it) || !containsConsonant(it) }) {
            return NameResult.NeedsVowelAndConsonant
        }
        if (words.any { looksRandomWord(it) }) return NameResult.RandomText

        return NameResult.Ok
    }

    fun validateSurname(surname: String, maxLength: Int = 15): NameResult {
        return validateName(surname, maxLength)
    }

    fun validateEmail(email: String): EmailResult {
        val trimmed = email.trim().lowercase(Locale.getDefault())
        if (trimmed.isEmpty()) return EmailResult.Empty

        val emailRegex = Regex("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")
        if (!trimmed.matches(emailRegex)) return EmailResult.InvalidFormat

        val parts = trimmed.split("@")
        if (parts.size != 2) return EmailResult.InvalidFormat

        val prefix = parts[0]
        val domain = parts[1]

        if (prefix.length < 3) return EmailResult.InvalidPrefix
        if (!prefix.matches(Regex("^[a-z0-9._-]+$"))) return EmailResult.InvalidPrefix
        if (prefix.all { it.isDigit() }) return EmailResult.RandomPrefix
        if (hasAbsurdRepetition(prefix.replace(Regex("[._-]"), ""))) return EmailResult.RandomPrefix
        if (looksRandomEmailPrefix(prefix)) return EmailResult.RandomPrefix
        if (domain !in allowedEmailDomains) return EmailResult.InvalidDomain

        return EmailResult.Ok
    }

    fun validateAddress(address: String, maxLength: Int = 120): AddressResult {
        val trimmed = normalizeSpaces(address)
        if (trimmed.isEmpty()) return AddressResult.Empty
        if (trimmed.length < 5) return AddressResult.TooShort
        if (trimmed.length > maxLength) return AddressResult.TooLong
        if (!trimmed.matches(addressRegex)) return AddressResult.InvalidChars

        val compact = trimmed.replace(" ", "").lowercase(Locale.getDefault())

        if (compact in nonsenseTokens) return AddressResult.RandomText
        if (Regex("^(asdf|qwer|zxcv|abc|abcd|test|demo)+$").matches(compact)) {
            return AddressResult.RandomText
        }
        if (hasAbsurdRepetition(compact)) return AddressResult.RandomText

        val hasLetter = compact.any { it.isLetter() }
        val hasUsefulContent = compact.any { it.isDigit() } || compact.contains("#") || compact.contains(".") || compact.contains("-")
        if (!hasLetter) return AddressResult.RandomText
        if (trimmed.length < 8 && !hasUsefulContent) return AddressResult.RandomText

        return AddressResult.Ok
    }

    fun isAtLeast18(fecha: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            val birthDate = sdf.parse(fecha) ?: return false
            val dob = Calendar.getInstance().apply { time = birthDate }
            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (
                today.get(Calendar.MONTH) < dob.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))
            ) {
                age--
            }
            age >= 18
        } catch (_: Exception) {
            false
        }
    }

    private fun normalizeSpaces(value: String): String {
        return value.trim().replace(Regex("\\s+"), " ")
    }

    private fun containsVowel(value: String): Boolean {
        return value.any { it.lowercaseChar() in setOf('a', 'e', 'i', 'o', 'u', 'á', 'é', 'í', 'ó', 'ú') }
    }

    private fun containsConsonant(value: String): Boolean {
        return value.any { char ->
            val c = char.lowercaseChar()
            c.isLetter() && c !in setOf('a', 'e', 'i', 'o', 'u', 'á', 'é', 'í', 'ó', 'ú')
        }
    }

    private fun hasAbsurdRepetition(value: String): Boolean {
        val compact = value.lowercase(Locale.getDefault())
        if (compact.isBlank()) return false
        if (Regex("(.)\\1{3,}").containsMatchIn(compact)) return true
        return compact.toSet().size == 1
    }

    private fun looksRandomEmailPrefix(prefix: String): Boolean {
        val compact = prefix.lowercase(Locale.getDefault()).replace(Regex("[._-]"), "")
        if (compact.length < 3) return true
        if (compact in nonsenseTokens) return true
        if (Regex("^(abc|abcd|asdf|qwer|zxcv|test|demo|user)+$").matches(compact)) return true
        if (!containsVowel(compact) && compact.length >= 5) return true
        return false
    }

    private fun looksRandomWord(word: String): Boolean {
        val compact = word.lowercase(Locale.getDefault()).replace(" ", "")
        if (compact in nonsenseTokens) return true
        if (Regex("^(asdf|qwer|zxcv|abc|abcd|wert|poi|lkj|mnb)+$").matches(compact)) return true
        if (Regex("^[bcdfghjklmnñpqrstvwxyz]{5,}$").matches(compact)) return true
        if (Regex("^[aeiouáéíóú]{4,}$").matches(compact)) return true
        if (compact.length >= 5 && compact.toSet().size <= 2) return true
        return false
    }

    private val nonsenseTokens = setOf(
        "asdf", "asdfg", "asdfgh", "qwer", "qwerty", "zxcv", "zxcvb",
        "abc", "abcd", "abcde", "zzzzz", "xxxxx", "aaaaa", "bbbbb",
        "test", "demo", "random", "nombre", "usuario"
    )
}