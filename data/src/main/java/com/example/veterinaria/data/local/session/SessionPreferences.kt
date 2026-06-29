package com.example.veterinaria.data.local.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.veterinaria.data.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_preferences")

class SessionPreferences(private val context: Context) {
    private val emailKey = stringPreferencesKey("email")
    private val roleKey = stringPreferencesKey("role")
    private val ownerIdKey = intPreferencesKey("owner_id")

    val session: Flow<Session?> = context.sessionDataStore.data.map { prefs ->
        val email = prefs[emailKey] ?: return@map null
        val role = prefs[roleKey] ?: return@map null
        Session(email = email, rol = role, ownerId = prefs[ownerIdKey])
    }

    suspend fun save(session: Session) {
        context.sessionDataStore.edit { prefs ->
            prefs[emailKey] = session.email
            prefs[roleKey] = session.rol
            if (session.ownerId != null) {
                prefs[ownerIdKey] = session.ownerId
            } else {
                prefs.remove(ownerIdKey)
            }
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}
