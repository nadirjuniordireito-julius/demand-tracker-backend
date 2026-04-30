# API Meta Produto - Resumo (Frontend)

Documentacao completa para consumo do endpoint de resumo financeiro e de execucao de produtos de uma meta.

---

## 1. Objetivo

Retornar, para um ou mais produtos, os principais indicadores de planejamento e execucao:

- Orcamento total do produto
- Valor em execucao (demandas em `E` e `F`)
- Valor executado (demandas em `G`)
- Percentual de execucao
- Medias mensais previstas e reais

---

## 2. Endpoint

- **GET** `/api/meta-produtos/resumo`

Base completa: `{API_BASE}/api/meta-produtos/resumo`

Autenticacao: JWT (padrao da API), via header `Authorization: Bearer <token>`.

---

## 3. Query params

| Parametro   | Tipo | Obrigatorio | Regra |
|-------------|------|-------------|-------|
| `idMeta`    | long | Nao*        | ID da meta (`ProjetoMeta.id`) |
| `idProduto` | long | Nao*        | ID do produto (`MetaProduto.id`) |

\* Pelo menos **um** dos dois deve ser enviado.

### Combinacoes validas

1. `idMeta` apenas  
   Retorna um `ProdutoResumoDTO` para **cada produto da meta**.

2. `idProduto` apenas  
   Retorna **apenas** o resumo do produto informado.

3. `idMeta` + `idProduto`  
   Retorna o resumo do produto informado, validando que ele pertence a meta.

### Erros de combinacao

- Se nao enviar nenhum parametro (`idMeta` e `idProduto` ausentes): `400 Bad Request`.
- Se enviar ambos, mas o produto nao pertencer a meta: `400 Bad Request`.

---

## 4. Contrato de resposta

O endpoint retorna sempre uma **lista**:

- `200 OK` + `[]` quando nao houver produtos para a meta informada.
- `200 OK` + `[ProdutoResumoDTO, ...]` nos demais casos.

### DTO (ProdutoResumoDTO)

```ts
interface ProdutoResumoDTO {
  idMeta: number;
  meta: string;
  idProduto: number;
  produto: string;
  situacao: string; // codigo de status do produto (ex.: "A", "C", etc.)
  inicioPrevisaoExecucao: string | null; // "YYYY-MM-DD"
  fimPrevisaoExecucao: string | null;    // "YYYY-MM-DD"
  mesesPrevistosExecucao: number;
  valorTotalOrcamento: number;              // decimal(18,2)
  valorTotalEmExecucao: number;             // decimal(18,2)
  valorTotalExecutado: number;              // decimal(18,2)
  percentualExecucao: number;               // decimal(18,2)
  valorMediaEntregaPrevistaMensal: number;  // decimal(18,2)
  valorMediaEntregaRealMensal: number;      // decimal(18,2)
}
```

---

## 5. Regras de calculo dos campos

1. `inicioPrevisaoExecucao`  
   = `MetaProduto.dataInicio`

2. `fimPrevisaoExecucao`  
   = `MetaProduto.dataFim`

3. `mesesPrevistosExecucao`  
   = quantidade de meses entre `inicioPrevisaoExecucao` e `fimPrevisaoExecucao` (contagem inclusiva por mes).  
   Se datas nulas ou invalidas (`fim < inicio`), retorna `0`.

4. `valorTotalOrcamento`  
   = `MetaProduto.valorUnitario * MetaProduto.quantidade`

5. `valorTotalEmExecucao`  
   = soma de `(qtdeHora * valorHora)` de `TermoPlanejamentoCusto` das demandas do produto com status:
   - `E` (Em execucao)
   - `F` (Em encerramento)

6. `valorTotalExecutado`  
   = soma de `(qtdeHora * valorHora)` de `TermoEncerramentoCusto` das demandas do produto com status:
   - `G` (Encerrada)

7. `percentualExecucao`  
   = `MetaProduto.percExecutado` (formatado em decimal com 2 casas).  
   Exemplo: `35` -> `35.00`.

8. `valorMediaEntregaPrevistaMensal`  
   = `valorTotalOrcamento / mesesPrevistosExecucao`

9. `valorMediaEntregaRealMensal`  
   = `(valorTotalEmExecucao + valorTotalExecutado) / mesesPrevistosExecucao`

### Regra de divisao por zero

Quando `mesesPrevistosExecucao <= 0`, as medias retornam `0.00`.

---

## 6. Exemplos de requisicao

### 6.1 Buscar todos os produtos de uma meta

```http
GET /api/meta-produtos/resumo?idMeta=10
```

### 6.2 Buscar um produto especifico

```http
GET /api/meta-produtos/resumo?idProduto=35
```

### 6.3 Buscar um produto validando a meta

```http
GET /api/meta-produtos/resumo?idMeta=10&idProduto=35
```

---

## 7. Exemplos de resposta

### 7.1 Sucesso (200) - multiplos produtos

```json
[
  {
    "idMeta": 10,
    "meta": "Meta de Modernizacao",
    "idProduto": 35,
    "produto": "Painel Gerencial",
    "situacao": "A",
    "inicioPrevisaoExecucao": "2026-01-01",
    "fimPrevisaoExecucao": "2026-12-31",
    "mesesPrevistosExecucao": 12,
    "valorTotalOrcamento": 120000.00,
    "valorTotalEmExecucao": 18000.00,
    "valorTotalExecutado": 24000.00,
    "percentualExecucao": 35.00,
    "valorMediaEntregaPrevistaMensal": 10000.00,
    "valorMediaEntregaRealMensal": 3500.00
  },
  {
    "idMeta": 10,
    "meta": "Meta de Modernizacao",
    "idProduto": 36,
    "produto": "Modulo Mobile",
    "situacao": "A",
    "inicioPrevisaoExecucao": "2026-02-01",
    "fimPrevisaoExecucao": "2026-08-31",
    "mesesPrevistosExecucao": 7,
    "valorTotalOrcamento": 70000.00,
    "valorTotalEmExecucao": 5000.00,
    "valorTotalExecutado": 10000.00,
    "percentualExecucao": 20.00,
    "valorMediaEntregaPrevistaMensal": 10000.00,
    "valorMediaEntregaRealMensal": 2142.86
  }
]
```

### 7.2 Sucesso (200) - sem produtos na meta

```json
[]
```

---

## 8. Erros esperados

### 400 - nenhum parametro informado

Exemplo:

```http
GET /api/meta-produtos/resumo
```

Mensagem esperada (resumo):
- "Informe ao menos um parametro: idMeta ou idProduto."

### 400 - produto nao pertence a meta informada

Exemplo:

```http
GET /api/meta-produtos/resumo?idMeta=10&idProduto=999
```

Mensagem esperada (resumo):
- "O idProduto informado nao pertence ao idMeta informado."

### 404 - produto inexistente

Exemplo:

```http
GET /api/meta-produtos/resumo?idProduto=999999
```

Mensagem esperada (resumo):
- "Produto da meta nao encontrado com ID: 999999"

---

## 9. Resumo rapido para implementacao no frontend

- Sempre esperar **array** na resposta.
- Para tela de meta, usar `idMeta`.
- Para detalhe de produto, usar `idProduto`.
- `situacao` e um codigo curto (string de 1 caractere).
- Campos monetarios ja retornam com 2 casas decimais.
- Datas no formato ISO (`YYYY-MM-DD`).

---

Documento alinhado ao endpoint `GET /api/meta-produtos/resumo`, ao `ProdutoResumoDTO` e as regras implementadas no backend.
