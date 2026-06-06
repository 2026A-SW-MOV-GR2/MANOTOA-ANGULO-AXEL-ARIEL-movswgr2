package epn.tallermoviles

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Versión 2: se agregó una pantalla de menú para que REST y Secretos sean visibles desde el inicio.
enum class Pantalla { MENU, LISTA, FORMULARIO, REST, SECRETOS }

@Composable
fun App() {
    MaterialTheme {
        val repository = remember { StorageFactory.createRepository() }
        val listaGatos = remember { mutableStateListOf<Gato>() }

        var pantallaActual by remember { mutableStateOf(Pantalla.MENU) }
        var gatoSeleccionado by remember { mutableStateOf<Gato?>(null) }
        var mensajeToast by remember { mutableStateOf("") }
        var motorActivo by remember { mutableStateOf(repository.motorActivo) }

        fun recargarLista() {
            listaGatos.clear()
            listaGatos.addAll(repository.obtenerTodos())
        }

        LaunchedEffect(Unit) { recargarLista() }

        ToastHandler(message = mensajeToast, onShown = { mensajeToast = "" })

        when (pantallaActual) {
            Pantalla.MENU -> MenuPrincipal(
                onOpenGatos = {
                    recargarLista()
                    pantallaActual = Pantalla.LISTA
                },
                onOpenRest = { pantallaActual = Pantalla.REST },
                onOpenSecretos = { pantallaActual = Pantalla.SECRETOS }
            )

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
                    },
                    onOpenRest = { pantallaActual = Pantalla.REST },
                    onOpenSecretos = { pantallaActual = Pantalla.SECRETOS },
                    onBackToMenu = { pantallaActual = Pantalla.MENU }
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
                    onCancel = { pantallaActual = Pantalla.LISTA }
                )
            }

            Pantalla.REST -> PantallaRestApi(onBack = { pantallaActual = Pantalla.MENU })

            Pantalla.SECRETOS -> PantallaGestionSecretos(onBack = { pantallaActual = Pantalla.MENU })
        }
    }
}

@Composable
private fun MenuPrincipal(
    onOpenGatos: () -> Unit,
    onOpenRest: () -> Unit,
    onOpenSecretos: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBarSimple("Proyecto Red y Seguridad") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Seleccione el módulo a probar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ModuloCard(
                titulo = "CRUD de Gatos",
                descripcion = "Examen anterior: persistencia dual SQL / NoSQL.",
                boton = "Abrir gatos",
                onClick = onOpenGatos
            )
            ModuloCard(
                titulo = "REST API",
                descripcion = "GET y PUT con JSONPlaceholder usando conexión HTTP asíncrona.",
                boton = "Abrir REST",
                onClick = onOpenRest
            )
            ModuloCard(
                titulo = "Secretos",
                descripcion = "SharedPreferences, DataStore y EncryptedSharedPreferences.",
                boton = "Abrir secretos",
                onClick = onOpenSecretos
            )
        }
    }
}

@Composable
private fun ModuloCard(titulo: String, descripcion: String, boton: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold)
            Text(descripcion, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(boton) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarSimple(title: String) {
    TopAppBar(title = { Text(title) })
}
