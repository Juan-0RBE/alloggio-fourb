package co.edu.unbosque.alloggio.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Cadena principal de filtros de seguridad de Spring Security.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth

                        // Rutas públicas
                        .requestMatchers("/auth/**", "/restablecimiento/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/politica-cancelacion/obtener/**").permitAll()

                        // Rutas que exigen autenticación
                        .requestMatchers("/usuario/perfil", "/usuario/desactivar").authenticated()
                        .requestMatchers("/publicacion/**").authenticated()

                        .anyRequest().authenticated()

                /*
                 * // Públicas: autenticación (login, registro, reactivar), recuperación y
                 * Swagger
                 * .requestMatchers("/auth/**", "/restablecimiento/**").permitAll()
                 * .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                 * "/swagger-ui.html").permitAll()
                 * 
                 * // Nuevas de este paso: cualquier usuario autenticado
                 * .requestMatchers("/usuario/perfil", "/usuario/desactivar").authenticated()
                 */
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
     * @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http)
     * throws Exception {
     * 
     * http // Desactiva CSRF .csrf(csrf -> csrf.disable())
     * 
     * // Permite CORS .cors(cors ->
     * cors.configurationSource(corsConfigurationSource()))
     * 
     * // Permitir acceso total a todos los endpoints .authorizeHttpRequests(auth ->
     * auth .anyRequest().permitAll())
     * 
     * // Stateless .sessionManagement(session -> session
     * .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
     * 
     * return http.build(); }
     */
    /**
     * Proveedor de autenticación basado en nuestra clase UserDetailsService.
     */
    /**
     * Proveedor de autenticación basado en nuestra clase UserDetailsService.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Expone el AuthenticationManager del contexto de Spring.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Codificador de contraseñas seguro (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura CORS para permitir peticiones desde el frontend (Angular).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // URL de tu frontend
        cfg.setAllowedOrigins(List.of("http://localhost:4200"));

        // Métodos permitidos
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers permitidos (incluye Authorization)
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));

        // Permitir credenciales (si usas cookies o headers personalizados)
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
