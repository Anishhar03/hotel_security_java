# Workflow Guide

This guide explains the complete end-to-end workflow for using, building, pushing, and deploying the Hotel Security project.

## Daily Use Workflow

1. Open the app at `http://localhost:8080/`.
2. Sign in as `staff / staff123` to view room inventory and dashboard stats.
3. Sign in as `admin / admin123` to manage rooms, change statuses, delete rooms, and inspect audit history.
4. Use search, status, floor, and sort filters to narrow the room table.
5. Admin changes are written to `audit-log.txt`.
6. Room data is stored in `rooms-db.txt`.

## Local Development Workflow

Run the backend:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd spring-boot:run
```

Meaning:

- `JAVA_HOME` selects JDK 17.
- `PATH` makes Java 17 the first Java found by the terminal.
- `.\mvnw.cmd` runs the Maven wrapper.
- `spring-boot:run` starts the backend from source code.

Run the frontend in another terminal:

```powershell
cd frontend
npm install
npm run dev
```

Meaning:

- `cd frontend` moves into the React project.
- `npm install` installs frontend dependencies.
- `npm run dev` starts the Vite development server.

## Production Jar Workflow

```powershell
cd frontend
npm install
npm run build
cd ..
.\mvnw.cmd test
.\mvnw.cmd package
& 'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe' -jar target\hotel-security-0.0.1-SNAPSHOT.jar
```

Meaning:

- `npm run build` creates `frontend/dist`.
- `.\mvnw.cmd test` runs automated backend/API tests.
- `.\mvnw.cmd package` creates the deployable Spring Boot jar.
- `java -jar ...` runs the final packaged app.

## Docker Deployment Workflow

Build and run with Docker Compose:

```powershell
docker compose up --build
```

Meaning:

- `docker compose` reads `docker-compose.yml`.
- `up` starts the application service.
- `--build` rebuilds the image before starting it.

Stop the Docker deployment:

```powershell
docker compose down
```

Meaning:

- Stops and removes the running container.
- Keeps the named data volume unless you explicitly delete volumes.

## GitHub Actions Workflow

The repository contains `.github/workflows/build-and-deploy.yml`.

On every pull request to `main`, it:

1. Checks out the repository.
2. Installs Node.js 20.
3. Runs `npm ci`.
4. Builds the React frontend.
5. Installs Java 17.
6. Runs Maven tests.
7. Packages the Spring Boot jar.
8. Uploads the jar as a workflow artifact.
9. Builds the Docker image.

On every push to `main`, it also:

1. Logs in to GitHub Container Registry.
2. Publishes the Docker image to `ghcr.io/anishhar03/hotel-security-java`.
3. Tags the image as `latest`.
4. Tags the image with the Git commit SHA.

## Git Push Workflow

Typical commands:

```powershell
git status
git add .
git commit -m "Upgrade hotel security operations dashboard"
git push origin main
```

Meaning:

- `git status` shows changed files.
- `git add .` stages project files.
- `git commit -m ...` creates a commit with a message.
- `git push origin main` sends the commit to GitHub.

Do not store GitHub tokens in the repository. Use credentials only through your terminal, Git credential manager, or GitHub Actions secrets.

## Health Check Workflow

After starting the app, run:

```powershell
curl.exe -sS http://localhost:8080/actuator/health
```

Meaning:

- Calls Spring Boot Actuator health.
- Confirms the deployed backend is alive.

Expected response includes:

```json
{"status":"UP"}
```
