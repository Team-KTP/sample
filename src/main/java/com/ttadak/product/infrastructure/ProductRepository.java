package com.ttadak.product.infrastructure;

import com.ttadak.product.domain.Product;
import com.ttadak.shared.entity.EntityStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStatus(Long id, EntityStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id and p.status = :status")
    Optional<Product> findByIdAndStatusForUpdate(Long id, EntityStatus status);

}
