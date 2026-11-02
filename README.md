# CampusConnect

CampusConnect is an Android app project for building a better-connected campus community. It is an early-stage project: the current app launches into a simple Compose screen while the rest of the experience is being developed.

## Project status

The Android foundation is in place, including a Jetpack Compose UI, Material 3 theme, Hilt application setup, and Firebase Cloud Messaging notification service. The project also includes dependencies for navigation, local storage, networking, maps, and image loading. These libraries provide a starting point; their presence does not mean every integration or user-facing feature is implemented.

## Tech stack

- Kotlin and Gradle Kotlin DSL
- Android Gradle Plugin
- Jetpack Compose and Material 3
- Hilt for dependency injection
- AndroidX libraries, including Navigation, Room, DataStore, and WorkManager
- Ktor for networking and Kotlin serialization
- Firebase Cloud Messaging
- Google Maps SDK

## Requirements

- Android Studio with JDK 17
- Android SDK 36
- An Android device or emulator running Android 8.0 (API 26) or later

The project uses the Gradle wrapper, so a separate Gradle installation is not required.

## Getting started

1. Clone the repository:

   ```bash
   git clone https://github.com/isackkibet/CampusConnect..git
   cd CampusConnect.
   ```

2. Open the project in Android Studio and allow Gradle sync to finish.

3. If you have the required service credentials, configure them in your local `local.properties` file or as Gradle properties/environment variables:

   | Property | Used for |
   | --- | --- |
   | `MAPS_API_KEY` | Google Maps SDK |
   | `SUPABASE_URL` | Supabase project URL |
   | `SUPABASE_ANON_KEY` | Supabase client key |
   | `STREAM_API_KEY` | Stream integration |
   | `BACKEND_BASE_URL` | Application backend |
   | `ADMIN_BASE_URL` | Admin service; defaults to a placeholder URL |

   For Firebase services, add your own `app/google-services.json` if your Firebase project is configured. Do not commit credentials or service configuration files.

4. Build and install the debug app from Android Studio, or run the build from a terminal:

   ```powershell
   .\gradlew.bat assembleDebug
   ```

   On macOS or Linux, use `./gradlew assembleDebug`.

## Project layout

```text
.
├── app/                 Android application
│   └── src/main/        Kotlin source, manifest, and resources
├── gradle/              Version catalog and Gradle wrapper
├── admin/               Admin project area
├── docs/                Documentation area
└── supabase/            Supabase project area
```

The `admin/`, `docs/`, and `supabase/` directories are reserved project areas; their contents may grow as development continues.

## Contributing

CampusConnect is under active development. Open an issue to discuss a change, or submit a pull request with a focused description of what you changed and how you tested it. Never include API keys, passwords, signing keys, or other secrets in commits.
