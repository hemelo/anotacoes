# Ansible

Requisitos: Projeto pré-pronto de wordpress encontrado [aqui]()

###### Passo a passo

```
vagrant up
```

```
# hosts

[wordpress]
172.17.177.40
```


```
ansible wordpress -u vagrant --private-key .vagrant/machines/wordpress/virtualbox/private_key -i hosts -m shell -a 'echo Hello, World'
```

### Falha de unreachable

Um pequeno passo a passo em caso de erro `UNREACHABLE ERROR`

```
172.17.177.40 | UNREACHABLE! => {
    "changed": false,
    "msg": "Failed to connect to the host via ssh: vagrant@172.17.177.40: Permission denied (publickey,password).\r\n",
    "unreachable": true
}
```

```
mkdir ssh-keys
cd ssh-keys
ssh-keygen -t rsa
Enter file in which to save the key : /ssh-keys/vagrant_id_rsa
Password: vagrant
```

```
vagrant up
cd ssh-keys
ssh-copy-id -i vagrant_id_rsa.pub vagrant@172.17.177.40
Password: vagrant
```

Talvez esteja necessário remover o arquivo `~/.ssh/known_hosts`.

## Playbooks

Este é o estilo de um playbook, iniciando com --- e seguido de - hosts:

```
---
- hosts: all
  tasks:
    - shell: 'echo hello > /vagrant/world.txt'
```

```
# provisioning.yml

---
- hosts: all
  tasks:
    - name: 'Instala o PHP5'
      apt:
        name: php5
        state: latest
      become: yes
    - name: 'Instala o Apache2'
      apt:
        name: apache2
        state: latest
      become: yes
    - name: 'Instala o modphp'
      apt:
        name: libapache2-mod-php5
        state: latest
      become: yes
```

Become indica que é uma tarefa com sudo

```
vagrant up

ansible-playbook provisioning.yml -u vagrant -i hosts --private-key .vagrant/machines/wordpress/virtualbox/private_key
```

### Loop

```
# provisioning.yml

---
- hosts: all
  tasks:
    - name: 'Instala pacotes do sistema operacional'
      apt:
        name:
        - php5
        - apache2
        - libapache2-mod-php5
        - php5-gd
        - libssh2-php
        - php5-mcrypt
        - mysql-server-5.6
        - python-mysqldb
        - php5-mysql
        state: latest
      become: yes
```

ou

```
# provisioning.yml

---
- hosts: all
  tasks:
   - name: 'Instala pacotes do sistema operacional'
     apt:
       name: '{{ item }}'
       state: latest
     become: yes
     with_items:
       - php5
       - apache2
       - libapache2-mod-php5
       - php5-gd
       - libssh2-php
       - php5-mcrypt
       - mysql-server-5.6
       - python-mysqldb
       - php5-mysql
```

### Editando hosts

```
hosts

[wordpress]
172.17.177.40

[wordpress:vars]
ansible_user=vagrant
ansible_ssh_private_key_file="/Users/henri/wordpress_com_ansible/.vagrant/machines/wordpress/virtualbox/private_key"
```

### Criando banco de dados e setando novo usuario

```
# provisioning.yml

- name: 'Cria o banco do MySQL'
  mysql_db:
    name: wordpress_db
    login_user: root
    state: present

- name: 'Cria o usuário do MySQL'
  mysql_user:
    login_user: root
    name: wordpress_user
    password: 12345
    priv: 'wordpress_db.*:ALL'
    state: present

```

Executar com: `ansible-playbook -i hosts provisioning.yml`

### Criando e configurando wordpress

```
# provisioning.yml

- name: 'Baixa o arquivo de instalacao do Wordpress'
  get_url:
    url: 'https://wordpress.org/latest.tar.gz'
    dest: '/tmp/wordpress.tar.gz'

- name: 'Descompacta o Wordpress'
  unarchive:
    src: '/tmp/wordpress.tar.gz'
    dest: '/var/www/'
    remote_src: yes
  become: yes
- copy:
    src: '/var/www/wordpress/wp-config-sample.php'
    dest: '/var/www/wordpress/wp-config.php'
    remote_src: yes
  become: yes
- name: 'Configura o wp-config com as entradas do banco de dados'
  replace:
    path: '/var/www/wordpress/wp-config.php'
    regexp: "{{ item.regex }}"
    replace: "{{ item.value }}"
  with_items:
    - { regex: 'database_name_here', value: 'wordpress_db' }
    - { regex: 'username_here', value: 'wordpress_user' }
    - { regex: 'password_here', value: '12345' }
  become: yes

```

### Configurando servidor

```
ansible-playbook -i hosts provisioning.yml
vagrant ssh
cat /etc/apache2/sites-available/000-default.conf
```

Copiar todo o conteúdo de `000-default.conf` e modificar a cópia para

```
ServerAdmin webmaster@localhost
DocumentRoot /var/www/wordpress
```

Adicionar ao provisioning.yml:

```
# provisioning.yml

---
- hosts: all
  handlers:
     - name: restart apache
       service:
         name: apache2
         state: restarted
  become: yes
  tasks:
    - name: 'Configura Apache para servir Wordpress'
      copy:
        src: 'files/000-default.conf'
        dest: '/etc/apache2/sites-available/000-default.conf'
      become: yes
      notify:
        - restart apache
```

E testar o wordpress :D

`ansible-playbook -i hosts provisioning.yml`

### Separando contextos


```
# Vagrantfile

Vagrant.configure("2") do |config|

  config.vm.box = "ubuntu/trusty64"

  config.vm.provider "virtualbox" do |v|
    v.memory = 1024
  end

  config.vm.define "wordpress" do |m|
    m.vm.network "private_network", ip: "172.17.177.40"
  end

  config.vm.define "mysql" do |m|
    m.vm.network "private_network", ip: "172.17.177.42"
  end

end
```

```
# hosts

[wordpress]
172.17.177.40

[wordpress:vars]
ansible_user=vagrant
ansible_ssh_private_key_file="/Users/henri/wordpress_com_ansible/.vagrant/machines/wordpress/virtualbox/private_key"

[database]
172.17.177.42

[database:vars]
ansible_user=vagrant
ansible_ssh_private_key_file="/Users/henri/wordpress_com_ansible/.vagrant/machines/mysql/virtualbox/private_key"
```

```
# provisioning.yml

---
- hosts: database
  handlers:
   - name: restart mysql
     service:
       name: mysql
       state: restarted
     become: yes
  tasks:
    - name: 'Instala pacotes de dependencia do sistema operacional'
      apt:
        name: "{{ item }}"
        state: latest
      become: yes
      with_items:
        - mysql-server-5.6
        - python-mysqldb

    - name: 'Cria o banco do MySQL'
      mysql_db:
        name: wordpress_db
        login_user: root
        state: present

    - name: 'Cria o usuário do MySQL'
      mysql_user:
        login_user: root
        name: wordpress_user
        password: 12345
        priv: 'wordpress_db.*:ALL'
        state: present
        host: "{{ item }}"
      with_items:
        - 'localhost'
        - '127.0.0.1'
        - '172.17.177.40'
    - name: 'Configura MySQL para aceitar conexões remotas'
       copy:
         src: 'files/my.cnf'
         dest: '/etc/mysql/my.cnf'
       become: yes
       notify:
         - restart mysql

```

Assim como o apache, precisamos copiar as configs do mysql para nossa máquina local, nesse caso na pasta /files do projeto


```
ansible-playbook -i hosts provisioning.yml
vagrant ssh mysql
cat /etc/mysql/my.cnf
```

Dessa forma, em nossa máquina editamos a propriedade `bind_address` para `bind-address = 0.0.0.0`


```
# provisioning.yml

- hosts: wordpress
  handlers:
    - name: restart apache
      service:
        name: apache2
        state: restarted
      become: yes

  tasks:
    - name: 'Instala pacotes de dependencia do sistema operacional'
      apt:
        name: "{{ item }}"
        state: latest
      become: yes
      with_items:
        - php5
        - apache2
        - libapache2-mod-php5
        - php5-gd
        - libssh2-php
        - php5-mcrypt
        - php5-mysql

    - name: 'Baixa o arquivo de instalacao do Wordpress'
      get_url:
        url: 'https://wordpress.org/latest.tar.gz'
        dest: '/tmp/wordpress.tar.gz'

    - name: 'Descompacta o wordpress'
      unarchive:
        src: '/tmp/wordpress.tar.gz'
        dest: /var/www/
        remote_src: yes
      become: yes

    - copy:
        src: '/var/www/wordpress/wp-config-sample.php'
        dest: '/var/www/wordpress/wp-config.php'
        remote_src: yes
      become: yes

    - name: 'Configura o wp-config com as entradas do banco de dados'
      replace:
        path: '/var/www/wordpress/wp-config.php'
        regexp: "{{ item.regex }}"
        replace: "{{ item.value }}"
      with_items:
        - { regex: 'database_name_here', value: 'wordpress_db'}
        - { regex: 'username_here', value: 'wordpress_user'}
        - { regex: 'password_here', value: '12345'}
        - { regex: 'localhost', value: '172.17.177.42'}                                    <------------
      become: yes

    - name: 'Configura Apache para servir o Wordpress'
      copy:
        src: 'files/000-default.conf'
        dest: '/etc/apache2/sites-available/000-default.conf'
      become: yes
      notify:
        - restart apache

```

Executar com `ansible-playbook -i hosts provisioning.yml`


### Uso de variáveis

Ao invés de usar valores muito repetitivos no nosso script podemos trocar para uma variável `"{{ variavel }}"` e criar um arquivo all.yml com todas variaveis

Exemplo:

```
# all.yml

---
wp-username: wordpress_user
wp_db_name: wordpress_db
wp_installation_dir: '/var/www/wordpress'
wp_user_password: wodpress_db
wp_host_ip: '172.17.177.40'
wp_db_ip: '172.17.177.42'

```

Agora basta substituir os casos como `172.17.177.42` para `"{{ wp_db_ip }}"`
Outro exemplo: `'wordpress_db.*:ALL'` para `"{{ wp_db_name }}.*:ALL"`

Também é possível separar as variaveis para contextos diferentes (database.yml, wordpress.yml) que o ansible automaticamente reconhece ao executar com `ansible-playbook -i hosts provisioning.yml`

### Uso de templates

Substituindo o antigo código para rodar o wordpress no apache.....

```
- name: 'Configura Apache para servir o Wordpress'
  template:
    src: 'templates/000-default.conf.j2'
    dest: '/etc/apache2/sites-available/000-default.conf'
  become: yes
  notify:
    - restart apache
```

Criamos um diretório templates e movemos o 000-default.conf para lá, mudando sua extensao para ```000-default.conf.j2``` que é a extensao de template do ansible

Dessa forma, podemos usar variáveis que o ansible irá interpretar normalmente:

```
# templates/000-default.conf.j2

DocumentRoot {{ wp_installation_dir }}
```

Executando com `ansible-playbook -i hosts provisioning.yml`

### Separando para reutilização

```
# provisioning.yml

---
- hosts: database
  roles:
    - mysql

- hosts: wordpress
  roles:
    - wordpress

```

```
# roles/wordpress/tasks/main.yml

---
- name: 'Baixa o arquivo de instalacao do Wordpress'
  get_url:
    url: https://wordpress.org/latest.tar.gz'
    dest: '/tmp/wordpress.tar.gz'
    mode: 0440

- name: 'Descompacta o wordpress'
  unarchive:
    src: '/tmp/wordpress.tar.gz'
    dest: '/var/www/'
    remote_src: yes
  become: yes

- copy:
    src: "{{ wp_installation_dir}}/wp-config-sample.php"
    dest: "{{ wp_installation_dir}}/wp-config.php"
    remote_src: yes
  become: yes

- name: 'Configura o wp-config com as entradas do banco de dados'
  replace:
    path: "{{ wp_installation_dir}}/wp-config.php"
    regexp: "{{ item.regex }}"
    replace: "{{ item.value }}"
    backup: yes
  with_items:
    - { regex: 'database_name_here', value: "{{ wp_db_name }}"}
    - { regex: 'username_here', value: "{{ wp_username }}"}
    - { regex: 'password_here', value: "{{ wp_user_password }}"}
    - { regex: 'localhost', value: "{{ wp_db_ip }}"}
  become: yes

- name: 'Configura Apache para servir o Wordpress'
  template:
    src: 'templates/000-default.conf.j2'
    dest: '/etc/apache2/sites-available/000-default.conf'
  notify:
    - restart apache
  become: yes
```

```
# roles/wordpress/handlers/main.yml

---
- name: restart apache
  service:
    name: apache2
    state: restarted
  become: yes
```

```
# roles/wordpress/meta/main.yml

---
dependencies:
  - webserver
```

- Não esquecer de mover o template de configuracoes do apache para `roles/wordpress/templates/000-default.conf.j2`
- A mesma separação acontece para o mysql e o webserver que é o apache, no entanto, no caso do mysql é `roles/mysql/files/my.cnf`
- Variáveis defaults podem ser criadas e colocadas no diretório `roles/[role]/defaults/main.yml`
