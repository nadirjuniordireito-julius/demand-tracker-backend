# Alteração de API – `custoTotalMensal` em Profissional

Relatório para o time de frontend ajustar cadastro, edição, listagens e tipos TypeScript relacionados a **Profissional**.

## Objetivo

Incluir o campo **`custoTotalMensal`**: valor do custo total mensal do profissional/empresa, obrigatório no banco (`NUMERIC(10,2)`), espelhado em criação, atualização e resposta da API.

## Backend alterado

- **Entidade:** `Profissional` – campo `custoTotalMensal` (`BigDecimal`, `nullable = false`, `precision = 10`, `scale = 2`).
- **Coluna SQL:** `profissional.custo_total_mensal`.
- **Migration Flyway:** `V14__profissional_add_custo_total_mensal.sql`
  - Linhas já existentes recebem **0** na migração; o frontend deve permitir exibir/editar o valor real após deploy.

## Endpoints impactados

| Método | Rota | Impacto |
|--------|------|---------|
| GET | `/api/profissionais` | Cada item em `content` inclui `custoTotalMensal`. |
| GET | `/api/profissionais/{id}` | Resposta inclui `custoTotalMensal`. |
| POST | `/api/profissionais` | Body **obrigatório:** `custoTotalMensal`. Resposta inclui o campo. |
| PUT | `/api/profissionais/{id}` | Body **opcional:** `custoTotalMensal` (atualização parcial). |

## Contratos aninhados

Qualquer JSON que inclua **`ProfissionalDTO`** completo passa a ter `custoTotalMensal`, por exemplo:

- `ProfissionalCustoMensalDTO.profissional`
- `DemandaExecucaoTarefaRecursoDTO.profissional`

Ajustar interfaces TypeScript desses DTOs se forem tipadas de forma explícita (ou usar um tipo `ProfissionalDTO` compartilhado).

---

## Request – criação (`ProfissionalCreateDTO`)

| Campo | Tipo | Obrigatório | Observação |
|--------|------|-------------|------------|
| `custoTotalMensal` | `number` | Sim | Decimal; até 10 dígitos no total, 2 casas decimais (alinhado a `valorHora`). |

**Exemplo:**

```json
{
  "nome": "Maria Silva",
  "tipoPessoa": "F",
  "documento": "12345678901",
  "valorHora": 150.5,
  "custoTotalMensal": 12000.0,
  "dataInicioAtividade": "2026-01-15",
  "funcao": "Desenvolvedor",
  "projetoId": 1,
  "perfilId": 1
}
```

Validação backend: `@NotNull` – mensagem típica em `errors.custoTotalMensal`: *"Custo total mensal é obrigatório"*.

---

## Request – atualização (`ProfissionalUpdateDTO`)

| Campo | Tipo | Obrigatório |
|--------|------|-------------|
| `custoTotalMensal` | `number` | Não (parcial) |

Se omitido, o valor atual no banco **não** é alterado.

---

## Response (`ProfissionalDTO`)

| Campo | Tipo | Sempre presente |
|--------|------|-----------------|
| `custoTotalMensal` | `number` | Sim (não nulo na API após migração) |

**Exemplo (trecho):**

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "tipoPessoa": "F",
  "documento": "12345678901",
  "valorHora": 150.5,
  "custoTotalMensal": 12000.0,
  "dataInicioAtividade": "2026-01-15",
  "funcao": "Desenvolvedor",
  "projetoId": 1,
  "perfilId": 1
}
```

---

## Checklist frontend

1. **Tipagem** – Incluir `custoTotalMensal: number` em `ProfissionalDTO` / tipos de lista e detalhe.
2. **Formulário de criação** – Campo obrigatório; label sugerida: *Custo total mensal* (ou equivalente de negócio).
3. **Formulário de edição** – Campo opcional no PATCH semântico se enviar só campos alterados; se o formulário for “tela cheia”, pode enviar sempre o valor atual.
4. **Tabelas / cards** – Coluna ou resumo opcional conforme UX (custos mensais).
5. **Telas que aninham profissional** – Atualizar mocks, fixtures e tipos em custo mensal e recursos de execução.
6. **Dados legados** – Após migração, registros antigos podem vir com `0` até o usuário corrigir; considerar aviso na UI na primeira edição se desejado.

---

## Documentação geral

O arquivo `docs/API-PROFISSIONAL-FRONTEND.md` foi alinhado com este contrato (campos, exemplos e interfaces sugeridas).
