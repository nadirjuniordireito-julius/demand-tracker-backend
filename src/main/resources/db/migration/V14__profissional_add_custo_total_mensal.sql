-- Custo total mensal do profissional/empresa (obrigatório; linhas existentes recebem 0 até edição)
ALTER TABLE profissional
    ADD COLUMN custo_total_mensal NUMERIC(10, 2) NOT NULL DEFAULT 0;

ALTER TABLE profissional
    ALTER COLUMN custo_total_mensal DROP DEFAULT;
