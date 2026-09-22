package um.edu.ar.omnitrucks.usuario;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una persona del sistema. El rol determina qué puede hacer.
 *
 * El chofer no es una tabla aparte: es un usuario con rol {@link Rol#CHOFER}.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 120)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Column(nullable = false, length = 60)
	private String nombre;

	@Column(nullable = false, length = 60)
	private String apellido;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Rol rol;

	@Column(nullable = false)
	private boolean activo = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public String getNombreCompleto() {
		return nombre + " " + apellido;
	}

	@PrePersist
	void alCrear() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

}
