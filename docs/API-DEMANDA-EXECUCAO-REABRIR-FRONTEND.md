# API - Reabrir Execução de Demanda Técnica

## Objetivo

Permitir que uma execução previamente **encerrada** (`DemandaExecucao.status = "CONCLUIDA"`) volte a ser editável pelo usuário, revertendo os efeitos de estado aplicados pelo encerramento (`POST /api/demandas-execucao/{id}/encerrar`).

## Endpoint

```
POST /api/demandas-execucao/{id}/reabrir
```

### Path params

| Parâmetro | Tipo   | Obrigatório | Descrição                                |
| --------- | ------ | ----------- | ---------------------------------------- |
| `id`      | number | sim         | ID da `DemandaExecucao` a ser reaberta. |

### Body

Sem body.

### Respostas

- `200 OK`: retorna o `DemandaExecucaoDTO` atualizado com os novos status.
- `400 Bad Request`: quando a reabertura não é permitida (ver regras abaixo).
- `404 Not Found`: quando a `DemandaExecucao` informada não existe.

## Contrato de resposta

`DemandaExecucaoDTO` (mesmo contrato já documentado em `API-DEMANDA-EXECUCAO-FRONTEND.md`).

Após reabertura, os campos relevantes ficam assim:

```json
{
  "id": 3,
  "status": "EM_ANDAMENTO",
  "percentualProgresso": 100.00,
  "demanda": {
    "id": 42,
    "status": "E",
    "...": "..."
  }
}
```

> Observação: `percentualProgresso` **não é alterado** automaticamente pela reabertura. Caso o frontend deseje exibir um valor diferente quando a execução estiver em andamento, basta editar via `PUT /api/demandas-execucao/{id}`.

## Regras de negócio

A reabertura é o inverso do `encerrarExecucaoInterna` no que tange a **estados**:

| Item                              | No encerramento (POST .../encerrar) | Na reabertura (POST .../reabrir) |
| --------------------------------- | ----------------------------------- | -------------------------------- |
| `DemandaExecucao.status`          | `CONCLUIDA`                         | `EM_ANDAMENTO`                  |
| `DemandaExecucao.percentual...`   | `100.00`                            | (mantido, não alterado)         |
| `DemandaTecnica.status`           | `F` (Em encerramento)               | `E` (Em execução)               |
| `TermoEncerramento`               | criado/atualizado (idempotente)    | **mantido como está**           |

### Por que o Termo de Encerramento não é apagado?

O `encerrarExecucaoInterna` é idempotente: se o termo já existir para a demanda, ele é **atualizado** com os custos atuais; caso contrário, é criado. Portanto, se o usuário reabrir, ajustar tarefas/recursos e encerrar novamente, o termo existente será atualizado automaticamente — sem necessidade de excluí-lo na reabertura. Essa abordagem evita efeitos colaterais em anexos/custos/profissionais já vinculados.

### Bloqueios

A reabertura é **rejeitada** (HTTP 400) nos seguintes casos:

1. **Demanda com Termo de Encerramento assinado** — `DemandaTecnica.status == "G"` (Encerrada).
   Mensagem:
   `"Não é possível reabrir a execução: o Termo de Encerramento já foi assinado (demanda com status 'G' - Encerrada)."`
2. **Execução não está concluída** — `DemandaExecucao.status != "CONCLUIDA"`.
   Mensagem:
   `"Apenas execuções com status CONCLUIDA podem ser reabertas. Status atual: <status>"`
3. **Execução sem demanda técnica vinculada** (caso de inconsistência de dados):
   `"Execução ID <id> não está vinculada a uma Demanda Técnica."`

## Exemplo de uso

### Request

```http
POST /api/demandas-execucao/3/reabrir
```

### Response (200 OK)

```json
{
  "id": 3,
  "status": "EM_ANDAMENTO",
  "percentualProgresso": 100.00,
  "demanda": {
    "id": 42,
    "codigo": "DT-2026-0042",
    "status": "E"
  }
}
```

### Response (400 Bad Request - termo assinado)

```json
{
  "timestamp": "2026-04-28T17:10:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Não é possível reabrir a execução: o Termo de Encerramento já foi assinado (demanda com status 'G' - Encerrada).",
  "path": "/api/demandas-execucao/3/reabrir"
}
```

## Sugestões de UX no frontend

- O botão "Reabrir execução" deve ser **mostrado** apenas quando `execucao.status === "CONCLUIDA"` **e** `demanda.status !== "G"`.
- Após sucesso, recarregar `GET /api/demandas-execucao/demanda/{demandaTecnicaId}` (ou o id direto) para atualizar visões dependentes (Gantt, lista de tarefas/recursos etc.).
- Importante orientar o usuário: o **Termo de Encerramento** continua existindo. Se ele encerrar novamente após editar, o termo será atualizado com os custos novos.
- Caso o frontend bloqueie edição de tarefas/recursos quando `execucao.status === "CONCLUIDA"`, basta reagir ao novo status `EM_ANDAMENTO` para reabilitar.
