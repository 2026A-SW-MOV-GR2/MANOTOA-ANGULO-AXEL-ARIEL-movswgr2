package epn.tallermoviles

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ToastHandler(message: String, onShown: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onShown()
        }
    }
}
