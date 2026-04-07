package com.auvenix.sigti.notifications

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.session.SessionManager
import com.auvenix.sigti.models.NotificationModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsBottomSheet : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_notifications, container, false)

    // 🔥 FORZAMOS EL 80% DE ALTURA
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val displayMetrics = requireActivity().resources.displayMetrics
                val height = (displayMetrics.heightPixels * 0.80).toInt() // 80% de la pantalla

                bottomSheet.layoutParams.height = height
                behavior.peekHeight = height
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        val rv = view.findViewById<RecyclerView>(R.id.rvNotifications)
        val myRole = session.getRole() ?: "SOLICITANTE"
        val myUid = auth.currentUser?.uid ?: return

        db.collection("notifications")
            .whereEqualTo("recipient_id", myUid)
            .whereEqualTo("recipient_role", myRole)
            //.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(NotificationModel::class.java) ?: emptyList()
                setupRecyclerView(rv, list)
            }
    }

    private fun setupRecyclerView(rv: RecyclerView, list: List<NotificationModel>) {
        rv.layoutManager = LinearLayoutManager(context)

        // 🔥 Si la lista está vacía mostramos el EmptyState, si no, mostramos el Recycler
        val emptyState = view?.findViewById<View>(R.id.llEmptyNotifications)
        if (list.isEmpty()) {
            rv.visibility = View.GONE
            emptyState?.visibility = View.VISIBLE
        } else {
            rv.visibility = View.VISIBLE
            emptyState?.visibility = View.GONE

            // CONECTAMOS EL ADAPTER
            rv.adapter = NotificationAdapter(list)
        }
    }
}