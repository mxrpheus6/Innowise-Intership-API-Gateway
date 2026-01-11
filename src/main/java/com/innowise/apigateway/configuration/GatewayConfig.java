package com.innowise.apigateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class GatewayConfig {

    @Value("${service.auth.url}")
    private String authServiceUrl;

    @Value("${service.order.url}")
    private String orderServiceUrl;

    @Value("${service.user.url}")
    private String userServiceUrl;

    @Value("${service.payment.url}")
    private String paymentServiceUrl;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri(authServiceUrl))
                .route("order-service", r -> r.path("/api/v1/orders/**", "/api/v1/items/**")
                        .uri(orderServiceUrl))
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .uri(userServiceUrl))
                .route("payment-service", r -> r.path("/api/v1/payments/**")
                        .uri(paymentServiceUrl))
                .build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }

}
