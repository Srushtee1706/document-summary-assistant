````markdown
# Document Summary Assistant

An AI-powered web application that extracts text from uploaded documents and generates concise summaries and key points.

## Live Demo

[Open Document Summary Assistant](https://document-summary-assistant-frontend-o0ol.onrender.com)

##  Features

- Upload PDF, PNG, JPG, and JPEG documents
- Extract text from PDF documents
- OCR support for image-based documents using Tesseract
- AI-powered summarization using Google Gemini
- Generate summaries with different lengths
- Extract important key points
- Responsive React frontend
- REST API powered by Spring Boot
- Dockerized backend for deployment

##  Tech Stack

**Frontend**
- React
- Vite
- CSS

**Backend**
- Java 17
- Spring Boot
- REST APIs
- Apache PDFBox
- Tesseract OCR

**AI**
- Google Gemini API

**Deployment**
- Docker
- Render

##  Architecture

The React frontend sends uploaded documents to the Spring Boot REST API. The backend determines whether text extraction or OCR is required, processes the document, and sends the extracted content to Gemini for summarization. The generated summary and key points are returned to the frontend.

##  Configuration

The Gemini API key is stored securely as an environment variable:

`GEMINI_API_KEY`

The API key is not included in the repository.

##  Run Locally

```bash
git clone https://github.com/Srushtee1706/document-summary-assistant.git
cd document-summary-assistant
````

Set your `GEMINI_API_KEY` environment variable, then start the backend:

```bash
./mvnw spring-boot:run
```

Start the frontend:

```bash
cd frontend
npm install
npm run dev
```

The application will be available at `http://localhost:5173`.

````

