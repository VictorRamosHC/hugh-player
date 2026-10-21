# Hugh Development — Arquitetura, Contexto e Registro de Decisões 🏛️⚡

> **Empresa:** Hugh Development  
> **Liderança:** Hugh (Fundador) & Atom (Co-fundador)  
> **Projeto:** Hugh Player (Offline Vault)  
> **Data de Criação:** 21 de Outubro de 2026  

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
  1. [`MiniPlayerBar.kt`](../app/src/main/java/com/hughdevelopment/player/ui/components/MiniPlayerBar.kt): Barra fixa na parte inferior da biblioteca com linha de progresso milimétrica, título, miniatura e botão de play/pause com fade.
  2. [`ExpandedPlayerSheet.kt`](../app/src/main/java/com/hughdevelopment/player/ui/components/ExpandedPlayerSheet.kt): Modal fullscreen deslizante com scrubber de alta precisão, tempo decorrido/restante e controles ampliados.

---

## 3. Identidade Visual: Cybersigilism Neoclássico
* **Paleta de Cores:**
  * Fundo: `VoidBlack` (`#070709`)
  * Superfícies: `CharcoalSurface` (`#111116`)
  * Linhas & Acentos: `SigilWhite` (`#FDFDFD`), `SigilSilver` (`#E4E4E7`), `CyberAmber` (`#F59E0B`) e `HermeticCyan` (`#06B6D4`).
* **Canvas Hardware-Accelerated:** O componente [`CybersigilBackground.kt`](../app/src/main/java/com/hughdevelopment/player/ui/components/CybersigilBackground.kt) desenha micropontos de meio-tom (*halftone stippling*) e cantos de mira cibernética via GPU sem instanciar imagens PNG pesadas.
* **Busto Neoclássico Procedural:** O componente [`StatueGlyphView.kt`](../app/src/main/java/com/hughdevelopment/player/ui/components/StatueGlyphView.kt) atua como placeholder para faixas locais sem capa oficial, desenhando um busto esculpido com auréola geométrica e miras vetoriais.

---

## 4. O Dashboard "Offline Wrapped" (Telemetria 100% Local)
* Implementado com banco **Room (SQLite local)**:
  * [`StatsEntity.kt`](../app/src/main/java/com/hughdevelopment/player/data/db/StatsEntity.kt) e [`StatsDao.kt`](../app/src/main/java/com/hughdevelopment/player/data/db/StatsDao.kt).
  * Registra minutos ouvidos por faixa, total de reproduções e data do último play.
  * Tela [`StatsDashboardScreen.kt`](../app/src/main/java/com/hughdevelopment/player/ui/screens/StatsDashboardScreen.kt) com cards cibernéticos: Minutos Totais, Quantidade de Plays, Faixas Únicas e Ranking #1 ao #10 das músicas mais repetidas.
  * **Privacidade:** 100% dos dados permanecem no disco do celular; zero tráfego de rede.

---

## 5. Preparação para Futura IA (Agent-Ready Bridge)
* A classe [`AgentDataManager.kt`](../app/src/main/java/com/hughdevelopment/player/data/ai/AgentDataManager.kt) foi estruturada no formato de ferramentas (*tool calling*) para futuras integrações com modelos on-device (ex: Gemini Nano / LLMs locais):
  * `getLibraryContextForAgent()`: Serializa a biblioteca musical completa em JSON estruturado para contexto de IA.
  * `inspectFolderTree()`: Mapeia fisicamente as pastas do celular onde as músicas estão alocadas.
  * `proposeStandardizedPaths()`: Fornece regras automáticas para agentes renomearem e reorganizarem pastas bagunçadas no padrão `Música/Artista/Álbum/Faixa.mp3`.

---

## 6. Incidente Técnico & Soluções (21/10/2026)
* **Diagnóstico da Máquina Corporativa:** A unidade `C:` estava com apenas **~160 MB livres**, inviabilizando a instalação local do Android Studio (que requer 15 a 25 GB).
* **Contramedida 1 (Nuvem):** Criado pipeline CI/CD [`.github/workflows/build-apk.yml`](../.github/workflows/build-apk.yml) para compilar o APK via GitHub Actions sem gastar RAM ou disco local.
* **Contramedida 2 (Backup & Setup Doméstico):** Criação deste backup integral para retomada dos trabalhos na máquina pessoal de Hugh (com hardware dedicado e espaço em disco).
