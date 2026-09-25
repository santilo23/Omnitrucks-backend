package um.edu.ar.omnitrucks.posicion;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.common.error.ConflictoException;
import um.edu.ar.omnitrucks.common.error.RecursoNoEncontradoException;
import um.edu.ar.omnitrucks.posicion.dto.PosicionRequest;
import um.edu.ar.omnitrucks.posicion.dto.PosicionResponse;
import um.edu.ar.omnitrucks.posicion.dto.UbicacionActivaResponse;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;

@Service
@Transactional(readOnly = true)
public class PosicionService {

	private static final Set<EstadoViaje> EN_RUTA = Set.of(EstadoViaje.EN_CURSO, EstadoViaje.PAUSADO);

	/** Tope por defecto del recorrido, para no devolver un viaje entero de golpe. */
	private static final int LIMITE_RECORRIDO = 1000;

	/** Bordes abiertos del rango, para cuando la consulta no acota por fecha. */
	private static final Instant INICIO_DE_LOS_TIEMPOS = Instant.EPOCH;
	private static final Instant FIN_DE_LOS_TIEMPOS = Instant.parse("9999-12-31T23:59:59Z");

	private final PosicionRepository posiciones;
	private final ViajeRepository viajes;

	public PosicionService(PosicionRepository posiciones, ViajeRepository viajes) {
		this.posiciones = posiciones;
		this.viajes = viajes;
	}

	/**
	 * Registra una o varias posiciones. Acepta un lote porque el chofer puede
	 * quedarse sin señal en la ruta y descargar después lo que acumuló.
	 */
	@Transactional
	public List<PosicionResponse> registrar(Long viajeId, List<PosicionRequest> pedidos) {
		Viaje viaje = viajes.findById(viajeId)
			.orElseThrow(() -> new RecursoNoEncontradoException("viaje", viajeId));

		if (!viaje.getEstado().enRuta()) {
			throw new ConflictoException(
				"No se pueden registrar posiciones en un viaje %s: tiene que estar en ruta."
					.formatted(viaje.getEstado()));
		}

		Instant ahora = Instant.now();

		List<Posicion> nuevas = pedidos.stream().map(pedido -> {
			Posicion posicion = new Posicion();
			posicion.setViaje(viaje);
			posicion.setLatitud(pedido.latitud());
			posicion.setLongitud(pedido.longitud());
			posicion.setVelocidadKmh(pedido.velocidadKmh());
			posicion.setRumboGrados(pedido.rumboGrados());
			posicion.setPrecisionM(pedido.precisionM());
			// Si el dispositivo no informa cuándo la tomó, se asume que es de ahora.
			posicion.setRegistradoEn(pedido.registradoEn() != null ? pedido.registradoEn() : ahora);
			return posicion;
		}).toList();

		return posiciones.saveAll(nuevas).stream().map(PosicionResponse::desde).toList();
	}

	public PosicionResponse ultima(Long viajeId) {
		exigirQueExista(viajeId);

		return posiciones.findFirstByViajeIdOrderByRegistradoEnDescIdDesc(viajeId)
			.map(PosicionResponse::desde)
			.orElseThrow(() -> new RecursoNoEncontradoException(
				"El viaje %d todavía no tiene posiciones registradas.".formatted(viajeId)));
	}

	public List<PosicionResponse> recorrido(Long viajeId, Instant desde, Instant hasta, Integer limite) {
		exigirQueExista(viajeId);

		int tope = (limite != null && limite > 0) ? limite : LIMITE_RECORRIDO;
		Instant inicio = (desde != null) ? desde : INICIO_DE_LOS_TIEMPOS;
		Instant fin = (hasta != null) ? hasta : FIN_DE_LOS_TIEMPOS;

		return posiciones
			.findByViajeIdAndRegistradoEnBetweenOrderByRegistradoEnAscIdAsc(
				viajeId, inicio, fin, Limit.of(tope))
			.stream().map(PosicionResponse::desde).toList();
	}

	/** Última posición de cada camión en ruta. Es lo que consume el mapa. */
	public List<UbicacionActivaResponse> ubicacionesActivas() {
		return posiciones.ultimasDeViajesEnEstados(EN_RUTA)
			.stream().map(UbicacionActivaResponse::desde).toList();
	}

	private void exigirQueExista(Long viajeId) {
		if (!viajes.existsById(viajeId)) {
			throw new RecursoNoEncontradoException("viaje", viajeId);
		}
	}

}
