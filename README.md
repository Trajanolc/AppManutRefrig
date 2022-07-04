# App para manutenção de refrigeração

## Introdução

Este app foi criado para automatizar a coleta de dados referente a manutenção de 300 máquinas de refrigeração da distribuidora de energia do estado do pará.

O mesmo funciona para entrada de dados pelos técnicos que realizam o serviço, esses dados são enviados para uma arquitetura em cloud AWS e posteriormente são tratados utilizando python para gerar relatórios.

## Motivações

O app foi criado enquanto eu trabalhava como supervisor da equipe de refrigeração, na época os serviços eram registrados em um formulário fisico escrito à mão, o que gerava desordem e dificuldade de consulta.

Além disso existiam dificuldades de armazenamento, visto que as máquinas estão distribuídas em 72 prédios diferentes, espalhados por 23 municípios na região nordeste do pará.

Com essas dificuldades propus a criação de um app que seguisse os seguintes preceitos:

- Facilidade de inserção de dados
- Padronização de inserção de dados
- Facilidade de consulta de dados

## O App

O app consiste de 3 telas simples, uma tela inicial, onde posteriormente será integrada uma lista com as ultimas ordens realizadas.

![image](https://user-images.githubusercontent.com/62985381/177211840-d900f89e-3aed-4cfb-b281-4ea3bbfb6432.png)

A tela de autenticação, para simplificar estou utilizando a API do auth0.com para controle de acessos.

![image](https://user-images.githubusercontent.com/62985381/177211971-947bd2a4-f4bf-427c-aa7e-1b968438c115.png)

Ao autenticar, é resgatado da tabela dynamo quais máquinas aquele determinado usuário tem acesso.

Por fim a tela de inserção de ordens com a maioria dos campos sendo checkboxes e um espaço para que sejam inseridas imagens antes e depois do serviço realizado.

![image](https://user-images.githubusercontent.com/62985381/177212004-ea833fba-9741-4843-841d-a9e3722b2c53.png)

As imagens antes de enviadas são compactadas e redimensionadas para terem no máximo 512px x 512px, em seguida enviadas para um bucket S3.

Os dados de entrada são enviados para uma tabela dynamo DB, junto com a referencia das imagens de antes e depois.

Ao final do mês esses dados são tratados com python e é gerado uma tabela com o ID de cada ordem e para cada ID é criado um relatório em PDF padronizado.

![image](https://user-images.githubusercontent.com/62985381/177212640-e03c3ce6-4e5b-4195-9f04-7f4c980d8fa7.png)
![image](https://user-images.githubusercontent.com/62985381/177212665-cec49cb1-a7ac-4ed6-b125-4a94e359db28.png)
![image](https://user-images.githubusercontent.com/62985381/177212684-41b3761d-0cf4-41d3-aaa5-9f08bf32515c.png)


