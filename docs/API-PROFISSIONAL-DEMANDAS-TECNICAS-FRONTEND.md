# API Profissional — Demandas técnicas alocadas (Frontend)

Lista as demandas técnicas (DT) em que o profissional foi alocado em execução, com totais de horas, período das tarefas, totais mensais por DT e **resumo mensal agregado** (custos de perfil e mensal).

---

## 1. Endpoint

**GET** `/api/profissionais/{id}/demandas-tecnicas`

| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `id` | path (Long) | Sim | ID do profissional |

Autenticação: `Authorization: Bearer <token>`

### Respostas

- `200 OK`: `ProfissionalDemandasTecnicasResponseDTO`
- `404 Not Found`: profissional inexistente

---

## 2. Contrato

```ts
interface ProfissionalDemandaTecnicaMensalDTO {
  ano: number;
  mes: number;              // 1–12
  totalPlanejado: number;
  totalExecutado: number;
}

interface ProfissionalDemandaTecnicaDTO {
  demandaTecnicaId: number;
  demandaCodigo: string;       // número/código da DT
  demandaNome: string;
  demandaStatus: string;       // status da DT (ex.: A, E, G)
  totalHorasExecutadas: number;
  totalHorasPlanejadas: number; // soma de horasPlanejadas dos recursos do profissional na execução da DT
  totalHorasUteisPeriodo: number; // dias úteis no período consolidado das tarefas × 8
  totaisMensais: ProfissionalDemandaTecnicaMensalDTO[];
  dataInicioExecucao: string;  // yyyy-MM-dd
  dataFimExecucao: string;     // yyyy-MM-dd
}

interface ProfissionalDemandaTecnicaResumoMensalDTO {
  ano: number;
  mes: number;                 // 1–12
  totalPlanejado: number;      // soma dos totaisMensais.totalPlanejado de todas as DTs
  totalExecutado: number;      // soma dos totaisMensais.totalExecutado de todas as DTs
  valorCustoPerfil: number;    // totalExecutado × profissional.perfil.valor
  valorCustoMensal: number;    // ProfissionalCustoMensal.custoTotal (ano/mês) ou 0
  horasPrevistas: number;      // dias úteis do mês × 8 a partir de max(1º do mês, dataInicioAtividade); exclui fds e dia_nao_util
}

interface ProfissionalDemandasTecnicasResponseDTO {
  demandasTecnicas: ProfissionalDemandaTecnicaDTO[];
  resumoMensal: ProfissionalDemandaTecnicaResumoMensalDTO[];
}
```

---

## 3. Regras de cálculo

### Fonte da alocação

- Tabela `demanda_execucao_tarefa_recurso` (`profissional_id` = profissional informado)
- Cadeia: recurso → tarefa → `demanda_execucao` → `demandas_tecnicas`

### Agregação por DT

- Um item por **demanda técnica** (mesmo profissional em várias tarefas da mesma DT → um único registro)
- `totalHorasExecutadas`: soma de `horas_executadas` dos recursos do profissional naquela DT (nulo tratado como 0)
- `totalHorasPlanejadas`: soma de `horas_planejadas` dos **recursos da execução** do profissional naquela DT (não usa o termo de planejamento)
- `totalHorasUteisPeriodo`: dias úteis entre `dataInicioExecucao` e `dataFimExecucao` × **8** h/dia. Exclui sábado, domingo e datas cadastradas em `dia_nao_util`. Capacidade de calendário do período consolidado

### Totais mensais por DT (`totaisMensais`)

Para cada recurso/tarefa do profissional na DT, **separadamente** para planejado e executado:

1. Período da tarefa: `data_inicio_real` / `data_fim_real` (fallback planejada)
2. Conta dias úteis em cada mês do intervalo (exclui fds e `dia_nao_util`)
3. Capacidade do mês: `capacidadeMes = diasUteisMes × 8`
4. Consome as horas lançadas (`horasPlanejadas` ou `horasExecutadas`) em ordem cronológica:
   - mês N recebe `min(capacidadeMes, saldoRestante)`
   - se o lançado for **menor** que a soma das capacidades, os meses finais ficam com o restante
   - se o lançado for **maior** que a soma das capacidades, o excedente vai para o **último** mês com capacidade
5. Horas lançadas ≤ 0: aquele recurso não contribui no mensal
6. Soma as parcelas de todos os recursos da DT no mesmo `ano`/`mes`

**Invariante:** para cada DT,

- `SUM(totaisMensais.totalPlanejado) === totalHorasPlanejadas`
- `SUM(totaisMensais.totalExecutado) === totalHorasExecutadas`

Lista ordenada por `ano`, `mes` crescente. Sempre presente (pode ser `[]`).

### Resumo mensal (`resumoMensal`)

Derivado **após** montar `demandasTecnicas` (não recalcula rateio):

1. Soma `totaisMensais.totalPlanejado` e `totalExecutado` de **todas** as DTs por `(ano, mes)`
2. `valorCustoPerfil = totalExecutado × profissional.perfil.valor` (perfil/valor nulo → 0)
3. `valorCustoMensal = profissional_custo_mensal.custoTotal` para profissional/ano/mês (sem registro → 0; se houver mais de um, maior `id`)
4. `horasPrevistas` = dias úteis do mês × **8** h/dia, a partir de `max(1º dia do mês, profissional.dataInicioAtividade)`. Exclui sábado, domingo e datas em `dia_nao_util`. Se a admissão for após o fim do mês → 0. Independente do recorte das tarefas
5. Ordenado por ano/mês crescente

### Período de execução

Datas consolidadas a partir das **tarefas** em que o profissional está alocado na DT (`demanda_execucao_tarefa`):

- Por tarefa: `data_inicio_real` / `data_fim_real`; se nulas, usa `data_inicio_planejada` / `data_fim_planejada`
- Com várias tarefas na mesma DT: `dataInicioExecucao` = menor início; `dataFimExecucao` = maior fim
- `totalHorasUteisPeriodo` usa esse intervalo consolidado (dias úteis × 8)

Não usa as datas de `demanda_execucao` (nível execução da DT).

### Ordenação

`demandasTecnicas` ordenada por `demandaCodigo` ascendente (case-insensitive).

---

## 4. Exemplo

```http
GET /api/profissionais/5/demandas-tecnicas
```

Tarefa `22/06/2026`–`10/07/2026` com 80h planejadas e 80h executadas: junho capacidade 56 (7×8); julho recebe o restante 24. Perfil com valor hora 100; custo mensal lançado em junho 5000.

```json
{
  "demandasTecnicas": [
    {
      "demandaTecnicaId": 12,
      "demandaCodigo": "DT-M001-001-2026",
      "demandaNome": "Implementação módulo X",
      "demandaStatus": "E",
      "totalHorasExecutadas": 80.00,
      "totalHorasPlanejadas": 80.00,
      "totalHorasUteisPeriodo": 120.00,
      "dataInicioExecucao": "2026-06-22",
      "dataFimExecucao": "2026-07-10",
      "totaisMensais": [
        { "ano": 2026, "mes": 6, "totalPlanejado": 56.00, "totalExecutado": 56.00 },
        { "ano": 2026, "mes": 7, "totalPlanejado": 24.00, "totalExecutado": 24.00 }
      ]
    }
  ],
  "resumoMensal": [
    {
      "ano": 2026,
      "mes": 6,
      "totalPlanejado": 56.00,
      "totalExecutado": 56.00,
      "valorCustoPerfil": 5600.00,
      "valorCustoMensal": 5000.00,
      "horasPrevistas": 176.00
    },
    {
      "ano": 2026,
      "mes": 7,
      "totalPlanejado": 24.00,
      "totalExecutado": 24.00,
      "valorCustoPerfil": 2400.00,
      "valorCustoMensal": 0.00,
      "horasPrevistas": 184.00
    }
  ]
}
```

---

## 5. Relação com outros endpoints

| Endpoint | Uso |
|----------|-----|
| `GET /api/profissionais/{id}/analise-resumida` | Série **mensal** de horas rateadas por dias úteis (visão global do profissional) |
| `GET /api/demandas-execucao/analytics/profissional/{id}` | Analytics mensal legado (previsto vs executado) |
| `GET /api/profissionais/{id}/demandas-tecnicas` | Lista **por DT** + resumo mensal agregado com custos |
