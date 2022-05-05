# Terraform

AWS Management Console > New Service IAM > Users > New User

- Access Type: Programmatic Access;
- Username: terraform;
- Permissions: Add User to Group;
- Policies: Create group: Admin > Add Policy Administrator
- [X] Create User
- Download CSV with credentials

```shell
$aws configure --------> Uso de credenciais do CSV
```

- Instalar .ext Terraform no Vscode

```shell
# main.tf

provider "aws" {
  version = "~> 2.0"
  region  = "us-east-1"
}

resource "aws_instance" "dev" {
  count         = 3
  ami           = "${var.amis["us-east-1"]}" # Amazon Machine Image do tipo Amazon Linux
  instance_type = "t2.micro"
  key_name      = "${var.key_name}"

  tags  = {
    Name = "machine-${count.index}"
  }
  vpc_security_group_ids = ["${aws_security_group.allow_tls.id}"]
}

resource "aws_instance" "dev4" {
  ami           = "${var.amis["us-east-1"]}"
  instance_type = "t2.micro"
  key_name      = "${var.key_name}"

  tags  = {
    Name = "machine-4"
  }
  vpc_security_group_ids = ["${aws_security_group.allow_tls.id}"]
  depends_on             = [aws_s3_bucket.dev4]
}

resource "aws_instance" "dev5" {
  ami           =  "${var.amis["us-east-1"]}"
  instance_type = "t2.micro"
  key_name      =  "${var.key_name}"

  tags  = {
    Name = "machine-5"
  }
  vpc_security_group_ids = ["${aws_security_group.allow_tls.id}"]
  depends_on             = [aws_dynamodb_table.dynamodb-homologacao]
}
```

```shell
# database.tf

resource "aws_dynamodb_table" "dynamodb-homologacao" {
  name           = "GameScores"
  billing_mode   = "PAY_PER_REQUEST"
  read_capacity  = 20
  write_capacity = 20
  hash_key       = "UserId"
  range_key      = "GameTitle"

  attribute {
    name = "UserId"
    type = "S"
  }

  attribute {
    name = "GameTitle"
    type = "S"
  }
}
```

```shell
# bucket.tf

resource "aws_s3_bucket" "dev4" {
  bucket = "bucket-dev4"
  acl    = "private"

  tags = {
    Name = "bucket-dev4"
  }
}
```

```shell
# security.tf

resource "aws_security_group" "allow_tls" {
  name        = "allow_tls"
  description = "Allow TLS inbound traffic"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port        = 22
    to_port          = 22
    protocol         = "tcp"
    cidr_blocks      = var.cdirs_remote_access
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = var.cdirs_remote_access
  }

  tags = {
    Name = "allow_tls"
  }
}
```

```shell
# vars.tf

variable "amis" {
  type = "map"
  default = {
    "us-east-1" = "ami-0022f774911c1d690"
    "us-east-1" = "ami-0022f774911c1d690"
  }
}

variable "cdirs_remote_access" {
  type = "list"
  default = ["0.0.0.0/0"]
}

variable "key_name" {
  default = "terraform_aws"
}
```

```shell
# outputs.tf

output "ips" {
  value = "${aws_instance.dev5.public_ip}"
}
```

### Outras regiões

Basta adicionar

```shell
provider "aws" {
  version = "~> 2.0"
  region  = "us-east-1"
}

provider "aws" {
  alias   = "us-east-2"
  version = "~> 2.0"
  region  = "us-east-1"
}
```

e no recurso colocar a linha

```shell
resource "" "" {
  provider = "aws.us-east-2"
}

Em caso de recursos duplicados, é preciso mudar o nome também
```

## SSH

### Acesso do Terraform a Amazon

```shell
$ssh-keygen -f terraform-aws -t rsa
$mv terraform-aws ~/.ssh/
$cp terraform-aws.pub ~/.ssh/
```

E importar a chave pública no AWS Console

### Acessando máquina virtual

```shell
$ssh -i ~/.ssh/terraform-aws [address-da-maquina]
```

## Comandos básicos terraform

```shell
$terraform init
$terraform show
$terraform plan
$terraform apply
$terraform refresh
$terraform destroy
$terraform destroy -target aws_instance.machine5 ----> Tipo.ID do recurso
```

## Comandos auxiliares AWS

Muitos comandos usados pra não ter que acessar o dashboard web

```shell
$aws ec2 describe-security-groups
$aws ec2 describe-images
```

## Cloud

```shell
$cd -------> Home do Usuário
$vi .terraformrc ------> Inserir token configurado no Site
```

Adicionar:

```shell
# remote-state.tf

terraform {
  backend "remote" {
    hostname     = "app.terraform.io"
    organization = "organizacao"
  }

  workspaces {
    name = "aws-infra-teste"
  }
}
