package pvh.prefeitura.sisfarma.logistica.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteEstoqueRepository extends JpaRepository<LoteEstoqueEntity, String> {

    List<LoteEstoqueEntity> findByLocalEstoqueIgnoreCaseAndMedicamentoIgnoreCaseAndValidadeGreaterThanEqualAndQuantidadeGreaterThanOrderByValidadeAscCreatedAtAsc(
        String localEstoque,
        String medicamento,
        LocalDate validade,
        int quantidade
    );

    Optional<LoteEstoqueEntity> findFirstByLocalEstoqueIgnoreCaseAndMedicamentoIgnoreCaseAndCodigoLoteIgnoreCaseAndValidade(
        String localEstoque,
        String medicamento,
        String codigoLote,
        LocalDate validade
    );
}
