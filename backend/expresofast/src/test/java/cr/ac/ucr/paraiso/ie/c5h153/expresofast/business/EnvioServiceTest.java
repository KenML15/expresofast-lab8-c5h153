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
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private ConductorRepository conductorRepository;
    @Mock
    private BitacoraEnvioRepository bitacoraEnvioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EnvioService envioService;

    private Vehiculo vehiculo;
    private Conductor conductor;
    private EnvioRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("SJO-123");
        vehiculo.setCapacidadKg(new BigDecimal("500.00"));

        conductor = new Conductor();
        conductor.setId(1);
        conductor.setNombre("Carlos");
        conductor.setApellidos("Mora Vargas");

        requestDTO = new EnvioRequestDTO();
        requestDTO.setCodigoRastreo("EXP-1001");
        requestDTO.setDireccionDestino("San José, Costa Rica");
        requestDTO.setPesoKg(new BigDecimal("100.00"));
        requestDTO.setCosto(new BigDecimal("15000.00"));
        requestDTO.setVehiculoId(1);
        requestDTO.setConductorId(1);
    }

    @Test
    @DisplayName("Debe registrar un envío válido y retornarlo en estado PENDIENTE")
    void registrarEnvio_DatosValidos_RetornaEnvioResponseDTO() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> {
            Envio envioGuardado = invocation.getArgument(0);
            envioGuardado.setId(10);
            return envioGuardado;
        });

        EnvioResponseDTO resultado = envioService.registrarEnvio(requestDTO);

        assertNotNull(resultado);
        assertEquals("PENDIENTE", resultado.getEstadoEnvio());
        assertEquals("EXP-1001", resultado.getCodigoRastreo());
        assertEquals("SJO-123", resultado.getPlacaVehiculo());
        assertEquals("Carlos Mora Vargas", resultado.getNombreConductor());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el peso supera la capacidad del vehículo")
    void registrarEnvio_PesoExcedeCapacidad_LanzaIllegalArgumentException() {
        requestDTO.setPesoKg(new BigDecimal("999.00"));

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));

        assertThrows(IllegalArgumentException.class, () -> envioService.registrarEnvio(requestDTO));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el vehículo no existe")
    void registrarEnvio_VehiculoInexistente_LanzaResourceNotFoundException() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> envioService.registrarEnvio(requestDTO));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidStateTransitionException si un envío ENTREGADO intenta volver a EN_TRANSITO")
    void actualizarEstadoEnvio_TransicionDeEntregadoAEnTransito_LanzaInvalidStateTransitionException() {
        Envio envio = new Envio();
        envio.setId(10);
        envio.setCodigoRastreo("EXP-1001");
        envio.setEstadoEnvio("ENTREGADO");

        CambioEstadoDTO cambioEstadoDTO = new CambioEstadoDTO();
        cambioEstadoDTO.setNuevoEstado("EN_TRANSITO");

        when(envioRepository.findById(10)).thenReturn(Optional.of(envio));

        assertThrows(InvalidStateTransitionException.class,
                () -> envioService.actualizarEstadoEnvio(10, cambioEstadoDTO));

        verify(envioRepository, never()).save(any(Envio.class));
        verify(bitacoraEnvioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar el estado y registrar la bitácora en una transición válida")
    void actualizarEstadoEnvio_TransicionValida_ActualizaYRegistraEnBitacora() {
        Envio envio = new Envio();
        envio.setId(10);
        envio.setCodigoRastreo("EXP-1001");
        envio.setEstadoEnvio("PENDIENTE");
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);

        CambioEstadoDTO cambioEstadoDTO = new CambioEstadoDTO();
        cambioEstadoDTO.setNuevoEstado("EN_TRANSITO");
        cambioEstadoDTO.setObservaciones("Sale de bodega central");

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setUsername("kmora");
        usuarioAutenticado.setNombreCompleto("Kenneth Mora");

        when(envioRepository.findById(10)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findByUsername("kmora")).thenReturn(Optional.of(usuarioAutenticado));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("kmora");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            EnvioResponseDTO resultado = envioService.actualizarEstadoEnvio(10, cambioEstadoDTO);

            assertEquals("EN_TRANSITO", resultado.getEstadoEnvio());
        }

        verify(bitacoraEnvioRepository, times(1))
                .save(argThat(bitacora -> bitacora.getEstadoAnterior().equals("PENDIENTE")
                        && bitacora.getEstadoNuevo().equals("EN_TRANSITO")
                        && bitacora.getObservaciones().equals("Sale de bodega central")));
    }

    @Test
    @DisplayName("No debe permitir cancelar envíos que están en ruta")
    void cancelarEnvio_EnvioEnTransito_LanzaExcepcion() {
        Envio envioEnRuta = new Envio();
        envioEnRuta.setId(1);
        envioEnRuta.setEstadoEnvio("EN_TRANSITO");

        when(envioRepository.findById(1)).thenReturn(Optional.of(envioEnRuta));

        assertThrows(IllegalArgumentException.class, () -> envioService.cancelarEnvio(1));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @ParameterizedTest
    @CsvSource({
            "5.0, 10.0, 2500.0",
            "15.0, 50.0, 7500.0",
            "100.0, 2.5, 12000.0"
    })
    @DisplayName("Debe calcular la tarifa correcta según peso y distancia")
    void calcularTarifa_CasosVariados_CalculaCorrectamente(double pesoKg, double distanciaKm, double tarifaEsperada) {
        double tarifaCalculada = envioService.calcularTarifa(pesoKg, distanciaKm);
        assertEquals(tarifaEsperada, tarifaCalculada, 0.01);
    }

   @Test
    @DisplayName("Debe registrar un envío exitosamente si el peso es adecuado")
    void registrarEnvio_DatosValidos_GuardaEnvio() {
        EnvioRequestDTO request = new EnvioRequestDTO();
        request.setVehiculoId(1);
        request.setConductorId(1);
        request.setPesoKg(BigDecimal.valueOf(50.0));
        request.setCodigoRastreo("TRK-123");

        Vehiculo vehiculo = new Vehiculo(); 
        vehiculo.setCapacidadKg(BigDecimal.valueOf(100.0)); 
        vehiculo.setPlaca("ABC-123");
        
        Conductor conductor = new Conductor(); 
        conductor.setNombre("Juan"); 
        conductor.setApellidos("Pérez");

        when(vehiculoRepository.findById(1)).thenReturn(java.util.Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(java.util.Optional.of(conductor));
        when(envioRepository.save(any(Envio.class))).thenAnswer(i -> {
            Envio e = i.getArgument(0); e.setId(10); return e;
        });

        EnvioResponseDTO response = envioService.registrarEnvio(request);

        assertEquals("TRK-123", response.getCodigoRastreo());
        verify(envioRepository).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debe fallar al registrar envío si el peso supera la capacidad")
    void registrarEnvio_PesoExcesivo_LanzaExcepcion() {
        EnvioRequestDTO request = new EnvioRequestDTO();
        request.setVehiculoId(1);
        request.setConductorId(1);
        request.setPesoKg(BigDecimal.valueOf(150.0)); 

        Vehiculo vehiculo = new Vehiculo(); 
        vehiculo.setCapacidadKg(BigDecimal.valueOf(100.0));
        Conductor conductor = new Conductor();

        when(vehiculoRepository.findById(1)).thenReturn(java.util.Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(java.util.Optional.of(conductor));

        assertThrows(IllegalArgumentException.class, () -> envioService.registrarEnvio(request));
        verify(envioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar el estado y registrar en bitácora si la transición es válida")
    void actualizarEstadoEnvio_TransicionValida_ActualizaYRegistraBitacora() {
        CambioEstadoDTO request = new CambioEstadoDTO(); request.setNuevoEstado("EN_TRANSITO");
        
        Envio envio = new Envio(); envio.setId(1); envio.setCodigoRastreo("TRK-123"); envio.setEstadoEnvio("PENDIENTE");
        Vehiculo vehiculo = new Vehiculo(); vehiculo.setPlaca("ABC-123");
        Conductor conductor = new Conductor(); conductor.setNombre("Juan"); conductor.setApellidos("Perez");
        envio.setVehiculo(vehiculo); envio.setConductor(conductor);

        // Simular usuario autenticado
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        org.springframework.security.core.context.SecurityContext ctx = mock(org.springframework.security.core.context.SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        Usuario usuario = new Usuario(); usuario.setUsername("admin");

        when(envioRepository.findById(1)).thenReturn(java.util.Optional.of(envio));
        when(usuarioRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(usuario));
        when(envioRepository.save(any(Envio.class))).thenReturn(envio);

        EnvioResponseDTO response = envioService.actualizarEstadoEnvio(1, request);

        assertEquals("EN_TRANSITO", response.getEstadoEnvio());
        verify(bitacoraEnvioRepository).save(any(BitacoraEnvio.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe fallar al actualizar si la transición es inválida")
    void actualizarEstadoEnvio_TransicionInvalida_LanzaExcepcion() {
        CambioEstadoDTO request = new CambioEstadoDTO(); request.setNuevoEstado("PENDIENTE");
        Envio envio = new Envio(); envio.setCodigoRastreo("TRK-123"); envio.setEstadoEnvio("ENTREGADO");

        when(envioRepository.findById(1)).thenReturn(java.util.Optional.of(envio));

        assertThrows(InvalidStateTransitionException.class, () -> envioService.actualizarEstadoEnvio(1, request));
    }

    @Test
    @DisplayName("Debe obtener envío por ID exitosamente")
    void obtenerEnvioPorId_Existe_RetornaResponse() {
        Envio envio = new Envio(); envio.setId(1); envio.setCodigoRastreo("TRK-123");
        Vehiculo vehiculo = new Vehiculo(); vehiculo.setPlaca("ABC-123");
        Conductor conductor = new Conductor(); conductor.setNombre("Juan"); conductor.setApellidos("Perez");
        envio.setVehiculo(vehiculo); envio.setConductor(conductor);

        when(envioRepository.findById(1)).thenReturn(java.util.Optional.of(envio));

        EnvioResponseDTO response = envioService.obtenerEnvioPorId(1);
        assertEquals("TRK-123", response.getCodigoRastreo());
    }

    @Test
    @DisplayName("Debe obtener la lista de envíos optimizados")
    void obtenerEnviosOptimizados_RetornaLista() {
        Envio envio = new Envio(); 
        envio.setId(1); 
        envio.setCodigoRastreo("TRK-123");
        Vehiculo vehiculo = new Vehiculo(); 
        vehiculo.setPlaca("123"); 
        envio.setVehiculo(vehiculo);
        Conductor conductor = new Conductor(); 
        conductor.setNombre("J"); 
        conductor.setApellidos("P"); 
        envio.setConductor(conductor);
        
        when(envioRepository.findAllWithDetails()).thenReturn(java.util.List.of(envio));
        
        java.util.List<EnvioResponseDTO> resultado = envioService.obtenerEnviosOptimizados();
        assertFalse(resultado.isEmpty());
        assertEquals("TRK-123", resultado.get(0).getCodigoRastreo());
    }

    @Test
    @DisplayName("Debe retornar la bitácora si el envío existe")
    void obtenerBitacoraDeEnvio_Existe_RetornaBitacora() {
        when(envioRepository.existsById(1)).thenReturn(true);
        
        BitacoraEnvio bitacora = new BitacoraEnvio();
        bitacora.setId(1); 
        bitacora.setEstadoAnterior("PENDIENTE"); 
        bitacora.setEstadoNuevo("EN_TRANSITO");
        Usuario usuario = new Usuario(); 
        usuario.setNombreCompleto("Admin"); 
        bitacora.setUsuario(usuario);
        
        when(bitacoraEnvioRepository.findByEnvioIdOrderByFechaCambioDesc(1))
            .thenReturn(java.util.List.of(bitacora));
        
        java.util.List<BitacoraResponseDTO> resultado = envioService.obtenerBitacoraDeEnvio(1);
        assertFalse(resultado.isEmpty());
        assertEquals("EN_TRANSITO", resultado.get(0).getEstadoNuevo());
    }

    @Test
    @DisplayName("Debe cancelar el envío si el estado lo permite")
    void cancelarEnvio_Valido_CambiaEstado() {
        Envio envio = new Envio(); 
        envio.setId(1); 
        envio.setEstadoEnvio("PENDIENTE");
        Vehiculo vehiculo = new Vehiculo(); 
        vehiculo.setPlaca("123"); 
        envio.setVehiculo(vehiculo);
        Conductor conductor = new Conductor(); 
        conductor.setNombre("J"); 
        conductor.setApellidos("P"); 
        envio.setConductor(conductor);
        
        when(envioRepository.findById(1)).thenReturn(java.util.Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(i -> i.getArgument(0));
        
        EnvioResponseDTO resultado = envioService.cancelarEnvio(1);
        assertEquals("CANCELADO", resultado.getEstadoEnvio());
    }

    @Test
    @DisplayName("Debe calcular tarifas correctamente según el peso")
    void calcularTarifa_DiferentesPesos_CalculaCorrecto() {
        assertEquals(2500.0, envioService.calcularTarifa(5.0, 10.0));
        assertEquals(7500.0, envioService.calcularTarifa(20.0, 10.0));
        assertEquals(12000.0, envioService.calcularTarifa(60.0, 10.0));
    }
}