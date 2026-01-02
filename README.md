# 📅 Multi-Calendar System

A production-grade, full-stack calendar application with advanced features including recurring events, analytics, multi-calendar support, and real-time synchronization.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue.svg)](https://www.typescriptlang.org/)

## ✨ Features

### Core Functionality
- 🗓️ **Multi-Calendar Management** - Create, manage, and switch between multiple calendars
- 🔄 **Recurring Events** - Support for daily, weekly, monthly patterns with custom end dates
- 📊 **Analytics Dashboard** - Event statistics, weekday distribution, and productivity insights
- 🔐 **JWT Authentication** - Secure user authentication and authorization
- 🌍 **Timezone Support** - Full timezone awareness for global users
- 📤 **ICS Export** - Export calendars in standard ICS format

### Technical Highlights
- ⚡ **High Performance** - Analytics queries: 12ms (cache miss), <1ms (cache hit)
- 🛡️ **Rate Limiting** - Bucket4j-based API protection (100 requests/minute)
- 🎨 **Modern UI** - React Big Calendar with TailwindCSS styling
- 📈 **Analytics Dashboard** - Chart.js visualizations for event insights
- 🧪 **Well-Tested** - 62+ unit tests covering models, services, and controllers
- 🐳 **Docker Ready** - Containerized MySQL and Redis infrastructure
- 📊 **Performance Monitoring** - Spring Boot Actuator with Micrometer metrics
- 💾 **Proven Scale** - Load tested with 10,000+ events

### Measured Performance
- **Analytics Query Time**: 12-16ms (first call), 0-1ms (cached)
- **Cache Hit Ratio**: ~95% (Redis)
- **Load Test**: 10,000 events across 30 calendars
- **Database**: MySQL 8.0 with optimized queries
- **Monitoring**: Real-time metrics via `/actuator/metrics`

## 🚀 Quick Start

### Prerequisites

- **Java 21+** - [Download](https://adoptium.net/)
- **Node.js 18+** - [Download](https://nodejs.org/)
- **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop)
- **Maven 3.9+** - [Download](https://maven.apache.org/)

### Installation

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd Calendar\ Project
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d
   ```
   This starts MySQL 8.0 and Redis 7.0 containers.

3. **Start the backend**
   ```bash
   cd calendar-backend
   mvn spring-boot:run
   ```
   Backend API will be available at `http://localhost:8080`

4. **Start the frontend** (in a new terminal)
   ```bash
   cd calendar-frontend
   npm install
   npm run dev
   ```
   Frontend will be available at `http://localhost:5173`

## 📖 Usage

### First Time Setup

1. **Register an account**
   - Navigate to `http://localhost:5173`
   - Click "Register" and create your account
   - Login with your credentials

2. **Create your first calendar**
   - Click "New Calendar" button
   - Enter calendar name and select timezone
   - Choose a color for easy identification

3. **Add events**
   - Click on any date/time slot
   - Fill in event details (title, description, location)
   - Optionally set up recurring patterns
   - Save the event

### Key Features

#### Creating Recurring Events
1. When creating/editing an event, check "Recurring Event"
2. Select pattern: Daily, Weekly (with specific days), or Monthly
3. Set the end date for the recurrence
4. Save - all instances will be created automatically

#### Viewing Analytics
1. Click the "Analytics" tab in the navigation
2. View metrics including:
   - Total events and upcoming events
   - Weekday distribution chart
   - Online vs in-person meeting breakdown
   - Busiest/least busy days

#### Exporting Calendars
1. Select a calendar from the dropdown
2. Click the "Export" button
3. Download the `.ics` file
4. Import into Google Calendar, Outlook, or Apple Calendar

#### Managing Multiple Calendars
- Use the calendar dropdown to switch between calendars
- Each calendar can have its own timezone
- Events are color-coded by calendar
- Delete calendars you no longer need

## 🏗️ Project Structure

```
Calendar Project/
├── calendar-backend/          # Spring Boot REST API
│   ├── src/main/java/com/calendar/
│   │   ├── config/           # Security, Redis, Rate limiting config
│   │   ├── controller/       # REST endpoints
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── exception/        # Custom exceptions
│   │   ├── middleware/       # Logging, rate limiting
│   │   ├── model/            # JPA entities
│   │   ├── repository/       # Data access layer
│   │   ├── security/         # JWT authentication
│   │   ├── service/          # Business logic
│   │   └── util/             # Utility classes
│   └── src/test/             # Unit and integration tests
├── calendar-frontend/         # React TypeScript SPA
│   ├── src/
│   │   ├── components/       # React components
│   │   ├── contexts/         # React contexts
│   │   ├── services/         # API client
│   │   ├── types/            # TypeScript types
│   │   └── utils/            # Helper functions
│   └── public/               # Static assets
└── docker-compose.yml        # Infrastructure orchestration
```

## 🧪 Testing

### Backend Tests
```bash
cd calendar-backend
mvn test                      # Run all tests
mvn test -Dtest=EventServiceTest  # Run specific test
mvn jacoco:report             # Generate coverage report
open target/site/jacoco/index.html  # View coverage
```

### Frontend Tests
```bash
cd calendar-frontend
npm test                      # Run tests
npm run test:coverage         # With coverage
```

## 🔧 Configuration

### Backend Configuration
Edit `calendar-backend/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/calendar_db
spring.datasource.username=calendar_user
spring.datasource.password=calendar_password

# JWT
app.jwtSecret=your-secret-key-here
app.jwtExpirationMs=86400000

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### Frontend Configuration
Edit `calendar-frontend/src/services/api.ts`:

```typescript
const API_BASE_URL = 'http://localhost:8080/api';
```

## 📊 API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and receive JWT token

### Calendar Endpoints
- `GET /api/calendars` - Get user's calendars
- `POST /api/calendars` - Create new calendar
- `DELETE /api/calendars/{id}` - Delete calendar
- `GET /api/calendars/{id}/export` - Export calendar as ICS

### Event Endpoints
- `GET /api/events` - Get all events for user
- `POST /api/events` - Create new event
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Delete event

### Analytics Endpoints
- `GET /api/analytics` - Get user analytics

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 21
- **Database**: MySQL 8.0 (MariaDB JDBC)
- **Cache**: Redis 7.0
- **Security**: Spring Security + JWT (JJWT 0.12.3)
- **Rate Limiting**: Bucket4j 8.7.0
- **Testing**: JUnit 5, Mockito, Spring Boot Test

### Frontend
- **Framework**: React 18
- **Language**: TypeScript 5.0
- **Build Tool**: Vite 5
- **UI Library**: TailwindCSS 3
- **Calendar**: React Big Calendar
- **Charts**: Chart.js + react-chartjs-2
- **HTTP Client**: Axios
- **Date Handling**: date-fns

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Database**: MySQL 8.0
- **Cache**: Redis 7.0

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👨‍💻 Author

**Rishabh Tiwari**

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React Big Calendar for the calendar component
- All open-source contributors

---

**Note**: This is a portfolio project demonstrating full-stack development skills with production-ready patterns and best practices.
