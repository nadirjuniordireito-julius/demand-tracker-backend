# API - Check de Horas Planejadas por Perfil (Demanda Técnica)

## Objetivo

Comparar, para uma mesma Demanda Técnica, o total de horas planejadas por perfil profissional em duas fontes distintas do sistema:

- **A — Termo de Planejamento (TermoPlanejamentoCusto)**: horas planejadas formais do termo (`qtdeHora` por perfil).
- **B — Execução da Demanda (DemandaExecucaoTarefaRecurso)**: horas planejadas distribuídas em recursos das tarefas da execução (`horasPlanejadas` por perfil).

O endpoint retorna a **união** dos perfis presentes em A e/ou B, completando com `0` o lado em que o perfil não estiver presente.

## Endpoint

```
GET /api/demandas-execucao/check/perfis/{demandaTecnicaId}
```

### Path params

| Parâmetro          | Tipo   | Obrigatório | Descrição                                |
| ------------------ | ------ | ----------- | ---------------------------------------- |
| `demandaTecnicaId` | number | sim         | ID da Demanda Técnica para o check A x B |

### Respostas

- `200 OK`: lista (possivelmente vazia) de `DemandaExecucaoPerfilCheckDTO`.
- `404 Not Found`: quando a Demanda Técnica informada não existe.

## Contrato de resposta

`List<DemandaExecucaoPerfilCheckDTO>`

```ts
type DemandaExecucaoPerfilCheckDTO = {
  perfilId: number;
  perfilNome: string;
  horasPlanejadasTermo: number;     // A: total de qtdeHora em TermoPlanejamentoCusto
  horasPlanejadasExecucao: number;  // B: total de horasPlanejadas em DemandaExecucaoTarefaRecurso
};
```

> Observação: `horasPlanejadasTermo` e `horasPlanejadasExecucao` são serializados como número decimal (BigDecimal no backend). No frontend trate como `number` ou `string` numérica conforme a configuração de Jackson do projeto.

## Exemplo de uso

### Request

```
GET /api/demandas-execucao/check/perfis/42
```

### Response (200 OK)

```json
[
  {
    "perfilId": 1,
    "perfilNome": "Analista de Sistemas",
    "horasPlanejadasTermo": 80.00,
    "horasPlanejadasExecucao": 80.00
  },
  {
    "perfilId": 2,
    "perfilNome": "Desenvolvedor",
    "horasPlanejadasTermo": 100.00,
    "horasPlanejadasExecucao": 0
  },
  {
    "perfilId": 5,
    "perfilNome": "QA",
    "horasPlanejadasTermo": 0,
    "horasPlanejadasExecucao": 40.00
  }
]
```

## Regras de negócio aplicadas

1. **Lado A (Termo de Planejamento)**
   - Busca o termo de planejamento da demanda (`TermoPlanejamentoRepository.findByDemandaTecnicaId`).
   - Soma `qtdeHora` dos `TermoPlanejamentoCusto`, agrupado por `perfil.id`.
   - Se a demanda não possuir termo ou custos, A = vazio (todos os perfis somente presentes em B aparecem com `horasPlanejadasTermo = 0`).
2. **Lado B (Execução da Demanda)**
   - Busca a execução da demanda (`DemandaExecucaoRepository.findByDemandaId`).
   - Lista os recursos das tarefas (`DemandaExecucaoTarefaRecursoRepository.findByDemandaExecucaoTarefa_DemandaExecucaoId`).
   - Soma `horasPlanejadas` agrupado por `perfil.id`.
   - Se a demanda não possuir execução ou recursos, B = vazio (todos os perfis somente presentes em A aparecem com `horasPlanejadasExecucao = 0`).
3. **Consolidação**
   - União das chaves de A e B.
   - Quando o perfil aparece em apenas um dos lados, o outro lado retorna `0`.
   - O campo `perfilNome` é preenchido a partir do próprio perfil encontrado em A ou B.
4. **Recursos sem perfil**
   - `DemandaExecucaoTarefaRecurso` com `perfil = null` é **ignorado** no agrupamento de B (mesmo padrão adotado em `encerrarExecucaoInterna`).
5. **Ordenação**
   - A lista é ordenada por `perfilNome` (case-insensitive), com desempate por `perfilId`. Frontend pode reordenar livremente.

## Sugestões de uso no frontend

- Tabela "Horas Planejadas por Perfil" com colunas:

  | Perfil | Horas Termo (A) | Horas Execução (B) | Diferença (A - B) |
  | ------ | --------------- | ------------------ | ----------------- |

- Destacar visualmente perfis em que `horasPlanejadasTermo` ≠ `horasPlanejadasExecucao` (divergência entre planejado no termo e o que foi distribuído na execução).
- Considerar o campo `perfilId` como chave estável para listas/keys do framework.

## Tratamento de erros

- `404`: exibir mensagem "Demanda técnica não encontrada".
- Lista vazia (`[]`): demanda existe, porém não há custos no termo nem recursos com perfil na execução. Frontend deve renderizar estado "Sem dados para comparação".
