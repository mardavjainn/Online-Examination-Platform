package com.example.submission_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            if (!jwtUtil.validateToken(jwt)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            String email = jwtUtil.extractEmail(jwt);
            String role = jwtUtil.extractRole(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String formattedRole = role == null || role.isBlank() ? "USER" : role.toUpperCase().trim();
                if (!formattedRole.startsWith("ROLE_")) {
                    formattedRole = "ROLE_" + formattedRole;
                }
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        email, null,
                        List.of(new SimpleGrantedAuthority(formattedRole))
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", "Unauthorized");
        body.put("message", message);

        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
