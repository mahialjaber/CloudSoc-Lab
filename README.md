# 🛡️ CloudSOC Lab — Cloud Security & SOC Analyst Training Platform

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2024%2B%20(Target%2036)-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material Design 3](https://img.shields.io/badge/Material%203-Expressive%20Dark-7B1FA2.svg?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Room Database](https://img.shields.io/badge/Room-SQLite%20Persistence-00E676.svg?style=for-the-badge&logo=sqlite&logoColor=black)](https://developer.android.com/training/data-storage/room)
[![Gemini AI](https://img.shields.io/badge/Gemini%202.5-AI%20SOC%20Mentor-00E5FF.svg?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev)

> **CloudSOC Lab** is an interactive, production-grade Android application designed to bridge the gap between cybersecurity theory and job-ready cloud incident response practice. Analysts triage simulated real-world cyber incidents across AWS, Linux, Windows Sysmon, and container environments using authentic SIEM log forensics, MITRE ATT&CK® matrix correlation, and containment execution.

---

## 🌟 Key Features

### 🔍 1. Interactive 7-Step Incident Investigation Lab
Investigate realistic cloud intrusion scenarios with authentic multi-source telemetry:
1. **SIEM Log Explorer**: Search, filter, and inspect raw payloads across AWS CloudTrail, Sysmon Process Parentage, Linux `auditd`/`auth.log`, S3 Access Logs, and VPC Flow Logs.
2. **Evidence & Verdict Bookmarking**: Tag suspicious log artifacts, classify alerts (True Positive / False Positive / Benign), and maintain an analyst scratchpad.
3. **IOC Extractor & Dissector**: Extract and contextualize malicious IPv4/IPv6 addresses, rogue IAM role ARNs, C2 domains, and suspicious SHA256 hashes.
4. **Attack Timeline Reconstruction**: Assemble chronological kill-chain sequences from initial reconnaissance to data exfiltration.
5. **MITRE ATT&CK® Mapping**: Map adversary behavior against enterprise cloud tactics with custom animated Compose `Canvas` radial dials and progression rails.
6. **Surgical Containment Playbooks**: Execute remediation actions (revoking IAM sessions, isolating EC2 security groups, restoring bucket policies) while balancing blast radius impact.
7. **Executive Reporting & Scoring Engine**: Automated evaluation across triage speed, detection accuracy, and technical reasoning (Grades S through F).

---

### 🎨 2. Visual Ergonomics & SOC Dark Modes
Triage shifts can span 8–12 hours. CloudSOC Lab provides four specialized low-fatigue dark palettes:
- **Standard Cyber SOC**: High-contrast neon cyan and electric telemetry.
- **SOC Night Watch (Low Blue-Light)**: Calibrated phosphor emeralds and warm ambers designed to eliminate optical glare and high-energy blue wavelengths (415–455nm).
- **OLED Tactical Midnight**: Absolute pitch-black (`#000000`) canvas for true zero-pixel emission on AMOLED displays.
- **Warm Amber CRT Terminal**: Nostalgic, eye-safe amber CRT styling with zero blue-light emissions.

---

### 📊 3. Jetpack Compose Canvas Visualizations
- **Radial Dial Meters (`MitreTaskProgressIndicatorCanvas`)**: 36 calibration ticks, multi-arc sweep gradients, and animated progress beacons.
- **Kill-Chain Telemetry Rail (`MitreKillChainFlowCanvas`)**: Glowing node connectors visualizing attack stages from Initial Access to Exfiltration.
- **Matrix Mastery Donut (`MitreGlobalCoverageDonutCanvas`)**: Segmented circular chart displaying tactic coverage across the MITRE ATT&CK framework.

---

### 🧠 4. AI SOC Mentor ("Alex Vance")
- Powered by **Google Gemini API** (with an intelligent built-in Senior Lead Detection Engineer offline fallback engine).
- Delivers progressive 3-tier hints without spoiling answers, parses tricky JSON payloads, and offers post-incident debriefings.

---

### 📚 5. Tiered Learning Path & Competency Rubrics
- Guided path from **Tier 1 Alert Triage** up to **Lead Detection Engineer**.
- Interactive knowledge checks covering AWS CloudTrail forensics, Sysmon process parentage, Sigma/KQL detection engineering, and blast-radius mitigation.

---

## 🛠️ Tech Stack & Architecture

| Layer | Technologies |
|---|---|
| **Language** | Kotlin 2.0 (100% Coroutines & Flow) |
| **UI Framework** | Jetpack Compose (Material Design 3, Compose BOM) |
| **Graphics** | Custom `androidx.compose.foundation.Canvas` |
| **Architecture** | Clean Architecture / MVVM (Model-View-ViewModel) |
| **Local Persistence** | Android Room SQLite with KSP Code Generation |
| **AI Integration** | Google Gemini API (REST + Fallback Engine) |
| **Configuration** | Android Secrets Gradle Plugin (`.env` support) |
| **Testing** | JUnit 4, Robolectric (JVM Testing), Roborazzi (Screenshot Verification) |

---

## 📂 Project Structure

```text
com.example/
├── MainActivity.kt               # Single-activity root with adaptive top bar and nav rail
├── data/
│   ├── ai/
│   │   └── SocMentorService.kt    # Dual-mode Gemini API / Senior SOC mentor service
│   ├── local/
│   │   ├── AppDao.kt              # Room Database Data Access Object
│   │   ├── AppDatabase.kt         # Room database singleton instance
│   │   └── RoomEntities.kt        # Entity definitions & TypeConverters
│   ├── repository/
│   │   └── SocRepository.kt       # Unified single-source-of-truth repository
│   └── sample/
│       ├── SampleIncidents.kt     # Authentic AWS, Sysmon & Linux incident telemetry
│       └── SampleLearningAndMitre.kt # MITRE ATT&CK matrix & curriculum data
├── model/
│   └── IncidentModels.kt          # Domain models (Incident, LogEvent, IOC, Report)
├── ui/
│   ├── components/
│   │   ├── CommonComponents.kt   # CyberCard, CyberBadge, TerminalBox, MetricStatCard
│   │   └── MitreProgressCanvas.kt # Custom Canvas radial dial and kill-chain flow rail
│   ├── screens/
│   │   ├── DashboardScreen.kt     # Readiness metrics & incident queue
│   │   ├── IncidentLabScreen.kt   # 7-step investigation lab workflow
│   │   ├── LearningPathScreen.kt  # Tiered SOC curriculum & topic checks
│   │   ├── MitreAttackScreen.kt   # MITRE ATT&CK cloud matrix explorer
│   │   ├── InvestigationsScreen.kt# All scenario dossiers & filter tabs
│   │   ├── ReportsScreen.kt       # Saved Room database investigation reports
│   │   ├── ProgressScreen.kt      # Algorithmic readiness rubrics & skill stats
│   │   ├── AiMentorScreen.kt      # Interactive AI SOC mentor terminal
│   │   └── SettingsScreen.kt      # Dark mode toggles & lab reset tools
│   └── theme/
│       ├── Color.kt               # Semantic SOC color tokens & 4 dark palettes
│       ├── Theme.kt               # Custom SocTheme composition provider
│       └── Type.kt                # Monospace & technical cybersecurity typography
└── util/
    ├── ScoringEngine.kt           # Algorithmic grading & XP calculation engine
    └── SiemFilter.kt              # Multi-field SIEM log query parser
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1+) or newer
- **JDK 17+** (Java 17 or Java 21)
- **Android SDK API 36** (Minimum SDK 24 / Android 7.0+)

### Building the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/mahialjaber/CloudSoc-Lab.git
   cd CloudSoc-Lab
   ```

2. *(Optional)* Configure your Gemini API key for live AI Mentorship:
   Create a `.env` file in the project root:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   > *Note: If no API key is provided, CloudSOC Lab automatically operates in offline mode with its built-in Senior Detection Engineer rule engine.*

3. Build and install the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

4. Run unit and Robolectric tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 🧪 Testing & Code Quality

- **Robolectric Local Testing**: Fast JVM tests for core business logic and state management without requiring an emulator.
- **Roborazzi Screenshot Verification**: Visual regression testing for custom Compose Canvas components.
- **Compilation Verification**: Clean build with zero warnings across Room KSP generation and Kotlin 2.0 Compose compiler.

---

## 📄 License

This project is licensed under the **Apache License 2.0** — see the [LICENSE](LICENSE) file for details.
