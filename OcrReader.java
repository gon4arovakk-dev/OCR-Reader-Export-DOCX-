// OcrReader.java
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class OcrReader {
    public static String extractText(String imagePath, String lang) {
        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage(lang);
        try {
            String text = tesseract.doOCR(new File(imagePath));
            return text.trim();
        } catch (TesseractException e) {
            System.err.println("Error processing " + imagePath + ": " + e.getMessage());
            return null;
        }
    }

    public static String exportDocx(String text, String outputPath) throws Exception {
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
        org.docx4j.wml.Document wmlDocument = wordPackage.getMainDocumentPart().getJaxbElement();
        P heading = new P();
        R runHeading = new R();
        Text tHeading = new Text();
        tHeading.setValue("OCR Extracted Text");
        runHeading.getContent().add(tHeading);
        heading.getContent().add(runHeading);
        wordPackage.getMainDocumentPart().addObject(heading);
        P paragraph = new P();
        R run = new R();
        Text t = new Text();
        t.setValue(text);
        run.getContent().add(t);
        paragraph.getContent().add(run);
        wordPackage.getMainDocumentPart().addObject(paragraph);
        wordPackage.save(new File(outputPath));
        return outputPath;
    }

    public static String exportTxt(String text, String outputPath) throws IOException {
        Files.write(Paths.get(outputPath), text.getBytes());
        return outputPath;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: OcrReader <image1> <image2> ... [--output FILE] [--lang LANG] [--format FORMAT]");
            return;
        }
        List<String> images = new ArrayList<>();
        String output = "output.docx";
        String lang = "eng";
        String format = "docx";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--output") && i + 1 < args.length) {
                output = args[++i];
            } else if (args[i].equals("--lang") && i + 1 < args.length) {
                lang = args[++i];
            } else if (args[i].equals("--format") && i + 1 < args.length) {
                format = args[++i];
            } else {
                images.add(args[i]);
            }
        }
        if (images.isEmpty()) {
            System.err.println("Error: No images provided.");
            System.exit(1);
        }
        System.out.println("📄 OCR Reader");
        System.out.println("Images: " + String.join(", ", images));
        System.out.println("Language: " + lang);
        List<String> allText = new ArrayList<>();
        for (String imgPath : images) {
            System.out.println("Processing: " + imgPath);
            String text = extractText(imgPath, lang);
            if (text != null && !text.isEmpty()) {
                allText.add("--- " + imgPath + " ---\n" + text);
            } else {
                System.out.println("⚠️ No text extracted from " + imgPath);
            }
        }
        if (allText.isEmpty()) {
            System.err.println("❌ No text extracted from any image.");
            System.exit(1);
        }
        String combinedText = String.join("\n", allText);
        System.out.println("\n📖 Extracted text:");
        if (combinedText.length() > 500) {
            System.out.println(combinedText.substring(0, 500) + "...");
        } else {
            System.out.println(combinedText);
        }
        String outputPath;
        if (format.equals("docx")) {
            outputPath = exportDocx(combinedText, output);
            System.out.println("\n📄 Exporting to DOCX... ✅");
        } else {
            outputPath = exportTxt(combinedText, output);
            System.out.println("\n📄 Exporting to TXT... ✅");
        }
        System.out.println("File saved: " + outputPath);
    }
}
