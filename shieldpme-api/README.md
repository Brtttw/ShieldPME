# ShieldPME API

API do site ShieldPME. Spring Boot 4 · Java 21 · JPA · SQL Server.
Adaptada da API de exemplo da escola (a antiga "pizzaria").

## Como rodar

1. **Banco:** abra `database/shieldpme_database.sql` no SSMS e execute tudo (F5).
   Pode executar quantas vezes quiser: nunca apaga dados (veja o cabeçalho do arquivo).
2. **Senha do banco:** copie `src/main/resources/application-local.properties.example`
   para `src/main/resources/application-local.properties` e coloque a senha do `sa`.
   Esse arquivo está no `.gitignore` e não vai para o Git.
3. **API:** `./mvnw spring-boot:run` (ou rode a classe `Startup` no IntelliJ). Sobe em `http://localhost:8080`.
4. **Front:** em `shieldpme-front/.env` troque `VITE_USE_MOCK=true` por `VITE_USE_MOCK=false`
   e rode `npm run dev` (o Vite encaminha `/api` para a porta 8080).

> `mvn package` executa o `StartupTests`, que precisa do banco no ar. Sem banco: `./mvnw package -DskipTests`.

## Endpoints

Erros sempre voltam como `{ "mensagem": "texto para o usuário" }`.

| Método | Rota | Acesso | Resposta |
|---|---|---|---|
| GET | `/api/planos` | público | lista de planos (com `beneficios`) |
| GET | `/api/planos/{codigo}` | público | plano (404 se não existir) |
| GET | `/api/servicos` | público | lista de serviços |
| GET | `/api/blog/posts` | público | lista de posts |
| GET | `/api/ferramentas` | público | lista de ferramentas (com `recursos`) |
| POST | `/api/auth/cadastro` | público | 201 + `{ nome, email, foto }` |
| POST | `/api/auth/login` | público | `{ token, usuario: { nome, email, foto } }` |
| POST | `/api/auth/google` | público | `{ token, usuario }` (precisa de internet) |
| POST | `/api/contatos` | público | 204 |
| POST | `/api/newsletter` | público | 204 |
| POST | `/api/pagamentos/pix` | público | `{ copiaECola, qrCodeData }` |
| POST | `/api/assinaturas` | público (se logado, liga ao usuário) | 201 + `{ id, planoCodigo, valorMensal, status }` |
| GET | `/api/contatos` | **ADMIN** | mensagens recebidas pelo formulário de contato |

O token do login é um JWT (`Authorization: Bearer <token>`), válido por 8 horas.

## Tornar alguém ADMIN

Não existe usuário/senha padrão. Cadastre a conta pelo site e rode no SQL Server:

```sql
UPDATE dbo.Usuario SET nivelAcesso = 'ADMIN' WHERE email = 'seu@email.com';
```

Depois faça login de novo (o nível de acesso vai dentro do token) e chame `GET /api/contatos` com o token.

## Estrutura

```
src/main/java/br/itb/projeto/shieldpme
├── config/      CorsConfig, SecurityConfig, JwtAuthFilter
├── controller/  endpoints REST + ApiExceptionHandler (erros em JSON)
├── dto/         formato exato dos JSONs que o front envia/recebe
├── model/
│   ├── entity/      tabelas (JPA)
│   └── repository/  acesso ao banco (Spring Data)
└── service/     regras de negócio
```

## Observações

- **Pagamento simulado:** não há gateway. A assinatura nasce `ATIVA`, o preço vem sempre do banco
  (nunca do front) e **número de cartão/CVV nunca são gravados nem logados**.
- **PIX:** o "copia e cola" tem formato e CRC válidos, mas a chave é fictícia.
- **Login com Google:** valida o token do Google no backend. Precisa de internet e de a origem
  `http://localhost:5173` estar autorizada no Client ID do Google Cloud.
- **JWT:** sem `shieldpme.jwt.secret` configurado, a API gera um segredo aleatório a cada início
  (os logins anteriores deixam de valer ao reiniciar). Para fixar, defina o segredo (32+ caracteres)
  no `application-local.properties`.
