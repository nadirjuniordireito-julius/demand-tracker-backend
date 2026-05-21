# API Dia Não Útil - Documentação para Frontend

Documento para implementação do CRUD de **dias não úteis** (feriados, recessos, pontos facultativos etc.) no frontend.

---

## 1. Visão geral

A entidade **DiaNaoUtil** registra datas em que não se considera dia útil para cálculos operacionais.

| Campo | Descrição |
|-------|-----------|
| `id` | Identificador do registro |
| `data` | Data do dia não útil (`yyyy-MM-dd`) |
| `descricao` | Descrição/motivo (ex.: "Natal", "Recesso forense") |

Regras:
- A **data é única** no sistema (não pode haver dois registros para a mesma data).
- Ordenação padrão da listagem: `data` ascendente.

---

## 2. Base URL e autenticação

- Base URL: `{API_BASE}/api/dias-nao-uteis`
- Autenticação: JWT no header `Authorization: Bearer <token>`

---

## 3. Endpoints

### 3.1 Listar (paginado)

**GET** `/api/dias-nao-uteis`

Query params opcionais:

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `dataInicio` | date (`yyyy-MM-dd`) | Filtra registros com `data >= dataInicio` |
| `dataFim` | date (`yyyy-MM-dd`) | Filtra registros com `data <= dataFim` |
| `page` | int | Página (default `0`) |
| `size` | int | Tamanho da página |
| `sort` | string | Ex.: `data,asc` |

Exemplos:

```
GET /api/dias-nao-uteis?dataInicio=2026-01-01&dataFim=2026-12-31&page=0&size=50&sort=data,asc
GET /api/dias-nao-uteis?page=0&size=20
```

Resposta `200 OK`: Spring `Page<DiaNaoUtilDTO>`.

Erros:
- `400 Bad Request`: `dataFim` anterior a `dataInicio`.

---

### 3.2 Buscar por ID

**GET** `/api/dias-nao-uteis/{id}`

- `200 OK`: retorna `DiaNaoUtilDTO`
- `404 Not Found`: registro não encontrado

---

### 3.3 Criar

**POST** `/api/dias-nao-uteis`

Body (`DiaNaoUtilCreateDTO`):

```json
{
  "data": "2026-12-25",
  "descricao": "Natal"
}
```

Validações:
- `data`: obrigatória
- `descricao`: obrigatória, não vazia, máximo 500 caracteres
- data não pode já existir

Resposta:
- `201 Created`: `DiaNaoUtilDTO`
- `400 Bad Request`: data duplicada ou validação de campos

Exemplo de erro:

```json
{
  "message": "Já existe um dia não útil cadastrado para a data: 2026-12-25",
  "status": 400,
  "timestamp": "2026-05-19T10:00:00"
}
```

---

### 3.4 Atualizar

**PUT** `/api/dias-nao-uteis/{id}`

Body (`DiaNaoUtilUpdateDTO`) — todos os campos opcionais:

```json
{
  "data": "2026-12-26",
  "descricao": "Ponto facultativo"
}
```

Validações:
- Se informar `data`, não pode colidir com outro registro
- Se informar `descricao`, não pode ser vazia (após trim)

Resposta:
- `200 OK`: `DiaNaoUtilDTO`
- `404 Not Found`: ID inexistente
- `400 Bad Request`: data duplicada ou descrição vazia

---

### 3.5 Excluir

**DELETE** `/api/dias-nao-uteis/{id}`

- `204 No Content`: excluído com sucesso
- `404 Not Found`: registro não encontrado

---

## 4. Contrato TypeScript sugerido

```ts
type DiaNaoUtilDTO = {
  id: number;
  data: string; // yyyy-MM-dd
  descricao: string;
};

type DiaNaoUtilCreateDTO = {
  data: string;
  descricao: string;
};

type DiaNaoUtilUpdateDTO = {
  data?: string;
  descricao?: string;
};
```

---

## 5. Checklist de implementação frontend

1. Tela de listagem com filtro por intervalo (`dataInicio` / `dataFim`) e paginação.
2. Modal/formulário create e edit com campos `data` (date picker) e `descricao`.
3. Tratar `400` de data duplicada com mensagem amigável ao usuário.
4. Confirmar antes de excluir (`DELETE`).
5. Após create/update/delete, recarregar a lista mantendo filtros atuais.
6. Ordenação recomendada: coluna `data` ascendente.

---

## 6. Prompt curto (copiar para agente de frontend)

```text
Implementar CRUD de Dias Não Úteis:

- Base: /api/dias-nao-uteis (JWT)
- Campos: data (yyyy-MM-dd), descricao (string, max 500)
- Listagem com filtros dataInicio e dataFim; ordenação padrão por data ASC
- POST/PUT: tratar 400 quando data já cadastrada
- DELETE: 204 sem body
- Tela sugerida: calendário/lista anual + modal create/edit
- Ver docs/API-DIA-NAO-UTIL-FRONTEND.md
```

---

## 7. Observação de integração futura

O cálculo de dias úteis em `DemandaExecucaoService` ainda usa feriados nacionais fixos em código. A tabela `dia_nao_util` **não é consumida automaticamente** por esse serviço nesta versão; integração futura pode substituir ou complementar a lista hardcoded.
