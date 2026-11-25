# HISTÓRICO DE ENGENHARIA // AETHER VAULT (CHANGELOG) 🏛️⚡

Todas as atualizações notáveis e marcos de engenharia do ecossistema **Aether Vault // Sovereign Offline Audio** são documentados neste arquivo cronológico.

O projeto segue estritamente o versionamento semântico (*SemVer*), sob liderança e orquestração executiva da **Hugh Development**:
* **Victor "Hugh" Ramos** — Fundador, CEO & Maestro de Produto
* **Atom** — Co-fundador & Co-CEO Técnico (Liderança de IA & Arquitetura de Sistemas)

---

## 📑 Índice de Versões

* [v2.0.0 — Fastex Enterprise Hardening & Sovereign Architecture v2.0](#v200--2026-11-25)
* [v1.9.0 — The Pantheon Theme Store & High-Speed Ingestion Edition](#v190--2026-11-23)
* [v1.8.0 — Direct YouTube Ingestion & Media Pipeline](#v180--2026-11-20)
* [v1.7.0 — The Sovereign Divine & Cybersigilism Overhaul](#v170--2026-11-04)
* [v1.6.0 — Sovereign Audio Cards & Real Waveform Scrubber](#v160--2026-10-31)
* [v1.5.0 — High-Fidelity Audio DSP & Offline Intelligence](#v150--2026-10-27)
* [v1.0.0 a v1.4.5 — Fundação, Motor de Áudio & Gamificação](#versões-anteriores-v100---v145)

---

## [v2.0.0] — (2026-11-25)
### 🏛️🛡️ Fastex Enterprise Hardening & Sovereign Architecture v2.0

A versão **2.0.0** representa o maior salto qualitativo, estrutural e de segurança da história do Aether Vault. Conduzida sob a auditoria rigorosa da **Fastex Enterprise**, esta entrega consolida a blindagem criptográfica total, resolução definitiva de bugs de engenharia e ciclo de vida, expansão de acessibilidade e novos pilares de design e UX.

#### 🔐 1. Blindagem Criptográfica & Segurança (OWASP MASVS)
* **Externalização de Credenciais:** Extração de senhas e dados sensíveis de assinatura do Gradle para `local.properties` e variáveis de ambiente seguras com fallbacks.
* **Blindagem de Repositório (.gitignore):** Inclusão estrita de regras para `*.keystore`, `*.jks`, ferramentas internas de agente e transcrições de logs com PII.
* **Regras de Extração e Backup Seguro:** Implementação de `data_extraction_rules.xml` e `backup_rules.xml` no AndroidManifest, bloqueando extrações não autorizadas de credenciais e histórico local via USB/adb backup.

#### ⚡ 2. Correções Críticas de Engenharia & Ciclo de Vida
* **Telemetria e Streak Preciso:** Correção do contador de reproduções (`songsPlayedCount`) no `DailyListeningDao`, impedindo que flushes periódicos de 5 segundos inflassem falsamente as métricas.
* **Internacionalização do Cache de Waveform:** Formatação forçada em `Locale.US` na serialização de amplitudes do `WaveformExtractor`, garantindo que o separador decimal seja sempre ponto e restaurando o cache de 42 barras em aparelhos configurados em `pt-BR`.
* **Reconexão Imediata em Cold Start:** Sincronização do estado da faixa ativa mesmo com a lista de reprodução vazia na inicialização, restaurando a exibição imediata do `MiniPlayerBar`.
* **Ciclo de Vida do DownloadService:** Invocação explícita de `stopSelf()` ao esvaziar a fila de downloads, eliminando vazamento de processo e consumo indevido de bateria.
* **Resiliência na Ingestão do YouTube & Scoped Storage:** Tratamento de erros de conexão, fallback seguro para SignatureCipher e gravação compatível com Scoped Storage no Android 11+ (TargetSdk 35) via MediaStore.
* **Otimização de Binder IPC:** Suavização dos passos de volume no `AudioFadeManager`, evitando engasgos de frames no Jetpack Compose.
* **Limpeza de Código Morto:** Remoção de typealiases órfãos (`HermesPetEngine.kt`, `MusicRepository.kt`) e métodos inativos mapeados na auditoria.

#### 🎨 3. Inovações de Design & Acessibilidade M3
* **Acessibilidade e Ergonomia:** Todos os controles interativos satisfazem a área mínima de toque de 48x48dp (`minimumInteractiveComponentSize`).
* **Contraste Aprimorado:** Elevação do contraste de texto secundário para `#94A3B8` (>4.8:1).
* **Semântica TalkBack:** Suporte a `progressBarRangeInfo` no `WaveformScrubber` para leitores de tela.
* **Preservação Soberana:** Manutenção integral dos 8 componentes lendários (gestos físicos do MiniPlayer, wallpapers procedurais em Canvas por GPU, letras LRC sincronizadas, glassmorphism com iluminação specular top-rim, visualizador polimórfico, personas do Hermes, scrubber tátil e busto clássico).

---

## [v1.9.0] — (2026-11-23)
### 🏛️ The Pantheon Theme Store & High-Speed Ingestion Edition

A versão **1.9.0** consolida a soberania estética e a independência acústica do Aether Vault. Introduz a **Pantheon Theme Store** (Loja de Temas), customização manual profunda de layouts, tipografias e papéis de parede, aceleração massiva da ingestão de mídia do YouTube com telemetria viva (ETA, velocidade, porcentagem) e a extinção de crashes em segundo plano.

#### 🎨 1. Pantheon Theme Store & Identidades Visuais Pré-definidas
* **Catálogo de 6 Temas Pré-definidos:**
  1. **Aether Classic (Clássico Divino):** Vidro obsidiana fumê, mármore grego digital, Ciano Elétrico (`#00F0FF`) e Âmbar Criptográfico (`#F59E0B`).
  2. **Arasaka Pantheon (Corporativo Cyberpunk):** Mármore monolítico, laser vermelho sangue (`#FF003C`) e Ouro Imperial (`#D4AF37`) com retículas militares HUD.
  3. **Delamain AI Core (Executivo Monólito):** Azul Neon quântico (`#00E5FF`), platina escovada e interface executiva de concierge autônomo.
  4. **Neo Olympus Gold (Sacralidade Imperial):** Tons solares dourados (`#FFD700`) e labaredas de bronze (`#D97706`) inspirados no Monte Olimpo.
  5. **Neural Violet (Abismo Bioluminescente):** Violeta laser pulsante (`#B026FF`) e ciano místico sob o vácuo obsidiana.
  6. **Cyber Mint Matrix (Terminal Zero):** Estética de terminal hacker puro com verde fósforo (`#00FF66`) e esmeralda matrix (`#059669`).
* **Preview em Tempo Real na Loja:** Cada card exibe badges temáticas, paleta de cores em círculos vetoriais, descrição da alma do tema e barra de visualizer gradiente animada.
* **Pontos de Acesso Rápidos:** Integrado no menu de três pontos da `LibraryScreen` e via banner com iluminação de quartzo na `SettingsScreen`.

#### 🎛️ 2. Customização Manual do Usuário
* **Controle Fino de Papéis de Parede e Auras:** Sliders ópticos independentes de opacidade (5% a 70%) para a Biblioteca e o Player Expandido.
* **Transições Acústicas Senoidais (Crossfade):** Toggle e slider tátil ajustável de 0.5s até 15.0s com curva de volume contínua sem estalos.
* **Morfologia Geométrica do Layout:** Alternância entre Curvas Divinas Neoclássicas (16dp), Lâmina Arasaka Cortante (0-4dp) e Neo Minimalismo (8dp).
* **Tipografia Soberana:** Suporte a seleção dinâmica entre Monospace de Terminal, Serifa Neoclássica e Cyber Sans-Serif.

#### ⚡ 3. Aceleração & Telemetria em Tempo Real na Ingestão (Downloads)
* **Downloads em Alta Velocidade:** Otimização de streaming com buffers de 64KB, reaproveitamento de conexões HTTP e cabeçalhos anti-throttling para downloads na velocidade máxima da banda.
* **Nova Tela Dedicada de Downloads (`DownloadsScreen`):** Abas "EM ANDAMENTO" e "CONCLUÍDAS" com busca em tempo real e listagem detalhada.
* **Telemetria ao Vivo:** Cálculo instantâneo de taxa de transferência (`MB/s` ou `KB/s`), tempo estimado restante (`ETA`), porcentagem precisa (0% a 100%) e contador de bytes transferidos.
* **Menu de Compartilhamento Nativo (`ShareOverlayActivity`):** Permite compartilhar links de músicas diretamente do YouTube ou navegador para o Aether Vault com prévia de arte e metadados.

#### 🛡️ 4. Correções Críticas de Estabilidade e UX
* **Eliminação do Crash de Looper no Download:** Envolvimento de todos os disparos de `Toast` e notificações de erro no contexto `Dispatchers.Main`.
* **Proteção Android 14+ contra `ForegroundServiceStartNotAllowedException`:** Delegação de inicialização segura de download via escopo da aplicação.
* **Resolução de `ClassCastException`:** Implementação de `Context.findActivity(): Activity?` seguro para contextos com `ContextThemeWrapper`.
* **Harmonização Cromática e Fim do Carnaval:**
  * Substituição de cores fixas soltas por propriedades reativas do tema ativo (`LocalVaultTheme.current`).
  * Remoção do roxo aleatório no card de Nível e Telemetria — adoção da disciplina de Ciano Elétrico, Âmbar e Ouro Imperial.
  * Texto dos botões das Configurações (`AJUSTAR`, `GERENCIAR`, `MINUTOS`) travado em Branco Puro (`#FFFFFF`) com negrito de alto contraste.
  * Correção do cálculo de safe area no topo, evitando sobreposição com o notch/ilha dinâmica.

---

## [v1.8.0] — (2026-11-20)
### 📥 Direct YouTube Ingestion & Media Pipeline

A versão **1.8.0** introduziu o motor autônomo de download e conversão local, permitindo ao usuário expandir sua coleção musical sem depender de computadores externos ou serviços pagos.

* **Integração com NewPipe Extractor:** Extração offline de streams de áudio e vídeo de alta fidelidade sem anúncios ou dependências do Google Play Services.
* **Serviço de Background (`DownloadService`):** Processamento contínuo em segundo plano gerenciado por notificação em primeiro plano com ações de pausar e cancelar.
* **Tagger Local Automático:** Injeção de metadados ID3 (título, artista, álbum e thumbnail como capa) diretamente nos arquivos baixados.
* **Indexação Imediata no Android MediaStore:** Notificação instantânea ao indexador do sistema operacional para disponibilização das faixas baixadas na biblioteca do app sem reiniciar.

---

## [v1.7.0] — (2026-11-04)
### 🏛️ The Sovereign Divine & Cybersigilism Overhaul

A versão **1.7.0** estabeleceu o padrão visual definitivo da **Hugh Development**, rejeitando o minimalismo genérico e criando o **Aether Cybersigilism Design System (ACDS)**.

* **Geometria Neoclássica Sagrada:** Padronização universal em 12-20dp de curvatura áurea, erradicação total de cantos agudos involuntários e criação de cápsulas de quartzo fumê (`quartzGlassCapsule`).
* **Erradicação do Azul Klein Opaco (`#0022FF`):** Substituição de fundos e textos azul-escuros ilegíveis por Ciano Elétrico luminescente e Branco Diamante (`SigilWhite`).
* **Colunas Sagradas de Luz (Telemetria):** Gráfico de minutos dos últimos 7 dias redesenhado como colunas cibernéticas verticais com capitéis iluminados em neon.
* **Console de Áudio de Precisão:** Slider solar (`PrecisionSunSlider`) com orbe em gradiente e botões com borda vítrea especular (`SpecularGlassRimTop`).
* **Watermark de Hermes no Chat:** Preenchimento do vazio central do diálogo com a estátua clássica e o grande astrolábio celestial renderizados a 7% de opacidade no vácuo obsidiana.
* **Santuário de Músicas sem Ruído:** Remoção de caixas pesadas ao redor de cada faixa na lista, divisores sutis de 1px e substituição do botão destrutivo `✕` por menu contextual de 3 pontos.
* **Blindagem do Dock MiniPlayer:** Atmospheric Scrim com base obsidiana sólida `#F80B0D15`, impedindo sobreposição e ghosting com a lista rolando por trás.

---

## [v1.6.0] — (2026-10-31)
### 🎴 Sovereign Audio Cards & Real Waveform Scrubber

* **Sovereign Audio Cards (100% Offline):** Síntese nativa em hardware Canvas de cartões editoriais 1080x1920 PNG com telemetria, ofensiva, patente de Arconte e relíquias, salvando diretamente na galeria (`Pictures/AetherVault/`) com compartilhamento social.
* **Real Waveform Scrubber & Cache Room:** Amostragem direta por `RandomAccessFile` em 42 setores do arquivo de áudio para extração de energia RMS, persistida no SQLite via Room (`song_waveforms` — DB v7) com cache de memória duplo e scrubber tátil com tooltip `[ mm:ss // SEEK ]`.
* **Fila de Reprodução Estilo Spotify (`QueueBottomSheet`):** Visualização e reordenação em tempo real das próximas músicas da sessão.
* **Multi-Seleção de Faixas em Lote:** Seleção múltipla para inclusão em playlists, adição à fila, ocultação em massa ou exclusão do disco com confirmação biométrica/segura.
* **Santuário de Faixas Ocultas:** Sistema de blacklist local que oculta faixas indesejadas da biblioteca sem deletá-las do armazenamento físico.

---

## [v1.5.0] — (2026-10-27)
### 🎧 High-Fidelity Audio DSP & Offline Intelligence

* **Equalizador DSP Paramétrico de 5 Bandas:** Conexão nativa ao `android.media.audiofx.Equalizer` e `BassBoost` integrados ao motor Media3 ExoPlayer, com 5 presets acústicos salvos no Room.
* **Sincronização de Letras Locais (.LRC):** Parser de letras offline de alta precisão com rolagem suave automática acompanhando o tempo da música.
* **Gestos de Deslize (Swipe) no MiniPlayer:** Ações táteis de arrasto horizontal estilo Spotify para avançar ou retroceder faixas com animação elástica.
* **Introdução dos Temas Delamain e Arasaka:** Criação dos primeiros temas corporativos do ecossistema (*Delamain AI Core* e *Arasaka Pantheon*).
* **Hermes Voice Engine (TTS Offline):** Síntese vocal de saudações, ofensivas e promoções de nível usando a engine de fala local do dispositivo Android.

---

## Versões Anteriores (v1.0.0 - v1.4.5)

* **v1.4.5:** Keystore de release unificada permanente (`hugh_vault.keystore`), motor de backup/restore completo em JSON e primeiro Theme Engine.
* **v1.4.4:** Companheiro Soberano Hermes AI (diálogos offline, conselhos acústicos e personas).
* **v1.4.3:** Gamificação da escuta no estilo Duolingo — chamas de ofensiva diária, calendário mensal, gráfico de 7 dias, balanceamento de XP anti-sleeper e notificações locais.
* **v1.3.0:** Suporte completo a Playlists locais, pastas físicas e favoritos instantâneos.
* **v1.2.0:** Fade in e Fade out senoidal configurável para evitar cortes abruptos entre faixas.
* **v1.1.0:** Scanner nativo ultrarrápido do `MediaStore` do Android com detecção de arte embarcada e metadados ID3.
* **v1.0.0:** Fundação do projeto em Kotlin 2.0 nativo, Jetpack Compose, Material3 e Media3 ExoPlayer foreground service.

---

## 🏛️ A Filosofia Soberana da Hugh Development

> *"Não criamos software descartável. Construímos santuários digitais imersivos que respeitam a soberania do usuário, operam em hardware puro sem vigilância externa e unem o rigor da computação de alta performance à beleza eterna da geometria sagrada."*
> — **Victor "Hugh" Ramos & Atom**
