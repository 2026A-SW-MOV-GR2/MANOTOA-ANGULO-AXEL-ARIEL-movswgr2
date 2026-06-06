package epn.tallermoviles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaGatos(
    gatos: List<Gato>,
    motorActivo: MotorPersistencia,
    onMotorChange: (MotorPersistencia) -> Unit,
    onAddGato: () -> Unit,
    onEditGato: (Gato) -> Unit,
    onDeleteGato: (Gato) -> Unit,
    onOpenRest: () -> Unit,
    onOpenSecretos: () -> Unit,
    onBackToMenu: () -> Unit
) {
    var gatoAEliminar by remember { mutableStateOf<Gato?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Gatos 🐱") },
                navigationIcon = { TextButton(onClick = onBackToMenu) { Text("←") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGato) {
                Text("➕", fontSize = 24.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            MotorPersistenciaSelector(
                motorActivo = motorActivo,
                onMotorChange = onMotorChange
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onOpenRest, modifier = Modifier.weight(1f)) {
                    Text("REST API")
                }
                Button(onClick = onOpenSecretos, modifier = Modifier.weight(1f)) {
                    Text("Secretos")
                }
            }

            if (gatos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay gatos guardados en ${motorActivo.etiqueta}")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(gatos) { gato ->
                        GatoCard(
                            gato = gato,
                            onClick = { onEditGato(gato) },
                            onDelete = { gatoAEliminar = gato }
                        )
                    }
                }
            }
        }

        // Dialogo de confirmación
        gatoAEliminar?.let { gato ->
            AlertDialog(
                onDismissRequest = { gatoAEliminar = null },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Estás seguro de que quieres eliminar a ${gato.nombre}?") },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteGato(gato)
                        gatoAEliminar = null
                    }) {
                        Text("Eliminar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { gatoAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}


@Composable
fun MotorPersistenciaSelector(
    motorActivo: MotorPersistencia,
    onMotorChange: (MotorPersistencia) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Motor de persistencia", fontWeight = FontWeight.Bold)
                Text("Activo: ${motorActivo.etiqueta}", style = MaterialTheme.typography.bodyMedium)
            }
            Text("SQL", fontSize = 12.sp)
            Switch(
                checked = motorActivo == MotorPersistencia.NOSQL,
                onCheckedChange = { checked ->
                    onMotorChange(if (checked) MotorPersistencia.NOSQL else MotorPersistencia.SQL)
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text("NoSQL", fontSize = 12.sp)
        }
    }
}

@Composable
fun GatoCard(gato: Gato, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Simple
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("🐈", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = gato.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "${gato.raza} • ${gato.edad} años", style = MaterialTheme.typography.bodyMedium)
            }

            IconButton(onClick = onDelete) {
                Text("🗑️", fontSize = 20.sp)
            }
        }
    }
}
