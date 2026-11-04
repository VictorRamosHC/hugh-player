# AETHER VAULT // CONTEXTO DE TRANSIÇÃO (WORK PC ➔ HOME PC)
> **Data de Geração:** 04/11/2026  
> **Versão Consolidada:** v1.7.0 (Build 9) — The Sovereign Divine Edition  
> **Branch Principal:** `main`  
> **Último Commit:** `1243258`  
> **Tag Oficial:** `v1.7.0`  
> **Repositório Remoto:** `https://github.com/VictorRamosHC/hugh-player.git`  
> **APK Assinado em Produção:** `C:\Users\victor.hugo\Downloads\aether-vault-v1.7.0.apk` (7.8 MB)

---

## 📌 PROMPT PRONTO PARA COLAR NO PC DE CASA

Copie e cole o bloco abaixo assim que abrir a sessão no Antigravity / Atom no PC de Casa:

```markdown
Salve, Atom! Sou o Victor Ramos (Hugh), Co-Founder e CEO do Aether Vault.
Estou no meu PC de Casa continuando a sessão iniciada no PC do trabalho.

Contexto do Projeto:
- Repositório: https://github.com/VictorRamosHC/hugh-player.git
- Branch: main (Commit 1243258 - v1.7.0 The Sovereign Divine & Acid Cybersigilism Edition)
- O projeto é um Player de Áudio Android Soberano e 100% Offline (Jetpack Compose, Kotlin 2.0, Media3 ExoPlayer, Room DB v7, Keystore permanente em app/keystore/hugh_vault.keystore).

O que foi consolidado e testado no PC do trabalho (v1.7.0):
1. Aether Cybersigilism Design System (ACDS):
   - Proibido app quadrado (rejeitados cantos 0px/militar). Todas as 12 telas padronizadas com curvas arquiteturais áureas de 12dp a 20dp, vácuo obsidiana (#07070B) e auras luminescentes.
   - Erradicação completa do azul KleinBlue (#0022FF) hardcoded. 100% dos botões, chips e acentos derivam do tema ativo com contraste em Branco Diamante (SigilWhite).
2. Hermes Oráculo IA:
   - Resolução total de insets (statusBarsPadding, navigationBarsPadding, imePadding) eliminando colisões com a ilha dinâmica / câmera e teclado virtual.
   - Preenchimento do vazio de 80% da tela com a marca d'água da Estátua Clássica + Astrolábio Celestial a 7% de opacidade.
3. Telemetria & Gráficos:
   - Gráfico semanal transformado em Colunas Sagradas de Luz Ácida com degradê vertical e capitéis luminosos.
   - Gerador de Cartões de Soberania (1080x1920 PNG offline) integrado via MediaStore.
4. Pastas & Configurações:
   - Corrigido bug UTF-8 (DURAÇÃO).
   - Linhas de faixas limpas sem o ícone destrutivo de "X", substituído por menu contextual de 3 pontos (⋮).
   - PrecisionSunSlider com feixe óptico e orbe solar com pulso luminescente.
   - Dock do MiniPlayer blindado contra vazamento de texto com Scrim atmosférico.

Por favor, faça um git pull (validando a árvore de arquivos e o commit 1243258), execute ./gradlew.bat testDebugUnitTest e reporte o status do ambiente para seguirmos com o roadmap!
```

---

## 🛠️ RESUMO TÉCNICO DAS ALTERAÇÕES NA v1.7.0

### 1. Arquitetura de Design & Geometria
- Criado o componente `SovereignVoidBackdrop.kt` unificando o fundo de vácuo obsidiana com auras dinâmicas em `LibraryScreen`, `FoldersScreen`, `SettingsScreen` e `StatsDashboardScreen`.
- Adicionado o modificador `quartzGlassCapsule` em `GlassmorphismModifiers.kt` para botões translúcidos com borda vítrea especular (`SpecularGlassRimTop`).
- Padronizados os raios de cantos em 14dp (cards), 12dp (chips e botões) e 10dp (artworks), eliminando os cantos de 4dp/6dp/0px.

### 2. Resolução de Contraste e Insets
- `SettingsScreen.kt`: Botões `AJUSTAR`, `GERENCIAR` e o seletor `[ MINUTOS ]` agora possuem texto em `SigilWhite` em negrito sobre vidro quartzo iluminado pelo tema ativo.
- `HermesPetCard.kt`: Adicionados `statusBarsPadding()`, `navigationBarsPadding()` e `imePadding()` na folha do Hermes para proteger contra notches de tela e teclados virtuais.
- Preenchimento central do chat do Hermes com a `placeholder_statue` e `HermesAstrolabeOracleEye` em baixa opacidade (7%).

### 3. Banco de Dados e Áudio
- `PlayerDatabase.kt`: Versão 7 consolidada (`song_waveforms`), permitindo extração e cache de 42 bandas de amplitude com busca no `WaveformScrubber.kt`.
- `EqualizerBottomSheet.kt`: 5 bandas DSP com presets táteis e sem conflito de cores.

---

## 💻 COMANDOS DE AMBIENTE PARA EXECUTAR NO PC DE CASA

```bash
# 1. Navegar até o repositório
cd C:\caminho\para\hugh-player

# 2. Atualizar com o repositório remoto
git fetch origin
git pull origin main
git status

# 3. Validar suíte de testes unitários (Adoptium JDK 17)
./gradlew.bat testDebugUnitTest

# 4. Gerar nova APK de Release assinada com a Keystore permanente
./gradlew.bat assembleRelease
```
