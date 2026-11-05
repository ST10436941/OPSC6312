# VetQueue API

A minimal Express + PostgreSQL REST API implementing every endpoint listed
in the VetQueue Part 1 document (Section 5, "Important API Endpoints") and
matching exactly what `app/src/main/java/com/vetqueue/app/data/remote/`
already expects on the Android side — once this is hosted, you only need
to change one line in the Android project (`API_BASE_URL`) to connect them.

## 1. What's here

```
vetqueue-api/
  src/
    index.js         Express app + route wiring
    db.js             Postgres connection pool
    mappers.js        snake_case DB rows -> camelCase JSON (matches the Kotlin DTOs)
    migrate.js         Applies sql/schema.sql + sql/seed.sql
    routes/            One file per resource (auth, pets, clinics, appointments, queues, notifications, reminders)
  sql/
    schema.sql          Table definitions (mirrors the Part 1 ERD)
    seed.sql            Demo clinics (matches the Android app's offline fallback data)
  package.json
  .env.example
```

Passwords are hashed **on the Android device** before they're ever sent
here (see `PasswordUtil.kt`) — this API only ever stores/compares an
opaque hash string, never a plain-text password.

## 2. Run it locally first (recommended before deploying)

You'll need Node 18+ and a local PostgreSQL install (or Docker).

```bash
cd vetqueue-api
npm install
cp .env.example .env
# edit .env if your local Postgres isn't on the default port/credentials

npm run migrate   # creates tables + seeds demo clinics
npm start          # starts the server on http://localhost:3000
```

Quick smoke test:
```bash
curl http://localhost:3000/health
curl http://localhost:3000/api/clinics
```

## 3. Deploying for free (Render.com example)

Render's free tier gives you both a web service and a Postgres database,
which is enough for this prototype.

1. **`vetqueue-api` lives inside the main project repository** — when creating
   the Render web service set **Root Directory** to `vetqueue-api`.
2. **Create the database first:** Render dashboard → New → PostgreSQL →
   pick the free plan → create. Once it's up, copy the **Internal
   Database URL** (or External, if connecting from your own machine to run
   the migration) shown on its page.
3. **Run the migration against that database** from your own machine:
   ```bash
   DATABASE_URL="<the External Database URL Render gave you>" DATABASE_SSL=true npm run migrate
   ```
4. **Create the web service:** Render dashboard → New → Web Service →
   connect the repo → set:
   - Build Command: `npm install`
   - Start Command: `npm start`
   - Add environment variables: `DATABASE_URL` (the **Internal** URL this
     time, since the web service and database live in the same Render
     network), `DATABASE_SSL=true`.
5. Deploy. Render gives you a public HTTPS URL like
   `https://vetqueue-api-xxxx.onrender.com` — that's your `API_BASE_URL`.

*(Railway, Fly.io, or a plain VM work the same way in spirit: provision
Postgres, run the migration once, deploy the Node app with `DATABASE_URL`
set as an environment variable.)*

## 4. Point the Android app at it

In the Android project, edit `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://vetqueue-api-xxxx.onrender.com/\"")
```
Keep the trailing slash. Rebuild the app. From here, every repository
(`AuthRepository`, `PetRepository`, `ClinicRepository`,
`AppointmentRepository`, `QueueRepository`) will start actually talking to
this API instead of silently falling back to local-only storage.

> Free-tier Render web services "sleep" after inactivity and take a few
> seconds to wake up on the first request — if your demo video shows a
> brief pause on the very first API call after opening the app, that's why.
> Hit the URL in a browser a minute before you start recording to warm it up.

## 5. Endpoint reference

| Endpoint | Method | Body / Query | Notes |
|---|---|---|---|
| `/api/auth/register` | POST | `{firstName, surname, email, passwordHash}` | 409 if email already exists |
| `/api/auth/login` | POST | `{email, passwordHash}` | 401 if hash doesn't match |
| `/api/pets` | GET | `?userId=` | |
| `/api/pets` | POST | `PetDto` | upsert by `petId` |
| `/api/pets/{id}` | PUT | partial `PetDto` fields | |
| `/api/pets/{id}` | DELETE | | |
| `/api/clinics` | GET | | queue counts are live, not static |
| `/api/appointments` | GET | `?userId=` | |
| `/api/appointments` | POST | `AppointmentDto` | **409 if the clinic/date/time slot is already booked** (DB-level unique index, not just a client-side check) |
| `/api/appointments/{id}` | PUT | partial fields | |
| `/api/appointments/{id}` | DELETE | | marks `cancelled`, doesn't hard-delete |
| `/api/queues/{clinicId}` | GET | | current waiting entries |
| `/api/queues/join` | POST | `{clinicId, userId, petId}` | position = clinic baseline + everyone already waiting via the app |
| `/api/queues/leave` | DELETE | `?queueId=` | |
| `/api/notifications` | GET | `?userId=` | |
| `/api/reminders` | GET | `?userId=` | |
| `/api/reminders` | POST | `ReminderDto` | |

## 6. Testing it without the Android app (for your demo video)

```bash
# Register
curl -X POST https://your-api-url/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Thabo","surname":"Molefe","email":"thabo@example.com","passwordHash":"demo-hash-value"}'

# List clinics
curl https://your-api-url/api/clinics

# Book an appointment (use the userId/petId returned/created above)
curl -X POST https://your-api-url/api/appointments \
  -H "Content-Type: application/json" \
  -d '{"appointmentId":"appt-1","userId":"<userId>","petId":"<petId>","clinicId":"clinic-1","service":"Check-up","date":"2026-08-25","time":"10:30"}'
```

For the "show what's stored in your hosted database" part of the demo
video, either run these curl commands and show the JSON responses, or open
your Postgres provider's dashboard (Render's Postgres page has a built-in
query console) and run `SELECT * FROM users;` / `SELECT * FROM
appointments;` etc.

## 7. Security notes for the write-up

- HTTPS only in production (Render/Railway/etc. provide this by default).
- Passwords are hashed client-side before this API ever sees them
  (Requirement 3.11).
- Double-booking is enforced at the database level via a unique index, not
  just trusted to client-side validation.
- CORS is currently wide open (`cors()` with no options) since this is a
  prototype talking only to a native Android client, not a browser app —
  tighten this with an explicit origin allowlist before using it for
  anything beyond coursework.


## 8. Part 3 additions

| Endpoint | Method | Notes |
|---|---|---|
| `/api/devices` | POST `{userId, token}` | Registers a phone's Firebase Cloud Messaging token |
| `/api/notifications/send` | POST `{userId, message, type?, title?}` | Stores a notification **and pushes it** — use it in the demo video |
| `/api/queues/{clinicId}` | GET | Positions are now **live**: baseline + rank among people still waiting |
| `/api/queues/leave` | DELETE `?queueId=` | Pushes a new position to everyone still waiting at that clinic |
| `/api/auth/register` | POST | Accepts the app's own `userId`; re-registering the same account is idempotent |

### Push notifications (PLACEHOLDER)
Set the environment variable **`FIREBASE_SERVICE_ACCOUNT_JSON`** on your host to the contents of a Firebase
service-account key (Firebase console → Project settings → Service accounts → *Generate new private key*; paste the
whole JSON on one line). Until it is set the server logs `push delivery disabled (placeholder)` and still stores the
notifications, so everything else keeps working.

After pulling these changes run `npm install` (adds `firebase-admin`) and `npm run migrate` (adds the `fcm_tokens` table).

### Demo: trigger a real-time push
```bash
curl -X POST https://YOUR-API/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"userId":"<id from the users table>","message":"Your turn is almost here!","type":"queue_update"}'
```
