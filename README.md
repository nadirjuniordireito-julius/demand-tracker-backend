# Demand Tracker Backend

Backend API REST desenvolvido em Java Spring Boot para o sistema de gestão de demandas técnicas.

## Tecnologias

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- JWT (JSON Web Token)
- H2 Database (desenvolvimento)
- PostgreSQL (produção)
- Maven

## Estrutura do Projeto

```
src/main/java/com/demandtracker/
├── config/          # Configurações (Security, Web)
├── controller/      # Controllers REST
├── dto/            # Data Transfer Objects
├── entity/          # Entidades JPA
├── exception/       # Tratamento de exceções
├── repository/      # Repositories JPA
├── security/        # Configurações de segurança
└── service/         # Lógica de negócio
```

## Endpoints da API

### Autenticação
- `POST /api/auth/login` - Login
- `GET /api/auth/me` - Usuário atual

### Usuários
- `GET /api/usuarios` - Listar usuários
- `GET /api/usuarios/{id}` - Buscar por ID
- `POST /api/usuarios` - Criar usuário
- `PUT /api/usuarios/{id}` - Atualizar usuário
- `DELETE /api/usuarios/{id}` - Deletar usuário

### Projetos
- `GET /api/projetos` - Listar projetos
- `GET /api/projetos/{id}` - Buscar por ID
- `POST /api/projetos` - Criar projeto
- `PUT /api/projetos/{id}` - Atualizar projeto
- `DELETE /api/projetos/{id}` - Deletar projeto

### Perfis
- `GET /api/perfis` - Listar perfis
- `GET /api/perfis/{id}` - Buscar por ID
- `POST /api/perfis` - Criar perfil
- `PUT /api/perfis/{id}` - Atualizar perfil
- `DELETE /api/perfis/{id}` - Deletar perfil

### Demandas
- `GET /api/demandas` - Listar demandas
- `GET /api/demandas/{id}` - Buscar por ID
- `POST /api/demandas` - Criar demanda
- `PUT /api/demandas/{id}` - Atualizar demanda
- `DELETE /api/demandas/{id}` - Deletar demanda

### Termos de Abertura
- `GET /api/termos-abertura` - Listar termos
- `GET /api/termos-abertura/{id}` - Buscar por ID
- `GET /api/termos-abertura/demanda/{demandaId}` - Buscar por demanda
- `POST /api/termos-abertura` - Criar termo
- `PUT /api/termos-abertura/{id}` - Atualizar termo
- `POST /api/termos-abertura/{id}/assinar` - Assinar termo
- `DELETE /api/termos-abertura/{id}` - Deletar termo

### Termos de Planejamento
- `GET /api/termos-planejamento` - Listar termos
- `GET /api/termos-planejamento/{id}` - Buscar por ID
- `GET /api/termos-planejamento/demanda/{demandaId}` - Buscar por demanda
- `POST /api/termos-planejamento` - Criar termo
- `PUT /api/termos-planejamento/{id}` - Atualizar termo
- `POST /api/termos-planejamento/{id}/assinar` - Assinar termo
- `DELETE /api/termos-planejamento/{id}` - Deletar termo

### Termos de Encerramento
- `GET /api/termos-encerramento` - Listar termos
- `GET /api/termos-encerramento/{id}` - Buscar por ID
- `GET /api/termos-encerramento/demanda/{demandaId}` - Buscar por demanda
- `POST /api/termos-encerramento` - Criar termo
- `PUT /api/termos-encerramento/{id}` - Atualizar termo
- `POST /api/termos-encerramento/{id}/assinar` - Assinar termo
- `DELETE /api/termos-encerramento/{id}` - Deletar termo

### Dashboard
- `GET /api/dashboard/stats` - Estatísticas gerais
- `GET /api/dashboard/demandas-por-projeto` - Demandas por projeto
- `GET /api/dashboard/demandas-por-status` - Demandas por status

## Configuração

### application.properties

```properties
server.port=8080
spring.datasource.url=jdbc:h2:mem:demandtracker
jwt.secret=your-secret-key
jwt.expiration=3600000
cors.allowed-origins=http://localhost:5173
```

## Execução

```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080/api`

## Autenticação

Todas as rotas (exceto `/api/auth/**`) requerem autenticação via JWT.

Envie o token no header:
```
Authorization: Bearer <token>
```
