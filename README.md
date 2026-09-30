# Backend Project

This is the backend service for the application, built with Spring Boot.

## Prerequisites

- **Java 17** must be installed on your system.

## Setup Instructions

1. **Clone the repository and navigate to the project folder:**
   ```bash
   git clone https://github.com/dvoron/backend.git
   cd backend/backend
   ```

3. **Run the Application:**
   You do not need to install Maven globally, as the project includes the Maven wrapper. Run one of the following commands depending on your operating system:

   **Linux/macOS:**
   ```bash
   ./mvnw spring-boot:run
   ```

   **Windows:**
   ```cmd
   mvnw.cmd spring-boot:run
   ```

## Development Notes

- **Database:** The application uses an **H2 database** that is configured to persist data to a local file (`./data/testdb`). Your data will be preserved between application restarts.
- **Port:** By default, the application runs on port `8080` (`http://localhost:8080`).

## Related Projects

- **[Frontend Project](https://github.com/dvoron/frontend)** - The Vue.js user interface.
- **[E2E Project](https://github.com/dvoron/E2E)** - End-to-End Playwright tests.