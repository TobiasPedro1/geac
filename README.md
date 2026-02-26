📅 Sistema de Gestão de Eventos Universitários
---


👥 Integrantes
---

  Dimas Celestino - Desenvolvedor(a)

  Douglas Henrique - Desenvolvedor(a)
  
  José Portela - Desenvolvedor(a)
  
  Julio Neto - Desenvolvedor(a)
  
  Pedro Tobias - Desenvolvedor(a)

  Rener Tomé - Desenvolvedor(a)

<br>
  
📃 Sobre o Projeto
---

  Este projeto consiste na implementação de uma plataforma completa para Gestão de Eventos Universitários, desenvolvido como requisito avaliativo para a disciplina de Engenharia de Software, ministrada pela Professora Thaís Alves Burity Rocha na UFAPE (Universidade Federal do Agreste de Pernambuco).
  
  O objetivo é criar um ecossistema que centralize a divulgação, organização e inscrição em eventos acadêmicos, conectando departamentos, centros acadêmicos e grupos estudantis com a comunidade universitária (alunos e professores). A plataforma resolve o problema da       fragmentação de informações, facilitando o acesso à cultura e conhecimento complementar.
  
<br>

📍 Objetivos e Funcionalidades
---

  O sistema visa aumentar a visibilidade das atividades acadêmicas e simplificar a burocracia de gestão. As principais funcionalidades incluem:

<br>
  
🎓 Para Organizadores (Departamentos/C.A.s):
---

  - Cadastro detalhado de eventos (palestras, seminários, feiras, festivais).
  
  - Definição de cronograma, palestrantes, local e requisitos.
  
  - Gerenciamento de inscritos e lista de presença.
  
  - Emissão automática de certificados de participação.
  
  - Coleta de feedback pós-evento para melhoria contínua.
    
<br>

🙋‍♂️ Para Participantes (Alunos/Professores):
---

  - Busca avançada de eventos por categoria, data, campus ou palavras-chave.
  
  - Inscrição rápida e facilitada.
  
  - Acesso ao histórico de participações e certificados.

<br>

🛠️ Tecnologias Utilizadas
---

O projeto é construído utilizando uma arquitetura moderna, separando o Back-end (API Rest) do Front-end.

Back-end (API)
  
  - Java 25 (Preview/Latest Features)
  
  - Spring Boot - Framework base para a aplicação.
  
  - Spring Security - Para autenticação e autorização.
  
  - JPA / Hibernate - Persistência de dados.

Front-end (Cliente Web)
  
  - React - Biblioteca para construção de interfaces.
  
  - Next.js - Framework React para produção.
  
  - Tailwind CSS - Para estilização.

Ferramentas & DevOps
  
  - Git & GitHub - Versionamento de código.
  
  - Docker - Containerização dos serviços.
  
  - PostgreSQL - Banco de dados relacional.

<br>

Ferramentas & DevOps
  
  - Git & GitHub - Versionamento de código.
  
  - Docker - Containerização dos serviços.
  
  - PostgreSQL - Banco de dados relacional.

<br>

🌐 Ambiente de Produção (Live)
---

A plataforma está hospedada na nuvem utilizando a infraestrutura do Render com deploy automatizado via Docker. Pode aceder à versão em produção através dos links abaixo:

- Acesso à Aplicação (Front-end): [https://geac-backend.onrender.com]
- Acesso à API (Back-end): [https://geac-backend.onrender.com]

(Nota: Como a hospedagem utiliza o plano gratuito, o primeiro acesso após um período de inatividade pode demorar cerca de 50 segundos enquanto os servidores "acordam".)

<br>
  
🚀 Como Executar o Projeto
---

  Pré-requisitos
  
  - Java JDK 25 instalado.
  
  - Node.js (versão LTS ou superior).
  
  - Docker (Opcional, mas recomendado para o Banco de Dados).

  Passos:

  1. Clone o repositório:
     
         git clone https://github.com/GestaoDeEventosAcademicosECulturais/geac.git

 2. Back-end:

        cd backend
        ./mvnw spring-boot:run

3. Front-end:

        cd frontend
        npm install
        npm run dev

4: Acesse a aplicação em http://localhost:3000

<br>

⚙️ Perfis de Configuração (Spring Profiles)
---
A API foi arquitetada utilizando múltiplos perfis para garantir a separação de responsabilidades entre os ambientes:

- Desenvolvimento (dev): É o perfil padrão. Utiliza o application.yaml e conecta-se a uma instância local do PostgreSQL. Ideal para o desenvolvimento diário.
- Testes / CI (test): Ativado através do application-test.yaml. Utiliza o banco de dados em memória H2. É utilizado automaticamente pela esteira do GitHub Actions para rodar a suíte de testes sem depender de infraestrutura externa. Para rodar localmente: mvn clean test -Dspring.profiles.active=test.
- Produção (prod): Ativado através do application-prod.yaml. Utiliza variáveis de ambiente injetadas pelo Render para conectar ao banco PostgreSQL na nuvem e desativa a exibição de logs SQL por segurança.