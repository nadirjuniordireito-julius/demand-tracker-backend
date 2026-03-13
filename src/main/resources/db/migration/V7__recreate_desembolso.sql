-- Recria a tabela desembolso (caso tenha sido dropada manualmente)

DROP TABLE IF EXISTS desembolso CASCADE;

CREATE TABLE desembolso (
    id BIGSERIAL PRIMARY KEY,
    documento VARCHAR(500),
    valor_previsto NUMERIC(10,2) NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    data_desembolso DATE NOT NULL,
    data_prevista_desembolso DATE NOT NULL,
    projeto_id BIGINT NOT NULL
    -- FK para projeto não é criada aqui porque a tabela `projeto` não é gerenciada por Flyway.
);

