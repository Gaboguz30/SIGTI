package com.auvenix.sigti.utils

object Constants {

    const val BASE_URL = "https://TU_SERVIDOR.com/api/"

    // Firestore / Realtime Database
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CHATS = "chats"
    const val COLLECTION_NOTIFICATIONS = "notifications"
    const val COLLECTION_REQUESTS = "requests"
    const val COLLECTION_REPORTS = "reports"
    const val COLLECTION_REVIEWS = "reviews"
    const val NODE_CONVERSATIONS = "conversations"

    // Roles
    const val ROLE_PROVIDER = "PRESTADOR"
    const val ROLE_CLIENT = "SOLICITANTE"

    // Firestore fields
    const val FIELD_ROL = "rol"
    const val FIELD_ROLE = "role"

    // Extras
    const val EXTRA_IS_GOOGLE = "extra_is_google"
    const val EXTRA_NOMBRE = "extra_nombre"
    const val EXTRA_EMAIL_GOOGLE = "extra_email_google"
    const val EXTRA_UID = "extra_uid"
    const val EXTRA_WORKER_ID = "extra_worker_id"
    const val EXTRA_WORKER_NAME = "extra_worker_name"
    const val EXTRA_WORKER_PROFESSION = "extra_worker_profession"
    const val EXTRA_REQUEST_ID = "extra_request_id"

    // Notifications
    const val NOTIF_TYPE_CHAT = "chat"
    const val NOTIF_TYPE_REQUEST = "request"
    const val NOTIF_TYPE_ALERT = "alert"
    const val NOTIF_TYPE_REMINDER = "reminder"
}