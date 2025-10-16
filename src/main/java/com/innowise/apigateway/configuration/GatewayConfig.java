package com.innowise.apigateway.configuration;

import com.innowise.apigateway.constants.ApplicationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${service.auth.url}")
    private String authServiceUrl;

    @Value("${service.order.url}")
    private String orderServiceUrl;

    @Value("${service.user.url}")
    private String userServiceUrl;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        var config = new JwtAuthenticationFilter.Config();
        config.setPublicEndpoints(ApplicationConstants.PUBLIC_ENDPOINTS);

        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(config)))
                        .uri(authServiceUrl))
                .route("order-service", r -> r.path("/api/v1/orders/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(config)))
                        .uri(orderServiceUrl))
                .route("user-service", r -> r.path("/api/v1/products/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(config)))
                        .uri(userServiceUrl))
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
