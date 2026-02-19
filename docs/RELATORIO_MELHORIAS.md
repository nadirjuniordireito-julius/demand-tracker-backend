# Relatório de Melhorias – Demand Tracker Backend

**Data:** Fevereiro 2026  
**Escopo:** Refatoração e endurecimento para padrão de produção, sem alteração de contratos de API (URLs, métodos, payloads de request/response).

---

## 1. Regra absoluta respeitada

- **Nenhuma URL foi alterada.**
- **Nenhum método HTTP (GET, POST, PUT, DELETE, etc.) foi alterado.**
- **Payloads de request e response mantidos** (mesmos campos e nomes usados pelo frontend).
- Qualquer melhoria que exigisse mudança de contrato foi feita por adaptação interna (ex.: CORS lido de propriedade, mesma forma de resposta de erro).

---

## 2. Melhorias implementadas

### 2.1 Segurança

| Item | Implementação | Arquivos |
|------|----------------|----------|
| **Headers de segurança** | Filtro que adiciona em todas as respostas: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY` (exceto H2-console), `X-XSS-Protection`, `Referrer-Policy`. Opcional: `Strict-Transport-Security` em produção. | `SecurityHeadersFilter.java` |
| **Rate limiting (login)** | Limite de tentativas por IP em `POST /api/auth/login`. Resposta 429 com mesmo formato `ErrorResponse`. Configurável: `app.rate-limit.login.max-attempts`, `app.rate-limit.login.window-seconds`. | `RateLimitFilter.java`, `SecurityConfig.java` |
| **Correlation ID** | Header `X-Correlation-Id` em toda resposta (propagado ou gerado). | `CorrelationIdFilter.java` |
| **CORS** | Origens lidas de `cors.allowed-origins` (application.properties). Comportamento do front inalterado. | `SecurityConfig.java` |
| **Password hashing** | BCrypt com strength 12. | `SecurityConfig.java` |
| **JWT** | Validação de tamanho mínimo do secret (32 caracteres) na subida; uso de UTF-8 para a chave. | `JwtService.java` |
| **Resposta de erro em falha de autenticação** | `UsernameNotFoundException` e `BadCredentialsException` tratados no `GlobalExceptionHandler` com mensagem genérica "Credenciais inválidas" (evita user enumeration). | `GlobalExceptionHandler.java` |
| **Health check público** | `GET /actuator/health` permitido sem autenticação (para balanceadores e monitoramento). Demais endpoints do Actuator exigem autenticação. | `SecurityConfig.java` |

### 2.2 Tratamento de erros

| Item | Implementação | Arquivos |
|------|----------------|----------|
| **ControllerAdvice global** | Já existia. Mantido e ampliado. | `GlobalExceptionHandler.java` |
| **Resposta padronizada** | `ErrorResponse`: `message`, `status`, `timestamp`, `errors` (opcional). Sem alteração de contrato. | `ErrorResponse.java` |
| **Não expor stack trace** | Em `Exception.class` a mensagem retornada é genérica: "Erro interno do servidor". Nenhum detalhe interno ou stack é enviado ao cliente. | `GlobalExceptionHandler.java` |
| **JSON inválido** | `HttpMessageNotReadableException` tratada com mensagem amigável e dica de formato de data (ISO-8601). | `GlobalExceptionHandler.java` |

### 2.3 Configuração e desempenho

| Item | Implementação | Arquivos |
|------|----------------|----------|
| **Pool de conexões (HikariCP)** | Configurado: `maximum-pool-size`, `minimum-idle`, `connection-timeout`, `idle-timeout`, `max-lifetime`. | `application.properties` |
| **Actuator** | Exposição de `health` e `metrics`. `show-details` do health: `when-authorized`. | `application.properties`, `pom.xml` |
| **Logging** | Níveis ajustados (root WARN, app INFO, security WARN). Perfil `prod` com logging mais restritivo. | `application.properties`, `application-prod.properties` |
| **Propriedades de segurança** | `app.security.headers.strict-transport`, `app.rate-limit.login.*` para uso em produção. | `application.properties`, `application-prod.properties` |

### 2.4 Validação e sanitização

| Item | Implementação | Arquivos |
|------|----------------|----------|
| **Utilitário de sanitização** | `InputSanitizer`: `trimToNull`, `trimAndMaxLength` para uso interno em services. | `util/InputSanitizer.java` |
| **Textos da avaliação** | Campos de texto (causaAtraso, gargalo, etc.) trimados e limitados a 10.000 caracteres antes de persistir. Sem alteração de contrato. | `DemandaAvaliacaoService.java` |

### 2.5 Persistência

| Item | Implementação | Arquivos |
|------|----------------|----------|
| **Queries seguras** | Revisão: todos os repositórios usam JPQL com parâmetros nomeados (`:param`). Nenhuma concatenação de string em queries. | Repositórios (já existente) |
| **N+1** | DemandaAvaliacao carregada com `LEFT JOIN FETCH a.texto`; riscos lazy. Evita múltiplos SELECTs desnecessários. | `DemandaAvaliacaoRepository.java` (já existente) |

### 2.6 Performance

| Item | Implementação | Arquivos |
|------|----------------|----------|
| **Cache** | `@EnableCaching` e `ConcurrentMapCacheManager`. Uso: `@Cacheable("nomeCache")` em métodos de service quando fizer sentido (ex.: listas read-only). Para produção com Redis: `spring.cache.type=redis`. | `CacheConfig.java`, `pom.xml` (spring-boot-starter-cache) |

### 2.7 Observabilidade

| Item | Implementação | Arquivos |
|------|----------------|----------|
| **Correlation ID no MDC** | Correlation ID colocado no MDC em cada requisição para aparecer em todos os logs daquele request. | `CorrelationIdFilter.java` |
| **Log por requisição** | Filtro que loga após cada request: método, path, status HTTP, duração (ms), correlationId. Não loga corpo. | `RequestLoggingFilter.java`, `SecurityConfig.java` |
| **Pattern de log** | Console inclui `[%X{correlationId:-}]` para correlacionar linhas de log à requisição. | `application.properties` (logging.pattern.console) |

---

## 3. Riscos mitigados

| Risco | Mitigação |
|-------|------------|
| Ataque de brute force no login | Rate limiting por IP no endpoint de login. |
| User enumeration | Mensagem única em falha de autenticação. |
| Exposição de detalhes internos em 500 | Mensagem genérica; stack trace não enviado ao cliente. |
| CORS permissivo demais | Origens configuráveis por propriedade (restritivo em prod). |
| Headers de segurança ausentes | Filtro aplica headers em todas as respostas. |
| JWT com secret fraco | Validação de tamanho mínimo na inicialização. |
| Rastreio de requisições | Correlation ID em header para correlação em logs/métricas. |
| JSON malformado / formato de data | Tratamento específico com mensagem e dica de formato. |
| Textos muito longos / armazenamento | Sanitização com limite de 10k caracteres nos campos de texto da avaliação. |

---

## 4. Pontos não alterados (para não quebrar contrato)

- **URLs e métodos** de todos os endpoints mantidos.
- **Corpo de request/response** inalterado (incluindo nomes de campos como `texto`/`textos`, `riscos`, etc.).
- **CORS** continua permitindo os mesmos métodos e headers que o front usa (`AllowedHeaders: *` mantido).
- **H2-console** continua permitido e com frame options liberados onde necessário para não quebrar uso em desenvolvimento.
- **Estrutura de `ErrorResponse`** mantida; nenhum campo novo obrigatório adicionado (apenas tratamento de mais exceções com o mesmo formato).

---

## 5. Arquitetura (estado atual)

- **Arquitetura:** Projeto segue controller → service → repository. Regras de negócio em services; repositórios sem lógica de negócio. Nenhuma alteração necessária.
- **Validação:** DTOs com Bean Validation; sanitização em service (InputSanitizer) para textos da avaliação.
- **Observabilidade:** Correlation ID (header + MDC), log por requisição (método, path, status, duração), pattern de log com correlationId, Actuator health/metrics.

---

## 6. Como usar em produção

1. **Perfil:** Subir com `spring.profiles.active=prod`.
2. **CORS:** Definir `cors.allowed-origins` com as origens reais (ex.: `https://app.empresa.com`).
3. **JWT:** Usar `jwt.secret` com pelo menos 32 caracteres e valor seguro (variável de ambiente recomendada).
4. **H2:** Em produção, não expor `/h2-console` (remover do `permitAll` ou desabilitar em prod).
5. **Rate limit:** Ajustar `app.rate-limit.login.max-attempts` e `window-seconds` conforme política de segurança.

---

## 7. Resumo – Todas as fases

- **Segurança:** Headers, rate limit no login, CORS configurável, BCrypt 12, validação de JWT secret, resposta genérica em falha de auth.
- **Erros:** ControllerAdvice único, resposta padronizada, sem stack trace em 500, tratamento de JSON inválido.
- **Validação:** InputSanitizer (trim + max length); textos da avaliação limitados a 10k caracteres.
- **Persistência:** Queries com parâmetros nomeados; fetch de texto na avaliação para evitar N+1.
- **Performance:** HikariCP configurado; cache habilitado (ConcurrentMap), pronto para @Cacheable onde fizer sentido.
- **Observabilidade:** Correlation ID (header + MDC), RequestLoggingFilter (método, path, status, durationMs), pattern de log com correlationId, Actuator health/metrics.
- **Contrato de API:** Preservado em todos os pontos; frontend não precisa de alterações.
