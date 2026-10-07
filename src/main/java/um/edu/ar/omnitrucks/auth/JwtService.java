package um.edu.ar.omnitrucks.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;

/**
 * Servicio encargado de la emisión, lectura y validación de tokens JWT (JSON Web Tokens).
 */
@Service
public class JwtService {

	private final SecretKey signingKey;
	private final long expiracionMs;

	public JwtService(
			@Value("${omnitrucks.jwt.secret:omnitrucks_secret_key_default_desarrollo_local_2026_super_segura}") String secret,
			@Value("${omnitrucks.jwt.expiration-ms:86400000}") long expiracionMs) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expiracionMs = expiracionMs;
	}

	/**
	 * Emite un nuevo token JWT para el usuario especificado.
	 */
	public String generarToken(Usuario usuario) {
		long ahora = System.currentTimeMillis();

		return Jwts.builder()
				.subject(usuario.getEmail())
				.claim("id", usuario.getId())
				.claim("rol", usuario.getRol().name())
				.claim("nombreCompleto", usuario.getNombreCompleto())
				.issuedAt(new Date(ahora))
				.expiration(new Date(ahora + expiracionMs))
				.signWith(signingKey)
				.compact();
	}

	/**
	 * Extrae el correo electrónico (subject) del token.
	 */
	public String extraerEmail(String token) {
		return extraerClaims(token).getSubject();
	}

	/**
	 * Extrae el rol asignado al usuario contenido en el token.
	 */
	public Rol extraerRol(String token) {
		String nombreRol = extraerClaims(token).get("rol", String.class);
		return Rol.valueOf(nombreRol);
	}

	/**
	 * Extrae el ID del usuario del token.
	 */
	public Long extraerUsuarioId(String token) {
		Number id = extraerClaims(token).get("id", Number.class);
		return id != null ? id.longValue() : null;
	}

	/**
	 * Valida si el token corresponde al usuario y no ha expirado.
	 */
	public boolean esTokenValido(String token, String emailEsperado) {
		try {
			Claims claims = extraerClaims(token);
			String email = claims.getSubject();
			boolean noVencido = claims.getExpiration().after(new Date());
			return email.equalsIgnoreCase(emailEsperado) && noVencido;
		} catch (Exception e) {
			return false;
		}
	}

	private Claims extraerClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
