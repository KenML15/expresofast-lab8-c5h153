package cr.ac.ucr.paraiso.ie.c5h153.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.business.AuthService;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.AuthResponseDTO;

// Ajusta estos imports al nombre exacto de tus clases DTO y controladores

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.security.JwtTokenProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) 
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

 @Test
    @DisplayName("POST /api/auth/login - Credenciales Correctas retorna 200 OK y Token")
    void login_CredencialesCorrectas_Retorna200YToken() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("password123");


        AuthResponseDTO responseMock = new AuthResponseDTO(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mockToken",
                "admin",
                java.util.List.of("ROLE_ADMIN"), 
                3600000L
        );

        when(authService.login(any(AuthRequestDTO.class))).thenReturn(responseMock);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login - Credenciales Incorrectas retorna 401 Unauthorized")
    void login_CredencialesIncorrectas_Retorna401() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("claveIncorrecta");

        when(authService.login(any(AuthRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); 
    }
}