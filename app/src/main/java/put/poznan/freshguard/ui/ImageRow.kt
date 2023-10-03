package put.poznan.freshguard.ui

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ImageRow(images: List<ByteArray>, onImageSelected: (ByteArray) -> Unit) {
    var selectedImage by remember { mutableStateOf(byteArrayOf()) }
    LazyRow {
        itemsIndexed(images) { _, image ->
            ImageCard(
                image = image,
                selectedImage = selectedImage,
                onImageSelected = { newImage ->
                    selectedImage = newImage
                    onImageSelected(newImage)
                }
            )
        }
    }
}