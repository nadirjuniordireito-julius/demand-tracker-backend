# API Desembolso – Documentação para Frontend

Documento para implementação do **CRUD de Desembolso** no frontend, incluindo regras de negócio e contrato da API.

---

## 1. Visão geral

**Desembolso** representa um lançamento financeiro com:

- Documento descritivo (opcional)
- Valor **previsto** e valor **efetivamente desembolsado**
- Data do desembolso
- Data **prevista** do desembolso

| Campo                     | Descrição                                                        |
|---------------------------|------------------------------------------------------------------|
| **documento**             | Texto livre (até 500 caracteres) para identificar o desembolso  |
| **valorPrevisto**         | Valor previsto do desembolso (decimal, 2 casas)                 |
| **valor**                 | Valor efetivo do desembolso (decimal, 2 casas)                  |
| **dataDesembolso**        | Data em que o desembolso ocorreu                                |
| **dataPrevistaDesembolso**| Data prevista para o desembolso                                 |
| **projetoId**             | ID do projeto ao qual o desembolso está vinculado              |

Cada desembolso pertence a um **Projeto** (relacionamento ManyToOne).

---

## 2. Base URL e autenticação

- **Base URL:** `{API_BASE}/api/desembolsos`
- **Autenticação:** Todas as rotas seguem o padrão do backend, utilizando JWT (ex.: `Authorization: Bearer <token>`).

---

## 3. Endpoints

### 3.1 Listar desembolsos de um projeto (com filtro opcional por documento)

**GET** `/api/desembolsos`

**Query params:**

| Parâmetro    | Tipo   | Obrigatório | Descrição                                            |
|--------------|--------|-------------|------------------------------------------------------|
| `projetoId`  | long   | **Sim**     | ID do projeto ao qual os desembolsos pertencem      |
| `documento`  | string | Não         | Filtro por trecho do documento (case insensitive)   |
| `page`       | int    | Não         | Número da página (0-based). Default: 0              |
| `size`       | int    | Não         | Tamanho da página. Default definido pelo backend    |
| `sort`       | string | Não         | Ordenação. Ex.: `dataDesembolso,desc`               |

**Exemplos de requisição:**

```http
GET /api/desembolsos?projetoId=1
GET /api/desembolsos?projetoId=1&documento=NF-2026
GET /api/desembolsos?projetoId=1&page=0&size=10&sort=dataDesembolso,desc
```

**Resposta:** `200 OK`  
Corpo: objeto de **paginação** (Spring Page) com `DesembolsoDTO` em `content`:

```json
{
  "content": [
    {
      "id": 1,
      "documento": "NF-2026-001",
      "valorPrevisto": 10000.0,
      "valor": 9500.0,
      "dataDesembolso": "2026-03-10",
      "dataPrevistaDesembolso": "2026-03-05",
      "projetoId": 1,
      "projeto": {
        "id": 1,
        "nome": "Projeto X",
        "codTed": "TED001"
      }
    }
  ],
  "pageable": { "...": "..." },
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### 3.2 Buscar desembolso por ID

**GET** `/api/desembolsos/{id}`

**Resposta:** `200 OK`  
Corpo: um **DesembolsoDTO**.

**Exemplo:**

```http
GET /api/desembolsos/1
```

```json
{
  "id": 1,
  "documento": "NF-2026-001",
  "valorPrevisto": 10000.0,
  "valor": 9500.0,
  "dataDesembolso": "2026-03-10",
  "dataPrevistaDesembolso": "2026-03-05",
  "projetoId": 1
}
```

**Erros:**

- `404 Not Found` – quando o ID não existir.

---

### 3.3 Criar desembolso

**POST** `/api/desembolsos`  
**Content-Type:** `application/json`

**Corpo (DesembolsoCreateDTO):**

| Campo                     | Tipo     | Obrigatório | Regras / Observações                               |
|---------------------------|----------|-------------|----------------------------------------------------|
| `documento`               | string   | Não         | Máx. 500 caracteres                                |
| `valorPrevisto`           | number   | Sim         | Decimal (2 casas); valor previsto do desembolso    |
| `valor`                   | number   | Sim         | Decimal (2 casas); valor efetivamente desembolsado |
| `dataDesembolso`          | string   | Sim         | Data real do desembolso – ISO `YYYY-MM-DD`         |
| `dataPrevistaDesembolso`  | string   | Sim         | Data prevista do desembolso – ISO `YYYY-MM-DD`     |
| `projetoId`               | number   | Sim         | ID de um projeto existente                         |

**Exemplo de corpo:**

```json
{
  "documento": "NF-2026-001",
  "valorPrevisto": 10000.0,
  "valor": 9500.0,
  "dataDesembolso": "2026-03-10",
  "dataPrevistaDesembolso": "2026-03-05",
  "projetoId": 1
}
```

**Resposta:** `201 Created`  
Corpo: **DesembolsoDTO** criado (inclui `id`).

**Erros:**

- `400 Bad Request` – campos obrigatórios ausentes, formato de data inválido, etc.

---

### 3.4 Atualizar desembolso

**PUT** `/api/desembolsos/{id}`  
**Content-Type:** `application/json`

**Corpo (DesembolsoUpdateDTO):** todos os campos são **opcionais**. Envie apenas o que precisar alterar.

| Campo                     | Tipo     | Obrigatório | Regras / Observações                               |
|---------------------------|----------|-------------|----------------------------------------------------|
| `documento`               | string   | Não         | Máx. 500 caracteres                                |
| `valorPrevisto`           | number   | Não         | Decimal (2 casas)                                  |
| `valor`                   | number   | Não         | Decimal (2 casas)                                  |
| `dataDesembolso`          | string   | Não         | ISO `YYYY-MM-DD`                                   |
| `dataPrevistaDesembolso`  | string   | Não         | ISO `YYYY-MM-DD`                                   |

**Exemplo (atualização parcial):**

```json
{
  "valor": 9800.00,
  "dataDesembolso": "2026-03-12"
}
```

**Resposta:** `200 OK`  
Corpo: **DesembolsoDTO** atualizado.

**Erros:**

- `400 Bad Request` – validação de campos.
- `404 Not Found` – desembolso não encontrado.

---

### 3.5 Excluir desembolso

**DELETE** `/api/desembolsos/{id}`

**Resposta:** `204 No Content` (sem corpo).

**Erros:**

- `404 Not Found` – desembolso não encontrado.
- `409 Conflict` ou `500 Internal Server Error` – se no futuro houver vínculos com outras entidades que impeçam a exclusão (atualmente não há).

---

## 4. Regras de negócio (resumo para o frontend)

1. **Documento opcional**  
   - Pode ser usado para registrar número de NF, comprovante, etc.  
   - Apenas limite de tamanho (500 caracteres).

2. **Valores obrigatórios**  
   - `valorPrevisto` e `valor` são sempre obrigatórios na criação.  
   - Podem ser ajustados via update.

3. **Datas obrigatórias**  
   - Tanto a data real (`dataDesembolso`) quanto a prevista (`dataPrevistaDesembolso`) são obrigatórias na criação.  
   - Devem ser enviadas em `YYYY-MM-DD`.

4. **Vínculo com Projeto**  
   - Todo desembolso deve estar associado a um projeto existente (`projetoId`).  
   - O frontend deve oferecer seleção de projeto (ex.: dropdown) e enviar o `projetoId` correspondente.

---

## 5. DTOs – Referência rápida

### 5.1 Resposta (DesembolsoDTO)

Usado em: GET por id, POST (retorno), PUT (retorno) e em cada item de `content` no GET paginado.

```ts
interface DesembolsoDTO {
  id: number;
  documento?: string | null;
  valorPrevisto: number;
  valor: number;
  dataDesembolso: string;         // "YYYY-MM-DD"
  dataPrevistaDesembolso: string; // "YYYY-MM-DD"
  projetoId: number;
  projeto?: ProjetoDTO;
}
```

### 5.2 Criação (DesembolsoCreateDTO)

```ts
interface DesembolsoCreateDTO {
  documento?: string | null;      // máx. 500 caracteres
  valorPrevisto: number;
  valor: number;
  dataDesembolso: string;         // "YYYY-MM-DD"
  dataPrevistaDesembolso: string; // "YYYY-MM-DD"
  projetoId: number;
}
```

### 5.3 Atualização (DesembolsoUpdateDTO)

Todos os campos opcionais:

```ts
interface DesembolsoUpdateDTO {
  documento?: string | null;
  valorPrevisto?: number;
  valor?: number;
  dataDesembolso?: string;
  dataPrevistaDesembolso?: string;
  projetoId?: number;
}
```

---

## 6. Resumo dos códigos HTTP

| Código | Uso                                           |
|--------|-----------------------------------------------|
| 200    | GET (lista ou por id), PUT – sucesso          |
| 201    | POST – desembolso criado                      |
| 204    | DELETE – desembolso excluído                  |
| 400    | Validação ou dados inválidos                  |
| 401    | Não autenticado (token inválido ou ausente)   |
| 404    | Recurso não encontrado (desembolso)           |
| 500    | Erro interno do servidor                      |

---

Documento alinhado à entidade `Desembolso`, `DesembolsoController`, DTOs e serviço no backend.  
Em caso de dúvida sobre algum endpoint ou campo, consulte os arquivos em `controller`, `service`, `repository`, `entity` e `dto` relacionados a `Desembolso`.

