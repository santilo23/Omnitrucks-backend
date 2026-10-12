package um.edu.ar.omnitrucks.chofer.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.chofer.application.dto.RutaChoferResponse;
import um.edu.ar.omnitrucks.chofer.application.dto.ViajeChoferResumenResponse;
import um.edu.ar.omnitrucks.chofer.application.port.in.ConsultarRutaChoferUseCase;
import um.edu.ar.omnitrucks.chofer.domain.port.ChoferRutaRepositoryPort;
import um.edu.ar.omnitrucks.common.error.AccesoDenegadoException;
import um.edu.ar.omnitrucks.common.error.CredencialesInvalidasException;
import um.edu.ar.omnitrucks.common.error.CuentaInactivaException;
import um.edu.ar.omnitrucks.common.error.RecursoNoEncontradoException;
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.domain.port.UsuarioRepositoryPort;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * Servicio de aplicación que implementa el caso de uso de consulta de rutas para choferes.
 * Aplica aislamiento estricto impidiendo el acceso a camiones o rutas de otros choferes.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarRutaChoferService implements ConsultarRutaChoferUseCase {

	private final ChoferRutaRepositoryPort choferRutaRepository;
	private final UsuarioRepositoryPort usuarioRepository;

	public ConsultarRutaChoferService(
			ChoferRutaRepositoryPort choferRutaRepository,
			UsuarioRepositoryPort usuarioRepository) {
		this.choferRutaRepository = choferRutaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public Optional<RutaChoferResponse> consultarRutaActiva(String emailChofer) {
		Usuario chofer = validarChofer(emailChofer);

		return choferRutaRepository.buscarViajeActivoPorChoferId(chofer.getId())
				.map(this::construirRutaResponse);
	}

	@Override
	public RutaChoferResponse consultarRutaPorViajeId(Long viajeId, String emailChofer) {
		Usuario usuario = validarChofer(emailChofer);
		Viaje viaje = choferRutaRepository.buscarPorId(viajeId)
				.orElseThrow(() -> new RecursoNoEncontradoException("viaje", viajeId));

		// REGLA DE NEGOCIO ESTRICTA: El chofer solo puede acceder a su vehículo y viaje asignado
		if (usuario.getRol() == Rol.CHOFER) {
			if (!viaje.getChofer().getId().equals(usuario.getId())) {
				throw new AccesoDenegadoException("No tiene permisos para acceder a la ruta de otro chofer.");
			}
		}

		return construirRutaResponse(viaje);
	}

	@Override
	public List<ViajeChoferResumenResponse> listarMisViajes(String emailChofer) {
		Usuario chofer = validarChofer(emailChofer);

		return choferRutaRepository.listarViajesPorChoferId(chofer.getId())
				.stream()
				.map(ViajeChoferResumenResponse::desde)
				.toList();
	}

	private Usuario validarChofer(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new CredencialesInvalidasException("Usuario no autenticado o no encontrado."));

		if (!usuario.isActivo()) {
			throw new CuentaInactivaException("La cuenta del usuario se encuentra inactiva.");
		}

		if (usuario.getRol() != Rol.CHOFER && usuario.getRol() != Rol.ADMIN) {
			throw new AccesoDenegadoException("Rol no autorizado para acceder al portal de choferes.");
		}

		return usuario;
	}

	private RutaChoferResponse construirRutaResponse(Viaje viaje) {
		Posicion ultimaPosicion = choferRutaRepository.buscarUltimaPosicion(viaje.getId()).orElse(null);
		List<Posicion> recorrido = choferRutaRepository.obtenerRecorrido(viaje.getId());
		return RutaChoferResponse.desde(viaje, ultimaPosicion, recorrido);
	}

}
