package put.poznan.freshguard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun CreditsScreen() {
    val infiniteTransition = rememberInfiniteTransition()
    val translateY by infiniteTransition.animateFloat(
        initialValue = 950f,
        targetValue = 550f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFridge(translateY)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Autorzy", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
            Text(text = "Maria Pietras", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(text = "majsterkovic", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

fun DrawScope.drawFridge(translateY: Float) {
    val fridgeColor = Color(0xFFC2C2C2)
    val doorColor = Color(0xFF7F7F7F)
    val handleColor = Color(0xFFD9D9D9)
    val fridgeWidth = 300f
    val fridgeHeight = 600f

    val centerX = (size.width - fridgeWidth) / 2

    drawRect(color = fridgeColor, size = Size(width = fridgeWidth, height = fridgeHeight), topLeft = Offset(x = centerX, y = translateY))
    drawRect(color = doorColor, size = Size(width = 180f, height = fridgeHeight), topLeft = Offset(x = centerX + 120f, y = translateY))
    drawCircle(color = handleColor, radius = 30f, center = Offset(x = centerX + 240f, y = translateY + fridgeHeight / 2))
}
