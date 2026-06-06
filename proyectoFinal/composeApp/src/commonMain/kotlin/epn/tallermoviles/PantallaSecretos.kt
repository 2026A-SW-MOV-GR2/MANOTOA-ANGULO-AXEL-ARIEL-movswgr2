package epn.tallermoviles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionSecretos(onBack: () -> Unit) {
    val repository = remember { SecretStorageFactory.createSecretRepository() }
    val scope = rememberCoroutineScope()

    var mecanismo by remember { mutableStateOf(MecanismoSecreto.SHARED_PREFS) }
    var llave by remember { mutableStateOf("token") }
    var valor by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("Ingrese llave y valor. Luego guarde o recupere el secreto.") }
    var procesando by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Secretos") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Almacenamiento seguro", fontWeight = FontWeight.Bold)
                    Text("Guarda y recupera secretos por llave usando el mecanismo seleccionado.")
                    Text("Activo: ${mecanismo.etiqueta}")
                }
            }

            MecanismoSecreto.values().forEach { opcion ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadioButton(
                        selected = mecanismo == opcion,
                        onClick = { mecanismo = opcion },
                        enabled = !procesando
                    )
                    Column {
                        Text(opcion.etiqueta, fontWeight = FontWeight.Bold)
                        Text(descripcionMecanismo(opcion), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            OutlinedTextField(
                value = llave,
                onValueChange = { llave = it.trim() },
                label = { Text("Llave") },
                enabled = !procesando,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = valor,
                onValueChange = { valor = it },
                label = { Text("Valor secreto") },
                enabled = !procesando,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (llave.isBlank() || valor.isBlank()) {
                            resultado = "Debe ingresar una llave y un valor."
                            return@Button
                        }
                        scope.launch {
                            procesando = true
                            runCatching { repository.guardar(mecanismo, llave, valor) }
                                .onSuccess { resultado = "Secreto guardado en ${mecanismo.etiqueta}." }
                                .onFailure { resultado = "Error al guardar: ${it.message ?: "sin detalle"}" }
                            procesando = false
                        }
                    },
                    enabled = !procesando,
                    modifier = Modifier.weight(1f)
                ) { Text("Guardar") }

                Button(
                    onClick = {
                        if (llave.isBlank()) {
                            resultado = "Ingrese la llave que desea recuperar."
                            return@Button
                        }
                        scope.launch {
                            procesando = true
                            runCatching { repository.recuperar(mecanismo, llave) }
                                .onSuccess { valorRecuperado ->
                                    resultado = valorRecuperado?.let { "Valor recuperado: $it" }
                                        ?: "No existe un secreto para esa llave en ${mecanismo.etiqueta}."
                                }
                                .onFailure { resultado = "Error al recuperar: ${it.message ?: "sin detalle"}" }
                            procesando = false
                        }
                    },
                    enabled = !procesando,
                    modifier = Modifier.weight(1f)
                ) { Text("Recuperar") }
            }

            if (procesando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(resultado)
        }
    }
}

private fun descripcionMecanismo(mecanismo: MecanismoSecreto): String = when (mecanismo) {
    MecanismoSecreto.SHARED_PREFS -> "Clave-valor plano para preferencias sencillas."
    MecanismoSecreto.DATASTORE -> "Alternativa moderna y reactiva basada en flujos."
    MecanismoSecreto.ENCRYPTED_PREFS -> "Clave-valor cifrado automáticamente en disco."
}
