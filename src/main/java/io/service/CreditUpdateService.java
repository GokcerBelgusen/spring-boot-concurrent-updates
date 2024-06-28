package io.service;


import io.dao.Customer;
import org.hibernate.StaleStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditUpdateService {

    @Autowired
    private CustomerService customerService;

    public Customer performCustomerCreditRead(Long customerId) {
        return customerService.readCustomerCreditLimit(customerId);
    }

    public void performCreditUpdateWithPessimisticLock(Long customerId, double amount) {
        customerService.updateCustomerCreditPessimistic(customerId, amount);
    }

    public void performCreditUpdateWithOptimisticLock(Long customerId, double amount) {
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                customerService.updateCustomerCreditOptimistic(customerId, amount);
                break;
            } catch (StaleStateException | OptimisticLockingFailureException e) {
                retryCount++;
            }
        }
    }


}
