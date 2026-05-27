# Hotel Security Project Guide

This file explains how the project works, how to run it, how the API is structured, and what each important command means.

## 1. What This Project Is

This is a Spring Boot and React hotel security operations dashboard. It lets staff users view room inventory, while admin users can manage rooms, change room states, and review audit history.

The application is designed to run locally without paid services or heavy infrastructure. Redis and Kafka are not required because the current system is synchronous CRUD with local file persistence. Adding them would make the project harder to run without solving a real problem in this scope.

## 2. Main Features

- Staff/admin authentication with Spring Security.
- Role-based authorization.
- Room inventory with advanced fields:
  - room number
  - room type
  - status
  - floor
  - nightly rate
  - guest/occupant
  - operational notes
  - last updated timestamp
- Room filtering by search text, status, floor, and sort order.
- Dashboard stats for total rooms, available rooms, occupied rooms, maintenance rooms, and projected nightly revenue.
- Admin room create/update/delete.
- Admin room status transitions.
- Append-only audit log for admin actions.
- React dashboard served by the same Spring Boot jar.
- Maven wrapper included, so a local Maven installation is not required.
- Dockerfile and Docker Compose deployment support.
- GitHub Actions workflow for jar build, tests, Docker build, and GitHub Container Registry publishing.

## 3. Project Structure

```text
hotel_security_java-main/
|-- src/main/java/com/example/hotel/
|   |-- config/
|   |   `-- SecurityConfig.java
|   |-- controller/
|   |   |-- HotelController.java
|   |   `-- PublicController.java
|   |-- model/
|   |   |-- AuditEvent.java
|   |   |-- Room.java
|   |   |-- RoomStats.java
|   |   |-- RoomStatus.java
|   |   `-- StatusChangeRequest.java
|   |-- repository/
|   |   |-- FileAuditRepository.java
|   |   `-- FileRoomRepository.java
|   |-- service/
|   |   `-- RoomService.java
|   `-- HotelApplication.java
|-- src/main/resources/
|   `-- application.properties
|-- src/test/java/com/example/hotel/
|   `-- HotelApplicationTests.java
|-- frontend/
|   |-- src/
|   |   |-- App.jsx
|   |   |-- main.jsx
|   |   `-- style.css
|   |-- dist/
|   |-- package.json
|   `-- vite.config.mts
|-- .mvn/wrapper/
|-- .github/workflows/
|   `-- build-and-deploy.yml
|-- Dockerfile
|-- docker-compose.yml
|-- mvnw.cmd
|-- pom.xml
|-- README.md
|-- CHANGES.md
|-- WORKFLOW_GUIDE.md
`-- PROJECT_GUIDE.md
```

## 4. Backend Flow

1. A browser or API client sends a request to Spring Boot.
2. `SecurityConfig.java` checks whether the route is public, staff/admin authenticated, or admin-only.
3. `HotelController.java` receives valid API requests.
4. `RoomService.java` applies business logic:
   - filtering
   - sorting
   - stats calculation
   - room status changes
   - audit event creation
5. `FileRoomRepository.java` reads and writes room records in `rooms-db.txt`.
6. `FileAuditRepository.java` appends and reads admin audit events in `audit-log.txt`.
7. JSON responses are returned to the React app or API client.

## 5. Frontend Flow

1. React starts from `frontend/src/main.jsx`.
2. `App.jsx` renders the operations dashboard.
3. The login area builds an HTTP Basic Auth header from username/password.
4. Staff users can load rooms and dashboard stats.
5. Admin users can additionally:
   - create rooms
   - edit rooms
   - change status
   - delete rooms
   - view audit events
6. `style.css` provides the responsive dashboard layout.

## 6. Authentication

The app uses demo users stored in `SecurityConfig.java`.

Staff:

```text
username: staff
password: staff123
role: STAFF
```

Admin:

```text
username: admin
password: admin123
role: ADMIN
```

Staff can view room data. Admin can manage room data and read audit history.

## 7. API Routes

Public routes:

```text
GET /public/ping
GET /api/public/ping
GET /actuator/health
```

Authenticated staff/admin routes:

```text
GET /api/rooms
GET /api/rooms?status=AVAILABLE
GET /api/rooms?floor=3
GET /api/rooms?q=deluxe
GET /api/rooms?sort=price
GET /api/rooms/stats
GET /api/rooms/{number}
```

Admin-only routes:

```text
POST /api/admin/rooms
PATCH /api/admin/rooms/{number}/status
DELETE /api/admin/rooms/{number}
GET /api/admin/audit
```

## 8. Room Status Values

The supported room statuses are defined in `RoomStatus.java`.

```text
AVAILABLE
RESERVED
OCCUPIED
CLEANING
MAINTENANCE
```

## 9. Local Data Files

The app uses local files instead of a database server.

```text
rooms-db.txt
audit-log.txt
```

`rooms-db.txt` stores the room inventory. If it does not exist, the backend creates seed rooms automatically.

`audit-log.txt` stores admin activity such as room creation, updates, status changes, and deletion.

Both files are ignored by Git because they are runtime data.

## 10. Required Software

- JDK 17 or newer
- Node.js 18 or newer
- npm

On this machine, Java 17 is installed here:

```powershell
C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
```

## 11. Build and Run Commands

Run these commands from:

```powershell
C:\Users\aniskuku\Downloads\leokb test\hotel_security_java-main
```

### Step 1: Build the React frontend

```powershell
cd frontend
npm install
npm run build
cd ..
```

Meaning:

- `cd frontend` changes the terminal into the frontend folder.
- `npm install` installs the JavaScript packages listed in `package.json`.
- `npm run build` runs Vite and creates the production frontend in `frontend/dist`.
- `cd ..` returns to the project root.

### Step 2: Run backend tests

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd test
```

Meaning:

- `$env:JAVA_HOME=...` tells Java tools which JDK to use.
- `$env:PATH=...` puts Java 17 first in the command search path.
- `.\mvnw.cmd` runs the Maven wrapper included in this project.
- `test` tells Maven to compile the app and run automated tests.

### Step 3: Package the deployable jar

```powershell
.\mvnw.cmd package
```

Meaning:

- `package` compiles Java code, runs tests, copies `frontend/dist` into Spring Boot static resources, and creates the final jar.
- The output jar is created in `target/`.

Final jar:

```text
target\hotel-security-0.0.1-SNAPSHOT.jar
```

### Step 4: Run the deployed jar

```powershell
& 'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe' -jar target\hotel-security-0.0.1-SNAPSHOT.jar
```

Meaning:

- `&` runs a quoted executable path in PowerShell.
- The quoted path points directly to Java 17.
- `-jar` tells Java to run an executable jar file.
- `target\hotel-security-0.0.1-SNAPSHOT.jar` is the packaged app.

Open the app:

```text
http://localhost:8080/
```

## 12. Validation Commands

### Public ping

```powershell
curl.exe -sS http://localhost:8080/public/ping
```

Meaning:

- `curl.exe` sends an HTTP request.
- `-sS` keeps output quiet unless an error happens.
- `/public/ping` checks whether the server is alive.

Expected response:

```text
ok
```

### Verify protected routes reject anonymous users

```powershell
curl.exe -sS -o NUL -w "%{http_code}" http://localhost:8080/api/rooms
```

Meaning:

- `-o NUL` discards the response body on Windows.
- `-w "%{http_code}"` prints only the HTTP status code.
- This verifies that `/api/rooms` requires login.

Expected response:

```text
401
```

### List rooms as staff

```powershell
curl.exe -sS -u staff:staff123 http://localhost:8080/api/rooms
```

Meaning:

- `-u staff:staff123` sends HTTP Basic Auth credentials.
- `/api/rooms` returns room inventory as JSON.

### Filter rooms

```powershell
curl.exe -sS -u staff:staff123 "http://localhost:8080/api/rooms?status=AVAILABLE&sort=price"
```

Meaning:

- `status=AVAILABLE` returns only available rooms.
- `sort=price` orders results by nightly rate.

### Read stats

```powershell
curl.exe -sS -u staff:staff123 http://localhost:8080/api/rooms/stats
```

Meaning:

- `/api/rooms/stats` returns dashboard totals and projected revenue.

### Create or update a room as admin

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/rooms' -Method Post -Headers @{Authorization=('Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('admin:admin123')))} -ContentType 'application/json' -Body '{"number":"401","type":"Executive","status":"AVAILABLE","floor":4,"pricePerNight":2800,"occupantName":"","notes":"Near elevator"}'
```

Meaning:

- `Invoke-RestMethod` is PowerShell's HTTP client.
- `-Uri` is the API endpoint.
- `-Method Post` sends a create/update request.
- `-Headers` sends the Basic Auth token.
- `[Convert]::ToBase64String(...)` encodes `admin:admin123` for HTTP Basic Auth.
- `-ContentType 'application/json'` tells Spring Boot the body is JSON.
- `-Body ...` contains the room record.

### Change a room status as admin

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/rooms/401/status' -Method Patch -Headers @{Authorization=('Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('admin:admin123')))} -ContentType 'application/json' -Body '{"status":"OCCUPIED","occupantName":"Test Guest","notes":"Checked in"}'
```

Meaning:

- `Patch` updates part of an existing record.
- The URL targets room `401`.
- The body changes status, occupant, and notes.

### Read audit events as admin

```powershell
curl.exe -sS -u admin:admin123 http://localhost:8080/api/admin/audit
```

Meaning:

- This returns recent admin actions from `audit-log.txt`.

### Delete a room as admin

```powershell
curl.exe -sS -o NUL -w "%{http_code}" -u admin:admin123 -X DELETE http://localhost:8080/api/admin/rooms/401
```

Meaning:

- `-X DELETE` sends an HTTP DELETE request.
- The endpoint removes room `401`.
- Expected success code is `204`.

## 13. Developer Commands

### Start backend in development mode

```powershell
.\mvnw.cmd spring-boot:run
```

Meaning:

- Runs Spring Boot directly from source code.
- Useful while editing backend code.
- Backend runs at `http://localhost:8080`.

### Start frontend in development mode

```powershell
cd frontend
npm run dev
```

Meaning:

- Starts the Vite dev server.
- Usually opens at `http://localhost:5173`.
- `vite.config.mts` proxies `/api` and `/public` to Spring Boot.

### Preview frontend build only

```powershell
cd frontend
npm run preview
```

Meaning:

- Serves the already built files from `frontend/dist`.
- This checks the frontend production build without Spring Boot.

## 14. How Packaging Works

The `pom.xml` file includes this resource rule:

```xml
<resource>
    <directory>frontend/dist</directory>
    <targetPath>static</targetPath>
    <filtering>false</filtering>
</resource>
```

Meaning:

- Maven copies the React production files into `target/classes/static`.
- Spring Boot automatically serves files from `static`.
- When the jar runs, `http://localhost:8080/` opens the React dashboard.

## 15. Docker and GitHub Deployment

The project includes `Dockerfile` and `docker-compose.yml`.

Run locally with Docker:

```powershell
docker compose up --build
```

Meaning:

- `docker compose` reads `docker-compose.yml`.
- `up` starts the app service.
- `--build` rebuilds the Docker image before starting.
- The app is exposed on `http://localhost:8080`.
- Runtime room and audit data are stored in a Docker volume.

The project also includes `.github/workflows/build-and-deploy.yml`.

On pull requests to `main`, the workflow builds and validates the app. On pushes to `main`, it also publishes a Docker image to GitHub Container Registry:

```text
ghcr.io/anishhar03/hotel-security-java
```

Read `WORKFLOW_GUIDE.md` for the short operational checklist.

## 16. Security Notes

This project is demo-ready but not production-hardened.

Before real production use:

- Replace `{noop}` passwords with BCrypt.
- Store users in a database or identity provider.
- Enable HTTPS.
- Review CSRF settings.
- Use a real database such as PostgreSQL, MySQL, or Oracle.
- Add structured application logs.
- Add deployment-specific secrets management.

## 17. Where to Extend Next

Good future improvements:

- Database persistence with JPA.
- JWT login flow.
- Role management UI.
- Booking/reservation module.
- Housekeeping assignment workflow.
- Export audit logs to CSV.
- Dockerfile and optional Docker Compose.
- Real metrics with Spring Boot Actuator.
