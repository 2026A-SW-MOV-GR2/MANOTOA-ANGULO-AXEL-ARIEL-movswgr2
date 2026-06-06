package epn.tallermoviles

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource

@Composable
actual fun getTextoTaller(): String {
    return stringResource(id = R.string.mi_texto)
}

@Composable
actual fun getColorTaller(): Color {
    return colorResource(id = R.color.mi_color_txt)
}

@Composable
actual fun getFondoTaller(): Color {
    return colorResource(id = R.color.mi_fondo)
}