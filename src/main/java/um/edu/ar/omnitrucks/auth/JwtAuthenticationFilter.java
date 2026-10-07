package um.edu.ar.omnitrucks.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import um.edu.ar.omnitrucks.usuario.Rol;

/**
 * Filtro que intercepta cada solicitud HTTP entrante para extraer y validar el
 * token JWT del encabezado 'Authorization: Bearer <token>'.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(BEARER_PREFIX.length()).trim();

		try {
			String email = jwtService.extraerEmail(token);

			if (StringUtils.hasText(email) && SecurityContextHolder.getContext().getAuthentication() == null) {
				if (jwtService.esTokenValido(token, email)) {
					Rol rol = jwtService.extraerRol(token);
					List<SimpleGrantedAuthority> authorities = List.of(
							new SimpleGrantedAuthority("ROLE_" + rol.name())
					);

					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							email,
							null,
							authorities
					);
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		} catch (Exception ignored) {
			// Si el token es inválido o expiró, no seteamos autenticación y el flujo continúa
		}

		filterChain.doFilter(request, response);
	}
}
