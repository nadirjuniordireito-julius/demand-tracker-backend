# API Execucao Profissional - Frontend

Endpoint analitico para retornar a execucao mensal de um profissional.

## Endpoint

- `GET /api/demandas-execucao/analytics/profissional/{profissionalId}`

## Resposta

Retorna lista de `ExecucaoProfissionalDTO` ordenada por ano/mes (ascendente), da admissao do profissional ate o mes atual.

```json
[
  {
    "id": 12,
    "mes": 3,
    "ano": 2026,
    "horasPrevistas": 168.00,
    "horasExecutadas": 84.44,
    "valorMensalRemuneracao": 18000.00,
    "valorTotalExecucao": 10132.80
  }
]
```

## Contrato DTO

```ts
interface ExecucaoProfissionalDTO {
  id: number; // profissionalId
  mes: number; // 1..12
  ano: number;
  horasPrevistas: number;
  horasExecutadas: number;
  valorMensalRemuneracao: number;
  valorTotalExecucao: number;
}
```

## Regras de calculo implementadas

1. **horasPrevistas**
   - Desde `Profissional.dataInicioAtividade`.
   - `diasUteis * 8` por mes.
   - Dias uteis = segunda a sexta excluindo feriados nacionais.
   - Mes de admissao considera do dia de admissao em diante.
   - Mes atual considera ate a data atual.

2. **horasExecutadas**
   - Origem: `DemandaExecucaoTarefaRecurso.horasExecutadas` por profissional.
   - Cada registro e distribuido entre meses conforme dias uteis do periodo da execucao da DT.
   - Periodo de execucao da DT:
     - inicio: `DemandaExecucao.dataInicioReal` (fallback `dataInicioPlanejada`)
     - fim: `DemandaExecucao.dataFimReal` (fallback `dataFimPlanejada`)

3. **valorMensalRemuneracao**
   - Origem: `ProfissionalCustoMensal.custoTotal` do mesmo `ano/mes`.
   - Sem registro no mes -> `0.00`.

4. **valorTotalExecucao**
   - Somatorio mensal de `horasExecutadasRateadas * perfil.valor`.
   - Perfil usado: `DemandaExecucaoTarefaRecurso.perfil`.
   - Sem perfil -> contribuicao `0.00`.

## Feriados nacionais considerados

- Fixos:
  - 01/01, 21/04, 01/05, 07/09, 12/10, 02/11, 15/11, 25/12
- Moveis:
  - Carnaval (segunda e terca), Sexta-feira Santa, Corpus Christi

## Status HTTP

- `200 OK`: sucesso
- `404 Not Found`: profissional nao encontrado

