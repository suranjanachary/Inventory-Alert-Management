package com.inventory.alert.repository;

import com.inventory.alert.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * Case-insensitive partial name match with pagination.
     * Derived query would work for ContainingIgnoreCase alone; JPQL keeps the intent explicit
     * when combining optional active filter in the service later without Criteria boilerplate.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    Page<Product> searchByName(@Param("name") String name, Pageable pageable);

    Page<Product> findByActiveTrueAndAvailableQuantityLessThanEqual(
            Integer threshold, Pageable pageable);

    /**
     * Products currently at or below their own minimum threshold (scheduler candidate set).
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = TRUE
              AND p.availableQuantity <= p.minimumQuantity
            """)
    Page<Product> findActiveBelowMinimum(Pageable pageable);
}
