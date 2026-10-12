package um.edu.ar.omnitrucks.viaje.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.common.error.AccesoDenegadoException;
import um.edu.ar.omnitrucks.common.error.CredencialesInvalidasException;
import um.edu.ar.omnitrucks.common.error.CuentaInactivaException;
import um.edu.ar.omnitrucks.common.error.RecursoNoEncontradoException;
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.domain.port.UsuarioRepositoryPort;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.application.dto.EnvioClienteResumenResponse;
import um.edu.ar.omnitrucks.viaje.application.dto.SeguimientoClienteResponse;
import um.edu.ar.omnitrucks.viaje.application.dto.UbicacionSeguimientoResponse;
import um.edu.ar.omnitrucks.viaje.application.port.in.ConsultarSeguimientoClienteUseCase;
import um.edu.ar.omnitrucks.viaje.domain.port.ViajeSeguimientoRepositoryPort;

/**
 * Servicio de aplicación que implementa el caso de uso de seguimiento para clientes.
 * Aplica estrictamente las políticas de autorización RBAC y propiedad del envío.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarSeguimientoClienteService implements ConsultarSeguimientoClienteUseCase {

	private final ViajeSeguimientoRepositoryPort viajeSeguimientoRepository;
	private final UsuarioRepositoryPort usuarioRepository;

	public ConsultarSeguimientoClienteService(
			ViajeSeguimientoRepositoryPort viajeSeguimientoRepository,
			UsuarioRepositoryPort usuarioRepository) {
		this.viajeSeguimientoRepository = viajeSeguimientoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public SeguimientoClienteResponse consultarPorCodigo(String codigoSeguimiento, String emailUsuarioAutenticado) {
		Usuario usuario = validarUsuario(emailUsuarioAutenticado);
		Viaje viaje = buscarYValidarPropiedadViaje(codigoSeguimiento, usuario);

		Posicion ultimaPosicion = viajeSeguimientoRepository.buscarUltimaPosicion(viaje.getId()).orElse(null);
		return SeguimientoClienteResponse.desde(viaje, ultimaPosicion);
	}

	@Override
	public List<EnvioClienteResumenResponse> listarEnviosCliente(String emailUsuarioAutenticado) {
		Usuario usuario = validarUsuario(emailUsuarioAutenticado);

		if (usuario.getRol() != Rol.CLIENTE && usuario.getRol() != Rol.ADMIN) {
			throw new AccesoDenegadoException("Rol no autorizado para acceder al panel de clientes.");
		}

		List<Viaje> envios = viajeSeguimientoRepository.listarPorClienteId(usuario.getId());
		return envios.stream()
				.map(EnvioClienteResumenResponse::desde)
				.toList();
	}

	@Override
	public UbicacionSeguimientoResponse consultarUbicacionEnVivo(String codigoSeguimiento, String emailUsuarioAutenticado) {
		Usuario usuario = validarUsuario(emailUsuarioAutenticado);
		Viaje viaje = buscarYValidarPropiedadViaje(codigoSeguimiento, usuario);

		Posicion ultimaPosicion = viajeSeguimientoRepository.buscarUltimaPosicion(viaje.getId()).orElse(null);
		return UbicacionSeguimientoResponse.desde(viaje, ultimaPosicion);
	}

	private Usuario validarUsuario(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new CredencialesInvalidasException("Usuario no autenticado o no encontrado."));

		if (!usuario.isActivo()) {
			throw new CuentaInactivaException("La cuenta del usuario se encuentra inactiva.");
		}

		return usuario;
	}

	private Viaje buscarYValidarPropiedadViaje(String codigoSeguimiento, Usuario usuario) {
		Viaje viaje = viajeSeguimientoRepository.buscarPorCodigoSeguimiento(codigoSeguimiento)
				.orElseThrow(() -> new RecursoNoEncontradoException("envío con código", codigoSeguimiento));

		// Administrador tiene acceso irrestricto de supervisión
		if (usuario.getRol() == Rol.ADMIN) {
			return viaje;
		}

		// Rol Chofer no tiene acceso a la vista de clientes
		if (usuario.getRol() == Rol.CHOFER) {
			throw new AccesoDenegadoException("Los choferes no tienen acceso a la vista de cliente.");
		}

		// Rol Cliente: REGLA DE NEGOCIO ESTRICTA -> Solo puede acceder a viajes asignados a su cuenta
		if (usuario.getRol() == Rol.CLIENTE) {
			if (viaje.getCliente() == null || !viaje.getCliente().getId().equals(usuario.getId())) {
				throw new AccesoDenegadoException("No tiene permisos para consultar este envío.");
			}
		}

		return viaje;
	}

}
