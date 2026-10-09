package um.edu.ar.omnitrucks.config;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import um.edu.ar.omnitrucks.auth.domain.port.PasswordEncoderPort;
import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.CamionRepository;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.domain.port.UsuarioRepositoryPort;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;

/**
 * Precarga datos de prueba para desarrollo local (perfil 'dev').
 *
 * REGLA ESTRICTA DE SEGURIDAD:
 * Ninguna contraseña en texto plano se almacena en el código fuente.
 * Las credenciales se inyectan mediante variables de entorno (SEED_*_PASSWORD)
 * o propiedades de configuración (omnitrucks.seed.*).
 * Si no se configuran, se generan contraseñas aleatorias seguras temporales.
 */
@Component
@Profile("dev")
public class DatosInicialesDev implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DatosInicialesDev.class);

	private final UsuarioRepositoryPort usuarios;
	private final CamionRepository camiones;
	private final ViajeRepository viajes;
	private final PasswordEncoderPort passwordEncoder;

	@Value("${omnitrucks.seed.admin-password:${SEED_ADMIN_PASSWORD:}}")
	private String adminPasswordConfig;

	@Value("${omnitrucks.seed.chofer-password:${SEED_CHOFER_PASSWORD:}}")
	private String choferPasswordConfig;

	@Value("${omnitrucks.seed.cliente-password:${SEED_CLIENTE_PASSWORD:}}")
	private String clientePasswordConfig;

	public DatosInicialesDev(
			UsuarioRepositoryPort usuarios,
			CamionRepository camiones,
			ViajeRepository viajes,
			PasswordEncoderPort passwordEncoder) {
		this.usuarios = usuarios;
		this.camiones = camiones;
		this.viajes = viajes;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) {
		log.info("Precargando/verificando datos iniciales de desarrollo con Clean Architecture...");

		// 1. Obtener contraseñas desde el entorno o generar tokens seguros aleatorios
		String adminPass = resolverContrasena(adminPasswordConfig, "admin@omnitrucks.com");
		String choferPass = resolverContrasena(choferPasswordConfig, "chofer@omnitrucks.com");
		String clientePass = resolverContrasena(clientePasswordConfig, "cliente@omnitrucks.com");

		// 2. Crear o actualizar Administrador (Rol: ADMIN)
		Usuario admin = usuarios.findByEmail("admin@omnitrucks.com").orElseGet(() -> {
			Usuario u = new Usuario();
			u.setEmail("admin@omnitrucks.com");
			u.setNombre("Martín");
			u.setApellido("González");
			u.setRol(Rol.ADMIN);
			u.setActivo(true);
			return u;
		});
		admin.setPasswordHash(passwordEncoder.encode(adminPass));
		admin.setActivo(true);
		usuarios.save(admin);

		// 3. Crear o actualizar Chofer (Rol: CHOFER)
		Usuario chofer = usuarios.findByEmail("chofer@omnitrucks.com").orElseGet(() -> {
			Usuario u = new Usuario();
			u.setEmail("chofer@omnitrucks.com");
			u.setNombre("Juan");
			u.setApellido("Pérez");
			u.setRol(Rol.CHOFER);
			u.setActivo(true);
			return u;
		});
		chofer.setPasswordHash(passwordEncoder.encode(choferPass));
		chofer.setActivo(true);
		usuarios.save(chofer);

		// 4. Crear o actualizar Cliente (Rol: CLIENTE)
		Usuario cliente = usuarios.findByEmail("cliente@omnitrucks.com").orElseGet(() -> {
			Usuario u = new Usuario();
			u.setEmail("cliente@omnitrucks.com");
			u.setNombre("Acme");
			u.setApellido("Logística S.A.");
			u.setRol(Rol.CLIENTE);
			u.setActivo(true);
			return u;
		});
		cliente.setPasswordHash(passwordEncoder.encode(clientePass));
		cliente.setActivo(true);
		usuarios.save(cliente);

		// 5. Camión de prueba
		Camion camion1 = camiones.findByPatente("AF123CD").orElseGet(() -> {
			Camion c = new Camion();
			c.setPatente("AF123CD");
			c.setMarca("Scania");
			c.setModelo("R450");
			c.setAnio(2022);
			c.setCapacidadKg(32000);
			return camiones.save(c);
		});

		// 6. Viaje en curso asignado
		if (viajes.count() == 0) {
			Viaje viaje = new Viaje();
			viaje.setCamion(camion1);
			viaje.setChofer(chofer);
			viaje.setCliente(cliente);
			viaje.setOrigenNombre("Buenos Aires (Puerto)");
			viaje.setOrigenLat(-34.6037);
			viaje.setOrigenLon(-58.3816);
			viaje.setDestinoNombre("Rosario (Parque Industrial)");
			viaje.setDestinoLat(-32.9468);
			viaje.setDestinoLon(-60.6393);
			viaje.setDescripcionCarga("Pallets de mercadería en seco");
			viaje.setEstado(EstadoViaje.EN_CURSO);
			viaje.setSalidaProgramada(Instant.now().minusSeconds(1800));
			viaje.setSalidaReal(Instant.now().minusSeconds(1800));
			viaje.setLlegadaEstimada(Instant.now().plusSeconds(14400));
			viajes.save(viaje);

			log.info("Viaje inicial configurado (Camión: {}, Chofer: {}, Cliente: {})",
					camion1.getPatente(), chofer.getNombreCompleto(), cliente.getNombreCompleto());
		}
	}

	private String resolverContrasena(String configurada, String usuarioIdentificador) {
		if (StringUtils.hasText(configurada)) {
			return configurada;
		}
		// Si no se configuró en el entorno, genera una contraseña temporal segura
		String temporal = UUID.randomUUID().toString().substring(0, 12);
		log.warn("⚠️ [SEGURIDAD] No se proveyó contraseña de entorno para {}. Se generó clave temporal: {}",
				usuarioIdentificador, temporal);
		return temporal;
	}

}
