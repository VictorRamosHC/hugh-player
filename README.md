# Hugh Player // Offline Vault 🏛️⚡
> **Desenvolvido por Hugh Development**  
> *Co-fundadores: Hugh & Atom*

---

## Visão Geral
O **Hugh Player** é um reprodutor de áudio local de alta performance para Android que combina a praticidade e fluidez de serviços de streaming (como o Spotify) com a soberania, privacidade e velocidade de um motor **100% offline**.

Ele foi construído sobre uma identidade visual exclusiva de **Cybersigilism Neoclássico** (inspirado na estética do *Hermes Agent / Nous Research*), trazendo pontilhado dither, glifos vetoriais, estátuas clássicas e transições cinematográficas nativas com **fade-in** e **fade-out**.

---

## Pilares de Engenharia

### 1. Motor de Áudio & Fade In/Out Nativo
* **Stack:** `androidx.media3:media3-exoplayer` + `androidx.media3:media3-session`.
* **AudioFadeManager:** Curvas senoidais e logarítmicas de rampa de volume. Ao dar play, pause ou pular faixas, o volume varia suavemente em ~350-400ms, eliminando estalos (*clicks/pops*) e cortes secos.
* **Segundo Plano Contínuo:** `MediaSessionService` com notificação persistente do sistema e integração nativa com controles de fone bluetooth, tela de bloqueio e Android Auto.

### 2. Estética Cybersigilism (Hermes Agent Style)
* **Canvas Hardware-Accelerated:** O pontilhado halftone (dither dot matrix) e os cantos de mira cibernética são calculados e desenhados nativamente na GPU via Canvas Compose, preservando a taxa de 120Hz sem queima de memória.
* **StatueGlyphView:** Músicas sem arte de capa oficial recebem um busto neoclássico estilizado em linhas de giz prateado com auréola geométrica e coordenadas de mira, gerado em tempo de execução.

### 3. Dashboard de Métricas ("Offline Wrapped")
* **Room Database Integrado:** Rastreamento local da telemetria de áudio:
  * Minutos totais ouvidos.
  * Quantidade total de reproduções.
  * Total de faixas únicas tocadas.
  * Ranking Top 10 músicas mais tocadas com contadores de repetição e minutos consumidos.

### 4. Preparado para Futura IA (Agent-Ready Bridge)
* A classe `com.hughdevelopment.player.data.ai.AgentDataManager` já expõe métodos e esquemas estruturados:
  * `getLibraryContextForAgent()`: Exporta a biblioteca em JSON pronto para contexto de LLM.
  * `inspectFolderTree()`: Mapeia fisicamente as pastas no disco do celular.
  * `proposeStandardizedPaths()`: Estrutura ferramentas para um agente local (ex: Gemini Nano) renomear, mover e organizar pastas musicais automaticamente.

---

## Como Abrir e Gerar o APK

### Opção 1: Via Android Studio (Recomendado)
1. Abra o **Android Studio**.
2. Selecione **Open** e aponte para:  
   `C:\Users\victor.hugo\.gemini\antigravity\scratch\hugh-player`
3. Aguarde o Gradle sincronizar as dependências.
4. Conecte seu celular Android via USB (ou inicie um emulador).
5. Clique no botão verde de **Run (Shift + F10)** ou vá em `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

### Opção 2: Linha de Comando (Gradle)
```bash
cd C:\Users\victor.hugo\.gemini\antigravity\scratch\hugh-player
gradle assembleDebug
```
O APK final será gerado em:
`app/build/outputs/apk/debug/app-debug.apk`
