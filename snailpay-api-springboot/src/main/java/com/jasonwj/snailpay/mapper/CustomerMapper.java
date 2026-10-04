package com.jasonwj.snailpay.mapper;

import com.jasonwj.snailpay.dto.CustomerRegistrationRequest;
import com.jasonwj.snailpay.dto.CustomerRegistrationResponse;
import com.jasonwj.snailpay.models.Customer;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerRegistrationRequest request);

    CustomerRegistrationResponse toResponse(Customer customer);
}