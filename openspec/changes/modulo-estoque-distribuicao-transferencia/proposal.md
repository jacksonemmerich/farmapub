## Why

A prefeitura precisa de um módulo operacional confiável para controlar medicamentos do estoque central e unidades, reduzindo perdas por vencimento, falhas de abastecimento e inconsistências de movimentação. Este recorte é prioritário agora porque estabelece a base transacional e de rastreabilidade necessária antes da integração com a API do Banafar em fase posterior.

## What Changes

- Implementar o módulo de estoque com controle por lote, validade, localização e movimentações auditáveis.
- Implementar distribuição do estoque central para UBS/UPA com priorização FEFO (primeiro vence, primeiro sai).
- Implementar transferência entre unidades com fluxo de envio e recebimento (duas etapas) e tratamento de divergências.
- Disponibilizar dashboard operacional com níveis de estoque, alertas de criticidade e pendências logísticas.
- Adotar frontend padrão em Next.js + Tailwind + shadcn/ui para as telas desta fase.

## Non-goals

- Integração com API Banafar (catálogo, sincronização ou mensageria) nesta fase.
- Dispensação ao cidadão, validação de receita e fluxos clínicos de atendimento.
- Faturamento, compras/licitação e módulos financeiros.
- Algoritmos preditivos avançados de demanda além de regras operacionais base.

## Capabilities

### New Capabilities
- `estoque-lotes`: gestão de saldo por lote e local, criticidade por validade, movimentações e auditoria.
- `distribuicao-fefo`: fluxo de pedidos e separação com priorização FEFO para abastecimento de UBS/UPA.
- `transferencia-entre-unidades`: transferência logística entre locais com controle de envio, recebimento e divergência.

### Modified Capabilities
- Nenhuma.

## Impact

- Frontend: novas telas e componentes para dashboard, listagens e fluxos operacionais em Next.js App Router.
- Backend: novos endpoints REST e regras de domínio para estoque, distribuição, transferência e auditoria.
- Dados: novas entidades relacionando medicamento, lote, local, movimentação e documentos de transferência/distribuição.
- Operação: melhora de rastreabilidade e redução de risco de ruptura/vencimento no abastecimento municipal.
