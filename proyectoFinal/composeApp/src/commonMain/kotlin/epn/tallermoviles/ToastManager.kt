package epn.tallermoviles

import androidx.compose.runtime.Composable

@Composable
expect fun ToastHandler(message: String, onShown: () -> Unit)
