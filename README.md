# Mini manual — Como rodar e implementar a API de Tarefas (com login)

Desenvolvimento Back-end II · TSI-4AN · Spring Boot 3.3.4 · Java 17

Este manual leva você de "baixei o projeto" até "criei minha própria entidade com CRUD completo".

---

## 1. O que você precisa instalar

| Ferramenta | Versão | Como conferir |
|---|---|---|
| JDK | **17** (use 17; Java 21 também compila, mas veja o erro comum 1) | `java -version` |
| Maven | 3.9 ou superior | `mvn -v` |
| Git | qualquer recente | `git --version` |
| VS Code + extensões Java e Spring | — | veja a seção 4 |
| Bruno (ou Postman) | — | cliente para testar a API |

> O `mvn -v` mostra qual Java o Maven está usando. Ele precisa apontar para o JDK 17.

## 2. Baixar o projeto

```powershell
git clone https://github.com/MariaEduarda004/Back-end-II-2026.2.git
cd Back-end-II-2026.2
```

Para atualizar quando houver novidades: `git pull`.

## 3. Rodar sem instalar banco (perfil H2) — comece por aqui

```powershell
mvn clean spring-boot:run "-Dspring-boot.run.profiles=h2"
```

Aguarde a mensagem `Started ApiAulaApplication`. Depois:

- **Front-end (login/cadastro e lista de tarefas): <http://localhost:8080/>**
- Swagger (teste pelo navegador): <http://localhost:8080/swagger-ui.html>
- Console do banco H2: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:api_aula`, usuário `sa`, senha vazia)

O H2 fica na memória: **ao parar a aplicação, os dados somem**. Isso é normal.

Para parar: `Ctrl + C` no terminal.

## 4. Ambiente de desenvolvimento: VS Code

Usaremos o **Visual Studio Code** (<https://code.visualstudio.com>).

**Extensões necessárias** (aba *Extensions*, `Ctrl+Shift+X`):

| Extensão | Para que serve |
|---|---|
| **Extension Pack for Java** (Microsoft) | Editar e executar Java, Maven e depurar (já inclui *Language Support for Java*, *Debugger for Java* e *Maven for Java*) |
| **Spring Boot Extension Pack** (VMware) | Painel do Spring Boot, atalhos e ajuda nos arquivos `application.properties` |
| **Lombok Annotations Support for VS Code** | Reconhece getters/setters gerados pelo Lombok (evita falsos erros) |

**Como trabalhar no VS Code:**
1. Abra a **pasta do projeto** (`File > Open Folder`, escolhendo `projeto_professor_backend_ii`, a que contém o `pom.xml`).
2. Aguarde o Java carregar o projeto (barra de status, canto inferior).
3. Abra o terminal integrado (`Ctrl + '`) e rode o comando da seção 3.
4. Alternativa ao terminal: no painel **Spring Boot Dashboard** ou abrindo `ApiAulaApplication.java` e clicando em **Run** acima do método `main` (para usar o H2, o terminal é mais simples).

> Se o VS Code usar outro JDK, configure em `Ctrl+Shift+P` > *Java: Configure Java Runtime* e escolha o 17.

## 5. Testar os endpoints

Só o cadastro (`POST /usuarios`) e o login são públicos. Todas as outras rotas exigem o cabeçalho
`Authorization: Bearer <token>`, obtido no login. Cada usuário só enxerga as **suas** tarefas.

| Método | Rota | Corpo | Sucesso | Erros possíveis |
|---|---|---|---|---|
| POST | `/usuarios` (cadastro) | JSON | 201 | 400 (validação), 409 (e-mail repetido) |
| POST | `/auth/login` | JSON | 200 (devolve `token`) | 400, 401 (e-mail ou senha errados) |
| POST | `/auth/logout` | — | 204 | 401 |
| GET | `/usuarios/{id}` | — | 200 | 401, 403 (id de outro usuário), 404 |
| PUT | `/usuarios/{id}` | JSON | 200 | 400, 401, 403, 404, 409 |
| DELETE | `/usuarios/{id}` | — | 204 | 401, 403, 409 (ainda tem tarefas) |
| GET | `/tarefas` | — | 200 | 401 |
| GET | `/tarefas/{id}` | — | 200 | 401, 404 (inclusive tarefa de outro usuário) |
| POST | `/tarefas` | JSON | 201 | 400, 401 |
| PUT | `/tarefas/{id}` | JSON | 200 | 400, 401, 404 |
| DELETE | `/tarefas/{id}` | — | 204 | 401, 404 |
| GET | `/tarefas/exportar/json` | — | 200 (baixa o arquivo `tarefas.json`) | 401 |

### Testar rotas protegidas pelo Swagger

O Swagger é gerado automaticamente pelo **springdoc-openapi** a partir dos controllers e DTOs da aplicação. A dependência já está no `pom.xml`; a classe `OpenApiConfig` configura o esquema Bearer, e as anotações `@SecurityRequirement` indicam quais operações exigem login.

1. Abra o Swagger em <http://localhost:8080/swagger-ui.html>.
2. Execute `POST /usuarios` para criar uma conta.
3. Execute `POST /auth/login` usando o e-mail e a senha dessa conta. Copie o valor de `token` da resposta.
4. Clique em **Authorize**, cole o token e confirme. Cole somente o token, sem escrever `Bearer `; o Swagger monta esse cabeçalho.
5. Agora execute as operações protegidas, como as rotas de `/tarefas` e de consulta/alteração/exclusão em `/usuarios`.

O cadastro e o login não precisam de token. A definição OpenAPI também fica disponível em <http://localhost:8080/v3/api-docs>.

`Tarefa` é a segunda entidade: cada tarefa pertence a **um** usuário (relação N:1), o usuário dono é o que está logado.

JSON de cadastro e de login:

```json
{ "nome": "Joao Silva", "email": "joao@email.com", "idade": 25, "ativo": true, "senha": "123456" }
```

```json
{ "email": "joao@email.com", "senha": "123456" }
```

JSON de tarefa:

```json
{ "titulo": "Estudar Spring", "concluida": false }
```

**No Swagger:** abra a rota, clique em *Try it out*, edite o JSON e clique em *Execute*. Para as rotas protegidas, passe o token no cabeçalho `Authorization` (`Bearer <token>`).

**No Bruno:** abra a pasta `bruno/` (*Open Collection*). As requisições estão numeradas na ordem de uso: cadastrar (00), login (01, guarda o token automaticamente), tarefas (02 a 05) e logout (06).

**Fluxo completo para demonstração/evidência (pelo front):** criar conta → (login automático) → adicionar tarefa → marcar como concluída → filtrar Pendentes/Concluídas → editar o título → excluir a tarefa → sair → entrar de novo. Crie uma segunda conta e confira que ela **não vê** as tarefas da primeira. Tire prints de cada etapa para a entrega.

**Observações didáticas:** a senha é guardada com hash BCrypt; o token é um UUID guardado na memória do servidor (ao reiniciar a aplicação, é preciso entrar de novo).

## 6. Exportar dados em JSON (Jackson `ObjectMapper`)

Exportar é transformar objetos Java em **texto JSON** e entregar como arquivo. Quem faz essa conversão é o `ObjectMapper`, da biblioteca Jackson (já vem com o Spring Boot, não precisa instalar nada).

**Como usar:** na tela de tarefas, clique em **Exportar JSON**; o navegador baixa o `tarefas.json` com as tarefas do usuário logado. Pela API: `GET /tarefas/exportar/json` com o token.

**Onde está o código:** [`ExportacaoService`](src/main/java/com/exemplo/apiaula/service/ExportacaoService.java) faz a conversão e o `TarefaController` devolve o arquivo.

```java
@Service
public class ExportacaoService {

    private final TarefaService tarefaService;
    private final ObjectMapper objectMapper;   // o Spring injeta pronto

    public String exportarTarefasEmJson(Long usuarioLogadoId) {
        List<TarefaResponse> tarefas = tarefaService.listar(usuarioLogadoId);
        try {
            // lista de objetos Java -> String JSON (com quebras de linha)
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tarefas);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Não foi possível gerar o JSON das tarefas.", e);
        }
    }
}
```

```java
// No controller: o cabeçalho "attachment" faz o navegador baixar como arquivo
@GetMapping("/exportar/json")
public ResponseEntity<String> exportarJson(@RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId) {
    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tarefas.json\"")
            .body(exportacaoService.exportarTarefasEmJson(usuarioLogadoId));
}
```

Resultado (`tarefas.json`):

```json
[ {
  "id" : 1,
  "titulo" : "Estudar",
  "concluida" : false
} ]
```

**Pontos para entender:**
- `writeValueAsString(objeto)` é a conversão Java → JSON (*serialização*). O caminho inverso é `readValue(texto, Classe.class)`.
- Exportamos o **DTO** (`TarefaResponse`), nunca a Entity: assim a senha e outros dados internos não vazam.
- `JsonProcessingException` é uma exceção verificada: precisa de `try/catch` ou `throws`.
- Na rota, `/exportar/json` precisa vir antes de `/{id}` por clareza; o Spring já prefere a rota literal.

**Desafio para o trabalho:** exporte também os **usuários** (sem a senha) e/ou crie `GET /tarefas/exportar/xml`. Para XML, adicione a dependência `jackson-dataformat-xml` e use `XmlMapper` (que funciona como o `ObjectMapper`).

## 7. Como o projeto está organizado

```
src/main/java/com/exemplo/apiaula/
├── ApiAulaApplication.java       ponto de partida
├── controller/   UsuarioController, TarefaController, AuthController (recebem HTTP e respondem)
├── service/      UsuarioService, TarefaService, AuthService, ExportacaoService (regras de negócio, login e exportação)
├── repository/   UsuarioRepository, TarefaRepository (acesso ao banco, JpaRepository)
├── entity/       Usuario, Tarefa (classes que viram tabelas, @Entity)
├── dto/          *Request (dados que ENTRAM, com validação) e *Response (dados que SAEM)
├── config/       AutenticacaoInterceptor (confere o token), WebConfig, SegurancaConfig (BCrypt)
└── exception/    exceções próprias + GlobalExceptionHandler (traduz exceção em HTTP)
src/main/resources/
├── application.properties         configuração base (não mexa; use o perfil h2)
├── application-h2.properties      configuração do perfil H2
└── static/                        front-end (index.html, style.css, app.js)
```

Caminho de uma requisição:

```
Cliente → Controller → Service → Repository → Banco
                ↑                                  │
              DTO de resposta  ←  Entity  ←────────┘
```

Regra de ouro: **cada camada só conversa com a vizinha**. O Controller nunca acessa o Repository direto, e a Entity nunca é devolvida na API: use DTO.
