// ============= ORDER REPOSITORY =============
package com.logistics.repository;

import com.logistics.model.Order;
import com.logistics.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Order entity operations
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find orders by status
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Find orders by client ID
     */
    List<Order> findByClientId(String clientId);
    
    /**
     * Find orders by client ID ordered by date descending
     */
    List<Order> findByClientIdOrderByOrderDateDesc(String clientId);
    
    /**
     * Count orders by status
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);
    
    /**
     * Find orders between specific dates
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find overdue orders (requested delivery date passed but not delivered)
     */
    @Query("SELECT o FROM Order o WHERE o.requestedDeliveryDate < :currentDate " +
           "AND o.status NOT IN ('DELIVERED', 'CANCELLED')")
    List<Order> findOverdueOrders(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find orders by product category
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.product.category = :category")
    List<Order> findOrdersByProductCategory(@Param("category") String category);
    
    /**
     * Find orders with total weight greater than specified value
     */
    @Query("SELECT o FROM Order o WHERE o.totalWeight > :weight")
    List<Order> findOrdersWithWeightGreaterThan(@Param("weight") Double weight);
    
    /**
     * Find recent orders (within last N days)
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :sinceDate ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(@Param("sinceDate") LocalDateTime sinceDate);
}