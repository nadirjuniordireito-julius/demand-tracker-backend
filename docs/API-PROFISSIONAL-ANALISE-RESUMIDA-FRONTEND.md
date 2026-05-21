# API Profissional — Análise Resumida (Frontend)

Série mensal de horas executadas (rateadas por dias úteis), valor faturado por perfil e custo mensal do profissional.

---

## 1. Visão geral

| Campo | Descrição |
|-------|-----------|
| `profissional` | Dados do profissional (`ProfissionalDTO`) |
| `ano` | Ano de referência |
| `mes` | Mês (1–12) |
| `horasExecutadas` | Horas calculadas no mês: dias úteis no período da tarefa × 8 h/dia (com teto no total informado no recurso) |
| `valorPerfilMes` | Soma de `horasMes × Perfil.valor` dos recursos daquele mês |
| `valorCustoMes` | `ProfissionalCustoMensal.custoTotal` do mês; `0.00` se não houver registro |

**Período retornado:** de `dataInicioAtividade` do profissional até o **mês atual**, um item por mês (meses sem execução vêm com zeros em horas/valor perfil).

---

## 2. Regras de cálculo

### Fonte das horas

- Tabela `demanda_execucao_tarefa_recurso`, campo `horas_executadas`
- Período da tarefa: `data_inicio_real` / `data_fim_real`; se nulos, usa `data_inicio_planejada` / `data_fim_planejada`

### Horas por mês (sem divisão proporcional)

Para cada mês entre o início e o fim da tarefa (datas reais ou planejadas):

1. Recorta o período da tarefa naquele mês (ex.: março só de 30/03 a 31/03).
2. Conta **dias úteis** no recorte: exclui sábado, domingo e datas cadastradas em `dia_nao_util`.
3. `horasBrutasMes = diasUteis × 8` (jornada fixa de 8 horas por dia útil).
4. **Teto:** a soma dos meses de um mesmo recurso **não ultrapassa** `horas_executadas`. Se o bruto exceder o limite, os meses são preenchidos em ordem cronológica até esgotar o saldo (sem redistribuir proporcionalmente).
5. **Piso:** se o bruto for **menor** que `horas_executadas` (ex.: feriado em `dia_nao_util` reduz dias no calendário), o saldo `horas_executadas − soma(brutas)` é somado ao **último mês** da tarefa que já tinha horas brutas, para não perder horas na agregação mensal.

**Exemplo:** tarefa 30/03/2026–10/04/2026, `horas_executadas = 80`

| Mês | Recorte | Dias úteis | Horas |
|-----|---------|------------|-------|
| Mar/2026 | 30–31/03 | 2 | 16 |
| Abr/2026 | 01–10/04 | 8 (10 dias − 2 fds) | 64 |
| **Total** | | | **80** |

Se o bruto fosse 100 h e o limite 50 h: março 16 h, abril 34 h (restante zerado nos meses seguintes).

### valorPerfilMes

Para cada recurso no mês: `horasMes × perfil.valor` (perfil do recurso; se ausente, valor 0).

### valorCustoMes

Registro em `profissional_custo_mensal` para `(profissionalId, ano, mes)`. Se houver duplicidade, prevalece o registro de maior `id`.

---

## 3. Endpoint

**GET** `/api/profissionais/{id}/analise-resumida`

| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `id` | path (Long) | Sim | ID do profissional |
| `demandaExecucaoId` | query (Long) | Não | Quando informado, a análise considera **apenas** os recursos do profissional na execução indicada |

Exemplos:

```
GET /api/profissionais/5/analise-resumida
GET /api/profissionais/5/analise-resumida?demandaExecucaoId=12
```

Autenticação: `Authorization: Bearer <token>`

### Respostas

- `200 OK`: `List<ProfissionalAnaliseResumidaDTO>`
- `404 Not Found`: profissional inexistente ou `demandaExecucaoId` inexistente

### Filtro por execução

- **Sem** `demandaExecucaoId`: soma recursos de **todas** as execuções em que o profissional participa.
- **Com** `demandaExecucaoId`: soma apenas recursos em `demanda_execucao_tarefa_recurso` cuja tarefa pertence à execução informada **e** cujo `profissional_id` é o do path.
- `valorCustoMes` continua vindo de `profissional_custo_mensal` (não filtra por execução).

---

## 4. Exemplo de resposta

```json
[
  {
    "profissional": {
      "id": 5,
      "nome": "Maria Silva",
      "tipoPessoa": "F",
      "documento": "12345678901",
      "valorHora": 85.00,
      "custoTotalMensal": 12000.00,
      "dataInicioAtividade": "2025-01-15",
      "funcao": "Desenvolvedora",
      "projetoId": 1,
      "perfilId": 2,
      "perfilNome": "Analista Sênior"
    },
    "ano": 2026,
    "mes": 5,
    "horasExecutadas": 24.00,
    "valorPerfilMes": 4800.00,
    "valorCustoMes": 12500.75
  },
  {
    "profissional": { "id": 5, "nome": "Maria Silva" },
    "ano": 2026,
    "mes": 6,
    "horasExecutadas": 56.00,
    "valorPerfilMes": 11200.00,
    "valorCustoMes": 12500.75
  }
]
```

---

## 5. Contrato TypeScript

```ts
type ProfissionalAnaliseResumidaDTO = {
  profissional: ProfissionalDTO;
  ano: number;
  mes: number;
  horasExecutadas: number;
  valorPerfilMes: number;
  valorCustoMes: number;
};
```

---

## 6. Diferença vs outros endpoints

| Endpoint | Foco |
|----------|------|
| `GET /api/profissionais/{id}/analise-resumida` | Série mensal com rateio por **tarefa** + `DiaNaoUtil` + custo mensal |
| `GET /api/demandas-execucao/analytics/profissional/{id}` | Série mensal com horas previstas (8h × dias úteis) e rateio proporcional por **execução** (lógica legada) |

---

## 7. Checklist frontend

1. Chamar após selecionar profissional na tela de análise/custos.
2. Gráfico de linhas ou tabela: eixo X = `ano/mes`, séries `horasExecutadas`, `valorPerfilMes`, `valorCustoMes`.
3. `profissional` repete em cada item — usar o primeiro ou cache local.
4. Meses futuros não aparecem (até mês corrente apenas).

---

## 8. Prompt curto (agente frontend)

```text
Implementar tela de análise resumida do profissional:

- GET /api/profissionais/{profissionalId}/analise-resumida
- GET /api/profissionais/{profissionalId}/analise-resumida?demandaExecucaoId={id} (opcional, filtra por execução)
- Query opcional: demandaExecucaoId (filtrar análise à execução da demanda)
- Exibir série mensal: horasExecutadas, valorPerfilMes, valorCustoMes
- Horas já vêm rateadas pelo backend (dias úteis + dia_nao_util)
- Tratar 404 se profissional não existir
- Ver docs/API-PROFISSIONAL-ANALISE-RESUMIDA-FRONTEND.md
```
