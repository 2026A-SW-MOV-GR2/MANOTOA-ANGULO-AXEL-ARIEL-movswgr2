package epn.tallermoviles

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

enum class Pantalla { LISTA, FORMULARIO }

@Composable
fun App() {
    MaterialTheme {
        val repository = remember { StorageFactory.createRepository() }
        val listaGatos = remember { mutableStateListOf<Gato>() }

        var pantallaActual by remember { mutableStateOf(Pantalla.LISTA) }
        var gatoSeleccionado by remember { mutableStateOf<Gato?>(null) }
        var mensajeToast by remember { mutableStateOf("") }
        var motorActivo by remember { mutableStateOf(repository.motorActivo) }

        fun recargarLista() {
            listaGatos.clear()
            listaGatos.addAll(repository.obtenerTodos())
        }

        LaunchedEffect(Unit) {
            recargarLista()
        }

        ToastHandler(message = mensajeToast, onShown = { mensajeToast = "" })

        when (pantallaActual) {
            Pantalla.LISTA -> {
                ListaGatos(
                    gatos = listaGatos,
                    motorActivo = motorActivo,
                    onMotorChange = { nuevoMotor ->
                        motorActivo = nuevoMotor
                        listaGatos.clear()
                        listaGatos.addAll(repository.cambiarMotor(nuevoMotor))
                        mensajeToast = "Motor activo: ${nuevoMotor.etiqueta}"
                    },
                    onAddGato = {
                        gatoSeleccionado = null
                        pantallaActual = Pantalla.FORMULARIO
                    },
                    onEditGato = { gato ->
                        gatoSeleccionado = gato
                        pantallaActual = Pantalla.FORMULARIO
                    },
                    onDeleteGato = { gato ->
                        repository.eliminar(gato.id)
                        recargarLista()
                        mensajeToast = "Gato eliminado en ${motorActivo.etiqueta}"
                    }
                )
            }
            Pantalla.FORMULARIO -> {
                FormularioGatos(
                    gatoOriginal = gatoSeleccionado,
                    onSave = { gatoEditado ->
                        if (gatoSeleccionado == null) {
                            repository.guardar(gatoEditado)
                            mensajeToast = "Gato guardado en ${motorActivo.etiqueta}"
                        } else {
                            repository.actualizar(gatoEditado)
                            mensajeToast = "Gato actualizado en ${motorActivo.etiqueta}"
                        }
                        recargarLista()
                        pantallaActual = Pantalla.LISTA
                    },
                    onCancel = {
                        pantallaActual = Pantalla.LISTA
                    }
                )
            }
        }
    }
}
