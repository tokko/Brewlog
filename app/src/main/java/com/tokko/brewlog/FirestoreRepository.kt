package com.tokko.brewlog

import com.google.firebase.firestore.FirebaseFirestore


interface IFirestoreRepository {
    fun addBrew(brew: Brew)
    fun getBrews(callback: (List<Brew>) -> Unit)
}

class FirestoreRepository : IFirestoreRepository {
    var db = FirebaseFirestore.getInstance()

    override fun addBrew(brew: Brew) {
        db.collection("brews")
            .add(brew)
    }

    override fun getBrews(callback: (List<Brew>) -> Unit) {
        db.collection("brews").addSnapshotListener { querySnapshot, _ ->
            val l = querySnapshot?.map { it.toObject(Brew::class.java) }?.toMutableList()
                ?: mutableListOf()
            callback(l)
        }
    }
}

