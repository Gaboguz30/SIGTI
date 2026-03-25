package com.auvenix.sigti.utils

import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.LinkedHashSet
import java.util.Locale

object Validators {

    private val acceptedEmailDomains = setOf(
        "gmail.com",
        "hotmail.com",
        "outlook.com",
        "yahoo.com",
        "icloud.com",
        "live.com",
        "msn.com",
        "proton.me",
        "protonmail.com",
        "aol.com",
        "me.com",
        "gmx.com",
        "yandex.com",
        "mail.com",
        "edu.mx",
        "alumno.uttehuacan.edu.mx"
    )

    val localidadesTehuacan = listOf(
        "Santa Cruz Acapa",
        "Santa Catarina Otzolotepec",
        "San Marcos Necoxtla",
        "Magdalena Cuayucatepec",
        "San Pedro Acoquiaco",
        "San Nicolás Tetitzintla",
        "Santa María Coapan",
        "San Lorenzo Teotipilco",
        "San Diego Chalma",
        "San Cristóbal Tepeteopan",
        "San Pablo Tepetzingo",
        "Santa Ana Teloxtoc"
    )

    val postalCodeDropdownOptions = listOf(
        "— Zona urbana Tehuacán —",
        "75700",
        "75710",
        "75717",
        "75718",
        "75719",
        "75720",
        "75725",
        "75726",
        "75730",
        "75731",
        "75732",
        "75740",
        "75741",
        "75742",
        "75743",
        "75750",
        "75758",
        "75760",
        "75763",
        "75764",
        "75765",
        "75766",
        "75768",
        "75769",
        "75770",
        "75780",
        "75784",
        "75786",
        "75790",
        "75793",
        "75794",
        "75795",
        "75796",
        "75797",
        "75799",
        "— Localidades —",
        "Magdalena Cuayucatepec — 75853",
        "San Cristóbal Tepeteopan — 75853",
        "San Lorenzo Teotipilco — 75855",
        "Santa Catarina Otzolotepec — 75855",
        "Santa Ana Teloxtoc — 75856",
        "Santa María Coapan — 75857",
        "San Marcos Necoxtla — 75859",
        "San Diego Chalma — 75859",
        "San Pablo Tepetzingo — 75859",
        "Santa Cruz Acapa — 75859"
    )

    private val validPostalCodes = LinkedHashSet(
        listOf(
            "75700", "75710", "75717", "75718", "75719", "75720", "75725", "75726", "75730",
            "75731", "75732", "75740", "75741", "75742", "75743", "75750", "75758", "75760",
            "75763", "75764", "75765", "75766", "75768", "75769", "75770", "75780", "75784",
            "75786", "75790", "75793", "75794", "75795", "75796", "75797", "75799", "75853",
            "75855", "75856", "75857", "75859"
        )
    )

    enum class NameResult(private val msg: String) {
        Ok(""),
        TooShort("Debe tener al menos 2 letras"),
        TooLong("Excede el máximo permitido"),
        InvalidChars("Solo se permiten letras y espacios");
        fun message(): String = msg
    }

    enum class EmailResult(private val msg: String) {
        Ok(""),
        MissingAt("El correo debe contener @"),
        InvalidFormat("Formato de correo inválido"),
        InvalidLocalPart("El texto antes de @ no es válido"),
        UnknownDomain("El dominio del correo no está permitido");
        fun message(): String = msg
    }

    enum class AddressResult(private val msg: String) {
        Ok(""),
        Empty("La dirección es obligatoria"),
        TooShort("La dirección es muy corta (mínimo 10 caracteres)");
        fun message(): String = msg
    }

    enum class LocalityResult(private val msg: String) {
        Ok(""),
        Empty("La localidad es obligatoria"),
        Invalid("Selecciona una localidad válida");
        fun message(): String = msg
    }

    enum class PostalCodeResult(private val msg: String) {
        Ok(""),
        Empty("El código postal es obligatorio"),
        InvalidCharacters("Solo se permiten números"),
        InvalidLength("El código postal debe tener exactamente 5 dígitos"),
        UnknownCode("El código postal no pertenece a las opciones válidas");
        fun message(): String = msg
    }

    fun validateName(name: String, maxLength: Int): NameResult {
        val trimmed = name.trim()
        if (trimmed.length < 2) return NameResult.TooShort
        if (trimmed.length > maxLength) return NameResult.TooLong
        val regex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$".toRegex()
        if (!trimmed.matches(regex)) return NameResult.InvalidChars
        return NameResult.Ok
    }

    fun validateEmail(email: String): EmailResult {
        val trimmed = email.trim().lowercase(Locale.ROOT)

        if (!trimmed.contains("@")) return EmailResult.MissingAt
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) return EmailResult.InvalidFormat

        val parts = trimmed.split("@")
        if (parts.size != 2) return EmailResult.InvalidFormat

        val localPart = parts[0]
        val domain = parts[1]

        val localRegex = "^[a-zA-Z0-9](?:[a-zA-Z0-9._%+\\-]{0,62}[a-zA-Z0-9])?$".toRegex()
        if (localPart.isBlank() || !localPart.matches(localRegex)) {
            return EmailResult.InvalidLocalPart
        }

        if (domain !in acceptedEmailDomains) {
            return EmailResult.UnknownDomain
        }

        return EmailResult.Ok
    }

    fun isAtLeast18(fecha: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                isLenient = false
            }
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
        } catch (e: Exception) {
            false
        }
    }

    fun validateAddress(address: String): AddressResult {
        val trimmed = address.trim()
        if (trimmed.isEmpty()) return AddressResult.Empty
        if (trimmed.length < 10) return AddressResult.TooShort
        return AddressResult.Ok
    }

    fun validateLocality(locality: String): LocalityResult {
        val trimmed = locality.trim()
        if (trimmed.isEmpty()) return LocalityResult.Empty
        if (trimmed !in localidadesTehuacan) return LocalityResult.Invalid
        return LocalityResult.Ok
    }

    fun validatePostalCode(postalCode: String): PostalCodeResult {
        val trimmed = postalCode.trim()

        if (trimmed.isEmpty()) return PostalCodeResult.Empty
        if (!trimmed.all { it.isDigit() }) return PostalCodeResult.InvalidCharacters
        if (trimmed.length != 5) return PostalCodeResult.InvalidLength
        if (trimmed !in validPostalCodes) return PostalCodeResult.UnknownCode

        return PostalCodeResult.Ok
    }

    fun extractPostalCodeFromOption(option: String): String {
        val regex = "\\b\\d{5}\\b".toRegex()
        return regex.find(option)?.value.orEmpty()
    }
}