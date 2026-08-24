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
## How I Built This Project

I built Document Summary Assistant to make it easier to understand long documents quickly. The user can upload a PDF or an image and select how long they want the summary to be.

I used React and Vite to build the frontend, where users can upload documents, choose the summary length, and view the generated summary and key points. For the backend, I used Java and Spring Boot to create REST APIs and handle the document processing.

For normal PDFs, the application extracts the text directly. For scanned documents and images, I added Tesseract OCR to read the text from the document. After extracting the text, I send it to the Google Gemini API, which generates the summary and important key points.

I also added error handling and file-size/type validation to make the application more reliable.

For deployment, I created a Dockerfile for the Spring Boot backend and deployed the backend and React frontend separately on Render. I kept the Gemini API key as an environment variable instead of putting it in GitHub.

The project helped me understand how a frontend, backend, OCR, AI API, Docker, and cloud deployment can work together as one complete application.

Submitted By:
Name: Srushtee Abhijit Patil
College: Vellore Institute of Technology AP
Reg_No: 23BCE7553

