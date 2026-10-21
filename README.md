# Hugh Player // Offline Vault 🏛️⚡
> **Desenvolvido por Hugh Development**  
> *Co-fundadores: Hugh & Atom*  
> **Versão Atual:** 1.3.0 (Build 4)

---

## Visão Geral
O **Hugh Player** é um reprodutor de áudio local de alta performance para Android que combina a praticidade e fluidez de serviços de streaming (como o Spotify) com a soberania, privacidade e velocidade de um motor **100% offline**.

Ele foi construído sobre uma identidade visual exclusiva de **Cybersigilism Neoclássico** (inspirado na estética do *Hermes Agent / Nous Research*), trazendo pontilhado dither, bustos de mármore clássico, auréolas douradas em contraste brutalista, gamificação de ranks e transições cinematográficas nativas com **fade-in** e **fade-out** customizáveis.

---

## Principais Novidades da Versão 1.3.0 🚀

1. **Otimização Extrema de Scroll (Zero Lag):**
   - Implementado registro global de falhas em memória (`ArtworkCacheRegistry`) que impede re-tentativas desnecessárias de leitura de capas inexistentes.
   - Cache L1 em RAM (25% do heap) e L2 em disco (64 MB) configurados via `ImageLoaderFactory`.
   - Downsampling na decodificação de miniaturas para 120×120px, reduzindo consumo de memória em 90%.
2. **Fade In & Fade Out Customizável:**
   - Tela de configurações com ativação/desativação do fade.
   - Slider de duração ajustável de 1.0s até 15.0s (padrão otimizado de 8.0s para transições cinematográficas imperceptíveis).
3. **Boot Cinematográfico Garantido:**
   - Tela de Splash (`SplashScreenView`) sincronizada com o ciclo de vida de permissões e carregamento de áudio, com indicador pulsante e toque na tela para entrada imediata.
4. **Seletor de Ordem Dinâmico na Biblioteca:**
   - Filtros instantâneos: **A-Z**, **Recentes** (últimos adicionados no celular), **Maiores** (tamanho em MB do maior para o menor), **Mais Ouvidas** (ranking de plays do banco Room) e **Favoritas**.
5. **Gamificação do Vault (Ranks & Sigils):**
   - Sistema de XP acumulado por minutos ouvidos e faixas tocadas: *Neophyte*, *Acolyte of Sound*, *Hermetic Alchemist* e *Archon of the Core*.
   - Sigils desbloqueáveis: Overclock, Devotee, Loop e Purist.
6. **Ponte de IA USB (Agent Vault Bridge):**
   - Exportação de manifesto JSON estruturado para `/sdcard/Download/hugh_vault_ai_manifest.json`.
   - Suite de scripts em `tools/agent-bridge/agent_vault_bridge.py` para agentes de IA (Atom, Codex, Hermes) conectarem via USB/ADB e organizarem pastas e tags automaticamente.

---

## Pilares de Engenharia

### 1. Motor de Áudio & Fade In/Out Nativo
* **Stack:** `androidx.media3:media3-exoplayer` + `androidx.media3:media3-session`.
* **AudioFadeManager:** Curvas senoidais e logarítmicas de rampa de volume com amostragem contínua de até 100 passos, garantindo fade suave de 8s a 10s sem travamento.

### 2. Estética Cybersigilism (Hermes Agent Style)
* **Assets Otimizados:** 6 texturas raster artísticas integradas via GPU, com opacidade customizável na biblioteca e player expandido.
* **Audio Visualizer:** Barras animadas de espectro de áudio com gradiente CyberAmber e HermeticCyan no player expandido.

### 3. Dashboard de Métricas ("Offline Wrapped 2.0")
* **Room Database:** Rastreamento persistente de minutos ouvidos, plays totais, faixas únicas e favoritas com ranking proporcional.

---

## Compilação e Instalação

O projeto conta com automação completa de CI/CD via GitHub Actions. Qualquer push na branch `main` dispara o workflow `.github/workflows/build-apk.yml`, gerando o APK de depuração pronto para download nos artefatos da ação.
