CREATE TABLE desembolso (
    id BIGSERIAL PRIMARY KEY,
    documento VARCHAR(500),
    valor_previsto NUMERIC(10,2) NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    data_desembolso DATE NOT NULL,
    data_prevista_desembolso DATE NOT NULL
);

