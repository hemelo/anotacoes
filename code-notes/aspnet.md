# ASPNET

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
