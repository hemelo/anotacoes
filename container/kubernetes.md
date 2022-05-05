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

```yaml
# portal-noticias.yaml

apiVersion: v1
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

```yaml
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

```yaml
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

```yaml
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

```yaml
# pod-1.yaml

apiVersion: v1
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

```yaml
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

```yaml
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

```yaml
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

```yaml
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
  replicas: 3
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

```yaml
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
  replicas: 3
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

#### Volume  

* Com uso de volume local

```yaml
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
            type: DirectoryOrCreate
  replicas: 3
  selector:
    matchLabels:
      app: portal-noticias
```

Incluir /C/users/henri/desktop/volume como volume no Docker Desktop no Windows e /home/volume no Linux

#### Persistent Volume

* Esquema de volume usado bastante na nuvem

```yaml
# persistent-volume.yaml

apiVersion: v1
kind: Deployment
metadata:
  name: pv-1
spec:
  capacity:
    storage: 10Gi
  accessMode:
    - ReadWriteMany
    # Many = Múltiplos pods por vez / Once = Único pod por vez
  gcePersistentDisk:
    pdName: pv-disk
    # Nome do disco na nuvem
  storageClassName: standard
```

```yaml
# persistent-volume-claim.yaml

apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: pvc-1
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 10Gi
  storageClassName: standard
```

```yaml
# testando-persistent-volume.yaml

apiVersion:v1
kind: Pod
metadata:
  name: pod-pv
spec:
  containers:
    - name: nginx-container
      image: nginx-latest
      volumeMounts:
        - mountPath: /volume-dentro-do-container
          name: primeiro-pv
  volumes:
    - name: primeiro-pv
      persistentVolumeClaim:
        claimName: pvc-1
```

#### Storage Classes

Criando Persistent Volumes dinamicos

```yaml
# storage-class-google-cloud-platform.yaml

apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: slow
provisioner: kubernetes.io/gce-pd
parameters:
  type: pd-standard
  fstype: ext4
```

```yaml
# persistent-volume-claim.yaml

apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: pvc-1
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 10Gi
  storageClassName: slow
```

Dessa forma, ao rodar o Claim no Shell da google, será criado um disco de 10Gb

#### Statefull Set

Cada pod tem um identificador, então se falhar não será substituido mas sim reiniciado com mesmo ID

```yaml
apiVersion: apps\v1
kind: StatefulSet
metadata:
  name: sistema-armazenamento-noticias
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: sistema-noticias
      name: sistema-noticias
    spec:
      containers:
        - name: sistema-noticias-container
          image: aluracursos/sistema-noticias:1
          ports:
            - containerPort: 80
          envFrom:
            - configMapRef:
                name: sistema-configmap
          volumeMounts:
            - name: imagens
              mountPath: /var/www/html/uploads
            - name: sessao
              mountPath: /tmp
    volumes:
      - name: imagens
        persistentVolumeClaim:
          claimName: imagens-pvc
      - name: sessao
        persistentVolumeClaim:
          claimName: sessao-pvc
    replicas: 3
    selector:
      matchLabels:
        app: sistema-noticias
    serviceName: svc-sistema-noticias
```

#### Liveness and Readliness Probes

Objetivo: Prever erros e forçar restart de pods

```yaml
# portalnoticias-deployment.yaml

apiVersion: apps/v1
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
          livenessProbe:
            httpGet:
              path: /
              port: 80
            periodSeconds: 10
            failureThreshold: 3
            initialDelaySeconds: 20
          readlinessProbe:
            httpGet:
              path: /inserir-noticias.php
              port: 80
            periodSeconds: 10
            failureThreshold: 3
            initialDelaySeconds: 3  
  selector:
    matchLabels:
      app: portal-noticias
```

#### Horizontal Pod Autoscale

```yaml
# portal-noticias-hpa.yaml

apiVersion: autoscaling/v2beta2
kind: HorizontalPodAutoscaler
metadata:
  name: portal-noticias-hpa
spec:
  scaleTargetRef:
    apiVersion: app/v1
    kind: Deployment
    name: portal-noticias-deployment
  minReplicas: 1
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 50
```

```yaml
# portalnoticias-deployment.yaml

apiVersion: apps/v1
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
          livenessProbe:
            httpGet:
              path: /
              port: 80
            periodSeconds: 10
            failureThreshold: 3
            initialDelaySeconds: 20
          readlinessProbe:
            httpGet:
              path: /inserir-noticias.php
              port: 80
            periodSeconds: 10
            failureThreshold: 3
            initialDelaySeconds: 3  
          resources:
            requests:
              cpu: 10m
  selector:
    matchLabels:
      app: portal-noticias
```

OBS: Setar o kubernet de métricas, que está em /iac/metricas.yaml: `$ kubectl apply -f  /iac/metricas.yaml` e no linux é `$ minikube addons enable metrics-server`

Testando infraestrutura

```
$ kubectl get hpa
$ sh stress.sh 0.001 > out2.txt
$ kubectl get hpa --watch ---------------------------> Exibirá o consumo subindo absurdamente
```

#### Vertical Pod Autoscale
