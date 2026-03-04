## ADDED Requirements

### Requirement: Transferência em duas etapas
O sistema DEVE executar transferências entre unidades em duas etapas (envio e recebimento), preservando a referência de lote original durante todo o fluxo.

#### Scenario: Envio de transferência
- **WHEN** o operador do local de origem confirma o envio de itens por lote
- **THEN** o sistema registra a transferência como enviada e reduz o saldo do local de origem

#### Scenario: Recebimento de transferência
- **WHEN** o operador do local de destino confirma o recebimento dos lotes enviados
- **THEN** o sistema registra a transferência como recebida e adiciona o saldo ao destino

### Requirement: Tratamento de divergência no recebimento
O sistema DEVE permitir registro de divergências de quantidade e condição no recebimento, mantendo pendência operacional até decisão de ajuste.

#### Scenario: Quantidade recebida menor que enviada
- **WHEN** o destino informa recebimento parcial de um lote
- **THEN** o sistema marca a transferência com divergência e gera pendência de ajuste logístico

#### Scenario: Conciliação de divergência aprovada
- **WHEN** um responsável aprova a conciliação da divergência com justificativa
- **THEN** o sistema aplica o ajuste de saldo correspondente e encerra a pendência