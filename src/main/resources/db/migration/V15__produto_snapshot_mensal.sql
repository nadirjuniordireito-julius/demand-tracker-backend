-- Snapshot mensal por produto (fotografia mensal congelada no fechamento do mês)
CREATE TABLE produto_snapshot_mensal (
    id BIGSERIAL PRIMARY KEY,
    meta_produto_id BIGINT NOT NULL,
    ano INTEGER NOT NULL,
    mes INTEGER NOT NULL,
    status_produto_mes VARCHAR(1) NOT NULL,
    situacao VARCHAR(255),
    valor_total_orcamento NUMERIC(18, 2),
    valor_total_em_execucao NUMERIC(18, 2),
    valor_total_executado NUMERIC(18, 2),
    percentual_execucao NUMERIC(10, 2),
    valor_media_entrega_prevista_mensal NUMERIC(18, 2),
    valor_media_entrega_real_mensal NUMERIC(18, 2),
    resumo_analitico TEXT,
    fechado BOOLEAN NOT NULL DEFAULT FALSE,
    data_fechamento TIMESTAMP,
    usuario_fechamento_id BIGINT,
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_produto_snapshot_meta_ano_mes UNIQUE (meta_produto_id, ano, mes)
);

CREATE INDEX idx_produto_snapshot_meta_produto
    ON produto_snapshot_mensal(meta_produto_id);

CREATE INDEX idx_produto_snapshot_ano_mes
    ON produto_snapshot_mensal(ano, mes);

CREATE INDEX idx_produto_snapshot_status_mes
    ON produto_snapshot_mensal(status_produto_mes);

-- Plano de ação vinculado ao snapshot mensal do produto
CREATE TABLE produto_snapshot_acao (
    id BIGSERIAL PRIMARY KEY,
    snapshot_id BIGINT NOT NULL,
    tipo_acao VARCHAR(20) NOT NULL,
    descricao TEXT NOT NULL,
    responsavel_id BIGINT,
    responsavel_nome VARCHAR(255),
    prazo DATE NOT NULL,
    impacto VARCHAR(1) NOT NULL,
    status_acao VARCHAR(20) NOT NULL,
    data_status TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao_status TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_produto_snapshot_acao_snapshot
        FOREIGN KEY (snapshot_id) REFERENCES produto_snapshot_mensal(id)
);

CREATE INDEX idx_produto_snapshot_acao_snapshot
    ON produto_snapshot_acao(snapshot_id);

CREATE INDEX idx_produto_snapshot_acao_status
    ON produto_snapshot_acao(status_acao);

CREATE INDEX idx_produto_snapshot_acao_prazo
    ON produto_snapshot_acao(prazo);
