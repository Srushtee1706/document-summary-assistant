import { useState } from "react";
import "./App.css";

function App() {
  const [file, setFile] = useState(null);
  const [length, setLength] = useState("MEDIUM");

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const [dragging, setDragging] = useState(false);

  // Controls Summary / Key Points tab
  const [activeTab, setActiveTab] = useState("summary");

  // Controls Copy button text
  const [copied, setCopied] = useState(false);

  const validateFile = (selectedFile) => {
    if (!selectedFile) {
      return false;
    }

    const fileName = selectedFile.name.toLowerCase();

    const allowedExtensions = [
      ".pdf",
      ".png",
      ".jpg",
      ".jpeg",
    ];

    const validExtension = allowedExtensions.some((extension) =>
      fileName.endsWith(extension)
    );

    if (!validExtension) {
      setError("Please upload a PDF, PNG, JPG, or JPEG file.");
      setFile(null);
      return false;
    }

    if (selectedFile.size > 10 * 1024 * 1024) {
      setError("File size must be less than 10 MB.");
      setFile(null);
      return false;
    }

    setError("");
    setResult(null);
    setCopied(false);
    setFile(selectedFile);

    return true;
  };

  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];

    validateFile(selectedFile);
  };

  const handleDragOver = (event) => {
    event.preventDefault();
    setDragging(true);
  };

  const handleDragLeave = (event) => {
    event.preventDefault();
    setDragging(false);
  };

  const handleDrop = (event) => {
    event.preventDefault();
    setDragging(false);

    const droppedFile = event.dataTransfer.files[0];

    validateFile(droppedFile);
  };

  const handleGenerateSummary = async () => {
    if (!file) {
      setError("Please upload a document first.");
      return;
    }

    setLoading(true);
    setError("");
    setResult(null);
    setCopied(false);

    const formData = new FormData();

    formData.append("file", file);

    try {
      const response = await fetch(
        `https://document-summary-assistant-pvm7.onrender.com/api/documents/process?length=${length}`,
        {
          method: "POST",
          body: formData,
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || "Unable to process the document."
        );
      }

      setResult(data);

      // Always start on Summary tab
      setActiveTab("summary");

    } catch (exception) {
      setError(
        exception.message ||
          "Something went wrong while processing the document."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = async () => {
    if (!result) {
      return;
    }

    let textToCopy = "";

    if (activeTab === "summary") {
      textToCopy = result.summary || "";
    } else {
      textToCopy =
        result.keyPoints
          ?.map((point) => `• ${point}`)
          .join("\n") || "";
    }

    if (!textToCopy) {
      return;
    }

    try {
      await navigator.clipboard.writeText(textToCopy);

      setCopied(true);

      setTimeout(() => {
        setCopied(false);
      }, 2000);

    } catch (clipboardError) {

      // Fallback for browsers where Clipboard API fails
      const textarea = document.createElement("textarea");

      textarea.value = textToCopy;

      textarea.style.position = "fixed";
      textarea.style.left = "-9999px";

      document.body.appendChild(textarea);

      textarea.focus();
      textarea.select();

      try {
        document.execCommand("copy");

        setCopied(true);

        setTimeout(() => {
          setCopied(false);
        }, 2000);

      } catch (fallbackError) {
        setError("Unable to copy content.");

      } finally {
        document.body.removeChild(textarea);
      }
    }
  };

  const handleNewSummary = () => {
    setFile(null);
    setResult(null);
    setError("");
    setLength("MEDIUM");
    setActiveTab("summary");
    setCopied(false);
  };

  return (
    <div className="app">

      {/* ================= NAVBAR ================= */}

      <header className="navbar">

        <div className="brand">

          <div className="brand-icon">
            ✦
          </div>

          <span>
            DocuSummarize
          </span>

        </div>

        <button
          className="new-summary-button"
          onClick={handleNewSummary}
        >
          + New Summary
        </button>

      </header>


      {/* ================= HERO ================= */}

      <section className="hero">

        <div className="hero-badge">
          ✦ AI-POWERED DOCUMENT SUMMARIZATION
        </div>

        <h1>
          {result ? (
            <>
              Your Summary is <span>Ready!</span>
            </>
          ) : (
            <>
              Turn Long Documents
              <br />
              Into Clear Insights.
            </>
          )}
        </h1>

        <p>
          Upload your document and let AI extract the most
          important information for you.
        </p>

      </section>


      {/* ================= DASHBOARD ================= */}

      <main className="dashboard">

        {/* ================= LEFT ================= */}

        <section className="left-panel">

          {/* Upload */}

          <div className="panel-card">

            <h2>
              Upload Document
            </h2>

            <div
              className={`drop-zone ${
                dragging ? "dragging" : ""
              }`}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
            >

              <div className="upload-symbol">
                ↑
              </div>

              <h3>
                {dragging
                  ? "Drop your document here"
                  : "Drag & drop your document"}
              </h3>

              <p>
                or choose a file from your computer
              </p>

              <label className="browse-button">

                Browse Files

                <input
                  type="file"
                  accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg"
                  hidden
                  onChange={handleFileChange}
                />

              </label>

              <small>
                PDF, PNG, JPG or JPEG · Max 10 MB
              </small>

            </div>


            {/* Selected file */}

            {file && (

              <div className="file-card">

                <div className="file-icon">

                  {file.name
                    .toLowerCase()
                    .endsWith(".pdf")
                    ? "PDF"
                    : "IMG"}

                </div>

                <div className="file-details">

                  <strong>
                    {file.name}
                  </strong>

                  <span>
                    {(file.size / 1024).toFixed(1)} KB
                  </span>

                </div>

                <button
                  className="remove-file"
                  onClick={() => setFile(null)}
                >
                  ×
                </button>

              </div>

            )}

          </div>


          {/* Summary Length */}

          <div className="panel-card">

            <h2>
              Summary Length
            </h2>

            <div className="length-grid">

              <button
                className={
                  length === "SHORT"
                    ? "length-option selected"
                    : "length-option"
                }
                onClick={() => setLength("SHORT")}
              >

                <strong>
                  Short
                </strong>

                <span>
                  2 sentences
                </span>

              </button>


              <button
                className={
                  length === "MEDIUM"
                    ? "length-option selected"
                    : "length-option"
                }
                onClick={() => setLength("MEDIUM")}
              >

                <strong>
                  Medium
                </strong>

                <span>
                  5 sentences
                </span>

              </button>


              <button
                className={
                  length === "LONG"
                    ? "length-option selected"
                    : "length-option"
                }
                onClick={() => setLength("LONG")}
              >

                <strong>
                  Long
                </strong>

                <span>
                  8 sentences
                </span>

              </button>

            </div>


            <button
              className="generate-button"
              onClick={handleGenerateSummary}
              disabled={loading}
            >

              {loading ? (
                <>
                  <span className="spinner"></span>
                  Processing...
                </>
              ) : (
                <>
                  ✦ Generate Summary
                </>
              )}

            </button>


            <div className="security-note">
              Your document is processed securely.
            </div>

          </div>

        </section>


        {/* ================= RIGHT ================= */}

        <section className="right-panel">

          {/* Empty */}

          {!result && !loading && (

            <div className="empty-result">

              <div className="empty-icon">
                ✦
              </div>

              <h2>
                Your summary will appear here
              </h2>

              <p>
                Upload a document, select your preferred
                summary length, and click Generate Summary.
              </p>

            </div>

          )}


          {/* Loading */}

          {loading && (

            <div className="empty-result">

              <div className="loading-icon">

                <span className="spinner large"></span>

              </div>

              <h2>
                Generating your summary...
              </h2>

              <p>
                Extracting content and analyzing your document.
                This may take a few seconds.
              </p>

            </div>

          )}


          {/* Result */}

          {result && (

            <div className="result-container">

              {/* Result Header */}

              <div className="result-header">

                <div className="tabs">

                  <button
                    className={
                      activeTab === "summary"
                        ? "tab active"
                        : "tab"
                    }
                    onClick={() =>
                      setActiveTab("summary")
                    }
                  >
                    Summary
                  </button>


                  <button
                    className={
                      activeTab === "keypoints"
                        ? "tab active"
                        : "tab"
                    }
                    onClick={() =>
                      setActiveTab("keypoints")
                    }
                  >
                    Key Points
                  </button>

                </div>


                <button
                  className="copy-button"
                  onClick={handleCopy}
                >
                  {copied ? "✓ Copied" : "Copy"}
                </button>

              </div>


              {/* SUMMARY TAB */}

              {activeTab === "summary" && (

                <div className="result-section">

                  <div className="section-title">

                    <span>
                      ✦
                    </span>

                    <h2>
                      Summary
                    </h2>

                  </div>

                  <p className="summary-text">
                    {result.summary}
                  </p>

                </div>

              )}


              {/* KEY POINTS TAB */}

              {activeTab === "keypoints" && (

                <div className="result-section">

                  <div className="section-title">

                    <span>
                      ☷
                    </span>

                    <h2>
                      Key Points
                    </h2>

                  </div>


                  {result.keyPoints &&
                  result.keyPoints.length > 0 ? (

                    <ul className="key-points-list">

                      {result.keyPoints.map(
                        (point, index) => (

                          <li key={index}>

                            <span className="bullet">
                              ✓
                            </span>

                            <span>
                              {point}
                            </span>

                          </li>

                        )
                      )}

                    </ul>

                  ) : (

                    <p className="no-key-points">
                      No key points were generated.
                    </p>

                  )}

                </div>

              )}

            </div>

          )}

        </section>

      </main>


      {/* ================= ERROR ================= */}

      {error && (

        <div className="error-toast">
          {error}
        </div>

      )}


      <footer>
        Document Summary Assistant · AI-powered document analysis
      </footer>

    </div>
  );
}

export default App;