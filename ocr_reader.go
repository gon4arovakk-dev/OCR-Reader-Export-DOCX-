// ocr_reader.go
package main

import (
	"flag"
	"fmt"
	"os"
	"strings"

	"github.com/nguyenthenguyen/docx"
	"github.com/otiai10/gosseract/v2"
)

func extractText(imagePath string, lang string) (string, error) {
	client := gosseract.NewClient()
	defer client.Close()
	client.SetLanguage(lang)
	client.SetImage(imagePath)
	text, err := client.Text()
	if err != nil {
		return "", err
	}
	return strings.TrimSpace(text), nil
}

func exportDocx(text, outputPath string) error {
	doc := docx.NewDocument()
	doc.AddHeading("OCR Extracted Text", 0)
	doc.AddParagraph(text)
	return doc.Save(outputPath)
}

func exportTxt(text, outputPath string) error {
	return os.WriteFile(outputPath, []byte(text), 0644)
}

func main() {
	var (
		images  = flag.String("images", "", "Comma-separated image files")
		output  = flag.String("output", "output.docx", "Output file")
		lang    = flag.String("lang", "eng", "OCR language")
		format  = flag.String("format", "docx", "Output format (docx|txt)")
	)
	flag.Parse()

	if *images == "" && len(flag.Args()) > 0 {
		*images = flag.Args()[0]
	}
	if *images == "" {
		fmt.Println("Usage: ocr_reader -images image1.png,image2.jpg [options]")
		os.Exit(1)
	}

	imageList := strings.Split(*images, ",")
	fmt.Println("📄 OCR Reader")
	fmt.Printf("Images: %s\n", *images)
	fmt.Printf("Language: %s\n", *lang)

	var allText []string
	for _, imgPath := range imageList {
		fmt.Printf("Processing: %s\n", imgPath)
		text, err := extractText(strings.TrimSpace(imgPath), *lang)
		if err != nil {
			fmt.Printf("⚠️ Error processing %s: %v\n", imgPath, err)
			continue
		}
		if text != "" {
			allText = append(allText, fmt.Sprintf("--- %s ---\n%s", imgPath, text))
		} else {
			fmt.Printf("⚠️ No text extracted from %s\n", imgPath)
		}
	}

	if len(allText) == 0 {
		fmt.Println("❌ No text extracted from any image.")
		os.Exit(1)
	}

	combinedText := strings.Join(allText, "\n")
	fmt.Println("\n📖 Extracted text:")
	if len(combinedText) > 500 {
		fmt.Println(combinedText[:500] + "...")
	} else {
		fmt.Println(combinedText)
	}

	var err error
	if *format == "docx" {
		err = exportDocx(combinedText, *output)
		fmt.Println("\n📄 Exporting to DOCX... ✅")
	} else {
		err = exportTxt(combinedText, *output)
		fmt.Println("\n📄 Exporting to TXT... ✅")
	}
	if err != nil {
		fmt.Printf("Error exporting: %v\n", err)
		os.Exit(1)
	}
	fmt.Printf("File saved: %s\n", *output)
}
