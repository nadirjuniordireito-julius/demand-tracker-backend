-- Execução da demanda técnica (1:1 com demanda_tecnica)
CREATE TABLE demanda_execucao (
    id BIGSERIAL PRIMARY KEY,
    demanda_tecnica_id BIGINT NOT NULL UNIQUE,
    usuario_id BIGINT,
    data_inicio_planejada DATE NOT NULL,
    data_fim_planejada DATE NOT NULL,
    data_inicio_real DATE,
    data_fim_real DATE,
    status VARCHAR(50) NOT NULL,
    percentual_progresso NUMERIC(10,2) NOT NULL,
    data_criacao_execucao TIMESTAMP NOT NULL
);

-- Tarefas da execução
CREATE TABLE demanda_execucao_tarefa (
    id BIGSERIAL PRIMARY KEY,
    demanda_execucao_id BIGINT NOT NULL,
    titulo VARCHAR(500) NOT NULL,
    descricao VARCHAR(9000),
    status VARCHAR(50) NOT NULL,
    prioridade VARCHAR(1) NOT NULL,
    data_inicio_planejada DATE NOT NULL,
    data_fim_planejada DATE NOT NULL,
    data_inicio_real DATE,
    data_fim_real DATE,
    percentual_progresso NUMERIC(10,2) NOT NULL,
    estimativa_horas NUMERIC(10,2) NOT NULL
);

-- Dependências entre tarefas (origem -> destino)
CREATE TABLE demanda_execucao_tarefa_dependencia (
    id BIGSERIAL PRIMARY KEY,
    tarefa_origem_id BIGINT NOT NULL,
    tarefa_destino_id BIGINT NOT NULL
);

-- Recursos (profissionais) por tarefa
CREATE TABLE demanda_execucao_tarefa_recurso (
    id BIGSERIAL PRIMARY KEY,
    demanda_execucao_tarefa_id BIGINT NOT NULL,
    profissional_id BIGINT NOT NULL,
    horas_planejadas NUMERIC(10,2) NOT NULL
);

-- Apontamentos de progresso por tarefa
CREATE TABLE demanda_execucao_tarefa_apontamento_progresso (
    id BIGSERIAL PRIMARY KEY,
    demanda_execucao_tarefa_id BIGINT NOT NULL,
    data DATE NOT NULL,
    percentual NUMERIC(10,2) NOT NULL,
    comentario VARCHAR(9000) NOT NULL
);

-- Índices para FKs (JPA/Hibernate gerencia FKs; índices melhoram consultas)
CREATE INDEX idx_demanda_execucao_demanda_tecnica ON demanda_execucao(demanda_tecnica_id);
CREATE INDEX idx_demanda_execucao_tarefa_execucao ON demanda_execucao_tarefa(demanda_execucao_id);
CREATE INDEX idx_dep_tarefa_origem ON demanda_execucao_tarefa_dependencia(tarefa_origem_id);
CREATE INDEX idx_dep_tarefa_destino ON demanda_execucao_tarefa_dependencia(tarefa_destino_id);
CREATE INDEX idx_recurso_tarefa ON demanda_execucao_tarefa_recurso(demanda_execucao_tarefa_id);
CREATE INDEX idx_apontamento_tarefa ON demanda_execucao_tarefa_apontamento_progresso(demanda_execucao_tarefa_id);
