# Kinetic Fitness

Production-style **microservices fitness platform**: users sign up / sign in with **JWT**, log workouts, and receive **AI coaching recommendations** generated asynchronously through **RabbitMQ + Google Gemini**.

This README is written so you can reopen the project months later and still explain every moving part in an interview — and restart the stack locally without guessing.

---

## Table of contents

1. [What you are building](#1-what-you-are-building)
2. [Architecture (interview view)](#2-architecture-interview-view)
3. [Repository map (every folder)](#3-repository-map-every-folder)
4. [Tech stack & why each library exists](#4-tech-stack--why-each-library-exists)
5. [Ports & service responsibilities](#5-ports--service-responsibilities)
6. [Authentication & security model](#6-authentication--security-model)
7. [Config Server (central configuration)](#7-config-server-central-configuration)
8. [API Gateway (Reactive / WebFlux)](#8-api-gateway-reactive--webflux)
9. [Async AI pipeline (RabbitMQ + Gemini)](#9-async-ai-pipeline-rabbitmq--gemini)
10. [Frontend (React + Vite)](#10-frontend-react--vite)
11. [Environment variables (secrets)](#11-environment-variables-secrets)
12. [Prerequisites](#12-prerequisites)
13. [How to run locally (step by step)](#13-how-to-run-locally-step-by-step)
14. [API reference & curl examples](#14-api-reference--curl-examples)
15. [Request / response bodies](#15-request--response-bodies)
16. [Production readiness checklist (no deploy steps)](#16-production-readiness-checklist-no-deploy-steps)
17. [Troubleshooting](#17-troubleshooting)
18. [Interview talking points](#18-interview-talking-points)

---

## 1. What you are building

Kinetic Fitness is a **distributed Spring Boot system** plus a **React SPA**.

| User action | What happens under the hood |
|---|---|
| Sign up / Sign in | `userService` hashes password (BCrypt), issues JWT |
| Log activity | `activityService` stores workout in MongoDB, publishes event to RabbitMQ |
| AI coaching | `aiService` consumes event, calls Gemini, stores recommendation in MongoDB |
| Browse UI | React app talks **only** to the **API Gateway** (`:8080`) with `Authorization: Bearer <token>` |

Clients **never** send `X-User-Id` as identity. The gateway validates JWT and injects a trusted `X-User-Id` for downstream services.

---

## 2. Architecture (interview view)

```
Browser (React :5173)
        |
        |  HTTPS/HTTP + Bearer JWT
        v
 Reactive API Gateway (:8080)
   - SecurityWebFilterChain
   - JwtAuthenticationFilter (validate JWT, strip forged X-User-Id, inject trusted X-User-Id)
   - Security headers
   - CORS
   - Eureka-aware routing (lb://serviceName)
        |
        +-----> userService (:8010)      PostgreSQL (users)
        +-----> activityService (:8020)  MongoDB (activities) --publish--> RabbitMQ
        +-----> aiService (:8030)        MongoDB (recommendations) <--consume-- RabbitMQ --> Gemini

 Eureka (:8761)         = service registry / discovery
 Config Server (:8888)  = central native configs from config-repo/
```

**Startup order (important):**

1. Eureka  
2. Config Server  
3. userService / activityService / aiService (any order after 1–2)  
4. Gateway  
5. Frontend  

---

## 3. Repository map (every folder)

```
kineticfitness/
├── README.md                 ← this file
├── .env.example              ← template for secrets (copy to local.env)
├── local.env                 ← YOUR secrets (gitignored — never commit)
├── .gitignore
├── config-repo/              ← editable central configs (source of truth)
├── configServer/             ← Spring Cloud Config Server (serves config-repo)
├── eureka/                   ← Netflix Eureka service registry
├── userService/              ← Auth + user profile (PostgreSQL + JWT)
├── activityService/          ← Workout CRUD + RabbitMQ publisher (MongoDB)
├── aiService/                ← Recommendation consumer + Gemini (MongoDB)
├── gateway/                  ← Reactive Spring Cloud Gateway + security
├── frontend/                 ← React (Vite) SPA
└── scripts/
    ├── package-all.sh        ← mvn package all modules + sync config
    ├── start-all.sh          ← start backend jars using local.env
    └── stop-all.sh           ← stop jars started by start-all.sh
```

### Backend modules — important files

#### `eureka/`
| File | Purpose |
|---|---|
| `EurekaApplication.java` | `@EnableEurekaServer` — registry process |
| `application.properties` | Port `8761`, does **not** register itself as a client |

#### `configServer/`
| File | Purpose |
|---|---|
| `ConfigServerApplication.java` | `@EnableConfigServer` |
| `application.properties` | `native` profile; searches `classpath:/config-repo` and `../config-repo` |
| `src/main/resources/config-repo/*` | Packaged copy of configs (kept in sync by `scripts/package-all.sh`) |

#### `config-repo/` (central configuration)
| File | Purpose |
|---|---|
| `application.properties` | Shared: Eureka URL, JWT secret/expiration, actuator exposure |
| `userService.properties` | Port, context-path, Postgres via env |
| `activityService.properties` | Port, Mongo URI, RabbitMQ |
| `aiService.properties` | Port, Mongo URI, RabbitMQ, Gemini settings |
| `gateway.properties` | Port `8080`, reactive routes, CORS |
| `eureka.properties` | Optional overrides for Eureka |

#### `userService/`
| File / package | Purpose |
|---|---|
| `controller/AuthController.java` | `POST /auth/signup`, `POST /auth/login` |
| `controller/UserController.java` | `GET /me`, `GET /{id}`, `GET /{id}/validate` (self-only) |
| `service/AuthService.java` | Register/login, BCrypt, JWT issue |
| `security/JwtService.java` | Create / parse / validate HS384 JWT |
| `security/JwtAuthenticationFilter.java` | Servlet filter — reads Bearer token |
| `config/SecurityConfig.java` | **`securityFilterChain`** — permit `/auth/**`, authenticate rest |
| `model/User.java` + `repository/UserRepository.java` | JPA entity + DB access |
| `dto/*` | Register/Login/AuthResponse/User DTOs |

#### `activityService/`
| File / package | Purpose |
|---|---|
| `controller/ActivityController.java` | Create / list / get activities from JWT principal |
| `service/ActivityService.java` | Business logic + publish event |
| `service/UserValidationService.java` | Calls userService validate with forwarded JWT |
| `messaging/ActivityEventPublisher.java` | Publishes activity JSON to RabbitMQ |
| `security/*` + `config/SecurityConfig.java` | JWT filter + **`securityFilterChain`** |
| `config/RabbitMQConfig.java` | Exchange / queue / binding |

#### `aiService/`
| File / package | Purpose |
|---|---|
| `messaging/ActivityMessageListener.java` | Consumes activity events |
| `service/GeminiService.java` | Calls Gemini generateContent API |
| `service/RecommendationService.java` | Persist / fetch recommendations |
| `service/ActivityClientService.java` | WebClient to activityService (forwards JWT) |
| `controller/RecommendationController.java` | Recommendation APIs (ownership checks) |
| `security/*` + `config/SecurityConfig.java` | JWT filter + **`securityFilterChain`** |

#### `gateway/`
| File / package | Purpose |
|---|---|
| `GatewayApplication.java` | Reactive gateway boot class |
| `config/SecurityConfig.java` | **`securityFilterChain` (`SecurityWebFilterChain`)** + CORS |
| `security/JwtAuthenticationFilter.java` | Edge JWT validation + trusted `X-User-Id` injection |
| `security/JwtService.java` | Token validation only (same secret as services) |
| `security/SecurityHeadersWebFilter.java` | `X-Content-Type-Options`, `X-Frame-Options`, etc. |
| `pom.xml` | Uses `spring-cloud-starter-gateway-server-webflux` (**not** MVC gateway) |

#### `frontend/`
| File / folder | Purpose |
|---|---|
| `src/api/client.js` | Fetch wrapper; attaches Bearer token |
| `src/context/AuthContext.jsx` | Login/signup/logout + session hydrate via `/me` |
| `src/pages/LoginPage.jsx` / `SignupPage.jsx` | Auth screens |
| `src/pages/ActivitiesPage.jsx` | List activities |
| `src/pages/LogActivityPage.jsx` | Create activity |
| `src/pages/ActivityDetailPage.jsx` | Detail + AI coaching panel |
| `src/components/ProtectedRoute.jsx` | Route guard |
| `src/components/AppShell.jsx` | Top nav + logout |
| `vite.config.js` | Dev server `:5173` + optional `/api` proxy |

---

## 4. Tech stack & why each library exists

### Backend
| Library | Where | Why |
|---|---|---|
| Spring Boot **4.1.2** | all services | App framework |
| Spring Cloud **2025.1.4** | all cloud modules | BOM for Gateway / Config / Eureka |
| Spring Cloud Config Server/Client | configServer + services | Centralized config |
| Netflix Eureka | eureka + clients | Service discovery (`lb://userService`) |
| Spring Security | gateway + services | Stateless JWT auth |
| JJWT **0.12.6** | gateway + services | Create/parse signed JWTs |
| Spring Data JPA + PostgreSQL | userService | Relational user store |
| Spring Data MongoDB | activity + ai | Document stores for workouts / recommendations |
| Spring AMQP (RabbitMQ) | activity + ai | Async decoupling for AI work |
| WebClient + LoadBalancer | activity + ai | Inter-service HTTP via Eureka |
| Spring Boot Actuator | gateway (+ shared props) | Health endpoints |
| Lombok | most modules | Boilerplate reduction |
| BCrypt (`PasswordEncoder`) | userService | Password hashing |

### Frontend
| Library | Why |
|---|---|
| React 19 | UI |
| React Router 7 | Client routes + guards |
| Vite 8 | Fast dev server + production build |

---

## 5. Ports & service responsibilities

| Service | Port | Context / base path | Role |
|---|---:|---|---|
| Eureka | 8761 | `/` | Service registry UI + API |
| Config Server | 8888 | `/` | Serves `{app}/{profile}` configs |
| userService | 8010 | `/api/v1/users` | Auth + users |
| activityService | 8020 | `/api/v1/activities` | Activities |
| aiService | 8030 | `/api/v1/ai` | Recommendations |
| Gateway | 8080 | `/` | Public edge |
| Frontend | 5173 | `/` | SPA |

**Public entrypoint for apps/clients:** only `http://localhost:8080` (and the React UI on `5173`).

---

## 6. Authentication & security model

### JWT flow (production pattern used here)

1. Client calls `POST /api/v1/users/auth/signup` or `/login` (**no token**).
2. `userService` returns:
   - `token` (JWT)
   - `tokenType: Bearer`
   - `expiresIn`
   - `user` profile
3. Client stores token (frontend: `localStorage` keys `kf_token`, `kf_user`).
4. Every protected call includes:
   ```http
   Authorization: Bearer <jwt>
   ```
5. **Gateway**:
   - Rejects missing/invalid JWT with `401`
   - **Removes any client-supplied `X-User-Id`**
   - Parses JWT subject → trusted user id
   - Adds `X-User-Id: <subject>`
   - Sets reactive `SecurityContext`
6. Downstream services also validate JWT (defense in depth) and/or trust gateway header when present.
7. Controllers take identity from `Authentication.getName()` — **not** from a client header.

### SecurityFilterChain / SecurityWebFilterChain

Every secured module exposes a bean named conceptually as the security filter chain:

- **Servlet services** (`userService`, `activityService`, `aiService`):  
  `SecurityFilterChain securityFilterChain(HttpSecurity http)`
- **Reactive gateway**:  
  `SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, ...)`

Common settings:
- CSRF disabled (stateless API)
- Session policy **STATELESS** (servlet apps)
- Public: auth endpoints + health
- Everything else: authenticated

### Extra gateway hardening
- Security response headers (`nosniff`, `DENY` framing, `no-store`, permissions policy)
- CORS allow-list (`FRONTEND_ORIGIN`, default `http://localhost:5173`)
- Ownership checks on user/activity/recommendation reads
- Secrets only via environment variables (not committed)

---

## 7. Config Server (central configuration)

### How linking works

Each service `application.properties` is intentionally thin:

```properties
spring.application.name=userService
spring.config.import=optional:configserver:http://localhost:8888
```

On boot, the service asks Config Server for `http://localhost:8888/<spring.application.name>/default`.

Config Server (`native` profile) loads files from:

1. `classpath:/config-repo` (inside the configServer jar/resources)
2. `file:./config-repo`
3. `file:../config-repo` (handy when running from module folders)

**Edit configs in repo-root `config-repo/`**, then run `./scripts/package-all.sh` (it copies into `configServer/src/main/resources/config-repo/`).

### Shared JWT secret

`config-repo/application.properties` defines:

```properties
jwt.secret=${JWT_SECRET:kinetic-fitness-dev-secret-change-me-32chars-min}
jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}
```

**All services + gateway must share the same `JWT_SECRET`** or tokens will fail validation at the edge.

---

## 8. API Gateway (Reactive / WebFlux)

The gateway **is not** Spring MVC.

`gateway/pom.xml` uses:

```xml
spring-cloud-starter-gateway-server-webflux
```

Routes (from `config-repo/gateway.properties`):

| Predicate | Target |
|---|---|
| `/api/v1/users/**` | `lb://userService` |
| `/api/v1/activities/**` | `lb://activityService` |
| `/api/v1/ai/**` | `lb://aiService` |

`lb://` means: resolve instances from Eureka + client-side load balancing.

---

## 9. Async AI pipeline (RabbitMQ + Gemini)

```
POST /api/v1/activities
   → activityService saves Mongo document
   → publishes ActivityMessage to RabbitMQ exchange/queue
   → returns activity immediately to client

aiService listener
   → receives message
   → GeminiService builds prompt from activity fields
   → stores Recommendation in MongoDB

Client later GET /api/v1/ai/recommendations/activity/{activityId}
   → returns cached recommendation (or generates on-demand if missing)
```

Required local dependency: **RabbitMQ** listening on `localhost:5672` (default guest/guest).

Gemini settings (env):
- `GEMINI_API_KEY` (required)
- `GEMINI_BASE_URL` (default Google Generative Language API)
- `GEMINI_MODEL` (default `gemini-3.6-flash`)

---

## 10. Frontend (React + Vite)

### Features
- Sign up / Sign in
- Session restore via `/api/v1/users/me`
- Log activity
- View all activities
- Activity detail + AI coaching panel
- Logout (clears token)

### Dev configuration
`frontend/.env.example`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

The SPA calls the **gateway**, never individual service ports.

### Run frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

Production build check:

```bash
npm run build
```

---

## 11. Environment variables (secrets)

**Never commit real secrets.** Use:

```bash
cp .env.example local.env
# edit local.env with real values
set -a && source local.env && set +a
```

| Variable | Used by | Meaning |
|---|---|---|
| `JWT_SECRET` | all secured apps | HMAC secret (≥ 32 chars) |
| `JWT_EXPIRATION_MS` | userService | Token lifetime |
| `DB_URL` | userService | JDBC Postgres URL |
| `DB_USERNAME` / `DB_PASSWORD` | userService | DB credentials |
| `MONGODB_URI` | activityService | Activities DB |
| `AI_MONGODB_URI` | aiService | Recommendations DB (falls back to `MONGODB_URI`) |
| `RABBITMQ_*` | activity + ai | Broker connection |
| `GEMINI_API_KEY` | aiService | Google AI key |
| `GEMINI_BASE_URL` / `GEMINI_MODEL` | aiService | Model endpoint |
| `FRONTEND_ORIGIN` | gateway | CORS allow-list |

`local.env` is gitignored.

---

## 12. Prerequisites

Install / have running:

1. **JDK 21**
2. **Maven 3.9+**
3. **Node.js 20+** and npm
4. **RabbitMQ** on `localhost:5672`
5. Network access to:
   - PostgreSQL (Neon or other)
   - MongoDB Atlas (or local Mongo)
   - Google Gemini API

Verify:

```bash
java -version
mvn -version
node -v
npm -v
# RabbitMQ
nc -z localhost 5672 && echo rabbit_ok
```

---

## 13. How to run locally (step by step)

### A. One-time setup

```bash
cd /path/to/kineticfitness
cp .env.example local.env
# Fill local.env with your DB / Mongo / Gemini values

set -a && source local.env && set +a
./scripts/package-all.sh
```

### B. Start backend

**Option 1 — scripts**

```bash
set -a && source local.env && set +a
./scripts/start-all.sh
```

**Option 2 — IntelliJ**

1. Create/import each module.
2. Add the same env vars from `local.env` to each Run Configuration (Environment variables).
3. Start in order: Eureka → Config Server → user/activity/ai → Gateway.

**Option 3 — manual jars**

```bash
set -a && source local.env && set +a
java -jar eureka/target/eureka-0.0.1-SNAPSHOT.jar
java -jar configServer/target/configServer-0.0.1-SNAPSHOT.jar
java -jar userService/target/userService-0.0.1-SNAPSHOT.jar
java -jar activityService/target/activityService-0.0.1-SNAPSHOT.jar
java -jar aiService/target/aiService-0.0.1-SNAPSHOT.jar
java -jar gateway/target/gateway-0.0.1-SNAPSHOT.jar
```

### C. Start frontend

```bash
cd frontend
npm install
npm run dev
```

### D. Sanity checks

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8761/          # 200
curl -s http://localhost:8888/userService/default | head                  # JSON propertySources
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/activities  # 401 without token
```

### E. Stop backend (script-started)

```bash
./scripts/stop-all.sh
```

---

## 14. API reference & curl examples

Base URL: `http://localhost:8080`

### Auth (public)

```bash
# Sign up
curl -s -X POST http://localhost:8080/api/v1/users/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Ada",
    "lastName": "Lovelace",
    "email": "ada@example.com",
    "password": "Password1"
  }'

# Login
curl -s -X POST http://localhost:8080/api/v1/users/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "ada@example.com",
    "password": "Password1"
  }'
```

Export token:

```bash
TOKEN='paste_token_here'
```

### Users (protected)

```bash
curl -s http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```

### Activities (protected)

```bash
# Create
curl -s -X POST http://localhost:8080/api/v1/activities \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "RUNNING",
    "duration": 30,
    "caloriesBurned": 250,
    "startTime": "2026-11-08T10:00:00"
  }'

# List mine
curl -s http://localhost:8080/api/v1/activities \
  -H "Authorization: Bearer $TOKEN"

# Get one
curl -s http://localhost:8080/api/v1/activities/<activityId> \
  -H "Authorization: Bearer $TOKEN"
```

### AI recommendations (protected)

```bash
curl -s http://localhost:8080/api/v1/ai/recommendations/activity/<activityId> \
  -H "Authorization: Bearer $TOKEN"

curl -s http://localhost:8080/api/v1/ai/recommendations/user/<userId> \
  -H "Authorization: Bearer $TOKEN"
```

---

## 15. Request / response bodies

### Signup / Login request

```json
{
  "firstName": "Ada",          // signup only
  "lastName": "Lovelace",      // signup only
  "email": "ada@example.com",
  "password": "Password1"      // min 8 chars on signup
}
```

### Auth response

```json
{
  "token": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": "uuid",
    "email": "ada@example.com",
    "firstName": "Ada",
    "lastName": "Lovelace"
  }
}
```

### Create activity request

```json
{
  "type": "RUNNING",
  "duration": 30,
  "caloriesBurned": 250,
  "startTime": "2026-11-08T10:00:00",
  "additionalMetrics": { "distanceKm": 5 }
}
```

`userId` in the body is ignored/overwritten from JWT.

### Activity response (note field name)

```json
{
  "id": "...",
  "userId": "...",
  "activityType": "RUNNING",
  "duration": 30,
  "caloriesBurned": 250,
  "startTime": "...",
  "createdAt": "...",
  "updatedAt": "..."
}
```

### Recommendation response

```json
{
  "id": "...",
  "userId": "...",
  "activityId": "...",
  "activityType": "RUNNING",
  "recommendations": ["..."],
  "suggestions": ["..."],
  "safety": ["..."],
  "createdAt": "..."
}
```

---

## 16. Production readiness checklist (no deploy steps)

This repo is structured so you can expose it behind HTTPS / a cloud load balancer later. Before public exposure, verify:

1. **Strong unique `JWT_SECRET`** (rotate-capable, ≥ 32 chars, not the sample default)
2. **All DB/API secrets only in env / secret manager** (not git)
3. **Only gateway port** is public; Eureka / Config / service ports stay private
4. **HTTPS termination** at reverse proxy / load balancer
5. **CORS** locked to your real frontend origin(s)
6. **RabbitMQ / Mongo / Postgres** network-restricted
7. **Actuator** limited (`health`/`info` only are intended)
8. **Rebuild + sync config** after config-repo edits (`./scripts/package-all.sh`)
9. Prefer managed secrets over `local.env` in real environments
10. Add rate limiting / WAF at the edge when you deploy (not bundled here to avoid infra lock-in)

This README intentionally **does not** include cloud deployment commands — you will deploy manually.

---

## 17. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| Service starts but has no DB/Mongo settings | Config Server down / wrong order | Start Eureka → Config → services |
| `401` on all gateway calls | Missing/invalid Bearer token | Login again; check `JWT_SECRET` match |
| Signup works, activities fail user validation | JWT not forwarded to userService | Ensure Authorization header present; activity filter stores token as credentials |
| AI recommendation empty / 5xx | Missing `GEMINI_API_KEY` or RabbitMQ down | Set key; ensure broker on `5672` |
| Frontend CORS errors | Origin not allowed | Set `FRONTEND_ORIGIN` to exact UI origin |
| Gateway route 404/503 | Service not registered in Eureka | Open `http://localhost:8761` and confirm instances UP |
| Config changes ignored | Classpath copy stale | Re-run `./scripts/package-all.sh` and restart Config Server + clients |
| Mongo / Postgres auth errors | Bad env values | Re-check `local.env` quoting (`&` in URIs must be inside quotes) |

Useful logs:

```bash
tail -f .run-logs/*.log
```

---

## 18. Interview talking points

Use these to explain the project cleanly:

1. **Microservices + API Gateway**: browser talks to one edge service; gateway routes by path to Eureka-registered instances.
2. **JWT at the edge**: gateway validates token once, strips forged identity headers, injects trusted `X-User-Id`.
3. **Defense in depth**: services still have Spring Security + JWT filters and ownership checks.
4. **Config Server**: externalized configuration (`native` file backend) so secrets/URLs are not hardcoded per service jar.
5. **Async AI**: activity write path stays fast; recommendation generation is event-driven via RabbitMQ.
6. **Reactive gateway**: Netty/WebFlux (`SecurityWebFilterChain`) instead of blocking MVC servlet gateway.
7. **Stateless security**: no server sessions; BCrypt passwords; HS JWT with shared secret (upgrade path: asymmetric keys / OAuth2 resource server).
8. **Frontend as a pure client**: React stores Bearer token and never trusts `X-User-Id`.

---

## Quick command card

```bash
# secrets
cp .env.example local.env && $EDITOR local.env
set -a && source local.env && set +a

# build
./scripts/package-all.sh

# run backend
./scripts/start-all.sh

# run UI
cd frontend && npm install && npm run dev

# stop backend
./scripts/stop-all.sh
```

That’s the whole system — from signup JWT, through gateway security, to async AI coaching and a production-minded React client.
