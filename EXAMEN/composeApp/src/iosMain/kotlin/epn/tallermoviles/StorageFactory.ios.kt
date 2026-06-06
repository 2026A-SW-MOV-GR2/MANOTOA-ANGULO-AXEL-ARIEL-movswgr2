package epn.tallermoviles

actual object StorageFactory {
    actual fun createRepository(): GatoRepository = GatoRepository(
        sqlEngine = MemoriaGatoStorage("SQL iOS temporal"),
        noSqlEngine = MemoriaGatoStorage("NoSQL iOS temporal")
    )
}

private class MemoriaGatoStorage(override val nombreMotor: String) : GatoStorageEngine {
    private val gatos = mutableListOf<Gato>()
    private var nextId = 1L

    override fun obtenerTodos(): List<Gato> = gatos.toList()
    override fun guardar(gato: Gato): Gato {
        val guardado = gato.copy(id = nextId++)
        gatos.add(guardado)
        return guardado
    }
    override fun actualizar(gato: Gato) {
        val index = gatos.indexOfFirst { it.id == gato.id }
        if (index != -1) gatos[index] = gato
    }
    override fun eliminar(id: Long) {
        gatos.removeAll { it.id == id }
    }
    override fun limpiar() {
        gatos.clear()
        nextId = 1L
    }
}
