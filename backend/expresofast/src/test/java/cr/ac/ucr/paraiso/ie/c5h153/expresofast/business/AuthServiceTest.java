package cr.ac.ucr.paraiso.ie.c5h153.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe autenticar al usuario y retornar un token JWT")
    void login_CredencialesCorrectas_RetornaToken() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("123456");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        

        GrantedAuthority authority = () -> "ROLE_ADMIN";
        doReturn(List.of(authority)).when(authentication).getAuthorities();

        when(jwtTokenProvider.generarToken(authentication)).thenReturn("mock.jwt.token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);

        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("admin", response.getUsername());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
    }
}