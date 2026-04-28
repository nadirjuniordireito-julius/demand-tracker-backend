# Alteracao de API - Recurso de Tarefa com Perfil

Este documento descreve a mudanca aplicada na API de recursos de tarefa da execucao para suporte a `perfil`.

## Objetivo

Permitir que cada registro de `DemandaExecucaoTarefaRecurso` tenha um `perfil` associado, alem do `profissional`.

## Backend alterado

- Entidade: `DemandaExecucaoTarefaRecurso` agora possui `perfil` opcional (`perfil_id`).
- Endpoints impactados:
  - `POST /api/demandas-execucao-tarefas-recursos`
  - `PUT /api/demandas-execucao-tarefas-recursos/{id}`
  - `GET /api/demandas-execucao-tarefas-recursos/{id}`
  - `GET /api/demandas-execucao-tarefas-recursos/tarefa/{tarefaId}`
  - `GET /api/demandas-execucao/gantt/demanda/{demandaTecnicaId}` (estrutura de recurso no Gantt)

## Mudanca de contrato

### CreateDTO (request)

Campo novo opcional:
- `perfilId: number`

Exemplo:

```json
{
  "demandaExecucaoTarefaId": 100,
  "profissionalId": 12,
  "perfilId": 3,
  "horasPlanejadas": 24.5
}
```

### UpdateDTO (request)

Campo novo opcional:
- `perfilId: number`

### RecursoDTO (response)

Campos novos opcionais:
- `perfilId: number`
- `perfil: PerfilDTO`

Exemplo:

```json
{
  "id": 1,
  "demandaExecucaoTarefaId": 100,
  "profissionalId": 12,
  "profissional": {
    "id": 12,
    "nome": "Maria Silva"
  },
  "perfilId": 3,
  "perfil": {
    "id": 3,
    "nome": "Analista"
  },
  "horasPlanejadas": 24.5
}
```

### GanttRecursoDTO (response)

Campos novos opcionais:
- `perfilId: number`
- `perfilNome: string`

Exemplo:

```json
{
  "id": 77,
  "profissionalId": 12,
  "nome": "Maria Silva",
  "perfilId": 3,
  "perfilNome": "Analista",
  "horasPlanejadas": 24.5
}
```

## Banco de dados

Nova migration:
- `V12__demanda_execucao_tarefa_recurso_add_perfil.sql`

Acao:
- adiciona coluna `perfil_id` em `demanda_execucao_tarefa_recurso`
- cria indice para `perfil_id`

## Acoes no frontend

1. Ajustar tipos/interfaces dos DTOs de recurso e Gantt para incluir campos de perfil opcionais.
2. Ajustar formularios de recurso para enviar `perfilId` (opcional).
3. Tratar retorno sem `perfil` (null/ausente) para manter compatibilidade.
4. Exibir perfil no Gantt e listas de recursos quando disponivel.

