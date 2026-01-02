# 🚀 Project Challenges & Solutions Log

This document records the significant technical challenges encountered during the development of this FAANG-ready Multi-Calendar System, along with the specific solutions implemented.

## 1. Unit Testing Compatibility with Java 25
### 🔴 The Problem
When running backend tests (`mvn test`), the build failed with:
`Java 25 (69) is not supported by the current version of Byte Buddy which officially supports Java 22 (66)`

### 🔍 Root Cause
The development environment had Java 25 installed (via Homebrew), but the Spring Boot 3.2.1 dependencies (specifically Mockito and Byte Buddy) only supported up to Java 22. This version mismatch prevented mocking frameworks from instrumenting classes.

### ✅ The Solution
Configured Maven to explicitly use the Java 21 LTS installation which is fully supported by the current Spring Boot ecosystem.
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
mvn test
```

---

## 2. Root URL Timeout on Production
### 🔴 The Problem
Users accessing the root domain `http://18.220.100.100.nip.io` experienced a "Connection timed out" error, while accessing `/login` directly worked fine.

### 🔍 Root Cause
1. React Router rendered the Dashboard (`/`) immediately for unauthenticated users.
2. The Dashboard attempted to fetch data (`fetchCalendars()`) without a valid JWT.
3. The browser stalled waiting for the API timeout (30-60s) before the `ProtectedRoute` logic could trigger the redirect to `/login`.

### ✅ The Solution
Updated the routing logic to make `/login` the default landing page for unauthenticated sessions, preventing the premature API calls entirely.

---

## 3. AWS RDS Password Authentication Failure
### 🔴 The Problem
Spring Boot app couldn't connect to AWS RDS MariaDB: `Access denied for user 'admin'`.

### 🔍 Root Cause
The password in `.env` was `#Admin2204`. The `#` character was interpreted as a comment start in the environment file parser, causing the actual password sent to be empty.

### ✅ The Solution
Wrapped the password in double quotes: `SPRING_DATASOURCE_PASSWORD="#Admin2204"`.

---

## 4. EC2 Instance Memory Exhaustion
### 🔴 The Problem
The `t2.micro` EC2 instance (1GB RAM) froze completely during Docker builds.

### 🔍 Root Cause
Running Maven build (~600MB) and Node.js build (~500MB) simultaneously exceeded physical RAM.

### ✅ The Solution
Implemented a 2GB swap file on the EC2 instance to provide virtual memory for peak usage during builds.
```bash
sudo fallocate -l 2G /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
```

---

## 5. CORS Blocking on Cloud Domain
### 🔴 The Problem
Frontend on `nip.io` domain couldn't communicate with Backend; requests stuck in "pending".

### 🔍 Root Cause
Spring Security CORS config only allowed `localhost`.

### ✅ The Solution
Updated `SecurityConfig.java` to explicitly allow the production origin:
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173", 
    "http://18.220.100.100.nip.io"
));
```

---

## 6. Port 8080 Blocking
### 🔴 The Problem
Users on corporate/school networks couldn't access the backend API on port 8080.

### 🔍 Root Cause
Non-standard ports like 8080 are often blocked by firewalls.

### ✅ The Solution
Implemented **Nginx Reverse Proxy** to route all traffic (frontend and API) through standard Port 80.
- Frontend: `http://domain/`
- Backend: `http://domain/api` -> proxies to `backend:8080`

---

## 7. React SPA Routing 404s
### 🔴 The Problem
Refreshing pages like `/register` returned 404 errors from Nginx.

### 🔍 Root Cause
Nginx looked for physical files matching the route. SPAs need all routes to fallback to `index.html`.

### ✅ The Solution
Added `try_files` directive to Nginx config:
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

---

## 8. Docker Build Args & Vite
### 🔴 The Problem
Frontend couldn't connect to the correct API URL in production despite setting env vars.

### 🔍 Root Cause
Vite embeds environment variables at **build time**, not runtime. Docker environment variables set at runtime were ignored.

### ✅ The Solution
Used Docker build arguments (`ARG`) to pass the API URL during the `docker build` phase so Vite could bake it into the static files.

---

## 9. Google OAuth Redirect Mismatch
### 🔴 The Problem
Google OAuth redirected users to `localhost` even when they initiated login from production.

### 🔍 Root Cause
The `redirect_uri` was hardcoded in `application.properties`.

### ✅ The Solution
Externalized the configuration to use environment variables (`GOOGLE_REDIRECT_URI`) passed via `docker-compose.yml`.

---

## 10. Database Emoji Support
### 🔴 The Problem
Saving events with emojis (e.g., "👋 Team Meeting") caused fatal database errors.

### 🔍 Root Cause
Database tables were using `latin1` or standard `utf8` (which is actually 3-byte UTF-8 in MySQL), not supporting 4-byte characters like emojis.

### ✅ The Solution
Migrated database charset to `utf8mb4` and ensured JDBC connection string included `useUnicode=true&characterEncoding=UTF-8`.

---

## 11. Silent Form Validation
### 🔴 The Problem
Registration form would sometimes simply "do nothing" when clicked.

### 🔍 Root Cause
React Hook Form validation failures were blocking submission, but the UI had no error message placeholders.

### ✅ The Solution
Added explicit error message rendering (red text) below form fields to give immediate user feedback.

---

## 12. Docker Layer Caching
### 🔴 The Problem
Deployments didn't reflect latest code changes.

### 🔍 Root Cause
Docker re-used cached layers because it didn't detect changes in the file copy step accurately or file timestamps were preserved.

### ✅ The Solution
Established a deployment routine using `--no-cache` for clean builds when necessary:
`docker-compose build --no-cache`
