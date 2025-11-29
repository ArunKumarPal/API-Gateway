#<div align="center">

# 🚀 Spring Cloud API Gateway

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen?style=flat&logo=spring)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.2-blue?style=flat&logo=spring)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**A production-ready, enterprise-grade API Gateway built with Spring Cloud Gateway**

[Features](#-features) • [Quick Start](#-quick-start) • [Configuration](#️-configuration) • [Architecture](#-architecture) • [Documentation](#-documentation)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Configuration](#️-configuration)
- [Project Structure](#-project-structure)
- [Route Configuration](#-route-configuration)
- [Security](#-security)
- [Rate Limiting](#-rate-limiting)
- [Monitoring & Observability](#-monitoring--observability)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)

## 🌟 Overview

This API Gateway serves as a single entry point for microservices architecture, providing routing, security, rate limiting, and observability features. Built on Spring Cloud Gateway's reactive foundation, it delivers high performance and scalability for modern distributed systems.

### Key Capabilities

- ✅ **Dynamic Route Management** - YAML-based route definitions with hot-reload support
- ✅ **JWT Authentication** - Multi-tenant JWT validation with JWK support
- ✅ **Rate Limiting** - Redis-backed distributed rate limiting
- ✅ **Observability** - OpenTelemetry integration with Prometheus metrics
- ✅ **Caching** - Caffeine-based in-memory caching for improved performance
- ✅ **Reactive Architecture** - Non-blocking I/O for maximum throughput

## ✨ Features

### 🔐 Security
- **JWT Token Validation** - Support for multiple JWT issuers and audiences
- **JWK Set Integration** - Automatic public key fetching and rotation
- **Custom Token Filters** - Pre-built filters for authentication and authorization
- **OAuth 2.0 Resource Server** - Standards-compliant security implementation

### 🚦 Traffic Management
- **Intelligent Routing** - Path-based, header-based, and custom predicates
- **Load Balancing** - Client-side load balancing with Spring Cloud LoadBalancer
- **Circuit Breaking** - Resilience4j integration for fault tolerance
- **Request Rate Limiting** - IP-based and user-based rate limiting strategies

### 📊 Monitoring & Observability
- **Prometheus Metrics** - Production-ready metrics export
- **OpenTelemetry** - Distributed tracing with OTLP exporter
- **Health Checks** - Spring Boot Actuator endpoints
- **Structured Logging** - Logstash-compatible JSON logging

### 🎯 Developer Experience
- **YAML-Based Routes** - Easy-to-maintain route configurations
- **Environment-Specific Routes** - Folder-based route organization
- **Hot Reload** - Dynamic route updates without restart
- **Comprehensive Testing** - Unit and integration test support

## 🛠 Technology Stack

| Category | Technology |
|----------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.3.2, Spring Cloud Gateway |
| **Security** | Spring Security OAuth2, JWT (Auth0) |
| **Caching** | Redis (Reactive), Caffeine |
| **Monitoring** | Micrometer, Prometheus, OpenTelemetry |
| **Logging** | Logback, Logstash Encoder |
| **Build Tool** | Maven 3.x |
| **Testing** | JUnit 5, Reactor Test, JaCoCo |

## 📦 Prerequisites

Before running this application, ensure you have:

- **Java 21** or higher ([Download](https://adoptium.net/))
- **Maven 3.6+** (or use included Maven Wrapper)
- **Redis 6.0+** (for rate limiting and caching)
- **Docker** (optional, for containerized deployment)

### Environment Variables

Configure the following environment variables:

```bash
# JWT Configuration - Primary
JWK_SET_URL=https://your-auth-provider.com/.well-known/jwks.json
AUTH_ISSUER=https://your-auth-provider.com/
AUTH_AUDIENCE=your-api-audience

# JWT Configuration - Secondary (Optional)
JWK_SET_URL_2=https://another-auth-provider.com/.well-known/jwks.json
AUTH_ISSUER_2=https://another-auth-provider.com/
AUTH_AUDIENCE_2=another-api-audience
```

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/ArunKumarPal/API-Gateway.git
cd API-Gateway
```

### 2. Start Redis

```bash
# Using Docker
docker run -d -p 6379:6379 --name redis redis:latest

# Or using Docker Compose
docker-compose up -d redis
```

### 3. Set Environment Variables

**Windows (PowerShell):**
```powershell
$env:JWK_SET_URL="https://your-auth-provider.com/.well-known/jwks.json"
$env:AUTH_ISSUER="https://your-auth-provider.com/"
$env:AUTH_AUDIENCE="your-api-audience"
```

**Linux/Mac:**
```bash
export JWK_SET_URL="https://your-auth-provider.com/.well-known/jwks.json"
export AUTH_ISSUER="https://your-auth-provider.com/"
export AUTH_AUDIENCE="your-api-audience"
```

### 4. Build the Application

```bash
# Using Maven Wrapper (recommended)
./mvnw clean install

# Or using system Maven
mvn clean install
```

### 5. Run the Application

```bash
# Using Maven Wrapper
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/gateway-0.0.1-SNAPSHOT.jar
```

The gateway will start on `http://localhost:8080` (default port).

## ⚙️ Configuration

### Application Configuration (`application.yml`)

```yaml
spring:
  main:
    web-application-type: reactive
  data:
    redis:
      host: localhost
      port: 6379

token:
  jwt:
    default:
      jwk-set-url: ${JWK_SET_URL}
      claims-validators:
        issuer: ${AUTH_ISSUER}
        audience: ${AUTH_AUDIENCE}

routes:
  folder: env  # Route configuration folder

ip:
  based:
    rate-limit: 200  # Requests per minute
```

### Custom Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `routes.folder` | Folder name under `resources/routes/` for route definitions | `env` |
| `ip.based.rate-limit` | Default IP-based rate limit (requests/min) | `200` |
| `spring.data.redis.host` | Redis server hostname | `localhost` |
| `spring.data.redis.port` | Redis server port | `6379` |

## 📁 Project Structure

```
API-Gateway/
├── src/
│   ├── main/
│   │   ├── java/com/arun/sample/gateway/
│   │   │   ├── GatewayApplication.java          # Main application
│   │   │   ├── config/
│   │   │   │   ├── CustomRouteDefinitionLocator.java   # Dynamic route loader
│   │   │   │   ├── JwkAuthProperties.java              # JWT configuration
│   │   │   │   └── YamlRouteDefinitionReader.java      # YAML parser
│   │   │   ├── constants/
│   │   │   │   ├── Constants.java                      # Application constants
│   │   │   │   └── ErrorCode.java                      # Error code definitions
│   │   │   ├── exception/
│   │   │   │   ├── ApiGatewayException.java           # Custom exceptions
│   │   │   │   └── TokenValidatorException.java
│   │   │   ├── filter/
│   │   │   │   ├── CustomKeyResolverConfiguration.java # Rate limit key resolver
│   │   │   │   ├── CustomRateLimiter.java             # Custom rate limiter
│   │   │   │   └── TokenValidationFilter.java         # JWT validation filter
│   │   │   └── model/
│   │   │       ├── JwkAuthInfo.java                   # JWT auth model
│   │   │       └── Pair.java                          # Utility model
│   │   └── resources/
│   │       ├── application.yml                   # Application configuration
│   │       └── routes/
│   │           └── env/
│   │               └── sampleroute.yml          # Route definitions
│   └── test/
│       └── java/com/arun/sample/gateway/
│           └── GatewayApplicationTests.java     # Integration tests
├── pom.xml                                      # Maven configuration
└── README.md                                    # This file
```

## 🛣 Route Configuration

Routes are defined in YAML files under `src/main/resources/routes/{folder}/`. The folder is configurable via `routes.folder` property.

### Example Route Definition

Create a file `src/main/resources/routes/env/my-service.yml`:

```yaml
- id: my-service                              # Unique route identifier
  uri: https://backend-service.example.com    # Target service URL
  predicates:
    - Path=/api/v1/users/**                   # URL pattern matching
    - Method=GET,POST                         # HTTP methods
  filters:
    - TokenValidationFilter                   # JWT validation
    - name: RequestRateLimiter                # Rate limiting
      args:
        redis-rate-limiter.replenishRate: 100
        redis-rate-limiter.burstCapacity: 150
        redis-rate-limiter.requestedTokens: 1
        key-resolver: "#{@userKeyResolver}"
        rate-limiter: "#{@customRateLimiter}"
    - StripPrefix=1                           # Remove /api prefix
```

### Available Predicates

- **Path**: `/api/v1/**` - Match URL path patterns
- **Method**: `GET`, `POST`, `PUT`, `DELETE` - Match HTTP methods
- **Header**: Match request headers
- **Query**: Match query parameters
- **Host**: Match hostname patterns
- **RemoteAddr**: Match client IP ranges

### Available Filters

- **TokenValidationFilter**: JWT token validation
- **RequestRateLimiter**: Rate limiting with Redis
- **StripPrefix**: Remove path prefix before forwarding
- **AddRequestHeader**: Add custom headers
- **AddResponseHeader**: Add response headers
- **RewritePath**: Rewrite request path
- **Retry**: Retry failed requests

## 🔒 Security

### JWT Token Validation

The gateway supports multiple JWT issuers for multi-tenant scenarios:

```yaml
token:
  jwt:
    default:                                    # Primary issuer
      jwk-set-url: https://auth.example.com/.well-known/jwks.json
      claims-validators:
        issuer: https://auth.example.com/
        audience: api-gateway
    another:                                    # Secondary issuer
      jwk-set-url: https://auth2.example.com/.well-known/jwks.json
      claims-validators:
        issuer: https://auth2.example.com/
        audience: api-gateway-v2
```

### Token Validation Flow

1. **Extract Token**: Extract JWT from `Authorization` header
2. **Fetch JWK**: Retrieve public keys from JWK Set URL
3. **Validate Signature**: Verify token signature using JWK
4. **Validate Claims**: Check issuer, audience, expiration
5. **Forward Request**: Add validated claims to request context

### Securing Routes

Add the `TokenValidationFilter` to any route requiring authentication:

```yaml
filters:
  - TokenValidationFilter
```

## 🚦 Rate Limiting

The gateway implements distributed rate limiting using Redis for consistent rate limit enforcement across multiple instances.

### Configuration

```yaml
ip:
  based:
    rate-limit: 200  # Default rate limit (requests/minute)
```

### Rate Limiting Strategies

#### 1. IP-Based Rate Limiting

```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 200      # Requests per second
      redis-rate-limiter.burstCapacity: 200      # Maximum burst size
      key-resolver: "#{@ipKeyResolver}"          # Use IP as key
```

#### 2. User-Based Rate Limiting

```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 100
      redis-rate-limiter.burstCapacity: 150
      key-resolver: "#{@userKeyResolver}"        # Use user ID as key
```

### Custom Rate Limiter

The `CustomRateLimiter` class provides enhanced rate limiting capabilities with custom logic and monitoring.

## 📊 Monitoring & Observability

### Actuator Endpoints

Access management endpoints:

- **Health**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`
- **Routes**: `http://localhost:8080/actuator/gateway/routes`

### Prometheus Metrics

Key metrics exposed:

- `http_server_requests_seconds` - Request duration
- `gateway_requests_total` - Total requests by route
- `gateway_requests_errors_total` - Errors by route
- `redis_rate_limiter_requests_allowed` - Rate limit statistics

### OpenTelemetry Tracing

The gateway includes OpenTelemetry Java agent for distributed tracing:

```bash
java -javaagent:target/javaagent/opentelemetry-javaagent.jar \
     -Dotel.service.name=api-gateway \
     -Dotel.exporter.otlp.endpoint=http://collector:4317 \
     -jar target/gateway-0.0.1-SNAPSHOT.jar
```

### Structured Logging

Logs are formatted in JSON for easy parsing:

```json
{
  "@timestamp": "2025-11-29T10:15:30.123Z",
  "level": "INFO",
  "logger": "com.arun.sample.gateway",
  "thread": "reactor-http-nio-2",
  "message": "Request routed successfully",
  "dd.trace_id": "1234567890",
  "dd.span_id": "9876543210"
}
```

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run with Code Coverage

```bash
./mvnw clean verify

# View coverage report
open target/site/jacoco/index.html
```

### Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTests {
    
    @Test
    void contextLoads() {
        // Test application context loads successfully
    }
}
```

## 🚢 Deployment

### Build Docker Image

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar
COPY target/javaagent/opentelemetry-javaagent.jar agent.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-javaagent:agent.jar", \
    "-Dotel.service.name=api-gateway", \
    "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t api-gateway:latest .
docker run -p 8080:8080 \
    -e JWK_SET_URL="https://auth.example.com/.well-known/jwks.json" \
    -e AUTH_ISSUER="https://auth.example.com/" \
    -e AUTH_AUDIENCE="api-gateway" \
    api-gateway:latest
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: JWK_SET_URL
          valueFrom:
            secretKeyRef:
              name: gateway-secrets
              key: jwk-set-url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

### Production Considerations

- **High Availability**: Deploy multiple instances behind a load balancer
- **Redis Cluster**: Use Redis Cluster for high availability
- **Resource Limits**: Set appropriate memory and CPU limits
- **Health Checks**: Configure liveness and readiness probes
- **Security**: Use secrets management for sensitive configuration
- **Monitoring**: Set up alerts for key metrics and error rates

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Coding Standards

- Follow Java code conventions
- Write unit tests for new features
- Maintain test coverage above 80%
- Update documentation for API changes

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For issues, questions, or suggestions:

- **Issues**: [GitHub Issues](https://github.com/ArunKumarPal/API-Gateway/issues)
- **Discussions**: [GitHub Discussions](https://github.com/ArunKumarPal/API-Gateway/discussions)

## 🙏 Acknowledgments

- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) team for the excellent framework
- [OpenTelemetry](https://opentelemetry.io/) community for observability standards
- All contributors who help improve this project

---

<div align="center">

**Made with ❤️ by [Arun Pal](https://github.com/ArunKumarPal)**

⭐ Star this repository if you find it helpful!

</div>

