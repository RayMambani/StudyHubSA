## Release Notes

### Version 1.0.0 - Final PoE release (October 2026)

This release takes StudyHub SA from the Part 2 prototype to a complete, tested and store-ready app. The three innovative features from my design document are now built, the PoE-only requirements are in, the backend is hosted on persistent storage, and every push is built and tested automatically by GitHub Actions.

#### :star: Innovative features added in this version

| Feature | What it does | How it works |
|---|---|---|
| :trophy: **Gamification (points and badges)** | Students earn points and badges for contributing to the community. Points are shown on the Profile screen. | Points = 15 per resource uploaded + 10 per study group joined + 2 per chat message + 5 per streak day. Badges are calculated by the server from real activity, so they cannot be faked. |
| :fire: **Study Streak Tracker** | Rewards students for coming back every day. The Dashboard shows the streak with fire emojis. | The streak goes up once for each consecutive day a student logs in and resets after a missed day. One fire = 1-2 days, two fires = 3-6 days, three fires = 7 days or more. |
| :robot: **AI Study Assist** | Three tools in one screen: generate a study summary from pasted notes, ask any academic question, and get personalised study recommendations. | The server calls Google's Gemini API. It asks Google which models are currently available instead of hard-coding a name, and falls back to built-in logic if Gemini is unreachable, so the student always gets an answer. |

**Badges**

| Badge | How it is earned |
|---|---|
| Academic Contributor | Upload your first resource |
| Resource Specialist | Upload 5 resources |
| Best Collaborator | Join a study group and send at least 3 messages |
| Study Champion | Reach a 3-day login streak |

#### :white_check_mark: PoE requirements added

- **Single sign-on:** "Continue with Google". The app gets a Google ID token, and the server verifies it before creating or opening the account.
- **Offline mode with sync:** personal notes are written to a local Room (SQLite) database, so they work with no internet. A WorkManager job uploads anything unsynced as soon as the phone is back online. Notes can be opened, edited and deleted, and each shows a Synced or Pending sync status.
- **Notifications:** a live in-app notification feed (new resources, group messages, group joins, calendar reminders) with read and delete. The Firebase Cloud Messaging receiver and the server-side sender are implemented. Push delivery to the phone switches on once the Firebase service-account key is added to the server.
- **Multi-language support:** English, isiZulu and Tshivenda, switchable at runtime from Settings.

#### :sparkles: New features and screens since the prototype

- Academic calendar with real date and time pickers and test, assignment and personal event types. Events also appear under "Upcoming" on the Dashboard.
- Resource Library with a real document preview, downloads through Android's download manager, search, and per-resource comments.
- Study groups with seeded member counts, existing conversations and downloadable module documents. Chat shows each sender's real name.
- Profile picture upload, edit profile (name and student number), and real activity stats.
- Settings that all work: language, light, dark or system theme, public or private profile, change password, notifications on or off, and delete account.
- Dashboard cards for Modules, Resources and Study Groups open the matching screen. The greeting changes with the time of day. The login screen says "Welcome" for new users and "Welcome back" for returning ones.

#### :lock: Security and compliance

- Passwords are hashed with bcrypt. New passwords need 8 or more characters, an uppercase letter, a number and a symbol, checked both in the app and on the server.
- Email addresses are matched case-insensitively.
- The login token is stored in AES-256 EncryptedSharedPreferences. The app only talks to the server over HTTPS.
- Account deletion inside the app, plus a public deletion page, for Google Play policy.
- The temporary demo endpoint that exposed database rows was removed.
- Public home, privacy policy and terms pages are hosted on the API for the Google OAuth consent screen.

#### :building_construction: Reliability and infrastructure

- The database moved from a temporary file to **Turso**, so accounts and data survive restarts, sleeping and redeploys.
- Uploaded files and profile pictures moved to **Cloudinary**, so they survive restarts too.
- Starter documents are committed to the repository, so they are always available.
- Network timeouts were raised to cope with the free-tier server waking up.
- The server catches unexpected errors and returns a clear message instead of crashing.
- The app now targets **Android 16 (API 36)**, with edge-to-edge screen insets handled on every screen, as Google Play requires.

#### :wrench: Fixes

- Login failing with the correct password because of email capitalisation.
- Button text invisible on the Login and AI Study Assist screens.
- Raw JSON such as `{"error": "..."}` showing in error messages.
- Offline notes could not be opened after saving.
- The resource preview was blank.
- Chat showed internal IDs instead of names.
- AI Study Assist stopped answering when Google retired a model name.

#### :gear: Engineering and testing

- GitHub Actions now builds and tests the Android app and the backend on every push.
- Backend: 10 automated tests (Jest and Supertest). Android: 5 unit tests.
- Gradle wrapper added so the project builds on any machine, not only mine.
- Full documentation (this README).

#### :construction: Known issues in this release

- Push notifications are not delivered to the phone until the Firebase service-account key is added on the server.
- Group chat refreshes every 4 seconds rather than using a persistent connection.
- Uploads are limited to 10 MB, the Cloudinary free-plan limit.
- The first request after the free-tier server has been idle can take up to about 50 seconds.

### Prototype (Part 2) - baseline

Registration and login with hashed passwords, a settings screen, a REST API connected to a hosted database, and the first versions of the Part 1 screens.