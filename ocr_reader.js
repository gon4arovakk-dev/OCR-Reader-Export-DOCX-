// ocr_reader.js
#!/usr/bin/env node
const fs = require('fs');
const path = require('path');
const { program } = require('commander');
const Tesseract = require('tesseract.js');
const { Document, Packer, Paragraph, TextRun, HeadingLevel } = require('docx');

async function extractText(imagePath, lang) {
    try {
        const result = await Tesseract.recognize(imagePath, lang);
        return result.data.text.trim();
    } catch (err) {
        console.error(`Error processing ${imagePath}: ${err.message}`);
        return null;
    }
}

function exportDocx(text, outputPath) {
    const doc = new Document({
        sections: [{
            properties: {},
            children: [
                new Paragraph({
                    text: "OCR Extracted Text",
                    heading: HeadingLevel.HEADING_1,
                }),
                new Paragraph({
                    children: [
                        new TextRun({ text: text }),
                    ],
                }),
            ],
        }],
    });
    const buffer = Packer.toBuffer(doc);
    fs.writeFileSync(outputPath, buffer);
    return outputPath;
}

function exportTxt(text, outputPath) {
    fs.writeFileSync(outputPath, text, 'utf8');
    return outputPath;
}

async function main() {
    program
        .argument('<images...>', 'Image files to process')
        .option('--output <file>', 'Output file', 'output.docx')
        .option('--lang <lang>', 'OCR language', 'eng')
        .option('--format <type>', 'Output format (docx|txt)', 'docx')
        .parse(process.argv);

    const opts = program.opts();
    const images = program.args;

    console.log('📄 OCR Reader');
    console.log(`Images: ${images.join(', ')}`);
    console.log(`Language: ${opts.lang}`);

    const allText = [];
    for (const imgPath of images) {
        console.log(`Processing: ${imgPath}`);
        const text = await extractText(imgPath, opts.lang);
        if (text) {
            allText.push(`--- ${imgPath} ---\n${text}`);
        } else {
            console.log(`⚠️ No text extracted from ${imgPath}`);
        }
    }

    if (allText.length === 0) {
        console.error('❌ No text extracted from any image.');
        process.exit(1);
    }

    const combinedText = allText.join('\n');
    console.log('\n📖 Extracted text:');
    if (combinedText.length > 500) {
        console.log(combinedText.slice(0, 500) + '...');
    } else {
        console.log(combinedText);
    }

    let outputPath;
    if (opts.format === 'docx') {
        outputPath = exportDocx(combinedText, opts.output);
        console.log('\n📄 Exporting to DOCX... ✅');
    } else {
        outputPath = exportTxt(combinedText, opts.output);
        console.log('\n📄 Exporting to TXT... ✅');
    }
    console.log(`File saved: ${outputPath}`);
}

main().catch(console.error);
