// OcrReader.cs
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Tesseract;
using Novacode;

class OcrReader
{
    static string ExtractText(string imagePath, string lang)
    {
        try
        {
            using (var engine = new TesseractEngine("./tessdata", lang, EngineMode.Default))
            {
                using (var img = Pix.LoadFromFile(imagePath))
                {
                    using (var page = engine.Process(img))
                    {
                        return page.GetText().Trim();
                    }
                }
            }
        }
        catch (Exception e)
        {
            Console.Error.WriteLine($"Error processing {imagePath}: {e.Message}");
            return null;
        }
    }

    static string ExportDocx(string text, string outputPath)
    {
        using (var doc = DocX.Create(outputPath))
        {
            doc.InsertParagraph("OCR Extracted Text").FontSize(18).Bold();
            doc.InsertParagraph(text);
            doc.Save();
        }
        return outputPath;
    }

    static string ExportTxt(string text, string outputPath)
    {
        File.WriteAllText(outputPath, text);
        return outputPath;
    }

    static void Main(string[] args)
    {
        if (args.Length < 1)
        {
            Console.WriteLine("Usage: OcrReader <image1> <image2> ... [--output FILE] [--lang LANG] [--format FORMAT]");
            return;
        }
        var images = new List<string>();
        string output = "output.docx";
        string lang = "eng";
        string format = "docx";
        for (int i = 0; i < args.Length; i++)
        {
            if (args[i] == "--output" && i + 1 < args.Length)
                output = args[++i];
            else if (args[i] == "--lang" && i + 1 < args.Length)
                lang = args[++i];
            else if (args[i] == "--format" && i + 1 < args.Length)
                format = args[++i];
            else
                images.Add(args[i]);
        }
        if (images.Count == 0)
        {
            Console.Error.WriteLine("Error: No images provided.");
            Environment.Exit(1);
        }
        Console.WriteLine("📄 OCR Reader");
        Console.WriteLine($"Images: {string.Join(", ", images)}");
        Console.WriteLine($"Language: {lang}");
        var allText = new List<string>();
        foreach (var imgPath in images)
        {
            Console.WriteLine($"Processing: {imgPath}");
            var text = ExtractText(imgPath, lang);
            if (!string.IsNullOrEmpty(text))
            {
                allText.Add($"--- {imgPath} ---\n{text}");
            }
            else
            {
                Console.WriteLine($"⚠️ No text extracted from {imgPath}");
            }
        }
        if (allText.Count == 0)
        {
            Console.Error.WriteLine("❌ No text extracted from any image.");
            Environment.Exit(1);
        }
        var combinedText = string.Join("\n", allText);
        Console.WriteLine("\n📖 Extracted text:");
        if (combinedText.Length > 500)
            Console.WriteLine(combinedText.Substring(0, 500) + "...");
        else
            Console.WriteLine(combinedText);
        string outputPath;
        if (format == "docx")
        {
            outputPath = ExportDocx(combinedText, output);
            Console.WriteLine("\n📄 Exporting to DOCX... ✅");
        }
        else
        {
            outputPath = ExportTxt(combinedText, output);
            Console.WriteLine("\n📄 Exporting to TXT... ✅");
        }
        Console.WriteLine($"File saved: {outputPath}");
    }
}
