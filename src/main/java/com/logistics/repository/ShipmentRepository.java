/ ============= SHIPMENT REPOSITORY =============
package com.logistics.repository;

import com.logistics.model.Shipment;
import com.logistics.model.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Shipment entity operations
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    /**
     * Find shipments by status
     */
    List<Shipment> findByStatus(ShipmentStatus status);
    
    /**
     * Find shipments by truck ID
     */
    List<Shipment> findByTruckId(String truckId);
    
    /**
     * Find shipments by driver ID
     */
    List<Shipment> findByDriverId(String driverId);
    
    /**
     * Find shipments by order ID
     */
    @Query("SELECT s FROM Shipment s WHERE s.order.id = :orderId")
    List<Shipment> findByOrderId(@Param("orderId") Long orderId);
    
    /**
     * Find overdue shipments (scheduled pickup time passed but not picked up)
     */
    @Query("SELECT s FROM Shipment s WHERE s.scheduledPickup < :currentTime AND s.actualPickup IS NULL")
    List<Shipment> findOverdueShipments(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find overdue shipments (convenience method using current time)
     */
    default List<Shipment> findOverdueShipments() {
        return findOverdueShipments(LocalDateTime.now());
    }
    
    /**
     * Find shipments scheduled for today
     */
    @Query("SELECT s FROM Shipment s WHERE DATE(s.scheduledPickup) = DATE(:date)")
    List<Shipment> findShipmentsScheduledForDate(@Param("date") LocalDateTime date);
    
    /**
     * Find shipments requiring special handling
     */
    @Query("SELECT s FROM Shipment s WHERE s.requiresSpecialHandling = true")
    List<Shipment> findShipmentsRequiringSpecialHandling();
    
    /**
     * Find shipments in transit
     */
    @Query("SELECT s FROM Shipment s WHERE s.status = 'IN_TRANSIT'")
    List<Shipment> findShipmentsInTransit();
    
    /**
     * Find shipments by client ID
     */
    @Query("SELECT s FROM Shipment s WHERE s.order.clientId = :clientId")
    List<Shipment> findByClientId(@Param("clientId") String clientId);
    
    /**
     * Find shipments delivered between dates
     */
    @Query("SELECT s FROM Shipment s WHERE s.actualDelivery BETWEEN :startDate AND :endDate")
    List<Shipment> findDeliveredBetweenDates(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count shipments by status
     */
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status = :status")
    long countByStatus(@Param("status") ShipmentStatus status);
}