# ocr_reader.rb
#!/usr/bin/env ruby
require 'optparse'
require 'rtesseract'
require 'docx'

def extract_text(image_path, lang)
  RTesseract.new(image_path, lang: lang).to_s.strip
rescue => e
  warn "Error processing #{image_path}: #{e.message}"
  nil
end

def export_docx(text, output_path)
  doc = Docx::Document.new
  doc.title = 'OCR Extracted Text'
  doc.paragraphs << text
  doc.save(output_path)
  output_path
end

def export_txt(text, output_path)
  File.write(output_path, text)
  output_path
end

options = {}
OptionParser.new do |opts|
  opts.banner = "Usage: ocr_reader.rb [options] <image1> <image2> ..."
  opts.on("--output FILE", "Output file (default: output.docx)") { |v| options[:output] = v }
  opts.on("--lang LANG", "OCR language (default: eng)") { |v| options[:lang] = v }
  opts.on("--format FORMAT", "Output format (docx|txt)", %w[docx txt]) { |v| options[:format] = v }
end.parse!

images = ARGV
if images.empty?
  warn "Error: No images provided."
  exit 1
end

options[:output] ||= 'output.docx'
options[:lang] ||= 'eng'
options[:format] ||= 'docx'

puts "📄 OCR Reader"
puts "Images: #{images.join(', ')}"
puts "Language: #{options[:lang]}"

all_text = []
images.each do |img_path|
  puts "Processing: #{img_path}"
  text = extract_text(img_path, options[:lang])
  if text
    all_text << "--- #{img_path} ---\n#{text}"
  else
    puts "⚠️ No text extracted from #{img_path}"
  end
end

if all_text.empty?
  warn "❌ No text extracted from any image."
  exit 1
end

combined_text = all_text.join("\n")
puts "\n📖 Extracted text:"
if combined_text.length > 500
  puts combined_text[0, 500] + '...'
else
  puts combined_text
end

if options[:format] == 'docx'
  output_path = export_docx(combined_text, options[:output])
  puts "\n📄 Exporting to DOCX... ✅"
else
  output_path = export_txt(combined_text, options[:output])
  puts "\n📄 Exporting to TXT... ✅"
end
puts "File saved: #{output_path}"
