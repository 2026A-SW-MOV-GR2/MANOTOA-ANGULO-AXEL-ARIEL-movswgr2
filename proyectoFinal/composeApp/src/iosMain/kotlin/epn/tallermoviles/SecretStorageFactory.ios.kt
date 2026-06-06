package epn.tallermoviles

actual object SecretStorageFactory {
    actual fun createSecretRepository(): SecretRepository {
        val engine = IosSecretStorage()
        return SecretRepository(engine, engine, engine)
    }
}

private class IosSecretStorage : SecretStorageEngine {
    override val mecanismo: MecanismoSecreto = MecanismoSecreto.SHARED_PREFS

    override suspend fun guardar(llave: String, valor: String) {
        error("Gestión de secretos implementada para Android en este proyecto académico.")
    }

    override suspend fun recuperar(llave: String): String? {
        error("Gestión de secretos implementada para Android en este proyecto académico.")
    }
}
