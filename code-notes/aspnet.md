# ASPNET

## Core x Framework

.NET Core é uma plataforma para desenvolvimento de aplicações criada e mantida pela Microsoft como um projeto de open-source. Sendo uma solução mais leve e modular que o .NET Framework e pode ser usada em diferentes sistemas operacionais como Windows, Mac e Linux. O .NET Framework que é suportado apenas no Windows.

Ambos os Frameworks compartilham muitos dos mesmos componentes e é possível compartilhar código entre os dois. No entanto, há diferenças fundamentais entre os dois para escolher um ou outro.

**1 - Considere usar o .NET Core se:**

- Você tiver necessidades de plataforma cruzada (Windows, macOS e Linux)
- Você estiver direcionando microsserviços.   
- Você estiver usando contêineres do Docker.
- Você precisar de alto desempenho e sistemas escalonáveis.
- Você precisar de versões do .NET correspondentes a cada aplicativo.

**2 - Considere usar o .NET Framework se:**

- Seu aplicativo usar o .NET Framework atualmente (a recomendação é estender em vez de migrar);
- Seu aplicativo usa bibliotecas .NET de terceiros ou pacotes NuGet não disponíveis para o .NET Core;
- Seu aplicativo usa tecnologias .NET que não estão disponíveis para o .NET Core;
  - Serviços WCF
  - VB.NET no Core não dá suporte a aplicações mobile
- Seu aplicativo usa uma plataforma que não oferece suporte ao .NET Core;

## Services

<table>
  <thead>
    <tr>
      <th>Tipo</th>
      <th>Mesma requisição</th>
      <th>Requisições diferentes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Singleton</td>
      <td>Mesma instância</td>
      <td>Mesma instância</td>
    </tr>
    <tr>
      <td>Scoped</td>
      <td>Mesma instância</td>
      <td>Nova instância</td>
    </tr>
    <tr>
      <td>Transient</td>
      <td>Nova instância</td>
      <td>Nova instância</td>
    </tr>
  </tbody>
</table>

Em um serviço *stateless* ou uma aplicação sem contexto de requisição, como um console por exemplo, Scoped pode ter o mesmo resultado de Transient, uma vez que se não for possível validar se está numa mesma requisição, sempre uma nova instância será criada.

## Qual técnica utilizar com o Entity Framework: Code First, Database First ou Model First?

### Database First

- Sistemas legados
- Banco de dados existente
- Cria o modelo em cima do que já existe e aí vai atualizando e modificando de acordo com a sua necessidade.
-  Trabalhar em uma equipe em que DBAs são responsáveis por modelar o banco de dados e depois os desenvolvedores irão fazer o sistema "em cima" do banco
-  Empresas que mantém as regras de negócio no banco em procedures, triggers, functions e etc e a aplicação é apenas uma "casca" que faz chamadas ao banco, onde está a lógica de verdade.

### Code First

- Quando as classes de modelo forem modificadas, o banco de dados irá refletir essas modificações
- Quando o banco de dados for apenas um lugar para armazenar dados mesmo, ou seja: quando não houver lógica de negócio no banco de dados
- O banco de dados não será alterado manualmente por desenvolvedores e/ou DBAs

### Model First

- Uso de ferramenta visual de modelagem e geração de Models em código. Exemplo: Entity Framework Designer
