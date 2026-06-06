package epn.tallermoviles

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.first

actual object SecretStorageFactory {
    actual fun createSecretRepository(): SecretRepository {
        return SecretRepository(
            sharedPreferences = SharedPrefsSecretStorage(StorageFactory.contextForProject()),
            dataStore = DataStoreSecretStorage(StorageFactory.contextForProject()),
            encryptedPreferences = EncryptedPrefsSecretStorage(StorageFactory.contextForProject())
        )
    }
}

private class SharedPrefsSecretStorage(context: Context) : SecretStorageEngine {
    override val mecanismo: MecanismoSecreto = MecanismoSecreto.SHARED_PREFS
    private val prefs: SharedPreferences = context.getSharedPreferences("secretos_shared_prefs", Context.MODE_PRIVATE)

    override suspend fun guardar(llave: String, valor: String) {
        prefs.edit().putString(llave, valor).apply()
    }

    override suspend fun recuperar(llave: String): String? = prefs.getString(llave, null)
}

private class DataStoreSecretStorage(context: Context) : SecretStorageEngine {
    override val mecanismo: MecanismoSecreto = MecanismoSecreto.DATASTORE
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("secretos_datastore.preferences_pb") }
    )

    override suspend fun guardar(llave: String, valor: String) {
        val key = stringPreferencesKey(llave)
        dataStore.edit { prefs -> prefs[key] = valor }
    }

    override suspend fun recuperar(llave: String): String? {
        val key = stringPreferencesKey(llave)
        return dataStore.data.first()[key]
    }
}

private class EncryptedPrefsSecretStorage(context: Context) : SecretStorageEngine {
    override val mecanismo: MecanismoSecreto = MecanismoSecreto.ENCRYPTED_PREFS
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secretos_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun guardar(llave: String, valor: String) {
        prefs.edit().putString(llave, valor).apply()
    }

    override suspend fun recuperar(llave: String): String? = prefs.getString(llave, null)
}
