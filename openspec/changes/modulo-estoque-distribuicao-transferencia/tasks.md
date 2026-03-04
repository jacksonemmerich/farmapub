## 1. Modelagem e base de domínio

- [ ] 1.1 Criar entidades e migrations de medicamento, lote, local_estoque e saldo_lote_local no backend
- [ ] 1.2 Criar entidades e migrations de movimentacao_estoque com trilha de auditoria
- [ ] 1.3 Implementar seed mínimo para locais padrão (estoque central, UBS, UPA)

## 2. APIs de estoque e criticidade

- [ ] 2.1 Implementar endpoint de entrada de lote e atualização de saldo por local
- [ ] 2.2 Implementar endpoint de consulta de saldos por item/lote/local com filtros
- [ ] 2.3 Implementar cálculo de criticidade por validade e endpoint de alertas

## 3. FEFO e distribuição

- [ ] 3.1 Implementar serviço de domínio FEFO com desempate por data de entrada
- [ ] 3.2 Implementar criação de pedido de distribuição e sugestão automática de separação FEFO
- [ ] 3.3 Implementar fluxo de estados da distribuição (rascunho, separada, expedida, recebida)
- [ ] 3.4 Implementar baixa na origem e entrada no destino no fechamento da distribuição

## 4. Transferência entre unidades

- [ ] 4.1 Implementar criação de transferência com itens por lote e etapa de envio
- [ ] 4.2 Implementar etapa de recebimento com atualização de saldo no destino
- [ ] 4.3 Implementar tratamento de divergência e pendência de conciliação

## 5. Frontend (Next.js + Tailwind + shadcn)

- [ ] 5.1 Criar shell de navegação (sidebar/topbar) para módulos de estoque, distribuição e transferência
- [ ] 5.2 Criar tela de dashboard operacional com cards de criticidade e pendências
- [ ] 5.3 Criar tela de estoque/lotes com listagem, filtros e detalhes por lote
- [ ] 5.4 Criar tela de distribuição com fluxo de pedido, sugestão FEFO e confirmação de expedição/recebimento
- [ ] 5.5 Criar tela de transferência com envio, recebimento e registro de divergência

## 6. Qualidade e validação

- [ ] 6.1 Criar testes backend para regras de FEFO, bloqueio de vencido e transições de estado
- [ ] 6.2 Criar testes frontend para fluxos críticos de estoque/distribuição/transferência
- [ ] 6.3 Executar validação final de backend e frontend (build, lint e testes) e registrar evidências