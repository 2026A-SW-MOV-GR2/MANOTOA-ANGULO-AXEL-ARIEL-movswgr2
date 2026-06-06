package epn.tallermoviles

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioGatos(
    gatoOriginal: Gato?,
    onSave: (Gato) -> Unit,
    onCancel: () -> Unit
) {
    var nombre by remember { mutableStateOf(gatoOriginal?.nombre ?: "") }
    var edad by remember { mutableStateOf(gatoOriginal?.edad ?: "") }
    var raza by remember { mutableStateOf(gatoOriginal?.raza ?: "") }
    var descripcion by remember { mutableStateOf(gatoOriginal?.descripcion ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (gatoOriginal == null) "Nuevo Gato" else "Editar Gato") },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = edad,
                onValueChange = { edad = it },
                label = { Text("Edad") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = raza,
                onValueChange = { raza = it },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSave(Gato(
                        id = gatoOriginal?.id ?: 0L,
                        nombre = nombre,
                        edad = edad,
                        raza = raza,
                        descripcion = descripcion
                    ))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
