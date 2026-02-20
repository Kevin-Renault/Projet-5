package com.openclassrooms.mddapi.config;

import com.openclassrooms.mddapi.security.JwtAuthenticationFilter;
import com.openclassrooms.mddapi.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint) throws Exception {

        http
                .cors(cors -> {
                })
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // For SPA clients that read the XSRF-TOKEN cookie and echo it
                        // back in the X-XSRF-TOKEN header.
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        // Allow first login/register without an existing CSRF cookie.
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/api/auth/login", "POST"),
                                new AntPathRequestMatcher("/api/auth/register", "POST")))
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        // Prevent Spring Security from clearing the CSRF token on
                        // every request that authenticates via our stateless JWT.
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger / OpenAPI
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()

                        // Auth endpoints (accept trailing slash too)
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf", "/api/auth/csrf/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/login/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/register/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh", "/api/auth/refresh/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout", "/api/auth/logout/").permitAll()
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
