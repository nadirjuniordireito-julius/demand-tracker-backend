# API Profissional Custo Mensal - Documentacao para Frontend

Documento para implementacao do CRUD de custo mensal por profissional no frontend.

---

## 1. Visao geral

A entidade **ProfissionalCustoMensal** registra o custo total de um profissional para um mes/ano especifico.

| Campo | Descricao |
|---|---|
| `id` | Identificador do registro |
| `profissionalId` | ID do profissional vinculado |
| `ano` | Ano de referencia do custo |
| `mes` | Mes de referencia do custo (`1..12`) |
| `custoTotal` | Custo total no periodo (decimal, 2 casas) |

---

## 2. Base URL e autenticacao

- Base URL: `{API_BASE}/api/profissionais-custos-mensais`
- Autenticacao: JWT no header `Authorization: Bearer <token>`

---

## 3. Endpoints

### 3.1 Listar (paginado)

**GET** `/api/profissionais-custos-mensais`

Query params opcionais:
- `profissionalId` (long)
- `ano` (int)
- `mes` (int)
- `page`, `size`, `sort` (paginacao Spring)

Observacao de filtro:
- Se vier `profissionalId + ano + mes`, aplica combinacao exata dos 3.
- Se vier so `profissionalId`, filtra por profissional.
- Se vier so `ano`, filtra por ano.
- Se vier so `mes`, filtra por mes.

Resposta `200 OK` (Spring `Page<ProfissionalCustoMensalDTO>`).

---

### 3.2 Buscar por ID

**GET** `/api/profissionais-custos-mensais/{id}`

- `200 OK`: retorna `ProfissionalCustoMensalDTO`
- `404 Not Found`: registro nao encontrado

---

### 3.3 Criar

**POST** `/api/profissionais-custos-mensais`

Body (`ProfissionalCustoMensalCreateDTO`):

```json
{
  "profissionalId": 12,
  "ano": 2026,
  "mes": 4,
  "custoTotal": 12500.75
}
```

Validacoes:
- `profissionalId`: obrigatorio (profissional precisa existir)
- `ano`: obrigatorio, entre `1900` e `3000`
- `mes`: obrigatorio, entre `1` e `12`
- `custoTotal`: obrigatorio, `>= 0.00`

Resposta:
- `201 Created` com `ProfissionalCustoMensalDTO`
- `404 Not Found` se `profissionalId` nao existir
- `400 Bad Request` em erro de validacao

---

### 3.4 Atualizar

**PUT** `/api/profissionais-custos-mensais/{id}`

Body (`ProfissionalCustoMensalUpdateDTO`) com todos os campos opcionais:

```json
{
  "mes": 5,
  "custoTotal": 13100.00
}
```

Validacoes (quando campo for enviado):
- `ano`: `1900..3000`
- `mes`: `1..12`
- `custoTotal`: `>= 0.00`
- `profissionalId`: se enviado, precisa existir

Resposta:
- `200 OK` com `ProfissionalCustoMensalDTO`
- `404 Not Found` para custo mensal ou profissional inexistente
- `400 Bad Request` em erro de validacao

---

### 3.5 Excluir

**DELETE** `/api/profissionais-custos-mensais/{id}`

Resposta:
- `204 No Content`
- `404 Not Found` se id nao existir

---

## 4. Contrato de DTO (frontend)

### 4.1 Resposta

```ts
interface ProfissionalCustoMensalDTO {
  id: number;
  profissionalId: number;
  profissional?: ProfissionalDTO;
  ano: number;
  mes: number; // 1..12
  custoTotal: number;
}
```

### 4.2 Criacao

```ts
interface ProfissionalCustoMensalCreateDTO {
  profissionalId: number;
  ano: number;   // 1900..3000
  mes: number;   // 1..12
  custoTotal: number; // >= 0
}
```

### 4.3 Atualizacao

```ts
interface ProfissionalCustoMensalUpdateDTO {
  profissionalId?: number;
  ano?: number;
  mes?: number;
  custoTotal?: number;
}
```

---

## 5. Erros (padrao backend)

Formato padrao:

```json
{
  "message": "Erro de validacao",
  "status": 400,
  "timestamp": "2026-04-27T09:30:00",
  "errors": {
    "mes": "Mes deve ser entre 1 e 12"
  }
}
```

Casos comuns:
- `400`: validacao de campos
- `404`: profissional nao encontrado ou custo mensal nao encontrado

---

## 6. Resumo de status HTTP

| Codigo | Uso |
|---|---|
| `200` | GET/PUT com sucesso |
| `201` | POST criado |
| `204` | DELETE com sucesso |
| `400` | erro de validacao |
| `404` | recurso nao encontrado |
| `401` | nao autenticado |

