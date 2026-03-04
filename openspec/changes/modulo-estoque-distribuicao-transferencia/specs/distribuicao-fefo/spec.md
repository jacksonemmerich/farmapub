## ADDED Requirements

### Requirement: Priorização FEFO na separação de pedidos
O sistema DEVE sugerir e aplicar separação de itens por FEFO (primeiro vence, primeiro sai), priorizando lotes com menor data de vencimento válida para abastecimento de UBS e UPA.

#### Scenario: Sugestão de separação para pedido de reposição
- **WHEN** um pedido de distribuição é criado com item e quantidade
- **THEN** o sistema retorna uma sugestão de lotes ordenada por vencimento crescente e quantidade disponível

#### Scenario: Empate de validade entre lotes
- **WHEN** dois ou mais lotes possuem a mesma data de vencimento
- **THEN** o sistema desempata por ordem de entrada mais antiga no estoque

### Requirement: Expedição e confirmação de distribuição
O sistema DEVE controlar o ciclo de distribuição com estados de separação, expedição e confirmação de recebimento no destino, garantindo rastreabilidade por lote.

#### Scenario: Expedição concluída
- **WHEN** o operador confirma a expedição de uma distribuição
- **THEN** o sistema baixa o estoque do local de origem e marca a distribuição como expedida

#### Scenario: Recebimento confirmado pela unidade destino
- **WHEN** a unidade de destino confirma o recebimento da distribuição
- **THEN** o sistema incrementa o estoque no destino e finaliza a distribuição