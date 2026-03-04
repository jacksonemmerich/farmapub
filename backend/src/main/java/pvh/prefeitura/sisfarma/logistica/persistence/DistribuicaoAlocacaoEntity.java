package pvh.prefeitura.sisfarma.logistica.persistence;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "distribuicao_alocacao")
public class DistribuicaoAlocacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribuicao_id", nullable = false)
    private DistribuicaoEntity distribuicao;

    @Column(name = "lote_id", nullable = false)
    private String loteId;

    @Column(name = "codigo_lote", nullable = false)
    private String codigoLote;

    @Column(nullable = false)
    private LocalDate validade;

    @Column(name = "quantidade_separada", nullable = false)
    private int quantidadeSeparada;

    @Column(nullable = false)
    private String local;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DistribuicaoEntity getDistribuicao() {
        return distribuicao;
    }

    public void setDistribuicao(DistribuicaoEntity distribuicao) {
        this.distribuicao = distribuicao;
    }

    public String getLoteId() {
        return loteId;
    }

    public void setLoteId(String loteId) {
        this.loteId = loteId;
    }

    public String getCodigoLote() {
        return codigoLote;
    }

    public void setCodigoLote(String codigoLote) {
        this.codigoLote = codigoLote;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    public int getQuantidadeSeparada() {
        return quantidadeSeparada;
    }

    public void setQuantidadeSeparada(int quantidadeSeparada) {
        this.quantidadeSeparada = quantidadeSeparada;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }
}
