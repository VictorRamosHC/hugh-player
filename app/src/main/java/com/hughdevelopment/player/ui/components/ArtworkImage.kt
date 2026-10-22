package com.hughdevelopment.player.ui.components

import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.hughdevelopment.player.R
import com.hughdevelopment.player.ui.theme.CyberAmber

object ArtworkCacheRegistry {
    // Thread-safe bounded LRU cache limited to 1,000 entries
    // Eliminates unbounded memory growth in huge 10,000+ track libraries
    private val lruFailedCache = LruCache<Uri, Boolean>(1000)

    fun isFailed(uri: Uri): Boolean = synchronized(this) {
        lruFailedCache.get(uri) == true
    }

    fun markFailed(uri: Uri) = synchronized(this) {
        lruFailedCache.put(uri, true)
    }
}

@Composable
fun ArtworkImage(
    artworkUri: Uri?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    accentColor: Color = CyberAmber,
    targetDimensionPx: Int = 128
) {
    val isKnownFailed = artworkUri == null || ArtworkCacheRegistry.isFailed(artworkUri)
    var isLoadFailed by remember(artworkUri) { mutableStateOf(isKnownFailed) }

    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        if (!isLoadFailed && artworkUri != null) {
            val context = LocalContext.current
            AsyncImage(
                model = remember(artworkUri, targetDimensionPx) {
                    ImageRequest.Builder(context)
                        .data(artworkUri)
                        .size(Size(targetDimensionPx, targetDimensionPx))
                        .precision(Precision.INEXACT)
                        .scale(Scale.FILL)
                        .allowHardware(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .crossfade(false)
                        .build()
                },
                contentDescription = "Capa do Álbum",
                contentScale = ContentScale.Crop,
                onError = {
                    ArtworkCacheRegistry.markFailed(artworkUri)
                    isLoadFailed = true
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // High-contrast neoclassical dithered bust fallback (Rendered instantly from optimized resource)
            Image(
                painter = painterResource(id = R.drawable.placeholder_statue),
                contentDescription = "Placeholder Neoclássico",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
