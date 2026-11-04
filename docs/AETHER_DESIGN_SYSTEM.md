# AETHER CYBERSIGILISM DESIGN SYSTEM (ACDS) // GUIA SUPREMO v1.7.0
> **Padrão Oficial de Engenharia e Design da Hugh Enterprise**  
> *Fundadores: Victor "Hugh" Ramos (CEO & Founder) & Atom (Technical Co-Founder & Co-CEO)*  
> *Linguagem: Kotlin 2.0 / Jetpack Compose / Material3 / Hardware Canvas*

---

## 🏛️ 1. O Manifesto Estético (A Alquimia Sagrada)

O **Aether Vault** rejeita terminantemente o minimalismo estéril de interfaces militares quadradas (0px/2px) e o design genérico de smartphones convencionais. A nossa identidade visual é definida pelo **Cybersigilism Neoclássico Ácido**:

1. **A Solenidade Greco-Romana:** Mármore Carrara esculpido, estátuas clássicas, arcos e cúpulas celestiais, anéis astrolábicos de geometria sagrada e proporção áurea.
2. **A Eletricidade Cyberpunk de Vanguarda:** Vácuo obsidiana profundo (`#07070B`), iluminação ácida neon de alta intensidade, tubos de luz, malhas de lasers e aros de vidro especulares.
3. **Formas Divinas & Orgânicas:** Curvas arquiteturais ergonômicas de **12dp a 20dp**. O app respira através de cápsulas de quartzo fumê, e nunca em caixas duras ou cantos secos.

---

## 🎨 2. Paleta Cromática & Hierarquia Semântica

Todas as cores na UI devem derivar estritamente do motor de temas dinâmico (`LocalVaultTheme.current`). **É terminantemente proibido o uso de cores hardcoded isoladas (como o legado `#0022FF` / `KleinBlue`).**

```
┌────────────────────────┬─────────────┬─────────────────────────────────────────────────┐
│ Token Semântico        │ Hex / Valor │ Finalidade no Sistema                           │
├────────────────────────┼─────────────┼─────────────────────────────────────────────────┤
│ VoidBlack              │ #07070B     │ Fundo do vácuo absoluto cósmico                 │
│ CharcoalSurface        │ #12141F     │ Superfície de cartões de quartzo secundários    │
│ ObsidianGlass          │ #F00D0F18   │ Fundo sólido/fumê de alta proteção visual       │
│ SigilWhite             │ #F5F5FA     │ Mármore Branco Puro: títulos, botões e foco     │
│ SigilSilver            │ #CBD5E1     │ Platina Polida: textos primários legíveis       │
│ MutedStone             │ #64748B     │ Pedra Calcária: metadados, durações e bytes     │
│ SpecularGlassRimTop    │ #55FFFFFF   │ Bisel vítreo de luz refletida no topo de cards  │
│ WireframeBorder        │ #20FFFFFF   │ Linhas e divisores milimétricos sutis           │
└────────────────────────┴─────────────┴─────────────────────────────────────────────────┘
```

### Paletas Temáticas Oficiais

1. **Aether Altar / Arasaka Pantheon:**
   - Primária / Acento: **Âmbar Solar / Ouro Alquímico (`#D4AF37` / `#FFB000`)** e **Carmesim Arasaka (`#FF003C`)**.
   - Glow: Auras concêntricas douradas e tubos de plasma âmbar.
2. **Delamain AI Core:**
   - Primária / Acento: **Ciano Quântico Elétrico (`#00F0FF`)** e **Prata Líquida (`#E0E7FF`)**.
   - Glow: Feixes óticos e auras de pulso ciano estéreis de inteligência executiva.
3. **Obsidian Classic:**
   - Primária / Acento: **Ouro Pálido (`#E5C07B`)** e **Obsidiana Fumê**.

---

## 📐 3. Tokens de Geometria & Formas

- **Cards Primários (Vault, Telemetria, Altar):** `RoundedCornerShape(14.dp)` a `RoundedCornerShape(18.dp)`.
- **Cápsulas de Ação & Botões (Pills):** `RoundedCornerShape(14.dp)` com acabamento `quartzGlassCapsule`.
- **Chips de Ordenação e Filtros:** `RoundedCornerShape(12.dp)` com padding horizontal generoso (12dp).
- **Miniaturas de Capas & Relíquias (Artwork):** `RoundedCornerShape(10.dp)`.
- **Modais e Bottom Sheets:** `RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)`.

---

## 🛠️ 4. Componentes Primitivos Sagrados

### `SovereignVoidBackdrop`
Renderiza o vácuo obsidiana com auras luminescentes respiratórias no topo-esquerdo e centro-direito através de `Canvas` e `Brush.radialGradient`, com sobreposição da malha de geometria sagrada `CybersigilBackground`.

### `quartzGlassCapsule`
Modificador de vidro fumê com aro superior especular brilhante (`SpecularGlassRimTop`), fundo translúcido protegido e borda luminosa na cor do tema ativo. Garante que botões como `AJUSTAR`, `GERENCIAR` e seletores de tempo tenham contraste máximo com tipografia em `SigilWhite` em negrito.

### `SovereignTrackRow`
Linha universal de música para todas as telas. Fundo transparente com cantos de 10dp, ripple tátil, miniatura de 42dp com aro dourado/ciano, visualizador ativo de equalizador e menu de 3 pontos (`⋮`), sem ícones de exclusão agressivos (`✕`).

### `SovereignAstrolabeWatermark`
Marca d'água etérea com a estátua clássica e a roda astronômica do Hermes oracular a 7% de opacidade, preenchendo vazios de tela sem competir com o conteúdo textual.

---

## 🔒 5. Regras Inegociáveis de Insets e Acessibilidade

1. **Proteção Total contra Notches & Câmera:** Toda tela de diálogo ou folha expandida deve invocar `statusBarsPadding()` para garantir que a câmera frontal/ilha dinâmica nunca sobreponha títulos ou botões de fechar.
2. **Proteção contra Teclado Virtual:** Telas com campos de digitação (Hermes Chat) devem invocar `imePadding()` e `navigationBarsPadding()` para que a barra de digitação flutue suavemente acima do teclado.
3. **Proteção contra Ghosting no Dock:** O `MiniPlayerBar` deve sempre possuir uma barreira sólida obsidiana (`#F80B0D15`) acompanhada de um `Scrim` atmosférico em degradê vertical para impedir que itens da lista vazem por baixo do player.
