# Changelog

## 2026-05-27

- Expanded the project into a hotel security operations dashboard with richer room metadata.
- Added typed room status values, filtering, sorting, dashboard stats, status transitions, and admin audit history.
- Added file-backed audit persistence in `audit-log.txt`.
- Rebuilt the React UI as a responsive operations dashboard with staff/admin role-aware views.
- Added MockMvc tests for public access, authentication, stats, admin room management, status changes, and audit.
- Added `PROJECT_GUIDE.md` with architecture, workflow, API details, and command-by-command explanations.
- Added `WORKFLOW_GUIDE.md`, Docker deployment files, and a GitHub Actions build/deploy workflow.
- Added Spring Boot Actuator health/info endpoints for deployment checks.
- Restored the Maven wrapper configuration so the project can build from the root with `.\mvnw.cmd`.
- Packaged the React production build into the Spring Boot jar under static resources.
- Added a public root ping endpoint at `/public/ping`.
- Added default room seed data and automatic file database initialization when `rooms-db.txt` is missing.
- Hid admin-only frontend controls for staff users so the UI matches backend authorization.
- Updated `.gitignore` for local room data and frontend dependencies.
- Documented Java 17, one-jar deployment, API validation commands, credentials, and the decision not to add Redis or Kafka.
