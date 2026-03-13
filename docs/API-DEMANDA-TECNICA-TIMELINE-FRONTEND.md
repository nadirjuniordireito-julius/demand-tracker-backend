# API Timeline da Demanda Técnica – Documentação para Frontend

Documento para implementação da **timeline (rastro)** da demanda técnica no frontend, com a sequência de eventos desde a criação até o encerramento.

---

## 1. Visão geral

A timeline retorna uma lista ordenada de eventos da demanda técnica, formada a partir das tabelas:

- **DemandaTecnica** (criação)
- **TermoAbertura** e **TermoAberturaDoc** (abertura e assinatura)
- **TermoPlanejamento** e **TermoPlanejamentoDoc** (planejamento e assinatura)
- **TermoEncerramento** e **TermoEncerramentoDoc** (encerramento e assinatura)

Cada item da resposta contém: **sequência**, **data/hora do evento**, **usuário** (com dados e indicação de foto) e **tipo do evento**.

---

## 2. Endpoint

### Obter timeline por ID da demanda técnica

**GET** `/api/demandas/{id}/timeline`

| Parâmetro | Tipo | Obrigatório | Descrição                    |
|-----------|------|-------------|------------------------------|
| `id`      | long | **Sim**     | ID da demanda técnica        |

**Exemplo de requisição:**

```http
GET /api/demandas/42/timeline
Authorization: Bearer <token>
```

**Resposta:** `200 OK`  
Corpo: array de objetos `DemandaTecnicaTimelineItemDTO`, ordenados por data/hora do evento, com `sequencia` 1, 2, 3, ...

**Exemplo de resposta:**

```json
[
  {
    "sequencia": 1,
    "dataHoraEvento": "2026-02-01T10:00:00",
    "usuario": {
      "id": 5,
      "nome": "Maria Silva",
      "email": "maria@exemplo.com",
      "fotoUrl": "/api/usuario-foto/usuario/5/download"
    },
    "tipoEvento": "C"
  },
  {
    "sequencia": 2,
    "dataHoraEvento": "2026-02-02T14:30:00",
    "usuario": {
      "id": 5,
      "nome": "Maria Silva",
      "email": "maria@exemplo.com",
      "fotoUrl": "/api/usuario-foto/usuario/5/download"
    },
    "tipoEvento": "A"
  },
  {
    "sequencia": 3,
    "dataHoraEvento": "2026-02-03T09:15:00",
    "usuario": {
      "id": 3,
      "nome": "João Santos",
      "email": "joao@exemplo.com",
      "fotoUrl": null
    },
    "tipoEvento": "A1"
  }
]
```

Se a demanda não existir: `404 Not Found` (corpo com mensagem de recurso não encontrado).

---

## 3. Estrutura dos dados

### 3.1 Item da timeline (`DemandaTecnicaTimelineItemDTO`)

| Campo           | Tipo     | Descrição                                                                 |
|-----------------|----------|----------------------------------------------------------------------------|
| `sequencia`    | integer  | Ordem do evento (1, 2, 3, …).                                              |
| `dataHoraEvento` | string (ISO 8601) | Data e hora do evento (ex.: `2026-02-01T10:00:00`).              |
| `usuario`      | object ou `null` | Usuário associado ao evento; `null` se não houver (ex.: assinatura sem usuário). |
| `tipoEvento`    | string   | Código do tipo do evento (ver tabela abaixo).                             |

### 3.2 Usuário na timeline (`UsuarioTimelineDTO`)

| Campo     | Tipo   | Descrição                                                                 |
|-----------|--------|----------------------------------------------------------------------------|
| `id`      | long   | ID do usuário.                                                            |
| `nome`    | string | Nome do usuário.                                                          |
| `email`   | string | E-mail do usuário.                                                        |
| `fotoUrl` | string ou `null` | Caminho para obter a foto: `GET {baseUrl}/api/usuario-foto/usuario/{id}/download`. Nulo se o usuário não tiver foto em `UsuarioFoto`. |

Para exibir a foto no frontend: se `fotoUrl` não for nulo, use `{API_BASE}{fotoUrl}` (ex.: `https://api.exemplo.com/api/usuario-foto/usuario/5/download`). Requisição deve enviar o mesmo JWT das demais APIs.

### 3.3 Tipos de evento (`tipoEvento`)

| Código | Descrição                              |
|--------|----------------------------------------|
| **C**  | Criação da demanda técnica             |
| **A**  | Termo de abertura da demanda           |
| **A1** | Assinatura do termo de abertura        |
| **B**  | Termo de planejamento                  |
| **B1** | Assinatura do termo de planejamento   |
| **E**  | Termo de encerramento                   |
| **E1** | Assinatura do termo de encerramento   |

A ordem dos eventos na resposta segue a ordem cronológica (e a `sequencia` reflete essa ordem). Só entram na lista eventos que existem (ex.: se não houver termo de encerramento, não haverá itens com tipo E ou E1).

---

## 4. Uso no frontend

1. Receber o **ID da demanda técnica** (ex.: da tela de detalhe ou da lista).
2. Chamar **GET** `/api/demandas/{id}/timeline` com o header de autenticação.
3. Renderizar a lista em ordem (ou usar `sequencia` para numeração).
4. Para cada item:
   - Exibir `dataHoraEvento` formatada.
   - Exibir label do `tipoEvento` conforme a tabela acima.
   - Exibir `usuario.nome` (e opcionalmente `usuario.email`).
   - Se `usuario.fotoUrl` não for nulo, usar como `src` da imagem (URL completa com base da API).

---

## 5. Autenticação

Todas as rotas utilizam o mesmo padrão do backend: **JWT** no header:

```http
Authorization: Bearer <access_token>
```
