# FoodBridge — Food Donation & Redistribution System

FoodBridge is a full-stack student project that connects food donors, verified NGOs and volunteers. Donors can list surplus food, NGOs can request it, donors can accept a request, volunteers can claim a collection task, and the delivery is completed with a handoff verification code.

This ZIP contains a **runnable local MVP** with React, Spring Boot, MySQL, JWT authentication, role-specific pages, seeded demo accounts and Docker startup scripts.

## Quick start on Windows

### What you need

1. Windows 10/11 (64-bit).
2. [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/) installed and running.
3. Internet connection for the first build, because Docker downloads the base images and dependencies.

You do **not** need to install Java, Maven, Node.js or MySQL on Windows to use the included Docker startup flow.

### Run it

1. Extract the ZIP file to a normal folder, for example `C:\Projects\FoodBridge`.
2. Open Docker Desktop and wait until it says the engine is running.
3. Double-click `start-windows.bat`. If Docker cannot access the folder, move the extracted project outside OneDrive / protected system folders.
4. Wait for the first build to finish. Open `http://localhost:5173` in your browser if it does not open automatically.
5. Keep the terminal window running while you use the app.

To stop the app, press `Ctrl+C` in the startup window, or run `stop-windows.bat`.

The local application uses:

- Frontend: http://localhost:5173
- Backend health: http://localhost:8080/api/health
- MySQL host port: 3307 (inside Docker, port 3306)

### Run without Docker

This option uses a local H2 database and does not change or require a MySQL installation. Install Java 21+, Maven and Node.js first, then open two terminals in the project folder.

In the first terminal:

```powershell
cd .\backend
$env:SPRING_PROFILES_ACTIVE = "local"
$env:PORT = "8081"
mvn spring-boot:run
```

In the second terminal:

```powershell
cd .\frontend
npm ci
$env:VITE_API_BASE_URL = "http://localhost:8081/api"
npm run dev
```

Open `http://localhost:5173`. The H2 database is stored under `backend/data/`. This local profile seeds the demo accounts listed below. The Docker Compose setup continues to use MySQL.

## Demo logins

All demo accounts use the password shown below. These credentials are for local demonstration only. Change them before any shared or public deployment.

| Role | Email | Password |
|---|---|---|
| Admin | `admin@foodbridge.local` | `Admin123!` |
| Donor | `donor@foodbridge.local` | `Donor123!` |
| Verified NGO | `ngo@foodbridge.local` | `Ngo123!` |
| Pending NGO | `pending@foodbridge.local` | `Ngo123!` |
| Volunteer | `volunteer@foodbridge.local` | `Volunteer123!` |

The database is seeded only when it is empty. The demo donation listings are sample records for demonstrating the workflow, not real-world offers of food.

## Main workflow to demonstrate

1. Sign in as the donor and open **My donations**. Add a food donation with a future pickup deadline and use-by time.
2. Sign in as the verified NGO in another browser/incognito window. Open **Find food** and request the listing.
3. Sign back in as the donor. Open **Donation requests** and accept the request.
4. Sign in as the volunteer. Open **Delivery tasks**, claim the task and mark it picked up.
5. Sign in as the NGO or donor to view the 6-digit delivery code. Enter that code as the volunteer and complete the delivery.
6. Sign in as admin to review users, approve pending NGOs and inspect donation listings.

To create more demo users, use the Register page. Public registration does not allow an ADMIN role. New NGO accounts begin unverified and must be approved by the admin.

## Technology stack

### Frontend

- React 19 + Vite
- React Router
- Axios
- Lucide React icons
- Responsive CSS design

### Backend

- Java 21
- Spring Boot 4.1.x
- Spring Web, Spring Data JPA and Spring Security
- JWT using JJWT
- Bean validation and BCrypt password hashing
- Maven

### Database and local runtime

- MySQL 8.4 for Docker Compose
- File-backed H2 for native local development and in-memory H2 for tests
- Hibernate updates the schema in local/demo profiles; production applies Flyway migrations and validates the schema
- Docker Compose
- Persistent Docker volumes for database data and uploaded images

## Features included

- Registration and login for Donor, NGO and Volunteer roles.
- Admin account is seeded locally and cannot be created through public registration.
- Production creates its initial admin from the configured `ADMIN_EMAIL` and `ADMIN_PASSWORD` environment variables.
- JWT-protected APIs and role checks.
- Donor food listing form, image upload, listing history and cancellation.
- NGO food discovery, search/filter, request submission and request history.
- Donor accept/reject workflow. Accepting a request reserves the donation and rejects other pending requests for that listing.
- Volunteer task claiming, pickup status and delivery completion using a 6-digit verification code.
- In-app notifications.
- Admin dashboard, NGO verification, account enable/disable and donation overview.
- Food type, use-by time, pickup deadline, storage, allergens, packaging and donor safety declaration fields.
- Responsive pages for desktop and mobile.

## Project structure

```text
FoodBridge/
├── backend/                 Spring Boot REST API
│   ├── src/main/java/       Controllers, services, entities, security
│   ├── src/main/resources/  application configuration and Flyway migrations
│   └── Dockerfile
├── frontend/                React + Vite application
│   ├── src/                  Pages, components, auth context and CSS
│   ├── Dockerfile
│   └── vercel.json           Vercel SPA routing
├── docs/                    Architecture, API and deployment notes
├── docker-compose.yml
├── render.yaml              Render API deployment
├── start-windows.bat
└── stop-windows.bat
```

## Troubleshooting

### Docker is not recognized
Install Docker Desktop, restart Windows if requested, open Docker Desktop, then try again.

### Docker engine is not running
Open Docker Desktop and wait for the engine to finish starting.

### Port 5173, 8080 or 8081 is already in use
Close the other application using that port, or change the port and frontend API URL in the run commands or `docker-compose.yml`.

### The frontend opens but API calls fail
Wait another 30–60 seconds for Spring Boot and MySQL to finish starting, then refresh the page. Check the startup terminal for backend errors.

### Reset the local database
This deletes the local FoodBridge database volume and all records stored in it. From the project folder run:

```bash
docker compose down -v
docker compose up --build
```

### View logs

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
```

## API overview

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register donor, NGO or volunteer |
| POST | `/api/auth/login` | Login and receive JWT |
| GET | `/api/auth/me` | Current signed-in user |
| GET | `/api/donations` | Available donations |
| POST | `/api/donations` | Create a donation (donor) |
| GET | `/api/donations/mine` | Donor's listings |
| POST | `/api/requests` | NGO requests a donation |
| GET | `/api/requests/mine` | NGO requests / donor incoming requests |
| PUT | `/api/requests/{id}/accept` | Donor accepts request |
| PUT | `/api/requests/{id}/reject` | Donor rejects request |
| GET | `/api/deliveries/available` | Volunteer sees unclaimed delivery tasks |
| PUT | `/api/deliveries/{id}/claim` | Volunteer claims task |
| PUT | `/api/deliveries/{id}/pickup` | Volunteer marks pickup |
| PUT | `/api/deliveries/{id}/complete` | Volunteer confirms delivery with code |
| GET | `/api/notifications` | Current user's notifications |
| GET | `/api/admin/overview` | Admin summary |
| GET | `/api/admin/ngos/pending` | NGOs awaiting verification |

All endpoints except login, registration, logout, the health endpoint and uploaded image retrieval require authentication. Admin endpoints require the ADMIN role.

## Important limitations before real-world launch

This is a functional **student MVP**, not a certified food-safety, logistics or production SaaS system. The backend tests and frontend production build have been verified; the Docker image build was not run because Docker is unavailable in this environment. Before public use:

- Replace demo passwords and local JWT secret with strong, unique production secrets.
- The production profile applies the checked-in Flyway schema migration. Test it against your selected managed database before using an existing database; do not point the initial migration at a database containing FoodBridge tables without first baselining it.
- Use a managed production MySQL-compatible database (the documented setup uses TiDB Cloud Starter) and HTTPS.
- Configure a production frontend origin and strict CORS.
- Move uploaded images to managed object storage (the included local upload volume is for local use).
- Add email/SMS delivery, abuse reporting, audit logs, backups and operational monitoring as needed.
- Verify organizations and volunteers through a real documented process.
- Establish local food-safety checks and user terms. FoodBridge does not inspect or certify donated food.
- Review concurrency and operational controls before handling real donations at scale.
- The frontend development/build dependency graph currently has an unpatched high-severity [Picomatch advisory](https://github.com/advisories/GHSA-fc5q-39xc-vjpf); no patched upstream release is listed. It is excluded from production frontend dependencies, but re-run the audit before each release.

The included startup script runs the project locally. It does not publish it to the public internet.
