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
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_ORGANIZER = "ORGANIZER";
    private static final String ROLE_PROFESSOR = "PROFESSOR";

    private static final String PATH_AUTH_ALL = "/auth/**";
    private static final String PATH_LOGOUT = "/auth/logout";
    private static final String PATH_EVENTS = "/events";
    private static final String PATH_EVENTS_ALL = "/events/**";
    private static final String PATH_CATEGORIES = "/categories";
    private static final String PATH_LOCATIONS = "/locations";
    private static final String PATH_REQUIREMENTS = "/requirements";
    private static final String PATH_ORGANIZERS = "/organizers";
    private static final String PATH_ORGANIZERS_ALL = "/organizers/**";
    private static final String PATH_VIEWS_ALL = "/views/**";
    private static final String PATH_ORGANIZER_REQUESTS = "/organizer-requests";
    private static final String PATH_ORGANIZER_REQUESTS_PENDING = "/organizer-requests/pending";
    private static final String PATH_ORGANIZER_REQUESTS_APPROVE = "/organizer-requests/*/approve";
    private static final String PATH_ORGANIZER_REQUESTS_REJECT = "/organizer-requests/*/reject";
    private static final String PATH_ORGANIZER_MEMBERS = "/organizers/*/members";
    private static final String PATH_ORGANIZER_MEMBERS_ALL = "/organizers/*/members/**";
    private static final String PATH_REGISTRATIONS_BULK = "/registrations/*/attendance/bulk";
    private static final String PATH_REGISTRATIONS_EVENT = "/registrations/event/*";

    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .cors(Customizer.withDefaults())
                // API stateless com JWT Bearer (sem sessao/cookies), entao CSRF nao se aplica.
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize


                        .requestMatchers(HttpMethod.POST, PATH_AUTH_ALL).permitAll()
                        //eventos: qualquer autenticado passa pela rota. a validação se e membro da Org ou Admin será feita no Service.
                        .requestMatchers(HttpMethod.GET, PATH_EVENTS_ALL).authenticated()
                        //acoes restritas ao ADMIN
                        .requestMatchers(HttpMethod.GET, PATH_ORGANIZER_REQUESTS_PENDING).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST, PATH_ORGANIZER_REQUESTS_APPROVE).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST, PATH_ORGANIZER_REQUESTS_REJECT).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST, PATH_ORGANIZERS, PATH_ORGANIZERS_ALL).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, PATH_ORGANIZERS_ALL).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, PATH_ORGANIZERS_ALL).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST, PATH_ORGANIZER_MEMBERS).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, PATH_ORGANIZER_MEMBERS_ALL).hasRole(ROLE_ADMIN)
                        //solicitações: exclusiva de professor/organizer/admin
                        .requestMatchers(HttpMethod.PUT, PATH_REGISTRATIONS_BULK)
                        .hasAnyRole(ROLE_PROFESSOR, ROLE_ORGANIZER, ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, PATH_REGISTRATIONS_EVENT)
                        .hasAnyRole(ROLE_PROFESSOR, ROLE_ORGANIZER, ROLE_ADMIN)

                        //solicitacoes: usuario comum pode apenas CRIAR a solicitação
                        .requestMatchers(HttpMethod.POST, PATH_ORGANIZER_REQUESTS).authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                PATH_CATEGORIES,
                                PATH_LOCATIONS,
                                PATH_REQUIREMENTS,
                                PATH_ORGANIZERS,
                                PATH_ORGANIZERS_ALL,
                                PATH_VIEWS_ALL
                        ).authenticated()

                        //solicitações: ADMIN e organizer
                        .requestMatchers(HttpMethod.POST, PATH_CATEGORIES, PATH_EVENTS, PATH_EVENTS_ALL)
                        .hasAnyRole(ROLE_ADMIN, ROLE_ORGANIZER)
                        .requestMatchers(HttpMethod.PATCH, PATH_CATEGORIES, PATH_EVENTS)
                        .hasAnyRole(ROLE_ADMIN, ROLE_ORGANIZER)
                        .requestMatchers(HttpMethod.DELETE, PATH_CATEGORIES, PATH_EVENTS)
                        .hasAnyRole(ROLE_ADMIN, ROLE_ORGANIZER)
                        .anyRequest().authenticated())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl(PATH_LOGOUT)
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
