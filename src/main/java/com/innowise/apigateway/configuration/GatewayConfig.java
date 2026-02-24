package com.innowise.apigateway.configuration;

import com.innowise.apigateway.security.RedisLogoutHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.HttpStatusReturningServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
public class GatewayConfig {

    @Value("${service.auth.url}")
    private String authServiceUrl;

    @Value("${service.order.url}")
    private String orderServiceUrl;

    @Value("${service.user.url}")
    private String userServiceUrl;

    @Value("${service.payment.url}")
    private String paymentServiceUrl;

    private final RedisLogoutHandler redisLogoutHandler;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri(authServiceUrl))
                .route("order-service", r -> r.path("/api/v1/orders/**", "/api/v1/items/**")
                        .filters(f -> f.removeRequestHeader("Cookie"))
                        .uri(orderServiceUrl))
                .route("user-service", r -> r.path("/api/v1/users/**", "/api/v1/card-infos/**")
                        .filters(f -> f.removeRequestHeader("Cookie"))
                        .uri(userServiceUrl))
                .route("payment-service", r -> r.path("/api/v1/payments/**")
                        .filters(f -> f.removeRequestHeader("Cookie"))
                        .uri(paymentServiceUrl))
                .build();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        return serverHttpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated()
                ).oauth2ResourceServer((oauth) -> oauth
                        .jwt(Customizer.withDefaults()))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutHandler(redisLogoutHandler)
                        .logoutSuccessHandler(
                                new HttpStatusReturningServerLogoutSuccessHandler(HttpStatus.OK)
                        ))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://frontend:5173"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
