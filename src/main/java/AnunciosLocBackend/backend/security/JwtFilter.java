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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter{
  @Autowired private JwtUtil jwtUtil;
    @Autowired private JwtBlacklist jwtBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // LIBERA ENDPOINTS PÚBLICOS (apenas os que devem ser públicos)
        // Rotas de arquivos/imagens
        if (path.startsWith("/uploads/") || path.startsWith("/imagens/")) {
            System.out.println("Rota publica de imagem - permitindo acesso");
            chain.doFilter(request, response);
            return;
        }
        
        // Permitir leitura pública de perfis de um utilizador (GET somente)
        if (path.matches(".*/api/users/\\d+/perfil.*") && "GET".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        // Permitir listagem pública de utilizadores (GET /api/users)
        if ("/api/users".equals(path) && "GET".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        if (path.startsWith("/api/users/login") ||
            path.startsWith("/api/users/register") ||
            path.startsWith("/api/locais") ||
            path.startsWith("/api/anuncios") ||
            path.startsWith("/api/notificacoes") ||
            path.startsWith("/api/perfis") ||
            path.startsWith("/api/guardados") ||
            path.matches(".*/api/users/\\d+/reset-password")||
            path.matches(".*/api/users/\\d+/change-username")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Endpoint de atualização de localização requer JWT válido (não é público)
        // Continua para validação JWT abaixo

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
            System.out.println("[JwtFilter] Validando token para path: " + path);
            System.out.println("[JwtFilter] Username extraído: " + username);
            System.out.println("[JwtFilter] UserId extraído: " + userId);
            if (jwtUtil.validateToken(token, username)) {
                // Define atributos no request (para uso nos controllers)
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                
                // CRÍTICO: Configura o SecurityContext para que o Spring Security reconheça o utilizador como autenticado
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                System.out.println("[JwtFilter] Token válido - userId " + userId + " definido no request e SecurityContext");
            } else {
                System.out.println("[JwtFilter] Token inválido ou expirado");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT expirado");
                return;
            }
        } catch (Exception e) {
            System.err.println("[JwtFilter] Erro ao validar token: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT inválido");
            return;
        }

        chain.doFilter(request, response);
    }
}
     
