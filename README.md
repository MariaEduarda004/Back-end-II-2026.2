# Back-end II — desafio inicial

## Objetivo da aula

Conferir se cada estudante consegue importar e executar uma aplicação Spring Boot, usar H2 como banco e realizar as operações básicas de cadastro, consulta, atualização e exclusão de usuários. O ponto extra de 1 ponto é uma proposta mencionada pelo professor anterior; a professora confirmará em aula como registrar/validar essa atividade.

**Sem autenticação e sem tarefas neste primeiro projeto.** A segunda entidade `Tarefa` e a relação `Tarefa → Usuario` serão construídas depois como evolução para o trabalho final.

## Tecnologias

- Java **21** (JDK; distribuição como Azul Zulu ou Eclipse Temurin)
- Spring Boot **4.1.1**
- Gradle **Wrapper** (incluído, sem instalação de Gradle ou Maven)
- Spring Web MVC, Spring Data JPA, Bean Validation
- Banco **H2** em memória, padrão; **MySQL** como alternativa
- Lombok para getters, setters e construtores

## Preparação do ambiente no Windows

1. Instale JDK 21. Abra o PowerShell: `java -version`. Se necessário, ajuste o **Project SDK** para Java 21 no IntelliJ.
2. Instale o [IntelliJ IDEA](https://www.jetbrains.com/idea/download/). A edição gratuita já é suficiente para abrir projeto Gradle e executar Java.
3. Baixe ou clone este repositório: `git clone URL_DO_SEU_REPOSITORIO_AQUI` (substitua após publicar no GitHub). Também é possível usar **Code > Download ZIP**.
4. Abra o IntelliJ → **Open** → selecione a **pasta que contém `build.gradle`** → **Trust Project**, se a origem for confiável.
5. Aguarde **Gradle Sync** terminar e confira **File > Project Structure > Project SDK = 21** e **Settings > Build Tools > Gradle > Gradle JVM = 21**.
6. Execute `AulaApplication.java` (botão verde próximo do `main`), ou no terminal do IntelliJ:

```powershell
.\gradlew.bat bootRun
```

Na primeira execução o Gradle Wrapper baixa a distribuição e bibliotecas pela internet. O perfil **h2 é padrão**. A aplicação inicia, por padrão, em `http://localhost:8080`.

## Passo 1 — conferir a API

Abra `http://localhost:8080/usuarios` no navegador. O esperado inicialmente é `[]`.

## Passo 2 — testar o CRUD

Rotas: `GET /usuarios`, `GET /usuarios/{id}`, `POST /usuarios`, `PUT /usuarios/{id}`, `DELETE /usuarios/{id}`. Sucessos esperados: 200, 201 e 204; erros demonstráveis: 400 (dados inválidos), 404 (ID não encontrado), 409 (e-mail duplicado).

## Banco H2

- Endereço da aplicação: `http://localhost:8080/usuarios`
- Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:backend_aula;DB_CLOSE_DELAY=-1`
- Usuário: `sa`; senha: **vazia**
- SQL: `SELECT * FROM USUARIOS;`

**Atenção:** o H2 configurado é em memória; os dados são perdidos ao parar a aplicação. O projeto usa `ddl-auto: create-drop` neste perfil para demonstração.

## MySQL opcional

1. Instale e inicie o MySQL; execute `database/criar_banco_mysql.sql`.
2. No PowerShell, defina `$env:DB_PASSWORD='SUA_SENHA_LOCAL'`.
3. Execute `.\gradlew.bat bootRun --args="--spring.profiles.active=mysql"`.
4. Veja `src/main/resources/application-mysql.yaml` para URL e credenciais. **Nunca publique senhas reais no GitHub.**

## Como o código está organizado

```text
src/main/java/com/ifsul/backend/aula/
  AulaApplication.java          # ponto de entrada
  entity/Usuario.java           # @Entity -> tabela usuarios
  repository/UsuarioRepository.java # JpaRepository
  dto/UsuarioRequest.java       # JSON que entra + validação
  dto/UsuarioResponse.java      # JSON que sai
  service/UsuarioService.java   # regras: e-mail único, localizar, CRUD
  controller/UsuarioController.java # endpoints HTTP
  exception/                    # mapeamento de erros 400 / 404 / 409
```

**Fluxo:** Cliente → Controller → DTO + validação → Service → Repository → JPA/Hibernate → H2/MySQL. A resposta retorna como DTO em JSON.
