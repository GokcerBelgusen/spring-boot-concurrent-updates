package io;


import io.dao.Customer;
import io.repository.CustomerRepository;
import io.service.CreditUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class TestOptimisticLocking {

    @Autowired
    private CreditUpdateService customerService;

    @Autowired
    private CustomerRepository customerRepository;


    public void setup() {

        // Clean up and initialize customer data
        customerRepository.deleteAll();

        // Initialize customers with known state
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setName("Customer 1");
        customer1.setCreditLimit(0);
        customer1.setCurrentCredit(0);
        customer1.setVersion(1);
        customerRepository.save(customer1);

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("Customer 2");
        customer2.setCreditLimit(0);
        customer2.setCurrentCredit(0);
        customer1.setVersion(1);
        customerRepository.save(customer2);

        // Logging to verify data
        List<Customer> customers = customerRepository.findAll();
        log.info("Initialized Customers: {}", customers);
    }

    @Test
    @Transactional
    public void testLostUpdate() throws Exception {

        Long customerId = 1L;

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Transaction 1
        Future<Void> future1 = executor.submit(() -> {
            customerService.performCustomerCreditRead(customerId);
            customerService.performCreditUpdateWithOptimisticLock(customerId, 50.0);
            return null;
        });

        // Transaction 2
        Future<Void> future2 = executor.submit(() -> {
            customerService.performCustomerCreditRead(customerId);
            customerService.performCreditUpdateWithOptimisticLock(customerId, -30.0);
            return null;
        });

        future1.get();
        future2.get();

        double finalCredit = customerService.performCustomerCreditRead(customerId).getCreditLimit();
        System.out.println("finalCredit() " + finalCredit);
        assertEquals(20.0, finalCredit, "Lost update occurred as the final credit should reflect only one successful update.");
    }

    @Test
    @Transactional
    public void testStaleDataReads() throws Exception {

        Long customerId = 1L;
        double initialCredit = customerService.performCustomerCreditRead(customerId).getCreditLimit();

        // Simulate an update in a separate transaction
        new Thread(() -> {
            customerService.performCreditUpdateWithOptimisticLock(customerId, 50.0);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        double updatedCredit = customerService.performCustomerCreditRead(customerId).getCreditLimit();
        System.out.println("initialCredit " + initialCredit + " updatedCredit " + updatedCredit);

        assertNotEquals(initialCredit, updatedCredit, "Stale data read occurred, the read after update should reflect the new credit.");
    }

    @Test
    @Transactional
    public void testOptimisticLockException() throws Exception {

        Thread.sleep(10000L);

        Long customerId = 1L;

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Void> future1 = executor.submit(() -> {
            customerService.performCreditUpdateWithOptimisticLock(customerId, 50.0);
            return null;
        });

        Thread.sleep(500); // Ensure the first transaction is processed

        Future<Void> future2 = executor.submit(() -> {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                customerService.performCreditUpdateWithOptimisticLock(customerId, -30.0);
            });
            assertTrue(exception.getMessage().contains("Row was updated or deleted by another transaction"), "Expected optimistic lock exception did not occur.");
            return null;
        });

        future1.get();
        future2.get();
    }
}
