# Development and Operations

<img width="75%" src="https://www.auctus.com.br/wp-content/uploads/2017/09/devops-process.png" />

- **Equipe de desenvolvimento:** altera o codigo e implementa novas funcionalidades
- **Equipe de operações:** se preocupe com a estabilidade e desempenho do sistema em produção

 A ideia é que engenheiros trabalham em todo o ciclo de vida da aplicativo, da fase de desenvolvimento e testes até a implantação e as operações. Em geral, eles não são limitados a uma única função, são full cycle (não full stack)

### Práticas comuns

 - Infraestrutura como código (IaC)
 - Pipeline de construção do software (integração e entrega contínua)
 - Virtualização e conteinerização
 - Feedback constante da aplicação em produção através de monitoramento

## DevSecOps

<img width="80%" src="https://www.primecontrol.com.br/wp-content/uploads/2019/10/diagrama-devsecops.png">

- Segurança em primeiro lugar
- Velocidade
- Entrega rápida de contínua (CI/CD)
- Confiabilidade

Uso de GitLab, Jenkins, CodePipeline, BitBucket

## Observabilidade

- Métricas

Prometheus - Banco de dados de métricas <br>
Grafana - Visualização

<img src="https://docs.microsoft.com/pt-br/azure/devops/service-hooks/services/media/grafana/dashboard-with-annotations.png?view=azure-devops">

- Traços distribuídos

Jagger - Possibilita instrumentação ao nível de aplicação ou a nível de infra. Possibilita colocar Service Mesh (Istio), Proxies (Envoy, Traefik, Kong)

<img src="https://opensource.com/sites/default/files/uploads/distributed-trace.png">

- Logs

<img src="/images/logger.png">
<img src="/images/loggercfg.png">

Opções: Sidecar, Logging agent instalado no host, Shell script <br>

Ferramentas: Graylog

<img src="https://e-tinet.com/wp-content/uploads/2019/04/graylog.png">

Ver depois: Netdata com Slack

## Impacto sob a perspectiva do cliente (4 Golden Signals)

Coisas que são importantes considerando o cliente com exemplos

- Latência
  - Rapidez de conseguir um táxi
- Erros
  - Transferência bancária
- Tráfego
  - Divulgação de promoção
  - Votação do BBB
- Saturação
  - Transferências por minuto
  - Downloads por minuto

## Containers vs VMs

<img src="https://media-exp1.licdn.com/dms/image/C5612AQFIWN_K9-VXPA/article-cover_image-shrink_600_2000/0/1520137261433?e=1652918400&v=beta&t=zSZNsiO2GoxWx8WCs3Upp3gK7TauZ_6Hsgp3cydw_XU">

## Serverless

Facilitamento do processo de deploy na nuvem. É um paradigma de executar código sem se preocupar com servidores. Ao contrário do que o nome sugere, os servidores ainda existem

- Amazon Web services
- Microsoft Azure
- Google Cloud

### Prós

- Paga somente pelo que usa
- Cada função pode ser criada em uma linguagem diferente
- Muitos eventos pré-configurados na Cloud ajudam a criar arquiteturas orientadas a eventos
- Auto escalável, altamente disponível por natureza

### Contras

- Duração de execução limitada
- Vendor Lock-In
- Difícil de debugar
- Configuração extra para controlar (parcialmente) o ambiente de execução (lambda layers)

### Componentes

<img src="https://miro.medium.com/max/929/1*AbVG9wBBOF2xCUdcEIo7Yw.png">

Além desses tem o SQS, SNS relacionados a Mensageria e Tópicos

### Custo

<img src="https://info.itemis.com/hubfs/Blog%20(2019)/Web%20Engineering/HOW%20SERVERLESS%20ARCHITECTURES%20CHANGE%20DEVELOPMENT%20PROCESS%20AND%20PROJECT%20BUSINESS/Grafik_How%20serverless%20architectures%20changes%20development%20process%20and%20project%20business_01.jpg">

## IoC - Infraestrutura como código

Dockerfile, dockercompose.yml, heroku.yml, setup-vagrant.sh.....

- Facilidade de implantação na nuvem
