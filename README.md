# AnunciosLocBackend

# Desenvolvedores : Wissel Filipe , Felicia Fianda e Henrique Mendes

## Descrição

AnunciosLocBackend é uma aplicação backend desenvolvida em Java utilizando o framework Spring Boot. O projeto visa fornecer uma API RESTful para um sistema de anúncios baseados em localização, permitindo a criação, gestão e interação com anúncios geolocalizados. Inclui funcionalidades de autenticação JWT, integração com Firebase para notificações push, upload de imagens e persistência de dados em PostgreSQL.

## Funcionalidades

- **Gestão de Anúncios**: Criar, listar, atualizar e deletar anúncios com informações detalhadas como título, descrição, datas, horários, políticas e imagens.
- **Usuários e Perfis**: Sistema de usuários com perfis personalizáveis e localização.
- **Locais**: Cadastro e gestão de locais para anúncios.
- **Notificações**: Integração com Firebase para envio de notificações push aos dispositivos.
- **Anúncios Salvos**: Funcionalidade para usuários salvarem anúncios de interesse.
- **Autenticação e Segurança**: Implementação de JWT para autenticação e autorização, com blacklist de tokens.
- **Upload de Imagens**: Suporte para upload e armazenamento de imagens associadas aos anúncios.
- **Enums e Políticas**: Definição de tipos de entrega, políticas e tipos de localização.

## Tecnologias Utilizadas

- **Java 17**: Linguagem de programação principal.
- **Spring Boot 3.2.0**: Framework para desenvolvimento de aplicações Java.
- **Spring Data JPA**: Para persistência de dados.
- **PostgreSQL**: Banco de dados relacional.
- **Spring Security**: Para autenticação e autorização.
- **JWT (JSON Web Tokens)**: Para gerenciamento de sessões.
- **Firebase Admin SDK**: Para notificações push.
- **Lombok**: Para redução de código boilerplate.
- **Commons FileUpload**: Para upload de arquivos.
- **Maven**: Gerenciamento de dependências e build.

## Pré-requisitos

Antes de executar o projeto, certifique-se de ter instalado:

- **Java 17** ou superior.
- **Maven 3.6+** para gerenciamento de dependências.
- **PostgreSQL** configurado e rodando.
- **Conta no Firebase** com projeto configurado para notificações (chaves em `firebase-key.json` e `serviceAccountKey.json`).

## Instalação

1. **Clone o repositório**:
   ```bash
   git clone <url-do-repositorio>
   cd AnunciosLocBackend
   ```

2. **Configure o banco de dados**:
   - Crie um banco de dados PostgreSQL chamado `anunciosloc`.
   - Atualize as credenciais no arquivo `src/main/resources/application.properties` se necessário.

3. **Configure o Firebase**:
   - Coloque os arquivos `firebase-key.json` e `serviceAccountKey.json` no diretório `src/main/resources/`.

4. **Instale as dependências**:
   ```bash
   mvn clean install
   ```

## Configuração

O arquivo `application.properties` contém as configurações principais:

- **Banco de Dados**: URL, usuário e senha do PostgreSQL.
- **Upload**: Limites de tamanho para arquivos (10MB por padrão).
- **Porta**: Aplicação roda na porta 8081.
- **JPA**: Configurações de Hibernate para PostgreSQL.

Certifique-se de que o banco de dados esteja acessível e as chaves do Firebase estejam corretas.

## Execução

Para executar a aplicação:

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8081`.

## API Endpoints

A API fornece endpoints RESTful organizados por controladores. Aqui um resumo dos principais:

### Anúncios (`/api/anuncios`)
- `POST /api/anuncios`: Criar novo anúncio (com upload de imagem).
- `GET /api/anuncios`: Listar todos os anúncios.
- `GET /api/anuncios/{id}`: Obter anúncio por ID.
- `PUT /api/anuncios/{id}`: Atualizar anúncio.
- `DELETE /api/anuncios/{id}`: Deletar anúncio.
- `GET /api/anuncios/user/{userId}`: Listar anúncios por usuário.
- `GET /api/anuncios/local/{localId}`: Listar anúncios por local.

### Usuários (`/api/users`)
- `POST /api/users`: Registrar novo usuário.
- `GET /api/users/{id}`: Obter usuário por ID.
- `PUT /api/users/{id}`: Atualizar usuário.
- `DELETE /api/users/{id}`: Deletar usuário.

### Locais (`/api/locais`)
- `POST /api/locais`: Criar novo local.
- `GET /api/locais`: Listar locais.
- `GET /api/locais/{id}`: Obter local por ID.

### Perfis (`/api/perfis`)
- Endpoints para gestão de perfis de usuário.

### Notificações (`/api/notificacoes`)
- `POST /api/notificacoes`: Enviar notificação.

### Autenticação (`/api/auth`)
- `POST /api/auth/login`: Login e geração de JWT.
- `POST /api/auth/logout`: Logout e blacklist do token.

Para documentação completa da API, considere usar ferramentas como Swagger ou Postman.

## Testes

Para executar os testes:

```bash
mvn test
```

Os testes estão localizados em `src/test/java/`.

## Estrutura do Projeto

```
AnunciosLocBackend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── AnunciosLocBackend/
│   │   │       └── backend/
│   │   │           ├── controller/     # Controladores REST
│   │   │           ├── model/          # Entidades JPA
│   │   │           ├── repository/     # Repositórios de dados
│   │   │           ├── service/        # Lógica de negócio
│   │   │           ├── security/       # Configurações de segurança
│   │   │           ├── firebase/       # Configuração Firebase
│   │   │           └── enums/          # Enums do projeto
│   │   └── resources/                  # Propriedades e chaves
│   └── test/                           # Testes unitários
├── uploads/                            # Diretório para uploads
├── pom.xml                             # Configuração Maven
└── README.md                           # Este arquivo
```

## Contribuição

1. Faça um fork do projeto.
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`).
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`).
4. Push para a branch (`git push origin feature/nova-feature`).
5. Abra um Pull Request.

## Licença

Este projeto está sob a licença [MIT](LICENSE). Consulte o arquivo LICENSE para mais detalhes.

## Autores

- Desenvolvido por [Nome do Autor] - [Contato ou GitHub]

## Troubleshooting

- **Erro de conexão com banco**: Verifique se o PostgreSQL está rodando e as credenciais estão corretas.
- **Problemas com Firebase**: Certifique-se de que os arquivos de chave estão no local correto e válidos.
- **Upload de imagens**: Verifique os limites de tamanho no `application.properties`.
- **Porta ocupada**: Mude a porta no `application.properties` se 8081 estiver em uso.

Para mais ajuda, consulte os logs da aplicação ou abra uma issue no repositório.
