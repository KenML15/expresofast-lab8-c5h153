package cr.ac.ucr.paraiso.ie.c5h153.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.data.BitacoraEnvioRepository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.exception.ResourceNotFoundException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnvioService {

    private static final Set<String> ESTADOS_FINALES = Set.of("ENTREGADO", "CANCELADO");
    private static final Set<String> ESTADOS_NO_PERMITIDOS_DESDE_FINAL = Set.of("PENDIENTE", "EN_TRANSITO");

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final BitacoraEnvioRepository bitacoraEnvioRepository;
    private final UsuarioRepository usuarioRepository;

    public EnvioService(EnvioRepository envioRepository,
                         VehiculoRepository vehiculoRepository,
                         ConductorRepository conductorRepository,
                         BitacoraEnvioRepository bitacoraEnvioRepository,
                         UsuarioRepository usuarioRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.bitacoraEnvioRepository = bitacoraEnvioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<EnvioResponseDTO> obtenerEnviosOptimizados() {
        return envioRepository.findAllWithDetails().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    public EnvioResponseDTO registrarEnvio(EnvioRequestDTO request) {
        Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("El vehículo especificado no existe."));

        Conductor conductor = conductorRepository.findById(request.getConductorId())
                .orElseThrow(() -> new ResourceNotFoundException("El conductor especificado no existe."));

        if (request.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new IllegalArgumentException("El peso del envío (" + request.getPesoKg()
                    + " kg) supera la capacidad máxima del vehículo (" + vehiculo.getCapacidadKg() + " kg).");
        }

        Envio envio = new Envio();
        envio.setCodigoRastreo(request.getCodigoRastreo());
        envio.setDireccionDestino(request.getDireccionDestino());
        envio.setPesoKg(request.getPesoKg());
        envio.setCosto(request.getCosto());
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);
        envio.setEstadoEnvio("PENDIENTE");

        Envio guardado = envioRepository.save(envio);
        return mapearAResponseDTO(guardado);
    }

    public EnvioResponseDTO actualizarEstadoEnvio(Integer id, CambioEstadoDTO request) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));

        String estadoAnterior = envio.getEstadoEnvio();
        String estadoNuevo = request.getNuevoEstado();

        validarTransicionDeEstado(envio.getCodigoRastreo(), estadoAnterior, estadoNuevo);

        envio.setEstadoEnvio(estadoNuevo);
        Envio actualizado = envioRepository.save(envio);

        registrarBitacora(envio, estadoAnterior, estadoNuevo, request.getObservaciones());

        return mapearAResponseDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public List<BitacoraResponseDTO> obtenerBitacoraDeEnvio(Integer envioId) {
        if (!envioRepository.existsById(envioId)) {
            throw new ResourceNotFoundException("Envío no encontrado con ID: " + envioId);
        }
        return bitacoraEnvioRepository.findByEnvioIdOrderByFechaCambioDesc(envioId).stream()
                .map(b -> new BitacoraResponseDTO(
                        b.getId(),
                        b.getEstadoAnterior(),
                        b.getEstadoNuevo(),
                        b.getFechaCambio(),
                        b.getUsuario().getNombreCompleto(),
                        b.getObservaciones()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnvioResponseDTO obtenerEnvioPorId(Integer id) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));
        
        return mapearAResponseDTO(envio);
    }

    private void validarTransicionDeEstado(String codigoRastreo, String estadoAnterior, String estadoNuevo) {
        if (ESTADOS_FINALES.contains(estadoAnterior) && ESTADOS_NO_PERMITIDOS_DESDE_FINAL.contains(estadoNuevo)) {
            throw new InvalidStateTransitionException(
                    "Transición de estado no permitida para el envío " + codigoRastreo);
        }
    }

    private void registrarBitacora(Envio envio, String estadoAnterior, String estadoNuevo, String observaciones) {
        Usuario usuarioActuante = obtenerUsuarioAutenticado();

        BitacoraEnvio bitacora = new BitacoraEnvio();
        bitacora.setEnvio(envio);
        bitacora.setEstadoAnterior(estadoAnterior);
        bitacora.setEstadoNuevo(estadoNuevo);
        bitacora.setFechaCambio(LocalDateTime.now());
        bitacora.setUsuario(usuarioActuante);
        bitacora.setObservaciones(observaciones);

        bitacoraEnvioRepository.save(bitacora);
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private EnvioResponseDTO mapearAResponseDTO(Envio envio) {
        return new EnvioResponseDTO(
                envio.getId(),
                envio.getCodigoRastreo(),
                envio.getDireccionDestino(),
                envio.getPesoKg(),
                envio.getCosto(),
                envio.getEstadoEnvio(),
                envio.getVehiculo().getPlaca(),
                envio.getConductor().getNombre() + " " + envio.getConductor().getApellidos());
    }

    public EnvioResponseDTO cancelarEnvio(Integer id) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));

        if ("EN_TRANSITO".equals(envio.getEstadoEnvio()) || "ENTREGADO".equals(envio.getEstadoEnvio())) {
            throw new IllegalArgumentException("No se puede cancelar un envío que ya está en ruta o entregado.");
        }

        envio.setEstadoEnvio("CANCELADO");
        Envio actualizado = envioRepository.save(envio);
        

        return mapearAResponseDTO(actualizado);
    }

    public double calcularTarifa(double pesoKg, double distanciaKm) {
     
        if (pesoKg <= 10.0) {
            return 2500.0;
        } else if (pesoKg <= 50.0) {
            return 7500.0;
        } else {
            return 12000.0;
        }
    }
}
