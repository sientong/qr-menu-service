# QR Menu Service

A secure and scalable service for managing restaurant menus with QR code integration, stock management, and real-time analytics.

## Features

- **Menu Management**
  - Create and manage digital menus
  - Generate QR codes for menu access
  - Categorize menu items
  - Support for multiple languages

- **Stock Management**
  - Real-time stock tracking
  - Stock valuation and analytics
  - Low stock alerts
  - Stock history and trends

- **Security**
  - Token-based authentication with Redis
  - Rate limiting protection
  - Session management
  - Security event logging
  - Protection against common vulnerabilities (SQL injection, XSS, etc.)

- **Analytics**
  - Stock trend analysis
  - Usage statistics
  - Performance metrics
  - Security event monitoring

## Technology Stack

- Java 17
- Spring Boot 3.2.3
- PostgreSQL
- Redis
- Flyway for database migrations
- Spring Security
- OpenAPI/Swagger for documentation

## Prerequisites

- JDK 17 or higher
- Maven 3.8+
- PostgreSQL 13+
- Redis 6+
- Docker (optional)

## Getting Started

1. Clone the repository:

bash
git clone https://github.com/yourusername/qr-menu-service.git
cd qr-menu-service

2. Configure the application:
   Create `application-local.properties` in `src/main/resources` with your local configuration:

spring.datasource.url=jdbc:postgresql://localhost:5432/qrmenu
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.redis.host=localhost
spring.redis.port=6379

3. Build the project:

bash
mvn clean install

4. Run the application:

bash
mvn spring-boot:run -Dspring.profiles.active=local

## API Documentation

The API documentation is available through Swagger UI at:

http://localhost:8080/swagger-ui.html

## Security Features

### Authentication
- Token-based authentication using Redis
- Session management with automatic expiration
- Concurrent session control
- Role-based access control (RESTAURANT_ADMIN, RESTAURANT_MANAGER, etc.)

### Protection Mechanisms
- Rate limiting per IP and endpoint
- SQL injection prevention
- XSS protection
- CSRF protection
- Path traversal prevention
- Request header injection prevention

### Monitoring
- Security event logging
- Authentication success/failure tracking
- Rate limit violation monitoring
- Session management events

## Testing

Run the tests using:

bash
mvn test

Different test categories:
- Unit tests
- Integration tests
- Security vulnerability tests
- Edge case tests
- Performance tests

## Docker Support

Build the Docker image:

bash
docker build -t qr-menu-service .

Run with Docker Compose:

bash
docker-compose up

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please open an issue in the GitHub repository or contact the maintainers.

## Acknowledgments

- Spring Boot team for the excellent framework
- The open-source community for various libraries used in this project

