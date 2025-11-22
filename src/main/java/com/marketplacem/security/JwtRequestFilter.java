package com.marketplacem.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtRequestFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtUtil jwtUtil;

    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.web.server.WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        logger.debug("Processing request: {}", path);

        List<String> authHeader = exchange.getRequest().getHeaders().get("Authorization");

        if (authHeader != null && !authHeader.isEmpty() && authHeader.get(0).startsWith("Bearer ")) {
            String jwt = authHeader.get(0).substring(7);
            logger.debug("Found JWT token in request: {}", jwt);

            try {
                if (jwtUtil.isTokenValid(jwt)) {
                    // Extract username/subject from token
                    String username = jwtUtil.extractUsername(jwt);
                    logger.debug("Valid token for user: {}", username);

                    // Extract roles if your token contains them
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_USER"));

                    // Try to extract roles from token if they exist
                    try {
                        Claims claims = jwtUtil.extractAllClaims(jwt);
                        if (claims.get("roles") != null) {
                            List<String> roles = (List<String>) claims.get("roles");
                            authorities = roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList());
                            logger.debug("Extracted roles: {}", roles);
                        }
                    } catch (Exception e) {
                        logger.debug("No roles found in token or error extracting them", e);
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                } else {
                    logger.debug("Token validation failed");
                }
            } catch (Exception e) {
                logger.error("JWT token validation error", e);
            }
        } else {
            logger.debug("No Authorization header or not a Bearer token");
        }

        return chain.filter(exchange);
    }
}