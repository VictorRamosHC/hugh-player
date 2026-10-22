# Hugh Player // Offline Vault 🏛️⚡
> **Desenvolvido por Hugh Development**  
> *Co-fundadores: Hugh & Atom*  
> **Versão Atual:** 1.4.1 (Build 5) — Produção Consolidada

---

## Visão Geral
O **Hugh Player** é um reprodutor de áudio local de alta performance para Android que combina a praticidade e fluidez de serviços de streaming (como o Spotify) com a soberania, privacidade e velocidade de um motor **100% offline**.

Construído sob a identidade visual exclusiva **Cybersigilism Neoclássico** (inspirado na estética do *Hermes Agent / Nous Research*), o aplicativo une pontilhado dither, bustos de mármore clássico, auréolas douradas em contraste brutalista, gamificação com Ranks de XP, sistema de pastas/playlists manuais e curadoria inteligente por IA via USB.

---

## Principais Recursos e Conquistas (v1.4.1) 🚀

1. **Correção Profunda de Controles de Áudio (Fade In/Out Responsivo):**
   - Eliminação total de travamentos ou congelamento da toolbar: toques em Pause, Play, Next e Previous respondem em **0ms** com rastreamento explícito de intenção (`isPlayIntended`).
   - Ao pular faixas com áudio pausado, o player **preserva o estado pausado** sem forçar reprodução indesejada.
   - Suporte a rampa suave de volume ajustável nas Configurações (de 1s a 15s, ou desligado).

2. **Favoritos com Atualização Atômica Instantânea:**
   - Implementado fluxo reativo de memória (`_favoriteOverrides`) que atualiza o coração dourado (`CyberAmber`) no **mesmo frame**, eliminando o delay da escrita assíncrona do SQLite.

3. **Sistema Completo de Pastas & Curadoria por IA:**
   - Nova tela de gerenciamento de pastas e playlists (`FoldersScreen`).
   - Criação manual de pastas com cores e nomes personalizados.
   - Inserção de músicas com 1 toque através do botão direto (`PlaylistAdd`) em cada linha da biblioteca e no player expandido.
   - Curadoria automática por IA testada e validada em 303 faixas reais do dispositivo:
     - *Músicas Cristãs (Playback)*: 113 faixas
     - *Músicas Cristãs (Cantado)*: 26 faixas
     - *Músicas Dia a Dia*: 153 faixas
     - *Áudios de Reuniões*: 11 gravações corporativas isoladas

4. **Desempenho e Fluidez de 120Hz:**
   - Cache L1 (RAM - 25% heap) e L2 (Disco - 64MB) via Coil.
   - `ArtworkCacheRegistry` que bloqueia re-tentativas desnecessárias de leitura de capas inexistentes.
   - Downsampling direto na decodificação para 120×120px.

5. **Gamificação do Vault (Ranks & Sigils):**
   - Sistema de XP acumulado por minutos ouvidos e faixas tocadas: *Neophyte*, *Acolyte of Sound*, *Hermetic Alchemist* e *Archon of the Core*.
   - Sigils desbloqueáveis: *Overclock*, *Devotee*, *Loop* e *Purist*.

6. **Ponte de IA USB (Agent Vault Bridge):**
   - Exportação de manifesto JSON para `/sdcard/Download/hugh_vault_ai_manifest.json`.
   - Script CLI (`tools/agent-bridge/agent_vault_bridge.py`) para agentes de desktop (Atom, Codex, ChatGPT) conectarem via USB/ADB e organizarem pastas e tags automaticamente.

---

## Pilares de Engenharia

### 1. Motor de Áudio & Media3
* **Stack:** `androidx.media3:media3-exoplayer` + `androidx.media3:media3-session`.
* `AudioFadeManager`: Interpolação senoidal e logarítmica com passos adaptativos de volume sem estalos (*clicks/pops*).
* Segundo plano persistente com notificação nativa, tela de bloqueio e controles Bluetooth.

### 2. Estética Cybersigilism (Hermes Agent Style)
* 6 texturas raster artísticas integradas via GPU, com opacidade customizável na biblioteca e player expandido.
* Visualizador espectral em tempo real (`AudioVisualizerBars`) e tela de splash imponente com busto de Hermes.

### 3. Banco de Dados Local (Room 2.0)
* Telemetria 100% privada e local: minutos ouvidos, contadores de reprodução com auto-flush a cada 5s, ranking de mais ouvidas e tabelas relacionais de playlists (`PlaylistEntity` e `PlaylistSongCrossRef`).

---

## Auditoria Externa Independente 🏛️
O projeto foi submetido a uma auditoria técnica cega pela **ChatGPT & Codex Company**:
* **Nota Oficial Recebida:** **86/100** ("Resultado Excepcional para Sprint de 1 Dia — Produto Funcional de Fundador")
* **Destaques Positivos:** Arquitetura de intenção explícita (`isPlayIntended`), UI otimista instantânea (`_favoriteOverrides`), identidade visual Neoclássica dither 1-bit, pipeline ágil de CI/CD em nuvem (1m30s) e soberania 100% offline.
* **Resiliência Preemptiva (Build v1.4.1 Hardened):** Antecipamos os apontamentos da auditoria implementando bounded `LruCache` (1000 itens) para capas, `WAL` (Write-Ahead Logging) no Room Database v4, índices B-Tree de busca, isolamento individual de exceções no scanner e painel de diagnóstico offline nas Configurações.
* **Parecer Completo da Diretoria:** Consulte [AUDITORIA_EXTERNA_CODEX_RESPOSTA.md](docs/AUDITORIA_EXTERNA_CODEX_RESPOSTA.md).

---

## Compilação e Instalação

O projeto conta com automação completa de CI/CD via GitHub Actions. Qualquer push na branch `main` dispara o workflow `.github/workflows/build-apk.yml`, gerando o APK de depuração pronto para download nos artefatos da ação.
