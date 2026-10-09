package um.edu.ar.omnitrucks.auth.domain.port;

/**
 * Puerto de dominio que desacopla la lógica de negocio del algoritmo
 * criptográfico concreto de hasheo de contraseñas (ej. BCrypt, Argon2).
 */
public interface PasswordEncoderPort {

	String encode(CharSequence rawPassword);

	boolean matches(CharSequence rawPassword, String encodedPassword);

}
