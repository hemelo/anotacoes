# Integracao Continua

Vantagens

- Agregar todo o processo de construção de artefatos
- Controle de qualidade e entrega de software em um fluxo contínuo e transparente para todos os envolvidos
- Melhora na cobertura de qualidade do código do projeto, visto que é possível analisar o produto durante o processo de construção
- Diminuição do time to market na entrega do produto para o mercado


## Inicializando a VM com Jenkins

O Jenkins é um servidor de Integração Contínua open-source escrito em Java. Ele é o mais popular mas não a única opção. Outros servidores de *Integração Contínua * são TeamCity, Bamboo, Travis CI ou Gitlab CI entre vários outros.


```shell
$vagrant plugin install vagrant-disksize
$vagrant up
$vagrant ssh

# Testar se banco de dados e databases foram criadas

$ps -ef | grep -i mysql
$mysql -u devops -p
> PASSWORD: mestre
> show databases
$mysql -u devops_dev -p
> PASSWORD: mestre
> show databases

# Instalar Jenkins

$cd /vagrant/scripts
$sudo ./jenkins.sh
$ip addr --------------------------> 192.168.33.10 ----------> Definido no Vagrantfile

Acessar 192.168.33.10:8080 no navegador e colar a chave do comando abaixo

$sudo cat /var/lib/jenkins/secrets/initialAdminPassword

Instalar plugins sugeridos e criar um novo usuário com as seguintes configurações no formulário web

Nome de usuário: mestre
Senha: mestre
Nome completo: Jenkins
Email: example@example.com.br

$vagrant reload
```

## Projeto de treinamento usando Jenkins

É um TODO list que se encontra em /jenkins/example-project.

### Versionando a aplicação

```shell
# Bem básico, gerando autenticacao na VM

$ssh-keygen -t rsa -b 4096 -C "x@gmail.com"
$cat ~/.ssh/id_rsa.pub
CHAVE SSH pra colocar nas config do github
$git config --global user.name "hemelo"
$git config --global user.email "x@gmail.com"
$ssh -T git@github.com

# Iniciando repositorio
$cd /vagrant/example-project
$git init
$git add .
$git commit -m "Initial commit"
$git log

# Remotamente
$$ git remote add origin git@github.com:hemelo/jenkins-todo-list.git
$$ git push -u origin master
```

## Integrando jenkins com github

```
$$ cat ~/.ssh/id_rsa
```

No navegador: Credentials > Jenkins > Global Credentials > Add Crendentials > SSH Username with private key [ github-ssh ] [ user: git ]


## Primeiro Job no Jenkins

No navegador: Novo job > **jenkins-todo-list-principal** do tipo Freestyle. Esse job vai fazer o build do projeto e registrar a imagem no repositório.

Repositorio Git: git@github.com:hemelo/jenkins-todo-list.git [ credential github-ssh ]
Branch: master

Configurações adicionais

- [x] Delete workspace before build starts
- [x] Consultar periodicamente o SCM. Value: * * * * *

- [x] Salvar

Qual a diferença entre o Git Log de consulta periódica e o log da Saída do console, em um job no Jenkins?

`O primeiro log mostra as consultas periódicas, configuradas no job, ao repositório, e somente após alguma alteração ele vai prosseguir com o build, onde seus logs podem ser acessados pela Saída do console.`

## Processo de build

### Manual

#### Variáveis de ambiente

```shell
$cd /vagrant/example-project/to_do/
$vi .env
[config]
# Secret configuration
SECRET_KEY = 'r*5ltfzw-61ksdm41fuul8+hxs$86yo9%k1%k=(!@=-wv4qtyv'

# conf
DEBUG=True

# Database
DB_NAME = "todo_dev"
DB_USER = "devops_dev"
DB_PASSWORD = "mestre"
DB_HOST = "localhost"
DB_PORT = "3306"
```

**Arquivos .env não devem ser subidos pro repositório remoto de jeito nenhum**. Para evitar isso tem ferramentas como o venv do python.

```shell
$sudo pip3 install virtualenv nose coverage nosexcover pylint
$cd ../    
$virtualenv  --always-copy  venv-django-todolist
$source venv-django-todolist/bin/activate
$pip install -r requirements.txt
$python manage.py makemigrations
$python manage.py migrate
$python manage.py createsuperuser
$python manage.py runserver 0:8000
```

Pronto! Só acessar http://192.168.33.10:8000

### Automatizado

Dando usermod ao Docker

```shell
$sudo usermod -aG docker $USER
$sudo usermod -aG docker jenkins

```

Expondo o Docker para o Jenkins

```shell
$sudo mkdir -p /etc/systemd/system/docker.service.d/
$sudo vi /etc/systemd/system/docker.service.d/override.conf
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd -H fd:// -H tcp://0.0.0.0:2376
$sudo systemctl daemon-reload
$sudo systemctl restart docker.service
```

Instalando plugin `Docker` no jenkins: Gerenciar Jenkins > Gerenciar Plugins > Disponíveis

Colocando o docker como remoto: Install without restart > Depois reiniciar o jenkins
Gerenciar Jenkins > Configurar o sistema > Nuvem

- [X] Name - docker
- [X] Docker Host URI - tcp://127.0.0.1:2376
- [X] Enabled

Editando o primeiro job criado adicionando as duas steps:

- Step 1
  - *Tipo*: Shell Command
  - *Comando*: $ docker run --rm -i hadolint/hadolint < Dockerfile
- Step 2
  - *Tipo*: Build / Publish Docker Image
  - *Directory for Dockerfile*: ./
  - *Cloud*: docker
  - *Image*: django-todo-list

Instalando plugin `Config File Provider` no jenkins: Gerenciar Jenkins > Gerenciar Plugins > Disponíveis

Criar custom files com os ambientes de dev e prod:

```py
# Name: .env-dev

[config]
# Secret configuration
SECRET_KEY = 'r*5ltfzw-61ksdm41fuul8+hxs$86yo9%k1%k=(!@=-wv4qtyv'

# conf
DEBUG=True

# Database
DB_NAME = "todo_dev"
DB_USER = "devops_dev"
DB_PASSWORD = "mestre"
DB_HOST = "localhost"
DB_PORT = "3306"
```

```py
# Name: .env-prod

[config]
# Secret configuration
SECRET_KEY = 'r*5ltfzw-61ksdm41fuul8+hxs$86yo9%k1%k=(!@=-wv4qtyv'

# conf
DEBUG=False

# Database
DB_NAME = "todo"
DB_USER = "devops"
DB_PASSWORD = "mestre"
DB_HOST = "localhost"
DB_PORT = "3306"
```

Editando a job que criei

- [X] Provide configuration files
  - *File*: .env-dev
  - *Target*: .env

- Step 3
  - *Tipo*: Shell Command
  - *Comando*: Os comandos abaixo:

```shell
# Subindo o container de teste
$docker run -d -p 82:8000 -v /var/run/mysqld/mysqld.sock:/var/run/mysqld/mysqld.sock -v /var/lib/jenkins/workspace/jenkins-todo-list-principal/to_do/.env:/usr/src/app/to_do/.env --name=todo-list-teste django-todo-list

# Testando a imagem
$docker exec -i todo-list-teste python manage.py test --keep
exit_code=$?

# Derrubando o container velho
$docker rm -f todo-list-teste

$if [ $exit_code -ne 0 ]; then
  exit 1
fi
```

Instalando plugin `Parameterized Trigger` no jenkins: Gerenciar Jenkins > Gerenciar Plugins > Disponíveis

Agora basta configurar a job adicionando variáveis após habilitar essa opção nas Configurações:

- [X] Este build é parametrizado

Adicionando parâmetros do tipo string

- Variavel 1
  - *Nome*: image
  - *Value*: hemelo/django-todo-list
- Variavel 2
  - *Name*: DOCKER_HOST
  - *Value*: tcp://127.0.0.1:2376

Com isso tive que mudar 3 coisas
  - Na Step de testes
    - Substituir o nome da imagem pela variavel ${image} em `$ docker run ............... ${image}`
  - Na Step de Build do Docker
    - Mudar o nome da build pra hemelo/django-todo-list
    - [X] Push Image > Tornar essa opção ativa

#### Integração com Slack

Instalando plugin `Slack Notification` no jenkins: Gerenciar Jenkins > Gerenciar Plugins > Disponíveis

Adicionar os dados fornecidos pelo Slack em Configurações > Global Slack Notifier Settings

Criar credencial de token do Tipo: Secret Text

### Automatizado: Continuação - Criação de um Pipeline

Criando uma nova job do formato Pipeline com nome **jenkins-todo-list-desenvolvimento**

- [X] Este build é parametrizado

Adicionando parâmetros do tipo string com valor vazio pois será recebido da job anterior

- Variavel 1
  - *Nome*: image
  - *Value*:
- Variavel 2
  - *Name*: DOCKER_HOST
  - *Value*: tcp://127.0.0.1:2376

Adicionando script de pipeline

```groovy
pipeline {

  agent any    

  stages {
    stage('Oi Mundo Pipeline como Codigo') {
      steps {
        sh 'echo "Oi Mundo"'
      }
    }
  }
}

pipeline {
  environment {
    dockerImage = "${image}"
  }
  agent any

  stages {
    stage('Carregando o ENV de desenvolvimento') {
      steps {
        configFileProvider([configFile(fileId: '<id do seu arquivo de desenvolvimento>', variable: 'env')]) {
          sh 'cat $env > .env'
        }
      }
    }
    stage('Derrubando o container antigo') {
      steps {
        script {
          try {
            sh 'docker rm -f django-todolist-dev'
            } catch (Exception e) {
              sh "echo $e"
            }
          }
        }
      }        
      stage('Subindo o container novo') {
        steps {
          script {
            try {
              sh 'docker run -d -p 81:8000 -v /var/run/mysqld/mysqld.sock:/var/run/mysqld/mysqld.sock -v /var/lib/jenkins/workspace/jenkins-todo-list-desenvolvimento/.env:/usr/src/app/to_do/.env --name=django-todolist-dev ' + dockerImage + ':latest'
              } catch (Exception e) {
                slackSend (color: 'error', message: "[ FALHA ] Não foi possivel subir o container - ${BUILD_URL} em ${currentBuild.duration}s", tokenCredentialId: 'slack-token')
                sh "echo $e"
                currentBuild.result = 'ABORTED'
                error('Erro')
              }
            }
          }
        }
        stage('Notificando o usuario') {
          steps {
            slackSend (color: 'good', message: '[ Sucesso ] O novo build esta disponivel em: http://192.168.33.10:81/ ', tokenCredentialId: 'slack-token')
          }
        }

      }
    }
```

Agora basta criar a job de produçãp **jenkins-todo-list-producao** do tipo Freestyle

Editando a job

- [X] Provide configuration files
  - *File*: .env-prod
  - *Target*: .env

- Step 3
  - *Tipo*: Shell Command
  - *Comando*: Os comandos abaixo:

```shell
{
  $docker run -d -p 80:8000 -v /var/run/mysqld/mysqld.sock:/var/run/mysqld/mysqld.sock -v /var/lib/jenkins/workspace/todo-list-producao/.env:/usr/src/app/to_do/.env --name=django-todolist-prod $image:latest
} || {
  $docker rm -f django-todolist-prod
  $docker run -d -p 80:8000 -v /var/run/mysqld/mysqld.sock:/var/run/mysqld/mysqld.sock -v /var/lib/jenkins/workspace/todo-list-producao/.env:/usr/src/app/to_do/.env --name=django-todolist-prod $image:latest
}    
```

- [X] Este build é parametrizado

Adicionando parâmetros do tipo string com valor vazio pois será recebido da job anterior

- Variavel 1
  - *Nome*: image
  - *Value*:
- Variavel 2
  - *Name*: DOCKER_HOST
  - *Value*: tcp://127.0.0.1:2376

Ações de pós-build > Slack Notifications: Notify Success e Notify Every Failure  

### Automatizado - Continuação: Integrando estágios

Adicionar no Post Build da primeira job (jenkins-todo-list-principal) a variavel `image=$image` pra passar pra próxima job através da opção `Trigger parameterized build on other projects`

- Projects to build -> jenkins-todo-list-desenvolvimento
- Trigger when build is -> Stable
- Add Predefined Parameters
  - image = $image

Adicionando Script no Pipeline de Desenvolvimento:

```
...
stage ('Fazer o deploy em producao?') {
  steps {
    script {
      slackSend (color: 'warning', message: "Para aplicar a mudança em produção, acesse [Janela de 10 minutos]: ${JOB_URL}", tokenCredentialId: 'slack-token')
      timeout(time: 10, unit: 'MINUTES') {
        input(id: "Deploy Gate", message: "Deploy em produção?", ok: 'Deploy')
      }
    }
  }
}
stage (deploy) {
  steps {
    script {
      try {
        build job: 'jenkins-todo-list-producao', parameters: [[$class: 'StringParameterValue', name: 'image', value: dockerImage]]
      } catch (Exception e) {
        slackSend (color: 'error', message: "[ FALHA ] Não foi possivel subir o container em producao - ${BUILD_URL}", tokenCredentialId: 'slack-token')
        sh "echo $e"
        currentBuild.result = 'ABORTED'
        error('Erro')
      }
    }
  }
}
```

### Assegurando qualidade de código

1. Instalar sonarscanner

```shell
$docker run -d --name sonarqube -p 9000:9000 sonarqube:lts
```

2. Acessar em http://192.168.33.10:9000 e

Etapas:
- Login
  - Usuário: admin
  - Senha: admin
- Configuracao Inicial
  - Gerar token por nome: jenkins-todolist
  - Salvar token gerado
  - Selecionar linguagem (Other (JS, Python, PHP, ...))
  - Linux
  - Unique project key: jenkins-todolist
  - Salvar comando de execucao, parecido com o comando abaixo:

```
sonar-scanner \
  -Dsonar.projectKey=jenkins-todolist \
  -Dsonar.sources=. \
  -Dsonar.host.url=http://192.168.33.10:9000 \
  -Dsonar.login=token-gerado
```

3. Criar um novo job parecido com o primeiro job (jenkins-todo-list-principal), colocando o repositório, o cronjob pra executar a cada update no repositório e ativando a opção de limpar resquícios antes de iniciar

- Adicionar script abaixo

```shell
# Baixando o Sonarqube
wget https://s3.amazonaws.com/caelum-online-public/1110-jenkins/05/sonar-scanner-cli-3.3.0.1492-linux.zip

# Descompactando o scanner
unzip sonar-scanner-cli-3.3.0.1492-linux.zip

# Rodando o Scanner
./sonar-scanner-3.3.0.1492-linux/bin/sonar-scanner   -X \
-Dsonar.projectKey=jenkins-todolist \
-Dsonar.sources=. \
-Dsonar.host.url=http://192.168.33.10:9000 \
-Dsonar.login=token-gerado
```
