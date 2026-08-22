# 🤖 RAG Corporativo — Assistente Virtual de Documentos

> **Challenge Alura IA & Java**  
> Aplicação corporativa completa de **RAG (Retrieval-Augmented Generation)** com **Spring AI**, **Groq / Ollama**, **PostgreSQL com PGVector (Supabase / Docker)** e **Frontend Web Integrado**, com deploy no **Railway**.

---

## 📋 Sumário
- [Sobre o Projeto](#-sobre-o-projeto)
- [Projeto no Ar (Ambiente em Produção)](#-projeto-no-ar-ambiente-em-produção)
- [Arquitetura e Fluxo do RAG](#-arquitetura-e-fluxo-do-rag)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução Local](#-instalação-e-execução-local)
- [Configuração de Deploy (Railway & Supabase)](#-configuração-de-deploy-railway--supabase)
- [Documentação da API REST](#-documentação-da-api-rest)
- [Como Testar a Aplicação](#-como-testar-a-aplicação)
- [Frontend Integrado](#-frontend-integrado)
- [Solução de Problemas (Troubleshooting)](#-solução-de-problemas-troubleshooting)

---

## 💡 Sobre o Projeto

O **RAG Corporativo** é uma solução inteligente desenvolvida para responder a dúvidas de colaboradores com base **estritamente em documentos oficiais da empresa** (políticas de RH, normas financeiras, manuais técnicos e regulamentos jurídicos).

### Principais Benefícios:
- 🚫 **Zero Alucinação**: O modelo responde apenas com informações contidas nos documentos carregados no banco vetorial.
- 📚 **Citação Precisa de Fontes**: Cada resposta indica o nome do documento e o número da página correspondente.
- 🔒 **Flexível (Nuvem ou Local)**: Funciona tanto em nuvem de alta disponibilidade (**Railway + Supabase + Groq**) quanto 100% local com **Ollama e Docker**.
- 🖥️ **Interface Web Moderna**: Frontend intuitivo para consulta via chat e ingestão de arquivos PDF com Drag & Drop.

---

## 🌐 Projeto no Ar (Ambiente em Produção)

A aplicação está pronta e configurada para execução contínua em nuvem (PaaS e DBaaS), integrando os seguintes serviços:

| Serviço / Infraestrutura | Finalidade no Projeto | Detalhes Técnicos |
| :--- | :--- | :--- |
| 🚂 **[Railway](https://railway.app/)** | **Hospedagem da Aplicação Fullstack** | Executa o backend Java 21 / Spring Boot 3 e serve a interface Web estática diretamente na nuvem. |
| ⚡ **[Supabase](https://supabase.com/)** | **Banco de Dados Relacional & Vetorial** | Instância PostgreSQL 16 gerenciada com extensão `pgvector` para armazenar chunks, metadados e executar buscas semânticas (HNSW). |
| 🚀 **[Groq Cloud](https://groq.com/)** | **Inferência de LLM em Alta Velocidade** | Processamento com modelos como `llama3.3` / `gpt-oss` via Spring AI OpenAI Starter com baixíssima latência. |
| 🧠 **ONNX Runtime / Transformers** | **Embeddings Semânticos** | Geração de vetores semânticos (`all-MiniLM-L6-v2`, 384 dimensões) diretamente na JVM, sem custos extras de API. |

### 🔗 Como Acessar a Aplicação Online

1. **Acesso Web**: Abra no seu navegador o endereço da aplicação no Railway:
   👉 **`https://agenteiachallengealura-production.up.railway.app/`**
2. **Ingestão no Ar**: Vá até a aba **Upload de Documentos**, anexe arquivos PDF oficiais e selecione a categoria. Os dados serão fatiados e persistidos diretamente no **Supabase**.
3. **Chat Inteligente**: Na aba **Chat de Dúvidas**, faça perguntas e veja o assistente buscar os trechos no banco vetorial do Supabase e formular as respostas via Groq citando as fontes.

---

## 🏛️ Arquitetura e Fluxo do RAG

```
[ Usuário / Colaborador ]
        │
        ├── 1. Upload de PDF (Categoria, Autor)
        │       │
        │       ▼
        │   [ Apache Tika ] ──> Extrai Texto Bruto e Metadados
        │       │
        │       ▼
        │   [ TokenTextSplitter ] ──> Divide em Chunks Inteligentes
        │       │
        │       ▼
        │   [ Ollama: nomic-embed-text ] ──> Gera Vetores (768 dimensões)
        │       │
        │       ▼
        │   [ PostgreSQL + PGVector ] ──> Armazena Vetores com HNSW e Índice Cosseno
        │
        └── 2. Pergunta no Chat ("Qual o prazo de férias?")
                │
                ▼
            [ Ollama: nomic-embed-text ] ──> Vetoriza a Dúvida
                │
                ▼
            [ PostgreSQL PGVector ] ──> Busca Semântica (Top K chunks mais similares)
                │
                ▼
            [ RAG Prompt Template ] ──> Injeta Chunks no Contexto + Regras Restritivas
                │
                ▼
            [ Ollama: Llama 3.2 ] ──> Gera Resposta Objetiva + Citações
                │
                ▼
        [ Frontend Web ] ──> Exibe Mensagem Formatada + Badges das Fontes
```

---

## 🛠️ Tecnologias Utilizadas

### Backend & Frameworks
* **Java 21**
* **Spring Boot 3.3.4**
* **Spring AI (1.0.0-M3)**: Orquestração de IA generativa, prompts e vector stores.
* **Spring AI PGVector Store**: Conexão com PostgreSQL com extensão vetorial (HNSW, Cosine Distance).
* **Spring AI OpenAI Starter**: Integração com API da **Groq** para geração de chat em tempo real.
* **Spring AI Transformers (ONNX)**: Geração de embeddings localmente no backend.
* **Apache Tika Document Reader**: Extração robusta de texto e metadados de arquivos PDF.

### Nuvem, Banco de Dados & Infraestrutura
* 🚂 **Railway**: Hospedagem PaaS da aplicação backend Spring Boot e do frontend.
* ⚡ **Supabase**: Banco de dados PostgreSQL 16 gerenciado com `pgvector` ativado.
* 🚀 **Groq Cloud**: Plataforma de aceleração de LLMs (Llama 3.3 / GPT-OSS).
* 🐳 **Docker & Docker Compose**: Ambiente local para execução de containers PGVector e Ollama.

### Modelos de Inteligência Artificial
* **LLM em Nuvem (Groq)**: `openai/gpt-oss-20b` ou `llama-3.3-70b-versatile` (respostas rápidas e precisas).
* **LLM Local (Ollama - opcional)**: `llama3.2`
* **Embedding Model**: `all-MiniLM-L6-v2` (ONNX local, 384 dimensões) / `nomic-embed-text` (Ollama)

### Frontend
* **HTML5 Semântico**: Estruturação acessível com suporte a navegação por abas.
* **CSS3 Moderno**: Tema escuro (Dark Theme), CSS Variables, Flexbox, Grid e layout responsivo (Desktop e Mobile).
* **Vanilla JavaScript (ES6+)**: Consumo assíncrono via `fetch`, gerenciamento de estados, eventos de Drag & Drop e manipulação dinâmica do DOM.
* **Marked.js**: Renderizador de Markdown para formatação de tópicos, negrito e código nas respostas da IA.
* **FontAwesome 6**: Ícones visuais de status e ações.

---

## 📂 Estrutura do Projeto

```
rag-alura/
├── docker-compose.yml             # Orquestração do PGVector e Ollama
├── pom.xml                        # Dependências Maven do Spring Boot e Spring AI
├── src/
│   ├── main/
│   │   ├── java/com/romualdo/rag_alura/
│   │   │   ├── RagAluraApplication.java          # Classe principal Spring Boot
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java                # Roteamento estático e boas-vindas (/)
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java           # Endpoint REST de Chat (/api/v1/chat)
│   │   │   │   └── DocumentController.java       # Endpoint REST de Upload (/api/v1/documents)
│   │   │   ├── dto/
│   │   │   │   ├── ChatRequestDTO.java           # Record DTO para requisição de pergunta
│   │   │   │   └── ChatResponseDTO.java          # Record DTO com resposta e fontes
│   │   │   └── service/
│   │   │       ├── DocumentIngestionService.java # Processamento e vetorização de PDFs
│   │   │       └── RagChatService.java           # Busca semântica e prompt contextualizado
│   │   └── resources/
│   │       ├── application.yml                   # Configurações do PostgreSQL, Ollama e Vector Store
│   │       └── static/                           # Frontend da Aplicação
│   │           ├── index.html                    # Interface Web unificada
│   │           ├── style.css                     # Estilos visuais e temas
│   │           └── app.js                        # Lógica cliente e integração REST
```

---

## 📦 Pré-requisitos

Certifique-se de ter instalado em seu ambiente:
1. **Java JDK 21** ou superior ([Download OpenJDK](https://adoptium.net/)).
2. **Docker Desktop** (com suporte a Docker Compose) ([Download Docker](https://www.docker.com/products/docker-desktop/)).
3. **Maven** (opcional, pois o projeto inclui o wrapper `./mvnw` ou `mvnw.cmd`).

---

## 🚀 Instalação e Execução Local

### Passo 1: Subir o Banco Vetorial e o Ollama
No diretório raiz do projeto, execute:
```bash
docker-compose up -d
```
> Isso iniciará dois containers:
> - `rag-postgres`: PostgreSQL 16 com extensão `pgvector` na porta `5432`.
> - `rag-ollama`: Servidor Ollama na porta `11434`.

### Passo 2: Baixar os Modelos no Ollama
Faça o download dos modelos de IA necessários executando:

```bash
# Baixar o modelo de Embeddings (nomic-embed-text)
docker exec -it rag-ollama ollama pull nomic-embed-text

# Baixar o modelo de Geração de Texto (llama3.2)
docker exec -it rag-ollama ollama pull llama3.2
```

### Passo 3: Executar a Aplicação Spring Boot
Execute o backend utilizando o Maven Wrapper:

* **Linux / macOS**:
  ```bash
  ./mvnw spring-boot:run
  ```
* **Windows (PowerShell / CMD)**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```

A aplicação iniciará na porta **`8080`**.

### Passo 4: Acessar a Interface Web
Abra o navegador e acesse:
👉 **[http://localhost:8080](http://localhost:8080)**

---

## 🔌 Documentação da API REST

### 1. Ingestão de Documentos (Upload de PDF)
Processa um arquivo PDF com Apache Tika, divide em chunks, gera embeddings e salva no PGVector.

* **URL**: `POST /api/v1/documents/upload`
* **Content-Type**: `multipart/form-data`
* **Parâmetros**:
  - `file` (obrigatório): Arquivo `.pdf`
  - `category` (opcional, padrão: `"GERAL"`): Categoria do documento (ex: `RH`, `FINANCEIRO`, `TI`)
  - `author` (opcional, padrão: `"Admin"`): Nome do autor ou departamento emissor

**Exemplo de Resposta de Sucesso (200 OK):**
```json
{
  "message": "Documento processado e indexado com sucesso!"
}
```

---

### 2. Chat com Busca Semântica (RAG)
Realiza a busca vetorial por similaridade no PGVector e gera a resposta contextualizada via Llama 3.2.

* **URL**: `POST /api/v1/chat`
* **Content-Type**: `application/json`
* **Corpo da Requisição (JSON)**:
```json
{
  "question": "Quais são as regras para solicitação de reembolso de combustível?",
  "category": "FINANCEIRO"
}
```

**Exemplo de Resposta (200 OK):**
```json
{
  "answer": "Para solicitar o reembolso de combustível, o colaborador deve apresentar o comprovante fiscal emitido em até 5 dias úteis após a viagem e preencher o formulário no portal corporativo.",
  "sources": [
    "politica_de_viagens_2026.pdf (Pág. 3)",
    "manual_reembolso.pdf (Pág. 1)"
  ]
}
```

---

## 🧪 Como Testar a Aplicação

### Teste 1: Pela Interface Web (Recomendado)
1. Acesse `http://localhost:8080`.
2. Clique na aba **"Upload de Documentos"** no menu lateral.
3. Arraste ou selecione um documento PDF com informações corporativas (ex: um manual ou cartilha).
4. Selecione a categoria (ex: `RH`) e clique em **"Processar e Indexar Documento"**.
5. Aguarde a mensagem de sucesso.
6. Vá para a aba **"Chat de Dúvidas"** e faça uma pergunta sobre o conteúdo do documento.
7. Observe a resposta estruturada e os **badges com as fontes e páginas** citadas logo abaixo do texto.

---

### Teste 2: Teste via cURL (Linha de Comando)

#### 1. Fazer Upload de um PDF:
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@caminho/para/seu/arquivo.pdf" \
  -F "category=RH" \
  -F "author=DepartamentoPessoal"
```

#### 2. Fazer uma Pergunta ao Chat:
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Como solicitar férias pelo sistema interno?",
    "category": "RH"
  }'
```

---

### Teste 3: Testando a Proteção Anti-Alucinação
Pergunte algo que **não existe** em nenhum dos documentos cadastrados (ex: *"Qual a receita secreta da torta de maçã da diretoria?"*).

**Comportamento Esperado:**
O assistente responderá formalmente:
> *"Não encontrei essa informação nos documentos oficiais disponíveis. Por favor, entre em contato com a área responsável (RH, Financeiro ou Jurídico)."*

---

### Teste 4: Consultar os Vetores no PostgreSQL
Caso queira inspecionar a base vetorial diretamente no banco de dados:

```bash
docker exec -it rag-postgres psql -U postgres -d rag_db -c "SELECT id, content, metadata FROM vector_store LIMIT 3;"
```

---

## 💻 Frontend Integrado

O frontend foi projetado para ser intuitivo, rápido e responsivo:
- **Painel Split-View & Abas**: Alternância suave entre Chat e Ingestão.
- **Filtro de Categoria**: Permite focar a busca em departamentos específicos.
- **Sugestões Rápidas**: Botões interativos para testar perguntas comuns com 1 clique.
- **Indicador de Digitação**: Animação em tempo real enquanto a IA gera a resposta.
- **Auto-Ajuste de Texto**: A caixa de mensagem se expande automaticamente conforme o usuário digita.

---

## 🔧 Solução de Problemas (Troubleshooting)

| Problema | Causa Provável | Solução |
| :--- | :--- | :--- |
| `Connection refused: localhost:11434` | O container do Ollama não está rodando. | Execute `docker-compose up -d` e confira com `docker ps`. |
| `Model 'nomic-embed-text' not found` | O modelo não foi baixado no Ollama. | Execute `docker exec -it rag-ollama ollama pull nomic-embed-text`. |
| `Model 'llama3.2' not found` | O modelo Llama 3.2 não foi baixado. | Execute `docker exec -it rag-ollama ollama pull llama3.2`. |
| `Connection refused: localhost:5432` | O PostgreSQL não iniciou corretamente. | Verifique se a porta 5432 não está em uso por outro serviço local. |
| `NoResourceFoundException: No static resource .` | Arquivos estáticos não foram carregados na compilação. | Reinicie o Spring Boot com `./mvnw spring-boot:run`. |

---

## 👨‍💻 Autor & Licença

Projeto desenvolvido para o **Challenge Alura IA & Java**.  
Distribuído sob a licença MIT. Sinta-se livre para usar, estudar e contribuir!
