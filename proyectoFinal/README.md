# TallerMovilesEPN3 - Proyecto Red y Seguridad

Proyecto Kotlin Multiplatform / Compose usado como base del examen de persistencia dual y extendido para el proyecto práctico **Red y Almacenamiento Seguro**.

## Módulos incluidos

### 1. CRUD de Gatos con Persistencia Dual
- SQLite / SQL.
- NoSQL local basado en SharedPreferences tipo JSON/texto.
- Switch de cambio de motor en tiempo de ejecución.
- Patrón Repository.
- Logs en Logcat con la etiqueta `[INFO][PersistenciaDual]`.

### 2. Módulo REST API
Pantalla nueva **REST API**.

Funciones:
- Consulta de posts por ID usando JSONPlaceholder.
- Petición `GET /posts/{id}`.
- Edición de título y contenido.
- Actualización simulada con `PUT /posts/{id}`.
- Estados de carga para deshabilitar botones/campos mientras la petición está en tránsito.
- Logs en Logcat con `[INFO][RedSeguridad]`.

Servidor usado:
```text
https://jsonplaceholder.typicode.com
```

### 3. Módulo de Gestión de Secretos
Pantalla nueva **Secretos**.

Permite guardar y recuperar secretos mediante:
- SharedPreferences.
- Jetpack DataStore.
- EncryptedSharedPreferences.

Funcionamiento:
1. Ingresar una llave.
2. Ingresar un valor.
3. Seleccionar el mecanismo de persistencia.
4. Guardar.
5. Recuperar usando la misma llave y el mismo mecanismo.

## Navegación
Desde la pantalla principal de gatos existen dos botones:
- `REST API`.
- `Secretos`.

## Permiso requerido
El proyecto incluye el permiso de Internet en `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Sustentación sugerida
El proyecto demuestra conectividad asíncrona HTTP REST y almacenamiento seguro usando APIs nativas de Android. La arquitectura mantiene separación por repositorios: `PostRepository` para red y `SecretRepository` para secretos, evitando mezclar la lógica de interfaz con la lógica de datos.

## Versión v3 estable
- La aplicación inicia en menú principal.
- Todas las pantallas principales tienen botón de regreso.
- El formulario de gatos también tiene flecha de regreso y botón Cancelar para evitar quedarse dentro del formulario.
