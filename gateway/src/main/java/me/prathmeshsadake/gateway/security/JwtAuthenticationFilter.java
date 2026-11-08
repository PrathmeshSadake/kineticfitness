package me.prathmeshsadake.gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Validates Bearer JWT at the edge, rejects forged {@code X-User-Id} headers from clients,
 * and injects the trusted user id for downstream services.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    public static final String USER_ID_HEADER = "X-User-Id";

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod())
                || path.startsWith("/api/v1/users/auth/")
                || path.startsWith("/actuator/")) {
            // Never trust client-supplied identity headers on public paths either
            ServerHttpRequest cleaned = request.mutate()
                    .headers(headers -> headers.remove(USER_ID_HEADER))
                    .build();
            return chain.filter(exchange.mutate().request(cleaned).build());
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Claims claims = jwtService.parseClaims(token);
        String userId = claims.getSubject();
        String role = String.valueOf(claims.getOrDefault("role", "USER"));

        ServerHttpRequest mutated = request.mutate()
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.set(USER_ID_HEADER, userId);
                })
                .build();

        var authentication = new UsernamePasswordAuthenticationToken(
                userId,
                token,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        return chain.filter(exchange.mutate().request(mutated).build())
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}
