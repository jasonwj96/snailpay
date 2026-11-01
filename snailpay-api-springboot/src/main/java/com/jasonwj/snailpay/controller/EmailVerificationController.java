package com.jasonwj.snailpay.controller;

import com.jasonwj.snailpay.dto.ConfirmEmailRequest;
import com.jasonwj.snailpay.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    // Called by the wizard step (initial send and "Resend").
    @PostMapping("/customers/{externalId}/email-verification/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> send(@PathVariable String externalId) {
        return emailVerificationService.sendVerificationEmail(externalId);
    }

    // Called by the /verify-email page after the user clicks the link.
    @PostMapping(value = "/email-verification/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> confirm(@RequestBody @Valid ConfirmEmailRequest request) {
        return emailVerificationService.confirmEmail(request.token());
    }
}