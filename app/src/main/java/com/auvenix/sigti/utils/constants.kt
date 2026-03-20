package com.auvenix.sigti.utils

object Constants {

    const val BASE_URL = "https://TU_SERVIDOR.com/api/"

    //Notificaciones
    const val COLLECTION_USERS         = "users"
    const val COLLECTION_CHATS         = "chats"
    const val COLLECTION_NOTIFICATIONS = "com/auvenix/sigti/ui/notifications"

    //  Base de datos
    const val NODE_CONVERSATIONS = "conversations"

    // Roles
    const val ROLE_PROVIDER = "PRESTADOR"
    const val ROLE_CLIENT   = "SOLICITANTE"

    // FireStore
    const val FIELD_ROL  = "rol"    // nombre antiguo
    const val FIELD_ROLE = "role"   // nombre actual

    const val EXTRA_IS_GOOGLE    = "extra_is_google"
    const val EXTRA_NOMBRE       = "extra_nombre"
    const val EXTRA_EMAIL_GOOGLE = "extra_email_google"
    const val EXTRA_UID          = "extra_uid"

    //  Notificaciones
    const val NOTIF_TYPE_CHAT     = "chat"
    const val NOTIF_TYPE_REQUEST  = "request"
    const val NOTIF_TYPE_ALERT    = "alert"
    const val NOTIF_TYPE_REMINDER = "reminder"
}