
package com.salessavvy.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.salessavvy.filter.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            .authorizeHttpRequests(auth -> auth

                // Public error and API documentation
                .requestMatchers("/error").permitAll()

                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml"
                ).permitAll()

                // Authentication
                .requestMatchers("/api/auth/**").permitAll()

                // Categories
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/categories/admin/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/categories/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/categories/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/categories/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/categories/**"
                ).hasRole("ADMIN")

                // Products
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/products/admin/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/products/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/products/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/products/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/products/**"
                ).hasRole("ADMIN")

                // Admin orders
                .requestMatchers(
                    "/api/admin/orders/**"
                ).hasRole("ADMIN")

                // Reviews
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/reviews/product/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/reviews/admin/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/reviews/admin/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/reviews/admin/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/reviews"
                ).hasRole("CUSTOMER")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/reviews/my-reviews"
                ).hasRole("CUSTOMER")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/reviews/**"
                ).hasRole("CUSTOMER")

                // Inventory
                .requestMatchers(
                    "/api/inventory/**"
                ).hasRole("ADMIN")

                // Addresses: customers only
                .requestMatchers(
                    "/api/addresses/**"
                ).hasRole("CUSTOMER")

                // Coupons
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/coupons/validate"
                ).hasRole("CUSTOMER")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/coupons/active"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/coupons/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/coupons/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/coupons/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/coupons/**"
                ).hasRole("ADMIN")

                // Admin customers
                .requestMatchers(
                    "/api/admin/customers/**"
                ).hasRole("ADMIN")

                // Admin dashboard
                .requestMatchers(
                    "/api/admin/dashboard/**"
                ).hasRole("ADMIN")

                // Customer cart and wishlist
                .requestMatchers(
                    "/api/cart/**",
                    "/api/wishlist/**"
                ).hasRole("CUSTOMER")

                // Customer orders
                .requestMatchers(
                    "/api/orders/**"
                ).hasRole("CUSTOMER")

                // Checkout: customers only
                .requestMatchers(
                    "/api/checkout/**"
                ).hasRole("CUSTOMER")

                // Payments: refund is admin-only
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/payments/*/refund"
                ).hasRole("ADMIN")

                // All other payment operations are customer-only
                .requestMatchers(
                    "/api/payments/**"
                ).hasRole("CUSTOMER")

                // All remaining endpoints
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
