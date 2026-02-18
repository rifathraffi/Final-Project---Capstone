# Docker Quick Start - Step by Step

## ⚡ 5-Minute Quick Start

### Prerequisites
- Docker Desktop installed and running
- Terminal/Command Prompt open in project root directory

### Step 1: Build Images
```bash
docker-compose build
```
**Expected:** "Successfully built" messages for all 5 services
**Time:** ~3-5 minutes

### Step 2: Start Services
```bash
docker-compose up -d
```
**Expected:** "Creating", "Starting" messages
**Time:** ~30 seconds

### Step 3: Check Status
```bash
docker-compose ps
```
**Expected:** All services showing "Up" status
```
NAME                      STATUS
postgres_db              Up (healthy)
service-registry         Up (healthy)
auth-service             Up (healthy)
product-catalog-service  Up (healthy)
order-management-service Up (healthy)
api-gateway              Up (healthy)
```

### Step 4: Test API
```bash
# Get authentication token
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

### Step 5: Use Token
```bash
# Copy token from response
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Get products
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8020/api/v1/products
```

---

## 📋 Service URLs

| Service | URL | Purpose |
|---------|-----|---------|
| Eureka Dashboard | http://localhost:8761 | View all services |
| Auth Service | http://localhost:8083/api/v1/auth | Login/Register |
| Product Catalog | http://localhost:8081/api/v1/products | Products API |
| Order Management | http://localhost:8082/api/v1/orders | Orders API |
| API Gateway | http://localhost:8020 | Main entry point |

---

## 🔐 Test Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| user | user123 | USER |

---

## 📝 Common Commands

### Start Services
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose stop
```

### View Logs (All)
```bash
docker-compose logs -f
```

### View Logs (Specific Service)
```bash
docker-compose logs -f auth-service
```

### Rebuild and Start
```bash
docker-compose up -d --build
```

### Clean Everything
```bash
docker-compose down -v
```

### Check Service Status
```bash
docker-compose ps
```

---

## 🐛 Troubleshooting

### Services won't start
```bash
# Check logs
docker-compose logs

# Rebuild
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

### Can't connect to database
```bash
# Check PostgreSQL
docker-compose ps postgres

# Check logs
docker-compose logs postgres

# Restart
docker-compose restart postgres
```

### Port already in use
```bash
# Find what's using the port (macOS/Linux)
lsof -i :8020

# Change port in docker-compose.yml or kill the process
```

### Services can't find each other
```bash
# Check network
docker network ls
docker network inspect microservices

# Restart services
docker-compose restart
```

---

## 🧪 Testing Endpoints

### 1. Login (Get Token)
```bash
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 2. Get Products (Any authenticated user)
```bash
TOKEN="<your-token-here>"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8020/api/v1/products
```

### 3. Create Product (ADMIN only)
```bash
TOKEN="<your-token-here>"

curl -X POST http://localhost:8020/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 29.99,
    "quantity": 100,
    "sku": "TEST-001"
  }'
```

### 4. Get Orders (ADMIN only)
```bash
TOKEN="<your-token-here>"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8020/api/v1/orders
```

### 5. Check Service Health
```bash
curl http://localhost:8083/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

---

## 💾 Database Access

### Connect to PostgreSQL
```bash
docker-compose exec postgres psql -U postgres
```

### List databases
```sql
\l
```

### Connect to product database
```sql
\c productcatalog
```

### View tables
```sql
\dt
```

### View products
```sql
SELECT * FROM product;
```

### Exit
```sql
\q
```

---

## 📊 Monitoring

### View all container stats
```bash
docker stats
```

### View specific container
```bash
docker stats postgres_db
```

### View container logs
```bash
docker logs <container-name>
```

### Follow logs in real-time
```bash
docker logs -f <container-name>
```

---

## 🎯 Development Workflow

### Make code changes
1. Edit source files in your IDE
2. Changes are NOT reflected in running containers
3. Rebuild to apply changes:
   ```bash
   docker-compose up -d --build
   ```

### Test your changes
1. Access services through API Gateway: http://localhost:8020
2. Use test credentials for authentication
3. Monitor logs: `docker-compose logs -f`

### Verify databases
```bash
docker-compose exec postgres psql -U postgres -d productcatalog -c "SELECT * FROM product;"
```

---

## ✅ Verification Checklist

- [ ] Docker Desktop is running
- [ ] All images built successfully
- [ ] All containers show "Up" status
- [ ] Can login with credentials
- [ ] Can access products endpoint
- [ ] Database tables created
- [ ] All services registered in Eureka
- [ ] Logs show no errors

---

## 🚀 Next Steps

1. **Explore the API:** Test different endpoints
2. **Check Eureka:** Visit http://localhost:8761
3. **Monitor logs:** `docker-compose logs -f`
4. **Read full guide:** See `DOCKER_DEPLOYMENT_GUIDE.md`
5. **Scale services:** Modify docker-compose.yml for production

---

## 📞 Help

- **Issues?** Check troubleshooting section above
- **Need details?** See `DOCKER_DEPLOYMENT_GUIDE.md`
- **Docker help:** `docker --help` or `docker-compose --help`
- **Docker docs:** https://docs.docker.com/

---

**Last Updated:** February 18, 2026
