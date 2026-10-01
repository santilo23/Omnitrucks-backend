package um.edu.ar.omnitrucks.simulacion;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import um.edu.ar.omnitrucks.common.geo.Coordenada;
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.posicion.PosicionRepository;
import um.edu.ar.omnitrucks.posicion.PosicionService;
import um.edu.ar.omnitrucks.posicion.dto.PosicionRequest;
import um.edu.ar.omnitrucks.simulacion.estrategia.EstrategiaMovimiento;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;
import um.edu.ar.omnitrucks.viaje.ViajeService;

/**
 * Servicio del simulador de recorridos para desarrollo (solo perfil 'sim').
 *
 * Mueve automáticamente los camiones que tengan viajes en estado EN_CURSO,
 * generando periódicamente nuevas coordenadas GPS hacia su destino final.
 */
@Service
@Profile("sim")
public class SimuladorRecorridosService {

	private static final Logger log = LoggerFactory.getLogger(SimuladorRecorridosService.class);

	private final ViajeRepository viajeRepository;
	private final ViajeService viajeService;
	private final PosicionRepository posicionRepository;
	private final PosicionService posicionService;
	private final EstrategiaMovimiento estrategiaMovimiento;

	private final double velocidadKmh;
	private final int intervaloSegundos;
	private final double umbralLlegadaMetros;

	public SimuladorRecorridosService(
			ViajeRepository viajeRepository,
			ViajeService viajeService,
			PosicionRepository posicionRepository,
			PosicionService posicionService,
			EstrategiaMovimiento estrategiaMovimiento,
			@Value("${omnitrucks.simulacion.velocidad-kmh:80.0}") double velocidadKmh,
			@Value("${omnitrucks.simulacion.intervalo-segundos:5}") int intervaloSegundos,
			@Value("${omnitrucks.simulacion.umbral-llegada-metros:100.0}") double umbralLlegadaMetros) {
		this.viajeRepository = viajeRepository;
		this.viajeService = viajeService;
		this.posicionRepository = posicionRepository;
		this.posicionService = posicionService;
		this.estrategiaMovimiento = estrategiaMovimiento;
		this.velocidadKmh = velocidadKmh;
		this.intervaloSegundos = intervaloSegundos;
		this.umbralLlegadaMetros = umbralLlegadaMetros;
	}

	@Scheduled(fixedDelayString = "${omnitrucks.simulacion.intervalo-ms:5000}")
	public void simularPaso() {
		List<Viaje> viajesEnCurso = viajeRepository.findAllByEstado(EstadoViaje.EN_CURSO);

		if (viajesEnCurso.isEmpty()) {
			return;
		}

		log.debug("Simulando movimiento para {} viaje(s) en curso.", viajesEnCurso.size());

		for (Viaje viaje : viajesEnCurso) {
			try {
				procesarViaje(viaje);
			} catch (Exception e) {
				log.error("Error al simular movimiento para el viaje {}: {}", viaje.getId(), e.getMessage(), e);
			}
		}
	}

	private void procesarViaje(Viaje viaje) {
		Optional<Posicion> ultimaPos = posicionRepository.findFirstByViajeIdOrderByRegistradoEnDescIdDesc(viaje.getId());

		Coordenada actual = ultimaPos
				.map(p -> new Coordenada(p.getLatitud(), p.getLongitud()))
				.orElseGet(() -> new Coordenada(viaje.getOrigenLat(), viaje.getOrigenLon()));

		Coordenada destino = new Coordenada(viaje.getDestinoLat(), viaje.getDestinoLon());
		double distanciaRestante = actual.distanciaMetros(destino);

		if (distanciaRestante <= umbralLlegadaMetros) {
			log.info("Viaje {} ha llegado a su destino en {}. Finalizando viaje.", viaje.getId(), viaje.getDestinoNombre());
			posicionService.registrar(viaje.getId(), List.of(new PosicionRequest(
					destino.latitud(),
					destino.longitud(),
					0.0,
					0.0,
					5.0,
					Instant.now()
			)));
			viajeService.finalizar(viaje.getId());
			return;
		}

		double velocidadMs = (velocidadKmh * 1000.0) / 3600.0;
		double pasoMetros = velocidadMs * intervaloSegundos;

		Coordenada siguiente = estrategiaMovimiento.siguientePosicion(actual, destino, pasoMetros);
		double rumbo = actual.rumboHacia(siguiente.equals(actual) ? destino : siguiente);

		posicionService.registrar(viaje.getId(), List.of(new PosicionRequest(
				siguiente.latitud(),
				siguiente.longitud(),
				velocidadKmh,
				rumbo,
				5.0,
				Instant.now()
		)));

		if (siguiente.equals(destino)) {
			log.info("Viaje {} completó su recorrido en este paso. Finalizando.", viaje.getId());
			viajeService.finalizar(viaje.getId());
		}
	}
}
