package com.tokko.brewlog

import com.google.firebase.firestore.FirebaseFirestore


interface IFirestoreRepository {
    fun addBrew(brew: Brew)
    fun getBrews(callback: (List<Brew>) -> Unit)
    fun addAlarm(alarm: Alarm)
    fun getBrew(id: String, callback: (Brew) -> Unit)
    fun getAlarms(callback: (List<Alarm>) -> Unit)
    fun deleteAlarm(id: String)
    fun getAlarm(id: String, callback: (Alarm) -> Unit)
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

    override fun getBrew(id: String, callback: (Brew) -> Unit) {
        try {
            db.collection("brews").document(id).get().addOnCompleteListener {
                callback(it.result?.toObject(Brew::class.java) ?: Brew())
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun getAlarms(callback: (List<Alarm>) -> Unit) {
        db.collection("alarms").addSnapshotListener { querySnapshot, _ ->
            val l = querySnapshot?.map { it.toObject(Alarm::class.java) }?.toMutableList()
                ?: mutableListOf()
            callback(l)
        }
    }

    override fun deleteAlarm(id: String) {
        db.collection("alarms").document(id).delete()
    }

    override fun getAlarm(id: String, callback: (Alarm) -> Unit) {
        db.collection("alarms").document(id).get().addOnCompleteListener {
            callback(it.result?.toObject(Alarm::class.java) ?: Alarm())
        }
    }

}

