package cr.ac.ucr.paraiso.ie.c5h153.expresofast.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioResponseDTO;
import jakarta.validation.Valid;

@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:5500", "http://localhost:5500"})
@RestController
@RequestMapping("/api/v1/envios")
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
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer id, @RequestBody CambioEstadoDTO request,
            Authentication authentication) {
        try {
            EnvioResponseDTO response = envioService.actualizarEstadoEnvio(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); 
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

    @GetMapping
    public ResponseEntity<Page<EnvioDTO>> obtenerEnviosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado) {
        Page<EnvioDTO> envios = envioService.listarPaginado(page, size, sortBy, direction, busqueda, estado);
        return ResponseEntity.ok(envios);
    }

    @GetMapping("/procedimiento/{estado}")
    public ResponseEntity<List<EnvioDTO>> obtenerEnviosPorProcedimiento(@PathVariable String estado) {
        List<EnvioDTO> envios = envioService.listarViaStoredProcedure(estado);
        return ResponseEntity.ok(envios);
    }
}
