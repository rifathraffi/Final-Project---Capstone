# Docker Setup - Complete Documentation Index

## 📚 Documentation Files

### Quick Start (5 minutes)
📄 **[DOCKER_QUICKSTART.md](./DOCKER_QUICKSTART.md)**
- ⚡ 5-minute quick start
- Service URLs and test credentials
- Common commands
- Basic troubleshooting
- Testing endpoints
- **START HERE if you want to run it NOW**

### Complete Deployment Guide
📄 **[DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md)**
- Prerequisites and installation
- Complete architecture overview
- Step-by-step instructions
- All Docker commands explained
- Advanced troubleshooting
- Performance tuning
- Production considerations
- **START HERE for comprehensive understanding**

### Docker Configuration Summary
📄 **[DOCKER_SUMMARY.md](./DOCKER_SUMMARY.md)**
- Files created (5 Dockerfiles)
- Architecture diagram
- Environment variables
- Port mapping
- Health checks
- Database setup
- Security notes
- **START HERE to understand the setup**

---

## 🐳 Docker Files Created

### Dockerfiles (5 files)
```
├── Dockerfile-service-registry       (Port 8761)
├── Dockerfile-auth-service            (Port 8083)
├── Dockerfile-api-gateway             (Port 8020)
├── Dockerfile-product-catalog         (Port 8081)
└── Dockerfile-order-management        (Port 8082)
```

### Docker Compose
```
├── docker-compose.yml                 (Main orchestration)
├── init-databases.sql                 (Database initialization)
└── .dockerignore                      (Build optimization)
```

### Helper Scripts
```
├── docker-start.sh                    (Linux/macOS interactive menu)
└── docker-start.bat                   (Windows interactive menu)
```

---

## 🎯 Choose Your Path

### ⚡ "I just want to run it now"
1. Read: [DOCKER_QUICKSTART.md](./DOCKER_QUICKSTART.md) (5 min)
2. Run: `docker-compose up -d`
3. Test: Use provided curl commands

### 📖 "I want to understand everything"
1. Read: [DOCKER_SUMMARY.md](./DOCKER_SUMMARY.md) (10 min)
2. Read: [DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md) (30 min)
3. Run: Follow step-by-step instructions

### 🔧 "I need to customize or troubleshoot"
1. Reference: [DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md)
2. Check: "Troubleshooting" section
3. Modify: `docker-compose.yml` as needed

### 📚 "I need production-ready setup"
1. Read: [DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md)
2. Section: "Production Considerations"
3. Implement: Security and scaling recommendations

---

## 🚀 Super Quick Start

```bash
# 1. Build all services
docker-compose build

# 2. Start all services
docker-compose up -d

# 3. Check if running
docker-compose ps

# 4. Get auth token
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 5. Use token to access APIs
TOKEN="<token-from-step-4>"
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8020/api/v1/products
```

---

## 📋 Service Details

| Service | Port | Type | Database | Endpoint |
|---------|------|------|----------|----------|
| API Gateway | 8020 | Gateway | - | http://localhost:8020 |
| Product Catalog | 8081 | Microservice | productcatalog | http://localhost:8081 |
| Order Management | 8082 | Microservice | ordermanagement | http://localhost:8082 |
| Auth Service | 8083 | Microservice | - | http://localhost:8083 |
| Service Registry | 8761 | Registry | - | http://localhost:8761 |
| PostgreSQL | 5432 | Database | - | localhost:5432 |

---

## 🔐 Authentication

### Test Credentials
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| user | user123 | USER |

### Get Token
```bash
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Use Token
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8020/api/v1/products
```

---

## 🎮 Interactive Setup

### Linux/macOS
```bash
chmod +x docker-start.sh
./docker-start.sh
```

### Windows
```cmd
docker-start.bat
```

Choose option from menu:
- 1: Build and start
- 2: Start (no rebuild)
- 3: Stop
- 4: View status
- 5: View logs
- 6: Troubleshoot

---

## 📊 Architecture

```
┌─────────────────────────────────────────────────────────┐
│                API Gateway (8020)                       │
│           (Main entry point for all requests)           │
└────────────────────┬────────────────────────────────────┘
                     │
          ┌──────────┼──────────┐
          │          │          │
     ┌────▼────┐ ┌──▼────┐ ┌───▼─────┐
     │ Service │ │ Auth  │ │ Product │
     │Registry │ │Service│ │ Catalog │
     │ (8761)  │ │(8083) │ │ (8081)  │
     └────┬────┘ └──────┘ └───────┬─┘
          │                       │
          └───────────┬───────────┘
                      │
                ┌─────▼──────┐     ┌──────────────┐
                │ PostgreSQL │     │Order Mgmt    │
                │ (5432)     │     │Service (8082)│
                └────────────┘     └──────────────┘
```

---

## ✅ Verification Checklist

After starting services, verify:

- [ ] All containers running: `docker-compose ps`
- [ ] Can login: `curl -X POST http://localhost:8020/api/v1/auth/login ...`
- [ ] Get products: `curl -H "Authorization: Bearer ..." http://localhost:8020/api/v1/products`
- [ ] Eureka shows services: http://localhost:8761
- [ ] Database connected: All service logs show no connection errors
- [ ] Health checks passing: `curl http://localhost:8081/actuator/health`

---

## 🛠️ Common Commands Reference

| Task | Command |
|------|---------|
| Build images | `docker-compose build` |
| Start services | `docker-compose up -d` |
| Stop services | `docker-compose stop` |
| Restart services | `docker-compose restart` |
| View status | `docker-compose ps` |
| View logs (all) | `docker-compose logs -f` |
| View logs (one) | `docker-compose logs -f auth-service` |
| Remove everything | `docker-compose down -v` |
| Execute command | `docker-compose exec <service> <cmd>` |
| Connect to DB | `docker-compose exec postgres psql -U postgres` |

---

## 🚨 Troubleshooting Quick Links

| Problem | Solution |
|---------|----------|
| Containers won't start | See: [DOCKER_DEPLOYMENT_GUIDE.md#issue-1](./DOCKER_DEPLOYMENT_GUIDE.md) |
| Database connection error | See: [DOCKER_DEPLOYMENT_GUIDE.md#issue-2](./DOCKER_DEPLOYMENT_GUIDE.md) |
| Port already in use | See: [DOCKER_DEPLOYMENT_GUIDE.md#issue-3](./DOCKER_DEPLOYMENT_GUIDE.md) |
| Services not discovering | See: [DOCKER_DEPLOYMENT_GUIDE.md#issue-4](./DOCKER_DEPLOYMENT_GUIDE.md) |
| JWT token errors | See: [DOCKER_DEPLOYMENT_GUIDE.md#issue-5](./DOCKER_DEPLOYMENT_GUIDE.md) |

---

## 📖 Recommended Reading Order

### For Beginners
1. [DOCKER_QUICKSTART.md](./DOCKER_QUICKSTART.md) - 5 minutes
2. [DOCKER_SUMMARY.md](./DOCKER_SUMMARY.md) - 10 minutes
3. Run and test the services

### For Developers
1. [DOCKER_SUMMARY.md](./DOCKER_SUMMARY.md) - Overview
2. [DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md) - Full details
3. Modify and customize as needed

### For DevOps/SysAdmins
1. [DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md) - Complete guide
2. "Production Considerations" section
3. Implement monitoring and scaling

---

## 📞 Getting Help

### I don't know where to start
→ Read [DOCKER_QUICKSTART.md](./DOCKER_QUICKSTART.md)

### Something is broken
→ Check "Troubleshooting" in [DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md)

### I need more details
→ Read [DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md)

### I want to customize
→ See environment variables section in [DOCKER_SUMMARY.md](./DOCKER_SUMMARY.md)

---

## 🔗 Additional Resources

- **Docker Official Docs:** https://docs.docker.com/
- **Docker Compose Reference:** https://docs.docker.com/compose/compose-file/
- **Spring Boot Docker Guide:** https://spring.io/guides/gs/spring-boot-docker/
- **PostgreSQL Docker:** https://hub.docker.com/_/postgres
- **OpenJDK Docker:** https://hub.docker.com/_/openjdk

---

## ✨ Features

✅ **5 Microservices** with health checks
✅ **PostgreSQL** with automatic database creation
✅ **Service Discovery** with Eureka
✅ **API Gateway** for routing
✅ **JWT Authentication** with role-based access
✅ **Multi-stage Docker builds** for optimized images
✅ **Docker Compose** for easy orchestration
✅ **Health checks** for reliability
✅ **Environment variables** for configuration
✅ **Complete documentation** with examples

---

## 📝 Notes

- All services use **Alpine Linux** for smaller images
- Database initializes automatically on first run
- Services have **40-second startup delay** for health checks
- Default credentials provided for testing
- Production setup requires security hardening

---

**Last Updated:** February 18, 2026

**Questions?** Check the relevant documentation file above.
