Basic Spring Security authentication for a Hotel Management API (Spring Boot 3)

Features
- In-memory users: admin/admin123 (ROLE_ADMIN), staff/staff123 (ROLE_STAFF)
- HTTP Basic and form login enabled
- Role-based access rules
  - Public: GET /public/ping
  - Authenticated: GET /api/rooms
  - Admin only: POST /api/admin/rooms

Prerequisites
- JDK 17+
- Maven 3.8+

Run
1) From the project folder:
   mvn spring-boot:run

2) Test endpoints (use a REST client or curl):

- Public (no auth):
  curl http://localhost:8080/public/ping

- Authenticated (rooms list):
  curl -u staff:staff123 http://localhost:8080/api/rooms

- Admin-only (create room):
  curl -u admin:admin123 -H "Content-Type: application/json" -d '{"number":"305"}' http://localhost:8080/api/admin/rooms

Notes
- Passwords use {noop} for simplicity; replace with BCrypt for production.
- CSRF is disabled for API simplicity; enable/configure for browser clients.
