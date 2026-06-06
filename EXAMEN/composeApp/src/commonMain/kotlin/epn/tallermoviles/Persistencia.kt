package epn.tallermoviles

enum class MotorPersistencia(val etiqueta: String) {
    SQL("SQLite / SQL"),
    NOSQL("NoSQL / JSON local")
}

interface GatoStorageEngine {
    val nombreMotor: String
    fun obtenerTodos(): List<Gato>
    fun guardar(gato: Gato): Gato
    fun actualizar(gato: Gato)
    fun eliminar(id: Long)
    fun limpiar()
}

class GatoRepository(
    private val sqlEngine: GatoStorageEngine,
    private val noSqlEngine: GatoStorageEngine
) {
    var motorActivo: MotorPersistencia = MotorPersistencia.SQL
        private set

    private fun engineActual(): GatoStorageEngine = when (motorActivo) {
        MotorPersistencia.SQL -> sqlEngine
        MotorPersistencia.NOSQL -> noSqlEngine
    }

    fun cambiarMotor(nuevoMotor: MotorPersistencia): List<Gato> {
        motorActivo = nuevoMotor
        log("Motor activo: ${nuevoMotor.etiqueta}")
        return obtenerTodos()
    }

    fun obtenerTodos(): List<Gato> = engineActual().obtenerTodos()

    fun guardar(gato: Gato): Gato {
        val guardado = engineActual().guardar(gato)
        log("Registro guardado en ${engineActual().nombreMotor}: $guardado")
        return guardado
    }

    fun actualizar(gato: Gato) {
        engineActual().actualizar(gato)
        log("Registro actualizado en ${engineActual().nombreMotor}: $gato")
    }

    fun eliminar(id: Long) {
        engineActual().eliminar(id)
        log("Registro eliminado en ${engineActual().nombreMotor}: id=$id")
    }

    fun limpiarMotorActual() {
        engineActual().limpiar()
        log("Datos eliminados de ${engineActual().nombreMotor}")
    }

    private fun log(mensaje: String) {
        println("[INFO][PersistenciaDual] $mensaje")
    }
}

expect object StorageFactory {
    fun createRepository(): GatoRepository
}
