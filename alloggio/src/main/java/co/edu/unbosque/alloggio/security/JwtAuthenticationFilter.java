package co.edu.unbosque.alloggio.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.unbosque.alloggio.entity.Usuario;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Utilidad para operaciones con tokens JWT. */
    private final JwtUtil jwtUtil;

    /** Servicio para cargar los detalles del usuario. */
    private final UserDetailsService userDetailsService;

    /**
     * Constructor que inicializa las dependencias necesarias para el filtro.
     *
     * @param jwtUtil            Utilidad para operaciones con tokens JWT
     * @param userDetailsService Servicio para cargar los detalles del usuario
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Método principal del filtro que se ejecuta para cada solicitud HTTP. Extrae y
     * valida el token JWT del encabezado de autorización. Si el token es válido,
     * establece la autenticación en el contexto de seguridad.
     *
     * @param request     Solicitud HTTP entrante
     * @param response    Respuesta HTTP saliente
     * @param filterChain Cadena de filtros para continuar el procesamiento
     * @throws ServletException Si ocurre un error durante el procesamiento del
     *                          servlet
     * @throws IOException      Si ocurre un error de entrada/salida
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Error extracting username from token", e);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails) && cuentaOperativa(userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (UsernameNotFoundException e) {
                // Token de un usuario que ya no existe: la petición sigue sin autenticar
                logger.warn("Token de un usuario inexistente: " + username);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Indica si la cuenta puede operar: no restringida ni desactivada, y
     * habilitada.
     * Se revisa en cada petición para que la restricción o la desactivación
     * invaliden los tokens ya emitidos.
     *
     * @param userDetails usuario cargado de la base de datos
     * @return true si la cuenta puede autenticarse
     */
    private boolean cuentaOperativa(UserDetails userDetails) {
        if (!(userDetails instanceof Usuario usuario)) {
            return false;
        }
        return !Usuario.ESTADO_RESTRINGIDA.equals(usuario.getEstadoCuenta())
                && !Boolean.FALSE.equals(usuario.getEstaActivo())
                && usuario.isEnabled() && usuario.isAccountNonLocked();
    }

}