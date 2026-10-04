package com.jasonwj.snailpay.resource;

import com.jasonwj.snailpay.dto.EmailVerificationResponse;
import com.jasonwj.snailpay.repository.CustomerRepository;
import com.jasonwj.snailpay.service.EmailVerificationService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/email-verification")
@Produces(MediaType.APPLICATION_JSON)
public class EmailVerificationResource {

    @Inject
    EmailVerificationService emailVerificationService;

    @Inject
    CustomerRepository customerRepository;

    @POST
    @Path("/{externalId}")
    public Uni<Response> sendVerificationEmail(@PathParam("externalId") @NotBlank String externalId) {
        return customerRepository.find("externalId", externalId)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Customer not found"))
                .chain(customer -> emailVerificationService.sendVerificationEmail(customer))
                .replaceWith(Response.accepted().build());
    }

    @GET
    public Uni<EmailVerificationResponse> verifyEmail(@QueryParam("token") @NotBlank String token) {
        return emailVerificationService.verifyEmail(token)
                .map(EmailVerificationResponse::new);
    }
}