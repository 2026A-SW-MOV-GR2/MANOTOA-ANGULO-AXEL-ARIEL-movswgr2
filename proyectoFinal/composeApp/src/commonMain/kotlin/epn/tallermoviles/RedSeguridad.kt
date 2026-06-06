package epn.tallermoviles

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PostRemoto(
    val userId: Int = 1,
    val id: Int = 0,
    val title: String = "",
    val body: String = ""
)

interface PostRemoteDataSource {
    suspend fun consultarPost(id: Int): PostRemoto
    suspend fun actualizarPost(post: PostRemoto): PostRemoto
}

class PostRepository(private val dataSource: PostRemoteDataSource) {
    suspend fun consultar(id: Int): PostRemoto {
        println("[INFO][RedSeguridad] GET /posts/$id")
        return dataSource.consultarPost(id)
    }

    suspend fun actualizar(post: PostRemoto): PostRemoto {
        println("[INFO][RedSeguridad] PUT /posts/${post.id}")
        return dataSource.actualizarPost(post)
    }
}

expect object NetworkFactory {
    fun createPostRepository(): PostRepository
}

enum class MecanismoSecreto(val etiqueta: String) {
    SHARED_PREFS("SharedPreferences"),
    DATASTORE("Jetpack DataStore"),
    ENCRYPTED_PREFS("EncryptedSharedPreferences")
}

interface SecretStorageEngine {
    val mecanismo: MecanismoSecreto
    suspend fun guardar(llave: String, valor: String)
    suspend fun recuperar(llave: String): String?
}

class SecretRepository(
    private val sharedPreferences: SecretStorageEngine,
    private val dataStore: SecretStorageEngine,
    private val encryptedPreferences: SecretStorageEngine
) {
    private fun engine(mecanismo: MecanismoSecreto): SecretStorageEngine = when (mecanismo) {
        MecanismoSecreto.SHARED_PREFS -> sharedPreferences
        MecanismoSecreto.DATASTORE -> dataStore
        MecanismoSecreto.ENCRYPTED_PREFS -> encryptedPreferences
    }

    suspend fun guardar(mecanismo: MecanismoSecreto, llave: String, valor: String) {
        engine(mecanismo).guardar(llave, valor)
        println("[INFO][RedSeguridad] Secreto guardado en ${mecanismo.etiqueta}: llave=$llave")
    }

    suspend fun recuperar(mecanismo: MecanismoSecreto, llave: String): String? {
        val valor = engine(mecanismo).recuperar(llave)
        println("[INFO][RedSeguridad] Recuperación en ${mecanismo.etiqueta}: llave=$llave existe=${valor != null}")
        return valor
    }
}

expect object SecretStorageFactory {
    fun createSecretRepository(): SecretRepository
}
