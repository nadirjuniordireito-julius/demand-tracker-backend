# Termo de Encerramento – Custos e Profissionais em uma única chamada

A partir desta alteração, o **create** e o **update** do Termo de Encerramento passam a aceitar, em cada item de **custo**, uma lista opcional de **profissionais**. Assim, as tabelas `termos_encerramento_custos` e `termos_encerramento_custos_profissionais` são preenchidas na mesma requisição (insert/update).

---

## 1. Visão geral

- **Antes:** o frontend enviava apenas os custos (perfil + qtdeHora + valorHora). Os profissionais do custo eram gravados em chamadas separadas à API de `termos-encerramento-custos-profissionais`.
- **Agora:** cada custo pode vir com o array `profissionais`. Na mesma chamada de **POST** (criar termo) ou **PUT** (atualizar termo), o backend:
  - grava/atualiza os **custos** do termo (perfil, qtdeHora, valorHora);
  - grava/atualiza os **profissionais** de cada custo (profissionalId, qtdeHora, valorHora).

A resposta do termo (GET por id, retorno do POST e do PUT) passa a trazer em cada custo o array `profissionais` preenchido.

---

## 2. Endpoints que usam o novo formato

- **POST** `/api/termos-encerramento` — criar termo (com custos e profissionais em cada custo).
- **PUT** `/api/termos-encerramento/{id}` — atualizar termo (substitui custos e profissionais pelos enviados).

Não é obrigatório enviar `profissionais` em todo custo: o campo é opcional. Se não enviar ou enviar array vazio, aquele custo fica sem profissionais.

---

## 3. Estrutura do payload (Create / Update)

### 3.1 Corpo da requisição (POST e PUT)

O corpo usa o mesmo formato de antes para o termo; a mudança está **dentro de cada item de `custos`**.

Estrutura de **um item de custo**:

| Campo           | Tipo   | Obrigatório | Descrição |
|----------------|--------|-------------|-----------|
| `perfilId`     | number | Sim         | ID do perfil |
| `qtdeHora`     | number | Sim         | Quantidade de horas (> 0) |
| `valorHora`    | number | Sim         | Valor hora (> 0) |
| `profissionais`| array  | Não         | Lista de profissionais desta linha de custo (ver abaixo). |

Cada elemento de `profissionais` (item de profissional do custo):

| Campo            | Tipo   | Obrigatório | Descrição |
|-----------------|--------|-------------|-----------|
| `profissionalId`| number | Sim         | ID do profissional (cadastrado em `/api/profissionais`) |
| `qtdeHora`      | number | Sim         | Quantidade de horas (> 0) |
| `valorHora`     | number | Sim         | Valor hora (> 0) |

### 3.2 Exemplo – POST criar termo (com profissionais em um dos custos)

```json
{
  "demandaTecnicaId": 1,
  "dataTermo": "2025-03-01T10:00:00",
  "dataInicioExecucao": "2025-01-15",
  "dataFimExecucao": "2025-02-28",
  "resultadoEntregue": "Descrição do resultado entregue com no mínimo 10 caracteres.",
  "usuarioId": 1,
  "custos": [
    {
      "perfilId": 10,
      "qtdeHora": 40.00,
      "valorHora": 120.00,
      "profissionais": [
        {
          "profissionalId": 5,
          "qtdeHora": 20.00,
          "valorHora": 120.00
        },
        {
          "profissionalId": 7,
          "qtdeHora": 20.00,
          "valorHora": 115.00
        }
      ]
    },
    {
      "perfilId": 11,
      "qtdeHora": 10.00,
      "valorHora": 80.00,
      "profissionais": []
    }
  ]
}
```

- Primeiro custo: perfil 10, 40h a R$ 120/h, com 2 profissionais (ids 5 e 7), cada um com suas horas e valor/hora.
- Segundo custo: perfil 11, 10h a R$ 80/h, sem profissionais (`profissionais` vazio; também pode omitir o campo).

### 3.3 Exemplo – PUT atualizar termo

O **PUT** usa a mesma estrutura de `custos` (e de `profissionais` dentro de cada custo). O backend **substitui** toda a lista de custos do termo (e, por consequência, a lista de profissionais de cada custo) pelo que for enviado.

```json
{
  "dataTermo": "2025-03-01T14:00:00",
  "dataInicioExecucao": "2025-01-15",
  "dataFimExecucao": "2025-02-28",
  "resultadoEntregue": "Resultado atualizado.",
  "custos": [
    {
      "perfilId": 10,
      "qtdeHora": 45.00,
      "valorHora": 125.00,
      "profissionais": [
        {
          "profissionalId": 5,
          "qtdeHora": 25.00,
          "valorHora": 125.00
        },
        {
          "profissionalId": 8,
          "qtdeHora": 20.00,
          "valorHora": 125.00
        }
      ]
    }
  ]
}
```

---

## 4. Resposta (GET / POST / PUT)

Ao buscar o termo (**GET** `/api/termos-encerramento/{id}`) ou ao receber o retorno do **POST** ou **PUT**, cada item em `custos` passa a incluir o array `profissionais`:

```json
{
  "id": 1,
  "demandaTecnicaId": 1,
  "resultadoEntregue": "...",
  "dataTermo": "2025-03-01T10:00:00",
  "dataInicioExecucao": "2025-01-15",
  "dataFimExecucao": "2025-02-28",
  "usuarioId": 1,
  "custos": [
    {
      "id": 100,
      "termoEncerramentoId": 1,
      "perfilId": 10,
      "qtdeHora": 40.00,
      "valorHora": 120.00,
      "perfil": { "id": 10, "nome": "Analista", ... },
      "profissionais": [
        {
          "id": 201,
          "termoEncerramentoId": 100,
          "qtdeHora": 20.00,
          "valorHora": 120.00,
          "profissional": { "id": 5, "nome": "Maria Silva", ... }
        },
        {
          "id": 202,
          "termoEncerramentoId": 100,
          "qtdeHora": 20.00,
          "valorHora": 115.00,
          "profissional": { "id": 7, "nome": "João Santos", ... }
        }
      ]
    }
  ]
}
```

- Em cada custo, `termoEncerramentoId` no nível do custo é o ID do termo; dentro de cada profissional, `termoEncerramentoId` é o **ID do custo** (termo_encerramento_custo_id).
- Use `custos[].profissionais` para exibir/editar a lista de profissionais por linha de custo.

---

## 5. Regras de negócio (resumo)

1. **Custos:** continuam obrigatórios conforme regras atuais (perfilId, qtdeHora, valorHora).
2. **Profissionais por custo:** opcional. Se não enviar `profissionais` ou enviar `[]`, aquele custo fica sem profissionais.
3. **Profissional:** `profissionalId` deve existir em `/api/profissionais` (e estar vinculado ao contexto esperado, ex.: projeto).
4. **Update:** o PUT **substitui** todos os custos e todos os profissionais do termo pelo payload. Não há “merge” parcial: enviar a lista completa de custos (com os profissionais desejados em cada um).
5. **Exclusão de perfil na aba de custos:** continua permitida apenas quando o status da demanda técnica for **"E"** (Planejado e assinado). Caso contrário o backend retorna 400.

---

## 6. Tipos sugeridos (TypeScript)

```ts
// Item de profissional dentro de um custo (envio)
interface TermoEncerramentoCustoProfissionalItemDTO {
  profissionalId: number;
  qtdeHora: number;
  valorHora: number;
}

// Item de custo no create/update do termo
interface TermoEncerramentoCustoCreateDTO {
  perfilId: number;
  qtdeHora: number;
  valorHora: number;
  profissionais?: TermoEncerramentoCustoProfissionalItemDTO[];
}

// Corpo do POST/PUT do termo (trecho)
interface TermoEncerramentoCreateDTO {
  demandaTecnicaId: number;
  dataTermo: string;
  dataInicioExecucao?: string;
  dataFimExecucao?: string;
  resultadoEntregue: string;
  usuarioId: number;
  custos: TermoEncerramentoCustoCreateDTO[];
}
```

---

## 7. Migração no frontend

- **Criar termo:** montar em `custos` cada linha de custo e, onde houver profissionais, preencher `profissionais` com `{ profissionalId, qtdeHora, valorHora }`. Uma única chamada **POST** `/api/termos-encerramento` grava custos e profissionais.
- **Editar termo:** ao abrir o termo, usar o `custos` já vindo com `profissionais` na resposta do GET. Ao salvar, enviar no **PUT** a lista completa de `custos` (incluindo `profissionais` em cada um). Não é mais necessário chamar separadamente a API de `termos-encerramento-custos-profissionais` para inserir/atualizar por custo.
- As rotas de **termos-encerramento-custos-profissionais** (listar por custo, total por custo, etc.) continuam disponíveis para consultas e eventual uso pontual; a gravação em lote passa a ser feita pelo POST/PUT do termo.

---

Documento alinhado ao backend (TermoEncerramentoService, TermoEncerramentoCustoCreateDTO, TermoEncerramentoCustoProfissionalItemDTO, TermoEncerramentoCustoDTO com `profissionais`).
