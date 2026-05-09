package com.tunhan.micsu.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import com.tunhan.micsu.security.JwtBlacklistFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtDecoderConfiguration jwtDecoderConfiguration;
        private final AuthenticationEntryPointConfiguration authenticationEntryPoint;
        private final JwtBlacklistFilter jwtBlacklistFilter;

        private static final String[] PUBLIC_POST = {
                        "/api/v1/auth/register",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        // "/api/v1/songs", // TODO: remove after testing
                        // "/api/v1/songs/upload", // TODO: remove after testing
                        // "/api/v1/hls", // TODO: remove after testing
                        // "/api/v2/hls", // TODO: remove after testing
                        // "/api/v3/hls" // TODO: remove after testing
                        "/api/test/**",
        };

        private static final String[] PUBLIC_DELETE = {
                        "/api/clean-bucket" // TODO: remove after testing
        };

        private static final String[] PUBLIC_GET = {
                        "/api/v1/songs",
                        "/api/v1/songs/*",
                        "/api/v1/songs/*/stream/**",
                        "/api/v1/search",
                        "/api/v1/search/**",
                        "/api/v1/songs/*/comments",
                        "/api/v1/users/*",
                        "/api/v1/users/*/songs",
                        "/api/v1/users/*/followers",
                        "/api/v1/users/*/following",
                        "/api/v1/users/*/reposts",
                        "/api/v1/users/*/playlists/public",
                        "/api/v1/playlists/*",
                        // Swagger UI
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        // Static OpenAPI docs
                        "/docs/**",
                        // Actuator
                        "/actuator/**" // TODO: remove after testing
        };

        private static final String[] PUBLIC_PATCH = {
                "/api/v1/views/songs/*" // TODO: remove after testing
        };
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.csrf(AbstractHttpConfigurer::disable);
                http.httpBasic(AbstractHttpConfigurer::disable);

                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
                                .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                                .requestMatchers(HttpMethod.DELETE, PUBLIC_DELETE).permitAll()
                                .requestMatchers(HttpMethod.PATCH, PUBLIC_PATCH).permitAll()
                                .anyRequest().authenticated());

                http.oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwt -> jwt
                                                .decoder(jwtDecoderConfiguration.jwtDecoder())
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                .authenticationEntryPoint(authenticationEntryPoint));

                http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                // Blacklist check must run before Spring's BearerTokenAuthenticationFilter
                http.addFilterBefore(jwtBlacklistFilter, BearerTokenAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthorityPrefix("");
                grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                return converter;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(10);
        }

}
