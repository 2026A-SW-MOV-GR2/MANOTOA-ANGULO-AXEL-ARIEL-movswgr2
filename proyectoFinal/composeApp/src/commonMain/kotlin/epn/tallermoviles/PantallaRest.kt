package epn.tallermoviles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRestApi(onBack: () -> Unit) {
    val repository = remember { NetworkFactory.createPostRepository() }
    val scope = rememberCoroutineScope()

    var postId by remember { mutableStateOf("1") }
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Ingrese un ID y consulte un post de JSONPlaceholder.") }
    var cargando by remember { mutableStateOf(false) }
    var postActual by remember { mutableStateOf<PostRemoto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Módulo REST API") },
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
                    Text("Consulta y actualización de posts", fontWeight = FontWeight.Bold)
                    Text("Servidor: https://jsonplaceholder.typicode.com")
                    Text("Endpoint GET/PUT: /posts/{id}")
                }
            }

            OutlinedTextField(
                value = postId,
                onValueChange = { nuevo -> postId = nuevo.filter { it.isDigit() } },
                label = { Text("ID del post") },
                enabled = !cargando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val id = postId.toIntOrNull()
                    if (id == null || id <= 0) {
                        estado = "Ingrese un ID numérico válido."
                        return@Button
                    }
                    scope.launch {
                        cargando = true
                        estado = "Consultando /posts/$id ..."
                        runCatching { repository.consultar(id) }
                            .onSuccess { post ->
                                postActual = post
                                titulo = post.title
                                contenido = post.body
                                estado = "Consulta exitosa. Código esperado: 200 OK."
                            }
                            .onFailure { error ->
                                estado = "Error al consultar: ${error.message ?: "sin detalle"}"
                            }
                        cargando = false
                    }
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (cargando) "Procesando..." else "Consultar GET")
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título editable") },
                enabled = !cargando && postActual != null,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contenido,
                onValueChange = { contenido = it },
                label = { Text("Contenido editable") },
                enabled = !cargando && postActual != null,
                minLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val post = postActual ?: run {
                        estado = "Primero consulte un post antes de actualizar."
                        return@Button
                    }
                    scope.launch {
                        cargando = true
                        estado = "Enviando PUT /posts/${post.id} ..."
                        val modificado = post.copy(title = titulo, body = contenido)
                        runCatching { repository.actualizar(modificado) }
                            .onSuccess { actualizado ->
                                postActual = actualizado
                                titulo = actualizado.title
                                contenido = actualizado.body
                                estado = "Actualización simulada exitosa. Código esperado: 200 OK."
                            }
                            .onFailure { error ->
                                estado = "Error al actualizar: ${error.message ?: "sin detalle"}"
                            }
                        cargando = false
                    }
                },
                enabled = !cargando && postActual != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar PUT")
            }

            if (cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text(estado, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
