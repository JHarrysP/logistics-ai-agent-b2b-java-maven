#!/bin/bash

# =============================================================================
# LOGISTICS AI AGENT - COMPLETE WORKFLOW TEST SCRIPT
# =============================================================================
# This script tests the entire order lifecycle from submission to delivery
# Make sure your Spring Boot application is running on http://localhost:8080

BASE_URL="http://localhost:8080/api"
CONTENT_TYPE="Content-Type: application/json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_step() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN} $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}️  $1${NC}"
}

print_error() {
    echo -e "${RED} $1${NC}"
}

# Function to wait for user input
wait_for_input() {
    echo -e "${YELLOW}Press Enter to continue to next step...${NC}"
    read
}

# Function to extract order ID from response
extract_order_id() {
    echo "$1" | grep -o '"orderId":[0-9]*' | grep -o '[0-9]*'
}

# Function to extract shipment ID from response
extract_shipment_id() {
    echo "$1" | grep -o '"shipmentId":[0-9]*' | grep -o '[0-9]*'
}

print_step "LOGISTICS AI AGENT - COMPLETE WORKFLOW TEST"
echo "This script will test the complete order lifecycle:"
echo "1. Submit Order → 2. AI Processing → 3. Warehouse Operations → 4. Delivery"
echo ""
wait_for_input

# =============================================================================
# STEP 1: SUBMIT A TEST ORDER
# =============================================================================
print_step "STEP 1: Submitting Test Order"

ORDER_JSON='{
  "clientId": "TEST_CLIENT_001",
  "clientName": "Hamburg Test Construction GmbH",
  "deliveryAddress": "Test Baustelle Hafencity, Überseeallee 10, 20457 Hamburg, Germany",
  "requestedDeliveryDate": "'$(date -d "+3 days" -Iseconds | cut -d'+' -f1)'",
  "items": [
    {
      "sku": "TILE-001",
      "quantity": 50,
      "unitPrice": 25.00
    },
    {
      "sku": "CONC-001",
      "quantity": 10,
      "unitPrice": 50.00
    }
  ]
}'

echo "Submitting order with JSON:"
echo "$ORDER_JSON" | jq .

ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/orders/submit" \
  -H "$CONTENT_TYPE" \
  -d "$ORDER_JSON")

if [ $? -eq 0 ] && echo "$ORDER_RESPONSE" | jq . > /dev/null 2>&1; then
    print_success "Order submitted successfully!"
    echo "$ORDER_RESPONSE" | jq .
    ORDER_ID=$(extract_order_id "$ORDER_RESPONSE")
    echo "Order ID: $ORDER_ID"
else
    print_error "Failed to submit order"
    echo "$ORDER_RESPONSE"
    exit 1
fi

wait_for_input

# =============================================================================
# STEP 2: MONITOR AI PROCESSING
# =============================================================================
print_step "STEP 2: Monitoring AI Processing"

echo "Waiting for AI agents to process the order..."
sleep 3

# Check order status multiple times to see progression
for i in {1..5}; do
    echo "Status check #$i:"
    STATUS_RESPONSE=$(curl -s "$BASE_URL/orders/$ORDER_ID/status")

    if echo "$STATUS_RESPONSE" | jq . > /dev/null 2>&1; then
        ORDER_STATUS=$(echo "$STATUS_RESPONSE" | jq -r '.status')
        STATUS_DESC=$(echo "$STATUS_RESPONSE" | jq -r '.statusDescription')
        echo "Current Status: $ORDER_STATUS - $STATUS_DESC"

        # Extract shipment info if available
        SHIPMENT_INFO=$(echo "$STATUS_RESPONSE" | jq -r '.shipmentInfo // empty')
        if [ "$SHIPMENT_INFO" != "" ] && [ "$SHIPMENT_INFO" != "null" ]; then
            SHIPMENT_ID=$(echo "$STATUS_RESPONSE" | jq -r '.shipmentInfo.shipmentId')
            TRUCK_ID=$(echo "$STATUS_RESPONSE" | jq -r '.shipmentInfo.truckId')
            echo "Shipment ID: $SHIPMENT_ID, Truck: $TRUCK_ID"
        fi
    else
        print_warning "Could not parse status response"
    fi

    sleep 2
done

wait_for_input

# =============================================================================
# STEP 3: WAREHOUSE OPERATIONS
# =============================================================================
print_step "STEP 3: Warehouse Operations Simulation"

# Get shipment ID for warehouse operations
STATUS_RESPONSE=$(curl -s "$BASE_URL/orders/$ORDER_ID/status")
SHIPMENT_ID=$(echo "$STATUS_RESPONSE" | jq -r '.shipmentInfo.shipmentId // empty')

if [ "$SHIPMENT_ID" != "" ] && [ "$SHIPMENT_ID" != "null" ]; then
    print_success "Found Shipment ID: $SHIPMENT_ID"

    # Get picking instructions
    echo "Getting picking instructions..."
    INSTRUCTIONS=$(curl -s "$BASE_URL/warehouse/shipments/$SHIPMENT_ID/instructions")
    if [ "$INSTRUCTIONS" != "" ]; then
        echo "Picking Instructions:"
        echo "$INSTRUCTIONS"
    fi

    wait_for_input

    # Start loading
    print_step "Starting Loading Process"
    LOADING_RESPONSE=$(curl -s -X POST "$BASE_URL/warehouse/shipments/$SHIPMENT_ID/start-loading")
    print_success "Loading started: $LOADING_RESPONSE"

    sleep 2

    # Complete loading
    print_step "Completing Loading Process"
    LOADED_RESPONSE=$(curl -s -X POST "$BASE_URL/warehouse/shipments/$SHIPMENT_ID/complete-loading")
    print_success "Loading completed: $LOADED_RESPONSE"

    sleep 2

    # Dispatch
    print_step "Dispatching Shipment"
    DISPATCH_RESPONSE=$(curl -s -X POST "$BASE_URL/warehouse/shipments/$SHIPMENT_ID/dispatch")
    print_success "Shipment dispatched: $DISPATCH_RESPONSE"

else
    print_warning "No shipment found yet, skipping warehouse operations"
fi

wait_for_input

# =============================================================================
# STEP 4: DELIVERY SIMULATION
# =============================================================================
print_step "STEP 4: Delivery Process Simulation"

if [ "$SHIPMENT_ID" != "" ] && [ "$SHIPMENT_ID" != "null" ]; then

    # Check current status
    STATUS_RESPONSE=$(curl -s "$BASE_URL/orders/$ORDER_ID/status")
    CURRENT_STATUS=$(echo "$STATUS_RESPONSE" | jq -r '.status')
    echo "Current Order Status: $CURRENT_STATUS"

    # Simulate delivery status update
    print_step "Driver Status Update"
    UPDATE_RESPONSE=$(curl -s -X POST "$BASE_URL/warehouse/shipments/$SHIPMENT_ID/update-status" \
      -d "statusUpdate=En route to delivery address" \
      -d "driverNotes=Traffic is light, should arrive on time" \
      -d "currentLocation=Hamburg City Center")
    print_success "Status updated: $UPDATE_RESPONSE"

    wait_for_input

    # Option 1: Successful Delivery
    echo "Choose delivery outcome:"
    echo "1) Successful Delivery"
    echo "2) Delivery Problem"
    echo "3) Delay Delivery"
    read -p "Enter choice (1-3): " DELIVERY_CHOICE

    case $DELIVERY_CHOICE in
        1)
            print_step "Marking as Delivered"
            DELIVERY_RESPONSE=$(curl -s -X POST "$BASE_URL/warehouse/shipments/$SHIPMENT_ID/delivered")
            print_success "Delivery completed: $DELIVERY_RESPONSE"
            ;;
        2)
            print_step "Reporting Delivery Problem"
            PROBLEM_RESPONSE=$(curl -s -X POST "$BASE_URL/warehouse/shipments/$SHIPMENT_ID/delivery-problem" \
              -d "problem=Customer not available at delivery address" \
              -d "newEstimatedDelivery=$(date -d "+1 day" -Iseconds | cut -d'+' -f1)")
            print_warning "Delivery problem reported: $PROBLEM_RESPONSE"

            # Schedule redelivery
            wait_for_input
            print_step "Scheduling Redelivery"
            REDELIVER_RESPONSE=$(curl -s -X POST "$BASE_URL/warehouse/shipments/$SHIPMENT_ID/redeliver" \
              -d "newDeliveryTime=$(date -d "+1 day" -Iseconds | cut -d'+' -f1)" \
              -d "notes=Customer confirmed availability for redelivery")
            print_success "Redelivery scheduled: $REDELIVER_RESPONSE"
            ;;
        3)
            print_step "Delaying Delivery"
            DELAY_RESPONSE=$(curl -s -X POST "$BASE_URL/orders/$ORDER_ID/delay" \
              -d "newEstimatedDelivery=$(date -d "+2 days" -Iseconds | cut -d'+' -f1)" \
              -d "reason=Weather conditions - safety delay")
            print_warning "Delivery delayed: $DELAY_RESPONSE"
            ;;
    esac

else
    print_error "Cannot simulate delivery - no shipment ID available"
fi

wait_for_input

# =============================================================================
# STEP 5: FINAL STATUS CHECK
# =============================================================================
print_step "STEP 5: Final Status Check"

echo "Final order status:"
FINAL_STATUS=$(curl -s "$BASE_URL/orders/$ORDER_ID/status")
echo "$FINAL_STATUS" | jq .

echo ""
echo "Order statistics:"
STATS=$(curl -s "$BASE_URL/orders/stats")
echo "$STATS" | jq .

echo ""
print_success "Test completed! Check the application logs to see all AI agent activities."

# =============================================================================
# ADDITIONAL TEST ENDPOINTS
# =============================================================================
print_step "ADDITIONAL ENDPOINTS TO TEST MANUALLY"

echo "You can also test these endpoints manually:"
echo ""
echo "ORDER MANAGEMENT:"
echo "GET    $BASE_URL/orders/client/TEST_CLIENT_001"
echo "GET    $BASE_URL/orders/status/DELIVERED"
echo "POST   $BASE_URL/orders/$ORDER_ID/reject?reason=Test%20rejection"
echo "PUT    $BASE_URL/orders/$ORDER_ID/status/DELIVERED?reason=Manual%20update"
echo ""
echo "WAREHOUSE MANAGEMENT:"
echo "GET    $BASE_URL/warehouse/pending-shipments"
echo "GET    $BASE_URL/warehouse/today-shipments"
echo "GET    $BASE_URL/warehouse/overdue-shipments"
echo "GET    $BASE_URL/warehouse/special-handling"
echo "GET    $BASE_URL/warehouse/failed-deliveries"
echo "GET    $BASE_URL/warehouse/today-deliveries"
echo ""
echo "MONITORING:"
echo "GET    $BASE_URL/orders/stats"
echo ""
echo "🔧 ACTUATOR (Health Check):"
echo "GET    http://localhost:8080/actuator/health"
echo "GET    http://localhost:8080/actuator/info"
echo ""
echo "API DOCUMENTATION:"
echo "Open: http://localhost:8080/swagger-ui.html"
echo ""

print_success "Test script completed successfully!"