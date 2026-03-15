Hotel Security – Java Backend + React Frontend + File Database
=============================================================

This project is a **simple hotel room management system** that demonstrates:

- **Java Spring Boot 3 backend**
- **React + Vite frontend**
- **File-based storage** used as a very lightweight "database"
- **Spring Security** with HTTP Basic auth and role-based access control

It is intended as a clear, minimal, but fully functional example that you can run locally and extend.

--------------------------------------------------------------------------------

High‑Level Architecture
-----------------------

- **Backend**
  - Framework: **Spring Boot 3 (Java 17)**.
  - Security: **Spring Security** with in-memory users and HTTP Basic auth.
  - Purpose: Exposes secured REST endpoints for managing hotel rooms.
  - Persistence: Uses a **plain text file** (`rooms-db.txt`) as the data store.

- **Frontend**
  - Framework: **React 18**, built with **Vite**.
  - Purpose: SPA that lets you:
    - Sign in with staff or admin credentials.
    - View the list of rooms.
    - (Admin) Add / update / delete rooms.
  - Dev server: Proxies `/api` and `/public` to the backend, so no CORS issues.

- **"Database"**
  - A single file in the project root called **`rooms-db.txt`**.
  - Each line represents one room in the format:

    `roomNumber|roomType|status`

    Example:

    `101|Deluxe|AVAILABLE`

--------------------------------------------------------------------------------

Backend – Details
-----------------

### Tech stack

- Java 17+
- Spring Boot 3 (web + security + validation)

### Main packages / classes

- `com.example.hotel.HotelApplication`
  - Standard Spring Boot entry point.

- `com.example.hotel.config.SecurityConfig`
  - Configures:
    - HTTP Basic and form login.
    - CSRF disabled (for simplicity with API + SPA).
    - Authorization rules:
      - Public:
        - `GET /public/ping`
        - `GET /api/public/**`
        - `GET /actuator/health`
      - Authenticated:
        - `GET /api/rooms`
        - `GET /api/rooms/{number}`
      - Admin only:
        - `POST /api/admin/rooms`
        - `DELETE /api/admin/rooms/{number}`
  - Defines in-memory users:
    - `admin / admin123` with role `ADMIN`
    - `staff / staff123` with role `STAFF`

- `com.example.hotel.model.Room`
  - Simple POJO representing a room:
    - `number` (String)
    - `type` (String)
    - `status` (String – e.g. `AVAILABLE`, `OCCUPIED`, `MAINTENANCE`)

- `com.example.hotel.repository.FileRoomRepository`
  - File-based repository using `rooms-db.txt`.
  - Responsibilities:
    - **Read all rooms** from the file.
    - **Find by room number**.
    - **Save** (create or update) a room.
    - **Delete** by room number.
  - Uses a `ReentrantLock` to keep read/write operations thread-safe.
  - File format per line:

    `number|type|status`

- `com.example.hotel.service.RoomService`
  - Service layer on top of `FileRoomRepository`.
  - Methods:
    - `listRooms()`
    - `createOrUpdate(Room room)`
    - `getByNumber(String number)`
    - `delete(String number)`

- `com.example.hotel.controller.HotelController`
  - Annotated with `@RestController` and `@RequestMapping("/api")`.
  - Endpoints:
    - **Health / public**
      - `GET /api/public/ping` (or via security config also `/public/ping`):
        - Returns `"ok"`, no auth required.
    - **Rooms (authenticated)**
      - `GET /api/rooms`
        - Returns JSON array of rooms from the file DB.
      - `GET /api/rooms/{number}`
        - Returns one room or `404` if it does not exist.
    - **Admin (requires role ADMIN)**
      - `POST /api/admin/rooms`
        - Body: JSON of a `Room`:

          ```json
          {
            "number": "101",
            "type": "Deluxe",
            "status": "AVAILABLE"
          }
          ```

        - Creates a new room or updates an existing room with that `number`.
      - `DELETE /api/admin/rooms/{number}`
        - Deletes the room with that `number` if it exists.

--------------------------------------------------------------------------------

Frontend – Details
------------------

The frontend lives in the `frontend` directory and is a small React app.

### Tech stack

- React 18
- Vite 5
- Vanilla CSS for styling (modern, dark themed UI)

### Key files

- `frontend/package.json`
  - Defines scripts:
    - `npm run dev` – start Vite dev server.
    - `npm run build` – build for production.

- `frontend/vite.config.mts`
  - Configures dev server:
    - Runs on port `5173` by default.
    - Proxies:
      - `/api` -> `http://localhost:8080`
      - `/public` -> `http://localhost:8080`
    - This means from the React app you can call `/api/...` directly and it will
      reach the backend without CORS issues in development.

- `frontend/src/main.jsx`
  - Mounts the React app into `index.html`.

- `frontend/src/App.jsx`
  - Main UI:
    - **Authentication section**
      - Username and password inputs.
      - Default values:
        - `staff / staff123`
      - Builds an `Authorization: Basic ...` header for backend calls.
    - **Rooms list**
      - Calls `GET /api/rooms`.
      - Shows room number, type, and status.
      - If logged in as `admin`, you can delete a room via the Delete button
        (which calls `DELETE /api/admin/rooms/{number}`).
    - **Add / Update room form (admin)**
      - Room number, type, status.
      - Calls `POST /api/admin/rooms`.
      - If the room already exists by number, it is updated; otherwise, created.
    - Shows error messages and loading states.

- `frontend/src/style.css`
  - Provides a modern, responsive dark UI with:
    - Cards
    - Simple grid layout
    - Color-coded statuses

--------------------------------------------------------------------------------

Prerequisites
-------------

- **Java**
  - JDK **17+**
  - `JAVA_HOME` set to your JDK installation.

- **Node.js + npm**
  - Node.js 18+ is recommended.
  - npm (comes with Node).

- **Git** (if you’re cloning from GitHub)

--------------------------------------------------------------------------------

How to Run the Project Locally
------------------------------

### 1. Clone the repository

```bash
git clone https://github.com/Anishhar03/hotel_security_java.git
cd hotel_security_java
```

### 2. Start the backend (Spring Boot)

Using the Maven wrapper included in the repo:

```bash
cd hotel_security_java
.\mwdist\mvnw.cmd spring-boot:run
```

or on Unix-like systems:

```bash
./mwdist/mvnw spring-boot:run
```

By default, the backend runs on: `http://localhost:8080`

### 3. Start the frontend (React + Vite)

In a new terminal:

```bash
cd frontend
npm install
npm run dev
```

Vite will print a URL like: `http://localhost:5173`

Open that URL in your browser.

--------------------------------------------------------------------------------

Login Credentials
-----------------

- **Staff (view only)**
  - Username: `staff`
  - Password: `staff123`

- **Admin (manage rooms)**
  - Username: `admin`
  - Password: `admin123`

Staff can view rooms but cannot create / update / delete them. Admin can do all operations.

--------------------------------------------------------------------------------

API Reference (Quick)
---------------------

Base URL: `http://localhost:8080`

All secured endpoints use HTTP Basic authentication.

### Public

- `GET /public/ping`
  - Response: `"ok"`
  - No authentication required.

### Rooms (Authenticated)

- `GET /api/rooms`
  - Auth: `staff` or `admin`.
  - Response: JSON array of rooms.

- `GET /api/rooms/{number}`
  - Auth: `staff` or `admin`.
  - Response: JSON room or `404`.

### Admin (Role ADMIN)

- `POST /api/admin/rooms`
  - Auth: `admin/admin123`.
  - Body:

    ```json
    {
      "number": "101",
      "type": "Deluxe",
      "status": "AVAILABLE"
    }
    ```

  - Creates or updates the room with that number.

- `DELETE /api/admin/rooms/{number}`
  - Auth: `admin/admin123`.
  - Deletes the given room.

--------------------------------------------------------------------------------

Examples with curl
------------------

Public ping:

```bash
curl http://localhost:8080/public/ping
```

List rooms (staff):

```bash
curl -u staff:staff123 http://localhost:8080/api/rooms
```

Create or update a room (admin):

```bash
curl -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"number":"305","type":"Suite","status":"AVAILABLE"}' \
  http://localhost:8080/api/admin/rooms
```

Delete a room (admin):

```bash
curl -u admin:admin123 -X DELETE \
  http://localhost:8080/api/admin/rooms/305
```

--------------------------------------------------------------------------------

Notes and Limitations
---------------------

- The **file database** is intentionally simple:
  - No transactions.
  - Entire file rewritten on each save/delete.
  - Not suitable for production, but excellent for demos and learning.
- Passwords use `{noop}` encoding:
  - Do **not** use as-is in production.
  - Replace with BCrypt (`{bcrypt}`) for real applications.
- CSRF is disabled to simplify SPA + API integration:
  - For a production web app, enable and configure CSRF protection properly.

