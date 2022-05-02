# Vagrant

Anotações do meu estudo de Vagrant

## Inicializando

```
$ vagrant init ubuntu/bionic64
$ vagrant up
$ vagrant ssh  -----------------------------> ssh -i .vagrant/machines/[nome_da_maquina](default)/virtualbox/private_key vagrant@[ip]
$$ sudo apt-get update
$$ sudo apt-get install -y nginx
```

###### Teste nginx:

```
$$ netstat -lntp
$$ curl http://localhost
```

## Configurar visibilidade e ip no vagrant file, possibilidades:

```
config.vm.network "forwarded_port", guest: 80, host:8080           --> Aparece como NAT no Vbox
config.vm.network "private_network", type: "dhcp"				--> Aparece como bridge no Vbox
config.vm.network "public_network", ip: "192.168.1.24"                     --> Aparece como bridge no Vbox
config.vm.network "public_network", type: "dhcp"			--> Aparece como bridge no Vbox
```

guest -> vm
host -> hypervisor

```
$ vagrant halt   
$ vagrant up
```

ou $ vagrant reload

## Gerando chave ssh pra acessar por outro computador

```
$ ssh-keygen -t rsa         --> Criar chaves e escolher diretorio pra salvar as chaves no diretorio compartilhado que aparece quando executado $ vagrant up [diretorio_compartilhado]/[nome_key] por padrao é a pasta que contem o Vagrantfile
$ vagrant ssh
$$ ls /vagrant/
$$ cp /vagrant/[nome_key].pub
$$ cat [nome_key].pub >> .ssh/authorized_keys   --> A chave pública deve ficar dentro do arquivo .ssh/authorized_keys da máquina virtual
$$ exit
$ ssh -i [nome_key] vagrant@[ip]
```

## Exibir configuracoes de ssh

`$ vagrant ssh-config`

## Configurando pasta comparatilhada.

Possibilidades:

```
config.vm.synced_folder "./configs", "/configs"
config.vm.synced_folder ".", "/vagrant", disabled: true         -----> Desabilita o Vagrantfile na vm
```

## Scripts

`config.vm.provision "shell", path: "script.sh"`

### Upando a SSH Key de forma automatizada

`config.vm.provision "shell", inline: "cat /[shared_folder]/[nome_key].pub >> .ssh/authorized_keys"`

### Atualizando o Ubuntu  

`config.vm.provision "shell", inline: "apt-get update"`

### Dependências

```
$script_mysql = <<-SCRIPT
	apt-get install -y mysql-server-5.7 && \
	mysql -e "create user 'phpuser'@'%' identified by 'pass';"
	cat /[shared_folder]/mysqld_custom_configs_file.cnf > /etc/mysql/mysql.conf.d/mysqld.cnf   --> custom file with bind address = 0.0.0.0 para ambiente de desenvolvimento
	service mysql restart
SCRIPT
```

`config.vm.provision "shell", inline: $script_mysql`

```
$ vagrant reload --provision
$ vagrant provision
```

### Desvantagens do Shell

Muito complicado de gerenciar dependências

## Multimachines

Recomeçando $ vagrant destroy

### Separação de containers

#### Uso do Puppet

Ferramenta instalada no guest que puxa configurações do host !
Desvantagem: precisa instalar no guest por shell script


```
# Vagrantfile

$script_mysql = <<-SCRIPT
	apt-get install -y mysql-server-5.7 && \
	mysql -e "create user 'phpuser'@'%' identified by 'pass';"
	cat /mysqld_custom_configs_file.cnf > /etc/mysql/mysql.conf.d/mysqld.cnf   --> custom file with bind address = 0.0.0.0 para ambiente de desenvolvimento
	service mysql restart
SCRIPT

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/bionic64"

  config.vm.define "mysqldb" do |mysql|
    mysql.vm.network "public_network", ip: "192.168.1.24"
    mysql.vm.provision "shell", inline: "cat /id_bionic.pub >> .ssh/authorized_keys"
    mysqldb.vm.provision "shell", inline: "apt-get update"
    mysqldb.vm.provision "shell", inline: $script_mysql
  end

  config.vm.define "phpweb" do |phpweb|
    phpweb.vm.network "forwarded_port", guest: 8888, host: 8888
    phpweb.vm.network "public_network", ip: "192.168.1.25"
    phpweb.vm.provision "shell", inline: "apt-get update && apt-get install -y puppet"
    phpweb.vm.provision "puppet" do |puppet|
      puppet.manifests_path = "./manifests"
      puppet.manifest_file = "phpweb.pp"
    end
  end
end
```

```
# manifests/phpweb.pp

exec { 'apt-update':
  command => '/usr/bin/apt-get update'
}

package { ['php7.2', 'php7.2-mysql']:
  require => Exec['apt-update'],
  ensure => installed,
}

exec { 'run-php7':
  require => Package['php7.2'],
  command => '/usr/bin/php -S 0.0.0.0:8888 -t /vagrant/src &'
}
```

```
# src/index.php

<?php
	echo "Testando conexao <br /> <br />";
	$servername = "192.168.1.24";
	$username = "phpuser";
	$password = "pass";

	// Create connection
	$conn = new mysqli($servername, $username, $password);

	// Check connection
	if ($conn->connect_error) {
    		die("Conexão falhou: " . $conn->connect_error);
	}
	echo "Connetado com sucesso";
?>
```

Nota: id_bionic é o nome da chave SSH criada pelo comando keygen anteriormente

#### Uso do Ansible

Ferramenta instalada no host que empurra configurações para o guest
Desvantagem: só roda em linux/mac, precisa ser instalado em uma vm em caso de uso no windows


```
# hosts

[mysqlserver]
192.168.1.22

[mysqlserver:vars]
ansible_user=vagrant
ansible_ssh_private_key_file=/home/vagrant/id_bionic
ansible_python_interpreter=/usr/bin/python3
ansible_ssh_common_args='-o StrictHostKeyChecking=no'

```

```
# playbook.yml

- hosts: all
  handlers:
    - name: restart mysql
      service:
        name: mysql
        state: restarted
      become: yes

  tasks:
    - name: 'Instalar MySQL Server'
      apt:
        update_cache: yes
        cache_valid_time: 3600 #1 hora
        name: ["mysql-server-5.7", "python3-mysqldb"]
        state: latest
      become: yes

    - name: 'Criar usuario no MySQL'
      mysql_user:
        login_user: root
        name: phpuser
        password: pass
        priv: '*.*:ALL'
        host: '%'
        state: present
      become: yes

    - name: 'Copiar arquivo mysqld.cnf'
      copy:
        src: /vagrant/mysqld.cnf
        dest: /etc/mysql/mysql.conf.d/mysqld.cnf
        owner: root
        group: root
        mode: 0644
      become: yes
      notify:
        - restart mysql

```

```
# Vagrantfile

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/bionic64"

  config.vm.define "phpweb" do |phpweb|
    phpweb.vm.network "forwarded_port", guest: 8888, host: 8888
    phpweb.vm.network "public_network", ip: "192.168.1.25"

    phpweb.vm.provision "shell",
      inline: "apt-get update && apt-get install -y puppet"

    phpweb.vm.provision "puppet" do |puppet|
      puppet.manifests_path = "./manifests"
      puppet.manifest_file = "phpweb.pp"
    end
  end

  config.vm.define "mysqlserver" do |mysqlserver|
    mysqlserver.vm.network "public_network", ip: "192.168.1.22"

    mysqlserver.vm.provision "shell",
      inline: "cat /vagrant/id_bionic.pub >> .ssh/authorized_keys"
  end

  config.vm.define "ansible" do |ansible|
    ansible.vm.network "public_network", ip: "192.168.1.26"

    ansible.vm.provision "shell",
      inline: "cp /vagrant/id_bionic  /home/vagrant && \
              chmod 600 /home/vagrant/id_bionic && \
              chown vagrant:vagrant /home/vagrant/id_bionic"

    ansible.vm.provision "shell",
      inline: "apt-get update && \
               apt-get install -y software-properties-common && \
               apt-add-repository --yes --update ppa:ansible/ansible && \
               apt-get install -y ansible"

     ansible.vm.provision "shell",
       inline: "ansible-playbook -i /vagrant/ansible/hosts \
                  /vagrant/ansible/playbook.yml"
  end
end
```

## Comandos uteis

$ vagrant global-status
$ vagrant box list
$ vagrant box prune
$ vagrant box remove "name"
$ vagrant global-status --prune

### Configurando hardware global

O comando abaixo após `config.vm.box = "ubuntu/bionic64"` garante que todas máquinas virtuais terão esse hardware

```
config.vm.provider "virtualbox" do |vb|
   vb.memory = 512
   vb.cpus = 1
end
```

### Configurando hardware especifico de uma máquina


```
config.vm.define "memcached" do |memcached|
  memcached.vm.box = "centos/7"
  memcached.vm.provider "virtualbox" do |vb|
    vb.memory = 512
    vb.cpus = 1
    vb.name = "centos7_memcached"
  end
end
```
