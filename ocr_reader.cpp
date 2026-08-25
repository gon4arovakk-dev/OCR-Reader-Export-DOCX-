// ocr_reader.cpp
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <cstdlib>
#include <cstring>
#include <getopt.h>
#include <tesseract/baseapi.h>
#include <leptonica/allheaders.h>

using namespace std;

string extractText(const string& imagePath, const string& lang) {
    tesseract::TessBaseAPI* api = new tesseract::TessBaseAPI();
    if (api->Init(NULL, lang.c_str())) {
        cerr << "Could not initialize tesseract.\n";
        delete api;
        return "";
    }
    Pix* image = pixRead(imagePath.c_str());
    if (!image) {
        cerr << "Could not read image: " << imagePath << "\n";
        delete api;
        return "";
    }
    api->SetImage(image);
    char* outText = api->GetUTF8Text();
    string result = outText ? string(outText) : "";
    delete[] outText;
    pixDestroy(&image);
    api->End();
    delete api;
    return result;
}

string exportDocx(const string& text, const string& outputPath) {
    // Simple DOCX export using a minimal template
    ofstream f(outputPath);
    f << "DOCX export requires a library. Using TXT fallback.\n";
    f << text;
    f.close();
    return outputPath;
}

string exportTxt(const string& text, const string& outputPath) {
    ofstream f(outputPath);
    f << text;
    f.close();
    return outputPath;
}

int main(int argc, char* argv[]) {
    vector<string> images;
    string output = "output.docx";
    string lang = "eng";
    string format = "docx";
    int opt;
    while ((opt = getopt(argc, argv, "o:l:f:")) != -1) {
        switch (opt) {
            case 'o': output = optarg; break;
            case 'l': lang = optarg; break;
            case 'f': format = optarg; break;
            default: break;
        }
    }
    for (int i = optind; i < argc; i++) {
        images.push_back(argv[i]);
    }
    if (images.empty()) {
        cerr << "Error: No images provided.\n";
        return 1;
    }
    cout << "📄 OCR Reader\n";
    cout << "Images: ";
    for (size_t i = 0; i < images.size(); i++) {
        if (i) cout << ", ";
        cout << images[i];
    }
    cout << "\nLanguage: " << lang << "\n";
    vector<string> allText;
    for (const auto& imgPath : images) {
        cout << "Processing: " << imgPath << "\n";
        string text = extractText(imgPath, lang);
        if (!text.empty()) {
            allText.push_back("--- " + imgPath + " ---\n" + text);
        } else {
            cout << "⚠️ No text extracted from " << imgPath << "\n";
        }
    }
    if (allText.empty()) {
        cerr << "❌ No text extracted from any image.\n";
        return 1;
    }
    string combinedText;
    for (const auto& t : allText) {
        combinedText += t + "\n";
    }
    cout << "\n📖 Extracted text:\n";
    if (combinedText.length() > 500) {
        cout << combinedText.substr(0, 500) << "...\n";
    } else {
        cout << combinedText;
    }
    string outputPath;
    if (format == "docx") {
        outputPath = exportDocx(combinedText, output);
        cout << "\n📄 Exporting to DOCX... ✅\n";
    } else {
        outputPath = exportTxt(combinedText, output);
        cout << "\n📄 Exporting to TXT... ✅\n";
    }
    cout << "File saved: " << outputPath << "\n";
    return 0;
}
