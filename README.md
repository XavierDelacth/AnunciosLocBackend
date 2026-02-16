# AnunciosLocBackend

**Desenvolvedores:** Wissel Filipe, Felicia Fianda e Henrique Mendes

## Descrição

AnunciosLocBackend é uma API RESTful escrita em Java (Spring Boot) para gerenciar anúncios geolocalizados.
Oferece autenticação JWT, upload de imagens, integração com Firebase para notificações push e persistência em PostgreSQL.

## Funcionalidades

- Gestão de anúncios (CRUD) com imagens
- Usuários, perfis e localização
- Locais para anúncios
- Notificações via Firebase
- Anúncios salvos (favoritos)
- Autenticação JWT com blacklist

## Tecnologias

- Java 17
- Spring Boot 3.2.x
- Spring Data JPA
- PostgreSQL
- Spring Security + JWT
- Firebase Admin SDK
- Lombok
- Maven

## Pré-requisitos

- Java 17+
- Maven 3.6+
- PostgreSQL em execução
- Conta Firebase e credenciais de serviço

## Instalação rápida

1. Clone o repositório:

```bash
git clone <url-do-repositorio>
cd AnunciosLocBackend
```

2. Configure o banco PostgreSQL (ex.: `anunciosloc`) e atualize `src/main/resources/application.properties`.

3. Coloque as credenciais do Firebase em `src/main/resources/`:

- `firebase-key.json` (usado pela configuração do SDK)
- `serviceAccountKey.json` (se usado em algum fluxo do projeto)

IMPORTANTE: nunca commite chaves ou arquivos de credenciais em repositórios públicos.

4. Build e execução:

```bash
mvn clean package
mvn spring-boot:run
# ou
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

Aplicação disponível em http://localhost:8081 por padrão.

## Configuração detalhada (exemplo)

Adicione/edite em `src/main/resources/application.properties`:

```properties
# Datasource
spring.datasource.url=jdbc:postgresql://localhost:5432/anunciosloc
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# Server
server.port=8081

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

# Upload limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Firebase (se aplicado)
firebase.config.path=classpath:firebase-key.json
```

### Variáveis de ambiente (opcional)

Você pode sobrescrever propriedades via variáveis de ambiente ou `SPRING_APPLICATION_JSON`.

Exemplo de variáveis úteis:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT`

## Firebase

- Coloque `firebase-key.json` em `src/main/resources/` ou aponte o caminho via `firebase.config.path`.
- Garanta permissões corretas na conta de serviço (Cloud Messaging e outros que a aplicação utilize).
- Teste envio manualmente usando os métodos do serviço de notificações (ver `firebase` package).

## Upload de imagens

- Diretório padrão para uploads: `uploads/imagens/` (ver `uploads/` na raiz do projeto).
- Formatos aceitos: `.jpg`, `.jpeg`, `.png` (validação na camada do controller/service).
- Limite padrão: 10MB por arquivo (configurável em `application.properties`).
- Em produção, recomenda-se usar armazenamento externo (S3, GCS) em vez do disco local.

## Autenticação (JWT)

- Endpoint de login: `POST /api/auth/login`.
- Resposta de sucesso retorna um objeto com o token JWT. Exemplo:

```json
{
   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
   "tipo": "Bearer",
   "expiraEm": 1672531199
}
```

- Para acessar endpoints protegidos, envie header:

```
Authorization: Bearer <token>
```

## Exemplos de requisição / resposta

- Login (cURL):

```bash
curl -X POST http://localhost:8081/api/auth/login \
   -H "Content-Type: application/json" \
   -d '{"email":"user@example.com","senha":"senha"}'
```

- Criar anúncio (exemplo JSON):

```json
{
   "titulo": "Promoção de Verão",
   "descricao": "Descontos em produtos selecionados.",
   "dataInicio": "2026-03-01",
   "dataFim": "2026-03-31",
   "localId": 1,
   "userId": 2
}
```

## Endpoints principais (resumo)

- Anúncios: `/api/anuncios`
- Usuários: `/api/users`
- Locais: `/api/locais`
- Perfis: `/api/perfis`
- Notificações: `/api/notificacoes`
- Auth: `/api/auth`

Para documentação interativa, recomenda-se integrar `springdoc-openapi`/Swagger.

## Estrutura do Projeto

Árvore de pastas (visão geral):

```
AnunciosLocBackend/
├── nb-configuration.xml
├── nbactions.xml
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── NewCfgProperties.java
│   │   │   └── AnunciosLocBackend/
│   │   │       └── backend/
│   │   │           ├── BackendApplication.java
│   │   │           ├── controller/
│   │   │           ├── enums/
│   │   │           ├── firebase/
│   │   │           ├── model/
│   │   │           ├── repository/
│   │   │           └── security/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── firebase-key.json
│   │       └── serviceAccountKey.json
│   └── test/
│       └── java/
│           └── AnunciosLocBackend/
│               └── backend/
├── target/
├── uploads/
│   └── imagens/
└── LICENSE (recomendado)
```

## Banco de dados / Migrações

- O projeto atualmente usa `spring.jpa.hibernate.ddl-auto=update` por conveniência.
- Para ambientes de produção, recomenda-se usar migrations com Flyway ou Liquibase e alterar `ddl-auto` para `validate`.
- Exemplo de criação de DB (Postgres):

```sql
CREATE DATABASE anunciosloc;
CREATE USER seu_usuario WITH ENCRYPTED PASSWORD 'sua_senha';
GRANT ALL PRIVILEGES ON DATABASE anunciosloc TO seu_usuario;
```

## Testes

- Executar testes unitários:

```bash
mvn test
```

- Se houver integração com ferramentas de cobertura (Jacoco), adicione plugin no `pom.xml` para gerar relatórios.

## CI / CD (sugestões)

- Adicione pipeline para `mvn clean package` e execução de testes (GitHub Actions, GitLab CI).
- Armazenar artefatos e usar ambientes separados para deploy.

## Licença & Contato

- Licença: MIT (adicione `LICENSE` na raiz se ainda não existir).
- Contato/Maintainers: adicione e-mail ou links dos desenvolvedores na seção "Autores".

## Roadmap / Limitações

- Melhorias sugeridas:
   - Mover armazenamento de imagens para S3/GCS
   - Adicionar documentação OpenAPI completa
   - Adicionar migrations (Flyway/Liquibase)
   - Monitoramento e métricas

## Contribuição

1. Fork
2. Crie branch: `git checkout -b feature/nome`
3. Commit: `git commit -am "Descrição"`
4. Push e abra PR

## Troubleshooting

- Erro conexão banco: verifique `spring.datasource.*` e se o Postgres está rodando.
- Firebase: valide `firebase-key.json` e permissões.
- Upload de imagens: confirme limites em `application.properties`.
- Porta em uso: altere `server.port`.

Para mais ajuda, consulte os logs da aplicação ou abra uma issue no repositório.
