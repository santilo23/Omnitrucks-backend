package um.edu.ar.omnitrucks.config;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.CamionRepository;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;

/**
 * Precarga datos de prueba para desarrollo local (perfil 'dev').
 * Permite tener camiones, choferes y viajes activos listos para usar en la app móvil.
 */
@Component
@Profile("dev")
public class DatosInicialesDev implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DatosInicialesDev.class);

	private final UsuarioRepository usuarios;
	private final CamionRepository camiones;
	private final ViajeRepository viajes;

	public DatosInicialesDev(UsuarioRepository usuarios, CamionRepository camiones, ViajeRepository viajes) {
		this.usuarios = usuarios;
		this.camiones = camiones;
		this.viajes = viajes;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (usuarios.count() > 0 && camiones.count() > 0 && viajes.count() > 0) {
			return;
		}

		log.info("Precargando datos iniciales de desarrollo...");

		Usuario chofer = usuarios.findByEmail("chofer@omnitrucks.com").orElseGet(() -> {
			Usuario u = new Usuario();
			u.setEmail("chofer@omnitrucks.com");
			u.setPasswordHash("dev-hash");
			u.setNombre("Juan");
			u.setApellido("Pérez");
			u.setRol(Rol.CHOFER);
			return usuarios.save(u);
		});

		Usuario admin = usuarios.findByEmail("admin@omnitrucks.com").orElseGet(() -> {
			Usuario u = new Usuario();
			u.setEmail("admin@omnitrucks.com");
			u.setPasswordHash("dev-hash");
			u.setNombre("Martín");
			u.setApellido("González");
			u.setRol(Rol.ADMIN);
			return usuarios.save(u);
		});

		Camion camion1 = camiones.findByPatente("AF123CD").orElseGet(() -> {
			Camion c = new Camion();
			c.setPatente("AF123CD");
			c.setMarca("Scania");
			c.setModelo("R450");
			c.setAnio(2022);
			c.setCapacidadKg(32000);
			return camiones.save(c);
		});

		if (viajes.count() == 0) {
			Viaje viaje = new Viaje();
			viaje.setCamion(camion1);
			viaje.setChofer(chofer);
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

			log.info("Viaje de prueba creado y puesto EN_CURSO (camión {}, chofer {})",
					camion1.getPatente(), chofer.getNombreCompleto());
		}
	}
}
