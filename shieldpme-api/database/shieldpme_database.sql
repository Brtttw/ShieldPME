USE master;
GO

IF DB_ID('ShieldPME_School') IS NULL
    CREATE DATABASE ShieldPME_School;
GO

USE ShieldPME_School;
GO

-- conteudo do site

IF OBJECT_ID('dbo.Plano', 'U') IS NULL
CREATE TABLE dbo.Plano (
    id           INT            IDENTITY(1,1) NOT NULL,
    codigo       VARCHAR(20)    NOT NULL,
    nome         NVARCHAR(50)   NOT NULL,
    descricao    NVARCHAR(500)  NOT NULL,
    precoMensal  DECIMAL(10,2)  NOT NULL,
    precoAnual   DECIMAL(10,2)  NOT NULL,
    destaque     BIT            NOT NULL DEFAULT 0,
    ativo        BIT            NOT NULL DEFAULT 1,
    ordem        INT            NOT NULL DEFAULT 0,
    CONSTRAINT PK_Plano        PRIMARY KEY (id),
    CONSTRAINT UQ_Plano_codigo UNIQUE (codigo),
    CONSTRAINT CK_Plano_precos CHECK (precoMensal > 0 AND precoAnual > 0)
);
GO

IF OBJECT_ID('dbo.PlanoBeneficio', 'U') IS NULL
CREATE TABLE dbo.PlanoBeneficio (
    id         INT            IDENTITY(1,1) NOT NULL,
    planoId    INT            NOT NULL,
    descricao  NVARCHAR(150)  NOT NULL,
    ordem      INT            NOT NULL DEFAULT 0,
    CONSTRAINT PK_PlanoBeneficio       PRIMARY KEY (id),
    CONSTRAINT FK_PlanoBeneficio_Plano FOREIGN KEY (planoId) REFERENCES dbo.Plano(id) ON DELETE CASCADE,
    INDEX IX_PlanoBeneficio_planoId (planoId)
);
GO

IF OBJECT_ID('dbo.Servico', 'U') IS NULL
CREATE TABLE dbo.Servico (
    id                INT            IDENTITY(1,1) NOT NULL,
    titulo            NVARCHAR(100)  NOT NULL,
    descricao         NVARCHAR(300)  NOT NULL,
    iconeArquivo      VARCHAR(100)   NOT NULL,
    textoAlternativo  NVARCHAR(150)  NOT NULL,
    ativo             BIT            NOT NULL DEFAULT 1,
    ordem             INT            NOT NULL DEFAULT 0,
    CONSTRAINT PK_Servico PRIMARY KEY (id)
);
GO

IF OBJECT_ID('dbo.Postagem', 'U') IS NULL
CREATE TABLE dbo.Postagem (
    id                INT            IDENTITY(1,1) NOT NULL,
    titulo            NVARCHAR(200)  NOT NULL,
    resumo            NVARCHAR(500)  NOT NULL,
    publicadoEm       DATE           NOT NULL,
    imagemArquivo     VARCHAR(100)   NOT NULL,
    textoAlternativo  NVARCHAR(150)  NOT NULL,
    ativo             BIT            NOT NULL DEFAULT 1,
    CONSTRAINT PK_Postagem PRIMARY KEY (id)
);
GO

IF OBJECT_ID('dbo.Ferramenta', 'U') IS NULL
CREATE TABLE dbo.Ferramenta (
    id         INT             IDENTITY(1,1) NOT NULL,
    nome       NVARCHAR(100)   NOT NULL,
    descricao  NVARCHAR(1000)  NOT NULL,
    ativo      BIT             NOT NULL DEFAULT 1,
    ordem      INT             NOT NULL DEFAULT 0,
    CONSTRAINT PK_Ferramenta      PRIMARY KEY (id),
    CONSTRAINT UQ_Ferramenta_nome UNIQUE (nome)
);
GO

IF OBJECT_ID('dbo.FerramentaRecurso', 'U') IS NULL
CREATE TABLE dbo.FerramentaRecurso (
    id            INT            IDENTITY(1,1) NOT NULL,
    ferramentaId  INT            NOT NULL,
    descricao     NVARCHAR(200)  NOT NULL,
    ordem         INT            NOT NULL DEFAULT 0,
    CONSTRAINT PK_FerramentaRecurso            PRIMARY KEY (id),
    CONSTRAINT FK_FerramentaRecurso_Ferramenta FOREIGN KEY (ferramentaId) REFERENCES dbo.Ferramenta(id) ON DELETE CASCADE,
    INDEX IX_FerramentaRecurso_ferramentaId (ferramentaId)
);
GO

-- usuarios

IF OBJECT_ID('dbo.Usuario', 'U') IS NULL
CREATE TABLE dbo.Usuario (
    id             INT            IDENTITY(1,1) NOT NULL,
    nome           NVARCHAR(150)  NOT NULL,
    email          NVARCHAR(254)  NOT NULL,
    senhaHash      VARCHAR(100)   NULL,  -- bcrypt
    fotoUrl        NVARCHAR(500)  NULL,  -- nao usado pela api
    nivelAcesso    VARCHAR(10)    NOT NULL DEFAULT 'USER',  -- nao usado pela api
    statusUsuario  VARCHAR(10)    NOT NULL DEFAULT 'ATIVO',
    dataCadastro   DATETIME2(0)   NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT PK_Usuario              PRIMARY KEY (id),
    CONSTRAINT UQ_Usuario_email        UNIQUE (email),
    CONSTRAINT CK_Usuario_nivelAcesso  CHECK (nivelAcesso IN ('ADMIN', 'USER')),
    CONSTRAINT CK_Usuario_status       CHECK (statusUsuario IN ('ATIVO', 'INATIVO'))
);
GO

-- formularios

IF OBJECT_ID('dbo.ContatoMensagem', 'U') IS NULL
CREATE TABLE dbo.ContatoMensagem (
    id        INT            IDENTITY(1,1) NOT NULL,
    nome      NVARCHAR(150)  NOT NULL,
    email     NVARCHAR(254)  NOT NULL,
    assunto   NVARCHAR(200)  NOT NULL,
    mensagem  NVARCHAR(2000) NOT NULL,
    criadoEm  DATETIME2(0)   NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT PK_ContatoMensagem PRIMARY KEY (id)
);
GO

IF OBJECT_ID('dbo.NewsletterInscricao', 'U') IS NULL
CREATE TABLE dbo.NewsletterInscricao (
    id              INT            IDENTITY(1,1) NOT NULL,
    email           NVARCHAR(254)  NOT NULL,
    dataNascimento  DATE           NULL,
    criadoEm        DATETIME2(0)   NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT PK_NewsletterInscricao       PRIMARY KEY (id),
    CONSTRAINT UQ_NewsletterInscricao_email UNIQUE (email)
);
GO

-- assinaturas: pagamento simulado, nao ha tabela de pagamento nem dados de cartao

IF OBJECT_ID('dbo.Assinatura', 'U') IS NULL
CREATE TABLE dbo.Assinatura (
    id                INT            IDENTITY(1,1) NOT NULL,
    planoId           INT            NOT NULL,
    usuarioId         INT            NULL,  -- nao usado pela api (checkout sem login)
    nome              NVARCHAR(150)  NOT NULL,
    email             NVARCHAR(254)  NOT NULL,
    telefone          VARCHAR(20)    NOT NULL,
    cpf               VARCHAR(14)    NOT NULL,
    empresa           NVARCHAR(150)  NOT NULL,
    valorMensal       DECIMAL(10,2)  NOT NULL,  -- preco no momento da compra
    metodoPagamento   VARCHAR(10)    NOT NULL,
    parcelas          TINYINT        NOT NULL DEFAULT 1,
    statusAssinatura  VARCHAR(10)    NOT NULL DEFAULT 'ATIVA',
    criadoEm          DATETIME2(0)   NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT PK_Assinatura          PRIMARY KEY (id),
    CONSTRAINT FK_Assinatura_Plano    FOREIGN KEY (planoId)   REFERENCES dbo.Plano(id),
    CONSTRAINT FK_Assinatura_Usuario  FOREIGN KEY (usuarioId) REFERENCES dbo.Usuario(id),
    CONSTRAINT CK_Assinatura_metodo   CHECK (metodoPagamento IN ('credito', 'debito', 'pix', 'boleto')),
    CONSTRAINT CK_Assinatura_parcelas CHECK (parcelas BETWEEN 1 AND 12),
    CONSTRAINT CK_Assinatura_status   CHECK (statusAssinatura IN ('ATIVA', 'CANCELADA')),
    INDEX IX_Assinatura_planoId (planoId)
);
GO

-- dados iniciais (so entram se a tabela estiver vazia)

IF NOT EXISTS (SELECT 1 FROM dbo.Plano)
BEGIN
    INSERT INTO dbo.Plano (codigo, nome, descricao, precoMensal, precoAnual, destaque, ordem) VALUES
        ('plus', N'PLUS', N'Comece sua jornada segura agora! Ideal para pequenas empresas que desejam segurança acessível sem comprometer a qualidade. Proteja seus dados desde o primeiro dia!', 122, 1464, 0, 1),
        ('pro', N'PRO', N'A escolha inteligente para empresas em crescimento! Ganhe tranquilidade total com suporte prioritário. Milhares de empresas confiam neste plano!', 161, 1932, 1, 2),
        ('ultra', N'ULTRA', N'Máxima segurança corporativa para empresas exigentes! Invista na proteção total da sua infraestrutura digital!', 256, 3072, 0, 3);

    INSERT INTO dbo.PlanoBeneficio (planoId, descricao, ordem)
    SELECT p.id, v.descricao, v.ordem
    FROM (VALUES
        ('plus', N'Monitoramento básico', 1),
        ('plus', N'Firewall padrão', 2),
        ('plus', N'Relatórios mensais', 3),
        ('plus', N'Suporte por email', 4),
        ('pro', N'Monitoramento 24/7', 1),
        ('pro', N'Firewall avançado', 2),
        ('pro', N'Análise de vulnerabilidade', 3),
        ('pro', N'Relatórios semanais', 4),
        ('pro', N'Suporte prioritário', 5),
        ('ultra', N'Segurança completa', 1),
        ('ultra', N'Proteção de servidores', 2),
        ('ultra', N'IA de detecção de ataques', 3),
        ('ultra', N'Auditoria de segurança', 4),
        ('ultra', N'Suporte dedicado', 5)
    ) AS v(codigo, descricao, ordem)
    JOIN dbo.Plano p ON p.codigo = v.codigo;
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Servico)
BEGIN
    INSERT INTO dbo.Servico (titulo, descricao, iconeArquivo, textoAlternativo, ordem) VALUES
        (N'Monitoramento', N'Monitoramento contínuo contra ataques.', 'monitoramento.png', N'Monitoramento 24/7', 1),
        (N'Firewall Corporativo', N'Bloqueio avançado de invasões.', 'firewall.png', N'Firewall Corporativo', 2),
        (N'Análise de Vulnerabilidade', N'Detectamos falhas antes de hackers.', 'analise.png', N'Análise de Vulnerabilidade', 3),
        (N'Proteção de Dados', N'Segurança completa para bancos de dados.', 'protecao.png', N'Proteção de Dados', 4),
        (N'Consultoria em Conformidade', N'Assessoria especializada para atender regulamentações como LGPD e ISO 27001.', 'consultoria.png', N'Consultoria em Conformidade', 5),
        (N'Resposta a Incidentes', N'Plano de ação rápido para mitigar e recuperar de ciberataques.', 'respostas.png', N'Resposta a Incidentes', 6),
        (N'Segurança na Nuvem', N'Proteção avançada para ambientes cloud e dados migrados.', 'nuvem.png', N'Segurança na Nuvem', 7),
        (N'Gestão de Riscos', N'Identificação e mitigação proativa de vulnerabilidades empresariais.', 'gestao.png', N'Gestão de Riscos', 8);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Postagem)
BEGIN
    INSERT INTO dbo.Postagem (titulo, resumo, publicadoEm, imagemArquivo, textoAlternativo) VALUES
        (N'Novo Ataque de Ransomware Afeta Milhares de Empresas', N'Um novo strain de ransomware tem se espalhado rapidamente por infraestruturas corporativas. Entenda como a ShieldPME está protegendo seus clientes.', '2026-01-15', 'blog1.jpg', N'Ataque Ransomware'),
        (N'Atualizações da LGPD para 2026: O Que Mudou', N'As novas regulamentações de proteção de dados entram em vigor este ano. Veja o que sua empresa precisa fazer para se manter em conformidade.', '2026-01-10', 'blog2.jpg', N'LGPD 2026'),
        (N'Inteligência Artificial Revoluciona a Detecção de Ameaças', N'Nossos algoritmos de IA identificaram mais de 10 mil tentativas de invasão no último mês. Conheça a tecnologia por trás dessa proteção.', '2026-01-05', 'blog3.jpg', N'IA na Segurança'),
        (N'Case de Sucesso: Como Salvamos uma Fintech de um Ataque DDoS', N'Leia o relato completo de como nossa equipe neutralizou um ataque massivo em tempo recorde, evitando prejuízos milionários.', '2025-12-28', 'blog4.jpg', N'Case de Sucesso'),
        (N'5 Práticas Essenciais para Criar Senhas Invioláveis', N'Ainda usa "123456"? Descubra as melhores técnicas para criar credenciais robustas e proteger suas contas corporativas.', '2025-12-20', 'blog5.jpg', N'Senhas Seguras'),
        (N'Segurança na Nuvem: Mitos e Verdades', N'A migração para a nuvem é segura? Desmistificamos os principais mitos e mostramos como proteger seus dados em ambientes cloud.', '2025-12-15', 'blog6.jpg', N'Nuvem Segura');
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Ferramenta)
BEGIN
    INSERT INTO dbo.Ferramenta (nome, descricao, ordem) VALUES
        (N'Wireshark', N'Wireshark é o analisador de protocolos de rede mais popular do mundo. Utilizamos esta ferramenta para capturar e inspecionar pacotes de dados em tempo real, permitindo diagnosticar problemas de rede, detectar tráfego suspeito e identificar tentativas de exfiltração de dados. Nossa equipe utiliza Wireshark em investigações forenses e para monitoramento contínuo da integridade das comunicações corporativas.', 1),
        (N'Nmap', N'Nmap (Network Mapper) é a ferramenta padrão da indústria para descoberta de redes e auditoria de segurança. Utilizamos Nmap para mapear completamente a infraestrutura de rede dos nossos clientes, identificar dispositivos conectados, portas abertas e serviços em execução. Esta ferramenta é essencial para nossas avaliações de vulnerabilidade iniciais e para manter um inventário atualizado de ativos digitais.', 2),
        (N'Metasploit Framework', N'O Metasploit Framework é a plataforma líder mundial para testes de penetração e desenvolvimento de exploits. Nossa equipe de red team utiliza o Metasploit para simular ataques reais em ambientes controlados, identificando vulnerabilidades antes que criminosos cibernéticos possam explorá-las. Todos os testes são realizados com autorização prévia e seguem rigorosos protocolos éticos.', 3),
        (N'Burp Suite', N'Burp Suite é a ferramenta definitiva para testes de segurança de aplicações web. Utilizamos Burp Suite para identificar vulnerabilidades em APIs, aplicações web e serviços cloud dos nossos clientes. A ferramenta permite interceptar, inspecionar e modificar tráfego HTTP/HTTPS, automatizando a descoberta de falhas como SQL Injection, XSS e CSRF.', 4),
        (N'Snort', N'Snort é um sistema de detecção e prevenção de intrusões (IDS/IPS) de código aberto amplamente utilizado em ambientes corporativos. Implementamos Snort em nossas soluções de monitoramento 24/7 para analisar tráfego de rede em busca de assinaturas de ataques conhecidos, anomalias de comportamento e padrões de malware. A ferramenta é constantemente atualizada com as últimas regras de detecção.', 5),
        (N'OSSEC', N'OSSEC é uma plataforma de detecção de intrusões baseada em host (HIDS) que monitora logs, integridade de arquivos e atividades de processos em servidores e endpoints. Utilizamos OSSEC para garantir que qualquer alteração não autorizada em sistemas críticos seja detectada imediatamente, desde modificações em arquivos de configuração até execução de comandos suspeitos.', 6);

    INSERT INTO dbo.FerramentaRecurso (ferramentaId, descricao, ordem)
    SELECT f.id, v.descricao, v.ordem
    FROM (VALUES
        (N'Wireshark', N'Análise profunda de mais de 2.000 protocolos de rede', 1),
        (N'Wireshark', N'Captura de pacotes em tempo real com filtros avançados', 2),
        (N'Wireshark', N'Decodificação de tráfego criptografado para análise de metadados', 3),
        (N'Wireshark', N'Geração de relatórios detalhados de anomalias de rede', 4),
        (N'Nmap', N'Varredura de portas TCP/UDP com técnicas de evasão de firewall', 1),
        (N'Nmap', N'Detecção de sistemas operacionais e versões de serviços', 2),
        (N'Nmap', N'Scripts NSE (Nmap Scripting Engine) para verificação de vulnerabilidades', 3),
        (N'Nmap', N'Geração de topologias de rede visuais para documentação', 4),
        (N'Metasploit Framework', N'Execução de testes de penetração automatizados e manuais', 1),
        (N'Metasploit Framework', N'Desenvolvimento de exploits customizados para ambientes específicos', 2),
        (N'Metasploit Framework', N'Simulação de ataques de engenharia social e spear phishing', 3),
        (N'Metasploit Framework', N'Relatórios executivos com métricas de risco e recomendações', 4),
        (N'Burp Suite', N'Proxy de interceptação para análise de requisições web', 1),
        (N'Burp Suite', N'Scanner automatizado de vulnerabilidades OWASP Top 10', 2),
        (N'Burp Suite', N'Extensões customizadas para testes de APIs REST e GraphQL', 3),
        (N'Burp Suite', N'Relatórios de conformidade com padrões PCI-DSS e ISO 27001', 4),
        (N'Snort', N'Detecção em tempo real de tentativas de intrusão e malware', 1),
        (N'Snort', N'Regras customizadas baseadas no perfil de risco de cada cliente', 2),
        (N'Snort', N'Integração com SIEM para correlação de eventos de segurança', 3),
        (N'Snort', N'Bloqueio automático de tráfego malicioso em modo IPS', 4),
        (N'OSSEC', N'Monitoramento de integridade de arquivos críticos do sistema', 1),
        (N'OSSEC', N'Análise de logs de múltiplas fontes em tempo real', 2),
        (N'OSSEC', N'Detecção de rootkits e atividades de privilege escalation', 3),
        (N'OSSEC', N'Resposta automática a incidentes com isolamento de hosts', 4)
    ) AS v(ferramenta, descricao, ordem)
    JOIN dbo.Ferramenta f ON f.nome = v.ferramenta;
END
GO

SELECT 'Plano' AS tabela, COUNT(*) AS linhas FROM dbo.Plano
UNION ALL SELECT 'PlanoBeneficio',   COUNT(*) FROM dbo.PlanoBeneficio
UNION ALL SELECT 'Servico',          COUNT(*) FROM dbo.Servico
UNION ALL SELECT 'Postagem',         COUNT(*) FROM dbo.Postagem
UNION ALL SELECT 'Ferramenta',       COUNT(*) FROM dbo.Ferramenta
UNION ALL SELECT 'FerramentaRecurso',COUNT(*) FROM dbo.FerramentaRecurso;
GO
