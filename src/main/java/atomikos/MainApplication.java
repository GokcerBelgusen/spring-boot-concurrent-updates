package atomikos;

import atomikos.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.sql.SQLException;

@SpringBootApplication
@ComponentScan(basePackages = {"atomikos.*"})
@Slf4j
public class MainApplication {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(MainApplication.class, args);
        CustomerService customerService = context.getBean(CustomerService.class);

        Runnable task1 = () -> {
            try {
                customerService.selectForUpdate(1L);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };

        Runnable task2 = () -> {
            try {
                // Wait a bit to ensure task1 gets the lock first
                Thread.sleep(1000);
                customerService.updateCustomer(1L, 3000.0);
            } catch (InterruptedException | SQLException e) {
                e.printStackTrace();
            }
        };

        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        context.close();
    }
}
