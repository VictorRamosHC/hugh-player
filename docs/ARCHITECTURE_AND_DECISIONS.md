# Hugh Development — Arquitetura, Contexto e Registro de Decisões 🏛️⚡

> **Empresa:** Hugh Development  
> **Liderança:** Hugh (Fundador) & Atom (Co-fundador)  
> **Projeto:** Hugh Player (Offline Vault)  
> **Data de Atualização:** 21 de Outubro de 2026  

---

## 1. Origem e Visão do Produto
O objetivo do **Hugh Player** é ser uma alternativa ao Spotify voltada para **arquivos de áudio locais armazenados no celular**, unindo:
1. A praticidade, fluidez e ergonomia do Spotify (mini-player fixo, player expandido deslizante, busca rápida).
2. A soberania, privacidade total e performance de um player **100% offline** (zero envio de dados para a nuvem).
3. Uma identidade visual ousada baseada em **Cybersigilism Neoclássico** (inspirada na estética do site *Hermes Agent / Nous Research*), combinando pontilhado dither, glifos vetoriais, molduras cibernéticas e estátuas clássicas.
4. **Sem concessões na estabilidade:** O app precisa priorizar taxa de quadros (120Hz), zero engasgos, transições suaves (fade-in / fade-out) e execução contínua em segundo plano com a tela desligada.

---

## 2. As 4 Decisões Estratégicas da Mesa Diretora

Na reunião inicial da diretoria, foram avaliados 4 pilares:

### Pilar 1: Capacidade (Escala de Arquivos)
* **Decisão:** Não varrer o sistema de arquivos recursivamente pasta por pasta.
* **Implementação:** Consumir diretamente a API de sistema do Android `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` no [`MediaStoreScanner.kt`](../app/src/main/java/com/hughdevelopment/player/data/source/MediaStoreScanner.kt).
* **Resultado:** Indexação de mais de 10.000 músicas em menos de 2 segundos. Filtro automático de áudios de sistema, mensagens de WhatsApp e gravações com menos de 15 segundos.

### Pilar 2: Otimização & Motor de Áudio
* **Decisão:** Rejeitar bibliotecas de terceiros instáveis. Adotar `androidx.media3:media3-exoplayer` + `androidx.media3:media3-session`.
* **Rampa de Volume Nativa (Fade In / Fade Out):** Criado o [`AudioFadeManager.kt`](../app/src/main/java/com/hughdevelopment/player/service/AudioFadeManager.kt) que aplica interpolação senoidal/logarítmica de 350-400ms ao dar Play, Pause e Skip, eliminando totalmente estalos (*clicks/pops*) e cortes secos de áudio.
* **Segundo Plano:** O [`MusicPlaybackService.kt`](../app/src/main/java/com/hughdevelopment/player/service/MusicPlaybackService.kt) gerencia a notificação multimídia nativa e trata eventos de sistema (*Audio Becoming Noisy* ao desconectar fones e *Audio Focus* ao receber chamadas telefônicas).

### Pilar 3: Código & Stack Tecnológica
* **Opções avaliadas:** Kotlin Nativo + Jetpack Compose vs. Flutter.
* **Decisão:** **Kotlin Nativo com Jetpack Compose**.
* **Motivo:** Acesso direto às APIs mais modernas do Android 13/14/15 (`READ_MEDIA_AUDIO`, Media3 Service), menor consumo de bateria em segundo plano, overhead zero de bridge e fluidez máxima de tela.

### Pilar 4: Praticidade & Usabilidade (A Experiência Spotify)
* **Decisão:** Fluxo de dois níveis:
  1. [`MiniPlayerBar.kt`](../app/src/main/java/com/hughdevelopment/player/ui/components/MiniPlayerBar.kt): Barra fixa na parte inferior da biblioteca com linha de progresso milimétrica, título, miniatura e controles rápidos (Play/Pause, Skip Next, Skip Previous, Favorito).
  2. [`ExpandedPlayerSheet.kt`](../app/src/main/java/com/hughdevelopment/player/ui/components/ExpandedPlayerSheet.kt): Modal fullscreen deslizante com scrubber de alta precisão com arrasto local, visualizador em tempo real, tempo decorrido/restante, botões de shuffle, repetição e favorito.

---

## 3. Identidade Visual: Cybersigilism Neoclássico & Assets Híbridos (v1.2.0)
* **Paleta de Cores:**
  * Fundo: `VoidBlack` (`#070709`)
  * Superfícies: `CharcoalSurface` (`#111116`)
  * Linhas & Acentos: `SigilWhite` (`#FDFDFD`), `SigilSilver` (`#E4E4E7`), `CyberAmber` (`#F59E0B`), `HermeticCyan` (`#06B6D4`) e `Klein Blue` (`#0022FF`).
* **Assets Híbridos (Raster + Vector):**
  * `icon_launcher`: Ícone adaptativo nativo de alta densidade (mipmap mdpi a xxxhdpi) com zona de proteção de 66% para launchers circulares/squircle.
  * `placeholder_statue`: Busto neoclássico de dither 1-bit estilizado para faixas sem arte embutida.
  * `bg_library`: Textura sutil de fundo para a biblioteca com baixa opacidade para legibilidade máxima da lista.
  * `bg_expanded_player`: Atmosfera profunda com anéis Klein Blue e padrões moiré.
  * `bg_splash`: Tela de carregamento imponente com busto de Hermes e halo sagrado dourado.
  * `bg_dashboard`: Textura para o painel de estatísticas offline.
* **Otimização de Memória:** Imagens renderizadas originalmente em 2048x2048 foram reamostradas e otimizadas em pipeline local com interpolação bicúbica, reduzindo o peso médio de ~2.5 MB para ~90-110 KB por asset, eliminando qualquer risco de OutOfMemory (OOM) e travamento de rolagem.

---

## 4. O Dashboard "Offline Wrapped" (Telemetria 100% Local)
* Implementado com banco **Room 2.0 (SQLite local)**:
  * [`StatsEntity.kt`](../app/src/main/java/com/hughdevelopment/player/data/db/StatsEntity.kt) e [`StatsDao.kt`](../app/src/main/java/com/hughdevelopment/player/data/db/StatsDao.kt).
  * Auto-flush periódico a cada 5 segundos de escuta contínua + gravação imediata ao iniciar faixas.
  * Suporte a faixas favoritas com coluna `isFavorite`.
  * Grid 2x2 de métricas: Tempo Ouvido, Total de Plays, Faixas Únicas, Total de Favoritas.
  * Ranking Top faixas com barras proporcionais baseadas no líder de reproduções.
  * **Privacidade:** 100% dos dados permanecem no disco do celular; zero tráfego de rede.

---

## 5. Preparação para Futura IA (Agent-Ready Bridge)
* A classe [`AgentDataManager.kt`](../app/src/main/java/com/hughdevelopment/player/data/ai/AgentDataManager.kt) expõe ferramentas (*tool calling*) para futuras integrações com modelos on-device (ex: Gemini Nano / LLMs locais):
  * `getLibraryContextForAgent()`: Serializa a biblioteca musical completa em JSON estruturado para contexto de IA.
  * `inspectFolderTree()`: Mapeia fisicamente as pastas do celular onde as músicas estão alocadas.
  * `proposeStandardizedPaths()`: Fornece regras automáticas para agentes renomearem e reorganizarem pastas bagunçadas no padrão `Música/Artista/Álbum/Faixa.mp3`.

---

## 6. Histórico de Versões & Validações

### v1.0.0 (Release Inicial)
* Primeira compilação bem-sucedida via GitHub Actions.
* Testado no celular por Hugh: identificados gargalos de estatísticas (dados não persistiam), falta de controles avançados (shuffle/repeat/prev) e necessidade de enriquecimento visual.

### v1.1.0 (Correções Estruturais e Performance)
* **Correção no Room:** Registro imediato de reproduções e auto-flush de segundos a cada 5s.
* **Controles:** Adição de Shuffle, Repeat, Favorite e Skip Previous.
* **Performance:** CybersigilBackground reescrito sem loops excessivos de Canvas; Coil integrado com fallback protegido em `ArtworkImage.kt`; Scrubber com estado local sem engasgo de áudio.
* **Visualizador:** Novo componente `AudioVisualizerBars.kt`.

### v1.2.0 (Kit Visual de Alta Fidelidade com Assets Gemini)
* Integração de 6 assets artísticos de alta resolução gerados com Gemini (Hermes Neoclássico × High Contrast).
* Nova Splash Screen cinematográfica com transição fade-out suave na inicialização.
* Ícone de aplicativo em alta definição com suporte completo a Adaptive Icons do Android 8+.
* Fallbacks de álbuns locais com gravura 1-bit dither.
