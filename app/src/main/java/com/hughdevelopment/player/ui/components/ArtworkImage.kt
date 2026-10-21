package com.hughdevelopment.player.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hughdevelopment.player.ui.theme.CyberAmber

@Composable
fun ArtworkImage(
    artworkUri: Uri?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    accentColor: Color = CyberAmber
) {
    var isLoadFailed by remember(artworkUri) { mutableStateOf(artworkUri == null) }

    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        if (!isLoadFailed && artworkUri != null) {
            val context = LocalContext.current
            AsyncImage(
                model = remember(artworkUri) {
                    ImageRequest.Builder(context)
                        .data(artworkUri)
                        .crossfade(false)
                        .build()
                },
                contentDescription = "Capa do Álbum",
                contentScale = ContentScale.Crop,
                onError = { isLoadFailed = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            StatueGlyphView(
                modifier = Modifier.fillMaxSize(),
                accentColor = accentColor
            )
        }
    }
}
