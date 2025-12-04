/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package AnunciosLocBackend.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author delacth
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // REMOVEU HttpServletRequest request
       http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
                 .requestMatchers(
                    "/api/users/login",
                    "/api/users/register",
                    "/uploads/**",
                    "/uploads/imagens/**",
                    "/imagens/**"
                ).permitAll()
                
            .requestMatchers("/api/users/login", "/api/users/register", "/api/locais/**").permitAll()
            .requestMatchers("/api/users/{id}/perfil").permitAll()
            .requestMatchers("/api/users/{id}/perfil/{chave}").permitAll()
            // logout requires authentication (handled by JwtFilter + controller checks)
            .requestMatchers("/api/users/logout-raw/**").permitAll()
            .requestMatchers("/api/anuncios/**").permitAll()
            .requestMatchers("/api/users").permitAll()
            .requestMatchers("/api/users/*/alterar-senha").permitAll()
            .requestMatchers("/api/locais/search").permitAll()
            .requestMatchers("/api/anuncios/centralizado/broadcast").permitAll()
            .requestMatchers("/api/users/{id}/fcm-token").permitAll()
            .requestMatchers("/api/notificacoes/**").permitAll()
            .requestMatchers("/api/perfis/**").permitAll()
            .requestMatchers("/api/guardados/**").permitAll()
            .requestMatchers("/api/notificacoes/count").permitAll()    
            .requestMatchers("/api/users/{id}/perfil").permitAll() 
            .requestMatchers("/api/users/{id}/reset-password").permitAll()
            .requestMatchers("/api/users/{id}/change-username").permitAll()
            .anyRequest().authenticated()  
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}