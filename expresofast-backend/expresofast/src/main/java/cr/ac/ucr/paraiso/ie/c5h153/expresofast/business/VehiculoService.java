package cr.ac.ucr.paraiso.ie.c5h153.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.VehiculoRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.VehiculoResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class VehiculoService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("DISPONIBLE", "MANTENIMIENTO", "INACTIVO");

    private final VehiculoRepository vehiculoRepository;
    private final EmpresaLogisticaRepository empresaLogisticaRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                            EmpresaLogisticaRepository empresaLogisticaRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.empresaLogisticaRepository = empresaLogisticaRepository;
    }

    public VehiculoResponseDTO registrarVehiculo(VehiculoRequestDTO request) {
        EmpresaLogistica empresa = empresaLogisticaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("La empresa especificada no existe."));

        if (vehiculoRepository.existsByPlaca(request.getPlaca())) {
            throw new DuplicateResourceException(
                    "Ya existe un vehículo registrado con la placa " + request.getPlaca());
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(request.getPlaca());
        vehiculo.setCapacidadKg(request.getCapacidadKg());
        vehiculo.setEmpresa(empresa);
        vehiculo.setEstado("DISPONIBLE");

        Vehiculo guardado = vehiculoRepository.save(vehiculo);
        return mapearAResponseDTO(guardado);
    }

    public VehiculoResponseDTO cambiarEstadoVehiculo(Integer id, CambioEstadoDTO request) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        String nuevoEstado = request.getNuevoEstado();
        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new IllegalArgumentException(
                    "Estado inválido: " + nuevoEstado + ". Los valores permitidos son " + ESTADOS_VALIDOS);
        }

        vehiculo.setEstado(nuevoEstado);
        Vehiculo actualizado = vehiculoRepository.save(vehiculo);
        return mapearAResponseDTO(actualizado);
    }

    private VehiculoResponseDTO mapearAResponseDTO(Vehiculo vehiculo) {
        return new VehiculoResponseDTO(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getCapacidadKg(),
                vehiculo.getEstado(),
                vehiculo.getEmpresa().getNombre());
    }
}