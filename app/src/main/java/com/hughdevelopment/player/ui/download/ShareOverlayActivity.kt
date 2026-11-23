package com.hughdevelopment.player.ui.download

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hughdevelopment.player.HughPlayerApplication
import com.hughdevelopment.player.data.download.ExtractedAudioInfo
import com.hughdevelopment.player.data.download.YouTubeExtractorHelper
import com.hughdevelopment.player.service.DownloadService
import com.hughdevelopment.player.ui.components.neoGlassCard
import com.hughdevelopment.player.ui.components.quartzGlassCapsule
import com.hughdevelopment.player.ui.theme.HughPlayerTheme
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.VaultThemes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShareOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as? HughPlayerApplication
        val activeThemeId = app?.settingsManager?.settings?.value?.activeThemeId ?: VaultThemes.Classic.id.name
        val themeConfig = VaultThemes.fromId(activeThemeId)

        val sharedText = extractSharedUrl(intent)

        setContent {
            HughPlayerTheme(themeConfig = themeConfig) {
                ShareOverlayContent(
                    initialUrl = sharedText,
                    onDismiss = { finish() },
                    onConfirmDownload = { url ->
                        try {
                            app?.downloadManager?.enqueueDownload(
                                url = url,
                                onStarted = {
                                    Toast.makeText(this@ShareOverlayActivity, "Ingestão iniciada no Vault!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    Toast.makeText(this@ShareOverlayActivity, "Erro: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                            // Tenta iniciar serviço de notificação em background de forma segura
                            try {
                                DownloadService.startDownload(this@ShareOverlayActivity, url)
                            } catch (_: Exception) {}
                        } catch (e: Exception) {
                            Toast.makeText(this@ShareOverlayActivity, "Falha ao enfileirar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        finish()
                    }
                )
            }
        }
    }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent == null || intent.action != Intent.ACTION_SEND) return null
        val extraText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val videoId = YouTubeExtractorHelper.extractVideoId(extraText)
        return if (videoId != null) "https://www.youtube.com/watch?v=$videoId" else null
    }
}

@Composable
fun ShareOverlayContent(
    initialUrl: String?,
    onDismiss: () -> Unit,
    onConfirmDownload: (String) -> Unit
) {
    val theme = LocalVaultTheme.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(initialUrl != null) }
    var extractedInfo by remember { mutableStateOf<ExtractedAudioInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialUrl) {
        if (initialUrl != null) {
            isLoading = true
            errorMessage = null
            try {
                val info = YouTubeExtractorHelper.extractAudio(initialUrl)
                extractedInfo = info
            } catch (e: Exception) {
                errorMessage = e.message ?: "Falha ao extrair metadados"
            } finally {
                isLoading = false
            }
        }
    }

    // Modal translúcido
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .neoGlassCard(
                    cornerRadius = theme.cardCornerRadius.coerceAtLeast(14.dp),
                    backgroundColor = theme.surfaceColor.copy(alpha = 0.95f),
                    borderWidth = 1.dp,
                    topRimColor = theme.primaryAccent.copy(alpha = 0.6f),
                    bottomRimColor = theme.secondaryAccent.copy(alpha = 0.2f)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header com glifo do tema
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CloudDownload,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${theme.lexicon.brandHeader.split("//").first().trim()} // INGESTÃO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = theme.primaryAccent,
                            letterSpacing = 1.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar",
                        tint = SigilWhite.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onDismiss)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            color = theme.primaryAccent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Decodificando fluxos de áudio...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = SigilWhite.copy(alpha = 0.8f)
                        )
                    }

                    errorMessage != null -> {
                        Text(
                            text = "AVISO DE CONEXÃO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = theme.secondaryAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = SigilWhite.copy(alpha = 0.7f)
                        )
                    }

                    extractedInfo != null -> {
                        val info = extractedInfo!!

                        // Card da mídia encontrada com as cores do tema
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(theme.cardCornerRadius.coerceAtLeast(8.dp)))
                                .background(theme.cardBorderColor.copy(alpha = 0.4f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!info.thumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = info.thumbnailUrl,
                                    contentDescription = info.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1C1C2A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MusicNote,
                                        contentDescription = null,
                                        tint = theme.primaryAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = info.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = SigilWhite,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${info.artist} • ${info.format.uppercase()}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = theme.primaryAccent.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Botão de Confirmação de Download
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .quartzGlassCapsule(
                                    cornerRadius = 14.dp,
                                    backgroundColor = theme.primaryAccent.copy(alpha = 0.25f),
                                    accentColor = theme.primaryAccent
                                )
                                .clickable {
                                    onConfirmDownload(info.originalUrl)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = SigilWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BAIXAR PARA O VAULT",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SigilWhite,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = "Nenhum link detectado",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = SigilWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
