# TEST_INFRA // Aether Vault v1.5.0 Sovereign Audio

**Test Track:** Requirements-Driven E2E & Component Test Infrastructure  
**Author:** E2E Testing Specialist (`teamwork_preview_test_writer_e2e`)  
**Scope:** v1.5.0 Sprint Completo de Alta Fidelidade (R1–R6, Features 1–18)  
**Date:** 2026-10-23  

---

## 1. Test Philosophy: Opaque-Box & Requirement-Driven

The testing architecture for Aether Vault v1.5.0 follows strict **opaque-box, requirement-driven verification**:
1. **Behavioral Focus Over Implementation Secrets**: Tests evaluate externally observable inputs and outputs, contract interfaces, and mathematical state transitions rather than private implementation details.
2. **Deterministic & Self-Contained Execution**: Every test sets up its own state fixtures, operates with zero inter-test side effects, and produces reproducible, deterministic results.
3. **Zero Facades / High-Integrity Assertion**: Tests strictly avoid trivial pass-throughs. Every assertion tests real domain boundaries, edge cases, special characters, and boundary conditions.
4. **Offline & Air-Gapped Verification**: All test logic executes completely offline without external network or cloud dependencies.

---

## 2. Feature Inventory: Complete 18-Feature Matrix (Tiers 1–4)

All 18 features from `PROJECT.md` and `ORIGINAL_REQUEST.md` are covered across Tiers 1 through 4:

| Feature ID | Feature Name | Requirement | Primary Milestone | Domain Scope & Contract |
| :--- | :--- | :--- | :--- | :--- |
| **F-01** | 5-Band Parametric Equalizer | R1 | M1 | `android.media.audiofx.Equalizer` band levels `[-1500, 1500]` mB, center frequencies (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz), pop-free adjustments |
| **F-02** | Native BassBoost Effect | R1 | M1 | `android.media.audiofx.BassBoost` strength range `[0, 1000]`, hardware enablement |
| **F-03** | Tactile Presets Persistence | R1 | M1 | Presets: Flat, Vocal Clarity, Sub-Bass Brutalist, Acoustic Worship, Podcast Enhancer, Custom; SharedPreferences persistence |
| **F-04** | Offline LRC Parser | R2 | M2 | Precision parsing of `[mm:ss.xx]`, `[mm:ss.xxx]`, `[offset:ms]`, multi-timestamps, chronological ordering |
| **F-05** | Multi-Tier LRC Discovery | R2 | M2 | 5-tier discovery: adjacent file, case-insensitive, title-based, `Lyrics/` subfolder, `Music/Lyrics/` |
| **F-06** | Real-Time Sync & Auto-Scroll | R2 | M2 | Millisecond timestamp matching to `playbackPositionMs`, active line calculation, tap-to-seek |
| **F-07** | Empty State Lyrics Glyph | R2 | M2 | Fallback to `StatueGlyphView` with message "Letras locais não encontradas" |
| **F-08** | MiniPlayer Horizontal Swipe | R3 | M3 | `pointerInput` drag gestures: left-swipe (`dx < -72dp`) -> skipNext, right-swipe (`dx > 72dp`) -> skipPrevious |
| **F-09** | MiniPlayer Fluid Animation | R3 | M3 | GPU translation, spring bounce snap back if `< 72dp`, click non-interference (open sheet & button clicks) |
| **F-10** | Delamain AI Core Theme Tokens | R4 | M4 | Neon Blue `#00E5FF`, Platinum `#E0E7FF`, Matte Graphite `#0A0B10`, Surface `#12141F`, 4dp corners, brutalist sharp |
| **F-11** | Corporate AI Executive Lexicon | R4 | M4 | "Delamain Concierge", "Rede de Transporte Sonoro", "Central de Comandos", executive tags |
| **F-12** | Dynamic Theme Selection | R4 | M4 | Instant hot-switching in Settings without Activity recreate, `LocalVaultTheme` update |
| **F-13** | Native Offline TTS Integration | R5 | M5 | `android.speech.tts.TextToSpeech` offline engine, zero network reliance, locale handling |
| **F-14** | Verbal Greetings & Celebrations | R5 | M5 | Sanitized speech quips for greetings, streak milestones (1, 3, 7+ days), and rank ascensions |
| **F-15** | Hermes Voice Settings Switch | R5 | M5 | `isHermesVoiceEnabled` toggle in `PlayerSettings.kt` and Settings UI switch card |
| **F-16** | Dead Code & Scan Pruning | R6 | M6 | Directory exclusions (`/.thumbnails/`, `/cache/`, `/temp/`, `/tmp/`, `/.trashed/`), remove duplicate manifest JSON |
| **F-17** | Version Bump to v1.5.0 | R6 | M6 | `versionCode = 11`, `versionName = "1.5.0"`, settings footer label update |
| **F-18** | Gradle Build & Keystore Signing| R6 | M6 | `:app:assembleDebug` with `hugh_vault.keystore`, V2 APK signature validation |

---

## 3. Test Architecture & Directory Layout

### 3.1 Test Runner & Toolchain
- **Runner**: Gradle JUnit 4 Test Runner via `testDebugUnitTest`
- **JDK Target**: JDK 17 (`C:\Users\victor.hugo\.jdks\jdk-17.0.20.1+1`)
- **Memory Flags**: `-Dorg.gradle.jvmargs="-Xmx1024m -XX:MaxMetaspaceSize=384m"`
- **Assertion Framework**: `org.junit.Assert` (`assertEquals`, `assertTrue`, `assertFalse`, `assertNull`, `assertNotNull`)

### 3.2 Directory Layout
```
app/src/test/java/com/hughdevelopment/player/
├── audio/
│   └── EqualizerPresetsTest.kt        # F-01, F-02, F-03 (Bands, ranges, clamp, presets)
├── lyrics/
│   └── LrcParserTest.kt               # F-04, F-05, F-06, F-07 (LRC parsing, timestamps, offsets)
├── ui/
│   ├── MiniPlayerSwipeLogicTest.kt    # F-08, F-09 (Swipe math, thresholds, drag direction)
│   └── PlaylistCoverResolverTest.kt   # Existing cover resolver unit tests
├── theme/
│   └── DelamainThemeTest.kt           # F-10, F-11, F-12 (Color tokens, 4dp radius, lexicon)
├── hermes/
│   └── HermesVoiceLogicTest.kt        # F-13, F-14, F-15 (Sanitization, phrases, toggle)
├── scanner/
│   └── MediaStoreScannerFilterTest.kt # F-16, F-17, F-18 (Noise filters, temp dir exclusions)
└── e2e/
    └── VaultCoreIntegrationTest.kt    # Tier 3 Pairwise & Tier 4 Real-World E2E Scenarios
```

---

## 4. Coverage Thresholds

| Test Tier | Target Description | Minimum Threshold Requirement |
| :--- | :--- | :--- |
| **Tier 1** | Core Unit & Functionality Tests | >= 5 test cases per feature (18 features x 5 = **>= 90 test cases**) |
| **Tier 2** | Contract & Boundary Tests | >= 5 test cases per feature (18 features x 5 = **>= 90 test cases**) |
| **Tier 3** | Pairwise Subsystem Interaction Tests | All pairwise combinatorial matrices across core modules (**>= 10 test cases**) |
| **Tier 4** | Real-World Application E2E Scenarios | Full composite workflows across entire stack (**>= 5 comprehensive scenarios**) |

---

## 5. Real-World Application Scenarios (Tier 4)

### Scenario 1: Sovereign Offline Playback & Dynamic Synced Lyrics
- **Flow**: User plays offline track with adjacent `.lrc` file.
- **Verification**:
  1. LRC parser reads multi-timestamp lines with millisecond precision and global `[offset:ms]`.
  2. Active lyric line updates synchronously with `playbackPositionMs`.
  3. Empty state renders cleanly when lyrics file is missing without audio playback disruption.

### Scenario 2: Studio Audio DSP Tuning & Preset Persistence
- **Flow**: User opens Equalizer, selects "Sub-Bass Brutalist", adjusts 3.6kHz presence slider, persists settings.
- **Verification**:
  1. Preset band levels match exact specifications (`+1400`, `+900`, `-200`, `-400`, `+200` mB).
  2. Hardware safety clamp restricts levels strictly to `[-1500, 1500]` mB.
  3. BassBoost strength configures to `900`.
  4. Moving individual band flags state as `CUSTOM` and persists to `PlayerSettings`.

### Scenario 3: Ergonomic MiniPlayer Queue Navigation
- **Flow**: User drags song info row on MiniPlayer bar.
- **Verification**:
  1. Dragging left past 72dp threshold triggers `skipNext`.
  2. Dragging right past 72dp threshold triggers `skipPrevious`.
  3. Dragging below 72dp threshold snaps back smoothly to center (`0f`) without skipping.
  4. Quick tap without drag does not trigger skip and opens `ExpandedPlayerSheet`.

### Scenario 4: Marketplace Delamain Corporate Executive Re-theming
- **Flow**: User selects "DELAMAIN AI CORE" in Theme Marketplace.
- **Verification**:
  1. Palette loads `#00E5FF` (neon blue), `#E0E7FF` (platinum), `#0A0B10` (graphite), `#12141F` (surface).
  2. Card corner radius is set to `4.dp`, `isBrutalistSharp` is `true`.
  3. Lexicon reflects Delamain executive nomenclature across HUD ("TRANSPORTE SONORO // ATIVO", "REDE DE DADOS", "DELAMAIN CONCIERGE").

### Scenario 5: Offline Gamification Streak & Rank Voice Celebration
- **Flow**: User reaches daily 5-minute listening milestone; streak advances and rank ascendance triggers.
- **Verification**:
  1. Text sanitization removes emojis (`🔥`, `⚡`, `🏛️`) and markdown formatting.
  2. Streak celebration quip reflects exact milestone bracket.
  3. Rank promotion generates executive announcement for Sovereign Rank tiers.
  4. Setting `isHermesVoiceEnabled = false` suppresses speech dispatch while keeping textual telemetry intact.

---
*End of Test Infrastructure Document*
