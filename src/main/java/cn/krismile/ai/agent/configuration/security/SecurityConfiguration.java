package cn.krismile.ai.agent.configuration.security;

import cn.krismile.ai.agent.configuration.security.filter.AuthenticationWebFilter;
import cn.krismile.ai.agent.configuration.security.handler.CustomServerAccessDeniedHandler;
import cn.krismile.ai.agent.configuration.security.handler.CustomServerAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Spring Security Configuration
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CustomServerAuthenticationEntryPoint customServerAuthenticationEntryPoint() {
        return new CustomServerAuthenticationEntryPoint();
    }

    @Bean
    public CustomServerAccessDeniedHandler customServerAccessDeniedHandler() {
        return new CustomServerAccessDeniedHandler();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            AuthenticationWebFilter authenticationWebFilter,
            CustomServerAuthenticationEntryPoint customServerAuthenticationEntryPoint,
            CustomServerAccessDeniedHandler customServerAccessDeniedHandler) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                // Stateless session
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers("/login/**", "/register/**").permitAll()
                        .pathMatchers("/actuator/**", "/favicon.ico").permitAll()
                        // Knife4j / Swagger
                        .pathMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers("/chat/message", "/webjars/**", "/v3/api-docs/**").permitAll()
                        // Require authentication for all other requests
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter, org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customServerAuthenticationEntryPoint)
                        .accessDeniedHandler(customServerAccessDeniedHandler)
                )
                .build();
    }
}
