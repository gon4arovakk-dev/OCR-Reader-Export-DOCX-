📄 OCR Reader (Export DOCX) — Multi‑Language Document Scanner
8 languages, one powerful OCR tool – extract text from images, recognize handwriting, and export results to DOCX format – right from your terminal.

✨ Features
🖼️ Image text extraction – supports PNG, JPG, JPEG, BMP, TIFF formats

🔤 Multi‑language OCR – recognizes text in multiple languages (English, Russian, etc.)

📄 DOCX export – generates professional Word documents with recognized text

🎨 Formatted output – preserves text with styling (bold, italic, font size)

📋 Plain text fallback – simple text output when DOCX library is unavailable

💾 Batch processing – process multiple images in a single run

🖥️ Cross‑platform – works on Windows, macOS, and Linux

🧰 Supported Languages & Files
Language	File	Dependencies
Python	ocr_reader.py	pytesseract, python-docx, Pillow
Go	ocr_reader.go	github.com/otiai10/gosseract/v2, github.com/nguyenthenguyen/docx
JavaScript (Node)	ocr_reader.js	tesseract.js, docx
Ruby	ocr_reader.rb	rtesseract, docx gem
PHP	ocr_reader.php	thiagoalessio/tesseract_ocr, phpdocx
Java	OcrReader.java	tess4j, org.docx4j
C#	OcrReader.cs	Tesseract.NET, DocX (Novacode)
C++	ocr_reader.cpp	tesseract (C‑API), docx (custom)
🚀 Quick Start
All implementations follow the same CLI pattern:

bash
# Extract text from a single image
<command> image.png

# Extract text from multiple images
<command> image1.png image2.jpg image3.bmp

# Export to DOCX (default)
<command> image.png --output result.docx

# Export to plain text
<command> image.png --output result.txt

# Specify language (default: eng)
<command> image.png --lang rus

# Show help
<command> --help
Arguments:

<image> – path to the image file (required)

--output <file> – output file path (default: output.docx)

--lang <lang> – OCR language (default: eng)

--format <docx|txt> – output format (default: docx)

📸 Example Output
text
📄 OCR Reader
Image: document.png
Language: eng
Extracting text... ✅

📖 Extracted text:
This is a sample document extracted from an image.
It contains multiple lines of text.

📄 Exporting to DOCX... ✅
File saved: document.docx
📁 Repository Structure
text
.
├── README.md
├── python/
│   └── ocr_reader.py
├── go/
│   └── ocr_reader.go
├── javascript/
│   └── ocr_reader.js
├── ruby/
│   └── ocr_reader.rb
├── php/
│   └── ocr_reader.php
├── java/
│   └── OcrReader.java
├── csharp/
│   └── OcrReader.cs
└── cpp/
    └── ocr_reader.cpp
