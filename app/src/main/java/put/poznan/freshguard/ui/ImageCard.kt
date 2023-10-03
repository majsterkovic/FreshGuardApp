package put.poznan.freshguard.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import put.poznan.freshguard.ui.theme.getPrimaryColor

@Composable
fun ImageCard(image: ByteArray, selectedImage: ByteArray, onImageSelected: (ByteArray) -> Unit) {
    val isSelected = image.contentEquals(selectedImage)
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(image) {
        imageBitmap.value = BitmapFactory.decodeByteArray(image, 0, image.size)?.asImageBitmap()
    }
    Card(
        modifier = Modifier
            .padding(8.dp)
            .border(
                width = 3.dp,
                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onImageSelected(image) },

        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        imageBitmap.value?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}