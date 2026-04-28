# API Demanda Execução – Documentação para Frontend

Documento para implementação no frontend dos recursos de **Execução da Demanda Técnica**: execução, tarefas, dependências entre tarefas, recursos (profissionais) e apontamentos de progresso.

---

## 1. Visão geral do modelo

- **DemandaExecucao**: uma por demanda técnica (1:1). Contém datas planejadas/reais, status, percentual de progresso e usuário responsável.
- **DemandaExecucaoTarefa**: tarefas da execução. Status (enum): `PLANEJADA`, `EM_ANDAMENTO`, `BLOQUEADA`, `CONCLUIDA`. Prioridade (1 caractere), datas, estimativa de horas.
- **DemandaExecucaoTarefaDependencia**: vínculo entre duas tarefas (origem → destino).
- **DemandaExecucaoTarefaRecurso**: alocação de profissional em uma tarefa com horas planejadas.
- **DemandaExecucaoTarefaApontamentoProgresso**: registro de progresso (data, percentual, comentário) por tarefa.

**Autenticação:** todas as rotas exigem JWT: `Authorization: Bearer <token>`.

---

## 2. Base URL

- Execução: `{API_BASE}/api/demandas-execucao`
- Tarefas: `{API_BASE}/api/demandas-execucao-tarefas`
- Dependências: `{API_BASE}/api/demandas-execucao-tarefas-dependencias`
- Recursos: `{API_BASE}/api/demandas-execucao-tarefas-recursos`
- Apontamentos: `{API_BASE}/api/demandas-execucao-tarefas-apontamentos`

---

## 3. Demanda Execução

### 3.1 Listar (paginado)

**GET** `/api/demandas-execucao`

Query params: `page`, `size`, `sort` (ex.: `dataCriacaoExecucao,desc`).

**Resposta:** `200 OK` – `Page<DemandaExecucaoDTO>` (campos: `content`, `totalElements`, `totalPages`, etc.).

### 3.2 Buscar por ID

**GET** `/api/demandas-execucao/{id}`

**Resposta:** `200 OK` – `DemandaExecucaoDTO`.

### 3.3 Buscar por demanda técnica

**GET** `/api/demandas-execucao/demanda/{demandaTecnicaId}`

**Resposta:** `200 OK` – `DemandaExecucaoDTO`. `404` se não existir execução para a demanda.

### 3.4 Criar

**POST** `/api/demandas-execucao`

**Body (DemandaExecucaoCreateDTO):**

| Campo                 | Tipo     | Obrigatório | Descrição                          |
|-----------------------|----------|-------------|------------------------------------|
| demandaTecnicaId      | long     | Sim         | ID da demanda técnica              |
| usuarioId             | long     | Não         | ID do usuário responsável          |
| dataInicioPlanejada   | date     | Sim         | Data início planejada              |
| dataFimPlanejada     | date     | Sim         | Data fim planejada                 |
| dataInicioReal       | date     | Não         | Data início real                   |
| dataFimReal          | date     | Não         | Data fim real                      |
| status                | string   | Sim         | Status da execução (até 50 caracteres) |
| percentualProgresso   | decimal  | Sim         | 0.00 a 100.00                      |

**Resposta:** `201 Created` – `DemandaExecucaoDTO`. `400` se já existir execução para a demanda.

### 3.5 Atualizar

**PUT** `/api/demandas-execucao/{id}`

**Body (DemandaExecucaoUpdateDTO):** todos os campos opcionais (usuarioId, dataInicioPlanejada, dataFimPlanejada, dataInicioReal, dataFimReal, status, percentualProgresso).

**Resposta:** `200 OK` – `DemandaExecucaoDTO`.

### 3.6 Excluir

**DELETE** `/api/demandas-execucao/{id}`

**Resposta:** `204 No Content`. `404` se não existir.

### 3.7 Encerrar execução e gerar Termo de Encerramento

**POST** `/api/demandas-execucao/{id}/encerrar`

**Query params:**

| Parâmetro  | Tipo | Obrigatório | Descrição                                  |
|-----------|------|-------------|--------------------------------------------|
| `usuarioId` | long | **Sim**     | ID do usuário que está encerrando a execução |

**O que a API faz (regra de negócio):**

1. Localiza a **DemandaExecucao** pelo `{id}`.
2. Atualiza a execução:
   - `status` = `"CONCLUIDA"`
   - `percentualProgresso` = `100`
3. Atualiza a **DemandaTecnica** associada:
   - `status` = `"F"` (Em encerramento).
4. Localiza o **TermoPlanejamento** da demanda (1:1 com DemandaTecnica).
5. Gera automaticamente um **Termo de Encerramento** com:
   - `resultadoEntregue` = `resultadoEsperado` do TermoPlanejamento
   - `dataTermo` = data/hora atual
   - `usuario` = usuário informado por `usuarioId`
   - `dataInicioExecucao` = `DemandaExecucao.dataInicioReal`
   - `dataFimExecucao` = `DemandaExecucao.dataFimReal`
6. Sumariza horas planejadas em `DemandaExecucaoTarefaRecurso` **por perfil**:
   - Agrupa todos os registros por `profissional.perfil`
   - Para cada perfil:
     - Soma `horasPlanejadas` de todos os recursos do perfil → `qtdeHora`
     - Usa `perfil.valor` como `valorHora`
7. Para cada perfil, cria um **TermoEncerramentoCusto**:
   - `termoEncerramento` = termo criado no passo 5
   - `perfil` = perfil do grupo
   - `qtdeHora` = soma das horas planejadas do grupo
   - `valorHora` = `perfil.valor`
8. Para cada recurso (`DemandaExecucaoTarefaRecurso`) do perfil, cria um **TermoEncerramentoCustoProfissional**:
   - `termoEncerramentoCusto` = custo do perfil correspondente
   - `profissional` = `DemandaExecucaoTarefaRecurso.profissional`
   - `qtdeHora` = `DemandaExecucaoTarefaRecurso.horasPlanejadas`
   - `valorHora` = `perfil.valor`

**Resposta:** `200 OK` – `TermoEncerramentoDTO` contendo o termo de encerramento gerado.

**Erros possíveis:**

- `404` se:
  - Execução `{id}` não existir
  - `usuarioId` não existir
- `400` se:
  - A execução não tiver DemandaTecnica associada
  - A demanda não possuir TermoPlanejamento
  - Não houver recursos em `DemandaExecucaoTarefaRecurso` para essa execução

**Nome da rotina interna no backend:**

- Serviço: `DemandaExecucaoService`
- Método: `encerrarExecucaoInterna(Long demandaExecucaoId, Long usuarioId)`

### 3.7 Dados para Gantt (por ID da Demanda Técnica)

**GET** `/api/demandas-execucao/gantt/demanda/{demandaTecnicaId}`

Retorna um DTO com dados suficientes para o frontend montar o diagrama de Gantt da execução: nível da execução (datas, progresso) e lista de tarefas com datas planejadas/reais, percentual, **predecessores** (IDs das tarefas das quais cada uma depende) e **recursos** (profissionais alocados).

**Resposta:** `200 OK` – `DemandaExecucaoGanttDTO`. `404` se não existir execução para a demanda.

**Estrutura DemandaExecucaoGanttDTO:**

| Campo                  | Tipo    | Descrição |
|------------------------|---------|-----------|
| demandaTecnicaId       | long    | ID da demanda técnica |
| demandaTecnicaCodigo   | string  | Código da demanda |
| demandaTecnicaNome     | string  | Nome da demanda |
| demandaExecucaoId      | long    | ID da execução |
| dataInicioPlanejada    | date    | Início planejado (execução) |
| dataFimPlanejada       | date    | Fim planejado (execução) |
| dataInicioReal         | date    | Início real (se houver) |
| dataFimReal            | date    | Fim real (se houver) |
| status                 | string  | Status da execução |
| percentualProgresso    | decimal | Progresso geral da execução |
| tarefas                | array   | Lista de `DemandaExecucaoGanttTarefaDTO` |

**Estrutura DemandaExecucaoGanttTarefaDTO (cada item de `tarefas`):**

| Campo                  | Tipo    | Descrição |
|------------------------|---------|-----------|
| id                     | long    | ID da tarefa |
| titulo                 | string  | Título da tarefa |
| descricao              | string  | Descrição (opcional) |
| status                 | enum    | PLANEJADA, EM_ANDAMENTO, BLOQUEADA, CONCLUIDA |
| prioridade             | string  | 1 caractere |
| dataInicioPlanejada    | date    | Início planejado da tarefa |
| dataFimPlanejada       | date    | Fim planejado da tarefa |
| dataInicioReal         | date    | Início real (se houver) |
| dataFimReal            | date    | Fim real (se houver) |
| percentualProgresso    | decimal | 0–100 |
| estimativaHoras        | decimal | Horas estimadas |
| predecessorIds         | array   | IDs das tarefas predecessoras (esta depende delas) |
| recursos               | array   | Lista de `DemandaExecucaoGanttRecursoDTO` |

**Estrutura DemandaExecucaoGanttRecursoDTO (cada item de `recursos`):**

| Campo            | Tipo    | Descrição |
|------------------|---------|-----------|
| id               | long    | ID do vínculo recurso-tarefa |
| profissionalId   | long    | ID do profissional |
| nome             | string  | Nome do profissional |
| perfilId         | long    | ID do perfil aplicado ao recurso (opcional) |
| perfilNome       | string  | Nome do perfil aplicado ao recurso (opcional) |
| horasPlanejadas  | decimal | Horas planejadas |
| horasExecutadas  | decimal | Horas executadas (opcional) |

No Gantt, use `dataInicioPlanejada`/`dataFimPlanejada` (ou `dataInicioReal`/`dataFimReal` quando preenchidas) para as barras; `predecessorIds` para desenhar as setas de dependência (tarefa A → tarefa B quando B.predecessorIds contém A.id).

**Exemplo de resposta JSON (GET /api/demandas-execucao/gantt/demanda/5):**

```json
{
  "demandaTecnicaId": 5,
  "demandaTecnicaCodigo": "DT-2026-001",
  "demandaTecnicaNome": "Implementação do módulo de relatórios",
  "demandaExecucaoId": 3,
  "dataInicioPlanejada": "2026-03-01",
  "dataFimPlanejada": "2026-05-15",
  "dataInicioReal": null,
  "dataFimReal": null,
  "status": "Em andamento",
  "percentualProgresso": 25.50,
  "tarefas": [
    {
      "id": 10,
      "titulo": "Levantamento de requisitos",
      "descricao": "Reuniões com stakeholders e documentação.",
      "status": "CONCLUIDA",
      "prioridade": "A",
      "dataInicioPlanejada": "2026-03-01",
      "dataFimPlanejada": "2026-03-10",
      "dataInicioReal": "2026-03-01",
      "dataFimReal": "2026-03-09",
      "percentualProgresso": 100.00,
      "estimativaHoras": 40.00,
      "predecessorIds": [],
      "recursos": [
        {
          "id": 1,
          "profissionalId": 7,
          "nome": "Maria Silva",
          "horasPlanejadas": 40.00
        }
      ]
    },
    {
      "id": 11,
      "titulo": "Desenvolvimento do backend",
      "descricao": "APIs e regras de negócio.",
      "status": "EM_ANDAMENTO",
      "prioridade": "A",
      "dataInicioPlanejada": "2026-03-11",
      "dataFimPlanejada": "2026-04-20",
      "dataInicioReal": "2026-03-10",
      "dataFimReal": null,
      "percentualProgresso": 30.00,
      "estimativaHoras": 120.00,
      "predecessorIds": [10],
      "recursos": [
        {
          "id": 2,
          "profissionalId": 8,
          "nome": "João Santos",
          "horasPlanejadas": 80.00
        },
        {
          "id": 3,
          "profissionalId": 9,
          "nome": "Ana Costa",
          "horasPlanejadas": 40.00
        }
      ]
    },
    {
      "id": 12,
      "titulo": "Testes e homologação",
      "descricao": null,
      "status": "PLANEJADA",
      "prioridade": "B",
      "dataInicioPlanejada": "2026-04-21",
      "dataFimPlanejada": "2026-05-15",
      "dataInicioReal": null,
      "dataFimReal": null,
      "percentualProgresso": 0.00,
      "estimativaHoras": 60.00,
      "predecessorIds": [11],
      "recursos": []
    }
  ]
}
```

Neste exemplo, a tarefa 11 depende da 10 (`predecessorIds: [10]`) e a tarefa 12 depende da 11 (`predecessorIds: [11]`), formando uma sequência no Gantt.

### Estrutura DemandaExecucaoDTO (resposta)

| Campo               | Tipo     | Descrição                              |
|---------------------|----------|----------------------------------------|
| id                  | long     | ID da execução                         |
| demandaTecnicaId    | long     | ID da demanda técnica                  |
| usuarioId           | long     | ID do usuário (pode ser null)          |
| usuario             | object   | UsuarioDTO (id, nome, email, etc.)      |
| dataInicioPlanejada | date     | Data início planejada                  |
| dataFimPlanejada    | date     | Data fim planejada                     |
| dataInicioReal      | date     | Data início real (pode ser null)       |
| dataFimReal         | date     | Data fim real (pode ser null)          |
| status              | string   | Status                                  |
| percentualProgresso | decimal  | Percentual 0–100                       |
| dataCriacaoExecucao | datetime | Data/hora de criação                    |
| tarefas             | array    | Lista de DemandaExecucaoTarefaDTO       |

---

## 4. Tarefas da execução

### 4.1 Listar por execução

**GET** `/api/demandas-execucao-tarefas/execucao/{demandaExecucaoId}`

**Resposta:** `200 OK` – array de `DemandaExecucaoTarefaDTO`.

### 4.2 Buscar por ID

**GET** `/api/demandas-execucao-tarefas/{id}`

**Resposta:** `200 OK` – `DemandaExecucaoTarefaDTO` (com recursos e apontamentos).

### 4.3 Criar

**POST** `/api/demandas-execucao-tarefas`

**Body (DemandaExecucaoTarefaCreateDTO):**

| Campo                 | Tipo     | Obrigatório | Descrição                          |
|-----------------------|----------|-------------|------------------------------------|
| demandaExecucaoId     | long     | Sim         | ID da execução da demanda          |
| titulo                | string   | Sim         | Até 500 caracteres                 |
| descricao             | string   | Não         | Até 9000 caracteres                |
| status                | enum     | Sim         | PLANEJADA, EM_ANDAMENTO, BLOQUEADA, CONCLUIDA |
| prioridade            | string   | Sim         | 1 caractere                        |
| dataInicioPlanejada   | date     | Sim         | Data início planejada              |
| dataFimPlanejada     | date     | Sim         | Data fim planejada                 |
| dataInicioReal        | date     | Não         | Data início real                   |
| dataFimReal           | date     | Não         | Data fim real                      |
| percentualProgresso   | decimal  | Sim         | 0.00 a 100.00                      |
| estimativaHoras       | decimal  | Sim         | ≥ 0                                |

**Resposta:** `201 Created` – `DemandaExecucaoTarefaDTO`.

### 4.4 Atualizar

**PUT** `/api/demandas-execucao-tarefas/{id}`

**Body (DemandaExecucaoTarefaUpdateDTO):** todos opcionais (titulo, descricao, status, prioridade, datas, percentualProgresso, estimativaHoras).

**Resposta:** `200 OK` – `DemandaExecucaoTarefaDTO`.

### 4.5 Excluir

**DELETE** `/api/demandas-execucao-tarefas/{id}`

**Resposta:** `204 No Content`.

### Estrutura DemandaExecucaoTarefaDTO

| Campo                 | Tipo   | Descrição                                  |
|-----------------------|--------|--------------------------------------------|
| id                    | long   | ID da tarefa                               |
| demandaExecucaoId     | long   | ID da execução                             |
| titulo                | string | Título                                      |
| descricao             | string | Descrição                                   |
| status                | enum   | PLANEJADA \| EM_ANDAMENTO \| BLOQUEADA \| CONCLUIDA |
| prioridade            | string | Prioridade (1 caractere)                   |
| dataInicioPlanejada   | date   | Data início planejada                      |
| dataFimPlanejada      | date   | Data fim planejada                         |
| dataInicioReal        | date   | Data início real                           |
| dataFimReal           | date   | Data fim real                              |
| percentualProgresso   | decimal| Percentual 0–100                           |
| estimativaHoras       | decimal| Estimativa em horas                        |
| recursos              | array  | Lista de DemandaExecucaoTarefaRecursoDTO    |
| apontamentos          | array  | Lista de DemandaExecucaoTarefaApontamentoProgressoDTO |

---

## 5. Dependências entre tarefas

### 5.1 Listar por tarefa origem

**GET** `/api/demandas-execucao-tarefas-dependencias/tarefa-origem/{tarefaOrigemId}`

**Resposta:** `200 OK` – array de `DemandaExecucaoTarefaDependenciaDTO`.

### 5.2 Listar por tarefa destino

**GET** `/api/demandas-execucao-tarefas-dependencias/tarefa-destino/{tarefaDestinoId}`

**Resposta:** `200 OK` – array de `DemandaExecucaoTarefaDependenciaDTO`.

### 5.3 Buscar por ID

**GET** `/api/demandas-execucao-tarefas-dependencias/{id}`

**Resposta:** `200 OK` – `DemandaExecucaoTarefaDependenciaDTO`.

### 5.4 Criar

**POST** `/api/demandas-execucao-tarefas-dependencias`

**Body:**

| Campo           | Tipo | Obrigatório | Descrição        |
|-----------------|------|-------------|------------------|
| tarefaOrigemId  | long | Sim         | ID da tarefa origem  |
| tarefaDestinoId | long | Sim         | ID da tarefa destino |

**Resposta:** `201 Created` – `DemandaExecucaoTarefaDependenciaDTO`. `400` se origem e destino forem iguais.

### 5.5 Excluir

**DELETE** `/api/demandas-execucao-tarefas-dependencias/{id}`

**Resposta:** `204 No Content`.

### Estrutura DemandaExecucaoTarefaDependenciaDTO

| Campo           | Tipo | Descrição          |
|-----------------|------|--------------------|
| id              | long | ID da dependência  |
| tarefaOrigemId  | long | ID da tarefa origem  |
| tarefaDestinoId | long | ID da tarefa destino |

---

## 6. Recursos (profissionais por tarefa)

### 6.1 Listar por tarefa

**GET** `/api/demandas-execucao-tarefas-recursos/tarefa/{tarefaId}`

**Resposta:** `200 OK` – array de `DemandaExecucaoTarefaRecursoDTO`.

### 6.2 Buscar por ID

**GET** `/api/demandas-execucao-tarefas-recursos/{id}`

**Resposta:** `200 OK` – `DemandaExecucaoTarefaRecursoDTO`.

### 6.3 Criar

**POST** `/api/demandas-execucao-tarefas-recursos`

**Body (DemandaExecucaoTarefaRecursoCreateDTO):**

| Campo                   | Tipo    | Obrigatório | Descrição              |
|-------------------------|---------|-------------|------------------------|
| demandaExecucaoTarefaId | long    | Sim         | ID da tarefa           |
| profissionalId          | long    | Sim         | ID do profissional     |
| perfilId               | long    | Não         | ID do perfil associado ao recurso |
| horasPlanejadas         | decimal | Sim         | ≥ 0                    |
| horasExecutadas         | decimal | Não         | ≥ 0                    |

**Resposta:** `201 Created` – `DemandaExecucaoTarefaRecursoDTO`.

### 6.4 Atualizar

**PUT** `/api/demandas-execucao-tarefas-recursos/{id}`

**Body (DemandaExecucaoTarefaRecursoUpdateDTO):** profissionalId (opcional), perfilId (opcional), horasPlanejadas (opcional), horasExecutadas (opcional).

**Resposta:** `200 OK` – `DemandaExecucaoTarefaRecursoDTO`.

### 6.5 Excluir

**DELETE** `/api/demandas-execucao-tarefas-recursos/{id}`

**Resposta:** `204 No Content`.

### Estrutura DemandaExecucaoTarefaRecursoDTO

| Campo                   | Tipo    | Descrição                |
|-------------------------|---------|--------------------------|
| id                      | long    | ID do recurso            |
| demandaExecucaoTarefaId | long    | ID da tarefa             |
| profissionalId          | long    | ID do profissional       |
| profissional            | object  | ProfissionalDTO          |
| perfilId               | long    | ID do perfil do recurso (opcional) |
| perfil                 | object  | PerfilDTO (opcional)     |
| horasPlanejadas         | decimal | Horas planejadas        |
| horasExecutadas         | decimal | Horas executadas (opcional) |

---

## 7. Apontamentos de progresso

### 7.1 Listar por tarefa

**GET** `/api/demandas-execucao-tarefas-apontamentos/tarefa/{tarefaId}`

**Resposta:** `200 OK` – array de `DemandaExecucaoTarefaApontamentoProgressoDTO` (ordenados por data).

### 7.2 Buscar por ID

**GET** `/api/demandas-execucao-tarefas-apontamentos/{id}`

**Resposta:** `200 OK` – `DemandaExecucaoTarefaApontamentoProgressoDTO`.

### 7.3 Criar

**POST** `/api/demandas-execucao-tarefas-apontamentos`

**Body (DemandaExecucaoTarefaApontamentoProgressoCreateDTO):**

| Campo                   | Tipo    | Obrigatório | Descrição        |
|-------------------------|---------|-------------|------------------|
| demandaExecucaoTarefaId | long    | Sim         | ID da tarefa     |
| data                    | date    | Sim         | Data do apontamento |
| percentual              | decimal | Sim         | 0.00 a 100.00    |
| comentario              | string  | Sim         | Até 9000 caracteres |

**Resposta:** `201 Created` – `DemandaExecucaoTarefaApontamentoProgressoDTO`.

### 7.4 Atualizar

**PUT** `/api/demandas-execucao-tarefas-apontamentos/{id}`

**Body (DemandaExecucaoTarefaApontamentoProgressoUpdateDTO):** data, percentual, comentario (todos opcionais).

**Resposta:** `200 OK` – `DemandaExecucaoTarefaApontamentoProgressoDTO`.

### 7.5 Excluir

**DELETE** `/api/demandas-execucao-tarefas-apontamentos/{id}`

**Resposta:** `204 No Content`.

### Estrutura DemandaExecucaoTarefaApontamentoProgressoDTO

| Campo                   | Tipo    | Descrição   |
|-------------------------|---------|-------------|
| id                      | long    | ID do apontamento |
| demandaExecucaoTarefaId | long    | ID da tarefa |
| data                    | date    | Data        |
| percentual              | decimal | Percentual 0–100 |
| comentario              | string  | Comentário  |

---

## 8. Fluxo sugerido no frontend

1. **Demanda técnica** → Verificar se existe execução: `GET /api/demandas-execucao/demanda/{demandaTecnicaId}`. Se 404, criar: `POST /api/demandas-execucao` com `demandaTecnicaId`.
2. **Tarefas** → Listar: `GET /api/demandas-execucao-tarefas/execucao/{demandaExecucaoId}`. Criar/editar/excluir via endpoints de tarefas.
3. **Dependências** → Criar após existirem duas tarefas; listar por tarefa origem ou destino para montar grafo/lista.
4. **Recursos** → Por tarefa: listar, criar, atualizar, excluir recursos (profissional + perfil opcional + horas).
5. **Apontamentos** → Por tarefa: listar (ordenado por data), criar, atualizar, excluir apontamentos de progresso.

---

## 9. Códigos de erro

| HTTP | Situação |
|------|----------|
| 400  | Dados inválidos ou regra de negócio (ex.: já existe execução para a demanda; origem = destino na dependência). |
| 404  | Recurso não encontrado (ID inexistente ou execução inexistente para a demanda). |
