# API Profissional – Documentação para Frontend

Documento para implementação do **CRUD de Profissional** no frontend, incluindo regras de negócio e contrato da API.

---

## 1. Visão geral

**Profissional** representa pessoas físicas (CPF) ou jurídicas (CNPJ) vinculadas a um **Projeto**, com valor/hora e data de início da atividade. Usado na operação do projeto (ex.: custos do termo de encerramento).

| Campo            | Descrição                                      |
|------------------|-------------------------------------------------|
| **tipoPessoa**   | `F` = Pessoa Física, `J` = Pessoa Jurídica      |
| **documento**    | CPF (F) ou CNPJ (J) – **único** no sistema      |
| **nome**         | Nome do profissional (até 500 caracteres)       |
| **valorHora**    | Valor cobrado por hora (decimal)                |
| **dataInicioAtividade** | Data de início da atividade (ISO date)   |
| **funcao**       | Função/cargo do profissional (opcional, máx. 255 caracteres) |
| **projetoId**    | ID do projeto ao qual o profissional pertence   |

---

## 2. Base URL e autenticação

- **Base URL:** `{API_BASE}/api/profissionais`
- **Autenticação:** Todas as rotas exigem token JWT no header (ex.: `Authorization: Bearer <token>`).

---

## 3. Endpoints

### 3.1 Listar profissionais (com filtros e paginação)

**GET** `/api/profissionais`

**Query params (todos opcionais):**

| Parâmetro   | Tipo   | Obrigatório | Descrição                                      |
|------------|--------|-------------|------------------------------------------------|
| `nome`     | string | Não         | Filtra por nome (case insensitive, contém)    |
| `projetoId`| long   | Não         | Filtra por ID do projeto                       |
| `page`     | int    | Não         | Número da página (0-based). Default: 0        |
| `size`     | int    | Não         | Tamanho da página. Default: definido pelo backend |
| `sort`     | string | Não         | Ordenação. Ex.: `nome,asc` ou `dataInicioAtividade,desc` |

**Exemplos de requisição:**
```
GET /api/profissionais
GET /api/profissionais?nome=Maria&projetoId=1
GET /api/profissionais?page=0&size=10&sort=nome,asc
```

**Resposta:** `200 OK`  
Corpo: objeto de **paginação** (Spring Page):

```json
{
  "content": [
    {
      "id": 1,
      "nome": "Maria Silva",
      "tipoPessoa": "F",
      "documento": "12345678901",
      "valorHora": 150.00,
      "dataInicioAtividade": "2025-01-15",
      "funcao": "Desenvolvedor",
      "projetoId": 1,
      "projeto": {
        "id": 1,
        "nome": "Projeto X",
        "codTed": "TED001",
        "termoInicial": "2025-01-01",
        "termoFinal": "2025-12-31",
        "dataEfetivaInicio": "2025-01-01",
        "dataUpdate": "2025-02-01T10:00:00",
        "usuarioId": 1,
        "usuario": { ... }
      }
    }
  ],
  "pageable": { ... },
  "totalPages": 5,
  "totalElements": 42,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false,
  "numberOfElements": 10,
  "empty": false
}
```

Use `content` para a lista de itens; `totalElements`, `totalPages`, `number`, `size` para montar paginação na UI.

---

### 3.2 Buscar por ID

**GET** `/api/profissionais/{id}`

**Resposta:** `200 OK`  
Corpo: um **ProfissionalDTO** (mesmo formato de um item em `content` acima).

**Erros:**
- `404 Not Found` – Profissional não encontrado.

---

### 3.3 Criar profissional

**POST** `/api/profissionais`  
**Content-Type:** `application/json`

**Corpo (ProfissionalCreateDTO):**

| Campo                 | Tipo    | Obrigatório | Regras / Observações                    |
|-----------------------|---------|-------------|-----------------------------------------|
| `nome`                | string  | Sim         | Não vazio; máx. 500 caracteres          |
| `tipoPessoa`          | string  | Sim         | Exatamente 1 caractere: `F` ou `J`      |
| `documento`           | string  | Sim         | Não vazio; máx. 100 caracteres; **único** no sistema |
| `valorHora`           | number  | Sim         | Decimal, > 0 (valor por hora)         |
| `dataInicioAtividade` | string  | Sim         | Data no formato ISO: `YYYY-MM-DD`      |
| `funcao`              | string  | Não         | Função/cargo; máx. 255 caracteres     |
| `projetoId`           | long    | Sim         | ID de um projeto existente             |

**Exemplo de corpo:**
```json
{
  "nome": "Maria Silva",
  "tipoPessoa": "F",
  "documento": "12345678901",
  "valorHora": 150.50,
  "dataInicioAtividade": "2025-01-15",
  "funcao": "Desenvolvedor",
  "projetoId": 1
}
```

**Resposta:** `201 Created`  
Corpo: **ProfissionalDTO** do profissional criado (incluindo `id` e `projeto`).

**Erros:**
- `400 Bad Request` – Validação (campos obrigatórios, tamanhos, formato) ou **documento já cadastrado**.
- `404 Not Found` – Projeto não encontrado.

---

### 3.4 Atualizar profissional

**PUT** `/api/profissionais/{id}`  
**Content-Type:** `application/json`

**Corpo (ProfissionalUpdateDTO):** todos os campos **opcionais**. Envie apenas os que devem ser alterados.

| Campo                 | Tipo   | Obrigatório | Regras / Observações                         |
|-----------------------|--------|-------------|----------------------------------------------|
| `nome`                | string | Não         | Máx. 500 caracteres                         |
| `tipoPessoa`          | string | Não         | 1 caractere: `F` ou `J`                     |
| `documento`           | string | Não         | Máx. 100 caracteres; **único** (exceto o próprio registro) |
| `valorHora`           | number | Não         | Decimal, > 0                                |
| `dataInicioAtividade` | string | Não         | ISO date: `YYYY-MM-DD`                      |
| `funcao`              | string | Não         | Função/cargo; máx. 255 caracteres           |
| `projetoId`           | long   | Não         | ID de projeto existente                     |

**Exemplo de corpo (atualização parcial):**
```json
{
  "nome": "Maria Silva Santos",
  "valorHora": 160.00,
  "funcao": "Analista Sênior"
}
```

**Resposta:** `200 OK`  
Corpo: **ProfissionalDTO** atualizado.

**Erros:**
- `400 Bad Request` – Validação ou **documento já usado por outro profissional**.
- `404 Not Found` – Profissional ou projeto (se informado) não encontrado.

---

### 3.5 Excluir profissional

**DELETE** `/api/profissionais/{id}`

**Resposta:** `204 No Content` (corpo vazio).

**Erros:**
- `404 Not Found` – Profissional não encontrado.
- Se o profissional estiver vinculado a registros de custo (ex.: termo de encerramento), o backend pode retornar erro de constraint (ex.: 400 ou 500). O frontend pode exibir mensagem orientando a remover vínculos antes de excluir.

---

## 4. Regras de negócio (resumo para o frontend)

1. **Documento único**  
   CPF/CNPJ não pode repetir no sistema.  
   - Na **criação**: não permitir enviar um documento já cadastrado.  
   - Na **edição**: não permitir alterar para um documento que já pertença a **outro** profissional (manter o mesmo documento do próprio registro é permitido).

2. **Projeto obrigatório**  
   Todo profissional pertence a um projeto. `projetoId` deve existir e o projeto deve estar cadastrado.

3. **Tipo de pessoa**  
   - `F` = Pessoa Física (CPF).  
   - `J` = Pessoa Jurídica (CNPJ).  
   O frontend pode validar formato/máscara de CPF/CNPJ conforme `tipoPessoa`.

4. **Valor hora**  
   Sempre numérico, maior que zero (duas casas decimais são suficientes).

5. **Datas**  
   Sempre em formato ISO: `YYYY-MM-DD` (ex.: `2025-01-15`).

---

## 5. Validações e mensagens de erro do backend

O backend retorna **400** com corpo no formato padrão de erro quando há falha de validação ou regra de negócio.

**Formato padrão de erro (ErrorResponse):**
```json
{
  "message": "Descrição do erro ou 'Erro de validação'",
  "status": 400,
  "timestamp": "2025-03-01T14:30:00",
  "errors": {
    "campo1": "Mensagem para o campo 1",
    "campo2": "Mensagem para o campo 2"
  }
}
```

- Em erros de **validação** (Bean Validation), `message` vem como `"Erro de validação"` e `errors` contém um mapa **campo → mensagem**.
- Em regras de negócio (ex.: documento duplicado, projeto inexistente), `message` traz a descrição e `errors` pode ser null.

**Mensagens comuns retornadas pelo backend:**

| Situação                    | Exemplo de `message` / uso de `errors`        |
|----------------------------|-----------------------------------------------|
| Campo obrigatório          | `errors.nome`: "Nome é obrigatório"           |
| Tamanho máximo             | `errors.nome`: "Nome deve ter no máximo 500 caracteres" |
| Documento duplicado       | `message`: "Já existe um profissional cadastrado com o documento informado: ..." |
| Profissional não encontrado| `message`: "Profissional não encontrado com ID: ..." (404) |
| Projeto não encontrado     | `message`: "Projeto não encontrado com ID: ..." (404) |

O frontend deve exibir `message` e, quando existir, o mapa `errors` (por campo) para melhor UX.

---

## 6. DTOs – Referência rápida

### 6.1 Resposta (ProfissionalDTO)

Usado em: GET por id, POST (retorno), PUT (retorno) e em cada item de `content` no GET paginado.

```ts
// Tipo sugerido (TypeScript)
interface ProfissionalDTO {
  id: number;
  nome: string;
  tipoPessoa: string;           // "F" | "J"
  documento: string;
  valorHora: number;
  dataInicioAtividade: string; // "YYYY-MM-DD"
  funcao?: string | null;      // função/cargo (opcional)
  projetoId: number;
  projeto?: ProjetoDTO;         // pode vir preenchido
}
```

### 6.2 Criação (ProfissionalCreateDTO)

```ts
interface ProfissionalCreateDTO {
  nome: string;
  tipoPessoa: string;           // "F" | "J"
  documento: string;
  valorHora: number;
  dataInicioAtividade: string; // "YYYY-MM-DD"
  funcao?: string | null;      // opcional; máx. 255 caracteres
  projetoId: number;
}
```

### 6.3 Atualização (ProfissionalUpdateDTO)

Todos os campos opcionais.

```ts
interface ProfissionalUpdateDTO {
  nome?: string;
  tipoPessoa?: string;
  documento?: string;
  valorHora?: number;
  dataInicioAtividade?: string;
  funcao?: string | null;      // máx. 255 caracteres; enviar "" para limpar
  projetoId?: number;
}
```

---

## 7. Sugestões de UX no frontend

1. **Listagem**  
   Usar `page`, `size` e `sort` na chamada GET; exibir tabela/cards com `content` e controles de paginação com `totalElements`/`totalPages`.

2. **Filtros**  
   Oferecer filtro por nome e por projeto (dropdown ou busca por projeto); enviar `nome` e `projetoId` na query do GET.

3. **Formulário de criação/edição**  
   - Validar obrigatoriedade e tamanhos antes de enviar (alinhado às regras acima).  
   - Para `tipoPessoa`, usar `F`/`J` e aplicar máscara de CPF ou CNPJ no campo `documento`.  
   - Em criação: checar (se possível) se o documento já existe (ex.: chamada extra ou mensagem do 400).  
   - Em edição: ao alterar documento, tratar 400 de “documento já cadastrado” e exibir mensagem clara.

4. **Exclusão**  
   Confirmar antes de chamar DELETE. Se o backend retornar erro por vínculo (ex.: custos), exibir mensagem orientando a remover ou ajustar os vínculos primeiro.

5. **Datas**  
   Enviar e exibir sempre em `YYYY-MM-DD` para evitar problemas de timezone e formato.

---

## 8. Resumo dos códigos HTTP

| Código | Uso                                              |
|--------|--------------------------------------------------|
| 200    | GET (lista ou por id), PUT – sucesso             |
| 201    | POST – profissional criado                       |
| 204    | DELETE – profissional excluído                  |
| 400    | Validação ou regra de negócio (documento único, etc.) |
| 401    | Não autenticado (token inválido ou ausente)     |
| 404    | Recurso não encontrado (profissional ou projeto) |
| 500    | Erro interno do servidor                        |

---

Documento alinhado ao backend (entidade Profissional, ProfissionalController, DTOs e regras de negócio). Em caso de dúvida sobre algum endpoint ou campo, validar com o time de backend ou com os arquivos em `controller`, `service` e `dto` do módulo de Profissional.
