ALTER TABLE demanda_execucao_tarefa_recurso
ADD COLUMN IF NOT EXISTS horas_executadas NUMERIC(10,2);
