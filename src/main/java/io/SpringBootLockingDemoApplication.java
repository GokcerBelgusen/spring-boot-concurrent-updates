package io;

import io.service.CreditUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "io.*")
@ComponentScan(basePackages = {"io.*"})
@EntityScan("io.*")
@Slf4j
public class SpringBootLockingDemoApplication implements CommandLineRunner {

    static boolean  debug = false;
    @Autowired
    private CreditUpdateService creditUpdateService;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootLockingDemoApplication.class, args);
    }

    private static double generateRandomAmount() {
        Random random = new Random();
        int randomNumber = random.nextInt(20); // Generate a random number between 0 and 20

        double amount = randomNumber * 5.0; // Multiply by 5 to get multiples of

        // Randomly make amount negative
        if (random.nextBoolean()) {
            amount = -amount;
        }
        return amount;
    }

    @Override
    public void run(String... args) throws Exception {


        if (debug) {
            int threadCount = 10; // Number of concurrent threads
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < threadCount; i++) {
                Long customerId = (long) (Math.random() < 0.5 ? 1L : 2L);
                // Generate a random number between 5 and 100, multiples of 5, that can be positive or negative
                double amount = generateRandomAmount();

                log.info("Sending request for customer " + customerId + " transaction amnt " + amount);

                executorService.execute(() -> {
                    //System.out.println("Updating customer credit with pessimistic lock...");
                    creditUpdateService.performCreditUpdateWithPessimisticLock(customerId, amount);

                    //System.out.println("Updating customer credit with optimistic lock...");
                    creditUpdateService.performCreditUpdateWithOptimisticLock(customerId, amount);
                });
            }

            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);

        }
    }
}
