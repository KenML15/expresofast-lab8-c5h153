package cr.ac.ucr.paraiso.ie.c5h153.expresofast.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.Conductor;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Integer> {
}