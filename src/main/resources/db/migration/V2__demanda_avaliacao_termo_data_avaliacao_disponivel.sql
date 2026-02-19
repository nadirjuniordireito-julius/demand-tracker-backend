-- DemandaAvaliacao: termoEncerramentoId e dataAvaliacao
ALTER TABLE demanda_avaliacao
    ADD COLUMN IF NOT EXISTS termo_encerramento_id BIGINT,
    ADD COLUMN IF NOT EXISTS data_avaliacao TIMESTAMP;

-- DemandaTecnica: flag avaliacaoDisponivel (quando demanda encerrada)
-- Coluna nullable para não falhar em tabelas já existentes; default false para novos registros
ALTER TABLE demandas_tecnicas
    ADD COLUMN IF NOT EXISTS avaliacao_disponivel BOOLEAN DEFAULT false;
