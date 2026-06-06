package epn.tallermoviles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GatoRepositoryTest {

    @Test
    fun crudEnMotorSql() {
        val repository = crearRepositoryDePrueba()
        repository.cambiarMotor(MotorPersistencia.SQL)

        val guardado = repository.guardar(Gato(nombre = "Michi", edad = "2", raza = "Persa", descripcion = "Duerme mucho"))
        assertEquals(1, repository.obtenerTodos().size)
        assertEquals("Michi", repository.obtenerTodos().first().nombre)

        repository.actualizar(guardado.copy(nombre = "Michi actualizado"))
        assertEquals("Michi actualizado", repository.obtenerTodos().first().nombre)

        repository.eliminar(guardado.id)
        assertTrue(repository.obtenerTodos().isEmpty())
    }

    @Test
    fun crudEnMotorNoSql() {
        val repository = crearRepositoryDePrueba()
        repository.cambiarMotor(MotorPersistencia.NOSQL)

        val guardado = repository.guardar(Gato(nombre = "Garfield", edad = "5", raza = "Naranja", descripcion = "Ama la lasaña"))
        assertEquals(1, repository.obtenerTodos().size)
        assertEquals("Garfield", repository.obtenerTodos().first().nombre)

        repository.actualizar(guardado.copy(raza = "Atigrado"))
        assertEquals("Atigrado", repository.obtenerTodos().first().raza)

        repository.eliminar(guardado.id)
        assertTrue(repository.obtenerTodos().isEmpty())
    }

    @Test
    fun cambioDeMotorMantieneDatosSeparados() {
        val repository = crearRepositoryDePrueba()

        repository.cambiarMotor(MotorPersistencia.SQL)
        repository.guardar(Gato(nombre = "SQL Cat", edad = "1", raza = "A", descripcion = "Guardado en SQL"))
        assertEquals(1, repository.obtenerTodos().size)

        repository.cambiarMotor(MotorPersistencia.NOSQL)
        assertEquals(0, repository.obtenerTodos().size)
        repository.guardar(Gato(nombre = "NoSQL Cat", edad = "3", raza = "B", descripcion = "Guardado en NoSQL"))
        assertEquals("NoSQL Cat", repository.obtenerTodos().first().nombre)

        repository.cambiarMotor(MotorPersistencia.SQL)
        assertEquals("SQL Cat", repository.obtenerTodos().first().nombre)
    }

    private fun crearRepositoryDePrueba(): GatoRepository = GatoRepository(
        sqlEngine = MemoriaGatoStorageTest("SQL"),
        noSqlEngine = MemoriaGatoStorageTest("NoSQL")
    )
}

private class MemoriaGatoStorageTest(override val nombreMotor: String) : GatoStorageEngine {
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
