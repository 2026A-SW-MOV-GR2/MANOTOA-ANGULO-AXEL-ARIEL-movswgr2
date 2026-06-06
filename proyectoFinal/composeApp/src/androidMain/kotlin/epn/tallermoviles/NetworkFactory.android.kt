package epn.tallermoviles

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

actual object NetworkFactory {
    actual fun createPostRepository(): PostRepository = PostRepository(AndroidPostRemoteDataSource())
}

private class AndroidPostRemoteDataSource : PostRemoteDataSource {
    private val baseUrl = "https://jsonplaceholder.typicode.com"

    override suspend fun consultarPost(id: Int): PostRemoto = withContext(Dispatchers.IO) {
        val json = request(method = "GET", endpoint = "/posts/$id")
        parsePost(json)
    }

    override suspend fun actualizarPost(post: PostRemoto): PostRemoto = withContext(Dispatchers.IO) {
        val jsonBody = """
            {
              "id": ${post.id},
              "userId": ${post.userId},
              "title": "${escapeJson(post.title)}",
              "body": "${escapeJson(post.body)}"
            }
        """.trimIndent()
        val json = request(method = "PUT", endpoint = "/posts/${post.id}", body = jsonBody)
        parsePost(json)
    }

    private fun request(method: String, endpoint: String, body: String? = null): String {
        val connection = (URL(baseUrl + endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
        }

        try {
            if (body != null) {
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) error("HTTP $code: $response")
            println("[INFO][RedSeguridad] Respuesta HTTP $code para $method $endpoint")
            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun parsePost(json: String): PostRemoto {
        return PostRemoto(
            userId = extractInt(json, "userId") ?: 1,
            id = extractInt(json, "id") ?: 0,
            title = extractString(json, "title").orEmpty(),
            body = extractString(json, "body").orEmpty()
        )
    }

    private fun extractInt(json: String, key: String): Int? {
        val regex = Regex("\\\"$key\\\"\\s*:\\s*(\\d+)")
        return regex.find(json)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun extractString(json: String, key: String): String? {
        val regex = Regex("\\\"$key\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"", RegexOption.DOT_MATCHES_ALL)
        return regex.find(json)?.groupValues?.getOrNull(1)
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
            ?.replace("\\\\", "\\")
    }

    private fun escapeJson(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
}
