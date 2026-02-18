#!/bin/bash
# Docker Quick Start Script for Microservices

set -e

echo "================================"
echo "Microservices Docker Setup"
echo "================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Docker installation
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}ERROR: Docker is not installed${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Docker is installed${NC}"
}

# Check Docker Compose installation
check_compose() {
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}ERROR: Docker Compose is not installed${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Docker Compose is installed${NC}"
}

# Display menu
show_menu() {
    echo ""
    echo "================================"
    echo "Select an option:"
    echo "================================"
    echo "1) Build and Start All Services"
    echo "2) Start Services (without rebuild)"
    echo "3) Stop Services"
    echo "4) View Service Status"
    echo "5) View Logs (All Services)"
    echo "6) View Logs (Specific Service)"
    echo "7) Stop and Remove All"
    echo "8) Run Tests"
    echo "9) Exit"
    echo "================================"
    read -p "Enter your choice [1-9]: " choice
}

# Build and start
build_and_start() {
    echo -e "${YELLOW}Building Docker images...${NC}"
    docker-compose build
    echo -e "${GREEN}✓ Build complete${NC}"
    
    echo -e "${YELLOW}Starting services...${NC}"
    docker-compose up -d
    echo -e "${GREEN}✓ Services starting${NC}"
    
    echo ""
    echo -e "${YELLOW}Waiting for services to be healthy...${NC}"
    sleep 10
    docker-compose ps
}

# Start without rebuild
start_services() {
    echo -e "${YELLOW}Starting services...${NC}"
    docker-compose up -d
    sleep 5
    docker-compose ps
}

# Stop services
stop_services() {
    echo -e "${YELLOW}Stopping services...${NC}"
    docker-compose stop
    echo -e "${GREEN}✓ Services stopped${NC}"
}

# View status
view_status() {
    echo -e "${YELLOW}Service Status:${NC}"
    docker-compose ps
}

# View all logs
view_logs() {
    docker-compose logs -f
}

# View specific service logs
view_service_logs() {
    echo "Available services:"
    docker-compose ps --services
    read -p "Enter service name: " service
    docker-compose logs -f "$service"
}

# Stop and remove
stop_and_remove() {
    read -p "This will stop and remove all containers. Continue? (y/n): " confirm
    if [[ $confirm == "y" ]]; then
        echo -e "${YELLOW}Removing services...${NC}"
        docker-compose down -v
        echo -e "${GREEN}✓ All services removed${NC}"
    fi
}

# Run tests
run_tests() {
    echo -e "${YELLOW}Running tests...${NC}"
    docker-compose exec -T product-catalog-service mvn test
}

# Main menu loop
main() {
    check_docker
    check_compose
    
    echo ""
    echo -e "${GREEN}Docker environment is ready!${NC}"
    
    while true; do
        show_menu
        case $choice in
            1) build_and_start ;;
            2) start_services ;;
            3) stop_services ;;
            4) view_status ;;
            5) view_logs ;;
            6) view_service_logs ;;
            7) stop_and_remove ;;
            8) run_tests ;;
            9) echo -e "${GREEN}Goodbye!${NC}"; exit 0 ;;
            *) echo -e "${RED}Invalid option${NC}" ;;
        esac
    done
}

main
