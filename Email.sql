create database email;

CREATE TABLE Usuario 
( 
 ID INT PRIMARY KEY NOT NULL,
 Email VARCHAR(30) NOT NULL,
 Nome VARCHAR(20) NOT NULL,
 Senha VARCHAR(20) NOT NULL,
 Contatos VARCHAR(100)
); 

CREATE TABLE Mensagens 
( 
 De VARCHAR(30) NOT NULL,
 Para VARCHAR(60) NOT NULL,
 Assunto VARCHAR(30) NOT NULL,
 Mensagem VARCHAR(1000) NOT NULL,
 Dia_hora varchar(20) NOT NULL
); 

INSERT INTO Usuario (Email, Senha, ID, Nome) VALUES ('admin@email.com', '123456', 1, 'Admin');
INSERT INTO Usuario (Email, Senha, ID, Nome) VALUES ('admin2@email.com', '123456', 2, 'Admin2');
INSERT INTO Usuario (Email, Senha, ID, Nome) VALUES ('admin3@email.com', '123456', 3, 'Admin3');