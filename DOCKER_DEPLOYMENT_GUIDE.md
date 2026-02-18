# Docker Setup and Deployment Guide

This guide provides step-by-step instructions to run the entire microservices architecture using Docker and Docker Compose.

## Prerequisites

1. **Docker** (version 20.10 or later)
   - Download from: https://www.docker.com/products/docker-desktop

2. **Docker Compose** (version 1.29 or later)
   - Usually comes with Docker Desktop

3. **Git** (to clone the repository)

4. **At least 4GB RAM** available for Docker

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (8020)                     │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│              Service Registry / Eureka (8761)               │
└─────────────────────────────────────────────────────────────┘
        ↓                    ↓                    ↓
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│Auth Service  │      │ Product      │      │ Order Mgmt   │
│   (8083)     │      │  Catalog     │      │  Service     │
│              │      │  (8081)      │      │   (8082)     │
└──────────────┘      └──────────────┘      └──────────────┘
                              ↓
                      ┌──────────────┐
                      │  PostgreSQL  │
                      │   (5432)     │
                      └──────────────┘
```

## Services and Ports

| Service | Port | Database |
|---------|------|----------|
| API Gateway | 8020 | - |
| Product Catalog Service | 8081 | productcatalog |
| Order Management Service | 8082 | ordermanagement |
| Auth Service | 8083 | - |
| Service Registry (Eureka) | 8761 | - |
| PostgreSQL | 5432 | postgres |

---

## Step-by-Step Instructions

### Step 1: Verify Docker Installation

```bash
# Check Docker version
docker --version

# Check Docker Compose version
docker-compose --version

# Start Docker daemon (if not already running)
# On Windows/Mac: Open Docker Desktop
# On Linux: systemctl start docker
```

### Step 2: Clone the Repository

```bash
git clone <repository-url>
cd Microservices-Capstone-master
```

### Step 3: Verify Directory Structure

Ensure the following files exist in the root directory:

```
- docker-compose.yml
- Dockerfile-service-registry
- Dockerfile-auth-service
- Dockerfile-api-gateway
- Dockerfile-product-catalog
- Dockerfile-order-management
- init-databases.sql
- pom.xml (parent)
```

### Step 4: Build Docker Images

Build all microservices images:

```bash
# Build all images
docker-compose build

# Or build individual services
docker-compose build auth-service
docker-compose build product-catalog-service
docker-compose build order-management-service
docker-compose build api-gateway
docker-compose build service-registry
```

**Expected Output:**
```
Building auth-service
Building product-catalog-service
...
Successfully tagged <project-name>_auth-service:latest
```

### Step 5: Start All Services

```bash
# Start all services in detached mode
docker-compose up -d

# Or start with logs (useful for debugging)
docker-compose up
```

**Wait for initialization** (approximately 2-3 minutes for all services to be healthy)

### Step 6: Verify Services Health

```bash
# Check container status
docker-compose ps

# View logs of all services
docker-compose logs -f

# View logs of specific service
docker-compose logs -f auth-service
```

**Expected output for `docker-compose ps`:**
```
NAME                      COMMAND             STATUS
postgres_db              /bin/sh             Up (healthy)
service-registry         java -jar           Up (healthy)
auth-service             java -jar           Up (healthy)
product-catalog-service  java -jar           Up (healthy)
order-management-service java -jar           Up (healthy)
api-gateway              java -jar           Up (healthy)
```

### Step 7: Verify Eureka Service Discovery

```bash
# Open in browser or use curl
curl http://localhost:8761

# Or visit in browser:
# http://localhost:8761/
```

**Expected:** Eureka UI showing all registered services

### Step 8: Test Authentication

```bash
# Login with admin credentials
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Response format:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "username": "admin",
#   "role": "ADMIN"
# }
```

### Step 9: Test API Endpoints

**Get Products (Any authenticated user):**
```bash
TOKEN="<token-from-login>"

curl -X GET http://localhost:8020/api/v1/products \
  -H "Authorization: Bearer $TOKEN"
```

**Create Product (ADMIN only):**
```bash
curl -X POST http://localhost:8020/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sample Product",
    "description": "A sample product",
    "price": 99.99,
    "quantity": 10,
    "sku": "SKU-001"
  }'
```

**Get Orders (ADMIN only):**
```bash
curl -X GET http://localhost:8020/api/v1/orders \
  -H "Authorization: Bearer $TOKEN"
```

---

## Useful Docker Commands

### View Logs

```bash
# All services
docker-compose logs

# Specific service
docker-compose logs auth-service

# Follow logs in real-time
docker-compose logs -f api-gateway

# Last 100 lines
docker-compose logs --tail=100
```

### Access Container Shell

```bash
# Connect to a running container
docker-compose exec postgres psql -U postgres

# Connect to auth-service
docker-compose exec auth-service /bin/sh
```

### Restart Services

```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart product-catalog-service

# Restart and rebuild (if code changed)
docker-compose up -d --build
```

### Stop and Remove Services

```bash
# Stop all services (data persists)
docker-compose stop

# Stop and remove containers and networks
docker-compose down

# Remove everything including volumes (WARNING: deletes database data)
docker-compose down -v
```

### Check Resource Usage

```bash
# View container resource usage
docker stats

# View specific container
docker stats postgres_db
```

---

## Troubleshooting

### Issue 1: Services Won't Start

**Symptom:** Containers crash immediately

**Solution:**
```bash
# Check logs
docker-compose logs service-name

# Rebuild images
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Issue 2: Database Connection Errors

**Symptom:** `Connection refused` or `database does not exist`

**Solution:**
```bash
# Check PostgreSQL status
docker-compose ps postgres

# Check database
docker-compose exec postgres psql -U postgres -l

# Restart PostgreSQL
docker-compose restart postgres
```

### Issue 3: Port Already in Use

**Symptom:** `bind: address already in use`

**Solution:**
```bash
# Find process using port
lsof -i :8020  # (macOS/Linux)
netstat -ano | findstr :8020  # (Windows)

# Kill the process or change port in docker-compose.yml
```

### Issue 4: Services Not Discovering Each Other

**Symptom:** Services can't connect to each other

**Solution:**
```bash
# Check Eureka
curl http://localhost:8761/eureka/apps

# Verify network
docker network ls
docker network inspect microservices

# Check service registration
docker-compose logs service-registry
```

### Issue 5: JWT Token Issues

**Symptom:** 401 Unauthorized errors

**Solution:**
```bash
# Verify JWT_SECRET environment variable
docker-compose exec auth-service env | grep JWT

# Check token expiry
# Tokens expire after 24 hours by default
```

---

## Environment Configuration

To customize environment variables, create a `.env` file in the root directory:

```bash
# .env
JWT_SECRET=YourCustomSecretKeyHere
POSTGRES_USER=postgres
POSTGRES_PASSWORD=yourpassword
EUREKA_DEFAULT_ZONE=http://service-registry:8761/eureka
```

Then use in `docker-compose.yml`:
```yaml
environment:
  - JWT_SECRET=${JWT_SECRET}
```

---

## Monitoring and Debugging

### Check Service Health

```bash
# Eureka Status
curl http://localhost:8761/eureka/status

# Auth Service Health
curl http://localhost:8083/actuator/health

# Product Catalog Health
curl http://localhost:8081/actuator/health

# Order Management Health
curl http://localhost:8082/actuator/health

# API Gateway Health
curl http://localhost:8020/actuator/health
```

### View Database Data

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U postgres

# List databases
\l

# Connect to product database
\c productcatalog

# List tables
\dt

# View products
SELECT * FROM product;
```

---

## Performance Tuning

### Increase Resource Limits

Edit `docker-compose.yml`:

```yaml
services:
  postgres:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 4G
        reservations:
          cpus: '1'
          memory: 2G
```

### Enable Docker Logging

```bash
# Configure logging in docker-compose.yml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

---

## Production Considerations

1. **Use environment variables** for sensitive data
2. **Enable SSL/TLS** for communication
3. **Configure persistent volumes** for data
4. **Set resource limits** for containers
5. **Use secrets management** (Docker Swarm/Kubernetes)
6. **Enable logging and monitoring**
7. **Use health checks** (already configured)
8. **Set proper restart policies**

---

## Next Steps

1. **Scale services**: Adjust `replicas` in docker-compose.yml
2. **Use Kubernetes**: Deploy to Kubernetes for production
3. **CI/CD Integration**: Automate Docker builds and deployments
4. **Monitoring**: Add Prometheus and Grafana
5. **Logging**: Add ELK stack for centralized logging

---

## Quick Reference Commands

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose stop

# View status
docker-compose ps

# View logs
docker-compose logs -f

# Rebuild and start
docker-compose up -d --build

# Clean up everything
docker-compose down -v

# Execute command in container
docker-compose exec <service> <command>

# View environment
docker-compose exec <service> env
```

---

## Support and Documentation

- Docker Documentation: https://docs.docker.com/
- Docker Compose Reference: https://docs.docker.com/compose/compose-file/
- Spring Boot Docker: https://spring.io/guides/gs/spring-boot-docker/

---

**Last Updated:** February 18, 2026
