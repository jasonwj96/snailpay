package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.dto.CustomerRegistrationRequest;
import com.jasonwj.snailpay.dto.CustomerRegistrationResponse;
import com.jasonwj.snailpay.mapper.CustomerMapper;
import com.jasonwj.snailpay.models.Customer;
import com.jasonwj.snailpay.repository.CustomerRepository;
import com.jasonwj.snailpay.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Transactional
    public CustomerRegistrationResponse createCustomer(CustomerRegistrationRequest request) {
        Customer newCustomer = customerMapper.toEntity(request);
        newCustomer.setExternalId(idGenerator.getPublicCustomerId());

        Customer persisted = customerRepository.save(newCustomer);
        return customerMapper.toResponse(persisted);
    }
}