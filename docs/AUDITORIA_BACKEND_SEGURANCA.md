# Auditoria Independente – Backend e Segurança  
## Demand Tracker Backend (Spring Boot)

**Data:** Fevereiro 2026  
**Escopo:** Varredura completa pós-hardening. Contratos de API com o frontend não foram alterados.

---

## 1. Tabela de problemas

| # | Arquivo | Linha aprox. | Categoria | Descrição | Risco | Sugestão | Exemplo de correção |
|---|---------|---------------|-----------|-----------|--------|----------|---------------------|
| 1 | SecurityConfig.java | 50 | Segurança | H2-console permanece em `permitAll()` para todos os perfis. Em produção o console pode ficar acessível se o perfil não for ajustado. | **Médio** | Restringir H2 por perfil (ex.: só quando `spring.profiles.active` incluir `dev`). | `@Value("${spring.profiles.active:}") String activeProfile` e em `authorizeHttpRequests`: `.requestMatchers("/h2-console/**").access((auth, req) -> activeProfile.contains("dev") ? permit() : deny())` ou usar `@Profile("dev")` em um SecurityFilterChain alternativo. |
| 2 | application.properties | 18 | Persistência | `spring.jpa.hibernate.ddl-auto=update` permite que o Hibernate altere o schema. Em produção deve ser `validate` ou `none` com Flyway como única fonte de mudanças. | **Médio** | Em produção usar `ddl-auto=validate` (ou `none`). | Em `application-prod.properties`: `spring.jpa.hibernate.ddl-auto=validate` |
| 3 | application.properties | 27-28 | Segurança | `jwt.secret` e `jwt.expiration` em arquivo. Em produção devem vir de variáveis de ambiente. | **Médio** | Documentar e usar `${JWT_SECRET}` etc. em prod. | `jwt.secret=${JWT_SECRET:}` e garantir que JWT_SECRET esteja definida no ambiente de produção. |
| 4 | GlobalExceptionHandler.java | 94-97 | Segurança/Erros | `IllegalArgumentException` retorna `ex.getMessage()` ao cliente. Pode expor mensagens internas (ex.: validações de configuração). | **Baixo** | Tratar com mensagem genérica ou mensagem controlada. | Retornar mensagem fixa: "Requisição inválida" ou mapear apenas mensagens consideradas seguras. |
| 5 | RateLimitFilter.java | 46-47 | Segurança | Condição `path.endsWith("/api/auth/login")` pode coincidir com paths malformados ou sob subpath. Preferir comparação exata. | **Baixo** | Usar comparação exata do path. | `return path == null \|\| !"/api/auth/login".equals(path) \|\| !"POST".equalsIgnoreCase(request.getMethod());` |
| 6 | SecurityConfig.java | 75 | Segurança | CORS `setAllowedHeaders(List.of("*"))` é permissivo. Para maior restrição em prod, limitar aos headers necessários. | **Baixo** | Em produção (ou por propriedade) restringir headers. | Ex.: `configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));` (respeitando contrato com o front). |
| 7 | DocumentoService.java / Termo*Controller | várias | Erros | Uso de `RuntimeException` com `e.getMessage()` em cadeia. O cliente recebe mensagem genérica via Exception handler, mas a mensagem pode aparecer em logs do servidor. | **Baixo** | Evitar incluir detalhes de exceção interna em mensagens; logar exceção com nível apropriado sem expor ao cliente. | Lançar `BusinessException` ou similar com mensagem genérica; logar a causa com `log.warn("Falha ao gerar PDF", e)` sem colocar `e.getMessage()` na resposta. |
| 8 | N/A (testes) | - | Testes | Cobertura limitada: apenas `DemandaAvaliacaoServiceTest`. Ausência de testes para segurança, controllers, integração com DB e outros services. | **Médio** | Aumentar cobertura (services críticos, fluxos de auth, integração). | Adicionar testes unitários para AuthService, JwtService; testes de integração para `POST /api/auth/login` e para endpoints protegidos. |

**Observação:** Nenhum log expõe password ou token (apenas método, path, status, duração, correlationId). `UsuarioDTO.fromEntity` não inclui password. Queries usam JPQL com parâmetros nomeados; não há `nativeQuery` com concatenação.

---

## 2. Checklist por categoria e % de conformidade

| Categoria | Item | Conforme? | % |
|-----------|------|-----------|---|
| **1) Segurança** | Autenticação (JWT) e autorização (authenticated vs permitAll) | Sim | 100 |
| | Tokens/senhas não expostos em logs | Sim | 100 |
| | Filtros (CorrelationId, RateLimit, SecurityHeaders, RequestLogging) seguros | Sim | 100 |
| | CORS configurável por propriedade | Sim | 100 |
| | CSRF desabilitado (API stateless JWT – aceitável) | Sim | 100 |
| | Headers de segurança aplicados | Sim | 100 |
| | Rate limiting no login | Sim | 100 |
| | H2-console restrito por perfil | Não | 0 |
| | JWT secret em env em prod | Não (arquivo) | 0 |
| **Subtotal Segurança** | | | **~78%** |
| **2) Validação** | DTOs com Bean Validation onde aplicável | Sim | 100 |
| | Sanitização em service (InputSanitizer, textos avaliação) | Sim | 100 |
| | Rejeição de JSON malformado (HttpMessageNotReadable) | Sim | 100 |
| **Subtotal Validação** | | | **100%** |
| **3) Tratamento de erros** | @ControllerAdvice global | Sim | 100 |
| | Respostas padronizadas (ErrorResponse) | Sim | 100 |
| | Sem stack trace na resposta 500 | Sim | 100 |
| | IllegalArgumentException com mensagem controlada | Não | 0 |
| **Subtotal Erros** | | | **~75%** |
| **4) Arquitetura** | Separação controller / service / repository | Sim | 100 |
| | Controllers sem regra de negócio | Sim | 100 |
| | Repositórios sem lógica de negócio | Sim | 100 |
| | Transações (@Transactional) nos services | Sim | 100 |
| **Subtotal Arquitetura** | | | **100%** |
| **5) Persistência** | Queries com parâmetros nomeados (JPQL) | Sim | 100 |
| | Atenção a N+1 (fetch texto em DemandaAvaliacao) | Sim | 100 |
| | ddl-auto seguro em produção | Não (update) | 0 |
| **Subtotal Persistência** | | | **~67%** |
| **6) Performance** | Pool de conexões (HikariCP) configurado | Sim | 100 |
| | Cache habilitado (ConcurrentMap, pronto para @Cacheable) | Sim | 100 |
| | Async onde necessário | Não aplicado / parcial | - |
| **Subtotal Performance** | | | **100%** |
| **7) Observabilidade** | Correlation ID (header + MDC) | Sim | 100 |
| | Log por requisição (método, path, status, duração) | Sim | 100 |
| | Pattern de log com correlationId | Sim | 100 |
| | Health e metrics (Actuator) | Sim | 100 |
| **Subtotal Observabilidade** | | | **100%** |
| **8) Testes** | Testes unitários (DemandaAvaliacaoService) | Parcial | ~20 |
| | Testes de integração | Não | 0 |
| | Cobertura mínima aceitável para produção | Não | 0 |
| **Subtotal Testes** | | | **~7%** |

**Conformidade geral ponderada (estimada):** ~82% (ponderando testes e segurança como críticos).

---

## 3. Nota final de maturidade: **7,0 / 10**

- **Pontos fortes:** Segurança (JWT, rate limit, headers, CORS, sem vazamento de credenciais), validação e sanitização, tratamento de erros centralizado, arquitetura em camadas, transações, observabilidade (correlation ID, log, Actuator), persistência com parâmetros nomeados e cuidado com N+1.
- **Pontos fracos:** H2 e ddl-auto em produção, secret em arquivo, testes insuficientes, detalhe de IllegalArgumentException na resposta e CORS muito permissivo em headers.

---

## 4. Top 3 riscos restantes

1. **H2-console e ddl-auto em produção**  
   Se o perfil de produção não desabilitar o console e não trocar `ddl-auto` para `validate`/`none`, há risco de acesso ao console e de alteração de schema fora do controle (Flyway).

2. **Cobertura de testes muito baixa**  
   Quase só um teste de service. Falta testes de integração (login, endpoints protegidos), de segurança (rate limit, JWT inválido) e de outros fluxos críticos, o que aumenta o risco de regressões e bugs em produção.

3. **Credenciais e configuração sensível**  
   JWT secret e dados de conexão em `application.properties`. Em produção devem vir de variáveis de ambiente ou de um vault, com perfil prod garantido e sem commit de segredos.

---

## 5. Veredito: **Pronto para produção?**

**Resposta: Condicionalmente sim, desde que sejam aplicados os ajustes de produção abaixo.**

- **Sim**, no sentido de que: hardening foi aplicado, não há exposição de stack trace nem de senha/token em resposta ou log, há rate limit no login, headers de segurança, validação e sanitização, e a arquitetura e a observabilidade estão em bom nível para um primeiro deploy controlado.
- **Condições para considerar “pronto para produção” de forma segura:**
  1. **Perfil prod obrigatório:** `spring.profiles.active=prod` e em `application-prod.properties`: `spring.jpa.hibernate.ddl-auto=validate`.
  2. **H2-console:** não acessível em produção (remover `/h2-console/**` do `permitAll` quando o perfil for prod, ou usar regra condicional por perfil).
  3. **JWT e DB:** `jwt.secret` (e se aplicável `jwt.expiration`) e credenciais do banco via variáveis de ambiente, não em arquivo versionado.
  4. **Testes:** pelo menos testes de integração para login e um fluxo crítico (ex.: criação de demanda ou de avaliação) e, idealmente, mais testes unitários em services críticos e em segurança.

Com essas condições atendidas, o backend pode ser considerado **pronto para produção** em ambiente corporativo, com o entendimento de que a baixa cobertura de testes continua sendo um risco residual a ser reduzido de forma contínua.
