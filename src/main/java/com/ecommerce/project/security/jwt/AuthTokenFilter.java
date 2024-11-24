package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// This class is responsible for filtering HTTP requests and checking if a valid JWT token exists.
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    // Injecting the JwtUtils and UserDetailsService dependencies
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // Logger for debugging purposes
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    // This method is called for every HTTP request to check if the JWT token is valid
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Log the URI of the request for debugging
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());

        try {
            // Extract JWT token from the request header
            String jwt = parseJwt(request);

            // If the token is not null and is valid, proceed with authentication
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // Extract the username (subject) from the JWT token
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // Load user details using the extracted username
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Create an authentication object with user details and authorities
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null, // No password needed after authentication
                                userDetails.getAuthorities()); // Authorities (roles/permissions)

                // Log the roles from JWT for debugging
                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

                // Set additional details from the HTTP request, like IP address
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the SecurityContext so Spring Security recognizes the user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Log any exceptions (e.g., invalid token, parsing errors)
            logger.error("Cannot set user authentication: {}", e);
        }

        // Continue with the filter chain to allow further processing of the request
        filterChain.doFilter(request, response);
    }

    // Helper method to extract the JWT from the Authorization header
    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromCookies(request); // Use JwtUtils to get token from header
        logger.debug("AuthTokenFilter.java: {}", jwt); // Log the extracted token for debugging
        return jwt; // Return the token, or null if not found
    }
}
