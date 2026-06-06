package epn.tallermoviles

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

actual object StorageFactory {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun createRepository(): GatoRepository {
        check(::appContext.isInitialized) { "StorageFactory.init(context) debe llamarse antes de App()" }
        return GatoRepository(
            sqlEngine = SqliteGatoStorage(appContext),
            noSqlEngine = SharedPreferencesGatoStorage(appContext)
        )
    }
}

private class GatoDbHelper(context: Context) : SQLiteOpenHelper(context, "gatos_sql.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE gatos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                edad TEXT NOT NULL,
                raza TEXT NOT NULL,
                descripcion TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS gatos")
        onCreate(db)
    }
}

private class SqliteGatoStorage(context: Context) : GatoStorageEngine {
    override val nombreMotor: String = "SQL"
    private val helper = GatoDbHelper(context)

    override fun obtenerTodos(): List<Gato> {
        val db = helper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, nombre, edad, raza, descripcion FROM gatos ORDER BY id ASC",
            null
        )
        val gatos = mutableListOf<Gato>()
        cursor.use {
            while (it.moveToNext()) {
                gatos.add(
                    Gato(
                        id = it.getLong(0),
                        nombre = it.getString(1),
                        edad = it.getString(2),
                        raza = it.getString(3),
                        descripcion = it.getString(4)
                    )
                )
            }
        }
        return gatos
    }

    override fun guardar(gato: Gato): Gato {
        val db = helper.writableDatabase
        val sql = "INSERT INTO gatos(nombre, edad, raza, descripcion) VALUES(?, ?, ?, ?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, gato.nombre)
        statement.bindString(2, gato.edad)
        statement.bindString(3, gato.raza)
        statement.bindString(4, gato.descripcion)
        val nuevoId = statement.executeInsert()
        return gato.copy(id = nuevoId)
    }

    override fun actualizar(gato: Gato) {
        val db = helper.writableDatabase
        val sql = "UPDATE gatos SET nombre=?, edad=?, raza=?, descripcion=? WHERE id=?"
        val statement = db.compileStatement(sql)
        statement.bindString(1, gato.nombre)
        statement.bindString(2, gato.edad)
        statement.bindString(3, gato.raza)
        statement.bindString(4, gato.descripcion)
        statement.bindLong(5, gato.id)
        statement.executeUpdateDelete()
    }

    override fun eliminar(id: Long) {
        helper.writableDatabase.delete("gatos", "id=?", arrayOf(id.toString()))
    }

    override fun limpiar() {
        helper.writableDatabase.delete("gatos", null, null)
    }
}

private class SharedPreferencesGatoStorage(context: Context) : GatoStorageEngine {
    override val nombreMotor: String = "NoSQL"
    private val prefs: SharedPreferences = context.getSharedPreferences("gatos_nosql", Context.MODE_PRIVATE)
    private val keyGatos = "gatos"
    private val keyNextId = "next_id"

    override fun obtenerTodos(): List<Gato> {
        val raw = prefs.getString(keyGatos, "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split("\n")
            .filter { it.isNotBlank() }
            .mapNotNull { decodeGato(it) }
            .sortedBy { it.id }
    }

    override fun guardar(gato: Gato): Gato {
        val nuevoId = prefs.getLong(keyNextId, 1L)
        val guardado = gato.copy(id = nuevoId)
        val actualizados = obtenerTodos() + guardado
        guardarLista(actualizados)
        prefs.edit().putLong(keyNextId, nuevoId + 1).apply()
        return guardado
    }

    override fun actualizar(gato: Gato) {
        guardarLista(obtenerTodos().map { if (it.id == gato.id) gato else it })
    }

    override fun eliminar(id: Long) {
        guardarLista(obtenerTodos().filterNot { it.id == id })
    }

    override fun limpiar() {
        prefs.edit().remove(keyGatos).putLong(keyNextId, 1L).apply()
    }

    private fun guardarLista(gatos: List<Gato>) {
        prefs.edit().putString(keyGatos, gatos.joinToString("\n") { encodeGato(it) }).apply()
    }

    private fun encodeGato(gato: Gato): String = listOf(
        gato.id.toString(), gato.nombre, gato.edad, gato.raza, gato.descripcion
    ).joinToString("|") { it.replace("|", " ").replace("\n", " ") }

    private fun decodeGato(raw: String): Gato? {
        val partes = raw.split("|", limit = 5)
        if (partes.size < 5) return null
        return Gato(
            id = partes[0].toLongOrNull() ?: return null,
            nombre = partes[1],
            edad = partes[2],
            raza = partes[3],
            descripcion = partes[4]
        )
    }
}
