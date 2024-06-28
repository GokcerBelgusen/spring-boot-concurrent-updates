package io.repository;

import io.dao.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE c.id = :customerId")
    Optional<Customer> readById(@Param("customerId") Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customer c WHERE c.id = :customerId")
    Optional<Customer> findByIdForUpdate(@Param("customerId") Long customerId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT c FROM Customer c WHERE c.id = :customerId")
    Optional<Customer> findById(@Param("customerId") Long customerId);
}
