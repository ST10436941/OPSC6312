# VetQueue 🐾
### Veterinary Queue and Appointment Management Application
**OPSC6312 — Part 2: App Prototype Development**

Omphile Kgope (ST10441474) · Luxolo Mqoqi (ST10436941) · Karabo Mashego (ST10442880)

> *"Less waiting. More wagging."*

---

## 1. Purpose of the App

Pet owners often have no visibility into how busy a veterinary clinic is, and
end up waiting for long, unpredictable periods with an anxious pet in tow.
**VetQueue** solves this by combining digital appointment booking with a
**live digital queue** — the innovative feature at the heart of the app —
so owners can see their position, pets ahead, and estimated wait time before
they even leave the house.

This repository contains the native Android (Kotlin) prototype built for
Part 2, implementing the design produced in Part 1 (see `/docs` for the
original Research, Planning and Design report).

## 2. Feature Checklist (Part 1 → Part 2 traceability)

| Part 1 Requirement | Status in this prototype |
|---|---|
| 3.1 Registration & Login | ✅ Implemented — passwords hashed with PBKDF2 + salt, never stored in plain text |
| 3.2 User Settings | ✅ Implemented — language selector, notification toggle, logout |
| 3.3 Pet Management | ✅ Implemented — add / view / delete pets |
| 3.4 Veterinary Clinic Search | ✅ Implemented — search + live queue/wait preview |
| 3.5 Appointment Booking | ✅ Implemented — double-booking prevention on clinic+date+time |
| 3.6 Digital Queue | ✅ Implemented — join/leave, live position + est. wait |
| 3.7 Real-Time Notifications | ⚙️ In-app notification inbox implemented; Firebase Cloud Messaging push wiring documented below (deferred to final PoE, needs your own `google-services.json`) |
| 3.8 Offline Mode & Sync | ✅ Implemented — Room-backed offline-first repositories, `syncStatus` pending/synced |
| 3.9 Pet Health Reminders | ⚙️ Data layer implemented (`ReminderRepository`); dedicated screen deferred to final PoE |
| 3.10 Gamification | ⚙️ Data layer implemented (`Achievement` / `UserAchievement` entities); UI deferred to final PoE |
| 3.11 Security | ✅ HTTPS-only Retrofit client, hashed passwords, per-user data scoping via `userId` |
| REST API connectivity | ✅ Retrofit `ApiService` matching the Part 1 endpoint table |
| GitHub + Actions | ✅ CI workflow builds the app and runs unit tests on every push |

Per the assignment brief, this is a **working prototype** — not every Part 1
feature needs to be complete for Part 2, and the items marked ⚙️ above are
explicitly deferred to the final Portfolio of Evidence.

## 3. Architecture

```
UI (Jetpack Compose)  →  ViewModel  →  Repository  →  ┬─ Room (local, offline-first)
                                                       └─ Retrofit (REST API, when reachable)
```

- **UI**: Jetpack Compose + Material 3, one screen per file under
  `ui/screens/...`, matching the wireframes in the Part 1 document
  (Splash, Welcome, Login, Register, Home, My Pets, Clinic Search, Clinic
  Details, Book Appointment, Live Queue, Appointments, Notifications,
  Profile & Settings).
- **Navigation**: `androidx.navigation.compose`, single `NavHost` in
  `ui/navigation/NavGraph.kt`.
- **ViewModels**: one per feature area, built via a small manual
  `VetQueueViewModelFactory` (no DI framework, to keep the project simple
  to open and run for markers).
- **Repositories** (`data/repository/`): implement an **offline-first**
  pattern — every write lands in Room immediately (tagged
  `syncStatus = "pending"`), then a REST call is attempted in the
  background. If the API is unreachable the app keeps working entirely
  from local storage and syncs later, satisfying Requirement 3.8.
- **Local storage**: Room (`data/local/`) — entities mirror the ERD from
  the Part 1 document (Figure 18).
- **Remote API**: Retrofit (`data/remote/`) — the `ApiService` interface
  maps 1:1 onto the "Important API Endpoints" table in the Part 1 document
  (Section 5).
- **Security**: `util/PasswordUtil.kt` hashes passwords with
  PBKDF2WithHmacSHA256 (10,000 iterations, random 16-byte salt per user)
  before they are stored locally or sent to the API. The raw password is
  never persisted or logged.

## 4. Getting Started

### Prerequisites
- Android Studio (Koala/2024.1 or newer)
- JDK 17
- An Android device or emulator running API 24+ (Android 7.0+)

### Steps
1. Clone this repository and open it in Android Studio. Android Studio
   will generate `gradlew` / `gradlew.bat` / `gradle-wrapper.jar`
   automatically on first sync (these are intentionally not committed as
   binary files — see `.gitignore`).
2. Point the app at your hosted REST API by editing
   `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"https://your-api.example.com/\"")
   ```
   Until you deploy your own backend, the app runs entirely offline against
   its local Room database — every screen still works, and pending writes
   are marked for sync once a real API is configured.
3. Build and run (`Shift+F10`).

### Adding Firebase Cloud Messaging (optional, for the final PoE)
The base build intentionally omits Firebase so it compiles without any
extra setup. To wire up push notifications:
1. Create a Firebase project and register the app (`com.vetqueue.app`),
   download `google-services.json` into `app/`.
2. Apply the Google Services plugin in the two `build.gradle.kts` files and
   add the `firebase-messaging-ktx` dependency (see commented note in
   `app/build.gradle.kts`).
3. Add a `FirebaseMessagingService` that writes incoming pushes into
   `NotificationRepository` so they appear in the Notifications screen.

## 5. Demo Video Checklist
When recording the required demonstration video, show in this order:
1. **Register** a new account → point out the password field note ("stored
   as a salted hash") → show the hashed value in the local database
   (Android Studio → App Inspection → Database Inspector → `users` table,
   `passwordHash` column) or in your hosted database if connected.
2. **Login** with the same account.
3. **Profile & Settings** — change language and toggle notifications.
4. **My Pets** — add a pet.
5. **Clinic Search → Clinic Details → Book Appointment** — show the booking
   confirmation, then attempt to book the exact same clinic/date/time again
   to demonstrate double-booking prevention.
6. **Clinic Details → Join Queue → Live Queue** — show position and
   estimated wait updating.
7. **Notifications** — show the queue-update notification that was created
   automatically when you joined the queue.
8. **Offline demo** — turn on Airplane Mode, add another pet or book another
   appointment, then show it still works and is stored locally
   (`syncStatus = pending` in the Database Inspector), reinforcing
   Requirement 3.8.
9. Show your hosted REST API / database (Postman call or hosting dashboard)
   with data that matches what's in the app.

## 6. Version Control with GitHub

This project follows a standard feature-branch workflow:
1. `main` is always kept buildable.
2. Work happens on short-lived branches (`feature/queue-screen`,
   `fix/booking-validation`, etc.) and is merged via pull request.
3. Commit early and often with descriptive messages, e.g.
   `feat: add PBKDF2 password hashing`, `fix: prevent double-booking on
   identical clinic/date/time`.

## 7. GitHub Actions (Continuous Integration)

`.github/workflows/android-build.yml` runs automatically on every push and
pull request to `main`:

1. Checks out the repository.
2. Sets up JDK 17 (`actions/setup-java`).
3. Sets up Gradle (`gradle/actions/setup-gradle`) — this does **not**
   depend on the binary `gradle-wrapper.jar` being committed, so the
   workflow keeps working even before you've opened the project in Android
   Studio locally.
4. Runs `gradle testDebugUnitTest` — our automated tests
   (`PasswordUtilTest`, `QueueRepositoryTest`) run headlessly via
   Robolectric/JUnit, no emulator required.
5. Runs `gradle assembleDebug` to confirm the app actually compiles into an
   installable APK.
6. Uploads the resulting debug APK and the test result XML files as
   workflow artifacts, downloadable from the Actions tab.

This means every commit is automatically verified to compile and pass its
tests — exactly the safety net a small team needs when multiple people are
pushing to the same repository. References used while setting this up:
- https://github.com/marketplace/actions/automated-build-android-app-with-github-action
- https://github.com/IMAD5112/Github-actions/blob/main/.github/workflows/build.yml

## 8. Automated Testing

| Test class | What it covers |
|---|---|
| `PasswordUtilTest` | Requirement 3.11 (Security) — verifies passwords never round-trip in plain text, that hashing is salted (two hashes of the same password differ), and that verification correctly accepts/rejects passwords without crashing on malformed input. |
| `QueueRepositoryTest` | Requirement 3.6 (Digital Queue) — verifies the estimated-wait-time calculation used by the Live Queue screen. |

Run locally with:
```
./gradlew testDebugUnitTest
```

## 9. Design Considerations

- **Offline-first over "online-only with error toasts"**: veterinary
  clinics are frequently in areas with patchy connectivity, so every
  feature that writes data (pets, appointments, reminders) is designed to
  succeed locally first and reconcile with the server later, rather than
  blocking the user.
- **No plain-text passwords, anywhere**: hashing happens on-device before
  the password is written to Room *or* sent over the network, so a
  compromised network capture or a leaked local database backup never
  exposes a usable password.
- **Manual DI over a framework**: for a student prototype of this size, a
  small hand-written `AppContainer` / `VetQueueViewModelFactory` is easier
  to read, debug and explain in a demo than introducing Hilt/Koin.
- **Material 3 + a single purple/white/neutral palette**, rounded cards and
  clear icons throughout, matching the Part 1 wireframes and icon design.

## 10. Known Limitations (Prototype Scope)

- Google Single Sign-On is present as a UI stub (button + explanatory
  text) — full OAuth wiring is deferred to the final PoE.
- Push notifications currently populate the in-app Notifications screen;
  Firebase Cloud Messaging wiring is documented above but not enabled by
  default (no `google-services.json` is committed).
- Pet health reminders and the gamification/achievements system have their
  data layer built but no dedicated screen yet.
- The REST API base URL is a placeholder
  (`https://vetqueue-api.example.com/`) until you deploy your own backend —
  until then the app runs fully offline against Room, which is by design
  for demoing Requirement 3.8.

## 11. References
- Android Developers. (n.d.). *Android Developers Documentation*. Google.
- Firebase. (n.d.). *Firebase Documentation*. Google.
- Google. (n.d.). *Google Identity Documentation*. Google.
- JetBrains. (n.d.). *Kotlin Documentation*. JetBrains.
- Square. (n.d.). *Retrofit Documentation*.
- Android Developers. (n.d.). *Room Persistence Library*. Google.
- GitHub Marketplace. (n.d.). *Automated Build Android App with GitHub
  Action*. https://github.com/marketplace/actions/automated-build-android-app-with-github-action
  [Accessed 03 November 2025].
- IMAD5112. (n.d.). *Github-actions build.yml*.
  https://github.com/IMAD5112/Github-actions/blob/main/.github/workflows/build.yml
  [Accessed 03 November 2025].
