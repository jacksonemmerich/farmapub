## ADDED Requirements

### Requirement: Controle de saldo por lote e local
O sistema DEVE manter saldo de medicamentos por lote e por local (estoque central, UBS e UPA), registrando toda movimentação com usuário, data/hora, tipo de operação e referência operacional.

#### Scenario: Entrada de lote no estoque central
- **WHEN** um operador registra uma entrada de medicamento com lote, validade, quantidade e local de destino
- **THEN** o sistema cria o lote, atualiza o saldo do local e grava a movimentação auditável

#### Scenario: Baixa de lote por operação logística
- **WHEN** uma operação de distribuição ou transferência consome quantidade de um lote
- **THEN** o sistema reduz o saldo do lote no local de origem e registra a baixa com rastreabilidade completa

### Requirement: Classificação de criticidade por validade
O sistema DEVE classificar cada lote por criticidade de validade para suporte à decisão logística, exibindo status operacional de risco para vencimento próximo.

#### Scenario: Lote com validade em faixa crítica
- **WHEN** a data de vencimento de um lote entra na faixa crítica configurada
- **THEN** o sistema marca o lote como crítico e o inclui nos alertas do dashboard

#### Scenario: Lote vencido
- **WHEN** a data de vencimento do lote é anterior à data atual
- **THEN** o sistema marca o lote como vencido e bloqueia seu uso em operações de saída