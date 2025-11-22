package com.marketplacem.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtUtil jwtUtil;

    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        logger.debug("Processing request: {}", request.getRequestURI());

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            logger.debug("Found JWT token in request");

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
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.debug("Token validation failed");
                }
            } catch (Exception e) {
                logger.error("JWT token validation error", e);
            }
        } else {
            logger.debug("No Authorization header or not a Bearer token");
        }

        chain.doFilter(request, response);
    }
}