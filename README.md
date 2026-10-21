# Hugh Player // Offline Vault 🏛️⚡
> **Desenvolvido por Hugh Development**  
> *Co-fundadores: Hugh & Atom*  
> **Versão Atual:** 1.2.0 (Build 3)

---

## Visão Geral
O **Hugh Player** é um reprodutor de áudio local de alta performance para Android que combina a praticidade e fluidez de serviços de streaming (como o Spotify) com a soberania, privacidade e velocidade de um motor **100% offline**.

Ele foi construído sobre uma identidade visual exclusiva de **Cybersigilism Neoclássico** (inspirado na estética do *Hermes Agent / Nous Research*), trazendo pontilhado dither, bustos de mármore clássico, auréolas douradas em contraste brutalista e transições cinematográficas nativas com **fade-in** e **fade-out**.

---

## Principais Novidades da Versão 1.2.0 🚀

1. **Kit Visual Autoral com Assets Gemini:**
   - **Splash Screen Cinematográfica:** Apresentação com busto neoclássico de Hermes, auréola geométrica e fade-out suave ao inicializar a biblioteca.
   - **Ícone do App em Alta Definição:** Novo ícone adaptativo suportando launchers circulares e squircles em todas as densidades (mdpi a xxxhdpi).
   - **Wallpapers Atmosféricos:** Texturas escuras profundas com interferência moiré e Klein Blue atrás da biblioteca, player expandido e dashboard de estatísticas.
   - **Fallback Brutalista de Capas:** Músicas sem arte embutida exibem um busto neoclássico em 1-bit dither halftone.
2. **Otimização de Performance:**
   - Reamostragem inteligente de imagens para consumo mínimo de memória RAM (zero engasgos de scroll e zero lag no scrubber).
3. **Controles Completos:**
   - Shuffle, Repeat (One/All), Favoritos persistentes e Skip Previous no Mini Player e Player Expandido.
4. **Dashboard Offline Wrapped 2.0:**
   - Métricas de tempo ouvido, contagem total de faixas, favoritos e ranking de reproduções persistidos via SQLite Room local.

---

## Pilares de Engenharia

### 1. Motor de Áudio & Fade In/Out Nativo
* **Stack:** `androidx.media3:media3-exoplayer` + `androidx.media3:media3-session`.
* **AudioFadeManager:** Curvas senoidais e logarítmicas de rampa de volume. Ao dar play, pause ou pular faixas, o volume varia suavemente em ~350-400ms, eliminando estalos (*clicks/pops*) e cortes secos.
* **Segundo Plano Contínuo:** `MediaSessionService` com notificação persistente do sistema e integração nativa com controles de fone bluetooth, tela de bloqueio e Android Auto.

### 2. Estética Cybersigilism (Hermes Agent Style)
* **Canvas Hardware-Accelerated + Raster Otimizado:** Pontilhados e linhas de mira cibernética desenhados via GPU combinados a texturas comprimidas de alta definição.
* **Audio Visualizer:** Barras animadas de espectro de áudio com gradiente CyberAmber e HermeticCyan no player expandido.

### 3. Dashboard de Métricas ("Offline Wrapped")
* **Room Database Integrado:** Rastreamento local da telemetria de áudio:
  * Minutos totais ouvidos.
  * Quantidade total de reproduções com auto-flush a cada 5 segundos.
  * Total de faixas únicas tocadas e contagem de favoritas.
  * Ranking Top faixas mais tocadas com barras proporcionais de repetição.

### 4. Preparado para Futura IA (Agent-Ready Bridge)
* A classe `com.hughdevelopment.player.data.ai.AgentDataManager` expõe esquemas estruturados para futuros agentes locais (ex: Gemini Nano):
  * `getLibraryContextForAgent()`: Exporta a biblioteca em JSON pronto para contexto de LLM.
  * `inspectFolderTree()`: Mapeia fisicamente as pastas no disco do celular.
  * `proposeStandardizedPaths()`: Estrutura ferramentas para organizar pastas musicais automaticamente.

---

## Compilação e Instalação

O projeto conta com automação completa de CI/CD via GitHub Actions. Qualquer push na branch `main` dispara o workflow `.github/workflows/build-apk.yml`, gerando o APK de depuração pronto para download nos artefatos da ação.
