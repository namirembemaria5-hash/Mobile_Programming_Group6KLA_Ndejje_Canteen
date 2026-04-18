package com.ndejje.ndejjecanteen.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ndejje.ndejjecanteen.data.model.User
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String
    ): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!

            // Save user profile to Firestore
            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                phone = phone
            )
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            AuthResult.Success(firebaseUser)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign up failed")
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(result.user!!)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    fun signOut() = auth.signOut()

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        try {
            firestore.collection("users")
                .document(uid)
                .update("fcmToken", token)
                .await()
        } catch (_: Exception) {}
    }

    suspend fun updateProfile(uid: String, name: String, phone: String): Boolean {
        return try {
            firestore.collection("users")
                .document(uid)
                .update(mapOf("name" to name, "phone" to phone))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): AuthResult {
        return try {
            val user = auth.currentUser ?: return AuthResult.Error("No user logged in")
            val email = user.email ?: return AuthResult.Error("Email not found")
            
            // Re-authenticate first
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential).await()
            
            // Update password
            user.updatePassword(newPassword).await()
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to change password")
        }
    }
}
