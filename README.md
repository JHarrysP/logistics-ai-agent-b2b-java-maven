# B2B Logistics AI Agent - Complete Project Structure


## Project Structure
```

logistics-ai-agent/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
├── .gitignore
├── mvnw
├── mvnw.cmd
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar
│       └── maven-wrapper.properties
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── logistics/
    │   │           ├── LogisticsAIAgentApplication.java
    │   │           ├── config/
    │   │           │   ├── AsyncConfig.java
    │   │           │   ├── WebConfig.java
    │   │           │   └── SwaggerConfig.java
    │   │           ├── model/
    │   │           │   ├── Order.java
    │   │           │   ├── OrderItem.java
    │   │           │   ├── Product.java
    │   │           │   ├── Shipment.java
    │   │           │   ├── OrderStatus.java
    │   │           │   └── ShipmentStatus.java
    │   │           ├── repository/
    │   │           │   ├── OrderRepository.java
    │   │           │   ├── ProductRepository.java
    │   │           │   └── ShipmentRepository.java
    │   │           ├── service/
    │   │           │   ├── LogisticsAIAgent.java
    │   │           │   ├── OrderValidationAgent.java
    │   │           │   ├── InventoryAgent.java
    │   │           │   ├── FulfillmentAgent.java
    │   │           │   ├── WarehouseAgent.java
    │   │           │   ├── ShippingAgent.java
    │   │           │   └── NotificationService.java
    │   │           ├── dto/
    │   │           │   ├── OrderRequest.java
    │   │           │   ├── OrderItemRequest.java
    │   │           │   ├── OrderResponse.java
    │   │           │   └── OrderStatusResponse.java
    │   │           ├── controller
    │   │           │   ├── OrderController.java
    │   │           │   └── WarehouseController.java
    │   │           ├── exception/
    │   │           │   ├── InvalidOrderException.java
    │   │           │   ├── OrderNotFoundException.java
    │   │           │   ├── InsufficientInventoryException.java
    │   │           │   └── GlobalExceptionHandler.java
    │   │           ├── event/
    │   │           │   ├── OrderReceivedEvent.java
    │   │           │   ├── OrderValidatedEvent.java
    │   │           │   ├── OrderFulfilledEvent.java
    │   │           │   └── LogisticsEventListener.java
    │   │           └── util/
    │   │               ├── ValidationResult.java
    │   │               ├── InventoryCheckResult.java
    │   │               ├── FulfillmentResult.java
    │   │               └── WarehouseInstructions.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-test.yml
    │       ├── application-prod.yml
    │       ├── data.sql
    │       └── static/
    │           └── api-docs.html
    └── test/
        └── java/
            └── com/
                └── logistics/
                    ├── LogisticsAIAgentApplicationTests.java
                    ├── integration/
                    │   └── OrderWorkflowIntegrationTest.java
                    ├── service/
                    │   ├── LogisticsAIAgentTest.java
                    │   ├── OrderValidationAgentTest.java
                    │   └── InventoryAgentTest.java
                    └── controller/
                        └── OrderControllerTest.java
```

## Key Features

###  AI-Powered Workflow Automation
- **Order Validation Agent**: Validates order data, delivery addresses, and business rules
- **Inventory Agent**: Real-time inventory checking and reservation
- **Fulfillment Agent**: Automated order fulfillment and stock allocation
- **Warehouse Agent**: Intelligent picking instructions and loading optimization
- **Shipping Agent**: Smart truck assignment and delivery scheduling

###  Order Management
- Async order processing with event-driven architecture
- Real-time order status tracking
- Client order history and analytics
- Automated inventory updates

### Warehouse Operations
- AI-generated picking instructions
- Location-based picking optimization
- Special handling for fragile/heavy items
- Loading sequence optimization

###  Shipping & Logistics
- Intelligent truck and driver assignment
- Delivery time estimation
- Real-time shipment tracking
- Exception handling and notifications

###  Event-Driven Architecture
- Order lifecycle events
- Real-time notifications
- Audit logging
- System monitoring

## Technology Stack

- **Backend**: Spring Boot 2.7.14, Java 11
- **Database**: H2 (development), PostgreSQL (production)
- **API Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito, TestContainers
- **Containerization**: Docker, Docker Compose
- **Build Tool**: Maven 3.6+

## Quick Start

### Prerequisites
- Java 11+
- Maven 3.6+
- Docker (optional)

### Run Locally
```bash
# Clone repository
git clone <repository-url>
cd logistics-ai-agent

# Build and run
mvn clean package
java -jar target/logistics-ai-agent.jar

# Or using Maven
mvn spring-boot:run
```

### Run with Docker
```bash
# Build image
docker build -t logistics-ai-agent .

# Run with docker-compose
docker-compose up -d
```

### Access Points
- **Application**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health

## API Examples

### Submit Order
```bash
curl -X POST http://localhost:8080/api/orders/submit \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "CLIENT_001",
    "clientName": "Hamburg Construction GmbH",
    "deliveryAddress": "Baustelle Hafencity, Hamburg 20457, Germany",
    "requestedDeliveryDate": "2025-08-01T10:00:00",
    "items": [
      {
        "sku": "TILE-001",
        "quantity": 10,
        "unitPrice": 25.99
      },
      {
        "sku": "CONC-001",
        "quantity": 5,
        "unitPrice": 15.50
      }
    ]
  }'
```

### Check Order Status
```bash
curl http://localhost:8080/api/orders/1/status
```

### Get Client Orders
```bash
curl http://localhost:8080/api/orders/client/CLIENT_001
```

### Get Pending Shipments
```bash
curl http://localhost:8080/api/warehouse/pending-shipments
```

## Sample Data

The application includes sample data for testing:
- **Products**: Tiles, construction materials, roofing supplies, plumbing supplies
- **Locations**: Warehouse locations (A-01-01, B-02-01, etc.)
- **Test Orders**: Sample orders from demo clients

## Configuration Profiles

- **dev**: Development profile with H2 database, detailed logging
- **test**: Test profile for automated testing
- **prod**: Production profile with PostgreSQL, optimized logging

## Testing

```bash
# Run all tests
mvn test

# Run integration tests
mvn verify

# Generate test coverage report
mvn jacoco:report
```

## Deployment

### Production Environment Variables
```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=logistics
DB_USERNAME=logistics_user
DB_PASSWORD=logistics_pass
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

### Docker Deployment
```bash
# Build production image
docker build -t logistics-ai-agent:prod --target prod .

# Deploy with compose
docker-compose -f docker-compose.prod.yml up -d
```#   l o g i s t i c s - a i - a g e n t - b 2 b - j a v a - m a v e n  
 