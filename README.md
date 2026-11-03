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

Antes de iniciar a Loja pela primeira vez, configure as credenciais do primeiro
administrador. Escolha uma senha exclusiva e não a compartilhe:

```powershell
$env:ADMIN_BOOTSTRAP_NAME = "Administrador"
$env:ADMIN_BOOTSTRAP_EMAIL = "admin@exemplo.com"
$env:ADMIN_BOOTSTRAP_PASSWORD = "<uma-senha-forte>"
```

O administrador inicial só é criado se ainda não houver administradores. Abra
`http://localhost:8080/` e entre com essas credenciais. Após o login, a interface
permite gerenciar carros, usuários, administradores, vendas, test drives,
contabilidade e logs de auditoria.

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

## CRUD dos módulos

Além de carros e usuários, a Loja disponibiliza formulários e API REST para
administradores, vendas, test drives e lançamentos contábeis. Os dados desses
módulos ficam no banco da Loja; usuários continuam no `usuarios-service`.

| Módulo | Rotas | Campos |
|---|---|---|
| Administradores | `/administradores` | `nome`, `email`, `senha` |
| Vendas | `/vendas` | `carroId`, `usuarioId`, `data`, `valor`, `status` |
| Test drives | `/test-drives` | `carroId`, `usuarioId`, `dataHora`, `status` |
| Contabilidade | `/lancamentos-contabeis` | `vendaId`, `tipoLancamento`, `valor`, `vencimento`, `pagamento`, `status` |

Cada rota aceita `GET` para listar e `GET /{id}`, `POST`, `PUT /{id}` e
`DELETE /{id}` para consultar, criar, atualizar e excluir. Datas usam o formato
ISO (`YYYY-MM-DD`; test drives usam `YYYY-MM-DDTHH:mm:ss`). O campo
`pagamento` é a data do pagamento e pode ser nulo. Vendas e test drives validam
carros e usuários existentes; lançamentos contábeis validam a venda. Não é
permitido excluir carros ou usuários vinculados a vendas ou test drives, nem
vendas com lançamentos contábeis.

As senhas de administradores são armazenadas com hash BCrypt e nunca aparecem
nas respostas da API.

## Segurança e auditoria

Todas as APIs da Loja, exceto o login e a obtenção do token CSRF, exigem uma
sessão autenticada de administrador. Operações de escrita também exigem o token
CSRF que a interface obtém automaticamente. A senha do administrador é
armazenada com BCrypt. Para criar o primeiro administrador em outro ambiente,
configure `ADMIN_BOOTSTRAP_NAME`, `ADMIN_BOOTSTRAP_EMAIL` e
`ADMIN_BOOTSTRAP_PASSWORD` antes de iniciar a Loja; se já existir um
administrador, as variáveis não criam outro.

Operações bem-sucedidas são gravadas na tabela `log_operacao` com ID/nome do
administrador autenticado, módulo, operação, ID do registro e data/hora. O
campo legado `carroId` permanece disponível para operações de carro. Os
registros podem ser consultados em `GET /logs` após o login.

O `usuarios-service` permanece como serviço independente; restrinja sua porta a
uma rede interna em implantação, pois chamadas diretas a ele não passam pela
sessão nem pela auditoria da Loja. Para as operações auditadas, use o proxy
`/usuarios` da Loja.
