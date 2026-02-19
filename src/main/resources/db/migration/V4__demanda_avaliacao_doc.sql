-- Documento PDF da Avaliação da Demanda (1:1 com demanda_avaliacao)
CREATE TABLE IF NOT EXISTS demanda_avaliacao_doc (
    id BIGSERIAL PRIMARY KEY,
    avaliacao_id BIGINT NOT NULL UNIQUE,
    data_upload TIMESTAMP,
    arquivo_pdf BYTEA,
    nome_arquivo VARCHAR(255),
    tipo_conteudo VARCHAR(100),
    tamanho_arquivo BIGINT,
    CONSTRAINT fk_demanda_avaliacao_doc_avaliacao FOREIGN KEY (avaliacao_id) REFERENCES demanda_avaliacao(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_demanda_avaliacao_doc_avaliacao_id ON demanda_avaliacao_doc(avaliacao_id);
