# Termo de Planejamento — regras de status (integração frontend)

Documento para alinhar telas de **Termo de Planejamento**, **PDF/assinatura** e **Execução da demanda** com as validações do backend após permitir planejamento em demanda **B** ou **C**, bloqueando apenas a ida para **E** (execução) sem assinatura formal.

---

## 1. Status da demanda técnica (referência)

| Código | Descrição | Papel no planejamento |
|--------|-----------|------------------------|
| **B** | Em abertura | Pode **criar/editar** planejamento (rascunho); **não** pode assinar planejamento |
| **C** | Aberta e assinada | Pode **criar/editar** planejamento; pode assinar **se** abertura estiver assinada |
| **D** | Em planejamento | Demanda com termo de planejamento salvo; pode **editar**; pode assinar **se** abertura assinada + custos |
| **E** | Em execução | Planejamento **travado**; permite módulo de **execução** |
| **F** | Em encerramento | Planejamento travado |
| **G** | Encerrada | Planejamento travado |
| **Z** | Cancelada | Sem edição |

Após **POST/PUT** bem-sucedido em `/api/termos-planejamento`, o backend tende a mover a demanda para **D** (se estava em B ou C).

---

## 2. Matriz de permissões (UI)

Use `demanda.status` retornado nas APIs de demanda / semáforo / view do produto.

| Ação na UI | B | C | D | E | F | G | Z |
|------------|---|---|---|---|---|---|---|
| Abrir modal de planejamento | Sim | Sim | Sim | Só leitura | Só leitura | Só leitura | Não |
| Salvar planejamento (POST/PUT termo) | Sim | Sim | Sim | Não | Não | Não | Não |
| Upload/substituir PDF (`termos-planejamento-doc`) | Sim* | Sim** | Sim** | Sim*** | Não | Não | Não |
| Gerar PDF rascunho (`gerar-pdf`) | Sim | Sim | Sim | Sim | Sim | Sim | Não |
| Botão **Assinar** digital (carimbo) | Não* | Sim**** | Sim**** | Sim**** | Não | Não | Não |
| Criar **DemandaExecucao** | Não | Não | Não | Sim | Não***** | Não | Não |
| Editar tarefas/recursos da execução | Não | Não | Não | Sim | Conforme regra atual | Não | Não |

\* **POST/PUT** do PDF em B só conclui se abertura já estiver assinada e houver custos (senão 400).

\** **POST/PUT** do PDF finaliza o planejamento → demanda vai para **E**.

\*** **PUT** do PDF permitido em **E** apenas para substituir o arquivo antes da assinatura digital.

\**** Assinatura digital (`PUT .../assinar`): exige abertura assinada + custos; em **E** apenas carimba o PDF.

\***** Encerramento tem fluxo próprio.

---

## 3. Fluxo recomendado na tela

```text
[B] Em abertura
  → (opcional) usuário preenche planejamento em paralelo → status vira [D]
  → assina Termo de Abertura → [C] ou permanece [D] se já planejou
[C] Aberta e assinada
  → cria/edita planejamento → [D]
[D] Em planejamento
  → gera PDF, anexa, revisa custos
  → POST/PUT /api/termos-planejamento-doc (envia PDF) → [E]
[E] Em execução
  → (opcional) assinatura digital PUT .../assinar
  → habilita aba Execução (POST /api/demandas-execucao)
```

**Mensagem sugerida** quando assinar estiver desabilitado em B:

> Assine o Termo de Abertura antes de levar o planejamento para execução.

**Mensagem sugerida** quando execução estiver desabilitada (status ≠ E):

> Assine o Termo de Planejamento para iniciar a execução da demanda.

---

## 4. Endpoints e respostas de erro (400)

### 4.1 Termo de planejamento

| Método | Path | Quando usar |
|--------|------|-------------|
| GET | `/api/termos-planejamento/demanda/{demandaId}` | Carregar termo; **404** se ainda não existir (não tratar como erro fatal) |
| POST | `/api/termos-planejamento` | Primeira gravação (B ou C) |
| PUT | `/api/termos-planejamento/{id}` | Atualizar (B, C ou D) |
| DELETE | `/api/termos-planejamento/{id}` | Remover planejamento (B, C ou D); demanda volta para C ou B |

**Exemplos de mensagem (400):**

- Criar/alterar com status inválido:  
  `Não é permitido criar/alterar o Termo de Planejamento: a demanda deve estar com status 'B', 'C' ou 'D'...`
- Assinar sem abertura assinada:  
  `Não é permitido assinar o Termo de Planejamento: o Termo de Abertura da demanda ainda não foi assinado.`
- Assinar sem custos:  
  `Não é permitido assinar o Termo de Planejamento: informe ao menos um custo no planejamento.`
- Assinar com status errado:  
  `... demanda deve estar com status 'C' ou 'D' ...`

### 4.2 Documento PDF do planejamento

| Método | Path |
|--------|------|
| POST | `/api/termos-planejamento-doc?termoPlanejamentoId=&arquivo=` |
| PUT | `/api/termos-planejamento-doc/{id}` |
| POST/PUT | `/api/termos-planejamento-doc` | Envia/substitui PDF → demanda **E** (planejamento finalizado) |
| PUT | `/api/termos-planejamento-doc/{id}/assinar` | Assinatura digital (carimbo); demanda permanece **E** |
| DELETE | `/api/termos-planejamento-doc/{id}` |

Mesmas regras de edição do termo (B/C/D) para POST/PUT/DELETE do documento.

### 4.3 Execução da demanda

| Método | Path | Regra |
|--------|------|--------|
| POST | `/api/demandas-execucao` | Somente se `demanda.status === "E"` |

**400 exemplo:**

`Não é permitido iniciar a execução da demanda: o Termo de Planejamento deve estar assinado (status da demanda 'E' - Em execução). Status atual: D.`

---

## 5. Checklist de implementação frontend

1. **Não exigir status C** para abrir o modal de planejamento; permitir **B** e **C** (e **D** para continuar edição).
2. Tratar **GET** `/termos-planejamento/demanda/{id}`: **404** = sem termo ainda → formulário vazio + POST na primeira gravação.
3. Após salvar planejamento, atualizar `demanda.status` na store (esperar **D** se estava B/C).
4. Botão **Assinar planejamento**:
   - `disabled` se `status` ∉ {C, D}
   - `disabled` se não houver custos no termo
   - `disabled` se abertura não assinada (usar timeline tipo **A1** ou `termoAbertura.dataAssinatura`)
5. Após assinatura bem-sucedida, atualizar UI para **E** e liberar módulo de execução.
6. **Não** chamar `POST /demandas-execucao` antes da assinatura do planejamento.
7. Exibir badge de status usando descrições do enum (B, C, D, E…).
8. Em relatórios locais: valores em `totalPrevisto` podem aparecer para demandas em B/C/D — considerar rótulo **“previsto (rascunho)”** quando `status` ∉ {E, F, G}.

---

## 6. Prompt curto (copiar para o agente de frontend)

```text
Implementar regras de Termo de Planejamento alinhadas ao backend demand-tracker:

- Permitir criar e editar planejamento (POST/PUT /api/termos-planejamento e CRUD de termos-planejamento-doc) quando demanda.status for B, C ou D.
- Após salvar planejamento, esperar demanda.status = D.
- POST/PUT /api/termos-planejamento-doc: após sucesso, atualizar demanda.status para E (exige abertura assinada e custos no termo).
- Assinatura digital (PUT .../assinar): opcional após E; exige PDF já enviado.
- Bloquear POST /api/demandas-execucao até demanda.status === "E".
- GET /api/termos-planejamento/demanda/{demandaId}: 404 = sem termo, não é erro de sistema.
- Mensagens de erro 400 do backend devem ser exibidas ao usuário; desabilitar botões conforme matriz B/C/D/E na doc API-TERMO-PLANEJAMENTO-STATUS-FRONTEND.md.
```

---

## 7. Observações

- O status **D** passa a ser usado pelo backend quando existe planejamento em elaboração.
- Métricas agregadas (`totalPrevisto` por produto) ainda somam termos em B/C/D; diferencie visualmente rascunho vs. comprometido em execução (E/F).
- Dashboard legado que usa `termoPlanejamento.dataAssinatura` pode divergir do campo `demanda.status`; priorize **`demanda.status`** na UI.
