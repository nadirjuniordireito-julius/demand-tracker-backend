CREATE TABLE dia_nao_util (
    id BIGSERIAL PRIMARY KEY,
    data DATE NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    CONSTRAINT uk_dia_nao_util_data UNIQUE (data)
);

CREATE INDEX idx_dia_nao_util_data ON dia_nao_util(data);
