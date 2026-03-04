package pvh.prefeitura.sisfarma.logistica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import pvh.prefeitura.sisfarma.logistica.persistence.DistribuicaoAlocacaoEntity;
import pvh.prefeitura.sisfarma.logistica.persistence.DistribuicaoEntity;
import pvh.prefeitura.sisfarma.logistica.persistence.DistribuicaoRepository;
import pvh.prefeitura.sisfarma.logistica.persistence.LoteEstoqueEntity;
import pvh.prefeitura.sisfarma.logistica.persistence.LoteEstoqueRepository;
import pvh.prefeitura.sisfarma.logistica.persistence.TransferenciaAlocacaoEntity;
import pvh.prefeitura.sisfarma.logistica.persistence.TransferenciaEntity;
import pvh.prefeitura.sisfarma.logistica.persistence.TransferenciaRepository;

@Service
public class LogisticaService {

    private final LoteEstoqueRepository loteRepository;
    private final DistribuicaoRepository distribuicaoRepository;
    private final TransferenciaRepository transferenciaRepository;

    public LogisticaService(
        LoteEstoqueRepository loteRepository,
        DistribuicaoRepository distribuicaoRepository,
        TransferenciaRepository transferenciaRepository
    ) {
        this.loteRepository = loteRepository;
        this.distribuicaoRepository = distribuicaoRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @PostConstruct
    @Transactional
    public void seedInicial() {
        if (loteRepository.count() > 0) {
            return;
        }
        adicionarLote("Dipirona 500mg", "DIP-001", LocalDate.now().plusDays(120), 500, "ESTOQUE_CENTRAL");
        adicionarLote("Dipirona 500mg", "DIP-002", LocalDate.now().plusDays(40), 220, "ESTOQUE_CENTRAL");
        adicionarLote("Amoxicilina 500mg", "AMX-001", LocalDate.now().plusDays(180), 300, "ESTOQUE_CENTRAL");
        adicionarLote("Soro Fisiológico 500ml", "SOR-001", LocalDate.now().plusDays(70), 180, "ESTOQUE_CENTRAL");
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<LoteEstoqueEntity> lotes = loteRepository.findAll();
        int estoqueTotal = lotes.stream().mapToInt(LoteEstoqueEntity::getQuantidade).sum();
        long criticos = lotes.stream().filter(l -> "CRITICO".equals(calcularStatus(l.getValidade()))).count();
        long atencao = lotes.stream().filter(l -> "ATENCAO".equals(calcularStatus(l.getValidade()))).count();

        long distribuicoesPendentes = distribuicaoRepository.findAll().stream().filter(d -> !"RECEBIDA".equals(d.getStatus())).count();
        long transferenciasPendentes = transferenciaRepository.findAll().stream().filter(t -> !"RECEBIDA".equals(t.getStatus())).count();

        return new DashboardResponse(estoqueTotal, (int) criticos, (int) atencao, distribuicoesPendentes, transferenciasPendentes);
    }

    @Transactional(readOnly = true)
    public List<LoteResponse> listarLotes(String local, String medicamento) {
        return loteRepository.findAll().stream()
            .filter(l -> local == null || local.isBlank() || l.getLocalEstoque().equalsIgnoreCase(local))
            .filter(l -> medicamento == null || medicamento.isBlank() || l.getMedicamento().toLowerCase().contains(medicamento.toLowerCase()))
            .sorted(Comparator.comparing(LoteEstoqueEntity::getValidade).thenComparing(LoteEstoqueEntity::getCreatedAt))
            .map(this::toLoteResponse)
            .toList();
    }

    @Transactional
    public LoteResponse entradaLote(EntradaLoteRequest request) {
        LoteEstoqueEntity lote = adicionarLote(
            request.medicamento(),
            request.codigoLote(),
            request.validade(),
            request.quantidade(),
            request.local()
        );
        return toLoteResponse(lote);
    }

    @Transactional(readOnly = true)
    public List<AlocacaoResponse> sugerirFefo(SolicitacaoFefoRequest request) {
        var candidatos = loteRepository
            .findByLocalEstoqueIgnoreCaseAndMedicamentoIgnoreCaseAndValidadeGreaterThanEqualAndQuantidadeGreaterThanOrderByValidadeAscCreatedAtAsc(
                request.origem(),
                request.medicamento(),
                LocalDate.now(),
                0
            );

        int restante = request.quantidade();
        List<AlocacaoResponse> alocacoes = new java.util.ArrayList<>();
        for (LoteEstoqueEntity lote : candidatos) {
            if (restante <= 0) {
                break;
            }
            int usar = Math.min(restante, lote.getQuantidade());
            restante -= usar;
            alocacoes.add(new AlocacaoResponse(
                lote.getId(),
                lote.getCodigoLote(),
                lote.getValidade(),
                usar,
                lote.getLocalEstoque()
            ));
        }

        return alocacoes;
    }

    @Transactional
    public DistribuicaoResponse criarDistribuicao(CriarDistribuicaoRequest request) {
        List<AlocacaoResponse> alocacoes = sugerirFefo(new SolicitacaoFefoRequest(
            request.origem(),
            request.medicamento(),
            request.quantidade()
        ));

        DistribuicaoEntity distribuicao = new DistribuicaoEntity();
        distribuicao.setId(UUID.randomUUID().toString());
        distribuicao.setOrigem(request.origem());
        distribuicao.setDestino(request.destino());
        distribuicao.setMedicamento(request.medicamento());
        distribuicao.setQuantidadeSolicitada(request.quantidade());
        distribuicao.setStatus("RASCUNHO");
        distribuicao.setCreatedAt(LocalDateTime.now());

        distribuicao.setAlocacoes(
            alocacoes.stream().map(alocacao -> {
                DistribuicaoAlocacaoEntity entity = new DistribuicaoAlocacaoEntity();
                entity.setDistribuicao(distribuicao);
                entity.setLoteId(alocacao.loteId());
                entity.setCodigoLote(alocacao.codigoLote());
                entity.setValidade(alocacao.validade());
                entity.setQuantidadeSeparada(alocacao.quantidadeSeparada());
                entity.setLocal(alocacao.local());
                return entity;
            }).toList()
        );

        return toDistribuicaoResponse(distribuicaoRepository.save(distribuicao));
    }

    @Transactional(readOnly = true)
    public List<DistribuicaoResponse> listarDistribuicoes() {
        return distribuicaoRepository.findAll().stream().map(this::toDistribuicaoResponse).toList();
    }

    @Transactional
    public Optional<DistribuicaoResponse> expedirDistribuicao(String id) {
        Optional<DistribuicaoEntity> maybe = distribuicaoRepository.findById(id);
        if (maybe.isEmpty()) {
            return Optional.empty();
        }
        DistribuicaoEntity atual = maybe.get();
        if (!"RASCUNHO".equals(atual.getStatus())) {
            return Optional.empty();
        }

        for (DistribuicaoAlocacaoEntity alocacao : atual.getAlocacoes()) {
            LoteEstoqueEntity lote = loteRepository.findById(alocacao.getLoteId()).orElse(null);
            if (lote == null || lote.getQuantidade() < alocacao.getQuantidadeSeparada()) {
                return Optional.empty();
            }
        }

        for (DistribuicaoAlocacaoEntity alocacao : atual.getAlocacoes()) {
            LoteEstoqueEntity lote = loteRepository.findById(alocacao.getLoteId()).orElseThrow();
            lote.setQuantidade(Math.max(0, lote.getQuantidade() - alocacao.getQuantidadeSeparada()));
            loteRepository.save(lote);
        }

        atual.setStatus("EXPEDIDA");
        return Optional.of(toDistribuicaoResponse(distribuicaoRepository.save(atual)));
    }

    @Transactional
    public Optional<DistribuicaoResponse> receberDistribuicao(String id) {
        Optional<DistribuicaoEntity> maybe = distribuicaoRepository.findById(id);
        if (maybe.isEmpty()) {
            return Optional.empty();
        }
        DistribuicaoEntity atual = maybe.get();
        if (!"EXPEDIDA".equals(atual.getStatus())) {
            return Optional.empty();
        }

        for (DistribuicaoAlocacaoEntity alocacao : atual.getAlocacoes()) {
            upsertLoteDestino(
                atual.getDestino(),
                atual.getMedicamento(),
                alocacao.getCodigoLote(),
                alocacao.getValidade(),
                alocacao.getQuantidadeSeparada()
            );
        }

        atual.setStatus("RECEBIDA");
        return Optional.of(toDistribuicaoResponse(distribuicaoRepository.save(atual)));
    }

    @Transactional
    public TransferenciaResponse criarTransferencia(CriarTransferenciaRequest request) {
        List<AlocacaoResponse> alocacoes = sugerirFefo(new SolicitacaoFefoRequest(
            request.origem(),
            request.medicamento(),
            request.quantidade()
        ));

        TransferenciaEntity transferencia = new TransferenciaEntity();
        transferencia.setId(UUID.randomUUID().toString());
        transferencia.setOrigem(request.origem());
        transferencia.setDestino(request.destino());
        transferencia.setMedicamento(request.medicamento());
        transferencia.setQuantidadeSolicitada(request.quantidade());
        transferencia.setQuantidadeRecebida(null);
        transferencia.setStatus("RASCUNHO");
        transferencia.setCreatedAt(LocalDateTime.now());

        transferencia.setAlocacoes(
            alocacoes.stream().map(alocacao -> {
                TransferenciaAlocacaoEntity entity = new TransferenciaAlocacaoEntity();
                entity.setTransferencia(transferencia);
                entity.setLoteId(alocacao.loteId());
                entity.setCodigoLote(alocacao.codigoLote());
                entity.setValidade(alocacao.validade());
                entity.setQuantidadeSeparada(alocacao.quantidadeSeparada());
                entity.setLocal(alocacao.local());
                return entity;
            }).toList()
        );

        return toTransferenciaResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional(readOnly = true)
    public List<TransferenciaResponse> listarTransferencias() {
        return transferenciaRepository.findAll().stream().map(this::toTransferenciaResponse).toList();
    }

    @Transactional
    public Optional<TransferenciaResponse> enviarTransferencia(String id) {
        Optional<TransferenciaEntity> maybe = transferenciaRepository.findById(id);
        if (maybe.isEmpty()) {
            return Optional.empty();
        }
        TransferenciaEntity atual = maybe.get();
        if (!"RASCUNHO".equals(atual.getStatus())) {
            return Optional.empty();
        }

        for (TransferenciaAlocacaoEntity alocacao : atual.getAlocacoes()) {
            LoteEstoqueEntity lote = loteRepository.findById(alocacao.getLoteId()).orElse(null);
            if (lote == null || lote.getQuantidade() < alocacao.getQuantidadeSeparada()) {
                return Optional.empty();
            }
        }

        for (TransferenciaAlocacaoEntity alocacao : atual.getAlocacoes()) {
            LoteEstoqueEntity lote = loteRepository.findById(alocacao.getLoteId()).orElseThrow();
            lote.setQuantidade(Math.max(0, lote.getQuantidade() - alocacao.getQuantidadeSeparada()));
            loteRepository.save(lote);
        }

        atual.setStatus("ENVIADA");
        atual.setQuantidadeRecebida(null);
        return Optional.of(toTransferenciaResponse(transferenciaRepository.save(atual)));
    }

    @Transactional
    public Optional<TransferenciaResponse> receberTransferencia(String id, ReceberTransferenciaRequest request) {
        Optional<TransferenciaEntity> maybe = transferenciaRepository.findById(id);
        if (maybe.isEmpty()) {
            return Optional.empty();
        }
        TransferenciaEntity atual = maybe.get();
        if (!"ENVIADA".equals(atual.getStatus())) {
            return Optional.empty();
        }

        int quantidadeRecebida = request.quantidadeRecebida() != null ? request.quantidadeRecebida() : atual.getQuantidadeSolicitada();
        quantidadeRecebida = Math.max(0, quantidadeRecebida);
        int quantidadeDisponivel = atual.getAlocacoes().stream().mapToInt(TransferenciaAlocacaoEntity::getQuantidadeSeparada).sum();
        quantidadeRecebida = Math.min(quantidadeRecebida, quantidadeDisponivel);

        int restante = quantidadeRecebida;
        for (TransferenciaAlocacaoEntity alocacao : atual.getAlocacoes()) {
            if (restante <= 0) {
                break;
            }
            int usar = Math.min(restante, alocacao.getQuantidadeSeparada());
            restante -= usar;
            upsertLoteDestino(
                atual.getDestino(),
                atual.getMedicamento(),
                alocacao.getCodigoLote(),
                alocacao.getValidade(),
                usar
            );
        }

        atual.setQuantidadeRecebida(quantidadeRecebida);
        atual.setStatus(quantidadeRecebida < quantidadeDisponivel ? "COM_DIVERGENCIA" : "RECEBIDA");
        return Optional.of(toTransferenciaResponse(transferenciaRepository.save(atual)));
    }

    private LoteEstoqueEntity adicionarLote(String medicamento, String codigoLote, LocalDate validade, int quantidade, String local) {
        LoteEstoqueEntity lote = new LoteEstoqueEntity();
        lote.setId(UUID.randomUUID().toString());
        lote.setMedicamento(medicamento);
        lote.setCodigoLote(codigoLote);
        lote.setValidade(validade);
        lote.setQuantidade(Math.max(quantidade, 0));
        lote.setLocalEstoque(local);
        lote.setCreatedAt(LocalDateTime.now());
        return loteRepository.save(lote);
    }

    private void upsertLoteDestino(String local, String medicamento, String codigoLote, LocalDate validade, int quantidade) {
        if (quantidade <= 0) {
            return;
        }

        Optional<LoteEstoqueEntity> existente = loteRepository
            .findFirstByLocalEstoqueIgnoreCaseAndMedicamentoIgnoreCaseAndCodigoLoteIgnoreCaseAndValidade(
                local,
                medicamento,
                codigoLote,
                validade
            );

        if (existente.isPresent()) {
            LoteEstoqueEntity lote = existente.get();
            lote.setQuantidade(lote.getQuantidade() + quantidade);
            loteRepository.save(lote);
            return;
        }

        adicionarLote(medicamento, codigoLote, validade, quantidade, local);
    }

    private String calcularStatus(LocalDate validade) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), validade);
        if (dias <= 30) {
            return "CRITICO";
        }
        if (dias <= 90) {
            return "ATENCAO";
        }
        return "OK";
    }

    private LoteResponse toLoteResponse(LoteEstoqueEntity lote) {
        return new LoteResponse(
            lote.getId(),
            lote.getMedicamento(),
            lote.getCodigoLote(),
            lote.getValidade(),
            lote.getQuantidade(),
            lote.getLocalEstoque(),
            calcularStatus(lote.getValidade())
        );
    }

    private DistribuicaoResponse toDistribuicaoResponse(DistribuicaoEntity distribuicao) {
        List<AlocacaoResponse> alocacoes = distribuicao.getAlocacoes().stream()
            .map(a -> new AlocacaoResponse(a.getLoteId(), a.getCodigoLote(), a.getValidade(), a.getQuantidadeSeparada(), a.getLocal()))
            .toList();

        return new DistribuicaoResponse(
            distribuicao.getId(),
            distribuicao.getOrigem(),
            distribuicao.getDestino(),
            distribuicao.getMedicamento(),
            distribuicao.getQuantidadeSolicitada(),
            distribuicao.getStatus(),
            distribuicao.getCreatedAt(),
            alocacoes
        );
    }

    private TransferenciaResponse toTransferenciaResponse(TransferenciaEntity transferencia) {
        List<AlocacaoResponse> alocacoes = transferencia.getAlocacoes().stream()
            .map(a -> new AlocacaoResponse(a.getLoteId(), a.getCodigoLote(), a.getValidade(), a.getQuantidadeSeparada(), a.getLocal()))
            .toList();

        return new TransferenciaResponse(
            transferencia.getId(),
            transferencia.getOrigem(),
            transferencia.getDestino(),
            transferencia.getMedicamento(),
            transferencia.getQuantidadeSolicitada(),
            transferencia.getQuantidadeRecebida(),
            transferencia.getStatus(),
            transferencia.getCreatedAt(),
            alocacoes
        );
    }

    public record DashboardResponse(
        int estoqueTotal,
        int lotesCriticos,
        int lotesAtencao,
        long distribuicoesPendentes,
        long transferenciasPendentes
    ) {
    }

    public record EntradaLoteRequest(
        String medicamento,
        String codigoLote,
        LocalDate validade,
        Integer quantidade,
        String local
    ) {
        public EntradaLoteRequest {
            Objects.requireNonNull(medicamento, "medicamento é obrigatório");
            Objects.requireNonNull(codigoLote, "codigoLote é obrigatório");
            Objects.requireNonNull(validade, "validade é obrigatória");
            Objects.requireNonNull(local, "local é obrigatório");
            if (quantidade == null || quantidade <= 0) {
                throw new IllegalArgumentException("quantidade deve ser maior que zero");
            }
        }
    }

    public record SolicitacaoFefoRequest(String origem, String medicamento, Integer quantidade) {
        public SolicitacaoFefoRequest {
            Objects.requireNonNull(origem, "origem é obrigatória");
            Objects.requireNonNull(medicamento, "medicamento é obrigatório");
            if (quantidade == null || quantidade <= 0) {
                throw new IllegalArgumentException("quantidade deve ser maior que zero");
            }
        }
    }

    public record CriarDistribuicaoRequest(String origem, String destino, String medicamento, Integer quantidade) {
        public CriarDistribuicaoRequest {
            Objects.requireNonNull(origem, "origem é obrigatória");
            Objects.requireNonNull(destino, "destino é obrigatório");
            Objects.requireNonNull(medicamento, "medicamento é obrigatório");
            if (quantidade == null || quantidade <= 0) {
                throw new IllegalArgumentException("quantidade deve ser maior que zero");
            }
        }
    }

    public record CriarTransferenciaRequest(String origem, String destino, String medicamento, Integer quantidade) {
        public CriarTransferenciaRequest {
            Objects.requireNonNull(origem, "origem é obrigatória");
            Objects.requireNonNull(destino, "destino é obrigatório");
            Objects.requireNonNull(medicamento, "medicamento é obrigatório");
            if (quantidade == null || quantidade <= 0) {
                throw new IllegalArgumentException("quantidade deve ser maior que zero");
            }
        }
    }

    public record ReceberTransferenciaRequest(Integer quantidadeRecebida) {
    }

    public record AlocacaoResponse(
        String loteId,
        String codigoLote,
        LocalDate validade,
        int quantidadeSeparada,
        String local
    ) {
    }

    public record LoteResponse(
        String id,
        String medicamento,
        String codigoLote,
        LocalDate validade,
        int quantidade,
        String local,
        String status
    ) {
    }

    public record DistribuicaoResponse(
        String id,
        String origem,
        String destino,
        String medicamento,
        int quantidadeSolicitada,
        String status,
        LocalDateTime createdAt,
        List<AlocacaoResponse> alocacoes
    ) {
    }

    public record TransferenciaResponse(
        String id,
        String origem,
        String destino,
        String medicamento,
        int quantidadeSolicitada,
        Integer quantidadeRecebida,
        String status,
        LocalDateTime createdAt,
        List<AlocacaoResponse> alocacoes
    ) {
    }
}
