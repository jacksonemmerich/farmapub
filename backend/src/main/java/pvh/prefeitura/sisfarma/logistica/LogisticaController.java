package pvh.prefeitura.sisfarma.logistica;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistica")
public class LogisticaController {

    private final LogisticaService logisticaService;

    public LogisticaController(LogisticaService logisticaService) {
        this.logisticaService = logisticaService;
    }

    @GetMapping("/dashboard")
    public LogisticaService.DashboardResponse dashboard() {
        return logisticaService.dashboard();
    }

    @GetMapping("/estoque/lotes")
    public List<LogisticaService.LoteResponse> listarLotes(
        @RequestParam(required = false) String local,
        @RequestParam(required = false) String medicamento
    ) {
        return logisticaService.listarLotes(local, medicamento);
    }

    @PostMapping("/estoque/entrada")
    public ResponseEntity<?> entradaLote(@RequestBody LogisticaService.EntradaLoteRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(logisticaService.entradaLote(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/distribuicoes/sugestao")
    public ResponseEntity<?> sugestaoFefo(@RequestBody LogisticaService.SolicitacaoFefoRequest request) {
        try {
            return ResponseEntity.ok(logisticaService.sugerirFefo(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/distribuicoes")
    public ResponseEntity<?> criarDistribuicao(@RequestBody LogisticaService.CriarDistribuicaoRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(logisticaService.criarDistribuicao(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/distribuicoes")
    public List<LogisticaService.DistribuicaoResponse> listarDistribuicoes() {
        return logisticaService.listarDistribuicoes();
    }

    @PostMapping("/distribuicoes/{id}/expedir")
    public ResponseEntity<?> expedirDistribuicao(@PathVariable String id) {
        return logisticaService.expedirDistribuicao(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.badRequest().body(new ErrorResponse("Distribuição não encontrada ou em status inválido")));
    }

    @PostMapping("/distribuicoes/{id}/receber")
    public ResponseEntity<?> receberDistribuicao(@PathVariable String id) {
        return logisticaService.receberDistribuicao(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.badRequest().body(new ErrorResponse("Distribuição não encontrada ou em status inválido")));
    }

    @PostMapping("/transferencias")
    public ResponseEntity<?> criarTransferencia(@RequestBody LogisticaService.CriarTransferenciaRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(logisticaService.criarTransferencia(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/transferencias")
    public List<LogisticaService.TransferenciaResponse> listarTransferencias() {
        return logisticaService.listarTransferencias();
    }

    @PostMapping("/transferencias/{id}/enviar")
    public ResponseEntity<?> enviarTransferencia(@PathVariable String id) {
        return logisticaService.enviarTransferencia(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.badRequest().body(new ErrorResponse("Transferência não encontrada ou em status inválido")));
    }

    @PostMapping("/transferencias/{id}/receber")
    public ResponseEntity<?> receberTransferencia(
        @PathVariable String id,
        @RequestBody(required = false) LogisticaService.ReceberTransferenciaRequest request
    ) {
        LogisticaService.ReceberTransferenciaRequest body = request != null
            ? request
            : new LogisticaService.ReceberTransferenciaRequest(null);

        return logisticaService.receberTransferencia(id, body)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.badRequest().body(new ErrorResponse("Transferência não encontrada ou em status inválido")));
    }

    private record ErrorResponse(String message) {
    }
}
