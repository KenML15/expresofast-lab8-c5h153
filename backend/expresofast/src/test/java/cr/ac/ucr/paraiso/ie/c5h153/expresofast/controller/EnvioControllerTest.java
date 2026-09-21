package cr.ac.ucr.paraiso.ie.c5h153.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.security.JwtTokenProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvioController.class)
@AutoConfigureMockMvc(addFilters = false) 
class EnvioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnvioService envioService;

  
    @MockBean
    private JwtTokenProvider jwtTokenProvider; 

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/envios/{id} - Debe retornar 200 OK y el DTO en JSON")
    void obtenerEnvio_IdValido_Retorna200Ok() throws Exception {

        EnvioResponseDTO responseDTO = new EnvioResponseDTO(
                1, 
                "TRK-12345", 
                "Paraíso, Cartago", 
                new BigDecimal("50.00"), 
                new BigDecimal("5000.00"), 
                "PENDIENTE", 
                "SJO-123", 
                "Juan Pérez"
        );
        
        when(envioService.obtenerEnvioPorId(1)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/envios/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoRastreo").value("TRK-12345")); 
    }

    @Test
    @DisplayName("GET /api/envios/{id} - Debe retornar 404 si el recurso no existe")
    void obtenerEnvio_IdInvalido_Retorna404() throws Exception {

        when(envioService.obtenerEnvioPorId(99))
                .thenThrow(new ResourceNotFoundException("Envío no encontrado con ID: 99"));

        mockMvc.perform(get("/api/envios/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); 
    }

    @Test
    @DisplayName("POST /api/envios - Payload inválido retorna 400 Bad Request")
    void crearEnvio_PayloadInvalido_Retorna400() throws Exception {
        EnvioRequestDTO requestInvalido = new EnvioRequestDTO();
        
        requestInvalido.setPesoKg(new BigDecimal("-10.00")); 
        


        mockMvc.perform(post("/api/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest()); 
    }
}