ALTER TABLE demanda_execucao_tarefa_recurso
ADD COLUMN IF NOT EXISTS perfil_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_demanda_execucao_tarefa_recurso_perfil
    ON demanda_execucao_tarefa_recurso(perfil_id);
