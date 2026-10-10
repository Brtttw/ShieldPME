# ShieldPME API

Spring Boot 4, Java 21, JPA e SQL Server. O banco é o `ShieldPME_School`; o `ShieldPME` do TCC não é usado.

## Rodar

1. SQL Server no ar (container que já existe) e `database/shieldpme_database.sql` executado nele.
2. API, na pasta `shieldpme-api`:
   ```bash
   DB_PASSWORD='senha_do_sa' ./mvnw spring-boot:run
   ```
   Se o SQL Server não estiver na porta 1433, acrescente `DB_PORT=...`. Também dá para copiar
   `src/main/resources/application-local.properties.example` para `application-local.properties`
   e colocar a senha ali (esse arquivo não vai pro git).
3. Front: `cd ../shieldpme-front && npm run dev` (com `VITE_USE_MOCK=false` no `.env`).
4. Teste rápido com a API no ar: `node scripts/smoke-test.mjs` (ou passando `http://localhost:5173` para testar pelo proxy do vite).
   Ele grava dados de teste (emails `smoke-...@example.com`).

Carregar o SQL de novo, se precisar (o script não apaga nada):

```bash
docker cp database/shieldpme_database.sql shieldpme-sqlserver:/tmp/escola.sql
docker exec -e SQLCMDPASSWORD="$SA_PASSWORD" shieldpme-sqlserver \
  /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -C -b -f 65001 -i /tmp/escola.sql
```

(em imagens mais antigas o sqlcmd fica em `/opt/mssql-tools/bin/sqlcmd` e não precisa do `-C`)

## Endpoints

Todos públicos. Erros voltam como `{ "mensagem": "..." }`.

| Método | Rota | Resposta |
|---|---|---|
| GET | `/api/planos`, `/api/planos/{codigo}` | planos com `beneficios` |
| GET | `/api/servicos` | serviços |
| GET | `/api/blog/posts` | posts |
| GET | `/api/ferramentas` | ferramentas com `recursos` |
| POST | `/api/auth/cadastro` | 201 com `{ nome, email }` |
| POST | `/api/auth/login` | 200 com `{ nome, email }` |
| POST | `/api/contatos` | 204 |
| POST | `/api/newsletter` | 204 |
| POST | `/api/pagamentos/pix` | `{ copiaECola, qrCodeData }` |
| POST | `/api/assinaturas` | 201 com `{ id, planoCodigo, valorMensal, status }` |

Não há token nem sessão: o site não tem área que exija login. A senha é guardada com BCrypt.
O pagamento é simulado (sem gateway): o preço vem sempre do banco e dados de cartão nunca são gravados.

## Estrutura

```
src/main/java/br/itb/projeto/shieldpme
  config/       CorsConfig
  controller/   endpoints e ApiExceptionHandler
  dto/          JSONs de entrada e saída
  model/        entity e repository
  service/      regras
```
