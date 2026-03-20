package notifications

import android.util.Log
import com.auvenix.sigti.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Gestiona el token FCM del dispositivo.
 * Lo obtiene de Firebase Messaging y lo guarda en Firestore
 * bajo users/{uid}/fcmToken para poder enviar push notifications.
 */
object FcmTokenManager {

    private const val TAG = "FcmTokenManager"
    private const val FIELD_FCM_TOKEN = "fcmToken"

    fun saveCurrentToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.w(TAG, "saveCurrentToken: no hay usuario autenticado")
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .update(FIELD_FCM_TOKEN, token)
                    .addOnSuccessListener {
                        Log.d(TAG, "Token FCM guardado correctamente")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al guardar token FCM: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener token FCM: ${e.message}")
            }
    }
}