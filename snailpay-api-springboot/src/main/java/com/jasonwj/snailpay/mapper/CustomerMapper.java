package com.jasonwj.snailpay.mapper;

import com.jasonwj.snailpay.dto.CustomerRegistrationRequest;
import com.jasonwj.snailpay.dto.CustomerRegistrationResponse;
import com.jasonwj.snailpay.models.Customer;
import org.mapstruct.Mapper;


@Mapper
public interface CustomerMapper {
    Customer toEntity(CustomerRegistrationRequest request);

    CustomerRegistrationResponse toResponse(Customer customer);
}