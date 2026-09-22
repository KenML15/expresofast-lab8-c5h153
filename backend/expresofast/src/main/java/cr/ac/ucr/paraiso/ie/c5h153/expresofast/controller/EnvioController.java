package cr.ac.ucr.paraiso.ie.c5h153.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioResponseDTO;
import jakarta.validation.Valid;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/optimizados")
    public ResponseEntity<List<EnvioResponseDTO>> obtenerEnviosOptimizados() {
        return ResponseEntity.ok(envioService.obtenerEnviosOptimizados());
    }

    @PostMapping
    public ResponseEntity<EnvioResponseDTO> registrarEnvio(@Valid @RequestBody EnvioRequestDTO request) {
        EnvioResponseDTO nuevoEnvio = envioService.registrarEnvio(request);
        return ResponseEntity.ok(nuevoEnvio);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer id, @RequestBody CambioEstadoDTO request, Authentication authentication) {
        try {
            EnvioResponseDTO response = envioService.actualizarEstadoEnvio(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Esto imprimirá la línea exacta del error en rojo en tu IDE
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDTO> obtenerEnvioPorId(@PathVariable Integer id) {
        EnvioResponseDTO envio = envioService.obtenerEnvioPorId(id);
        return ResponseEntity.ok(envio);
    }

    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerBitacoraDeEnvio(id));
    }
}
