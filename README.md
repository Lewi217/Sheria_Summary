# SheriaSummary

AI-powered chatbot for understanding Kenyan AI & Data Protection legislation. Upload a legal PDF and ask questions in plain English — get instant answers that cite exact Sections and Clauses.

## The Problem

Kenya's AI Bill 2026 and updated Data Protection regulations are long, technical, and intimidating. Small tech startups and students struggle to know what applies to them.

## The Solution

A RAG (Retrieval-Augmented Generation) backend that lets you upload any Kenyan legal PDF and ask specific questions — with answers grounded in the actual document text.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.6 |
| AI / RAG | Spring AI 2.0.0-M4 |
| LLM | Azure OpenAI `gpt-4.1` |
| Embeddings | Azure OpenAI `text-embedding-3-large` (1536 dims) |
| Vector Store | Neon PostgreSQL + pgvector |
| PDF Parsing | Spring AI PDF Document Reader |
| Deployment | Docker + Docker Compose (DigitalOcean) |

---

## API Endpoints

### Documents

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/documents/upload` | Upload a PDF for ingestion |
| `GET` | `/api/documents` | List all uploaded documents |
| `DELETE` | `/api/documents/{id}` | Remove a document |

### Chat

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/chat` | Ask a question (JSON response) |
| `POST` | `/api/chat/stream` | Ask a question (SSE streaming) |
| `GET` | `/api/health` | Health check |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- A [Neon](https://neon.tech) PostgreSQL database (with pgvector enabled)
- An [Azure OpenAI](https://azure.microsoft.com/en-us/products/ai-services/openai-service) resource with `gpt-4.1` and `text-embedding-3-large` deployments

### 1. Clone the repository

```bash
git clone https://github.com/your-username/SheriaSummary.git
cd SheriaSummary
```

### 2. Configure credentials

Create `src/main/resources/keys.properties` (this file is gitignored — never commit it):

```properties
DB_URL=jdbc:postgresql://<your-neon-host>/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=your_neon_password
AZURE_OPENAI_API_KEY=your_azure_openai_api_key
AZURE_OPENAI_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/
AZURE_OPENAI_CHAT_DEPLOYMENT=gpt-4.1
AZURE_OPENAI_EMBEDDING_DEPLOYMENT=text-embedding-3-large
```

### 3. Run locally

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8079`.

---

## Testing with Postman

### Step 1 — Upload a PDF

```
POST http://localhost:8079/api/documents/upload
Body → form-data
  key:   file   (type: File)
  value: [select your PDF]
```

Response:

```json
{
  "documentId": "3f7a1b2c-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "filename": "AI_Bill_2026.pdf",
  "chunksCreated": 42,
  "message": "Document ingested successfully. Ready for querying."
}
```

### Step 2 — Ask a question

```
POST http://localhost:8079/api/chat
Content-Type: application/json
```

```json
{
  "question": "What are the fines for non-compliance?",
  "documentId": null
}
```

Pass `documentId` from the upload response to scope the search to a specific document. Leave it `null` to search across all uploaded documents.

Response:

```json
{
  "answer": "According to Section 45 of the AI Bill 2026, non-compliant entities face...",
  "sources": ["AI_Bill_2026.pdf - Page 12", "AI_Bill_2026.pdf - Page 13"],
  "chunksRetrieved": 6
}
```

### Step 3 — Streaming (optional)

```
POST http://localhost:8079/api/chat/stream
Content-Type: application/json
Accept: text/event-stream
```

```json
{
  "question": "Summarize the data controller obligations"
}
```

---

## Docker Deployment

### 1. Create a `.env` file (never commit this)

```env
DB_URL=jdbc:postgresql://<your-neon-host>/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=your_password
AZURE_OPENAI_API_KEY=your_azure_openai_api_key
AZURE_OPENAI_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/
AZURE_OPENAI_CHAT_DEPLOYMENT=gpt-4.1
AZURE_OPENAI_EMBEDDING_DEPLOYMENT=text-embedding-3-large
```

### 2. Build and run

```bash
docker-compose up --build -d
```

### 3. Check logs

```bash
docker-compose logs -f sheria-summary
```

---

## How It Works (RAG Flow)

```
Upload PDF
    └── Extract text (page by page)
        └── Chunk into ~512 token segments
            └── Embed each chunk  →  Azure OpenAI text-embedding-3-large
                └── Store vectors in Neon pgvector

Ask a Question
    └── Embed the question
        └── Find top-6 similar chunks (cosine similarity)
            └── Send chunks + question to Azure OpenAI gpt-4.1
                └── Return answer with section citations
```

---

## Example Questions to Try

After uploading the AI Bill or Data Protection Act PDF:

- *"Does this law require me to register my backend service with the government?"*
- *"What are the fines for a data breach?"*
- *"What counts as sensitive personal data?"*
- *"Which government body oversees AI compliance?"*
- *"What rights do data subjects have under this law?"*
- *"What is the deadline for compliance?"*

---

## Project Structure

```
src/main/java/com/DevLewi/SheriaSummary/
├── config/          AppConfig.java         (ChatClient, CORS, pgvector init)
├── controller/      ChatController.java    (chat + streaming endpoints)
│                    DocumentController.java (upload, list, delete)
│                    HealthController.java  (health check)
├── dto/             ChatRequest.java
│                    ChatResponse.java
│                    UploadResponse.java
├── exception/       GlobalExceptionHandler.java
├── model/           DocumentRecord.java    (JPA entity)
├── repository/      DocumentRepository.java
└── service/         DocumentService.java   (PDF ingestion + vectorization)
                     ChatService.java       (RAG query + streaming)
```

---

## License

MIT
