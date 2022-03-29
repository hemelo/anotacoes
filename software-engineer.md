# Engenharia de Software

#### Design vs Arquitetura de Software

<img src="https://miro.medium.com/max/1400/1*zXB_ISU0aFma_Hp6D4NS1Q.png">

- Arquitetura de Software: visão de mais alto nível, separação de camadas, pastas da aplicação

- Design: mais baixo nível, padrões como SOLID

## Arquitetura Hexagonal

<img src="https://blog.allegro.tech/img/articles/2020-05-21-hexagonal-architecture-by-example/ha_example.png">

<img src="https://miro.medium.com/max/1400/1*LF3qzk0dgk9kfnplYYKv4Q.png">

## Arquitetura Clean

<img src="https://www.mytaskpanel.com/wp-content/uploads/2021/06/2021-05-31.jpg">

<img src="https://www.oncehub.com/hs-fs/hubfs/Marketing/02.%20Website%20assets/03.%20Blog/new-posts/explaining-clean-architecture/Payroll%20App%20Data%20flow%20diagram.png?width=1282&name=Payroll%20App%20Data%20flow%20diagram.png">

### Domain Driven Design

#### Padrões táticos
<img src="http://www.macoratti.net/11/05/ddd_pd1.gif">

#### Padrões estratégicos

- Divisão de contextos/funcionalidades - prevenir que uma aplicação quebre por completo, apenas pequenas partes quebrariam
- Domínio e subdomínio
- Linguagem ubíqua

## Arquitetura movida a eventos

<img src="https://docs.microsoft.com/pt-br/dotnet/architecture/microservices/multi-container-microservice-net-applications/media/microservice-application-design/eshoponcontainers-reference-application-architecture.png">

- Event Bus: Mensageria
- Exemplo de aplicação que abusa de event sourcing: Git

### Escalabilidade

- Horizontal: Instâncias (servidores)
- Vertical: Hardware
- Em profundidade


### Modelagem de eventos

1. Brainstorming

<img src="https://www.opus-software.com.br/wp-content/uploads/2021/03/imagem-1-event-modeling.png">

- Simplificação

<img src="https://www.opus-software.com.br/wp-content/uploads/2021/03/imagem-2-event-modeling.png">

2. Ordenação lógica

<img src="https://www.opus-software.com.br/wp-content/uploads/2021/03/imagem-3-event-modeling.png">

3. Storyboard

<img src="https://www.opus-software.com.br/wp-content/uploads/2021/03/imagem-4-event-modeling.png">

4. Identificando entradas

<img src="https://www.opus-software.com.br/wp-content/uploads/2021/03/definy-imputs-1500x460.png">

5. Identificando saídas

<img src="https://www.opus-software.com.br/wp-content/uploads/2021/03/Encontrados-os-modelos-de-visualiza%C3%A7%C3%B5es-do-sistema-1500x469.png">

6. Lei de conway

<img src="https://www.opus-software.com.br/wp-content/uploads/2021/03/apply-1500x509.png">

7. Elaboração de cenários

<img src="https://www.opus-software.com.br/wp-content/uploads/2021/03/elaborate.png">

#### Vantagens

<img src="https://media-exp1.licdn.com/dms/image/C5612AQFvZWT8anAZoA/article-inline_image-shrink_1500_2232/0/1563009403704?e=1652313600&v=beta&t=O-IO_T55_pWXgBkRFN5IUYQWSvRQGwH0QuTtLATGjMw">

## Arquitetura de microsserviços

<img src="https://docs.microsoft.com/en-us/azure/architecture/includes/images/microservices-logical.png">

<img src="https://storage.googleapis.com/gweb-cloudblog-publish/images/microservices_architecture.max-2000x2000.jpg">

### API

<img src="https://santexgroup.com/wp-content/uploads/2020/05/api-definition-01-1024x620.png">

#### Padrões de API

- SOAP
- REST
- RPC

#### API Gateway

<img src="https://docs.microsoft.com/pt-br/dotnet/architecture/microservices/architect-microservice-container-applications/media/direct-client-to-microservice-communication-versus-the-api-gateway-pattern/custom-service-api-gateway.png">

- Cache
- SSL
- Logging

##### Comportamentos

- Autorização e redirecionamento
- Uso de decorator pra adicionar informações necessárias nos requests
- Limitar o acesso ou conteúdo trafegado

#### Service Mesh

Camada dedicada de infraestrutura para observação, testes, gerenciamento de tráfego e segurança de serviços. Ela é adicionada, existem ferramentas para isso

<img src="https://miro.medium.com/max/1200/1*ROUp0-7rYZQXWq42l3Yuhg.png">

<img src="https://istio.io/latest/img/service-mesh.svg">

### Tipos de microsserviços

- **Data service:** conexão com banco de dados
- **Business service:** regras de negócio, ex: validação de dados
- **Translation service:** conector com API externa, feito para facilitar manutenção
- **Edge service:** define comportamento, ex.: envia dados diferentes de uma API para Mobile ou Web

### CQRS - Command Query Responsibility Seggregation

Para microsserviços com muito tráfego

- Leitura e escrita separados
- O modelo de leitura pode ter informações agregadas de outros domínios
- O modelo de escrita pode ter dados sendo automaticamente gerados
- Aumenta MUITO a complexibilidade do sistema

### Falhas em comunicação síncrona

- Uso de Circuit Breaker & Cache no Gateway

### Asynchronous Events

- Determinados problemas não podem ser resolvidos imediatamente
- Um serviço emite um evento que será tratado em seu devido tempo
- Serviços de mensageria/stream de dados brilham

### Falhas em comunicação assíncrona

- Retry
- Retry com back-off
- Fila de mensagens mortas
- Mensagens devem poder ser lidas fora de ordem
- Mensagens devem poder ser repetidas rapidamente (idempotência)

### Componentes

São servidores ou aplicacoes de infra

<img src="images/microsservicecomponents">

### Independência

- Apenas faça modificações aditivas
  - Novos endpoints
  - Novos campos opcionais em cada recurso
- Versionamento de APIs
  - Ao lançar uma v2, a v1 deve continuar funcionando inalterada
- Manter equipes separadas, donas de cada serviços
  - A mesma equipe não vai alterar os clientes
  - Para adicionar funcionalidades que dependem de outros, solicitações formais podem ser feitas

### Definição de um padrão

- Criação de Logs
- Health checks
- Métricas
- Busca por configs e secrets  

### Técnicas de autenticação

- Basic http
- Tokens jwt
- OAuth
- OpenID connect

### Técnicas de autorização

- ACL (Acess Control List)
- RBAC (Role-based access control)
- On behalf of

### Firewall

<img src="images/firewall.png">

### Como me proteger mais?

- Atacantes (hackers) utilizam ferramentas modernas. Conheça essas ferramentas! Estude os possíveis ataques
- Ter uma equipe de infosec e executar pentests
- Automatize verificações de segurança. Fazer requisições com certificados, usuários não autorizados, etc
- Monitorar e detectar ataques em tempo real
- Ter logs e auditar os sistemas com freq.

### Ambientes

- Dev
- Staging / QA
- Homologação
- Prod

#### Configurações parametrizadas

- Configuração do ambiente em si
  - Quantidade de recursos
  - Localização
- Configurações da aplicação
  - Destino de logs
  - Dependências
  - Dados de acesso

### Estratégias de releases

- Rolling upgrade: atualizar os servidores aos poucos
- Blue-green: redirecionamento pra novos servidores, se der ruim tira o redirecionamento
- Feature-toggle: ativar a nova release apenas para poucas pessoas

### Comparação: Arquitetura Monolítica x Clean x de microsserviços

<img src="https://www.smartwavesa.com/wp-content/uploads/2019/04/2019-04-Microservices-Gartner.png">

#### Desvantagens

- Maior complexidade de desenvolvimento e infra
- Debug mais complexo
- Comunicação entre os serviços deve ser bem pensada
- Monitoramento é crucial
- Diversas tecnologias pode ser um problema

Leitura: https://martinfowler.com/bliki/MonolithFirst.html
