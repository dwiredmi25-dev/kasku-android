package com.example.kasku.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/** Semua data disimpan di users/{uid}/transactions sehingga terpisah per pengguna. */
class TransactionRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun col(): CollectionReference =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "unknown")
            .collection("transactions")

    fun listenAll(
        onData: (List<Transaction>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        col().orderBy("date", Query.Direction.DESCENDING).addSnapshotListener { snap, e ->
            if (e != null) onError(e)
            else if (snap != null) onData(snap.toObjects(Transaction::class.java))
        }

    fun listenOne(
        id: String,
        onData: (Transaction?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        col().document(id).addSnapshotListener { snap, e ->
            if (e != null) onError(e) else onData(snap?.toObject(Transaction::class.java))
        }

    fun getOnce(id: String): Task<DocumentSnapshot> = col().document(id).get()

    fun add(t: Transaction): Task<Void> = col().document().set(t)

    fun update(t: Transaction): Task<Void> = col().document(t.id).set(t)

    fun delete(id: String): Task<Void> = col().document(id).delete()
}
