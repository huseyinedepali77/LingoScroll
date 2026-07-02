package com.example.lingoscroll.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {
    private val auth = FirebaseAuth.getInstance()

    fun signInAnonymously(onComplete: (FirebaseUser?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("AuthManager", "Kullanıcı zaten giriş yapmış: ${currentUser.uid}")
            onComplete(currentUser)
            return
        }

        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("AuthManager", "Anonim Giriş Başarılı. UID: ${user?.uid}")
                    onComplete(user)
                } else {
                    Log.w("AuthManager", "Anonim Giriş Başarısız!", task.exception)
                    onComplete(null)
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
