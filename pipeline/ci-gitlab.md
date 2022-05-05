# Gitlab

Usando o mesmo projeto /jenkins/example-project

```yaml
image: docker:stable

services:
- docker:dind

stages:
- pre-build
- build
- test
- deploy
- notificator

before_script:
- docker info
- docker login -u $DOCKER_USER -p $DOCKER_PASSWORD

build-docker:
  stage: pre-build
  retry: 2
  script:
  - docker build -t django-todo-list .
  - docker tag django-todo-list hemelo/django-todo-list:latest
  - docker push hemelo/django-todo-list:latest

build-project:
  stage: build
  retry: 2
  services:
  - docker:dind
  - mysql:5.7
  image: hemelo/django-todo-list:latest
  variables:
    MYSQL_USER: $DEVELOPMENT_MYSQL_USERNAME
    MYSQL_PASSWORD: $DEVELOPMENT_MYSQL_PASSWORD
    MYSQL_DATABASE: $DEVELOPMENT_MYSQL_DATABASE
    MYSQL_ROOT_PASSWORD: $MYSQL_ROOT_PASSWORD
    DB_HOST: $DB_HOST
    DB_PORT: $DB_PORT
    DB_NAME: $DEVELOPMENT_MYSQL_DATABASE
    DB_USER: $DEVELOPMENT_MYSQL_USERNAME
    DB_PASSWORD: $DEVELOPMENT_MYSQL_PASSWORD
    SECRET_KEY: $APP_KEY
  tags:
  - my-local-runner
  dependencies:
  - build-docker
  script:
  - python manage.py makemigrations
  - python manage.py migrate
  - python manage.py createsuperuser

test-project:
  stage: test
  services:
  - docker:dind
  - mysql:5.7
  image: hemelo/django-todo-list:latest
  variables:
    MYSQL_USER: $DEVELOPMENT_MYSQL_USERNAME
    MYSQL_PASSWORD: $DEVELOPMENT_MYSQL_PASSWORD
    MYSQL_DATABASE: $DEVELOPMENT_MYSQL_DATABASE
    MYSQL_ROOT_PASSWORD: $MYSQL_ROOT_PASSWORD
    DB_HOST: $DB_HOST
    DB_PORT: $DB_PORT
    DB_NAME: $DEVELOPMENT_MYSQL_DATABASE
    DB_USER: $DEVELOPMENT_MYSQL_USERNAME
    DB_PASSWORD: $DEVELOPMENT_MYSQL_PASSWORD
    SECRET_KEY: $APP_KEY
  tags:
  - my-local-runner
  dependencies:
  - build-project
  script:
  - python -m unittest setUp

deploy-project:
  stage: deploy
  tags:
  - my-local-runner-deploy # SHELL RUNNER
  scripts:
  - tar cfz arquivos.tgz *
  - scp arquivos.tgz '"$ADDRESS_SSH":$PATH_DEPLOY'
  - ssh $ADDRESS_SSH 'cd $PATH_DEPLOY'; tar xfz arquivos.tgz; /usr/local/bin/docker-compose up -d'

notificador-sucesso:
  stage: notificator
  tags:
  - my-local-runner-deploy # SHELL RUNNER
  when: on_success
  script:
  - sh notificacaoSucesso.sh

notificador-erro:
  stage: notificator 
  tags:
  - my-local-runner-deploy # SHELL RUNNER
  when: on_failure
  script:
  - sh notificacaoFalha.sh
```

## Custom Runners

```shell
$docker pull gitlab/gitlab-runner:latest
$docker run -d --name gitlab-runner --restart always -v /Users/Shared/gitlab-runner/config:/etc/gitlab-runner -v /var/run/docker.sock:/var/run/docker.sock gitlab/gitlab-runner:latest

or

$docker run -d --name gitlab-runner --restart always -v /src/gitlab-runner/config:/etc/gitlab-runner -v /var/run/docker.sock:/var/run/docker.sock gitlab/gitlab-runner:latest (linux)

$docker exec -it gitlab-runner bash
$gitlab-runner register
>Where Repository: https://gitlab.com/
>Token: "[token pegado nas configuracoes de CI/CD do Gitlab]"
>Default Image: hemelo/django-todo-list:latest, shell
```

Adicionar tag ao Runnner nas configurações de CI/CD do Gitlab para poder referenciar no arquivo de configuracao de deploy .gitlab-ci.yml

## Deploy externo

Criação de Chaves SSH

```shell
$docker exec -it gitlab-runner bash
$su gitlab-runner
$ssh-keygen
$cd /home/gitlab-runner/.ssh/
$cat id_rsa.pub ----------> Copiar e colar nas authorized keys da máquina de deploy


```
