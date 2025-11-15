/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.security;

/**
 *
 * @author delacth
 */

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter{
  @Autowired private JwtUtil jwtUtil;
    @Autowired private JwtBlacklist jwtBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // LIBERA ENDPOINTS PÚBLICOS
        if (path.matches(".*/api/users/\\d+/perfil.*") ||
            path.startsWith("/api/users/login") ||
            path.startsWith("/api/users/register") ||
            path.startsWith("/api/locais") ||
            path.startsWith("/api/users/logout/") 
                || path.startsWith("/api/anuncios")
                || path.startsWith("/api/users")
                || path.startsWith("/api/locais")
                || path.startsWith("/api/notificacoes")
                || path.startsWith("/api/perfis")
                 || path.startsWith("/api/guardados")) {
            chain.doFilter(request, response);
            return;
        }

        // EXIGE JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT ausente");
            return;
        }

        String token = authHeader.substring(7);

        if (jwtBlacklist.contains(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalidado");
            return;
        }

        try {
            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            if (jwtUtil.validateToken(token, username)) {
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT expirado");
                return;
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT inválido");
            return;
        }

        chain.doFilter(request, response);
    }
}
     
