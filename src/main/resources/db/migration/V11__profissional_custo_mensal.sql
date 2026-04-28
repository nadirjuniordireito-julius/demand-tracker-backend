CREATE TABLE profissional_custo_mensal (
    id BIGSERIAL PRIMARY KEY,
    profissional_id BIGINT NOT NULL,
    ano INTEGER NOT NULL,
    mes INTEGER NOT NULL,
    custo_total NUMERIC(18,2) NOT NULL
);

CREATE INDEX idx_profissional_custo_mensal_profissional
    ON profissional_custo_mensal(profissional_id);

CREATE INDEX idx_profissional_custo_mensal_ano_mes
    ON profissional_custo_mensal(ano, mes);
