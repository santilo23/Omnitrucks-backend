package um.edu.ar.omnitrucks.admin.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.admin.application.dto.AsignarViajeAdminRequest;
import um.edu.ar.omnitrucks.admin.application.dto.CrearChoferRequest;
import um.edu.ar.omnitrucks.admin.application.dto.ViajeMonitoreoAdminResponse;
import um.edu.ar.omnitrucks.admin.application.port.in.GestionAdminOperacionesUseCase;
import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.CamionRepository;
import um.edu.ar.omnitrucks.common.error.ConflictoException;
import um.edu.ar.omnitrucks.common.error.RecursoNoEncontradoException;
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.posicion.PosicionRepository;
import um.edu.ar.omnitrucks.posicion.PosicionService;
import um.edu.ar.omnitrucks.posicion.dto.PosicionRequest;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;
import um.edu.ar.omnitrucks.usuario.dto.UsuarioResponse;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;
import um.edu.ar.omnitrucks.viaje.ViajeService;
import um.edu.ar.omnitrucks.viaje.dto.ViajeRequest;
import um.edu.ar.omnitrucks.viaje.dto.ViajeResponse;

@Service
@Transactional
public class GestionAdminOperacionesService implements GestionAdminOperacionesUseCase {

	private static final List<EstadoViaje> EN_RUTA = List.of(EstadoViaje.EN_CURSO, EstadoViaje.PAUSADO);

	private final UsuarioRepository usuarioRepository;
	private final CamionRepository camionRepository;
	private final ViajeRepository viajeRepository;
	private final ViajeService viajeService;
	private final PosicionRepository posicionRepository;
	private final PosicionService posicionService;
	private final PasswordEncoder passwordEncoder;

	public GestionAdminOperacionesService(
			UsuarioRepository usuarioRepository,
			CamionRepository camionRepository,
			ViajeRepository viajeRepository,
			ViajeService viajeService,
			PosicionRepository posicionRepository,
			PosicionService posicionService,
			PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.camionRepository = camionRepository;
		this.viajeRepository = viajeRepository;
		this.viajeService = viajeService;
		this.posicionRepository = posicionRepository;
		this.posicionService = posicionService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UsuarioResponse crearChofer(CrearChoferRequest request) {
		String email = request.email().trim().toLowerCase();
		if (usuarioRepository.existsByEmail(email)) {
			throw new ConflictoException("Ya existe un usuario registrado con el email " + email);
		}

		Usuario chofer = new Usuario();
		chofer.setNombre(request.nombre().trim());
		chofer.setApellido(request.apellido().trim());
		chofer.setEmail(email);
		chofer.setPasswordHash(passwordEncoder.encode(request.password()));
		chofer.setRol(Rol.CHOFER);
		chofer.setActivo(true);

		Usuario guardado = usuarioRepository.save(chofer);
		return UsuarioResponse.de(guardado);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UsuarioResponse> listarChoferes() {
		return usuarioRepository.findAllByRolAndActivoTrue(Rol.CHOFER)
				.stream()
				.map(UsuarioResponse::de)
				.toList();
	}

	@Override
	public ViajeMonitoreoAdminResponse asignarViaje(AsignarViajeAdminRequest request) {
		ViajeRequest pedido = new ViajeRequest(
			request.camionId(),
			request.choferId(),
			request.clienteId(),
			request.origenNombre(),
			request.origenLat(),
			request.origenLon(),
			request.destinoNombre(),
			request.destinoLat(),
			request.destinoLon(),
			request.descripcionCarga(),
			request.codigoSeguimiento(),
			Instant.now()
		);

		ViajeResponse creado = viajeService.crear(pedido);

		if (request.iniciarInmediatamente()) {
			viajeService.iniciar(creado.id());

			// Se registra la primera posición en origen para que el simulador comience de inmediato
			posicionService.registrar(creado.id(), List.of(new PosicionRequest(
				request.origenLat(),
				request.origenLon(),
				0.0,
				0.0,
				5.0,
				Instant.now()
			)));
		}

		Viaje viaje = viajeRepository.findById(creado.id())
				.orElseThrow(() -> new RecursoNoEncontradoException("viaje", creado.id()));

		Posicion ultima = posicionRepository.findFirstByViajeIdOrderByRegistradoEnDescIdDesc(viaje.getId()).orElse(null);
		return ViajeMonitoreoAdminResponse.desde(viaje, ultima);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ViajeMonitoreoAdminResponse> listarViajesActivos() {
		List<Viaje> activos = viajeRepository.findAllByEstadoInOrderByCreatedAtDesc(EN_RUTA);
		return activos.stream().map(v -> {
			Posicion ultima = posicionRepository.findFirstByViajeIdOrderByRegistradoEnDescIdDesc(v.getId()).orElse(null);
			return ViajeMonitoreoAdminResponse.desde(v, ultima);
		}).toList();
	}

	@Override
	public ViajeMonitoreoAdminResponse generarViajeSimuladoDemo() {
		// 1. Obtener o crear un camión disponible
		List<Camion> camiones = camionRepository.findAll();
		Camion camionElegido = camiones.stream()
				.filter(c -> c.isActivo() && !viajeRepository.existsByCamionIdAndEstadoIn(c.getId(), EN_RUTA))
				.findFirst()
				.orElseGet(() -> {
					Camion nuevo = new Camion();
					nuevo.setPatente("DEMO" + (System.currentTimeMillis() % 1000));
					nuevo.setMarca("Mercedes-Benz");
					nuevo.setModelo("Actros 2548");
					nuevo.setAnio(2024);
					nuevo.setCapacidadKg(30000);
					nuevo.setActivo(true);
					return camionRepository.save(nuevo);
				});

		// 2. Obtener o crear un chofer
		List<Usuario> choferes = usuarioRepository.findAllByRolAndActivoTrue(Rol.CHOFER);
		Usuario choferElegido = choferes.stream()
				.filter(ch -> !viajeRepository.findFirstByChoferIdAndEstadoInOrderByCreatedAtDesc(ch.getId(), EN_RUTA).isPresent())
				.findFirst()
				.orElseGet(() -> {
					if (!choferes.isEmpty()) {
						return choferes.get(0);
					}
					Usuario demo = new Usuario();
					demo.setNombre("Conductor");
					demo.setApellido("Demo");
					demo.setEmail("chofer.demo" + System.currentTimeMillis() + "@omnitrucks.com");
					demo.setPasswordHash(passwordEncoder.encode("chofer123"));
					demo.setRol(Rol.CHOFER);
					demo.setActivo(true);
					return usuarioRepository.save(demo);
				});

		// 3. Crear viaje simulado en ruta Buenos Aires -> Rosario o Rosario -> Córdoba
		String origenNombre = "Buenos Aires (Puerto)";
		double origenLat = -34.6037;
		double origenLon = -58.3816;
		String destinoNombre = "Rosario (Centro Logístico)";
		double destinoLat = -32.9468;
		double destinoLon = -60.6393;

		AsignarViajeAdminRequest req = new AsignarViajeAdminRequest(
			camionElegido.getId(),
			choferElegido.getId(),
			null,
			origenNombre,
			origenLat,
			origenLon,
			destinoNombre,
			destinoLat,
			destinoLon,
			"Simulación de Carga Pesada en Ruta 9",
			null,
			true
		);

		return asignarViaje(req);
	}

}
