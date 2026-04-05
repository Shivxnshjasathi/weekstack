# 🌕 Hepta Productivity Suite

> **The 7-Day Intentionality Engine.** 
> *Hepta is a premium, glassmorphic productivity dashboard designed for high-performance individuals who focus on a weekly cadence of intentionality.*

---

## ✨ Core Features

### 🔔 Stellar Reminders
Never lose a day's purpose. Integration with **AlarmManager** brings core push notifications for your daily intentions, with custom-set precision.
*   **Dynamic Alarms**: Set reminders directly from your task sheet.
*   **Intention Focus**: High-priority notifications that keep you aligned.

### ⏱️ Zen Focus Timer
Tailor your deep work with our dynamic focus selectors. Choose your session depth:
*   **10 min** (Mini Sprint)
*   **25 min** (Classic Pomodoro)
*   **50 min** (Deep Focus)
*   **90 min** (Mastery Session)

### 📈 Weekly Stats (Zen Analytics)
Visualize your velocity with our theme-aware, alpha-graded metric nodes.
*   **Focus Depth Tracking**: Monitor your session completion.
*   **Intentionality Score**: Velocity mapping for your 7-day completion rate.

### 📅 Zero-Friction Calendar Sync
Bi-directional visibility. Your system calendar events are seamlessly integrated into your 7-day dashboard for a unified perspective.

### 🔐 Zen Vault
Keep your sensitive milestones and goals private. Implementation of **androidx.biometric** ensures your most critical data is secured by your fingerprint or face.

---

## 🛠️ Technical Architecture

Hepta is built using the latest Android standards for stability and performance:

- **Jetpack Compose**: 100% Declarative UI with a stunning glassmorphic design system.
- **Room Persistence**: Local-first data with v5 schema migration and `@Upsert` integration.
- **Hilt Dependency Injection**: Modularized architecture for absolute testability.
- **Core Library Desugaring**: Support for modern `java.time` APIs on API 24+.
- **Stable KSP Engine**: Hardened symbol processing via explicit mapping and standard Gradle caches.

---

## 🚀 Getting Started

1.  **Clone**: `git clone https://github.com/[USER]/weekstack.git`
2.  **SDK**: Minimum Android SDK 24.
3.  **Build**: `./gradlew assembleDebug` (KSP Incremental is disabled for absolute build stability).

---

## 🎨 Design Philosophy

High performance requires a calm mind. Hepta uses:
- **HSL-tailored colors**: Harmonious, non-fatiguing palettes.
- **Glassmorphic Overlays**: Subtle blurs for a depth-rich experience.
- **Micro-animations**: Theme-aware transitions for focus-aware UX.

---

> [!TIP]
> **Build Reliability Note:** 
> This project has been hardened with a custom `ksp.incremental=false` configuration to eliminate "Storage already registered" cache corruption. Always use `./gradlew clean` if making major schema changes.

**Hepta — Intention, Defined.**
