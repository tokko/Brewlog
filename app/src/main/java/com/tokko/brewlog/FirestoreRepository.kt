package com.tokko.brewlog

import com.google.firebase.firestore.FirebaseFirestore


interface IFirestoreRepository {
    fun addBrew(brew: Brew)
    fun getBrews(callback: (List<Brew>) -> Unit)
    fun addAlarm(alarm: Alarm)
}

class FirestoreRepository : IFirestoreRepository {
    var db = FirebaseFirestore.getInstance().collection("root").document("brewer")

    override fun addBrew(brew: Brew) {
        db.collection("brews")
            .document(brew.id)
            .set(brew)
    }

    override fun getBrews(callback: (List<Brew>) -> Unit) {
        db.collection("brews").addSnapshotListener { querySnapshot, _ ->
            val l = querySnapshot?.map { it.toObject(Brew::class.java) }?.toMutableList()
                ?: mutableListOf()
            callback(l)
        }
    }

    override fun addAlarm(alarm: Alarm) {
        db.collection("alarms").document(alarm.id)
            .set(alarm)
    }

}

