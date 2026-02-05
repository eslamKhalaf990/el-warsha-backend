package com.warsha.erp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService adminService;
    private final CustomerDetailsService customerService;

    // Formatter for consistent log timestamps
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   CustomUserDetailsService adminService,
                                   CustomerDetailsService customerService) {
        this.jwtUtil = jwtUtil;
        this.adminService = adminService;
        this.customerService = customerService;
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();
        String username = null;
        String jwtToken = null;

        // 1. Check for JWT in Header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
                System.out.println("[" + getTimestamp() + "] INFO: JWT detected for user: " + username + " on URI: " + requestURI);
            } catch (Exception e) {
                System.out.println("[" + getTimestamp() + "] WARN: Failed to extract username from JWT: " + e.getMessage());
            }
        }

        // 2. Authenticate user if not already authenticated in this context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;

            try {
                // Try loading as Admin first
                System.out.println("[" + getTimestamp() + "] INFO: Attempting to load as ADMIN: " + username);
                userDetails = adminService.loadUserByUsername(username);
            } catch (Exception e) {
                // If not found, try loading as Customer
                try {
                    System.out.println("[" + getTimestamp() + "] INFO: Admin not found. Attempting to load as CUSTOMER: " + username);
                    userDetails = customerService.loadUserByUsername(username);
                } catch (Exception ex) {
                    System.out.println("[" + getTimestamp() + "] ERROR: Security breach or invalid user. Not found in Admin or Customer tables: " + username);
                }
            }

            // 3. Validate token against the found userDetails
            if (userDetails != null && jwtUtil.validateToken(jwtToken, userDetails)) {
                System.out.println("[" + getTimestamp() + "] SUCCESS: JWT Validated. Setting security context for: " + username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else if (userDetails != null) {
                System.out.println("[" + getTimestamp() + "] ERROR: Token validation failed for user: " + username);
            }
        }

        chain.doFilter(request, response);
    }
}