# PetCARE — City Veterinary Office, Santa Rosa, Laguna

Spring Boot back end with selectable MySQL or PostgreSQL storage and the PetCARE web front end.

## Status

| Phase | State |
|---|---|
| 1. Clean-up, Spring Boot skeleton | Done |
| 2. Database persistence + eligibility engine in Java | Done (MySQL and PostgreSQL supported) |
| 3. REST API, real sign-in, role protection | Done |
| 4. Public landing page + responsive workflows | Done |
| **5. Deployment, production email, HTTPS, backups, pilot** | **Deployment package ready** |

**What changed in Phase 5:** PetCARE now has a production profile with SMTP email codes, secure first-admin bootstrap,
public-auth rate limiting, security headers, health checks, and graceful shutdown. Docker Compose runs the application with
MySQL or PostgreSQL and Caddy automatic HTTPS. Backup/restore scripts, update instructions, incident procedures, and pilot acceptance
criteria are under `deployment/` and `docs/`. Infrastructure is ready; actual go-live still requires the CVO's domain,
server and SMTP credentials.

**CVO interview workflow update:** completed records now distinguish the person presenting the pet, the service provider's
role, source paper/event reference and service location. Staff can search completed records by pet, owner, household/address,
barangay, year, location or reference. Residents can print/save their vaccination history, nonresident accounts can apply for
adoption while City veterinary services remain residency-restricted, and generic staff are no longer automatically labeled DVM.

**What changed in Phase 4:** PetCARE opens on a public, mobile-first landing page that explains registration,
anti-rabies vaccination, microchipping, kapon, and the verification process. The signed-in resident, staff, and administrator
screens retain their maroon design but now adapt to phones and tablets with a navigation drawer, responsive cards, tables,
forms, and modals. Registration wording now distinguishes **registered / under verification / verified / needs correction**;
reports and named action history are administrator-only; and the QR screens explain that scanning identifies a pet but does
not by itself approve or complete a service. Phase 4 does not add hosting, a domain, production email/SMS, or deployment.

**What changed in Phase 3:** the website no longer keeps anything in the browser. Sign-in, every
registration, request, decision, adoption and admin change now goes through the server and is saved in the configured database.
Reload the page and you are still signed in; close the browser and everything is still there.
Accounts you create now really exist in the database.

## Run it

Same as Phase 2. JDK 17+, Maven, MySQL 8 running. **Your existing `petcare` database upgrades itself**:
Flyway applies the new `V4` migration on top of your Phase 2 data; nothing is lost and you do not recreate anything.

```powershell
cd C:\path\to\petcare           # the folder that contains pom.xml
$env:PETCARE_DB_URL = "jdbc:mysql://localhost:3307/petcare?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
$env:PETCARE_DB_USER = "root"
$env:PETCARE_DB_PASSWORD = "your-mysql-password"
mvn clean spring-boot:run
```
(Leave `PETCARE_DB_URL` out if MySQL is on the standard port 3306.) Open **http://localhost:8081**.

### Test real Gmail verification locally

PetCARE can send sign-up and password-reset codes through Gmail while retaining the development database. The Gmail
account must have Google 2-Step Verification enabled; use a Google **App Password**, never the account's normal password.

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev,email"
$env:PETCARE_SMTP_USER = "your-sender@gmail.com"
$env:PETCARE_SMTP_PASSWORD = "your-16-character-app-password"
$env:PETCARE_MAIL_FROM = "your-sender@gmail.com"
$env:PETCARE_EMAIL_NOTIFICATIONS_ENABLED = "true"
mvn clean spring-boot:run
```

The `email` profile uses `smtp.gmail.com:587` with STARTTLS, checks the connection during startup, and disables the
development code banner. Credentials are read only from environment variables and must not be committed to Git. After
startup, create a resident account using an inbox you can access; the six-digit code expires after 15 minutes and is usable
only once. For a City/CVO-managed sender, replace Gmail with the office's approved SMTP values.

### Run with PostgreSQL instead of MySQL

PostgreSQL is an alternative authoritative database for a PetCARE installation; it does not mirror the MySQL database.
Create an empty `petcare` database and user, then start the same application with the PostgreSQL profile:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev,postgresql"
$env:PETCARE_DB_URL = "jdbc:postgresql://localhost:5432/petcare"
$env:PETCARE_DB_USER = "petcare"
$env:PETCARE_DB_PASSWORD = "your-postgresql-password"
mvn clean spring-boot:run
```

Flyway creates the PostgreSQL schema and loads the same development accounts. MySQL remains unchanged and stays the
default when the `postgresql` profile is absent. For a complete PostgreSQL production stack, fill in `.env` and run:

```powershell
docker compose -f compose.postgresql.yaml up -d --build
```

Use `deployment/backup-postgresql.ps1` and `deployment/restore-postgresql.ps1` for PostgreSQL. The existing backup and
restore scripts continue to serve MySQL.

If start-up fails with a Flyway message about a *checksum*, you edited an old migration file: restore it. Applied migrations (`V1`–`V4`) must never change.

### Test accounts (dev profile; stored in the selected database as bcrypt hashes)

| Role | Email | Password |
|---|---|---|
| Resident | `resident@petcare.local` | `Resident#2026` |
| CVO Staff | `staff@petcare.local` | `Staff#2026` |
| Administrator | `admin@petcare.local` | `Admin#2026` |

Others (`ana.reyes@email.com`, `juan.delacruz@email.com`, `pedro.villanueva@email.com`, `miguel.alonzo@santarosalaguna.gov.ph`) use `Petcare#2026`.

### Things to try (this is the quickest way to see it is real)

1. Sign in as **Resident**, register a pet. Run `SELECT * FROM pet ORDER BY created_at DESC;` in MySQL Workbench or pgAdmin — it is there.
2. Sign out, sign in as **Staff**, verify the registration, request and assess a service. The classification comes from the Java engine.
3. Sign in as **Administrator**, *Eligibility Criteria*, switch a criterion off, then assess another request: the result changes. (In Phase 2 this switch did nothing.)
4. Sign up a **new resident**. The account can sign in immediately; with `dev,email` or `prod`, a code is delivered only to the submitted address to verify email ownership and enable secure password recovery.
5. As Staff, *Walk-in* → create a resident account: a random temporary password is shown **once**. That resident must change it on first sign-in.
6. Try to break it: as a resident, open the browser console and run `fetch("/api/admin/users/U-1002/status",{method:"POST"})` — the server answers 403/401. The browser is never trusted.

## Security (what the server enforces)

- **Passwords:** bcrypt only. Wrong email and wrong password give the same message. 5 wrong tries lock the account 15 minutes.
- **Session:** an HttpOnly, SameSite=Lax cookie (8 hours). The browser holds no token and no password data; nothing is kept in localStorage.
- **Forged requests:** every state-changing call must carry `X-Requested-With: PetCARE`, which other websites cannot add (no CORS). Together with SameSite this replaces token-based CSRF for this same-origin app.
- **Roles are checked on the server, every call, from the database** (not the session): a deactivated user is cut off on their next request. A resident only ever receives their own records; someone else's pet or notification looks exactly like one that does not exist (404). Staff-only and admin-only actions return 403.
- **Rules are enforced server-side**: only verified pets can request services; one open request per pet and service; approve needs an assessment; decisions cannot be repeated; adoption order (interview, then approve); microchip numbers are unique; "Verified at Barangay" can only be recorded by staff (a resident's claim is stored as pending).
- **One-time codes:** 6 digits, 15 minutes, 5 tries, single use, separate for sign-up and reset. "Forgot password" answers the same for unknown emails.
- **Temporary passwords:** random, shown once, must be changed before anything else works. (The old fixed `changeme123` is gone.)
- Every action writes a notification and an audit row with the real acting user.

### API (all JSON, under `/api`)

| | |
|---|---|
| Public | `POST auth/login · signup · verify · resend-code · forgot · reset/check · reset · logout` |
| Signed in | `GET bootstrap` (your data) · `GET auth/me` · `POST auth/change-password` · `POST notifications/{id}/read` |
| Resident + staff | `POST pets` · `GET pets/{id}/eligibility-preview?service=` · `POST service-requests` |
| Resident | `POST adoptions` |
| Staff | `POST registrations/{id}/verify · reject` · `service-requests/{id}/assess · decision · record` · `service-requests/quick-visit` · `adoptions/{id}/interview · approve · reject` · `residents` |
| Admin | `POST admin/users/{id}/status` · `PUT admin/criteria` |
| Dev profile, admin only | `GET dev/eligibility/{petId}` · (`GET dev/db-status` is open) |

Errors are `{"error":"…"}` with a normal HTTP status; no stack traces or SQL ever reach the browser.

## Profiles

| | `dev` (default) | `dev,email` | `prod` (`SPRING_PROFILES_ACTIVE=prod`) |
|---|---|---|---|
| Demo accounts and data | yes | yes | **no** (empty database apart from services and criteria) |
| One-time codes | not delivered or exposed; account remains usable | delivered by SMTP email | delivered by SMTP email |
| "Today" | fixed 17 Aug 2026 | fixed 17 Aug 2026 | real date, Asia/Manila |
| Session cookie | plain HTTP ok | plain HTTP ok | `Secure` (needs HTTPS) |

For a production deployment, follow [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md). It covers secrets, DNS, HTTPS, the first
administrator, backups, monitoring, updates, rollback and the staff pilot. Keep `deployment/.env` off source control.

For the thesis presentation's six-module completion matrix and the exact meaning of the 100% figure, see
[`docs/CORE-FUNCTIONALITY-VERIFICATION.md`](docs/CORE-FUNCTIONALITY-VERIFICATION.md).
For tomorrow's role-by-role demonstration order, expected answers and recovery steps, see
[`docs/PRESENTATION-DEMO-GUIDE.md`](docs/PRESENTATION-DEMO-GUIDE.md).

## How the front end changed

The screens are unchanged. Only the data layer moved: `services/dataStore.js`, `authService.js` and the new `apiClient.js`
fetch your data from `GET /api/bootstrap` (the server decides what your role may see) and every button that used to edit
local data now calls the API through one helper (`app/actions.js`) that prevents double-clicks, shows the server's message if
it refuses, reloads the data after success, and sends you back to sign-in if the session ends.
Also fixed: new pets now keep the rabies status the resident chose (it was being dropped); `todayISO()` no longer shows
the previous day in Philippine time; jsQR is served locally instead of from a CDN; the fake "Save" on System Settings and the
"Reset Local Data" button are gone. The seed data and the old JavaScript decision tree moved to `tools/reference/` (they are
only the reference the tests compare against).

## What was verified, and what was not

Verified in the build environment:

- **Java:** compiles with no warnings; **228 tests pass**: the decision tree against 144 cases from the original JavaScript, and the
  services run against in-memory repositories (permissions, state rules, adoption, admin, lockout, one-time codes, data scoping).
  Deliberately breaking 7 security rules (staff-only check, "own pets only", lockout, approve-without-assessment, self-verified
  residency, resident sees all pets, anyone can mark anyone's notification) each made tests fail.
- **Data contract:** the real `StateAssembler` was run over the seeded MySQL-family database for all 7 users, and that exact JSON
  was fed to the real front end in a headless browser: every screen renders cleanly for every user (0 script errors), nothing
  sensitive reaches the page or browser storage, and **61 checks** confirm each button sends the right call and handles
  refusal, double-click, expired session, sign-up/verify/forgot, and temporary-password flows.
- **SQL:** V1–V4 apply on a fresh database, on top of the Phase 2 state (your upgrade path), and in the prod profile; all 157 mapped
  columns match the schema (what Hibernate's `validate` checks). V1–V3 are byte-identical to what Phase 2 shipped.
- Seeded password hashes verify with the passwords above.

The production JAR builds successfully. The complete production stack and health endpoint still need to be exercised on the
chosen Linux host because Docker and MySQL are not running on this development laptop. Email delivery needs a real CVO SMTP account.

## Known limits (honest list)

1. The Digital Pet ID QR is an identifier, not a cryptographically signed credential; staff must verify the server record.
2. CVO-owned photos should replace the remaining Unsplash links before public launch.
3. The CVO must confirm eligibility thresholds and questionnaire wording during the pilot.
4. The City must approve the privacy notice, retention schedule, breach contacts and off-server backup location.
5. The built-in rate limiter is intended for one application instance. Multi-server deployment should use a shared gateway limiter.
6. Whole-state reload after actions is suitable for the pilot; measure it with production-sized data before wider rollout.

## Layout

```
pom.xml
Dockerfile, compose.yaml, compose.postgresql.yaml
deployment/     Caddy HTTPS config, environment template, backup and restore scripts
docs/           deployment guide and operations runbook
src/main/java/ph/gov/santarosa/petcare/
  security/     SecurityConfig, RequestedWithFilter, CurrentUser
  service/      AuthService, PetWorkflowService, RequestWorkflowService, AdoptionWorkflowService, AdminService,
                StateAssembler, Notifier, IdGenerator, Validation, Values, CodeSender (+dev / unconfigured)
  web/          controllers (thin), ApiExceptionHandler, DevStatusController (dev profile)
  eligibility/  EligibilityEngine (pure Java), EligibilityService
  domain/ repository/ config/
src/main/resources/
  application*.properties
  db/migration/   V1 schema, V2 reference data, V4 security columns      (V3 demo seed is in db/seed-demo/)
  static/         the website (index.html, src/…)
src/test/         71 service tests + 151 decision-tree tests (144 from the golden file), in-memory repository fake
tools/            generate-seed.js (+ reference/ = original seed data and JavaScript rules)
```

**Regenerating seed/golden data** (`cd tools && npm install && node generate-seed.js`): the golden test file is always rewritten;
migrations are never overwritten (Flyway checksums). Change the database with a new `V5__….sql`.
