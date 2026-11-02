package sdpp_backend.config;

import java.time.LocalDateTime;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import sdpp_backend.security.JwtAuthFilter;

/**
 * Autenticação stateless por JWT e autorização por perfil (RF-SEG-01, RF-SEG-02, RNF-SEG-01).
 * As regras por pessoa e equipe ficam nos services (AcessoService).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Operações exclusivas do RH são barradas antes de ler/validar o corpo da requisição
                        // (o @PreAuthorize nos controllers continua como segunda camada).
                        .requestMatchers("/api/usuarios/**").hasRole("RH")
                        .requestMatchers(HttpMethod.GET, "/api/pesquisas-clima/*/resultados").hasRole("RH")
                        .requestMatchers(HttpMethod.POST, "/api/colaboradores", "/api/departamentos", "/api/cargos",
                                "/api/competencias", "/api/ciclos", "/api/ciclos/*/liberar",
                                "/api/ciclos/*/liberar-resultados", "/api/pesquisas-clima",
                                "/api/pesquisas-clima/*/perguntas", "/api/pesquisas-clima/*/publicar",
                                "/api/pesquisas-clima/*/encerrar").hasRole("RH")
                        .requestMatchers(HttpMethod.PUT, "/api/colaboradores/*", "/api/departamentos/*", "/api/cargos/*",
                                "/api/competencias/*", "/api/ciclos/*", "/api/ciclos/*/competencias",
                                "/api/pesquisas-clima/*", "/api/pesquisas-clima/perguntas/*",
                                "/api/nine-box/quadrantes/*").hasRole("RH")
                        .requestMatchers(HttpMethod.DELETE, "/api/colaboradores/*", "/api/departamentos/*",
                                "/api/cargos/*", "/api/competencias/*", "/api/ciclos/*", "/api/pesquisas-clima/*",
                                "/api/pesquisas-clima/perguntas/*").hasRole("RH")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escreverErro(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                                        "Autenticação necessária."))
                        .accessDeniedHandler((req, res, ex) ->
                                escreverErro(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                                        "Você não tem permissão para realizar esta operação.")))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** O filtro JWT roda só dentro da cadeia do Spring Security, não como filtro de servlet avulso. */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filtro) {
        FilterRegistrationBean<JwtAuthFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(SdppProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(props.cors().origens());
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static void escreverErro(HttpServletResponse res, int status, String erro, String mensagem)
            throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"erro\":\"" + erro + "\",\"mensagem\":\""
                + mensagem + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}");
    }
}
