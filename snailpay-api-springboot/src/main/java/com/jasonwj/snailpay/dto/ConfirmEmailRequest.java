package com.jasonwj.snailpay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmEmailRequest(

        @NotBlank
        @Size(max = 128)
        String token
) {
}