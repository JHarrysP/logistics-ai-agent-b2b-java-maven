// ============= PRODUCT REPOSITORY =============
package com.logistics.repository;

import com.logistics.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity operations
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Find products by category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find products with low stock (less than specified quantity)
     */
    List<Product> findByStockQuantityLessThan(Integer quantity);
    
    /**
     * Find products by warehouse location
     */
    List<Product> findByLocation(String location);
    
    /**
     * Find products by weight range
     */
    @Query("SELECT p FROM Product p WHERE p.weight BETWEEN :minWeight AND :maxWeight")
    List<Product> findByWeightRange(@Param("minWeight") Double minWeight, 
                                   @Param("maxWeight") Double maxWeight);
    
    /**
     * Find heavy products (weight > 50kg)
     */
    @Query("SELECT p FROM Product p WHERE p.weight > 50.0")
    List<Product> findHeavyProducts();
    
    /**
     * Find fragile products (tiles category)
     */
    @Query("SELECT p FROM Product p WHERE p.category = 'TILES'")
    List<Product> findFragileProducts();
    
    /**
     * Search products by name (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find products available in sufficient quantity
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity >= :requiredQuantity")
    List<Product> findAvailableProducts(@Param("requiredQuantity") Integer requiredQuantity);
    
    /**
     * Count products by category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
    long countByCategory(@Param("category") String category);
}