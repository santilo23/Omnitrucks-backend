package um.edu.ar.omnitrucks.simulacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.CamionRepository;
import um.edu.ar.omnitrucks.common.geo.Coordenada;
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.posicion.PosicionRepository;
import um.edu.ar.omnitrucks.posicion.PosicionService;
import um.edu.ar.omnitrucks.posicion.dto.PosicionResponse;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;
import um.edu.ar.omnitrucks.viaje.ViajeService;
import um.edu.ar.omnitrucks.viaje.dto.ViajeResponse;

@SpringBootTest
@ActiveProfiles({"dev", "sim"})
@Transactional
class SimuladorRecorridosIntegrationTest {

	@Autowired
	private CamionRepository camiones;

	@Autowired
	private UsuarioRepository usuarios;

	@Autowired
	private ViajeRepository viajes;

	@Autowired
	private PosicionRepository posiciones;

	@Autowired
	private PosicionService posicionService;

	@Autowired
	private ViajeService viajeService;

	@Autowired
	private SimuladorRecorridosService simuladorService;

	private Long camionId;
	private Long choferId;

	// Coordenadas
	// CABA: -34.6037, -58.3816
	// Rosario: -32.9468, -60.6393
	private final double cabaLat = -34.6037;
	private final double cabaLon = -58.3816;
	private final double rosarioLat = -32.9468;
	private final double rosarioLon = -60.6393;

	@BeforeEach
	void prepararDatos() {
		posiciones.deleteAllInBatch();
		viajes.deleteAllInBatch();
		camiones.deleteAllInBatch();
		usuarios.deleteAllInBatch();

		Camion camion = new Camion();
		camion.setPatente("AA111BB");
		camion.setMarca("Mercedes-Benz");
		camion.setModelo("Actros");
		camionId = camiones.save(camion).getId();

		Usuario chofer = new Usuario();
		chofer.setEmail("chofer.sim@omnitrucks.com");
		chofer.setPasswordHash("hash123");
		chofer.setNombre("Carlos");
		chofer.setApellido("Camionero");
		chofer.setRol(Rol.CHOFER);
		choferId = usuarios.save(chofer).getId();
	}

	@Test
	@DisplayName("Un viaje EN_CURSO genera posiciones periódicas hacia su destino mediante el simulador")
	void simulaPasoDeViajeEnCurso() {
		Viaje viaje = new Viaje();
		viaje.setCamion(camiones.getReferenceById(camionId));
		viaje.setChofer(usuarios.getReferenceById(choferId));
		viaje.setOrigenNombre("Buenos Aires");
		viaje.setOrigenLat(cabaLat);
		viaje.setOrigenLon(cabaLon);
		viaje.setDestinoNombre("Rosario");
		viaje.setDestinoLat(rosarioLat);
		viaje.setDestinoLon(rosarioLon);
		viaje.setEstado(EstadoViaje.PROGRAMADO);
		viaje = viajes.save(viaje);

		// Iniciamos el viaje
		viajeService.iniciar(viaje.getId());

		// Ejecutamos el primer paso de simulación
		simuladorService.simularPaso();

		// Verificamos que se haya registrado la primera posición
		PosicionResponse primeraPosicion = posicionService.ultima(viaje.getId());
		assertThat(primeraPosicion).isNotNull();
		assertThat(primeraPosicion.velocidadKmh()).isEqualTo(80.0);

		Coordenada coordOrigen = new Coordenada(cabaLat, cabaLon);
		Coordenada coordPaso1 = new Coordenada(primeraPosicion.latitud(), primeraPosicion.longitud());

		// Avanzó unos ~111 metros hacia Rosario
		assertThat(coordOrigen.distanciaMetros(coordPaso1)).isCloseTo(111.11, within(10.0));

		// Ejecutamos un segundo paso de simulación
		simuladorService.simularPaso();

		PosicionResponse segundaPosicion = posicionService.ultima(viaje.getId());
		Coordenada coordPaso2 = new Coordenada(segundaPosicion.latitud(), segundaPosicion.longitud());

		// La segunda posición avanzó otros ~111 metros
		assertThat(coordPaso1.distanciaMetros(coordPaso2)).isCloseTo(111.11, within(10.0));
		assertThat(posiciones.findAll()).hasSize(2);
	}
}
