# Library Management System Setup Guide

## Prerequisites
- Java 17 or higher
- Maven
- Git (optional)

## Project Structure
The application consists of two main components:
- Backend (Spring Boot)
- Frontend (JavaFX)

## Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Build the project:
```bash
./mvnw clean install
```

3. Run the Spring Boot application:
```bash
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8090`

## Frontend Setup

1. Navigate to the JavaFX UI directory:
```bash
cd "JavaFX UI"
```

2. Build the project:
```bash
./mvnw clean install
```

3. Run the JavaFX application:
```bash
./mvnw javafx:run
```

## Configuration Details

### Backend Configuration
The backend uses the following configuration (from `application.properties`):
```properties
server.port=8090
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Frontend Configuration
The frontend is configured to connect to the backend at `http://localhost:8090/api/books`

## Testing the Application

1. The H2 console is available at `http://localhost:8090/h2-console`
2. The frontend UI will display a table of books with CRUD operations
3. Basic operations available:
    - Add new books
    - Update existing books
    - Delete books
    - View paginated list of books

## Troubleshooting

- Ensure both applications are running simultaneously
- Check if port 8090 is available for the backend
- Verify Java 17 is installed and configured correctly
- Ensure all Maven dependencies are properly downloaded

## Notes

- The frontend uses JavaFX 17.0.1
- The backend uses Spring Boot with JPA and H2 database
- All data is stored in-memory and will be reset when the backend is restarted