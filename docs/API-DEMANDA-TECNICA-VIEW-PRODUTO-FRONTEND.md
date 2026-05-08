# API Demanda Técnica - View por Produto

Endpoint para listar uma visão simplificada das demandas técnicas vinculadas a um produto (`idProduto`), com totais previstos e executados por demanda.

## Endpoint

- **GET** `/api/demandas/produto/{idProduto}/view`

## Parâmetros

- **Path param obrigatório**
  - `idProduto` (Long): ID do produto (`MetaProduto`).

## Regra de negócio

- Se `idProduto` não for informado: retorna `400 Bad Request`.
- Se o produto não existir: retorna `404 Not Found`.
- Se o produto existir e não tiver demandas: retorna `200 OK` com lista vazia.

## Campos retornados por item

- `codigo`: código da demanda técnica.
- `dataInicioExecucao`: data de início da execução (`TermoPlanejamento.dataInicioExecucao`).
- `nome`: nome da demanda.
- `status`: status da demanda.
- `totalPrevisto`: somatório de `TermoPlanejamentoCusto` da demanda.
- `totalExecutado`: somatório de `TermoEncerramentoCusto` da demanda.

## Exemplo de resposta (`200 OK`)

```json
[
  {
    "codigo": "DT-M001-003-2026",
    "dataInicioExecucao": "2026-04-10",
    "nome": "Integração com serviço externo",
    "status": "F",
    "totalPrevisto": 12500.00,
    "totalExecutado": 9800.00
  },
  {
    "codigo": "DT-M001-004-2026",
    "dataInicioExecucao": null,
    "nome": "Ajuste de validações",
    "status": "B",
    "totalPrevisto": 0.00,
    "totalExecutado": 0.00
  }
]
```

## Observações para frontend

- `dataInicioExecucao` pode vir `null` quando a demanda ainda não possui termo de planejamento.
- `totalPrevisto` e `totalExecutado` sempre retornam número (zero quando não houver custos).
- Lista ordenada por `codigo` para manter estabilidade visual.
