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
  codigoMeta: string;
  nomeMeta: string;
  idProduto: number;
  codigoProduto: string;
  nomeProduto: string;
  situacao: string; // codigo de status do produto (ex.: "A", "C", etc.)
  inicioPrevisaoExecucao: string | null; // "YYYY-MM-DD"
  inicioRealExecucao: string | null;     // "YYYY-MM-DD"
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

2. `inicioRealExecucao`  
   = primeira `dataAbertura` (apenas parte da data) encontrada em `TermoPlanejamento`
   das demandas do produto.  
   Se nao houver termo de planejamento para o produto, retorna `null`.

3. `fimPrevisaoExecucao`  
   = `MetaProduto.dataFim`

4. `mesesPrevistosExecucao`  
   = quantidade de meses entre `inicioPrevisaoExecucao` e `fimPrevisaoExecucao` (contagem inclusiva por mes).  
   Se datas nulas ou invalidas (`fim < inicio`), retorna `0`.

5. `valorTotalOrcamento`  
   = `MetaProduto.valorUnitario * MetaProduto.quantidade`

6. `valorTotalEmExecucao`  
   = soma de `(qtdeHora * valorHora)` de `TermoPlanejamentoCusto` das demandas do produto com status:
   - `E` (Em execucao)
   - `F` (Em encerramento)

7. `valorTotalExecutado`  
   = soma de `(qtdeHora * valorHora)` de `TermoEncerramentoCusto` das demandas do produto com status:
   - `G` (Encerrada)

8. `percentualExecucao`  
   = `MetaProduto.percExecutado` (formatado em decimal com 2 casas).  
   Exemplo: `35` -> `35.00`.

9. `valorMediaEntregaPrevistaMensal`  
   = `valorTotalOrcamento / mesesPrevistosExecucao`

10. `valorMediaEntregaRealMensal`  
   = `(valorTotalEmExecucao + valorTotalExecutado) / mesesExecucao`

11. `mesesExecucao` (regra interna de calculo, nao retornado no DTO)  
   = quantidade de meses entre o ano/mes da primeira `dataAbertura` de `TermoPlanejamento`
   e o ano/mes atual (contagem inclusiva).  
   Se nao houver termo de planejamento, considera `0`.

### Regra de divisao por zero

Quando o divisor for `<= 0`, a media correspondente retorna `0.00`.

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
    "codigoMeta": "M001",
    "nomeMeta": "Meta de Modernizacao",
    "idProduto": 35,
    "codigoProduto": "P001",
    "nomeProduto": "Painel Gerencial",
    "situacao": "A",
    "inicioPrevisaoExecucao": "2026-01-01",
    "inicioRealExecucao": "2026-02-15",
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
    "codigoMeta": "M001",
    "nomeMeta": "Meta de Modernizacao",
    "idProduto": 36,
    "codigoProduto": "P002",
    "nomeProduto": "Modulo Mobile",
    "situacao": "A",
    "inicioPrevisaoExecucao": "2026-02-01",
    "inicioRealExecucao": null,
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
- Novo campo: `inicioRealExecucao` pode vir `null` (quando nao houver termo de planejamento).

## 10. Ajuste necessario no Frontend (DTO local)

Se o frontend possui tipagem local para esse retorno, atualize para:

```ts
export interface ProdutoResumoDTO {
  idMeta: number;
  codigoMeta: string;
  nomeMeta: string;
  idProduto: number;
  codigoProduto: string;
  nomeProduto: string;
  situacao: string;
  inicioPrevisaoExecucao: string | null;
  inicioRealExecucao: string | null; // novo campo
  fimPrevisaoExecucao: string | null;
  mesesPrevistosExecucao: number;
  valorTotalOrcamento: number;
  valorTotalEmExecucao: number;
  valorTotalExecutado: number;
  percentualExecucao: number;
  valorMediaEntregaPrevistaMensal: number;
  valorMediaEntregaRealMensal: number;
}
```

---

Documento alinhado ao endpoint `GET /api/meta-produtos/resumo`, ao `ProdutoResumoDTO` e as regras implementadas no backend.
