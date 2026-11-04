package com.tripwise.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.UserProfile
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Last known profile (not secret), so the app can start signed-in while offline. */
@Singleton
class ProfileCache @Inject constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun get(): UserProfile? {
        val p = dataStore.data.first()
        val id = p[ID] ?: return null
        return UserProfile(id, p[NAME].orEmpty(), p[EMAIL].orEmpty(), null, CurrencyCode(p[CURRENCY] ?: "INR"))
    }

    suspend fun save(user: UserProfile) {
        dataStore.edit {
            it[ID] = user.id
            it[NAME] = user.displayName
            it[EMAIL] = user.email
            it[CURRENCY] = user.homeCurrency.code
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(ID); it.remove(NAME); it.remove(EMAIL); it.remove(CURRENCY) }
    }

    private companion object {
        val ID = stringPreferencesKey("profile_id")
        val NAME = stringPreferencesKey("profile_name")
        val EMAIL = stringPreferencesKey("profile_email")
        val CURRENCY = stringPreferencesKey("profile_currency")
    }
}
