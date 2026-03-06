package br.com.geac.backend.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final SecurityFilter securityFilter;
    private static final String ADMIN = "ADMIN";
    private static final String ORGANIZER = "ORGANIZER";
    private static final String EVENTS_ENDPOINT = "/events";
    private static final String CATEGORIES_ENDPOINT = "/categories";
    private static final String ORGANIZER_ENDPOINT = "/organizers/**";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize


                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        //eventos: qualquer autenticado passa pela rota. a validação se e membro da Org ou Admin será feita no Service.
                        .requestMatchers(HttpMethod.GET, "/events/**").authenticated()
                        //acoes restritas ao ADMIN
                        .requestMatchers(HttpMethod.GET, "/organizer-requests/pending").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/organizer-requests/*/approve").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/organizer-requests/*/reject").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/organizers", ORGANIZER_ENDPOINT).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, ORGANIZER_ENDPOINT).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, ORGANIZER_ENDPOINT).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/organizers/*/members").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/organizers/*/members/**").hasRole(ADMIN)
                        //solicitações: exclusiva de professor/organizer/admin
                        .requestMatchers(HttpMethod.PUT, "/registrations/*/attendance/bulk").hasAnyRole("PROFESSOR", ORGANIZER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/registrations/event/*").hasAnyRole("PROFESSOR", ORGANIZER, ADMIN)

                        //solicitacoes: usuario comum pode apenas CRIAR a solicitação
                        .requestMatchers(HttpMethod.POST, "/organizer-requests").authenticated()
                        .requestMatchers(HttpMethod.GET, CATEGORIES_ENDPOINT, "/locations", "/requirements", "/organizers", "/organizers/**,", "/views/**").authenticated()

                        //solicitações: ADMIN e organizer
                        .requestMatchers(HttpMethod.POST, CATEGORIES_ENDPOINT, EVENTS_ENDPOINT, "/events/**").hasAnyRole(ADMIN, ORGANIZER)
                        .requestMatchers(HttpMethod.PATCH, CATEGORIES_ENDPOINT, EVENTS_ENDPOINT).hasAnyRole(ADMIN, ORGANIZER)
                        .requestMatchers(HttpMethod.DELETE, CATEGORIES_ENDPOINT, EVENTS_ENDPOINT).hasAnyRole(ADMIN, ORGANIZER)
                        .anyRequest().authenticated())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .permitAll()
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true))
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
