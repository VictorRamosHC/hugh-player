package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.R
import com.hughdevelopment.player.data.ai.HermesPetEngine
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Playlist
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.ObsidianGlass
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.SpecularGlassRimTop
import com.hughdevelopment.player.ui.theme.VoidBlack
import com.hughdevelopment.player.ui.theme.WireframeBorder
import com.hughdevelopment.player.ui.theme.LocalVaultTheme

@Composable
fun HermesPetBanner(
    overview: ListeningOverview,
    songsCount: Int,
    playlistsCount: Int,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalVaultTheme.current
    val quip = remember(overview, songsCount, playlistsCount) {
        HermesPetEngine.getDynamicGreeting(overview, songsCount, playlistsCount)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = if (theme.cardCornerRadius < 12.dp) theme.cardCornerRadius else 12.dp,
                backgroundColor = Color(0xF20D0F18),
                borderWidth = 1.dp,
                topRimColor = theme.primaryAccent.copy(alpha = 0.7f),
                bottomRimColor = SpecularGlassRimTop
            )
            .clickable { onOpenChat() }
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sovereign Astrolabe Oracle Eye for Hermes (Compact HUD size)
            HermesAstrolabeOracleEye(
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = theme.lexicon.petName,
                        color = theme.primaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(theme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = quip,
                    color = SigilWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    lineHeight = 15.sp,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(if (theme.isBrutalistSharp) RoundedCornerShape(4.dp) else CircleShape)
                    .background(theme.primary.copy(alpha = 0.15f))
                    .border(1.dp, theme.primaryAccent.copy(alpha = 0.5f), if (theme.isBrutalistSharp) RoundedCornerShape(4.dp) else CircleShape)
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Conversar",
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HermesDialogueSheet(
    overview: ListeningOverview,
    songsCount: Int,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onAutoOrganize: () -> Unit,
    onPlayFavorites: () -> Unit
) {
    val theme = LocalVaultTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var userInput by remember { mutableStateOf("") }

    val initialGreeting = remember {
        HermesPetEngine.HermesMessage(
            sender = "Hermes",
            text = HermesPetEngine.getDynamicGreeting(overview, songsCount, playlists.size)
        )
    }

    val messages = remember {
        mutableStateListOf(initialGreeting)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VoidBlack,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            CybersigilBackground(showHalo = false)

            // Celestial astrolabe & neoclassical Hermes watermark filling the screen void at 7% opacity
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.placeholder_statue),
                    contentDescription = null,
                    modifier = Modifier
                        .size(260.dp)
                        .alpha(0.07f),
                    contentScale = ContentScale.Fit
                )
                HermesAstrolabeOracleEye(
                    modifier = Modifier
                        .size(240.dp)
                        .alpha(0.08f),
                    size = 240.dp
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HermesAstrolabeOracleEye(
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = theme.lexicon.petName,
                                color = theme.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = theme.lexicon.petSubtitle,
                                color = theme.primaryAccent,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = SigilSilver
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Suggestion Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(label = "🔥 Ofensiva?") {
                        messages.add(HermesPetEngine.HermesMessage("User", "Como está minha ofensiva?"))
                        messages.add(HermesPetEngine.respondToUserQuery("ofensiva", overview, songsCount, playlists))
                    }
                    SuggestionChip(label = "⚡ Organizar Pastas") {
                        messages.add(HermesPetEngine.HermesMessage("User", "Organizar minhas pastas"))
                        messages.add(HermesPetEngine.respondToUserQuery("organizar pastas", overview, songsCount, playlists))
                    }
                    SuggestionChip(label = "🎧 O que ouvir?") {
                        messages.add(HermesPetEngine.HermesMessage("User", "O que devo ouvir agora?"))
                        messages.add(HermesPetEngine.respondToUserQuery("o que ouvir", overview, songsCount, playlists))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Message Stream
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(
                            message = msg,
                            onTriggerAction = { action ->
                                when (action) {
                                    HermesPetEngine.HermesActionType.AUTO_ORGANIZE_FOLDERS -> onAutoOrganize()
                                    HermesPetEngine.HermesActionType.PLAY_FAVORITES -> onPlayFavorites()
                                    else -> {}
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sleek terminal input bar with integrated theme-accented send button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CharcoalSurface)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    SpecularGlassRimTop,
                                    theme.primaryAccent.copy(alpha = 0.45f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ">_",
                        color = theme.primaryAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = SigilWhite,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(theme.primaryAccent),
                        decorationBox = { innerTextField ->
                            if (userInput.isEmpty()) {
                                Text(
                                    text = "Transmitir diretriz ao Hermes...",
                                    color = MutedStone.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            innerTextField()
                        }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                val text = userInput.trim()
                                userInput = ""
                                messages.add(HermesPetEngine.HermesMessage("User", text))
                                messages.add(HermesPetEngine.respondToUserQuery(text, overview, songsCount, playlists))
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (userInput.isNotBlank()) theme.primaryAccent else theme.primaryAccent.copy(alpha = 0.25f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if (userInput.isNotBlank()) VoidBlack else SigilWhite.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    val theme = LocalVaultTheme.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalSurface)
            .border(1.dp, theme.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = theme.secondaryAccent,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ChatBubble(
    message: HermesPetEngine.HermesMessage,
    onTriggerAction: (HermesPetEngine.HermesActionType) -> Unit
) {
    val theme = LocalVaultTheme.current
    val isUser = message.sender == "User"
    val bubbleBg = if (isUser) theme.primaryAccent.copy(alpha = 0.22f) else CharcoalSurface
    val bubbleBorder = if (isUser) theme.primaryAccent.copy(alpha = 0.6f) else WireframeBorder

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(14.dp))
                .background(bubbleBg)
                .border(1.dp, bubbleBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = if (isUser) "VOCÊ" else "HERMES",
                    color = if (isUser) theme.primaryAccent else CyberAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    color = SigilWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )

                if (message.actionType != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onTriggerAction(message.actionType) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberAmber,
                            contentColor = VoidBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (message.actionType) {
                                HermesPetEngine.HermesActionType.AUTO_ORGANIZE_FOLDERS -> "⚡ EXECUTAR ORGANIZAÇÃO DE PASTAS"
                                HermesPetEngine.HermesActionType.PLAY_FAVORITES -> "🎧 REPRODUZIR NO SHUFFLE"
                                else -> "VER DETALHES"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
