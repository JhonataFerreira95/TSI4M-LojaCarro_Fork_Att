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

Para usar o banco em nuvem, os dois serviços podem compartilhar o schema
`jonathan`; cada aplicação cria/atualiza as tabelas das suas entidades ao
iniciar. O usuário MySQL precisa ter permissão de conexão e de criação/alteração
de tabelas nesse schema. Não coloque a senha no repositório: configure-a apenas
no ambiente de execução. Como a senha enviada na conversa foi exposta, troque-a
no MySQL antes de usar a conexão.

Configure as variáveis no terminal de cada processo (ou no gerenciador de
segredos do ambiente de implantação):

```powershell
$env:SPRING_PROFILES_ACTIVE = "cloud"
$env:MYSQL_HOST = "56.125.4.130"
$env:MYSQL_DATABASE = "jonathan"
$env:MYSQL_USERNAME = "aluno"
$env:MYSQL_PASSWORD = "<senha-nova-configurada-fora-do-repositorio>"
```

Inicie o serviço de usuários em um terminal:

```powershell
.\mvnw.cmd -f usuarios-service\pom.xml spring-boot:run
```

Em outro terminal, defina as mesmas variáveis de banco, configure a URL do
serviço de usuários e inicie a Loja:

```powershell
$env:USUARIOS_API_URL = "http://localhost:8081"
.\mvnw.cmd spring-boot:run
```

O perfil `cloud` configura o datasource MySQL nos dois serviços. O perfil
`demo` continua usando bancos H2 locais e temporários.

## CRUD de usuários

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/usuarios` | Cria usuário com `{"nome":"Ana Silva"}` |
| `GET` | `/usuarios/{id}` | Consulta usuário; também é a API chamada pela Loja |
| `GET` | `/usuarios` | Lista usuários |
| `PUT` | `/usuarios/{id}` | Atualiza o nome |
| `DELETE` | `/usuarios/{id}` | Exclui usuário |

A consulta por ID retorna `{"id":1,"nome":"Ana Silva"}`. No perfil `cloud`, o
serviço usa o mesmo schema MySQL `jonathan` da Loja, mantendo as tabelas dos
serviços separadas.

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

Sem broker externo, os serviços usam uma outbox transacional no próprio MySQL:
cada operação bem-sucedida grava metadados em `audit_outbox` na mesma
transação da operação. No perfil `cloud`, a Loja consome os eventos pendentes
periodicamente e os grava em `log_operacao`; a leitura de `GET /logs` também
processa eventos pendentes antes de responder. Se o consumidor falhar, a
transação mantém os eventos pendentes para nova tentativa. Os eventos contêm
serviço, módulo, operação, ID do registro, tipo de ator e data/hora, não o corpo
da requisição nem dados dos usuários. Para operações da Loja, o ator é o
administrador autenticado. O campo legado `carroId` permanece disponível para
operações de carro.

O `usuarios-service` permanece como serviço independente; restrinja sua porta a
uma rede interna em implantação, pois chamadas diretas a ele não passam pela
sessão do administrador da Loja. As operações diretas ainda são registradas na
outbox como originadas pelo serviço; use o proxy `/usuarios` da Loja quando a
operação também precisar ser associada ao administrador autenticado.
