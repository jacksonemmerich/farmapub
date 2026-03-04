## Context

O projeto Sisfarma precisa iniciar pelo núcleo logístico (estoque, distribuição e transferência) para suportar operação municipal com rastreabilidade por lote e menor risco de ruptura/vencimento. O frontend padrão do projeto será Next.js App Router com Tailwind e shadcn/ui. O backend seguirá Spring Boot com persistência em PostgreSQL e cache Redis quando útil para consultas agregadas. A integração com Banafar será uma fase posterior, portanto este desenho prioriza contratos internos estáveis e extensão futura por adaptador.

## Goals / Non-Goals

**Goals:**
- Entregar domínio transacional de estoque por lote/local com auditoria.
- Implementar distribuição com priorização FEFO.
- Implementar transferência entre unidades em duas etapas com divergência.
- Expor APIs REST claras para frontend e futura camada de integração externa.
- Disponibilizar dashboard operacional para criticidade e pendências.

**Non-Goals:**
- Conector Banafar nesta fase.
- Dispensação ao cidadão e validação de receitas.
- Módulos financeiros e compras.

## Decisions

1. **Separação por bounded contexts logísticos no backend**
   - Contextos: `estoque`, `distribuicao`, `transferencia`, `auditoria`.
   - Rationale: reduz acoplamento e permite evolução independente.
   - Alternativa considerada: módulo único monolítico por CRUD; rejeitada por dificultar regras transacionais e manutenção.

2. **Modelo de dados orientado a lote e localização**
   - Entidades principais: medicamento, lote, local_estoque, saldo_lote_local, movimentacao_estoque, distribuicao, distribuicao_item_lote, transferencia, transferencia_item_lote.
   - Rationale: FEFO e rastreabilidade dependem de granularidade por lote/local.
   - Alternativa considerada: saldo agregado por item; rejeitada por perda de rastreabilidade e controle de validade.

3. **FEFO como serviço de domínio centralizado**
   - Regra FEFO implementada em serviço reutilizável para distribuição e transferências de saída.
   - Desempate por data de entrada mais antiga.
   - Rationale: evita duplicação de regra em múltiplos fluxos.

4. **Fluxos de distribuição/transferência com máquina de estados simples**
   - Distribuição: `rascunho -> separada -> expedida -> recebida`.
   - Transferência: `rascunho -> enviada -> recebida` (+ estado `com_divergencia`).
   - Rationale: rastreabilidade operacional e reconciliação clara.

5. **Frontend em shell único com navegação por módulos**
   - Layout: sidebar + topbar; componentes shadcn para tabelas, formulários, badges e toasts.
   - Rationale: produtividade e consistência visual sem dependência de bibliotecas adicionais pesadas.

## Risks / Trade-offs

- **[Risco] Complexidade transacional em baixa/entrada concorrente por lote** → **Mitigação:** transações atômicas, lock otimista/pessimista e testes de concorrência.
- **[Risco] Divergências frequentes em recebimento gerarem backlog operacional** → **Mitigação:** fila de pendências com SLA e trilha de decisão obrigatória.
- **[Risco] Critérios de criticidade inadequados à operação local** → **Mitigação:** parametrizar janelas de alerta por administração.
- **[Trade-off] Mais tabelas e relacionamentos para garantir rastreabilidade** → ganho em governança e conformidade, com custo de maior complexidade inicial.

## Migration Plan

1. Criar schema inicial e migrations das entidades de estoque/lote/local/movimentação.
2. Implementar APIs de estoque e dashboard sem FEFO (baseline funcional).
3. Implementar serviço FEFO e APIs de distribuição.
4. Implementar fluxo de transferência com divergência.
5. Ativar auditoria completa de operações e perfis.
6. Publicar incrementalmente por feature flags de módulo.

**Rollback:**
- Reverter por versão de aplicação e migrations reversíveis.
- Em falhas de fluxo, bloquear novas operações e manter consultas somente leitura até recuperação.

## Open Questions

- Quais perfis e alçadas serão obrigatórios para aprovar divergência?
- Quais janelas exatas de criticidade (dias) por tipo de medicamento?
- Haverá regras diferentes de distribuição para UBS vs UPA já nesta fase?
- Quais campos devem ficar mandatórios para futura compatibilidade com Banafar?