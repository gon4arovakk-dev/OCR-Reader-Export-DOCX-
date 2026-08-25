# ocr_reader.php
#!/usr/bin/env php
<?php

require_once 'vendor/autoload.php';

use thiagoalessio\TesseractOCR\TesseractOCR;
use PhpOffice\PhpWord\PhpWord;
use PhpOffice\PhpWord\IOFactory;

function extractText($imagePath, $lang) {
    try {
        $text = (new TesseractOCR($imagePath))
            ->lang($lang)
            ->run();
        return trim($text);
    } catch (Exception $e) {
        fwrite(STDERR, "Error processing $imagePath: " . $e->getMessage() . "\n");
        return null;
    }
}

function exportDocx($text, $outputPath) {
    $phpWord = new PhpWord();
    $section = $phpWord->addSection();
    $section->addTitle('OCR Extracted Text', 1);
    $section->addText($text);
    $writer = IOFactory::createWriter($phpWord, 'Word2007');
    $writer->save($outputPath);
    return $outputPath;
}

function exportTxt($text, $outputPath) {
    file_put_contents($outputPath, $text);
    return $outputPath;
}

$opts = getopt("", ["output:", "lang:", "format:"]);
$images = array_slice($argv, 1);
foreach ($images as $i => $img) {
    if (strpos($img, '--') === 0) {
        unset($images[$i]);
    }
}
$images = array_values($images);

if (empty($images)) {
    fwrite(STDERR, "Error: No images provided.\n");
    exit(1);
}

$output = $opts['output'] ?? 'output.docx';
$lang = $opts['lang'] ?? 'eng';
$format = $opts['format'] ?? 'docx';

echo "📄 OCR Reader\n";
echo "Images: " . implode(', ', $images) . "\n";
echo "Language: $lang\n";

$allText = [];
foreach ($images as $imgPath) {
    echo "Processing: $imgPath\n";
    $text = extractText($imgPath, $lang);
    if ($text) {
        $allText[] = "--- $imgPath ---\n$text";
    } else {
        echo "⚠️ No text extracted from $imgPath\n";
    }
}

if (empty($allText)) {
    fwrite(STDERR, "❌ No text extracted from any image.\n");
    exit(1);
}

$combinedText = implode("\n", $allText);
echo "\n📖 Extracted text:\n";
if (strlen($combinedText) > 500) {
    echo substr($combinedText, 0, 500) . "...\n";
} else {
    echo $combinedText . "\n";
}

if ($format === 'docx') {
    $outputPath = exportDocx($combinedText, $output);
    echo "\n📄 Exporting to DOCX... ✅\n";
} else {
    $outputPath = exportTxt($combinedText, $output);
    echo "\n📄 Exporting to TXT... ✅\n";
}
echo "File saved: $outputPath\n";
?>
