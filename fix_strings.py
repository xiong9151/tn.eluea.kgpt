import os
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape

def get_keys_and_values(file_path):
    keys = {}
    if not os.path.exists(file_path):
        return keys
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        for child in root:
            # We want to preserve specific tags, but mostly strings and string-arrays
            # For simplicity in this patch script, we focus on <string> tags as they are the most common source of crashes
            if child.tag == 'string':
                name = child.attrib.get('name')
                if name:
                    # We store the raw element to reconstruct if needed, or just text?
                    # Storing text is safer for simple appending.
                    keys[name] = child.text
    except Exception as e:
        print(f"Error parsing {file_path}: {e}")
    return keys

def append_missing_keys(lang_path, default_data, missing_keys):
    with open(lang_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the closing </resources> tag
    end_tag_pos = content.rfind('</resources>')
    if end_tag_pos == -1:
        print(f"Error: Could not find </resources> in {lang_path}")
        return

    new_content_chunk = "\n    <!-- Missing Translations (English Defaults) -->\n"
    for key in missing_keys:
        val = default_data[key]
        if val is None: val = ""
        # Escape simplified for XML
        # Note: Android strings.xml often have specific escaping (like \', \n). 
        # Since we are reading from XML and writing to text, we need to be careful.
        # However, ET.parse .text property usually unescapes some entities. 
        # But for ' and " it might not be perfect to just dump it back without check.
        # A safer bet: read the "raw" line from default file? No, that's hard to match.
        # Let's just do a best effort escape.
        
        # Actually, if we use the value from ElementTree, it's the inner text.
        # We need to wrap it in <string name="...">...</string>
        # And escape & < >. And ' needs to be \' if appropriate etc.
        
        # Better approach: Read the RAW content of specific keys from the default file using Regex to preserve formatting/escaping.
        pass

    # New plan: Use regex to extract the exact line(s) from default file for the missing keys.
    pass

def load_raw_map(file_path):
    # Map key -> raw_xml_string
    # e.g. "app_name" -> '<string name="app_name">KGPT</string>'
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    mapping = {}
    import re
    # Match <string name="KEY">VALUE</string>
    # or <string name="KEY" ...>VALUE</string>
    # This regex handles single line well. Multiline might be tricky but let's try dotall
    pattern = re.compile(r'(<string\s+name="([^"]+)"[^>]*>.*?</string>)', re.DOTALL)
    for match in pattern.finditer(content):
        full_tag = match.group(1)
        key = match.group(2)
        mapping[key] = full_tag
        
    return mapping

def main():
    base_dir = "app/src/main/res"
    default_path = os.path.join(base_dir, "values", "strings.xml")
    
    print("Loading default keys...")
    default_map = load_raw_map(default_path)
    default_keys = set(default_map.keys())
    
    languages = [d for d in os.listdir(base_dir) if d.startswith("values-") and d != "values-night"]
    
    for lang_dir in languages:
        lang_path = os.path.join(base_dir, lang_dir, "strings.xml")
        lang_map = load_raw_map(lang_path)
        lang_keys = set(lang_map.keys())
        
        missing = [k for k in default_keys if k not in lang_keys]
        
        if missing:
            print(f"Fixing {lang_dir}: Adding {len(missing)} keys...")
            
            with open(lang_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Remove the closing tag
            if '</resources>' in content:
                content = content.replace('</resources>', '')
            
            content += "\n    <!-- Auto-added Missing Keys -->\n"
            for k in missing:
                if k in default_map:
                    content += "    " + default_map[k] + "\n"
            
            content += "</resources>"
            
            with open(lang_path, 'w', encoding='utf-8') as f:
                f.write(content)
        else:
            print(f"{lang_dir} is up to date.")

if __name__ == "__main__":
    main()
