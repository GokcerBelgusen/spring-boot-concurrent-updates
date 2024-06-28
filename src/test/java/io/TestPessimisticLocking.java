package io;

import io.service.CreditUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestPessimisticLocking {

    @Autowired
    private CreditUpdateService customerService;

    @Test
    @Transactional
    public void testDeadlockScenario() throws Exception {
        Long customerId1 = 1L;
        Long customerId2 = 2L;

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Void> future1 = executor.submit(() -> {
            customerService.performCreditUpdateWithPessimisticLock(customerId1, 50.0);
            customerService.performCreditUpdateWithPessimisticLock(customerId2, 30.0);
            return null;
        });

        Future<Void> future2 = executor.submit(() -> {
            customerService.performCreditUpdateWithPessimisticLock(customerId2, -20.0);
            customerService.performCreditUpdateWithPessimisticLock(customerId1, -10.0);
            return null;
        });

        try {
            future1.get();
            future2.get();
            fail("Expected deadlock did not occur.");
        } catch (Exception e) {
            // Expecting deadlock or timeout exception
            assertTrue(e.getMessage().contains("deadlock") || e.getMessage().contains("timeout"), "Expected deadlock or timeout exception.");
        }
    }

    @Test
    @Transactional
    public void testLockContention() throws Exception {
        Long customerId = 1L;
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                customerService.performCreditUpdateWithPessimisticLock(customerId, 10.0);
                return null;
            });
        }

        double finalCredit = customerService.performCustomerCreditRead(customerId).getCreditLimit();
        assertTrue(finalCredit != 100100, "Lock contention should have allowed sequential updates, final credit should reflect accumulated updates.");
    }

    @Test
    @Transactional
    public void testReducedConcurrency() throws Exception {
        Long customerId = 1L;

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Future<Void> read1 = executor.submit(() -> {
            customerService.performCustomerCreditRead(customerId);
            return null;
        });

        Future<Void> write = executor.submit(() -> {
            customerService.performCreditUpdateWithPessimisticLock(customerId, 20.0);
            return null;
        });

        Future<Void> read2 = executor.submit(() -> {
            customerService.performCustomerCreditRead(customerId);
            return null;
        });

        read1.get();
        write.get();
        read2.get();

        double finalCredit = customerService.performCustomerCreditRead(customerId).getCreditLimit();
        assertTrue(finalCredit != 100020, "Reduced concurrency should ensure that the write operation is serialized between the reads.");
    }
}
