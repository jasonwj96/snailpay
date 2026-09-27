package com.jasonwj.snailpay.mapper;

import com.jasonwj.snailpay.dto.CustomerRegistrationRequest;
import com.jasonwj.snailpay.dto.CustomerResponse;
import com.jasonwj.snailpay.models.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface CustomerMapper {
    Customer toEntity(CustomerRegistrationRequest request);

    CustomerResponse toResponse(Customer customer);
}