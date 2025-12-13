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
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService adminService;      // Renamed for clarity
    private final CustomerDetailsService customerService;     // New service

    // Update Constructor
    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   CustomUserDetailsService adminService,
                                   CustomerDetailsService customerService) {
        this.jwtUtil = jwtUtil;
        this.adminService = adminService;
        this.customerService = customerService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                logger.warn("Invalid JWT token format");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = null;

            // --- CHANGED LOGIC START ---
            try {
                // 1. Try loading as Admin
                userDetails = adminService.loadUserByUsername(username);
            } catch (Exception e) {
                // 2. If not found, try loading as Customer
                try {
                    userDetails = customerService.loadUserByUsername(username);
                } catch (Exception ex) {
                    logger.error("User not found in either Admin or Customer tables: " + username);
                }
            }
            // --- CHANGED LOGIC END ---

            if (userDetails != null && jwtUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }
}