# IsleStudio — Precision Dynamic Island & Live Activity Engine for Android

IsleStudio brings Apple iOS and macOS Dynamic Island fluid physics, adaptive pill morphing, and real-time live activities to Android through both a system-wide floating overlay service and an interactive in-app studio, crafted with a disciplined, minimalist dark aesthetic free of visual clutter.

### User Review & Critical Decisions

> [!IMPORTANT]
> All core architectural and product choices below incorporate your confirmed preferences from our planning session. Review the specifications and select **Proceed** to begin implementation.

- **Confirmed Decision 1 — Execution Mode**: **System-Wide Overlay + Interactive In-App Studio**. Users can preview, drag, and test every island state directly inside the app's interactive canvas and toggle a real system-wide floating window (`TYPE_APPLICATION_OVERLAY` + `NotificationListenerService`) with guided permission onboarding.
- **Confirmed Decision 2 — Live Activity Modules**: **All Four Modules Included**:
  1. **Media Playback & Real-Time Waveform**: Album art, track scrub bar, playback controls, and a physics-driven multi-bar audio frequency visualizer.
  2. **Active Timers, Stopwatch & Progress Trackers**: Live countdowns, lap stopwatch, circular progress rings, and split-pill multitasking support (primary pill + detached secondary droplet).
  3. **Incoming Notifications & System Battery Alerts**: Real-time notification listener capture, charging/low-battery morphing animations, and quick action replies.
  4. **Navigation Directions & Live Sports / Delivery Tracker**: Turn-by-turn street maneuver cards, distance progress bar, and live score/delivery ETA pills.
- **Confirmed Decision 3 — Persistence & Security**: **Local Encrypted Database with Import/Export & Offline Sync Queue**. All custom presets, X/Y alignment profiles, and plugin configurations are persisted in a local Room SQLite database with AES-256-GCM payload encryption via Android Keystore, paired with an offline synchronization queue and JSON backup/restore.
- **Confirmed Decision 4 — Developer & Plugin Extensibility**: **Both Visual Action Builder & JSON Broadcast Intent API**. Users can build custom triggers/webhooks visually in-app, while third-party apps and automation tools (such as Tasker or macro scripts) can push live activities via structured JSON intents.

---

### 1. Overview & Core Concept

- **What It Does**: IsleStudio renders an adaptive, spring-animated capsule at the top of the screen that smoothly morphs between idle cutout, compact activity pill, split-droplet multitasking, and expanded interactive card states. It reacts to real device events (notifications, battery/charging state, active timers, media sessions) as well as custom plugin broadcasts.
- **Target Audience / Persona**: Android power users, customization enthusiasts, and developers who want Apple-caliber fluid motion, pixel-precise X/Y cutout alignment across any camera punch-hole configuration, and an extensible live-activity API.
- **Key Value**: Combines authentic damped-harmonic spring physics and sub-pixel X/Y calibration with an uncluttered, ultra-clean dark interface inspired by `unslop` design principles—prioritizing typographic hierarchy, purposeful contrast, and zero decorative bloat.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Interactive Live Studio & Preview Canvas**: On launch, the top viewport hosts a live, interactive Dynamic Island hovering above a simulated device status bar and notch grid. Users can tap to expand, long-press for haptic scale bounce, swipe to dismiss/compact, or trigger simultaneous activities to observe the split-droplet physics in real time.
  2. **Precision X/Y & Cutout Calibration**: Dedicated calibration controls let users adjust Horizontal Offset (`X` in `dp`/`px`), Vertical Offset (`Y` from status bar top), Pill Width, Pill Height, Corner Radius, and Notch Anchor Mode (Center, Left Punch-Hole, Right Punch-Hole) with a toggleable sub-pixel crosshair reticle and per-orientation/resolution profiles.
  3. **Physics & Theme Customizer**: Users fine-tune spring stiffness (`200f–1500f`), damping ratio (`0.45f–1.0f` for bouncy vs. critically damped motion), border glow opacity, surface tint, accent colors, and dark/light theme overrides, saving configurations as named presets.
  4. **Plugin Studio (Visual Builder + JSON API)**: Users create custom rule-based plugins (e.g., Battery threshold, Webhook poll, or App Notification filter -> Island Pill template) or test/copy the `com.example.islestudio.ACTION_PUSH_ACTIVITY` JSON broadcast schema with one-tap live injection.
  5. **Seamless Permission & System Overlay Center**: A clear status bar and guided permission sheet checks `SYSTEM_ALERT_WINDOW`, `POST_NOTIFICATIONS`, and `BIND_NOTIFICATION_LISTENER_SERVICE`, launching the exact system settings intents when the user activates the system-wide overlay.

- **Visual Identity & Theme (`unslop` Minimalist Discipline)**:
  - *Aesthetic Direction*: Ultra-clean monochrome studio inspired by Apple Pro hardware interfaces and `heydamir/unslop` design rigor—no neon cyberpunk gradients, no glowing rainbow cards, no emoji clutter, and disciplined 4dp/8dp spatial rhythm.
  - *Color Palette & Mood*:
    - `--background`: `#09090B` (True OLED Obsidian)
    - `--surface`: `#121215` (Elevated Studio Matte)
    - `--surface-elevated`: `#1C1C21` (Interactive Control Surface with `1dp` `#27272A` hairline border)
    - `--island-capsule`: `#000000` (Pure Pitch Black capsule so it blends seamlessly into physical camera cutouts, framed by a subtle `#222226` specular rim)
    - `--primary`: `#F4F4F6` (Crisp Alabaster for primary actions and high-contrast typography)
    - `--accent`: Customizable semantic accents per module (`#34C759` System Green for battery/calls, `#FF9F0A` Amber for timers, `#0A84FF` Precision Blue for navigation, `#FA2D48` Coral for live media waveform)
  - *Typography & Hierarchy*:
    - **Display & Headings**: `Plus Jakarta Sans` (bundled locally in `res/font/`) with tight tracking (`-0.02em`) for studio headers and expanded island titles.
    - **Telemetry, Coordinates & Timers**: `JetBrains Mono` (bundled locally in `res/font/`) with tabular numerals (`tnum`) so live countdowns, stopwatches, and `X/Y` coordinate readouts never jitter horizontally during rapid updates.
  - *Adaptive Layout*: Responsive across `Compact`, `Medium`, and `Expanded` window size classes. Automatically scales and switches from a bottom navigation bar on phones to a side navigation rail with a split studio preview + inspector layout on tablets and foldables.

- **Interactive Feedback & Motion**:
  - **Spring Morphing Engine**: Driven by Jetpack Compose `Animatable` and `updateTransition` using custom `spring(dampingRatio, stiffness)` specs for width, height, corner radius, X/Y offset, and content crossfade.
  - **Split-Droplet Metaball Effect**: When two high-priority activities run concurrently (e.g., Music + Active Timer), a secondary circular droplet detaches from the main capsule with elastic spring physics.
  - **Gesture Physics**: Horizontal swipe scrubs media or collapses an expanded card into its compact state; long-press triggers a tactile `1.04x` scale pulse before expanding.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual Rendering Engine (In-App Studio + Foreground System Overlay Service)**
  - *Chosen Approach*: Share a single `@Composable DynamicIslandHost` state machine between the in-app Studio Preview and a lifecycle-aware Android `Service` backed by `WindowManager` (`TYPE_APPLICATION_OVERLAY`).
  - *Why*: Ensures 100% visual and physics parity whether the user is calibrating inside the app or hovering the island over other apps, while allowing immediate zero-permission testing inside the app before granting system overlay access.
  - *Alternatives Considered*: Overlay-only service, which fails to render until the user navigates through multi-step Android special permission settings.

- **Decision 2: AES-256-GCM Encrypted Local Room Database with Offline Sync Queue**
  - *Chosen Approach*: Store presets, X/Y screen profiles, and plugin definitions in Room SQLite, encrypting sensitive plugin webhook headers and serialized configuration payloads using `javax.crypto.Cipher` (`AES/GCM/NoPadding`) backed by Android Keystore, plus an `OfflineSyncOperation` queue table and JSON export/import.
  - *Why*: Guarantees instant offline read/write performance, deterministic JVM/Robolectric testability, zero external cloud sign-in friction, and verifiable cryptographic security.
  - *Alternatives Considered*: Unencrypted SharedPreferences, which lacks relational querying, reactive `Flow` streams, and payload encryption.

- **Decision 3: Dual Plugin Architecture (Visual Builder + Broadcast Receiver SDK)**
  - *Chosen Approach*: Provide an in-app Visual Action Builder alongside an exported `PluginBroadcastReceiver` that parses validated JSON payloads into live `IslandActivityState` items.
  - *Why*: Empowers both everyday users (via UI templates and webhook triggers) and Android developers/automation tools (via standard `Intent` broadcasts) without violating Google Play Dynamic Code Loading (DCL) policies.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        UI & PRESENTATION LAYER                              │
│  ┌───────────────────────────────┐      ┌────────────────────────────────┐  │
│  │     In-App Studio Activity    │      │  System Island Overlay Service │  │
│  │  ┌─────────────────────────┐  │      │  (WindowManager + ComposeView) │  │
│  │  │  Interactive Top Stage  │  │      └───────────────┬────────────────┘  │
│  │  └────────────┬────────────┘  │                      │                   │
│  │  ┌────────────┴────────────┐  │                      ▼                   │
│  │  │  4-Tab Control Studio   │  │      ┌────────────────────────────────┐  │
│  │  │ • Modules & Live Stage  │  ├─────►│   Shared DynamicIslandHost     │  │
│  │  │ • X/Y & Physics Tuner   │  │      │ • Spring Morphing StateMachine │  │
│  │  │ • Plugin & Intent SDK   │  │      │ • Split-Droplet Secondary Pill │  │
│  │  │ • Presets, Sync & Perms │  │      │ • Gesture & Haptic Controller  │  │
│  │  └─────────────────────────┘  │      └───────────────┬────────────────┘  │
│  └───────────────┬───────────────┘                      │                   │
└──────────────────┼──────────────────────────────────────┼───────────────────┘
                   │            StateFlow / Intents       │
                   ▼                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      DOMAIN & ACTIVITY ENGINE LAYER                         │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                       IslandEngineViewModel                           │  │
│  │  • Priority Queue & Split-Pill Multiplexer (Primary + Droplet State)  │  │
│  │  • Live Module Controllers:                                           │  │
│  │    [Media + Waveform] [Timer + Stopwatch] [Notif + Battery] [Nav/ETA] │  │
│  │  • Adaptive Screen Profile Resolver (Compact / Medium / Expanded)     │  │
│  └───────────────┬──────────────────────────────────────┬────────────────┘  │
└──────────────────┼──────────────────────────────────────┼───────────────────┘
                   │                                      │
                   ▼                                      ▼
┌─────────────────────────────────────┐  ┌────────────────────────────────────┐
│     SYSTEM & PLUGIN INTEGRATIONS    │  │   ENCRYPTED DATA & SYNC LAYER      │
│  • NotificationListenerService      │  │  • Room Database (AppDatabase)     │
│  • Battery & Charging State Monitor │  │    - IslandPresetEntity            │
│  • PluginBroadcastReceiver (JSON)   │  │    - PluginRuleEntity (AES-GCM)    │
│  • Webhook / Custom Action Runner   │  │    - SyncQueueEntity (Offline Sync)│
└─────────────────────────────────────┘  │  • CryptoManager (AES-256-GCM)     │
                                         │  • JSON Backup Import / Export     │
                                         └────────────────────────────────────┘
```

- **Data Model & State**:
  - `IslandDisplayMode`: Sealed hierarchy (`IdleCutout`, `CompactPill`, `SplitMultitask`, `ExpandedCard`).
  - `IslandModuleState`: Sealed interface representing live telemetry for each of the 4 modules:
    - `MediaSessionState` (track title, artist, duration, elapsed, playing state, 12-band real-time waveform amplitudes, accent hex).
    - `TimerStopwatchState` (countdown remaining ms, total ms, stopwatch running/laps, pause/resume/reset handlers).
    - `NotificationBatteryState` (sender, message, app icon, battery percentage, charging wattage/state, quick-reply actions).
    - `NavigationLiveTrackerState` (maneuver icon, street name, distance remaining, ETA, or live sports/delivery progress stage).
  - `IslandConfiguration` & `IslandPresetEntity`: Stores `offsetXDp`, `offsetYDp`, `compactWidthDp`, `compactHeightDp`, `expandedWidthDp`, `expandedHeightDp`, `cornerRadiusDp`, `springStiffness`, `springDamping`, `accentColorHex`, `glowOpacity`, `anchorMode`, `autoScaleByResolution`, and `darkThemeMode`.
  - `PluginRuleEntity` & `SyncQueueEntity`: Stores custom plugin triggers, encrypted webhook/action JSON payloads, and pending/synced offline operations with SHA-256 integrity checksums.

- **Interactive Component & State Mapping**:
  - **Live Stage & Module Switcher**: Tapping any module chip (Media, Timer, Notification/Battery, Navigation/Live Tracker) activates or stacks the activity in the priority multiplexer; toggling two modules simultaneously triggers `SplitMultitask` mode with a primary pill and detached secondary droplet.
  - **X/Y & Physics Calibration Sliders**: Dragging `X Offset (-160dp to +160dp)`, `Y Offset (0dp to +80dp)`, `Width`, `Height`, `Stiffness`, or `Damping` immediately mutates the active `StateFlow<IslandConfiguration>` and renders a precision alignment crosshair on the preview stage.
  - **Plugin Builder & JSON Broadcast Tester**: Submitting the Visual Builder form or editing the raw JSON payload in the Developer SDK tab validates the schema, encrypts and saves the plugin in Room, and dispatches a live activity into the island queue.
  - **Encrypted Preset & Offline Sync Manager**: Saving, importing, or exporting a preset runs AES-256-GCM encryption/decryption through `CryptoManager`, logs a timestamped record in the Offline Sync Queue, and supports one-tap sync replay and resolution profile switching.
  - **Automated Verification Suite**: Comprehensive JUnit + Robolectric tests (`@Config(sdk = [36])` and multi-screen qualifiers) validate the spring configuration math, priority queue multiplexing, AES-GCM encryption round-trips, JSON plugin parser, and Compose UI interactions via `Modifier.testTag`.
