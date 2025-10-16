package com.innowise.apigateway.client.auth;

import com.innowise.apigateway.constants.ApplicationConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(@Qualifier("authWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<TokenValidationResponse> validate(TokenRequest tokenRequest) {
        return webClient.post()
                .uri(ApplicationConstants.AUTH_SERVICE_VALIDATE_URL)
                .bodyValue(tokenRequest)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class);
    }
}
