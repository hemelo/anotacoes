# Kubernetes

Escabilidade vertical - + Hardware
Escabilidade horizontal - +

###### Estrutura

Master (Api + C-m + Sched + Etcd)

- Gerenciar o cluster
- Manter e atualizar o estado desejado
- Receber e executar novos comandos

Node (Kubelet + K-proxy)

- Executar as aplicações

###### Comunicação

A comunicação pra fazer qualquer coisa é sempre com a API por meio de requests declarativas ou imperativas. Para comunicar com a API é preciso do **kubectl**

## Pods

- Agrupamento de containers
- São restartados quando TODOS os containers dentro falham
- Compartilham namespaces de rede e IPC
- Podem compartilhar volumes  

### Criando o o primeiro pod

Utilizando o Kubernet do Docker

```
$ kubectl run nginx-pod --image=nginx:latest
$ kubectl describe pod nginx-pod
$ kubectl edit pod nginx-pod ------------------> Abre um editor de texto
$ kubectl delete pod nginx-pod
```

E se... YamL?

```
# portal-noticias.yaml

apiVersion:v1
kind: Pod
metadata:
  name: portal-noticias
spec:
  containers:
    - name: portal-noticias-container
      image: aluracursos/portal-noticias:1
```

```
$ kubectl apply -f .\portal-noticias.yaml
$ kubectl get pods --watch
// $ kubectl delete -f .\portal-noticias.yaml
```

O projeto acima é de um site, entao para acessar é só pegar o ip em `$kubectl describe pod portal-noticias` ou `$kubectl get podes -o wide`. No entanto, o site tá mal configurado ainda (sem mapeamento) e não dá pra acessar via navegador, somente por:

```
$ kubectl exec -it portal-noticas -- bash
$$ curl localhost
```

#### Recurso SVC - Service

- Abstrações para expor aplicações executando em um ou mais pods
- Proveem IP's fixos para comunicação
- Proveem um DNS para um ou mais pods
- São capazes de fazer balanceamento de carga

Tipos: ClusterIP, NodePort, LoadBalancer

- Cluster IP - Compartilhamento entre pods
- NodePort - ClusterIP com compartilhamento externo
- LoadBalancer - Envia conexões para o primeiro servidor no pool até atingir a capacidade e, em seguida, envia novas conexões para o próximo servidor disponível. Esse algoritmo é ideal para ambientes hospedados.

```
# svc-pod-1-cluster.yaml

apiVersion: v1
kind: Service
metadata:
  name: svc-pod-1
spec:
  type: ClusterIP
  selector:
    app:  primeiro-pod
  ports:
    - port: 8080
      targetPort: 80
```

```
# svc-pod-1-node.yaml

apiVersion: v1
kind: Service
metadata:
  name: svc-pod-1
spec:
  type: NodePort
  selector:
    app:  primeiro-pod
  ports:
    - port: 80
      nodePort: 30000
```

```
# svc-pod-1-balancer.yaml

apiVersion: v1
kind: Service
metadata:
  name: svc-pod-1
spec:
  type: LoadBalancer
  selector:
    app:  primeiro-pod
  ports:
    - port: 80
      nodePort: 30000
```

```
# pod-1.yaml

apiVersion:v1
kind: Pod
metadata:
  name: pod-1
  labels:
    app: primeiro-pod
spec:
  containers:
    - name: container-pod-1
      image: nginx:latest
      ports:
        - containerPort: 80
```

#### Resetando tudo

```
$ kubectl delete pods -all
$ kubectl delete svc -all
```

#### Variáveis de ambiente

```
# database.yml

apiVersion: v1
kind: Pod
metadata:
  name: database
  labels:
    app: database
spec:
  containers:
    - name: database-container
      image: mysql:latest
      ports:
        - containerPort: 3306
      # env:
        - name: "MYSQL_ROOT_PASSWORD"
          value: "123456"
        - name: "MYSQL_DATABASE"
          value: "db"
        - name: "MYSQL_PASSWORD"
          value: "123456"
```

Separando variaveis

```
# database-config.yaml

apiVersion: v1
kind: ConfigMap
metadata:
  name: database-config
data:
  MYSQL_ROOT_PASSWORD: 123456
  MYSQL_DATABASE: db
  MYSQL_PASSWORD: 123456
```

```
$ kubectl apply -f .\database-config.yaml
$ kubectl get configmap
$ kubectl describe configmap database-config
```

Utilizando em alguma dependencia

```
# database.yml

apiVersion: v1
kind: Pod
metadata:
  name: database
  labels:
    app: database
spec:
  containers:
    - name: database-container
      image: mysql:latest
      ports:
        - containerPort: 3306
      envFrom:
        - configMapRef:
            name: database-config
```

E pra usar o banco de dados? Basta criar um Cluster Service e passar como endereco pro banco de dados o [nome-servico-banco-de-dados]:3306, o nome-servico é um DNS que redireciona automaticamente pro IP

#### Replica Sets

```
# portal-noticias.yaml

apiVersion: v1
kind: ReplicaSet
metadata:
  name: app-replicaset
  labels:
    app: app-replicaset
spec:
  template:
    metadata:
      name: portal-noticias
      labels:
        app: portal-noticias
    spec:
      containers:
        - name: portal-noticias
          image: portal-noticas:1
          ports:
            - containerPort: 80
          envFrom:
            - configMapRef:
                name: database-config
  replicas:3
  selector:
    matchLabels:
      app: portal-noticias
```

```
$ kubectl get replicasets
$ kubectl get rs --watch
$ kubectl get pods
$ kubectl delete pod app-replicaset-[id]
$ kubectl get pods -> exibe um novo pod com outro id que substitui o removido automaticamente
```

#### Deployments

Deployment é basicamente um replicaset com mais funcionalidades. Quando criados, Deployments auxiliam com controle de versionamento e criam um ReplicaSet automaticamente.

```
# nginx-deployment.yaml

apiVersion: v1
kind: Deployment
metadata:
  name: nginx-deployment
spec:
  template:
    metadata:
      name: nginx-pod
      labels:
        app: nginx-pod
    spec:
      containers:
        - name: nginx-container
          image: nginx-stable
          ports:
            - containerPort: 80
  replicas:3
  selector:
    matchLabels:
      app: nginx-pod
```

```
$ kubectl apply -f .\nginx-deployment.yaml
$ kubectl get pods
$ kubectl get rs
$ kubectl get deployments
$ kubectl rollout history deployment nginx-deployment
$ kubectl apply -f .\nginx-deployment.yaml --record
$ kubectl rollout history deployment nginx-deployment
$ kubectl annotate deployment nginx-deployment kubernets.io/change-cause="Definindo a imagem com versão latest"
$ kubectl rollout undo deployment nginx-deployment --to-revision=2
```


##### Resetando tudo

```
$ kubectl delete pods -all
$ kubectl delete svc -all
$ kubectl delete deployments -all
$ kubectl delete rs -all
```

##### Deployment de projeto real

```
# nginx-deployment.yaml

apiVersion: v1
kind: Deployment
metadata:
  name: portal-noticias-deployment
spec:
  template:
    metadata:
      name: portal-noticias
      labels:
        app: portal-noticias
    spec:
      containers:
        - name: portal-noticias-container
          image: aluracursos/portal-noticias:1
          ports:
            - containerPort: 80
          envFrom:
            - configMapRef:
                name: portal-configmap
          volumeMounts:
            - mountPath: /volume-dentro-do-container
              name: volume
      volumes:
        - name: volume
          hostPath:
            path: /C/users/henri/desktop/volume
            type: Directory
  replicas:3
  selector:
    matchLabels:
      app: portal-noticias
```

Incluir /C/users/henri/desktop/volume como volume no Docker Desktop
