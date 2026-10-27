package com.jasonwj.snailpay.controller;

import com.jasonwj.snailpay.dto.VerifyPhoneCodeRequest;
import com.jasonwj.snailpay.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/phone-verification/{externalId}/")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> send(@PathVariable String externalId) {
        return phoneVerificationService.sendCode(externalId);
    }

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> verify(
            @PathVariable String externalId,
            @RequestBody @Valid VerifyPhoneCodeRequest request) {
        return phoneVerificationService.verifyCode(externalId, request.code());
    }
}
