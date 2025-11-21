# Workshop 3 – Full Stack Implementation (GOAT Project)

This folder contains the implementation for **Software Engineering II – Workshop 3**:

- Relational database (PostgreSQL)
- Java backend (Identity, Navigation, Listing, Authentication)
- Python backend (OTP & Catalog support)
- Web frontend (React)
- Basic unit tests for Java and Python (optional for running the system)

---

## 1. Repository Layout

```text
Workshop-3/
  README.md

  database/
    README.md          # schema description and how to apply it

  java-backend/
    README.md          # how to configure and run the Java service
    AUTH_INTEGRATION.md# frontend ↔ Java auth & OTP integration guide

  python-backend/
    README.md          # how to configure and run the Python service

  web-frontend/
    README.md          # how to run the React app and connect it to Java
2. Prerequisites
To run the full system locally you need:

PostgreSQL (local or Docker)

MongoDB (local or Docker)

Java 17+ (for Spring Boot backend)

Python 3.10+ (for OTP & catalog backend)

Node.js 18+ and npm (for React frontend)

Optional: Poetry (for Python dependency management)

3. Services and Ports
By convention, the services run on:

PostgreSQL: localhost:5432

MongoDB: localhost:27017

Java backend: http://localhost:8081

Python backend: http://localhost:8082

React frontend: http://localhost:5173 (Vite default)

4. How to Run Everything (Quick Start)
4.1 Step 1 – Start PostgreSQL and MongoDB
You can use local installations or Docker. Example with Docker:

bash
Copiar código
# PostgreSQL
docker run --name goat-postgres -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=goat \
  -p 5432:5432 -d postgres:16

# MongoDB
docker run --name goat-mongo -p 27017:27017 -d mongo:7
Then, apply the SQL schema as described in database/README.md.

4.2 Step 2 – Run the Java Backend (port 8081)
Go to the java-backend folder and follow the instructions in:

java-backend/README.md

Typical flow:

bash
Copiar código
cd Workshop-3/java-backend
# If you use Maven:
mvn spring-boot:run

# Or if you use Gradle:
./gradlew bootRun
The Java backend must be reachable at:

text
Copiar código
http://localhost:8081
4.3 Step 3 – Run the Python Backend (port 8082)
Go to the python-backend folder and follow:

python-backend/README.md

Typical flow (with Poetry, for example):

bash
Copiar código
cd Workshop-3/python-backend
poetry install
poetry run uvicorn app.main:app --reload --port 8082
The Python service must be reachable at:

text
Copiar código
http://localhost:8082
Remember: the frontend never calls Python directly. Only Java ↔ Python.

4.4 Step 4 – Run the React Frontend
Go to the web-frontend folder and follow:

web-frontend/README.md

Basic flow:

bash
Copiar código
cd Workshop-3/web-frontend
npm install
npm run dev
By default, Vite will run at:

text
Copiar código
http://localhost:5173
The frontend is configured to call the Java backend at:

text
Copiar código
http://localhost:8081
(Using an environment variable such as VITE_API_BASE_URL.)

5. How Components Talk to Each Other
Frontend React → Java backend (8081)
All REST API calls (auth, listings, menus) go here.

Java backend (8081) → Python backend (8082)
For OTP generation and verification.

Java backend → PostgreSQL
Identity, navigation and listings are stored here.

Python backend → MongoDB
OTPs (TTL) and catalog-related documents are stored here.

6. Additional Documents
database/README.md – explains the SQL schema and how to apply it.

java-backend/README.md – explains endpoints, configuration and run instructions.

java-backend/AUTH_INTEGRATION.md – detailed frontend ↔ Java auth/OTP integration.

python-backend/README.md – explains the internal OTP API and how to run it.

web-frontend/README.md – explains how to configure and run the React app.

All documents are in English as required by the course.

yaml
Copiar código

---

## 2️⃣ `Workshop-3/java-backend/README.md` – cómo correr Java

```md
# Java Backend – Identity, Navigation & Listing Service

## 1. Overview

This backend is responsible for:

- **Identity**: users, roles, RBAC.
- **Navigation**: dynamic menus (public + role-based).
- **Listing**: sneaker listings created by sellers.
- **Authentication & OTP orchestration**: communicates with the Python service.

Base URL:

```text
http://localhost:8081
All frontend calls go to this service.

2. Prerequisites
Java 17 or newer.

Maven or Gradle.

Access to a PostgreSQL instance with the identity, navigation and listing schemas created (see ../database/README.md).

3. Configuration
Typical application.yml example:

yaml
Copiar código
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/goat
    username: goat_app_user
    password: goat_password
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

python:
  otp:
    base-url: http://localhost:8082
Adjust the URL, username and password to match your local PostgreSQL configuration.

4. How to Run the Service
From the java-backend folder:

4.1 Using Maven
bash
Copiar código
mvn clean install
mvn spring-boot:run
4.2 Using Gradle
bash
Copiar código
./gradlew clean build
./gradlew bootRun
If everything is correct, the service will be available at:

text
Copiar código
http://localhost:8081
Useful health check:

bash
Copiar código
curl http://localhost:8081/actuator/health
(if actuator is enabled).

5. Main REST Endpoints
Detailed documentation is in AUTH_INTEGRATION.md. In summary:

Authentication & OTP
POST /api/auth/register

POST /api/auth/login

POST /api/auth/otp

POST /api/auth/verify

GET /api/auth/confirm-email?email=...

Navigation
GET /api/navigation/menus
Returns public + role-based menus for the current user.

Listings (example design)
GET /api/listings – public published listings for the shop.

GET /api/listings/{id} – listing details.

GET /api/listings/mine – listings for the authenticated seller.

POST /api/listings – create draft listing.

PUT /api/listings/{id} – update draft listing only.

PUT /api/listings/{id}/publish – change status to PUBLISHED.

PUT /api/listings/{id}/archive – change status to ARCHIVED.

6. Running With the Full Stack
To see the system working end-to-end:

Start PostgreSQL and apply the SQL schema.

Start MongoDB.

Start the Python backend (OTP) at http://localhost:8082.

Start this Java backend at http://localhost:8081.

Start the React frontend and open it in your browser.

The frontend will:

Call this service at http://localhost:8081 for all operations.

Trigger OTP flows that are internally delegated to the Python service.

yaml
Copiar código

---

## 3️⃣ `Workshop-3/python-backend/README.md` – cómo correr Python

```md
# Python Backend – OTP & Catalog Service

## 1. Overview

This service provides:

- **OTP management with TTL in MongoDB**:
  - Purposes: `REGISTER`, `LOGIN`, `EMAIL_CONFIRMATION`, `RESET_PASSWORD`.
- Optional **catalog support** (canonical sneakers, brands, categories).

The frontend does **not** call this service directly.  
Only the Java backend uses it internally.

Base URL (for Java):

```text
http://localhost:8082
2. Prerequisites
Python 3.10 or newer.

MongoDB running at mongodb://localhost:27017 (or another URI).

One of:

Poetry (recommended), or

pip + virtual environment.

3. Installation
From the python-backend folder:

3.1 Using Poetry (recommended)
bash
Copiar código
poetry install
3.2 Using pip + venv (alternative)
bash
Copiar código
python -m venv .venv
source .venv/Scripts/activate  # Windows PowerShell: .venv\Scripts\Activate.ps1
pip install -r requirements.txt
4. Configuration
Typical environment variables:

bash
Copiar código
export MONGODB_URI="mongodb://localhost:27017/goat"
export OTP_EXPIRE_MINUTES=5
export SERVICE_PORT=8082
On Windows PowerShell:

powershell
Copiar código
$env:MONGODB_URI="mongodb://localhost:27017/goat"
$env:OTP_EXPIRE_MINUTES="5"
$env:SERVICE_PORT="8082"
5. How to Run the Service
Assuming a FastAPI application with app/main.py and app as the package:

5.1 With Poetry
bash
Copiar código
poetry run uvicorn app.main:app --reload --port 8082
5.2 With plain Python
bash
Copiar código
uvicorn app.main:app --reload --port 8082
The service will be available at:

text
Copiar código
http://localhost:8082
6. Internal Endpoints (for Java)
Example design:

6.1 Create OTP
POST /internal/otp

json
Copiar código
{
  "email": "user@example.com",
  "purpose": "EMAIL_CONFIRMATION"
}
6.2 Verify OTP
POST /internal/otp/verify

json
Copiar código
{
  "email": "user@example.com",
  "otp": "123456",
  "purpose": "EMAIL_CONFIRMATION"
}
Responses are consumed by the Java backend, which then updates the relational database.

7. Running With the Full Stack
Start MongoDB.

Run this Python service on port 8082.

Configure the Java backend so that python.otp.base-url = http://localhost:8082.

Start the Java backend and then the React frontend.

The complete registration + OTP flow will use this service transparently.

yaml
Copiar código

---

## 4️⃣ `Workshop-3/web-frontend/README.md` – cómo correr el frontend React (`npm run dev`)

```md
# Web Frontend – React (Vite) for GOAT Project

## 1. Overview

This is a React-based frontend (using Vite) that:

- Allows users to **register** and confirm their email using OTP.
- Allows users to **log in**.
- Displays a **shop page** with published listings.
- Builds the navigation menu from the Java backend responses.

It communicates **only** with the Java backend:

```text
Java backend: http://localhost:8081
2. Prerequisites
Node.js 18+

npm (comes with Node.js)

3. Installation
From the web-frontend folder:

bash
Copiar código
npm install
This installs all project dependencies.

4. Configuration – API Base URL
The frontend needs to know where the Java backend is running.
We use an environment variable, for example with Vite:

Create a file .env.local (or .env) in web-frontend:

env
Copiar código
VITE_API_BASE_URL=http://localhost:8081
In your React code you can then use:

ts
Copiar código
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
All calls should use this base URL, for example:

ts
Copiar código
fetch(`${API_BASE_URL}/api/auth/login`, { ... });
5. How to Run the Frontend
From the web-frontend folder:

bash
Copiar código
npm run dev
Vite will start a development server, usually at:

text
Copiar código
http://localhost:5173
Open this URL in your browser.

Make sure the Java backend (http://localhost:8081) is already running, otherwise the API calls will fail.

6. Main Flows
6.1 Registration + Email Confirmation
POST /api/auth/register

POST /api/auth/otp with purpose = "EMAIL_CONFIRMATION"

POST /api/auth/verify with the OTP

6.2 Login
POST /api/auth/login

The JWT token is stored in localStorage (or another mechanism) and sent in the Authorization: Bearer {token} header for protected endpoints.

6.3 Shop
GET /api/listings
Renders the list of published listings.

7. Running With the Full Stack
Start PostgreSQL and MongoDB.

Start the Python backend on port 8082.

Start the Java backend on port 8081.

Configure VITE_API_BASE_URL in .env.local as http://localhost:8081.

Run the frontend:

bash
Copiar código
npm run dev
Open the browser at http://localhost:5173 and go through:

Register → enter OTP → login → browse shop.

This demonstrates the full integration required for Workshop 3.