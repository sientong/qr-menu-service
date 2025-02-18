package com.qrmenu.config;

import com.qrmenu.filter.RateLimitFilter;
import com.qrmenu.filter.TokenAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final TokenAuthenticationFilter tokenAuthFilter;
    private final RateLimitFilter rateLimitFilter;
    private final AuthenticationProvider authenticationProvider;

    @Autowired
    public SecurityConfig(TokenAuthenticationFilter tokenAuthFilter, 
                          RateLimitFilter rateLimitFilter,
                          AuthenticationProvider authenticationProvider) {
        this.tokenAuthFilter = tokenAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(false);
        firewall.setAllowUrlEncodedSlash(false);
        firewall.setAllowBackSlash(false);
        firewall.setAllowUrlEncodedPercent(false);
        firewall.setAllowUrlEncodedPeriod(false);
        return firewall;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", 
                               "/v3/api-docs/**",
                               "/swagger-ui/**",
                               "/actuator/health").permitAll()
                .requestMatchers("/api/v1/stock/**").hasAnyRole("RESTAURANT_ADMIN", "RESTAURANT_MANAGER")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(rateLimitFilter, TokenAuthenticationFilter.class)
            .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} 