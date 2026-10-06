# Loja de Carros e serviço de usuários

A solução tem dois projetos Spring Boot independentes:

- **Loja de Carros** (raiz do repositório): CRUD de carros e persistência dos logs de operação.
- **usuarios-service/**: novo projeto para CRUD e consulta de usuários por API REST.

## Executar localmente

### Demonstração rápida (H2 em memória)

Inicie cada serviço em um terminal separado. Os dados são temporários e
desaparecem quando os processos param; nenhum banco MySQL é usado.

```powershell
$env:SPRING_PROFILES_ACTIVE = "demo"
.\mvnw.cmd -f usuarios-service\pom.xml spring-boot:run
```

```powershell
$env:SPRING_PROFILES_ACTIVE = "demo"
.\mvnw.cmd spring-boot:run
```

Depois, crie um usuário com `POST http://localhost:8081/usuarios` e o corpo
`{"nome":"Ana Silva"}`. Use o ID retornado no header `X-User-Id` das requisições
à Loja. Consulte os logs em `http://localhost:8080/logs`.

### Execução com MySQL

1. Crie um banco MySQL chamado `usuarios`.
2. Inicie o serviço de usuários na raiz do repositório:

   ```powershell
   .\mvnw.cmd -f usuarios-service\pom.xml spring-boot:run
   ```

   Por padrão, ele escuta em `http://localhost:8081`. Configure `USUARIOS_DB_URL`,
   `USUARIOS_DB_USERNAME` e `USUARIOS_DB_PASSWORD` para apontar para o banco.
3. Inicie a Loja de Carros com o banco de carros configurado e defina
   `USUARIOS_API_URL=http://localhost:8081` e o perfil Spring já usado pelo projeto (`SERVER`).

## CRUD de usuários

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/usuarios` | Cria usuário com `{"nome":"Ana Silva"}` |
| `GET` | `/usuarios/{id}` | Consulta usuário; também é a API chamada pela Loja |
| `GET` | `/usuarios` | Lista usuários |
| `PUT` | `/usuarios/{id}` | Atualiza o nome |
| `DELETE` | `/usuarios/{id}` | Exclui usuário |

A consulta por ID retorna `{"id":1,"nome":"Ana Silva"}`. O serviço usa o banco
`usuarios`, separado do banco da Loja.

## Auditoria das operações de carro

As rotas de carro exigem o header `X-User-Id`, por exemplo:

```http
GET /carro/listarCarros
X-User-Id: 1
```

Antes de executar cada operação, a Loja consulta `GET /usuarios/{id}`. Se o
usuário não existir, retorna `404`; se o serviço estiver indisponível, retorna
`503` e não executa a operação. Operações bem-sucedidas são gravadas na tabela
`log_operacao`, com ID/nome do usuário, operação, ID do carro quando aplicável e
data/hora. Os registros podem ser consultados em `GET /logs`.

A comunicação permanece síncrona por API REST nesta etapa; a substituição por
mensageria fica para a etapa 2.
