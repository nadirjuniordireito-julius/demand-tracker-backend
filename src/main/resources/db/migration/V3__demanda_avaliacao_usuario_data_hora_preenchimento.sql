-- DemandaAvaliacao: usuário que preencheu e data/hora de preenchimento (insert/update)
ALTER TABLE demanda_avaliacao
    ADD COLUMN IF NOT EXISTS usuario_id BIGINT,
    ADD COLUMN IF NOT EXISTS data_hora_preenchimento TIMESTAMP;

ALTER TABLE demanda_avaliacao
    ADD CONSTRAINT fk_demanda_avaliacao_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_demanda_avaliacao_usuario_id ON demanda_avaliacao(usuario_id);
