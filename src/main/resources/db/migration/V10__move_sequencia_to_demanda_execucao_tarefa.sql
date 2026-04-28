ALTER TABLE demanda_execucao_tarefa
ADD COLUMN IF NOT EXISTS sequencia INTEGER;

ALTER TABLE demanda_execucao
DROP COLUMN IF EXISTS sequencia;
