package io.service;

import io.dao.Customer;
import org.hibernate.StaleStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.repository.CustomerRepository;

import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;


    @Transactional
    public Customer readCustomerCreditLimit(Long customerId) {
        Optional<Customer> customerOpt = customerRepository.readById(customerId);
        return customerOpt.get();
    }

    // Pessimistic Locking Method
    @Transactional
    public void updateCustomerCreditPessimistic(Long customerId, double amount) {
        Optional<Customer> customerOpt = customerRepository.findByIdForUpdate(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setCreditLimit(customer.getCreditLimit() + amount);
            customerRepository.save(customer);
            System.out.println("Updated customer credit with pessimistic lock");
        } else {
            throw new RuntimeException("Customer not found");
        }
    }

    @Transactional
    public void updateCustomerCreditOptimistic(Long customerId, double amount) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setCreditLimit(customer.getCreditLimit() + amount);
        customerRepository.save(customer);
    }

}
