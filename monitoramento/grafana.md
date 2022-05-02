# Grafana

Para utilizar o grafana, utilizei uma imagem simples do ubuntu provisionada pelo vagrant

Provisionei o Docker com os comandos:

```
# Download do script
$$ curl -fsSL https://get.docker.com -o get-docker.sh

# Instalando
$$ sh get-docker.sh

# Dando permissão para o usuário
$$ sudo usermod -aG docker vagrant

# Recarregando as permissões
$$ exit
$ vagrant ssh

# Testando a instalação
$$ docker ps

```

Provisionei o grafana com os comandos:

```
$$ mkdir -p  $PWD/grafana-alura/volumes/grafana
$$ cd $PWD/grafana-alura/

# Criando a rede do docker para o grafana
$$ docker network create grafana-net

# Definindo a instalação do grafana para o user atual e instalando
$$ ID=$(id -u)
$$ docker run -d --user $ID                  \
    -v "$PWD/volumes/grafana:/var/lib/graf  \
    -p 3000:3000                            \
    --name=grafana                          \
    --network=grafana-net                   \
    grafana/grafana
$$ docker ps
```

Como o grafana só exibe, cria e gere paineis com gráficos e detalhes de monitoramento e infraestrutura, é preciso de programas externos e compatíveis para armazenar e enviar os dados para ele. Pra isso usei o InfluxDB

```
$ mkdir -p $PWD/volumes/influxdb

$ docker run -d -v "$PWD/volumes/influxdb:/var/lib/influxdb"    \
    -p 8083:8083                                                \
    -p 8086:8086                                                \
    -p 25826:25826/udp                                          \
    --name=influxdb                                             \
    --network=grafana-net                                       \
    influxdb:1.0
```

Além disso é preciso ter um coletor de métricas para enviar os dados para o banco de dados de métricas. Neste caso utilizei o Telegraf.

```
$wget -qO- https://repos.influxdata.com/influxdb.key | sudo apt-key add -
$source /etc/lsb-release
$echo "deb https://repos.influxdata.com/${DISTRIB_ID,,} \
    ${DISTRIB_CODENAME} stable" |                   \
    sudo tee /etc/apt/sources.list.d/influxdb.list
$sudo apt-get update && sudo apt-get install telegraf
$sudo service telegraf start
```

Ver as métricas pelo próprio GIT:

```
# Conectando no Influx
$ docker exec -ti influxdb bash
$ influx

# Acessando as métricas
> use telegraf
> show measurements;
> exit
$ exit
```

Acessar o grafana por `[ip]:3000` no navegador e:

###### Configurar a database

Configuration > Data Sources > Add data source > InfluxDB

- Name: InfluxDB
- URL: [ip]:8086
- Access: Browser
- Database: telegraf
- Save & Test


###### Criar filtro por servidor

O telegraf pode ser instalado em inumeras máquinas servidores e salvar os dados em um banco de dados central, sendo assim é possível criar esse filtro. No meu caso só usei uma máquina com Telegraf.

Create > Dashboard > Configurações

General > Name: Nome qualquer
Variables > Type: Query
Variables > Name: server
Variables > Type: Query
Variables > Data source: InfluxDB
Variables > Query: SHOW TAG VALUES FROM system WITH KEY=host
New Panel > Add Query

##### Criando gráficos

<img src="imagens/1.jpg" />
