package com.openclassrooms.mddapi.config;

import com.openclassrooms.mddapi.security.JwtAuthenticationFilter;
import com.openclassrooms.mddapi.security.RestAuthenticationEntryPoint;
import com.openclassrooms.mddapi.ApiEndpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.SessionManagementFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
        @Bean
        public CookieCsrfTokenRepository csrfTokenRepository() {
                CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
                repository.setCookiePath("/");
                return repository;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        RestAuthenticationEntryPoint authenticationEntryPoint) throws Exception {

                http
                                .cors(cors -> {
                                })
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(csrfTokenRepository())
                                                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                                .authorizeHttpRequests(auth -> auth
                                                // CORS preflight
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // Swagger / OpenAPI
                                                .requestMatchers("/v3/api-docs/**").permitAll()
                                                .requestMatchers("/swagger-ui.html").permitAll()
                                                .requestMatchers("/swagger-ui/**").permitAll()

                                                // Public env endpoint
                                                .requestMatchers(HttpMethod.GET, "/api/env").permitAll()

                                                // Auth endpoints (accept trailing slash too)
                                                .requestMatchers(HttpMethod.GET, ApiEndpoints.AUTH_CSRF,
                                                                ApiEndpoints.AUTH_CSRF + "/")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, ApiEndpoints.AUTH_LOGIN,
                                                                ApiEndpoints.AUTH_LOGIN + "/")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, ApiEndpoints.AUTH_REGISTER,
                                                                ApiEndpoints.AUTH_REGISTER + "/")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, ApiEndpoints.AUTH_REFRESH,
                                                                ApiEndpoints.AUTH_REFRESH + "/")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, ApiEndpoints.AUTH_LOGOUT,
                                                                ApiEndpoints.AUTH_LOGOUT + "/")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                // Place JWT auth AFTER SessionManagementFilter to avoid triggering
                                // session authentication strategies (incl. CSRF token clearing) on
                                // every stateless request.
                                .addFilterAfter(jwtAuthenticationFilter, SessionManagementFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
