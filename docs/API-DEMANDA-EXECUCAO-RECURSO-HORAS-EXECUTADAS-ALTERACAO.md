# Alteracao de API - Horas Executadas no Recurso de Tarefa

Documento de mudanca para frontend sobre o novo campo `horasExecutadas` em `DemandaExecucaoTarefaRecurso`.

## Objetivo

Permitir registrar horas efetivamente executadas por recurso na tarefa, alem de horas planejadas.

## Backend alterado

- Entidade: `DemandaExecucaoTarefaRecurso`
  - novo campo:
  - `@Column(name = "horas_executadas", precision = 10, scale = 2, nullable = true)`
  - `private BigDecimal horasExecutadas;`

- Migration:
  - `V13__demanda_execucao_tarefa_recurso_add_horas_executadas.sql`
  - adiciona coluna `horas_executadas` (nullable)

## Endpoints impactados

- `POST /api/demandas-execucao-tarefas-recursos`
- `PUT /api/demandas-execucao-tarefas-recursos/{id}`
- `GET /api/demandas-execucao-tarefas-recursos/{id}`
- `GET /api/demandas-execucao-tarefas-recursos/tarefa/{tarefaId}`
- `GET /api/demandas-execucao/gantt/demanda/{demandaTecnicaId}`

## Alteracoes de contrato

### Request - Create (`DemandaExecucaoTarefaRecursoCreateDTO`)

Novo campo opcional:
- `horasExecutadas: number` (>= 0)

Exemplo:

```json
{
  "demandaExecucaoTarefaId": 200,
  "profissionalId": 12,
  "perfilId": 3,
  "horasPlanejadas": 40.0,
  "horasExecutadas": 12.5
}
```

### Request - Update (`DemandaExecucaoTarefaRecursoUpdateDTO`)

Novo campo opcional:
- `horasExecutadas: number` (>= 0)

### Response - Recurso (`DemandaExecucaoTarefaRecursoDTO`)

Novo campo opcional:
- `horasExecutadas: number`

Exemplo:

```json
{
  "id": 10,
  "demandaExecucaoTarefaId": 200,
  "profissionalId": 12,
  "perfilId": 3,
  "horasPlanejadas": 40.0,
  "horasExecutadas": 12.5
}
```

### Response - Gantt (`DemandaExecucaoGanttRecursoDTO`)

Novo campo opcional:
- `horasExecutadas: number`

## Regras para frontend

1. Campo opcional: pode vir `null`/ausente.
2. Validar no formulario como numero >= 0 quando preenchido.
3. Exibir fallback visual quando `horasExecutadas` nao estiver informada.
4. Ajustar tipagem TypeScript dos DTOs de recurso e recurso no Gantt.

## Tipos sugeridos

```ts
interface DemandaExecucaoTarefaRecursoCreateDTO {
  demandaExecucaoTarefaId: number;
  profissionalId: number;
  perfilId?: number;
  horasPlanejadas: number;
  horasExecutadas?: number;
}

interface DemandaExecucaoTarefaRecursoDTO {
  id: number;
  demandaExecucaoTarefaId: number;
  profissionalId: number;
  perfilId?: number;
  horasPlanejadas: number;
  horasExecutadas?: number | null;
}
```

