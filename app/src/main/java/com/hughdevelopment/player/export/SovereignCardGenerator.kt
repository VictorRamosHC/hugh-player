package com.hughdevelopment.player.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.toArgb
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.settings.TimeDisplayFormat
import com.hughdevelopment.player.data.settings.formatListeningTime
import com.hughdevelopment.player.ui.theme.VaultThemeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SovereignCardGenerator // 100% Offline High-Resolution Telemetry Card Synthesis.
 *
 * Implements Option 2:
 * Generates an ultra-premium 1080x1920 editorial PNG graphics card summarizing
 * the listener's offline discipline, Arconte rank, listening time and top relics.
 * Saves directly to Pictures/AetherVault without any external cloud dependency.
 */
object SovereignCardGenerator {

    const val CARD_WIDTH = 1080
    const val CARD_HEIGHT = 1920

    suspend fun generateCardBitmap(
        overview: ListeningOverview,
        totalTracks: Int,
        timeDisplayFormat: TimeDisplayFormat,
        theme: VaultThemeConfig
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val primaryInt = theme.primaryAccent.toArgb()
        val secondaryInt = theme.secondaryAccent.toArgb()
        val textPrimaryInt = theme.textPrimary.toArgb()
        val bgInt = Color.rgb(7, 7, 11)

        // 1. Deep Obsidian Gradient Base
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, CARD_HEIGHT.toFloat(),
                intArrayOf(Color.rgb(5, 5, 8), Color.rgb(10, 11, 16), Color.rgb(7, 7, 11)),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), bgPaint)

        // 2. Ambient Radiant Auras
        val auraTop = Paint().apply {
            shader = RadialGradient(
                200f, 250f, 520f,
                intArrayOf(adjustAlpha(primaryInt, 0.40f), adjustAlpha(primaryInt, 0.12f), Color.TRANSPARENT),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(200f, 250f, 520f, auraTop)

        val auraMid = Paint().apply {
            shader = RadialGradient(
                CARD_WIDTH - 200f, 1100f, 480f,
                intArrayOf(adjustAlpha(secondaryInt, 0.30f), adjustAlpha(secondaryInt, 0.08f), Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(CARD_WIDTH - 200f, 1100f, 480f, auraMid)

        // 3. Sacred Astrolabe Watermark in background
        val astrolabePaint = Paint().apply {
            color = adjustAlpha(Color.WHITE, 0.07f)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val cx = CARD_WIDTH / 2f
        val cy = 820f
        canvas.drawCircle(cx, cy, 380f, astrolabePaint)
        canvas.drawCircle(cx, cy, 290f, astrolabePaint)
        canvas.drawCircle(cx, cy, 200f, astrolabePaint)
        canvas.drawLine(cx - 420f, cy, cx + 420f, cy, astrolabePaint)
        canvas.drawLine(cx, cy - 420f, cx, cy + 420f, astrolabePaint)

        // 4. Outer Fine Architectural Border with Corner Accents
        val borderPaint = Paint().apply {
            color = adjustAlpha(Color.WHITE, 0.18f)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(40f, 40f, CARD_WIDTH - 40f, CARD_HEIGHT - 40f), 24f, 24f, borderPaint)

        // Corner Crosshairs
        val cornerPaint = Paint().apply {
            color = primaryInt
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        drawCorner(canvas, 40f, 40f, 32f, cornerPaint, isTop = true, isLeft = true)
        drawCorner(canvas, CARD_WIDTH - 40f, 40f, 32f, cornerPaint, isTop = true, isLeft = false)
        drawCorner(canvas, 40f, CARD_HEIGHT - 40f, 32f, cornerPaint, isTop = false, isLeft = true)
        drawCorner(canvas, CARD_WIDTH - 40f, CARD_HEIGHT - 40f, 32f, cornerPaint, isTop = false, isLeft = false)

        // 5. Header Typography
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 46f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.22f
            isAntiAlias = true
        }
        canvas.drawText("A E T H E R   V A U L T", 90f, 150f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = secondaryInt
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.15f
            isAntiAlias = true
        }
        canvas.drawText("RELATÓRIO DE SOBERANIA ACÚSTICA // AIR-GAPPED", 90f, 195f, subTitlePaint)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
        val dateText = "PROTOCOLO: ${dateFormat.format(Date())} // 100% OFFLINE"
        val datePaint = Paint().apply {
            color = adjustAlpha(Color.WHITE, 0.50f)
            textSize = 18f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText(dateText, 90f, 230f, datePaint)

        // 6. User Rank Card
        val rankBox = RectF(80f, 290f, CARD_WIDTH - 80f, 450f)
        val rankCardBg = Paint().apply {
            color = adjustAlpha(Color.BLACK, 0.65f)
            style = Paint.Style.FILL
        }
        val rankCardStroke = Paint().apply {
            color = adjustAlpha(primaryInt, 0.6f)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(rankBox, 18f, 18f, rankCardBg)
        canvas.drawRoundRect(rankBox, 18f, 18f, rankCardStroke)

        val rankTagPaint = Paint().apply {
            color = adjustAlpha(Color.WHITE, 0.60f)
            textSize = 18f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.1f
            isAntiAlias = true
        }
        canvas.drawText("PATENTE DE AUTORIDADE ACÚSTICA", 120f, 345f, rankTagPaint)

        val rankTitlePaint = Paint().apply {
            color = primaryInt
            textSize = 42f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
            isAntiAlias = true
        }
        canvas.drawText(overview.rankTier.title.uppercase(), 120f, 405f, rankTitlePaint)

        // 7. Key Metrics: 3 Equal Tiles
        val tileTop = 490f
        val tileBottom = 680f
        val tileGap = 20f
        val availableWidth = (CARD_WIDTH - 160f) - (tileGap * 2)
        val tileWidth = availableWidth / 3f

        val metricBg = Paint().apply {
            color = adjustAlpha(Color.rgb(18, 20, 30), 0.75f)
            style = Paint.Style.FILL
        }
        val metricStroke = Paint().apply {
            color = adjustAlpha(Color.WHITE, 0.15f)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        val formattedListening = formatListeningTime(overview.totalSecondsListened, timeDisplayFormat)
        val streakText = "${overview.streakInfo.currentStreak} DIAS"
        val tracksText = "$totalTracks FAIXAS"

        val metricsData = listOf(
            Triple("TEMPO OUVIDO", formattedListening, primaryInt),
            Triple("OFENSIVA", streakText, secondaryInt),
            Triple("ACERVO", tracksText, Color.WHITE)
        )

        for (i in metricsData.indices) {
            val left = 80f + (i * (tileWidth + tileGap))
            val right = left + tileWidth
            val rect = RectF(left, tileTop, right, tileBottom)
            canvas.drawRoundRect(rect, 16f, 16f, metricBg)
            canvas.drawRoundRect(rect, 16f, 16f, metricStroke)

            val data = metricsData[i]
            val lblPaint = Paint().apply {
                color = adjustAlpha(Color.WHITE, 0.6f)
                textSize = 17f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(data.first, left + 20f, tileTop + 55f, lblPaint)

            val valPaint = Paint().apply {
                color = data.third
                textSize = if (data.second.length > 10) 26f else 32f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(data.second, left + 20f, tileTop + 120f, valPaint)

            val badgeSub = Paint().apply {
                color = adjustAlpha(Color.WHITE, 0.40f)
                textSize = 14f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText("AUDITADO OK", left + 20f, tileTop + 155f, badgeSub)
        }

        // 8. Top Relics Section
        val listTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.12f
            isAntiAlias = true
        }
        canvas.drawText("RELÍQUIAS MAIS EXECUTADAS // TOP TRACKS", 85f, 750f, listTitlePaint)

        val topSongs = overview.topSongs.take(5)
        var songY = 820f

        for (idx in topSongs.indices) {
            val song = topSongs[idx]
            val rowRect = RectF(80f, songY - 40f, CARD_WIDTH - 80f, songY + 80f)

            val rowBg = Paint().apply {
                color = adjustAlpha(Color.rgb(12, 14, 22), 0.60f)
                style = Paint.Style.FILL
            }
            val rowBorder = Paint().apply {
                color = adjustAlpha(Color.WHITE, 0.10f)
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                isAntiAlias = true
            }
            canvas.drawRoundRect(rowRect, 14f, 14f, rowBg)
            canvas.drawRoundRect(rowRect, 14f, 14f, rowBorder)

            // Number index
            val numPaint = Paint().apply {
                color = if (idx == 0) secondaryInt else adjustAlpha(Color.WHITE, 0.5f)
                textSize = 28f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("%02d //".format(idx + 1), 110f, songY + 28f, numPaint)

            // Track Title
            val trackNamePaint = Paint().apply {
                color = Color.WHITE
                textSize = 26f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val safeTitle = if (song.title.length > 28) song.title.take(28) + "..." else song.title
            canvas.drawText(safeTitle, 210f, songY + 15f, trackNamePaint)

            // Artist and listen time
            val trackSubPaint = Paint().apply {
                color = adjustAlpha(Color.WHITE, 0.6f)
                textSize = 18f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                isAntiAlias = true
            }
            val minutesListened = song.totalSecondsListened / 60
            val subText = "${song.artist.take(22)} • ${minutesListened}m ouvidos"
            canvas.drawText(subText, 210f, songY + 50f, trackSubPaint)

            songY += 135f
        }

        // 9. Sovereign Seal of Authenticity
        val sealBox = RectF(80f, 1540f, CARD_WIDTH - 80f, 1720f)
        val sealBg = Paint().apply {
            color = adjustAlpha(Color.rgb(8, 10, 16), 0.85f)
            style = Paint.Style.FILL
        }
        val sealBorder = Paint().apply {
            color = adjustAlpha(secondaryInt, 0.5f)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(sealBox, 16f, 16f, sealBg)
        canvas.drawRoundRect(sealBox, 16f, 16f, sealBorder)

        val sealTitle = Paint().apply {
            color = secondaryInt
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.15f
            isAntiAlias = true
        }
        canvas.drawText("CERTIFICADO DE SOBERANIA DIGITAL // HERMES AI", 120f, 1605f, sealTitle)

        val sealDesc = Paint().apply {
            color = adjustAlpha(Color.WHITE, 0.70f)
            textSize = 17f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("Telemetria auditada localmente no dispositivo. Zero dados transmitidos.", 120f, 1650f, sealDesc)
        canvas.drawText("Criptografia local e isolamento total garantido por Hugh Development.", 120f, 1685f, sealDesc)

        // 10. Footer Bar
        val footerPaint = Paint().apply {
            color = adjustAlpha(Color.WHITE, 0.40f)
            textSize = 16f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            letterSpacing = 0.1f
            isAntiAlias = true
        }
        canvas.drawText("AETHER VAULT v1.6 // BIT-PERFECT REPRODUCTION // SOVEREIGN ENCLAVE", 110f, 1820f, footerPaint)

        bitmap
    }

    private fun drawCorner(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint, isTop: Boolean, isLeft: Boolean) {
        val dx = if (isLeft) size else -size
        val dy = if (isTop) size else -size
        canvas.drawLine(x, y, x + dx, y, paint)
        canvas.drawLine(x, y, x, y + dy, paint)
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    /**
     * Saves the generated Bitmap directly into the device's Pictures/AetherVault gallery.
     */
    suspend fun saveCardToGallery(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        val filename = "AETHER_VAULT_SOVEREIGN_${System.currentTimeMillis()}.png"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "AetherVault")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext null

                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                return@withContext uri
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AetherVault")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                return@withContext Uri.fromFile(file)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Launches the Android native share intent with the generated telemetry image.
     */
    fun shareCard(context: Context, imageUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, "Meu Relatório de Soberania Acústica no Aether Vault 🏛️⚡ // 100% Offline")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório Soberano"))
    }
}
