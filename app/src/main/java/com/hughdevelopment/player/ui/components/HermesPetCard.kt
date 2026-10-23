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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.data.ai.HermesPetEngine
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Playlist
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.KleinBlue
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.ObsidianGlass
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.SpecularGlassRimTop
import com.hughdevelopment.player.ui.theme.VoidBlack
import com.hughdevelopment.player.ui.theme.WireframeBorder

@Composable
fun HermesPetBanner(
    overview: ListeningOverview,
    songsCount: Int,
    playlistsCount: Int,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quip = remember(overview, songsCount, playlistsCount) {
        HermesPetEngine.getDynamicGreeting(overview, songsCount, playlistsCount)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 10.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.dp,
                topRimColor = HermeticCyan.copy(alpha = 0.8f),
                bottomRimColor = SpecularGlassRimTop
            )
            .clickable { onOpenChat() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stylized Neoclassical Bust Glyph for Hermes
            StatueGlyphView(
                modifier = Modifier.size(46.dp),
                accentColor = HermeticCyan
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HERMES // COMPANHEIRO IA",
                        color = HermeticCyan,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "🏛️", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = quip,
                    color = SigilWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CharcoalSurface)
                    .border(1.dp, HermeticCyan.copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Conversar",
                    tint = CyberAmber,
                    modifier = Modifier.size(16.dp)
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
                .padding(16.dp)
        ) {
            CybersigilBackground(showHalo = false)

            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatueGlyphView(
                            modifier = Modifier.size(36.dp),
                            accentColor = HermeticCyan
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HERMES // DIALOGUE CORE",
                                color = SigilWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "IA SOBERANA 100% OFFLINE • ZERO CLOUD",
                                color = HermeticCyan,
                                fontSize = 10.sp,
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

                // Chat Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Fale com o Hermes...",
                                color = MutedStone,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HermeticCyan,
                            unfocusedBorderColor = WireframeBorder,
                            focusedTextColor = SigilWhite,
                            unfocusedTextColor = SigilSilver,
                            focusedContainerColor = CharcoalSurface,
                            unfocusedContainerColor = CharcoalSurface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

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
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KleinBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = SigilWhite,
                            modifier = Modifier.size(20.dp)
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CharcoalSurface)
            .border(1.dp, WireframeBorder, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = HermeticCyan,
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
    val isUser = message.sender == "User"
    val bubbleBg = if (isUser) KleinBlue.copy(alpha = 0.85f) else CharcoalSurface
    val bubbleBorder = if (isUser) KleinBlue else WireframeBorder

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(8.dp))
                .background(bubbleBg)
                .border(1.dp, bubbleBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = if (isUser) "VOCÊ" else "HERMES",
                    color = if (isUser) SigilWhite else CyberAmber,
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
                        shape = RoundedCornerShape(4.dp),
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
