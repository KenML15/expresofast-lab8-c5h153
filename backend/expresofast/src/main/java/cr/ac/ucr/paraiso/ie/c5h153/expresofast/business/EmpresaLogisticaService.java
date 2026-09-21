package cr.ac.ucr.paraiso.ie.c5h153.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class EmpresaLogisticaService {

    private final EmpresaLogisticaRepository repository;

    public EmpresaLogisticaService(EmpresaLogisticaRepository repository) {
        this.repository = repository;
    }

    public EmpresaLogistica registrarEmpresa(EmpresaLogistica empresa) {
        empresa.setFechaRegistro(LocalDateTime.now());
        return repository.save(empresa);
    }

    @Transactional(readOnly = true)
    public EmpresaLogistica obtenerEmpresaPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + id));
    }
}