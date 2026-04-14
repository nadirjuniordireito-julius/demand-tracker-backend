# API - Evolucao Trimestral de Produto

## Objetivo

Disponibilizar para o frontend os dados de evolucao do produto por trimestre para construcao de grafico com duas series:

- `totalPrevisto`
- `totalExecutado`

A janela da analise trimestral usa:

- `dataInicio` do `MetaProduto` como mes inicial
- `dataFim` do `MetaProduto` como mes final

## Endpoint

- **GET** `/api/meta-produtos/{id}/evolucao-trimestral`

### Path params

- `id` (Long): ID do `MetaProduto`

### Query params

- Nenhum

## Regras de negocio

- Se `dataInicio` ou `dataFim` do produto estiver nulo, retorna erro 400.
- Se `dataFim < dataInicio`, retorna erro 400.
- Cada item da lista `trimestres` representa um periodo de ate 3 meses dentro da janela do produto.
- `totalPrevisto` do trimestre: soma dos custos (`qtdeHora * valorHora`) de todos os `TermoPlanejamento` das DTs relacionadas ao produto, considerando `dataAbertura` dentro do intervalo do trimestre.
- `totalExecutado` do trimestre: soma dos custos (`qtdeHora * valorHora`) de todos os `TermoEncerramento` das DTs relacionadas ao produto, considerando `dataTermo` dentro do intervalo do trimestre.

## Exemplo de resposta (200)

```json
{
  "metaProdutoId": 12,
  "codigoProduto": "P001",
  "nomeProduto": "Modulo de Atendimento",
  "dataInicioAnalise": "2026-01-01",
  "dataFimAnalise": "2026-12-31",
  "totalPrevistoProduto": 120000.00,
  "totalExecutadoProduto": 45600.00,
  "trimestres": [
    {
      "trimestreSequencia": 1,
      "dataInicio": "2026-01-01",
      "dataFim": "2026-03-31",
      "totalPrevisto": 30000.00,
      "totalExecutado": 10000.00
    },
    {
      "trimestreSequencia": 2,
      "dataInicio": "2026-04-01",
      "dataFim": "2026-06-30",
      "totalPrevisto": 30000.00,
      "totalExecutado": 15600.00
    },
    {
      "trimestreSequencia": 3,
      "dataInicio": "2026-07-01",
      "dataFim": "2026-09-30",
      "totalPrevisto": 30000.00,
      "totalExecutado": 12000.00
    },
    {
      "trimestreSequencia": 4,
      "dataInicio": "2026-10-01",
      "dataFim": "2026-12-31",
      "totalPrevisto": 30000.00,
      "totalExecutado": 8000.00
    }
  ]
}
```

## Estrutura de retorno

- `metaProdutoId`: ID do produto
- `codigoProduto`: codigo do produto
- `nomeProduto`: nome do produto
- `dataInicioAnalise`: data inicial da janela de analise (`MetaProduto.dataInicio`)
- `dataFimAnalise`: data final da janela de analise (`MetaProduto.dataFim`)
- `totalPrevistoProduto`: soma dos custos planejados (TermoPlanejamento) de todas as DTs do produto
- `totalExecutadoProduto`: soma dos custos executados (TermoEncerramento) de todas as DTs do produto
- `trimestres[]`:
  - `trimestreSequencia`
  - `dataInicio`
  - `dataFim`
  - `totalPrevisto`
  - `totalExecutado`

