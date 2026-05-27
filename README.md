Hotel Security - Spring Boot and React
======================================

This project is a fully runnable hotel security operations dashboard with:

- Spring Boot 3 backend
- React and Vite frontend
- HTTP Basic authentication with staff/admin roles
- File-based room and audit storage
- Room filtering, stats, status transitions, and admin audit history
- A production-style single jar that serves the built React app
- Docker and GitHub Actions deployment workflow

Redis and Kafka are not required for this application. The current workload is synchronous local room operations and audit logging, so adding message brokers or caches would make local deployment heavier without improving the core behavior.

Architecture
------------

Backend:

- Java 17
- Spring Boot Web, Security, and Validation
- In-memory demo users
- Room operations API under `/api`
- Public health/ping endpoints
- File-backed repositories using `rooms-db.txt` and `audit-log.txt`

Frontend:

- React 18
- Vite
- Calls backend endpoints through `/api`
- Supports staff room viewing and admin room management/audit workflows

Storage:

- `rooms-db.txt` in the project root
- `audit-log.txt` in the project root
- Runtime files are created automatically if needed.
- Room data is stored in a versioned, Base64-safe line format so notes and names can contain ordinary text safely.

For a complete explanation of every file, workflow, endpoint, and command, read `PROJECT_GUIDE.md`.

For the day-to-day run/build/deploy process, read `WORKFLOW_GUIDE.md`.

Prerequisites
-------------

- JDK 17 or newer
- Node.js 18 or newer
- npm

On this machine, Java 17 is available at:

```powershell
C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
```

The default `java` on the machine may be Java 8, so use the Java 17 executable directly or set `JAVA_HOME` before running Maven/Spring Boot.

Build and Run as a Single Deployed App
--------------------------------------

From the project root:

```powershell
cd frontend
npm install
npm run build
cd ..
.\mvnw.cmd package
& 'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe' -jar target\hotel-security-0.0.1-SNAPSHOT.jar
```

Open:

```text
http://localhost:8080/
```

The packaged Spring Boot app serves the React production build from the jar.

Docker Run
----------

```powershell
docker compose up --build
```

Open:

```text
http://localhost:8080/
```

GitHub Workflow
---------------

The GitHub Actions workflow at `.github/workflows/build-and-deploy.yml` builds the frontend, runs backend tests, packages the jar, builds a Docker image, and publishes the image to GitHub Container Registry on pushes to `main`.

Development Run
---------------

Backend:

```powershell
& 'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe' -version
.\mvnw.cmd spring-boot:run
```

Frontend, in another terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the Vite URL shown in the terminal, usually:

```text
http://localhost:5173/
```

The Vite dev server proxies `/api` and `/public` to `http://localhost:8080`.

Login Credentials
-----------------

Staff user:

```text
username: staff
password: staff123
```

Admin user:

```text
username: admin
password: admin123
```

Staff users can view rooms. Admin users can create, update, and delete rooms.

API Reference
-------------

Base URL:

```text
http://localhost:8080
```

Public:

- `GET /public/ping`
- `GET /api/public/ping`
- `GET /actuator/health`

Authenticated:

- `GET /api/rooms`
- `GET /api/rooms/{number}`
- `GET /api/rooms/stats`
- `GET /api/rooms?status=AVAILABLE&sort=price`

Admin only:

- `POST /api/admin/rooms`
- `PATCH /api/admin/rooms/{number}/status`
- `DELETE /api/admin/rooms/{number}`
- `GET /api/admin/audit`

Example request body for creating or updating a room:

```json
{
  "number": "305",
  "type": "Suite",
  "status": "AVAILABLE",
  "floor": 3,
  "pricePerNight": 2600,
  "occupantName": "",
  "notes": "Quiet side"
}
```

Quick Validation
----------------

Public ping:

```powershell
curl.exe -sS http://localhost:8080/public/ping
```

List rooms:

```powershell
curl.exe -sS -u staff:staff123 http://localhost:8080/api/rooms
```

Create or update a room:

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/rooms' -Method Post -Headers @{Authorization=('Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('admin:admin123')))} -ContentType 'application/json' -Body '{"number":"305","type":"Suite","status":"AVAILABLE","floor":3,"pricePerNight":2600,"occupantName":"","notes":"Quiet side"}'
```

Delete a room:

```powershell
curl.exe -sS -o NUL -w "%{http_code}" -u admin:admin123 -X DELETE http://localhost:8080/api/admin/rooms/305
```

Project Structure
-----------------

```text
.
|-- .mvn/wrapper/
|-- frontend/
|   |-- src/
|   |-- dist/
|   `-- package.json
|-- src/main/java/com/example/hotel/
|   |-- config/
|   |-- controller/
|   |-- model/
|   |-- repository/
|   `-- service/
|-- pom.xml
|-- mvnw.cmd
|-- rooms-db.txt
|-- audit-log.txt
|-- PROJECT_GUIDE.md
|-- WORKFLOW_GUIDE.md
`-- README.md
```

Notes
-----

- This is a demo-ready local deployment, not a hardened production system.
- Demo passwords use `{noop}` encoding and should be replaced with BCrypt before real use.
- CSRF is disabled to keep the SPA/API demo simple.
- File storage is intentionally small and local. Use a real database before exposing the app to production traffic.
