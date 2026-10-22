# Mini manual — Como instalar, executar e implementar a API de Tarefas (com login)

**Desenvolvimento Back-end II · TSI-4AN · Spring Boot 3.5.15 · Java 17 (compatível com Java 21)**

Este manual vai de **"ainda não instalei as ferramentas"** até **"executei a API e criei minha própria entidade com CRUD"**. O passo a passo principal considera **Windows + PowerShell + VS Code**. Quem usa Spring Tool Suite (STS) encontra orientações ao final.

> **Importante:** não é necessário instalar MySQL para começar. O projeto usa **H2 como banco padrão**, em memória; MySQL é uma opção para quem precisa de persistência externa. Também não é obrigatório substituir o Java já usado por outros programas: é possível configurar o JDK apenas para o terminal de desenvolvimento.

---

**Escopo:** projeto educacional. Os tokens de sessão são UUIDs armazenados em memória, não JWT; a proteção das rotas usa um `HandlerInterceptor`, não uma configuração completa do Spring Security. Não exponha esta aplicação publicamente sem revisar autenticação, sessões, segredos e configurações.

## 1. Ferramentas e verificação inicial

| Ferramenta | Requisito para este projeto | Verificação |
|---|---|---|
| **JDK (Java Development Kit)** | **17 ou 21**, preferencialmente uma destas versões LTS | `java -version` e `javac -version` |
| **Apache Maven** | **3.9 ou superior**, distribuição **binária** | `mvn -v` |
| **Git** | Versão recente | `git --version` |
| **Visual Studio Code** | Versão recente, com extensões Java e Spring | Abrir o VS Code |
| **Bruno ou Postman** | Opcional para testar as rotas | Abrir o aplicativo |
| **MySQL** | **Opcional**; use somente se quiser persistência em banco externo | Conectar ao servidor MySQL |

> **Versões de Java:** o `pom.xml` define `<java.version>17</java.version>`, ou seja, o código é compilado para Java 17. É possível compilar/executar o projeto usando **JDK 21**. O Maven deve apontar para **um JDK compatível (17 ou 21)**; não precisa obrigatoriamente mostrar Java 17.

### 1.1. Instalar o JDK (Java)

1. Verifique primeiro se já existe Java instalado:

   ```powershell
   java -version
   javac -version
   ```

2. Se faltar o **JDK**, instale uma distribuição como [Eclipse Temurin](https://adoptium.net/) ou [Azul Zulu](https://www.azul.com/downloads/), escolhendo **JDK 17 ou 21** para Windows.
3. Após a instalação, abra um **novo** PowerShell e execute novamente os dois comandos.
4. Se não reconhecer `java` ou `javac`, verifique `JAVA_HOME` e `Path` nas variáveis de ambiente do Windows:
   - `JAVA_HOME` deve apontar para a **pasta raiz do JDK**, por exemplo `C:\Program Files\Zulu\zulu-21` (sem `\bin`).
   - A variável `Path` deve conter `%JAVA_HOME%\bin` ou a pasta `bin` correspondente.

**Se já utiliza Java em outro software:** **não desinstale nem substitua o JDK do outro sistema**. Para escolher o Java somente na janela atual do PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Zulu\zulu-21"  # ajuste para o seu JDK
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
```

Essas alterações são temporárias, válidas apenas para esse PowerShell e os processos iniciados nele.

### 1.2. Instalar o Apache Maven no Windows

1. Acesse o site oficial: <https://maven.apache.org/download.cgi>.
2. Baixe **Binary zip archive**, **não** `Source zip archive`.
3. Extraia para uma pasta fora do projeto, por exemplo `C:\Ferramentas\apache-maven-3.9.x` (substitua `3.9.x` pela versão realmente baixada).
4. Confira a estrutura. A instalação binária deve conter:

   ```text
   apache-maven-3.9.x/
   ├── bin/
   │   └── mvn.cmd
   ├── boot/
   ├── conf/
   └── lib/
   ```

5. No Windows, procure **Editar as variáveis de ambiente do sistema** → **Variáveis de Ambiente** → `Path` → **Editar** → **Novo**.
6. Adicione a pasta `bin` **da distribuição binária**, por exemplo `C:\Ferramentas\apache-maven-3.9.x\bin`.
7. Feche e reabra o terminal/VS Code. Teste:

   ```powershell
   mvn -v
   where.exe mvn
   ```

**Atenção ao erro `Error: -classpath requires class path specification`:** confira onde o `mvn.cmd` está instalado:

```powershell
Get-Command mvn
where.exe mvn
```

Se apontar para algo semelhante a `C:\apache-maven-3.9.9\apache-maven\src\bin\mvn.cmd`, provavelmente foi extraído o **código-fonte do Maven**, e não sua distribuição binária executável. Instale o **Binary zip archive** e atualize o `Path` para o `bin` correto, que deve estar na mesma instalação que contém as pastas `boot` e `lib`. Não é necessário alterar o Java de outro software.

Para testar o executável **sem** mexer no `Path` global:

```powershell
& "C:\Ferramentas\apache-maven-3.9.x\bin\mvn.cmd" -v
```

### 1.3. Instalar o Git

1. Baixe pelo site oficial: <https://git-scm.com/downloads/win>.
2. Execute o instalador, mantendo as opções padrão se não tiver uma necessidade específica.
3. Abra outro PowerShell e confirme:

   ```powershell
   git --version
   ```

O Git permite baixar o repositório com `git clone` e obter atualizações com `git pull`.

### 1.4. Instalar o Visual Studio Code e as extensões

1. Baixe: <https://code.visualstudio.com/>.
2. Abra **Extensions** (`Ctrl+Shift+X`) e instale:

| Extensão | Função |
|---|---|
| **Extension Pack for Java** | Suporte a Java, depuração, Maven e gerenciamento de projetos |
| **Spring Boot Extension Pack** | Ferramentas para Spring Boot e painel Spring Boot Dashboard |
| **Lombok Annotations Support for VS Code** | Suporte complementar às anotações do Lombok, caso necessário |

3. Abra a paleta de comandos (`Ctrl+Shift+P`) → **Java: Configure Java Runtime** e confira se o JDK 17 ou 21 foi identificado.

> A IDE é uma escolha pessoal: quem já utiliza **Spring Tool Suite, Eclipse ou IntelliJ** não precisa trocar de ferramenta. Veja as instruções para STS na seção 10.

### 1.5. Instalar um cliente para testar a API

- [Bruno](https://www.usebruno.com/downloads) — recomendado porque o repositório já inclui uma coleção em `bruno/`.
- [Postman](https://www.postman.com/downloads/) — alternativa.
- **Swagger UI** — já disponibilizado pela aplicação em `http://localhost:8080/swagger-ui.html`, sem instalação separada.

### 1.6. Checklist de ambiente

Abra um novo PowerShell:

```powershell
java -version
javac -version
mvn -v
git --version
```

Se `mvn -v` mostra `Java version: 17...` ou `Java version: 21...`, o JDK utilizado pelo Maven é adequado para este projeto.

---

## 2. Baixar o projeto

No PowerShell, escolha uma pasta de trabalho e execute:

```powershell
git clone https://github.com/MariaEduarda004/Back-end-II-2026.2.git
cd Back-end-II-2026.2
```

Se já baixou pelo navegador, extraia o ZIP e **abra a pasta onde fica o `pom.xml`**.

> Os comandos devem ser executados na raiz do repositório. Se o endereço do GitHub mudar ou o repositório não estiver acessível, baixe o arquivo ZIP pela página do projeto e abra sua pasta raiz.


## 3. Rodar sem instalar banco (H2 padrão) — comece por aqui

**Na pasta que contém `pom.xml`**, execute no PowerShell:

```powershell
mvn clean spring-boot:run
```

Na primeira execução, o Maven pode precisar de conexão à internet para baixar dependências. Aguarde no terminal a mensagem `Started ApiAulaApplication` **sem erro**. Então abra:

- **Front-end:** login em <http://localhost:8080/>, cadastro em <http://localhost:8080/front/cadastro.html> e tarefas em <http://localhost:8080/front/tarefas.html>.
- **Swagger UI:** <http://localhost:8080/swagger-ui.html>
- **Console do H2:** <http://localhost:8080/h2-console>

No console H2, use:

```text
JDBC URL: jdbc:h2:mem:api_aula
User Name: sa
Password: (deixe vazio)
```

No projeto, o H2 foi configurado **em memória**. Isso significa que **os dados somem quando a aplicação para**. Para encerrar o servidor, use `Ctrl + C` no terminal.

**Como o Spring escolhe o banco?** O `application.properties` é carregado por padrão e configura o H2 em memória. Para usar MySQL, ative o perfil `mysql`; o Spring carrega `application-mysql.properties` junto com as configurações padrão e substitui as configurações do banco. O código de Controller, Service e Repository não precisa mudar.

### 3.1. Fluxo de uso do front-end

1. Acesse <http://localhost:8080/>. Essa é a tela de login.
2. Se ainda não tiver uma conta, selecione **Criar conta**. Após o cadastro, o sistema autentica o usuário e abre a lista de tarefas.
3. Se já tiver uma conta, informe e-mail e senha para entrar.
4. Na tela de tarefas, adicione, edite, conclua ou exclua tarefas. Os filtros e a exportação JSON são recursos adicionais para explorar.
5. Clique em **Sair** para encerrar a sessão.

O login fica em `src/main/resources/static/index.html`. As outras páginas e o CSS ficam em `src/main/resources/static/front`. Os arquivos JavaScript ficam em `src/main/resources/static/js`: `login.js` cuida do login, `cadastro.js` do cadastro, `tarefas.js` das tarefas e `api.js` reúne a comunicação compartilhada com a API.

```text
index.html (login)
    ├── front/cadastro.html → js/cadastro.js ─┐
    └── js/login.js ──────────────────────────┴→ front/tarefas.html
                                                      │
                                  js/tarefas.js → js/api.js → API REST
```

### 3.2. Entendendo os arquivos de configuração

- `src/main/resources/application.properties`: configuração padrão do H2 (`jdbc:h2:mem:api_aula`), usuário `sa` e console web H2.
- `src/main/resources/application-mysql.properties`: configurações do perfil MySQL; permite informar `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` por variáveis de ambiente.
- `spring.jpa.hibernate.ddl-auto=update`: permite ao Hibernate tentar atualizar tabelas no desenvolvimento. 
- `spring.jpa.show-sql=true`: imprime instruções SQL no terminal, útil para aprender como o JPA persiste entidades.

---

## 4. Ambiente de desenvolvimento: VS Code

1. No VS Code, escolha **File → Open Folder** e abra **`Back-end-II-2026.2`**, a pasta que contém `pom.xml`.
2. Aguarde a importação do projeto Java e o carregamento das dependências Maven.
3. Abra o terminal integrado (**Terminal → New Terminal** ou `Ctrl + '`), selecione PowerShell e execute o comando da **seção 3**.
4. Para explorar o código, abra `src/main/java/com/exemplo/apiaula/ApiAulaApplication.java` e localize o método `main`.
5. É possível usar o **Spring Boot Dashboard** ou o botão **Run** sem ativar um perfil: o H2 já é a configuração padrão.
6. Se o VS Code escolher o JDK incorreto: `Ctrl+Shift+P` → **Java: Configure Java Runtime** → selecione o JDK 17 ou 21 compatível.

---

## 5. Testar os endpoints

O cadastro (`POST /usuarios`) e o login (`POST /auth/login`) são públicos. As rotas protegidas exigem o cabeçalho `Authorization: Bearer <token>`. O projeto restringe o acesso às tarefas do usuário autenticado.

| Método | Rota | Corpo | Sucesso | Erros possíveis |
|---|---|---|---|---|
| POST | `/usuarios` (cadastro) | JSON | 201 | 400, 409 |
| POST | `/auth/login` | JSON | 200 (retorna `token`) | 400, 401 |
| POST | `/auth/logout` | — | 204 | 204 mesmo sem token (comportamento atual) |
| GET | `/usuarios/{id}` | — | 200 | 401, 403, 404 |
| PUT | `/usuarios/{id}` | JSON | 200 | 400, 401, 403, 404, 409 |
| DELETE | `/usuarios/{id}` | — | 204 | 401, 403, 409 |
| GET | `/tarefas` | — | 200 | 401 |
| GET | `/tarefas/{id}` | — | 200 | 401, 404 |
| POST | `/tarefas` | JSON | 201 | 400, 401 |
| PUT | `/tarefas/{id}` | JSON | 200 | 400, 401, 404 |
| DELETE | `/tarefas/{id}` | — | 204 | 401, 404 |
| GET | `/tarefas/exportar/json` | — | 200 (download `tarefas.json`) | 401 |

> A tabela resume os resultados previstos pelo código. Falhas inesperadas ou indisponibilidade do servidor também são possíveis.

### 5.1. Testar rotas protegidas pelo Swagger

O Swagger UI é gerado pelo **springdoc-openapi** a partir dos controllers e DTOs. `OpenApiConfig` define o esquema Bearer, e as anotações `@SecurityRequirement` marcam os endpoints protegidos.

1. Abra <http://localhost:8080/swagger-ui.html>.
2. Execute `POST /usuarios` para cadastrar uma conta.
3. Execute `POST /auth/login` com e-mail e senha. Copie o `token` retornado.
4. Clique em **Authorize** e cole somente o token; a interface inclui o prefixo `Bearer`.
5. Execute `/tarefas` e outras rotas protegidas.

A especificação OpenAPI também está em <http://localhost:8080/v3/api-docs>.

**Exemplo de cadastro:**

```json
{ "nome": "Joao Silva", "email": "joao@email.com", "idade": 25, "ativo": true, "senha": "123456" }
```

**Exemplo de login:**

```json
{ "email": "joao@email.com", "senha": "123456" }
```

**Exemplo de tarefa:**

```json
{ "titulo": "Estudar Spring", "concluida": false }
```

**No Bruno:** escolha **Open Collection**, abra `bruno/` e execute as requisições na ordem: cadastro (`00`), login (`01`), operações de tarefa (`02`–`05`) e logout (`06`). A coleção possui configuração de token para facilitar os testes.

**Fluxo de demonstração pelo navegador:** criar conta → login automático → adicionar tarefa → marcar como concluída → filtrar Pendentes/Concluídas → editar título → excluir → sair → entrar de novo. Depois, crie uma segunda conta para verificar que ela **não enxerga as tarefas da primeira**.

**Observações didáticas:** senhas são armazenadas como **hash BCrypt**. O token é um UUID mantido na memória do servidor; ao reiniciar, é necessário fazer login novamente. Esse mecanismo simplificado **não deve ser adotado como autenticação de produção**.

---

## 6. Exportar dados em JSON (Jackson `ObjectMapper`)

Exportar significa transformar objetos Java em **texto JSON** e disponibilizá-lo como arquivo. O projeto usa a biblioteca **Jackson**, já disponível pelas dependências Spring Web.

**Como usar:** na lista de tarefas, clique em **Exportar JSON**. A API também fornece `GET /tarefas/exportar/json`, exigindo o token.

**Onde está implementado:** [`ExportacaoService`](src/main/java/com/exemplo/apiaula/service/ExportacaoService.java) serializa os dados e [`TarefaController`](src/main/java/com/exemplo/apiaula/controller/TarefaController.java) devolve o arquivo.

```java
@Service
public class ExportacaoService {

    private final TarefaService tarefaService;
    private final ObjectMapper objectMapper;

    public String exportarTarefasEmJson(Long usuarioLogadoId) {
        List<TarefaResponse> tarefas = tarefaService.listar(usuarioLogadoId);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(tarefas);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Não foi possível gerar o JSON das tarefas.", e);
        }
    }
}
```

No controller:

```java
@GetMapping("/exportar/json")
public ResponseEntity<String> exportarJson(
        @RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId) {
    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"tarefas.json\"")
            .body(exportacaoService.exportarTarefasEmJson(usuarioLogadoId));
}
```

**Exemplo de conteúdo gerado:**

```json
[
  { "id": 1, "titulo": "Estudar Spring", "concluida": false }
]
```

**Conceitos importantes:**

- `writeValueAsString(objeto)` transforma um objeto Java em JSON (**serialização**).
- `readValue(texto, Classe.class)` é um exemplo do caminho inverso (**desserialização**).
- O serviço exporta `TarefaResponse` (**DTO**), evitando expor a entidade de persistência diretamente.
- `JsonProcessingException` exige tratamento com `try/catch` ou `throws`.
- A rota literal `/exportar/json` não é confundida com `/{id}`; o Spring prioriza a correspondência específica.

**Desafio opcional:** adicionar exportação XML em `GET /tarefas/exportar/xml` usando a dependência `jackson-dataformat-xml` e a classe `XmlMapper`; **isso não está implementado neste repositório :)**.

---

## 7. Como o projeto está organizado

```text
src/main/java/com/exemplo/apiaula/
├── ApiAulaApplication.java   ponto de partida
├── controller/               recebe HTTP e responde (Usuario, Tarefa, Auth)
├── service/                  regras de negócio, autenticação e exportação
├── repository/               interfaces Spring Data JPA
├── entity/                   Usuario e Tarefa (mapeamento para tabelas)
├── dto/                      objetos de entrada/saída da API
├── config/                   interceptor de autenticação, configurações web/BCrypt
└── exception/                exceções e tratamento HTTP
src/main/resources/
├── application.properties      configuração padrão (H2 em memória)
├── application-mysql.properties configuração opcional para MySQL
└── static/
    ├── index.html            tela de login (endereço raiz)
    ├── front/                páginas HTML e CSS
    │   ├── cadastro.html     cadastro de usuário
    │   ├── tarefas.html      operações com tarefas
    │   └── style.css
    └── js/                   JavaScript do front-end
        ├── api.js            chamadas compartilhadas à API
        ├── login.js
        ├── cadastro.js
        └── tarefas.js
```

**Fluxo simplificado de uma requisição protegida:**

```text
front/tarefas.html → js/tarefas.js → js/api.js
      │ HTTP + JSON + token
      ▼
AutenticacaoInterceptor
      │ identifica usuarioLogadoId
      ▼
Controller ── recebe DTO e valida os dados
      ▼
Service ───── regras de negócio
      ▼
Repository ── Spring Data JPA
      ▼
Hibernate / JDBC
      ▼
Banco H2 ou MySQL
      │
      └──► Entity → DTO de resposta → JSON HTTP → front-end
```

**Regra didática:** separe responsabilidades: Controller organiza a entrada HTTP, Service processa regras e Repository opera sobre a persistência. A API deve expor DTOs, não a entidade diretamente.

---

## 8. Opcional — rodar com MySQL

Use esta opção se precisar manter os dados após reiniciar a aplicação. Primeiro, confirme que o H2 padrão está funcionando.

1. Instale/configure um servidor MySQL e um cliente SQL de sua preferência.
2. Execute [`database/script.sql`](database/script.sql), que cria o banco `api_aula`, as tabelas `usuarios` e `tarefas` e a chave estrangeira.
3. **Atenção ao esquema:** o `script.sql` incluído no arquivo original usa `INT` nos IDs da tabela `usuarios`, enquanto a entidade Java usa `Long`. Antes de usar MySQL com esse script, padronize os identificadores como `BIGINT` (inclusive `tarefas.usuario_id`). No pacote revisado, o script foi alinhado.
4. Defina as variáveis na **sessão atual** do PowerShell:

   ```powershell
   $env:DB_URL = "jdbc:mysql://localhost:3306/api_aula"
   $env:DB_USERNAME = "root"  # somente para estudo local; prefira um usuário próprio
   $env:DB_PASSWORD = "SUA_SENHA_LOCAL"
   mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
   ```

5. Teste os mesmos endpoints. Ao contrário do H2 em memória, os registros devem permanecer após o reinício do Spring Boot.

> **Segurança:** não inclua senhas reais no README nem publique credenciais no Git. Prefira variáveis de ambiente e usuários com permissões limitadas. O MySQL utiliza um servidor de banco e não fica "dentro" da aplicação Java como o H2 embutido.

---

## 9. Problemas frequentes e soluções

| Sintoma | O que verificar |
|---|---|
| `mvn : O termo 'mvn' não é reconhecido` | Maven não instalado ou `bin` não incluído no `Path`. |
| `Error: -classpath requires class path specification` | Verifique `Get-Command mvn`; pode estar apontando para `...\apache-maven\src\bin` (distribuição de código-fonte) em vez do `bin` da distribuição binária com `lib/` e `boot/`. |
| `JAVA_HOME is not defined correctly` | `JAVA_HOME` deve apontar para a **raiz** do JDK; confira `javac -version`. |
| `mvn -v` exibe Java diferente do esperado | Verifique `JAVA_HOME`, `Path` e a configuração do terminal/IDE. Java 17 ou 21 são adequados para este projeto. |
| Erro de conexão com MySQL | Sem o perfil `mysql`, a aplicação usa o H2 padrão; ao usar MySQL, confira se o perfil está ativo e verifique serviço, URL, banco, usuário e senha. |
| `Port 8080 was already in use` | Finalize a aplicação anterior ou configure outra porta (`server.port`). |
| Classes Lombok parecem sem getters/setters | Aguarde a importação Maven; verifique extensão/processamento de anotações. |
| 401 ao acessar `/tarefas` | Faça login e envie `Authorization: Bearer <token>`. |
| Dados do H2 sumiram | Com `jdbc:h2:mem:api_aula`, isso é esperado ao reiniciar a aplicação. |
| Dependências não carregam | Confirme acesso à internet e atualização das dependências Maven. |

**Comandos úteis para diagnóstico (PowerShell):**

```powershell
java -version
javac -version
mvn -v
where.exe java
where.exe mvn
Get-Command mvn
$env:JAVA_HOME
$env:MAVEN_OPTS
```

---

## 10. Alternativa: Spring Tool Suite (STS)

Alunos que já utilizam **Spring Tool Suite / Spring Tools for Eclipse** podem continuar nessa IDE.

1. Instale Spring Tools: <https://spring.io/tools>.
2. Abra a IDE e escolha um workspace.
3. Vá a **File → Import → Maven → Existing Maven Projects**.
4. Selecione a pasta **`Back-end-II-2026.2`** (onde está `pom.xml`) e conclua a importação.
5. Verifique o JDK configurado para o projeto (17 ou 21).
6. Localize `ApiAulaApplication.java` e abra **Run Configurations** para uma execução Spring Boot.
7. Para usar a configuração padrão H2, não informe argumentos de perfil. Se quiser usar MySQL, informe:

   ```text
   --spring.profiles.active=mysql
   ```

8. Execute como **Spring Boot App**. Com o servidor iniciado, teste `http://localhost:8080/`.

Os nomes dos menus podem variar conforme a versão da IDE. Para depurar, coloque um breakpoint em `TarefaController.cadastrar()` e execute em modo **Debug**, acompanhando a passagem pelo Service e Repository.

---

## 11. Como estudar este projeto por camadas

Para acompanhar o caminho de uma criação de tarefa, abra nesta ordem: `static/js/tarefas.js` → `static/js/api.js` → `config/AutenticacaoInterceptor.java` → `controller/TarefaController.java` → `dto/TarefaRequest.java` → `service/TarefaService.java` → `repository/TarefaRepository.java` → `entity/Tarefa.java`. Ao voltar, observe `TarefaResponse` e o JSON enviado à interface. Veja os diagramas e a explicação detalhada em [FLUXO-DO-SISTEMA.md](docs/FLUXO-DO-SISTEMA.md).

## 12. Verificação final da instalação

1. No terminal, inicie a configuração padrão H2 e confirme que o servidor iniciou sem exceções.
2. Abra a tela inicial e cadastre um usuário.
3. Cadastre uma tarefa, edite o título, marque como concluída, filtre e exporte.
4. Saia e tente listar tarefas sem token: a API deve retornar `401`.
5. Entre novamente e confirme as tarefas; reiniciando com H2 em memória, elas desaparecem.
6. Crie outra conta e confirme que ela não vê as tarefas do primeiro usuário.

**Validação do pacote:** esta documentação foi produzida por inspeção estática dos arquivos. A execução real, a compilação e os testes no seu ambiente ainda devem ser realizados; não foram considerados concluídos.

---

## Links úteis

- [Java (Eclipse Temurin)](https://adoptium.net/)
- [Java (Azul Zulu)](https://www.azul.com/downloads/)
- [Apache Maven — download](https://maven.apache.org/download.cgi)
- [Apache Maven — instalação no Windows](https://maven.apache.org/guides/getting-started/windows-prerequisites.html)
- [Git — download](https://git-scm.com/downloads/win)
- [VS Code — download](https://code.visualstudio.com/)
- [VS Code — extensões Java e Spring](https://code.visualstudio.com/docs/java/extensions)
- [Spring Tools (STS)](https://spring.io/tools)
- [Bruno](https://www.usebruno.com/downloads)
- [Documentação do Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/reference/)
