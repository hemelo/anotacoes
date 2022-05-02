# REST(Representational State Transfer)

Modelo de arquitetura que fornece diretrizes/dados para que os sistemas se comuniquem diretamente usando os princípios e protocolos existentes da Web sem a necessidade de protocolos como o SOAP (Simple Object Access Protocol)

**E o que é o SOAP?**

O SOAP é:

- Mecanismo para definir a unidade de comunicação
- Protocolo para troca de informações
- Mecanismo para lidar com erros

### Características

- Fornece acesso aos recursos para que o cliente REST acesse e renderize os recursos no lado do cliente
- *Identificação de cada recurso:* URI ou IDs globais
- *Representação:* XML, JSON, Texto, Imagens e assim por diante

### Responsabilidades
Existe no REST um princípio chamado ***STATELESSNESS*** (sem estado), onde o servidor não precisa saber em qual estado o cliente está e vice-versa. Mas o que é um servidor e um cliente?

- **Cliente:** Solicitante de um serviço e envia solicitações para vários tipos de serviços ao servidor

- **Servidor:** Provedor de serviços e fornece continuamente serviços ao cliente conforme as solicitações

1. Separação de responsabilidades entre a interface do usuário e o armazenamento de dados. Ou seja, quando uma solicitação REST é realizada, o servidor envia uma representação dos estados que foram requeridos.

2. Não há limite superior no número de clientes que podem ser atendidos por um único servidor

3. Não é obrigatório que o cliente e o servidor residam em sistemas separados.

4. A comunicação entre cliente e servidor ocorre através da troca de mensagens usando um padrão de solicitação-resposta > O cliente basicamente envia uma solicitação de serviço e o servidor retorna uma resposta.

### Requisições e comunicações

Uma requisição consiste em:

- Um **método HTTP**, que define que tipo de operação o servidor vai realizar;
- Um **header**, com o cabeçalho da requisição que passa informações sobre a requisição;
- Um **path** para o servidor
- Informação no corpo da requisição, sendo esta informação opcional.

## Métodos HTTP

- GET solicitar que um servidor envie um recurso
- POST enviar dados de entrada para o servidor
- PUT edita e atualiza documentos em um servidor
- DELETE que como o próprio nome já diz, deleta certo dado ou coleção do servidor.

## Códigos de Respostas

- 200 (OK), requisição atendida com sucesso;
- 201 (CREATED), objeto ou recurso criado com sucesso;
- 204 (NO CONTENT), objeto ou recurso deletado com sucesso;
- 400 (BAD REQUEST), ocorreu algum erro na requisição (podem existir inumeras causas);
- 404 (NOT FOUND), rota ou coleção não encontrada;
- 500 (INTERNAL SERVER ERROR), ocorreu algum erro no servidor.
