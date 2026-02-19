-- Módulo de Avaliação de Qualidade da Demanda Técnica
CREATE TABLE IF NOT EXISTS demanda_avaliacao (
    id BIGSERIAL PRIMARY KEY,
    demanda_tecnica_id BIGINT NOT NULL UNIQUE,
    data_encerramento DATE,
    atraso BOOLEAN,
    desvio_prazo_percentual NUMERIC(10, 2),
    impacto_atraso INTEGER,
    desvio_custo_percentual NUMERIC(10, 2),
    impacto_financeiro INTEGER,
    atendimento_requisitos INTEGER,
    estabilidade INTEGER,
    retrabalho INTEGER,
    satisfacao_usuario INTEGER,
    clareza_requisitos INTEGER,
    qualidade_planejamento INTEGER,
    aderencia_cronograma INTEGER,
    comunicacao INTEGER,
    capacidade_equipe INTEGER,
    disponibilidade_equipe INTEGER,
    possui_backup_critico BOOLEAN,
    rotatividade_impactou BOOLEAN,
    valor_percebido INTEGER,
    alinhamento_meta INTEGER,
    reutilizacao VARCHAR(10),
    avaliacao_geral INTEGER,
    repetiria_modelo BOOLEAN,
    indice_saude NUMERIC(10, 4),
    indice_confiabilidade NUMERIC(10, 4),
    indice_risco NUMERIC(10, 4),
    CONSTRAINT fk_demanda_avaliacao_demanda FOREIGN KEY (demanda_tecnica_id) REFERENCES demandas_tecnicas(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demanda_avaliacao_risco (
    id BIGSERIAL PRIMARY KEY,
    avaliacao_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    CONSTRAINT fk_demanda_avaliacao_risco_avaliacao FOREIGN KEY (avaliacao_id) REFERENCES demanda_avaliacao(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demanda_avaliacao_texto (
    id BIGSERIAL PRIMARY KEY,
    avaliacao_id BIGINT NOT NULL UNIQUE,
    causa_atraso TEXT,
    causa_custo TEXT,
    gargalo TEXT,
    impacto_equipe TEXT,
    correcoes TEXT,
    licoes_positivas TEXT,
    licoes_negativas TEXT,
    melhorias TEXT,
    CONSTRAINT fk_demanda_avaliacao_texto_avaliacao FOREIGN KEY (avaliacao_id) REFERENCES demanda_avaliacao(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_demanda_avaliacao_demanda_id ON demanda_avaliacao(demanda_tecnica_id);
CREATE INDEX IF NOT EXISTS idx_demanda_avaliacao_risco_avaliacao_id ON demanda_avaliacao_risco(avaliacao_id);
