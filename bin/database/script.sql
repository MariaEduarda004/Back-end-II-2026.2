CREATE DATABASE IF NOT EXISTS api_aula;
USE api_aula;

-- A senha é guardada como hash BCrypt (60 caracteres). Por isso não há usuários de exemplo:
-- cadastre-se pela tela do sistema para que o hash seja gerado corretamente.
CREATE TABLE IF NOT EXISTS usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    idade INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    senha VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS tarefas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    concluida BOOLEAN NOT NULL DEFAULT FALSE,
    usuario_id INT NOT NULL,
    CONSTRAINT fk_tarefas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
