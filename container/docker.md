# Docker

## Anotacoes aleatorias Curso de Docker

```
$docker run -it ubuntu bash
$docker build -t henri/app-node:1.0
$docker stop $(docker container ls)
$docker tag henri/app-node:1.0 henri2/app-node:1.0
$docker rm $(docker images ls -a) --force
$docker ps -s
$docker run -it -v /home/henri/vdocker:/app ubunut bash
$docker run –mount type=bind,source=/home/diretorio,target=/app nginx
$docker run –v /home/diretorio:/app nginx
$docker volume ls
$docker volume create qualquer-volume
$docker run -it -v qualquer-volume:\app bash
$docker run –mount type=tmpfs,source=/home/diretorio,target=/app nginx
$docker network create --drive bridge my-bridge
$docker run -it ubuntu --network my-bridge ubuntu bash

-d = detached
-p = ports
```

* Usar inspect pra pegar IP

## Anotacoes aleatorias curso de Docker Sworm

```
$docker-machine create -d virtualbox vm1
$docker-machine ssh vm1
$$docker swarm init --advertise-addr [ip-host]
$docker-machine create -d virtualbox vm2
$docker-machine create -d virtualbox vm3
$docker-machine ssh vm2
$$docker swarm join --token [token que aparece em $$docker swarm join-token worker]
$docker-machine ssh vm1
$$docker node ls -------------------------> comando que só a máquina Lider pode executar
```

### Remocao de nó

```
$docker-machine ssh vm2
$$docker swarm leave      ----------------------------> coloca como status down
$docker-machine ssh vm1
$$docker node rm [id que aparece em node ls] -----------------------> remove efetivamente
```

### Serviços

```
$docker-machine ssh vm1
$$docker service create -p 8080:3000 [imagem] ------------> cria um servico com um container que é alocado em uma vm aleatoria
$$docker container ls
$$docker container rm [id container criado]
$$docker service ls
$$docker service ps ul --------------> aparece que o container foi reiniciado
```

Com a criação de serviço, a rota para o container é compartilhado entre as máquinas independente da máquina em que ele está. Com isso, se tiver um container com uma aplicação web, ela poderá ser acessada pelo ip de qualquer uma das VMs por causa do Routing Mesh

### Fazendo backup do Swarm

```
$docker-machine ssh vm1
$$sudo su
$$cd /var/lib/docker/swarm
$$cp -r /var/lib/docker/swarm/ backup
```

Testando backup

```
$$docker node rm vm2 --force
$$docker node rm vm3 --force
$$docker swarm leave --force
$$cd /var/lib/docker/swarm ------------> pasta vazia
$$cp -r  backup/* /var/lib/docker/swarm/
$$docker swarm init --force-new-cluster --advertise-addr [ip]
```

### Adicionando manager

```
$$docker swarm join-token manager
$docker-machine create -d virtualbox vm4
$docker-machine ssh vm4
$$docker swarm join --token [token]
```

Quando a vm1 é desligada por exemplo com $$docker swarm leave --force, a vm4 vai virar lider. Caso tenha mais máquinas managers, ocorre uma eleicao pra nova maquina lider

Quanto mais managers -> Menos desempenho. Docker recomenda 3, 5, 7 devido ao algoritmo RAFT.

#### RAFT

N numero de managers
(N-1)/2 numero de falhas
(N/2)+1 quórum

### Removendo manager

```
$$docker node demote [id] -> rebaixamento a worker
$$docker node rm [id]
```

### Restricao de nós

```
$$docker service rm $(docker service ls -q) ---> removendo servicos testes que tao rodando
$$docker node update --availability drain [vm-name]
$$docker node update --availability active [vm-name]
```

### Restricao de servicos

```
$$docker service update --constraint-add node.role==worker [id-servico]
$$docker service update --constraint-add node.id==[id-node] [id-servico]
$$docker service update --constraint-add node.hostname==[vm-name] c
$$docker service update --constraint-rm node.id==[id-node] [id-servico]
$$docker service update --constraint-rm node.hostname==[vm-name] [id-servico]
```

### Replicas

```
$$docker service update --replicas 4 [id-servico]
$$docker service scale [id-servico]=4
```

#### Servico global

```
$$docker service create -p 8080:3000 --mode global [imagem] ----------> replica pra todas vms
```

#### Descobridor de serviços

```
$$docker network create -d overlay test_overlay
$$docker service create --name servico --network test_overlay --replicas 2 alpine sleep 1D
$$docker service ls ---------------------> copia o nome completo do servico, "servico.1.[id]"
$$docker exec -it servico.1.[id] sh
$$$ ping servico.2.[id] --------------------> irá funcionar exibindo o ping pro container replicado sem precisar informar o IP
```

O driver overlay é feito para comunicar multiplos hosts em uma mesma rede, mas tambem pode ser usado para conectar containers em escopo local. Permitindo, então, conectar serviços e containers standalone:

```
$$docker network create -d overlay --attachable my_overlay
```

### Stack Deploy

```
$$cat > docker-compose.yml
> Copiar e colar o arquivo aqui e dar enter
$$docker stack deploy --compose-file docker-compose.yml [nome-da-stack]
$$docker stack rm [nome-da-stack]
```

Por padrão, tanto o Docker no modo standalone quanto o Docker Swarm, partilham apenas de um driver local para uso de volumes. Isso quer dizer que o Docker Swarm não possui, até então, solução nativa para distribuir volumes entre os nós.
Ao definir o volume para cada serviço, é criado um volume local dentro de cada nó que for executar a tarefa. Logo, os volumes não são compartilhados entre os diferentes nós do cluster.
Existem soluções que não são nativas do Docker Swarm para utilizar volumes distribuídos entre nós, que podem ser encontradas na Docker Store.
