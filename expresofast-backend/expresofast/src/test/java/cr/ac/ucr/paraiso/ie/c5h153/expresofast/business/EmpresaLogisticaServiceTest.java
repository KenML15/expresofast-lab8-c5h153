package cr.ac.ucr.paraiso.ie.c5h153.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaLogisticaServiceTest {

    @Mock
    private EmpresaLogisticaRepository repository;

    @InjectMocks
    private EmpresaLogisticaService service;

    private EmpresaLogistica empresa;

    @BeforeEach
    void setUp() {
        empresa = new EmpresaLogistica();
        empresa.setId(1);
        empresa.setNombre("Logística Paraíso");
        empresa.setCedulaJuridica("3-101-123456");
        empresa.setTelefono("2574-0000");
    }

    @Test
    @DisplayName("Debe registrar una empresa exitosamente asignando la fecha de registro")
    void registrarEmpresa_DatosValidos_RetornaEmpresaGuardada() {
        when(repository.save(any(EmpresaLogistica.class))).thenAnswer(invocation -> {
            EmpresaLogistica guardada = invocation.getArgument(0);
            guardada.setId(100);
            return guardada;
        });

        EmpresaLogistica resultado = service.registrarEmpresa(empresa);

        assertNotNull(resultado);
        assertNotNull(resultado.getFechaRegistro());
        assertEquals(100, resultado.getId());
        verify(repository, times(1)).save(any(EmpresaLogistica.class));
    }

    @Test
    @DisplayName("Debe retornar la empresa si el ID existe")
    void obtenerEmpresa_IdValido_RetornaEmpresa() {
        when(repository.findById(1)).thenReturn(Optional.of(empresa));

        EmpresaLogistica resultado = service.obtenerEmpresaPorId(1);

        assertNotNull(resultado);
        assertEquals("Logística Paraíso", resultado.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la empresa no existe")
    void obtenerEmpresa_IdInvalido_LanzaExcepcion() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerEmpresaPorId(99));
        verify(repository, times(1)).findById(99);
    }
}