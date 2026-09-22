package um.edu.ar.omnitrucks.viaje;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.CamionRepository;
import um.edu.ar.omnitrucks.common.error.ConflictoException;
import um.edu.ar.omnitrucks.common.error.RecursoNoEncontradoException;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;
import um.edu.ar.omnitrucks.viaje.dto.ViajeRequest;
import um.edu.ar.omnitrucks.viaje.dto.ViajeResponse;

/**
 * Máquina de estados de los viajes.
 *
 * Todas las transiciones válidas se deciden acá; el controlador solo traduce
 * HTTP. Cada operación valida el estado actual antes de cambiarlo.
 */
@Service
@Transactional(readOnly = true)
public class ViajeService {

	private static final Set<EstadoViaje> EN_RUTA = Set.of(EstadoViaje.EN_CURSO, EstadoViaje.PAUSADO);

	private final ViajeRepository viajes;
	private final CamionRepository camiones;
	private final UsuarioRepository usuarios;

	public ViajeService(ViajeRepository viajes, CamionRepository camiones, UsuarioRepository usuarios) {
		this.viajes = viajes;
		this.camiones = camiones;
		this.usuarios = usuarios;
	}

	public List<ViajeResponse> listar(EstadoViaje estado) {
		List<Viaje> encontrados = (estado == null)
			? viajes.findAllByOrderByCreatedAtDesc()
			: viajes.findAllByEstadoOrderByCreatedAtDesc(estado);

		return encontrados.stream().map(ViajeResponse::desde).toList();
	}

	/** Viajes que están en la ruta ahora mismo: es lo que alimenta el mapa. */
	public List<ViajeResponse> activos() {
		return viajes.findAllByEstadoInOrderByCreatedAtDesc(EN_RUTA)
			.stream().map(ViajeResponse::desde).toList();
	}

	public ViajeResponse obtener(Long id) {
		return ViajeResponse.desde(buscar(id));
	}

	@Transactional
	public ViajeResponse crear(ViajeRequest pedido) {
		Camion camion = camiones.findById(pedido.camionId())
			.orElseThrow(() -> new RecursoNoEncontradoException("camión", pedido.camionId()));

		Usuario chofer = usuarios.findById(pedido.choferId())
			.orElseThrow(() -> new RecursoNoEncontradoException("chofer", pedido.choferId()));

		Viaje viaje = new Viaje();
		viaje.setCamion(camion);
		viaje.setChofer(chofer);

		if (pedido.clienteId() != null) {
			viaje.setCliente(usuarios.findById(pedido.clienteId())
				.orElseThrow(() -> new RecursoNoEncontradoException("cliente", pedido.clienteId())));
		}

		viaje.setOrigenNombre(pedido.origenNombre().trim());
		viaje.setOrigenLat(pedido.origenLat());
		viaje.setOrigenLon(pedido.origenLon());
		viaje.setDestinoNombre(pedido.destinoNombre().trim());
		viaje.setDestinoLat(pedido.destinoLat());
		viaje.setDestinoLon(pedido.destinoLon());
		viaje.setDescripcionCarga(pedido.descripcionCarga());
		viaje.setSalidaProgramada(pedido.salidaProgramada());
		viaje.setEstado(EstadoViaje.PROGRAMADO);

		return ViajeResponse.desde(viajes.save(viaje));
	}

	@Transactional
	public ViajeResponse iniciar(Long id) {
		Viaje viaje = buscar(id);
		exigirEstado(viaje, EstadoViaje.PROGRAMADO, "iniciar");

		Camion camion = viaje.getCamion();
		if (viajes.existsByCamionIdAndEstadoIn(camion.getId(), EN_RUTA)) {
			throw new ConflictoException(
				"El camión %s ya tiene un viaje en curso.".formatted(camion.getPatente()));
		}

		viaje.setEstado(EstadoViaje.EN_CURSO);
		viaje.setSalidaReal(Instant.now());
		return ViajeResponse.desde(viaje);
	}

	@Transactional
	public ViajeResponse pausar(Long id) {
		Viaje viaje = buscar(id);
		exigirEstado(viaje, EstadoViaje.EN_CURSO, "pausar");

		viaje.setEstado(EstadoViaje.PAUSADO);
		return ViajeResponse.desde(viaje);
	}

	@Transactional
	public ViajeResponse reanudar(Long id) {
		Viaje viaje = buscar(id);
		exigirEstado(viaje, EstadoViaje.PAUSADO, "reanudar");

		viaje.setEstado(EstadoViaje.EN_CURSO);
		return ViajeResponse.desde(viaje);
	}

	@Transactional
	public ViajeResponse finalizar(Long id) {
		Viaje viaje = buscar(id);

		if (!viaje.getEstado().enRuta()) {
			throw new ConflictoException(
				"No se puede finalizar un viaje %s: primero tiene que estar en curso."
					.formatted(viaje.getEstado()));
		}

		viaje.setEstado(EstadoViaje.FINALIZADO);
		viaje.setLlegadaReal(Instant.now());
		return ViajeResponse.desde(viaje);
	}

	@Transactional
	public ViajeResponse cancelar(Long id) {
		Viaje viaje = buscar(id);

		if (viaje.getEstado().esFinal()) {
			throw new ConflictoException(
				"El viaje ya está %s y no se puede cancelar.".formatted(viaje.getEstado()));
		}

		viaje.setEstado(EstadoViaje.CANCELADO);
		return ViajeResponse.desde(viaje);
	}

	private Viaje buscar(Long id) {
		return viajes.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("viaje", id));
	}

	private void exigirEstado(Viaje viaje, EstadoViaje esperado, String accion) {
		if (viaje.getEstado() != esperado) {
			throw new ConflictoException(
				"No se puede %s un viaje %s: tiene que estar %s."
					.formatted(accion, viaje.getEstado(), esperado));
		}
	}

}
