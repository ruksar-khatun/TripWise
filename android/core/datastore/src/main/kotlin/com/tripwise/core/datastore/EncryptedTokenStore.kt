package com.tripwise.core.datastore

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.tripwise.core.network.auth.AuthTokens
import com.tripwise.core.network.auth.TokenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.GeneralSecurityException

/**
 * Stores session tokens encrypted with AES-256-GCM (Tink). The Tink keyset is itself encrypted by a
 * non-exportable Android Keystore key, so a copied data directory cannot be decrypted elsewhere.
 * Both files are excluded from backups (see app/src/main/res/xml/backup_rules.xml).
 *
 * If the Keystore is unavailable or data can't be decrypted (e.g. restored onto a new device), the
 * store behaves as empty and the user simply signs in again.
 */
internal class EncryptedTokenStore(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
) : TokenStore {

    @Serializable private data class Payload(val access: String, val refresh: String)

    private val mutex = Mutex()
    @Volatile private var cache: AuthTokens? = null
    @Volatile private var loaded = false

    private val aead: Aead? by lazy {
        try {
            AeadConfig.register()
            AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .keysetHandle
                .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        } catch (e: GeneralSecurityException) {
            Log.w(TAG, "Keystore unavailable; tokens will not be persisted", e)
            null
        } catch (e: java.io.IOException) {
            Log.w(TAG, "Keyset unreadable; tokens will not be persisted", e)
            null
        }
    }

    override suspend fun get(): AuthTokens? = mutex.withLock {
        if (!loaded) {
            cache = read()
            loaded = true
        }
        cache
    }

    override suspend fun save(tokens: AuthTokens): Unit = mutex.withLock {
        cache = tokens
        loaded = true
        val cipher = aead ?: return@withLock
        val plain = Json.encodeToString(Payload.serializer(), Payload(tokens.accessToken, tokens.refreshToken)).toByteArray()
        val encrypted = Base64.encodeToString(cipher.encrypt(plain, ASSOCIATED_DATA), Base64.NO_WRAP)
        dataStore.edit { it[KEY] = encrypted }
        Unit
    }

    override suspend fun clear(): Unit = mutex.withLock {
        cache = null
        loaded = true
        dataStore.edit { it.remove(KEY) }
        Unit
    }

    private suspend fun read(): AuthTokens? {
        val encrypted = dataStore.data.first()[KEY] ?: return null
        val cipher = aead ?: return null
        return try {
            val plain = cipher.decrypt(Base64.decode(encrypted, Base64.NO_WRAP), ASSOCIATED_DATA)
            Json.decodeFromString(Payload.serializer(), String(plain)).let { AuthTokens(it.access, it.refresh) }
        } catch (e: GeneralSecurityException) {
            Log.w(TAG, "Stored tokens could not be decrypted; discarding", e)
            dataStore.edit { it.remove(KEY) }
            null
        } catch (e: IllegalArgumentException) {
            dataStore.edit { it.remove(KEY) }
            null
        }
    }

    private companion object {
        const val TAG = "TokenStore"
        const val KEYSET_NAME = "tripwise_token_keyset"
        const val KEYSET_PREFS = "tripwise_token_keyset_prefs"
        const val MASTER_KEY_URI = "android-keystore://tripwise_token_master_key"
        val ASSOCIATED_DATA = "tripwise-session-tokens".toByteArray()
        val KEY = stringPreferencesKey("session")
    }
}
