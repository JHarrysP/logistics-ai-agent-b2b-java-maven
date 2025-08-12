package com.logistics.service;

import com.logistics.dto.OrderItemRequest;
import com.logistics.dto.OrderRequest;
import com.logistics.model.Product;
import com.logistics.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bulk Testing Service for Real-World Load Testing
 */
@Service
public class BulkTestingService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final Random random = new Random();

    // Realistic German B2B client data
    private final List<TestClient> testClients = Arrays.asList(
            // Construction Companies
            new TestClient("HOCHTIEF_001", "HOCHTIEF Solutions AG", "Infrastructure Division, Essen, Germany", "LARGE_CONSTRUCTION", 50000),
            new TestClient("STRABAG_001", "STRABAG SE Hamburg", "Commercial Projects, Hamburg, Germany", "LARGE_CONSTRUCTION", 45000),
            new TestClient("BAM_001", "BAM Deutschland AG", "Residential Division, Berlin, Germany", "MEDIUM_CONSTRUCTION", 25000),
            new TestClient("GOLDBECK_001", "GOLDBECK GmbH", "Industrial Buildings, Bielefeld, Germany", "MEDIUM_CONSTRUCTION", 30000),

            // Wholesale Distributors
            new TestClient("BAUHAUS_001", "BAUHAUS AG Wholesale", "Distribution Center, Mannheim, Germany", "MAJOR_WHOLESALE", 100000),
            new TestClient("HORNBACH_001", "HORNBACH Baumarkt AG", "B2B Division, Bornheim, Germany", "MAJOR_WHOLESALE", 80000),
            new TestClient("OBI_001", "OBI Group Holding B2B", "Professional Sales, Cologne, Germany", "MAJOR_WHOLESALE", 75000),

            // Specialized Contractors
            new TestClient("DACHDECKEREI_HAM", "Hamburg Roofing Specialists GmbH", "Roofing Projects, Hamburg, Germany", "ROOFING_SPECIALIST", 15000),
            new TestClient("FLIESEN_MEISTER", "Fliesen Meister München", "Tile Installation, Munich, Germany", "TILE_SPECIALIST", 12000),
            new TestClient("ROHRLEITUNGSBAU", "Rohrleitungsbau Nord GmbH", "Plumbing Contractor, Bremen, Germany", "PLUMBING_SPECIALIST", 18000),

            // Industrial Clients
            new TestClient("VOLKSWAGEN_FAC", "Volkswagen Factory Logistics", "Wolfsburg Manufacturing, Germany", "INDUSTRIAL", 200000),
            new TestClient("SIEMENS_CONST", "Siemens Construction Division", "Infrastructure Projects, Munich, Germany", "INDUSTRIAL", 150000),
            new TestClient("THYSSENKRUPP", "ThyssenKrupp Materials", "Steel Construction, Duisburg, Germany", "INDUSTRIAL", 120000)
    );

    /**
     * Generate massive bulk orders for stress testing
     */
    public void generateBulkOrders(int numberOfOrders, String intensity) {
        System.out.println("🚀 BULK TEST: Starting generation of " + numberOfOrders + " orders with " + intensity + " intensity");

        switch (intensity.toLowerCase()) {
            case "light":
                generateLightLoad(numberOfOrders);
                break;
            case "medium":
                generateMediumLoad(numberOfOrders);
                break;
            case "heavy":
                generateHeavyLoad(numberOfOrders);
                break;
            case "stress":
                generateStressLoad(numberOfOrders);
                break;
            default:
                generateRealisticMix(numberOfOrders);
        }
    }

    /**
     * Light load: Small orders, normal timing
     */
    private void generateLightLoad(int numberOfOrders) {
        for (int i = 0; i < numberOfOrders; i++) {
            try {
                TestClient client = getRandomClientByType("MEDIUM_CONSTRUCTION", "TILE_SPECIALIST", "PLUMBING_SPECIALIST");
                OrderRequest order = createRealisticOrder(client, 1, 3, false);
                submitOrderAsync(order, "LIGHT_LOAD_" + i);

                // Small delay between orders
                Thread.sleep(100 + random.nextInt(200)); // 100-300ms
            } catch (Exception e) {
                System.err.println("Error in light load generation: " + e.getMessage());
            }
        }
    }

    /**
     * Medium load: Mixed order sizes, faster timing
     */
    private void generateMediumLoad(int numberOfOrders) {
        for (int i = 0; i < numberOfOrders; i++) {
            try {
                TestClient client = getRandomClient();
                OrderRequest order = createRealisticOrder(client, 2, 8, false);
                submitOrderAsync(order, "MEDIUM_LOAD_" + i);

                Thread.sleep(50 + random.nextInt(100)); // 50-150ms
            } catch (Exception e) {
                System.err.println("Error in medium load generation: " + e.getMessage());
            }
        }
    }

    /**
     * Heavy load: Large orders, bulk quantities
     */
    private void generateHeavyLoad(int numberOfOrders) {
        for (int i = 0; i < numberOfOrders; i++) {
            try {
                TestClient client = getRandomClientByType("MAJOR_WHOLESALE", "LARGE_CONSTRUCTION", "INDUSTRIAL");
                OrderRequest order = createRealisticOrder(client, 5, 15, true);
                submitOrderAsync(order, "HEAVY_LOAD_" + i);

                Thread.sleep(25 + random.nextInt(75)); // 25-100ms
            } catch (Exception e) {
                System.err.println("Error in heavy load generation: " + e.getMessage());
            }
        }
    }

    /**
     * Stress test: Maximum load, minimal delays
     */
    private void generateStressLoad(int numberOfOrders) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfOrders; i++) {
            final int orderIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    TestClient client = getRandomClient();
                    OrderRequest order = createRealisticOrder(client, 3, 12, random.nextBoolean());
                    submitOrderSync(order, "STRESS_LOAD_" + orderIndex);
                } catch (Exception e) {
                    System.err.println("Error in stress load generation: " + e.getMessage());
                }
            });
            futures.add(future);
        }

        // Wait for all orders to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        System.out.println("🔥 STRESS TEST: All " + numberOfOrders + " orders submitted!");
    }

    /**
     * Realistic mix: Simulates real-world distribution
     */
    private void generateRealisticMix(int numberOfOrders) {
        System.out.println("🎯 Generating realistic order mix...");

        // Realistic distribution: 60% small, 30% medium, 10% large
        int smallOrders = (int) (numberOfOrders * 0.6);
        int mediumOrders = (int) (numberOfOrders * 0.3);
        int largeOrders = numberOfOrders - smallOrders - mediumOrders;

        // Generate small orders (1-3 items)
        generateOrderBatch(smallOrders, 1, 3, false, "REALISTIC_SMALL");

        // Generate medium orders (4-8 items)
        generateOrderBatch(mediumOrders, 4, 8, false, "REALISTIC_MEDIUM");

        // Generate large orders (9-20 items)
        generateOrderBatch(largeOrders, 9, 20, true, "REALISTIC_LARGE");
    }

    private void generateOrderBatch(int count, int minItems, int maxItems, boolean bulk, String prefix) {
        for (int i = 0; i < count; i++) {
            try {
                TestClient client = bulk ?
                        getRandomClientByType("MAJOR_WHOLESALE", "LARGE_CONSTRUCTION", "INDUSTRIAL") :
                        getRandomClient();

                OrderRequest order = createRealisticOrder(client, minItems, maxItems, bulk);
                submitOrderAsync(order, prefix + "_" + i);

                // Realistic timing: spread orders over time
                Thread.sleep(random.nextInt(500) + 100); // 100-600ms
            } catch (Exception e) {
                System.err.println("Error in batch generation: " + e.getMessage());
            }
        }
    }

    private OrderRequest createRealisticOrder(TestClient client, int minItems, int maxItems, boolean bulk) {
        List<Product> availableProducts = productRepository.findAll();
        if (availableProducts.isEmpty()) {
            throw new RuntimeException("No products available for testing");
        }

        OrderRequest order = new OrderRequest();
        order.setClientId(client.id);
        order.setClientName(client.name);
        order.setDeliveryAddress(client.address);

        // Realistic delivery dates based on client type
        LocalDateTime deliveryDate = calculateRealisticDeliveryDate(client);
        order.setRequestedDeliveryDate(deliveryDate);

        // Generate items based on client preferences
        List<OrderItemRequest> items = generateItemsForClientType(client, availableProducts, minItems, maxItems, bulk);
        order.setItems(items);

        return order;
    }

    private List<OrderItemRequest> generateItemsForClientType(TestClient client, List<Product> products,
                                                              int minItems, int maxItems, boolean bulk) {
        List<OrderItemRequest> items = new ArrayList<>();
        int itemCount = ThreadLocalRandom.current().nextInt(minItems, maxItems + 1);

        // Filter products relevant to client type
        List<Product> relevantProducts = products.stream()
                .filter(p -> isProductRelevantForClientType(p, client.type))
                .filter(p -> p.getStockQuantity() > 0)
                .collect(java.util.stream.Collectors.toList());

        if (relevantProducts.isEmpty()) {
            relevantProducts = products; // Fallback to all products
        }

        Set<String> usedSkus = new HashSet<>();

        for (int i = 0; i < itemCount && usedSkus.size() < relevantProducts.size(); i++) {
            Product product = relevantProducts.get(random.nextInt(relevantProducts.size()));

            if (usedSkus.contains(product.getSku())) {
                i--; // Try again
                continue;
            }

            usedSkus.add(product.getSku());

            OrderItemRequest item = new OrderItemRequest();
            item.setSku(product.getSku());
            item.setQuantity(calculateRealisticQuantity(client, product, bulk));
            item.setUnitPrice(calculateRealisticPrice(client, product));

            items.add(item);
        }

        return items;
    }

    private int calculateRealisticQuantity(TestClient client, Product product, boolean bulk) {
        int baseQuantity;

        // Base quantity by product category
        switch (product.getCategory()) {
            case "TILES":
                baseQuantity = bulk ? 500 : 50;
                break;
            case "CONSTRUCTION_MATERIALS":
                baseQuantity = bulk ? 200 : 20;
                break;
            case "ROOFING_MATERIALS":
                baseQuantity = bulk ? 150 : 15;
                break;
            case "PLUMBING_SUPPLIES":
                baseQuantity = bulk ? 100 : 10;
                break;
            default:
                baseQuantity = bulk ? 100 : 10;
        }

        // Adjust by client size
        double multiplier = getClientSizeMultiplier(client.type);
        int quantity = (int) (baseQuantity * multiplier * (0.5 + random.nextDouble()));

        return Math.max(1, Math.min(quantity, product.getStockQuantity()));
    }

    private double calculateRealisticPrice(TestClient client, Product product) {
        // Base prices by category (realistic German B2B prices)
        double basePrice;
        switch (product.getCategory()) {
            case "TILES":
                basePrice = 25.0 + random.nextDouble() * 75.0; // €25-100
                break;
            case "CONSTRUCTION_MATERIALS":
                basePrice = 50.0 + random.nextDouble() * 150.0; // €50-200
                break;
            case "ROOFING_MATERIALS":
                basePrice = 30.0 + random.nextDouble() * 120.0; // €30-150
                break;
            case "PLUMBING_SUPPLIES":
                basePrice = 15.0 + random.nextDouble() * 85.0; // €15-100
                break;
            default:
                basePrice = 20.0 + random.nextDouble() * 80.0;
        }

        // Volume discount for large clients
        double discount = getClientDiscount(client.type);
        return Math.round((basePrice * (1 - discount)) * 100.0) / 100.0;
    }

    private LocalDateTime calculateRealisticDeliveryDate(TestClient client) {
        int baseDays;
        switch (client.type) {
            case "INDUSTRIAL":
                baseDays = 7 + random.nextInt(14); // 1-3 weeks
                break;
            case "MAJOR_WHOLESALE":
                baseDays = 3 + random.nextInt(7); // 3-10 days
                break;
            case "LARGE_CONSTRUCTION":
                baseDays = 5 + random.nextInt(10); // 5-15 days
                break;
            default:
                baseDays = 2 + random.nextInt(5); // 2-7 days
        }

        return LocalDateTime.now().plusDays(baseDays).plusHours(random.nextInt(8) + 8); // Business hours
    }

    @Async
    public void submitOrderAsync(OrderRequest order, String testId) {
        submitOrderSync(order, testId);
    }

    private void submitOrderSync(OrderRequest order, String testId) {
        try {
            // Validate order before submission
            if (order == null) {
                System.err.println("❌ " + testId + " - Order is null");
                return;
            }

            if (order.getItems() == null || order.getItems().isEmpty()) {
                System.err.println("❌ " + testId + " - Order has no items");
                return;
            }

            String url = "http://localhost:8080/api/orders/submit";

            // Add timeout and error handling
            try {
                Object response = restTemplate.postForObject(url, order, Object.class);
                System.out.println("📦 " + testId + " - Order submitted for " + 
                    (order.getClientName() != null ? order.getClientName() : "Unknown Client") +
                    " (" + order.getItems().size() + " items)");

                // Record successful processing if monitoring is active
                // This would integrate with PerformanceMonitoringService if needed

            } catch (org.springframework.web.client.RestClientException e) {
                System.err.println("❌ " + testId + " - REST API error: " + e.getMessage());
                throw e; // Re-throw for monitoring
            }

        } catch (Exception e) {
            System.err.println("❌ " + testId + " - Failed to submit order: " + e.getMessage());
            // Log more details for debugging
            if (order != null) {
                System.err.println("   Order details - Client: " + order.getClientName() + 
                    ", Items: " + (order.getItems() != null ? order.getItems().size() : "null"));
            }
        }
    }

    // Helper methods
    private TestClient getRandomClient() {
        return testClients.get(random.nextInt(testClients.size()));
    }

    private TestClient getRandomClientByType(String... types) {
        List<TestClient> filtered = testClients.stream()
                .filter(client -> Arrays.asList(types).contains(client.type))
                .collect(java.util.stream.Collectors.toList());

        return filtered.isEmpty() ? getRandomClient() : filtered.get(random.nextInt(filtered.size()));
    }

    private boolean isProductRelevantForClientType(Product product, String clientType) {
        switch (clientType) {
            case "ROOFING_SPECIALIST":
                return "ROOFING_MATERIALS".equals(product.getCategory());
            case "TILE_SPECIALIST":
                return "TILES".equals(product.getCategory());
            case "PLUMBING_SPECIALIST":
                return "PLUMBING_SUPPLIES".equals(product.getCategory());
            case "LARGE_CONSTRUCTION":
            case "MEDIUM_CONSTRUCTION":
                return "CONSTRUCTION_MATERIALS".equals(product.getCategory()) ||
                        "ROOFING_MATERIALS".equals(product.getCategory());
            case "MAJOR_WHOLESALE":
            case "INDUSTRIAL":
                return true; // Buy everything
            default:
                return random.nextBoolean();
        }
    }

    private double getClientSizeMultiplier(String clientType) {
        switch (clientType) {
            case "INDUSTRIAL": return 5.0;
            case "MAJOR_WHOLESALE": return 4.0;
            case "LARGE_CONSTRUCTION": return 3.0;
            case "MEDIUM_CONSTRUCTION": return 2.0;
            default: return 1.0;
        }
    }

    private double getClientDiscount(String clientType) {
        switch (clientType) {
            case "INDUSTRIAL": return 0.25; // 25% discount
            case "MAJOR_WHOLESALE": return 0.20; // 20% discount
            case "LARGE_CONSTRUCTION": return 0.15; // 15% discount
            case "MEDIUM_CONSTRUCTION": return 0.10; // 10% discount
            default: return 0.05; // 5% discount
        }
    }

    // Test client data class
    static class TestClient {
        final String id;
        final String name;
        final String address;
        final String type;
        final int annualVolume; // Annual order volume in EUR

        TestClient(String id, String name, String address, String type, int annualVolume) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.type = type;
            this.annualVolume = annualVolume;
        }
    }

}