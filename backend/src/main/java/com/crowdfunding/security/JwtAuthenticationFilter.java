package com.crowdfunding.security;

import com.crowdfunding.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Skip JWT validation for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final String jwt = authHeader.substring(7);
            System.out.println("JWT Filter: Processing token for request: " + request.getRequestURI());
            final String username = jwtService.extractUsername(jwt);
            System.out.println("JWT Filter: Extracted username: " + username);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("JWT Filter: Loaded user details for: " + username);
                
                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JWT Filter: Authentication set successfully for user: " + username);
                    logger.debug("Authentication set for user: " + username);
                } else {
                    System.out.println("JWT Filter: Token validation FAILED for user: " + username);
                    logger.warn("JWT token validation failed for user: " + username);
                }
            } else if (username == null) {
                System.out.println("JWT Filter: Could not extract username from token");
                logger.warn("Could not extract username from JWT token");
            } else {
                System.out.println("JWT Filter: Authentication already exists in context");
            }
        } catch (Exception e) {
            System.out.println("JWT Filter: ERROR - " + e.getMessage());
            e.printStackTrace();
            logger.error("Cannot set user authentication: " + e.getMessage(), e);
            // Clear any existing authentication on error
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
}
