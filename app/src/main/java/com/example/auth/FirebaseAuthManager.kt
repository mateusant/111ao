package com.example.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Gestor de Autenticação Firebase do Sistema Ocorrência Remota
 * Permite a autenticação na nuvem de cada agente para sua conta particular,
 * com inicialização segura e suporte a ambiente conectado.
 */
object FirebaseAuthManager {
    private const val TAG = "FirebaseAuthManager"

    fun getAuth(context: Context): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setApiKey("AIzaSyDefaultPlaceholderKeyForInitialization")
                    .setProjectId("ocorrencia-remota")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Inicialização do FirebaseApp: ${e.message}")
            try {
                FirebaseAuth.getInstance()
            } catch (e2: Exception) {
                null
            }
        }
    }

    fun getCurrentFirebaseUser(context: Context): FirebaseUser? {
        return try {
            getAuth(context)?.currentUser
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithEmail(context: Context, email: String, pass: String): Result<FirebaseUser> {
        val auth = getAuth(context) ?: return Result.failure(Exception("Serviço Firebase Auth indisponível."))
        return suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email.trim(), pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        cont.resume(Result.success(user))
                    } else {
                        cont.resume(Result.failure(Exception("Utilizador não retornado pelo Firebase.")))
                    }
                }
                .addOnFailureListener { exception ->
                    val msg = when {
                        exception.message?.contains("password", ignoreCase = true) == true -> "Palavra-passe incorreta ou formato inválido."
                        exception.message?.contains("user", ignoreCase = true) == true -> "Nenhuma conta encontrada com este email."
                        exception.message?.contains("network", ignoreCase = true) == true -> "Falha de rede ao conectar ao servidor Firebase."
                        else -> exception.localizedMessage ?: "Erro na autenticação Firebase."
                    }
                    cont.resume(Result.failure(Exception(msg, exception)))
                }
        }
    }

    suspend fun signUpWithEmail(context: Context, email: String, pass: String): Result<FirebaseUser> {
        val auth = getAuth(context) ?: return Result.failure(Exception("Serviço Firebase Auth indisponível."))
        return suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email.trim(), pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        cont.resume(Result.success(user))
                    } else {
                        cont.resume(Result.failure(Exception("Registo concluído sem retorno de utilizador.")))
                    }
                }
                .addOnFailureListener { exception ->
                    val msg = when {
                        exception.message?.contains("already in use", ignoreCase = true) == true -> "Este email já está registado no sistema."
                        exception.message?.contains("weak-password", ignoreCase = true) == true -> "A palavra-passe deve ter pelo menos 6 caracteres."
                        else -> exception.localizedMessage ?: "Erro ao criar conta no Firebase."
                    }
                    cont.resume(Result.failure(Exception(msg, exception)))
                }
        }
    }

    suspend fun sendPasswordReset(context: Context, email: String): Result<Unit> {
        val auth = getAuth(context) ?: return Result.failure(Exception("Serviço Firebase Auth indisponível."))
        return suspendCancellableCoroutine { cont ->
            auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener {
                    cont.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }
    }

    fun signOut(context: Context) {
        try {
            getAuth(context)?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao terminar sessão Firebase: ${e.message}")
        }
    }
}
