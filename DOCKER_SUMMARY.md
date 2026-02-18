# Docker Configuration Summary

## Files Created

### 1. Dockerfiles (5 files)
Each service has its own multi-stage Dockerfile for optimized builds:

#### `Dockerfile-service-registry`
- **Base Image:** eclipse-temurin:17-jre-alpine
- **Port:** 8761
- **Purpose:** Eureka Service Discovery
- **Health Check:** Yes

#### `Dockerfile-auth-service`
- **Base Image:** eclipse-temurin:17-jre-alpine
- **Port:** 8083
- **Purpose:** JWT Authentication Service
- **Health Check:** Yes
- **Dependencies:** Service Registry

#### `Dockerfile-api-gateway`
- **Base Image:** eclipse-temurin:17-jre-alpine
- **Port:** 8020
- **Purpose:** API Gateway (Spring Cloud Gateway)
- **Health Check:** Yes
- **Dependencies:** Service Registry

#### `Dockerfile-product-catalog`
- **Base Image:** eclipse-temurin:17-jre-alpine
- **Port:** 8081
- **Purpose:** Product Catalog Service
- **Health Check:** Yes
- **Dependencies:** PostgreSQL, Service Registry
- **Database:** productcatalog

#### `Dockerfile-order-management`
- **Base Image:** eclipse-temurin:17-jre-alpine
- **Port:** 8082
- **Purpose:** Order Management Service
- **Health Check:** Yes
- **Dependencies:** PostgreSQL, Service Registry
- **Database:** ordermanagement

### 2. Docker Compose (`docker-compose.yml`)
Orchestrates all services with:
- Service dependencies
- Health checks
- Environment variables
- Port mappings
- Volume management
- Network configuration

**Services included:**
- PostgreSQL (5432)
- Service Registry (8761)
- Auth Service (8083)
- Product Catalog Service (8081)
- Order Management Service (8082)
- API Gateway (8020)

### 3. Database Initialization (`init-databases.sql`)
SQL script that:
- Creates `productcatalog` database
- Creates `ordermanagement` database
- Grants privileges to postgres user
- Runs automatically on PostgreSQL startup

### 4. Documentation (`DOCKER_DEPLOYMENT_GUIDE.md`)
Comprehensive guide including:
- Prerequisites and architecture overview
- Service ports and database mapping
- Step-by-step deployment instructions
- Docker commands reference
- Troubleshooting guide
- Performance tuning tips
- Production considerations

### 5. Helper Scripts

#### `docker-start.sh` (Linux/macOS)
Interactive menu-driven script for:
- Building and starting services
- Viewing status and logs
- Stopping services
- Running tests
- Cleanup

#### `docker-start.bat` (Windows)
Windows batch equivalent with:
- Service management options
- Log viewing
- Status checks

### 6. Build Optimization (`.dockerignore`)
Excludes unnecessary files from Docker builds:
- Git files
- Build artifacts
- IDE configuration
- Documentation
- CI/CD files

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                  Docker Network: microservices           │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ┌─────────────┐         API Gateway (8020)            │
│  │  PostgreSQL │         │                              │
│  │  (5432)     │         ├─→ Service Registry (8761)   │
│  │             │         │        │                     │
│  │ Products    │         ├────────┼────────┐            │
│  │ Orders      │         │        │        │            │
│  └─────────────┘    Auth (8083) Product Order          │
│                          (8081)   (8082)               │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## Quick Start Commands

### Build Everything
```bash
docker-compose build
```

### Start All Services
```bash
docker-compose up -d
```

### Check Status
```bash
docker-compose ps
```

### View Logs
```bash
docker-compose logs -f
```

### Stop Everything
```bash
docker-compose stop
```

### Remove Everything
```bash
docker-compose down -v
```

---

## Environment Variables

All services use the following environment variables:

| Variable | Default Value | Used By |
|----------|---------------|---------|
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | http://service-registry:8761/eureka | All microservices |
| `JWT_SECRET` | MySecretKeyForJwtTokenGenerationThatIsAtLeast256BitsLongForHs256AlgorithmSecurity | Auth, Gateway, All Services |
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://postgres:5432/{dbname} | Product, Order Services |
| `SPRING_DATASOURCE_USERNAME` | postgres | Product, Order Services |
| `SPRING_DATASOURCE_PASSWORD` | postgres | Product, Order Services |
| `SPRING_APPLICATION_NAME` | {service-name} | All services |

---

## Port Mapping

| Service | Container Port | Host Port | Protocol |
|---------|-----------------|-----------|----------|
| API Gateway | 8020 | 8020 | HTTP |
| Product Catalog | 8081 | 8081 | HTTP |
| Order Management | 8082 | 8082 | HTTP |
| Auth Service | 8083 | 8083 | HTTP |
| Eureka Registry | 8761 | 8761 | HTTP |
| PostgreSQL | 5432 | 5432 | TCP |

---

## Health Checks

All services have health checks configured to:
- Start checking after 40 seconds
- Check every 30 seconds
- Timeout after 10 seconds
- Retry up to 3 times before marking unhealthy

```yaml
healthcheck:
  test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:{port}/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

---

## Database Setup

PostgreSQL automatically initializes with:
- **Two databases:**
  - `productcatalog` - Used by Product Catalog Service
  - `ordermanagement` - Used by Order Management Service
- **User:** postgres
- **Password:** postgres
- **Persistent storage:** `postgres_data` volume

Hibernate will automatically create/update tables on first run.

---

## Security Notes

⚠️ **Important for Production:**

1. **Change Default Passwords:**
   - Update PostgreSQL password in `docker-compose.yml`
   - Use strong JWT_SECRET

2. **Use Environment Files:**
   - Create `.env` file with sensitive data
   - Never commit secrets to repository

3. **Enable SSL/TLS:**
   - Configure HTTPS for all services
   - Use certificate management tools

4. **Network Security:**
   - Implement firewall rules
   - Use private networks where possible
   - Restrict external access

---

## Troubleshooting Quick Links

| Issue | Solution |
|-------|----------|
| Port already in use | Change port in docker-compose.yml |
| Services not connecting | Check network: `docker network inspect microservices` |
| Database errors | Check: `docker-compose logs postgres` |
| Token errors | Verify JWT_SECRET matches across services |
| Slow startup | Increase wait time in docker-compose.yml |

---

## Performance Optimization Tips

1. **Use Alpine images** (already done) - Smaller, faster
2. **Multi-stage builds** (already done) - Reduces image size
3. **Resource limits** - Add limits in docker-compose.yml
4. **Caching** - Maven caches dependencies between builds
5. **Volume optimization** - Use named volumes for data

---

## Next Steps

1. **Run the services:**
   ```bash
   docker-compose up -d
   ```

2. **Verify deployment:**
   ```bash
   docker-compose ps
   ```

3. **Test endpoints:**
   ```bash
   curl http://localhost:8020/api/v1/auth/login
   ```

4. **Monitor logs:**
   ```bash
   docker-compose logs -f
   ```

---

## Support

For more information, see:
- `DOCKER_DEPLOYMENT_GUIDE.md` - Detailed deployment guide
- Docker Documentation: https://docs.docker.com/
- Spring Boot Docker Guide: https://spring.io/guides/gs/spring-boot-docker/

---

**Last Updated:** February 18, 2026
