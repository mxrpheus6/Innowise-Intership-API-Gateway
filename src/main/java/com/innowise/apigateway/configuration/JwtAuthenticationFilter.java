package com.innowise.apigateway.configuration;

import com.innowise.apigateway.client.auth.AuthServiceClient;
import com.innowise.apigateway.client.auth.TokenRequest;
import com.innowise.apigateway.configuration.JwtAuthenticationFilter.Config;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<Config> {

    @Autowired
    @Lazy
    private AuthServiceClient authServiceClient;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();

            if (config.getPublicEndpoints().stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header");
            }

            TokenRequest tokenRequest = new TokenRequest(authHeader.substring(7));

            return authServiceClient.validate(tokenRequest)
                    .flatMap(response -> {
                        if (!response.success()) {
                            return onError(exchange, "Invalid token");
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(e -> onError(exchange, "Token validation error"));
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @RequiredArgsConstructor
    @Data
    public static class Config {
        private List<String> publicEndpoints;
    }
}