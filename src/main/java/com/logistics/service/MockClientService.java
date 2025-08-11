package com.logistics.service;

import com.logistics.dto.OrderItemRequest;
import com.logistics.dto.OrderRequest;
import com.logistics.model.Product;
import com.logistics.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Mock Client Service for generating realistic test data and order patterns
 */
@Service
public class MockClientService {

    @Autowired
    private LogisticsAIAgent aiAgent;

    @Autowired
    private ProductRepository productRepository;

    private final Random random = new Random();

    // Mock client database
    private final List<MockClient> mockClients = Arrays.asList(
            new MockClient("CLI_HAM_001", "Hamburg Construction GmbH", "Baustelle Hafencity, Überseeallee 10, 20457 Hamburg, Germany", "CONSTRUCTION"),
            new MockClient("CLI_HAM_002", "Premium Tiles Hamburg", "Showroom Eppendorf, Eppendorfer Weg 95, 20249 Hamburg, Germany", "RETAIL"),
            new MockClient("CLI_BER_001", "Berlin Building Supplies Ltd", "Alexanderplatz Construction Site, 10178 Berlin, Germany", "WHOLESALE"),
            new MockClient("CLI_MUN_001", "München Bau Materials", "Marienplatz Renovation Project, 80331 München, Germany", "CONSTRUCTION"),
            new MockClient("CLI_COL_001", "Köln Industrial Supply", "Industriegebiet Niehl, 50735 Köln, Germany", "INDUSTRIAL"),
            new MockClient("CLI_FRA_001", "Frankfurt Roofing Specialists", "Zeil Commercial District, 60313 Frankfurt, Germany", "SPECIALIST"),
            new MockClient("CLI_STU_001", "Stuttgart Auto Parts & Construction", "Mercedes District, 70173 Stuttgart, Germany", "AUTOMOTIVE"),
            new MockClient("CLI_DUS_001", "Düsseldorf Premium Builders", "Königsallee Business Center, 40212 Düsseldorf, Germany", "PREMIUM"),
            new MockClient("CLI_HAM_003", "Hamburg Port Logistics", "Speicherstadt Warehouse District, 20457 Hamburg, Germany", "LOGISTICS"),
            new MockClient("CLI_BRE_001", "Bremen Maritime Construction", "Weser Riverfront Development, 28195 Bremen, Germany", "MARITIME")
    );

    /**
     * Generate realistic orders during business hours
     * Runs every 10 minutes during business hours (8 AM - 6 PM)
     */
    @Scheduled(cron = "0 */10 8-18 * * MON-FRI") // Every 10 minutes, 8-18h, Mon-Fri
    public void generateBusinessHourOrders() {
        if (shouldGenerateOrder(0.3)) { // 30% chance every 10 minutes
            generateRandomOrder("BUSINESS_HOURS");
        }
    }

    /**
     * Generate occasional orders during off-hours
     * Runs every 30 minutes during off-hours
     */
    @Scheduled(cron = "0 */30 19-23,0-7 * * *") // Every 30 minutes, off-hours
    public void generateOffHourOrders() {
        if (shouldGenerateOrder(0.05)) { // 5% chance every 30 minutes
            generateRandomOrder("OFF_HOURS");
        }
    }

    /**
     * Generate weekend orders (reduced frequency)
     */
    @Scheduled(cron = "0 0 */2 * * SAT-SUN") // Every 2 hours on weekends
    public void generateWeekendOrders() {
        if (shouldGenerateOrder(0.15)) { // 15% chance every 2 hours
            generateRandomOrder("WEEKEND");
        }
    }

    /**
     * Generate urgent/rush orders randomly
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void generateRushOrders() {
        if (shouldGenerateOrder(0.08)) { // 8% chance per hour
            generateRushOrder();
        }
    }

    /**
     * Generate large bulk orders for wholesale clients
     */
    @Scheduled(cron = "0 0 9,14 * * MON-FRI") // 9 AM and 2 PM on weekdays
    public void generateBulkOrders() {
        if (shouldGenerateOrder(0.25)) { // 25% chance at these times
            generateBulkOrder();
        }
    }

    private void generateRandomOrder(String context) {
        try {
            MockClient client = getRandomClient();
            OrderRequest orderRequest = createOrderRequest(client, context);

            System.out.println("🎭 Mock Client (" + context + "): " + client.name +
                    " placing order with " + orderRequest.getItems().size() + " items");

            // Submit through AI agent like a real client would
            // Note: We create a simplified version here - in real system would go through controller
            processOrderThroughAI(orderRequest);

        } catch (Exception e) {
            System.err.println("Error generating mock order: " + e.getMessage());
        }
    }

    private void generateRushOrder() {
        MockClient client = getRandomClient();
        OrderRequest orderRequest = createRushOrderRequest(client);

        System.out.println("🚨 RUSH ORDER: " + client.name + " placing urgent order");
        processOrderThroughAI(orderRequest);
    }

    private void generateBulkOrder() {
        MockClient wholesaleClient = mockClients.stream()
                .filter(c -> c.type.equals("WHOLESALE") || c.type.equals("CONSTRUCTION"))
                .skip(random.nextInt(3))
                .findFirst()
                .orElse(mockClients.get(0));

        OrderRequest orderRequest = createBulkOrderRequest(wholesaleClient);

        System.out.println("📦 BULK ORDER: " + wholesaleClient.name + " placing large order with " +
                orderRequest.getItems().size() + " product types");
        processOrderThroughAI(orderRequest);
    }

    private OrderRequest createOrderRequest(MockClient client, String context) {
        OrderRequest request = new OrderRequest();
        request.setClientId(client.id);
        request.setClientName(client.name);
        request.setDeliveryAddress(client.address);

        // Set delivery date based on context
        LocalDateTime deliveryDate = calculateDeliveryDate(context);
        request.setRequestedDeliveryDate(deliveryDate);

        // Generate items based on client type
        List<OrderItemRequest> items = generateItemsForClient(client, context);
        request.setItems(items);

        return request;
    }

    private OrderRequest createRushOrderRequest(MockClient client) {
        OrderRequest request = createOrderRequest(client, "RUSH");

        // Rush orders: next day delivery
        request.setRequestedDeliveryDate(LocalDateTime.now().plusDays(1));

        // Typically smaller orders
        if (request.getItems().size() > 2) {
            request.getItems().subList(2, request.getItems().size()).clear();
        }

        return request;
    }

    private OrderRequest createBulkOrderRequest(MockClient client) {
        OrderRequest request = createOrderRequest(client, "BULK");

        // Bulk orders: longer delivery time acceptable
        request.setRequestedDeliveryDate(LocalDateTime.now().plusDays(7));

        // Increase quantities for bulk
        request.getItems().forEach(item ->
                item.setQuantity(item.getQuantity() * (3 + random.nextInt(5)))
        );

        return request;
    }

    private List<OrderItemRequest> generateItemsForClient(MockClient client, String context) {
        List<OrderItemRequest> items = new ArrayList<>();
        List<Product> availableProducts = productRepository.findAll();

        if (availableProducts.isEmpty()) {
            return items; // No products available
        }

        // Filter products based on client type
        List<Product> relevantProducts = filterProductsForClient(availableProducts, client);

        // Determine number of items based on context and client type
        int itemCount = determineItemCount(client, context);

        for (int i = 0; i < itemCount && !relevantProducts.isEmpty(); i++) {
            Product product = relevantProducts.get(random.nextInt(relevantProducts.size()));

            OrderItemRequest item = new OrderItemRequest();
            item.setSku(product.getSku());
            item.setQuantity(generateRealisticQuantity(product, client));
            item.setUnitPrice(generateRealisticPrice(product, client));

            items.add(item);
            relevantProducts.remove(product); // Avoid duplicates
        }

        return items;
    }

    private List<Product> filterProductsForClient(List<Product> products, MockClient client) {
        return products.stream()
                .filter(p -> isProductRelevantForClient(p, client))
                .filter(p -> p.getStockQuantity() > 0)
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean isProductRelevantForClient(Product product, MockClient client) {
        switch (client.type) {
            case "CONSTRUCTION":
                return product.getCategory().equals("CONSTRUCTION_MATERIALS") ||
                        product.getCategory().equals("ROOFING_MATERIALS");

            case "RETAIL":
                return product.getCategory().equals("TILES") ||
                        product.getCategory().equals("PLUMBING_SUPPLIES");

            case "WHOLESALE":
                return true; // Wholesale clients buy everything

            case "SPECIALIST":
                return product.getCategory().equals("ROOFING_MATERIALS");

            case "INDUSTRIAL":
                return product.getCategory().equals("CONSTRUCTION_MATERIALS") ||
                        product.getCategory().equals("PLUMBING_SUPPLIES");

            case "PREMIUM":
                return product.getName().contains("Premium") ||
                        product.getName().contains("Elegant");

            default:
                return random.nextBoolean(); // Random for others
        }
    }

    private int determineItemCount(MockClient client, String context) {
        int baseCount;
        switch (client.type) {
            case "WHOLESALE":
                baseCount = 5 + random.nextInt(8); // 5-12 items
                break;
            case "CONSTRUCTION":
                baseCount = 3 + random.nextInt(5); // 3-7 items
                break;
            case "INDUSTRIAL":
                baseCount = 2 + random.nextInt(6); // 2-7 items
                break;
            default:
                baseCount = 1 + random.nextInt(4); // 1-4 items
        }

        // Adjust based on context
        switch (context) {
            case "BULK":
                return baseCount * 2;
            case "RUSH":
                return Math.min(baseCount, 3);
            case "OFF_HOURS":
            case "WEEKEND":
                return Math.min(baseCount, 2);
            default:
                return baseCount;
        }
    }

    private int generateRealisticQuantity(Product product, MockClient client) {
        int baseQuantity;
        switch (product.getCategory()) {
            case "TILES":
                baseQuantity = 20 + random.nextInt(80); // 20-100 tiles
                break;
            case "CONSTRUCTION_MATERIALS":
                baseQuantity = 5 + random.nextInt(20); // 5-25 units
                break;
            case "ROOFING_MATERIALS":
                baseQuantity = 10 + random.nextInt(30); // 10-40 units
                break;
            case "PLUMBING_SUPPLIES":
                baseQuantity = 3 + random.nextInt(15); // 3-18 units
                break;
            default:
                baseQuantity = 1 + random.nextInt(10);
        }

        // Adjust for client type
        double multiplier;
        switch (client.type) {
            case "WHOLESALE":
                multiplier = 1.5 + random.nextDouble();
                break;
            case "CONSTRUCTION":
                multiplier = 1.2 + random.nextDouble() * 0.8;
                break;
            case "INDUSTRIAL":
                multiplier = 1.3 + random.nextDouble() * 0.7;
                break;
            default:
                multiplier = 0.8 + random.nextDouble() * 0.4;
        }

        return Math.max(1, (int) (baseQuantity * multiplier));
    }

    private double generateRealisticPrice(Product product, MockClient client) {
        // Base prices by category (realistic German market prices)
        double basePrice;
        switch (product.getCategory()) {
            case "TILES":
                basePrice = 15.0 + random.nextDouble() * 35.0; // €15-50 per m²
                break;
            case "CONSTRUCTION_MATERIALS":
                basePrice = 25.0 + random.nextDouble() * 75.0; // €25-100
                break;
            case "ROOFING_MATERIALS":
                basePrice = 20.0 + random.nextDouble() * 60.0; // €20-80
                break;
            case "PLUMBING_SUPPLIES":
                basePrice = 8.0 + random.nextDouble() * 42.0; // €8-50
                break;
            default:
                basePrice = 10.0 + random.nextDouble() * 40.0;
        }

        // Premium clients pay more, wholesale clients pay less
        double priceMultiplier;
        switch (client.type) {
            case "PREMIUM":
                priceMultiplier = 1.2 + random.nextDouble() * 0.3; // 20-50% premium
                break;
            case "WHOLESALE":
                priceMultiplier = 0.7 + random.nextDouble() * 0.2; // 10-30% discount
                break;
            case "CONSTRUCTION":
                priceMultiplier = 0.9 + random.nextDouble() * 0.2; // Small discount
                break;
            default:
                priceMultiplier = 0.95 + random.nextDouble() * 0.1; // Near list price
        }

        return Math.round(basePrice * priceMultiplier * 100.0) / 100.0;
    }

    private LocalDateTime calculateDeliveryDate(String context) {
        int baseDays = 2 + random.nextInt(5); // 2-6 days base

        switch (context) {
            case "RUSH":
                return LocalDateTime.now().plusDays(1); // Next day
            case "BULK":
                return LocalDateTime.now().plusDays(7 + random.nextInt(7)); // 1-2 weeks
            case "OFF_HOURS":
            case "WEEKEND":
                return LocalDateTime.now().plusDays(baseDays + 2); // Longer
            default:
                return LocalDateTime.now().plusDays(baseDays);
        }
    }

    private void processOrderThroughAI(OrderRequest orderRequest) {
        // Create order entity (simplified - normally would go through controller validation)
        try {
            // This is a simplified direct processing - in real system would use OrderController
            System.out.println("🤖 Processing mock order through AI agent...");

            // Here we would actually create and process the order
            // For now, we'll just log it to demonstrate the concept
            System.out.println("📋 Mock Order Details:");
            System.out.println("   Client: " + orderRequest.getClientName());
            System.out.println("   Items: " + orderRequest.getItems().size());
            System.out.println("   Delivery: " + orderRequest.getRequestedDeliveryDate());

        } catch (Exception e) {
            System.err.println("Error processing mock order: " + e.getMessage());
        }
    }

    private MockClient getRandomClient() {
        return mockClients.get(random.nextInt(mockClients.size()));
    }

    private boolean shouldGenerateOrder(double probability) {
        return random.nextDouble() < probability;
    }

    /**
     * Get all mock clients for testing purposes
     */
    public List<MockClient> getAllMockClients() {
        return new ArrayList<>(mockClients);
    }

    /**
     * Generate a specific order for testing
     */
    public OrderRequest generateTestOrder(String clientType) {
        MockClient client = mockClients.stream()
                .filter(c -> c.type.equals(clientType))
                .findFirst()
                .orElse(mockClients.get(0));

        return createOrderRequest(client, "TEST");
    }

    /**
     * Mock client data class
     */
    public static class MockClient {
        public final String id;
        public final String name;
        public final String address;
        public final String type;

        public MockClient(String id, String name, String address, String type) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.type = type;
        }

        @Override
        public String toString() {
            return "MockClient{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}