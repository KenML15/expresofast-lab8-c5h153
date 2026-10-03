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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private EmpresaLogisticaRepository empresaLogisticaRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    private EmpresaLogistica empresa;
    private VehiculoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        empresa = new EmpresaLogistica();
        empresa.setId(1);
        empresa.setNombre("Transportes Paraíso S.A.");

        requestDTO = new VehiculoRequestDTO();
        requestDTO.setPlaca("SJO-123");
        requestDTO.setCapacidadKg(new BigDecimal("500.00"));
        requestDTO.setEmpresaId(1);
    }

    @Test
    @DisplayName("Debe registrar un vehículo válido en estado DISPONIBLE")
    void registrarVehiculo_DatosValidos_RetornaVehiculoResponseDTO() {
        when(empresaLogisticaRepository.findById(1)).thenReturn(Optional.of(empresa));
        when(vehiculoRepository.existsByPlaca("SJO-123")).thenReturn(false);
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(invocation -> {
            Vehiculo guardado = invocation.getArgument(0);
            guardado.setId(1);
            return guardado;
        });

        VehiculoResponseDTO resultado = vehiculoService.registrarVehiculo(requestDTO);

        assertNotNull(resultado);
        assertEquals("DISPONIBLE", resultado.getEstado());
        assertEquals("SJO-123", resultado.getPlaca());
        assertEquals("Transportes Paraíso S.A.", resultado.getNombreEmpresa());
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException si la placa ya existe")
    void registrarVehiculo_PlacaDuplicada_LanzaDuplicateResourceException() {
        when(empresaLogisticaRepository.findById(1)).thenReturn(Optional.of(empresa));
        when(vehiculoRepository.existsByPlaca("SJO-123")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehiculoService.registrarVehiculo(requestDTO));
        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la empresa no existe")
    void registrarVehiculo_EmpresaInexistente_LanzaResourceNotFoundException() {
        when(empresaLogisticaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.registrarVehiculo(requestDTO));
        verify(vehiculoRepository, never()).existsByPlaca(any());
        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("Debe actualizar el estado del vehículo cuando el nuevo estado es válido")
    void cambiarEstadoVehiculo_EstadoValido_ActualizaEstado() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("SJO-123");
        vehiculo.setEstado("DISPONIBLE");
        vehiculo.setEmpresa(empresa);

        CambioEstadoDTO cambioEstadoDTO = new CambioEstadoDTO();
        cambioEstadoDTO.setNuevoEstado("MANTENIMIENTO");

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehiculoResponseDTO resultado = vehiculoService.cambiarEstadoVehiculo(1, cambioEstadoDTO);

        assertEquals("MANTENIMIENTO", resultado.getEstado());
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el nuevo estado no es válido")
    void cambiarEstadoVehiculo_EstadoInvalido_LanzaIllegalArgumentException() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setEstado("DISPONIBLE");

        CambioEstadoDTO cambioEstadoDTO = new CambioEstadoDTO();
        cambioEstadoDTO.setNuevoEstado("EN_RUTA_XYZ");

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));

        assertThrows(IllegalArgumentException.class,
                () -> vehiculoService.cambiarEstadoVehiculo(1, cambioEstadoDTO));
        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el vehículo no existe")
    void cambiarEstadoVehiculo_VehiculoInexistente_LanzaResourceNotFoundException() {
        CambioEstadoDTO cambioEstadoDTO = new CambioEstadoDTO();
        cambioEstadoDTO.setNuevoEstado("MANTENIMIENTO");

        when(vehiculoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehiculoService.cambiarEstadoVehiculo(99, cambioEstadoDTO));
    }
}