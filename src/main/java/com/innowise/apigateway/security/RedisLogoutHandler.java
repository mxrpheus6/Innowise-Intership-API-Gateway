package com.innowise.apigateway.security;

import com.innowise.apigateway.service.TokenBlacklistService;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RedisLogoutHandler implements ServerLogoutHandler {

    private final TokenBlacklistService blacklistService;

    @Override
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String sid = (String) jwtAuth.getToken().getClaims().get("sid");
            Instant expiresAt = jwtAuth.getToken().getExpiresAt();

            if (sid != null && expiresAt != null) {
                long ttlSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();
                if (ttlSeconds > 0) {
                    return blacklistService.addToBlacklist("sid:" + sid, ttlSeconds).then();                }
            }
        }
        return Mono.empty();
    }
}
