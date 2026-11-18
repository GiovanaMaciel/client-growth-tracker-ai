# Client Growth Tracker

### Business Intelligence & AI-Powered Restaurant Analytics

Client Growth Tracker is a full-stack web application developed for a marketing agency specializing in restaurant growth and performance.

The platform centralizes financial and operational data from food delivery marketplaces, providing interactive dashboards, executive reports, and a foundation for AI-powered business insights.

The project aims to replace manual spreadsheet-based reporting with a scalable data management and analytics solution.

## Project Overview

Marketing agencies working with restaurants often need to collect performance data from multiple delivery platforms.

This process typically involves:

- Accessing different marketplace dashboards.
- Manually collecting financial and operational metrics.
- Consolidating information into spreadsheets.
- Calculating performance indicators.
- Preparing reports for client meetings.

Client Growth Tracker addresses these challenges by centralizing restaurant data and automating reporting, with marketplace API integrations and AI-powered analytics being developed progressively.

## Main Features

### Restaurant Management

- Restaurant registration and management.
- Role-based authentication and authorization.
- Administrative access to all registered restaurants.
- Individual restaurant accounts with restricted data access.
- Restaurant deletion restricted to administrators.

### Financial Analytics

- Gross revenue tracking.
- Net revenue tracking.
- Order volume monitoring.
- Average ticket calculation.
- Marketplace performance comparison.
- Revenue distribution by sales channel.
- Financial indicators organized by reporting period.

### Conversion Funnel Analytics

- Marketplace conversion funnel visualization.
- Funnel stage tracking.
- Conversion rate calculations.
- Identification of potential conversion bottlenecks.

### Paid Traffic Analytics

- Advertising investment tracking.
- Reach and impressions.
- Attributed purchases.
- Return on Ad Spend (ROAS).
- Cost per Acquisition (CPA).
- Advertising frequency.

### Dashboards and Reports

- Interactive dashboards.
- Restaurant and reporting period filters.
- Financial and operational KPIs.
- Data visualization with charts.
- Executive PDF report generation.

## Technology Stack

### Backend

- Java 25
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- REST APIs
- PostgreSQL
- Flyway
- Maven

### Frontend

- React
- TypeScript
- Vite
- TanStack Query
- Axios
- Recharts

### Development Tools

- Git and GitHub
- Eclipse IDE
- PostgreSQL / pgAdmin
- iFood Developer Portal

## Marketplace API Integrations

The project is designed to integrate data from multiple food delivery marketplaces.

| Platform | Integration Status | Description                                                                                             |
| -------- | ------------------ | ------------------------------------------------------------------------------------------------------- |
| iFood    | In Progress        | OAuth 2.0 authentication implemented and successfully validated using iFood Developer test credentials. |
| 99 Food  | Pending            | API access and integration depend on platform approval.                                                 |
| Anota Aí | Pending            | API access and integration depend on platform approval.                                                 |

### iFood API Integration

The backend includes an integration with the iFood Developer Authentication API.

Implemented components:

- OAuth 2.0 Client Credentials authentication.
- Secure configuration using environment variables.
- HTTP communication using Spring RestClient.
- Access token retrieval.
- In-memory token caching.
- Token renewal before expiration.
- Authentication validation against the iFood API.

**Current status:** Authentication is functional and has been successfully tested.

### 99 Food and Anota Aí

Both platforms are planned integrations.

While API access and approval remain pending, data can be entered manually through the application.

This allows restaurants to use the platform without depending on external API availability.

## Artificial Intelligence Roadmap

One of the primary goals of Client Growth Tracker is to transform operational data into actionable business intelligence.

The AI layer is planned to include:

### AI-Generated Business Insights

Use Large Language Models (LLMs) to analyze structured restaurant performance data and generate:

- Performance summaries.
- Revenue trend analysis.
- Conversion funnel observations.
- Paid traffic performance insights.
- Potential operational issues.
- Data-supported improvement recommendations.

### Intelligent Executive Reports

Integrate AI-generated insights into executive reports to provide additional context beyond traditional financial metrics.

### Future Capabilities

- Historical trend analysis.
- Performance anomaly detection.
- Revenue forecasting.
- Automated business recommendations.
- AI-assisted performance analysis.

AI-generated conclusions will be grounded in stored metrics and reviewed before being presented to clients.

**Status:** Planned / In Development.

## Application Architecture

The application follows a layered backend architecture:

Controller → Service → Repository → JPA → PostgreSQL

The frontend communicates with the backend through REST APIs.

Main components:

- React frontend for dashboards and data management.
- Spring Boot backend for business logic and API endpoints.
- PostgreSQL for persistent data storage.
- Flyway for database migrations.
- Marketplace integration services for external data collection.
- Planned AI services for analytical insight generation.

## Data Management

The application currently supports manual data entry and editing.

Data is organized by:

- Restaurant.
- Reporting period.
- Marketplace channel.
- Financial metrics.
- Conversion funnel metrics.
- Paid traffic metrics.

Calculated KPIs are derived from the registered operational data.

Future API integrations will progressively reduce manual data collection.

## Local Development

### Requirements

- Java 25
- Maven
- PostgreSQL
- Node.js and npm
- Git

### Environment Variables

Configure the required environment variables before starting the backend.

| Variable            | Description                               |
| ------------------- | ----------------------------------------- |
| DB_PASSWORD         | PostgreSQL database password              |
| IFOOD_CLIENT_ID     | iFood Developer application client ID     |
| IFOOD_CLIENT_SECRET | iFood Developer application client secret |

Use the centralized iFood test application credentials during development.

Never commit credentials, access tokens, or sensitive restaurant data to version control.

### Backend

1. Configure PostgreSQL.
2. Review the database connection settings in `application.properties`.
3. Configure the required environment variables.
4. Start the Spring Boot application.
5. Flyway will apply any pending database migrations.

### Frontend

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

## Development Roadmap

- [x] Initial database schema
- [x] Spring Boot backend structure
- [x] Database migrations with Flyway
- [x] Authentication and role-based authorization
- [x] Restaurant management
- [x] Reporting period management
- [x] Marketplace channel management
- [x] Financial metrics management
- [x] Conversion funnel metrics
- [x] Paid traffic metrics
- [x] Interactive dashboards
- [x] Executive PDF reports
- [x] iFood OAuth 2.0 authentication
- [ ] iFood Analytics API data retrieval
- [ ] Automated iFood data synchronization
- [ ] iFood production homologation
- [ ] 99 Food API integration
- [ ] Anota Aí API integration
- [ ] LLM-powered business insights
- [ ] AI-enhanced executive reports
- [ ] Predictive analytics and forecasting

## Project Status

**Active Development**

The application currently provides restaurant management, structured data storage, dashboards, and executive reporting.

Marketplace integrations and AI-powered analytics are the next major development priorities.
