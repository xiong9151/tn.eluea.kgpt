import os
import xml.etree.ElementTree as ET
import re

def get_keys_from_file(file_path):
    keys = {}
    if not os.path.exists(file_path):
        return keys
    
    try:
        # Read file manually to handle encoding issues and namespaces if any
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # Regex is often more robust for simple XML line parsing if the XML is malformed or has entities
        # But let's try ElementTree first for correctness, fallback to regex if needed?
        # Actually proper XML parsing is better.
        try:
            tree = ET.parse(file_path)
            root = tree.getroot()
            for child in root:
                if child.tag == 'string':
                    name = child.attrib.get('name')
                    if name:
                        keys[name] = child.text
        except ET.ParseError:
            print(f"Warning: Parse error in {file_path}, falling back to regex")
            # Fallback regex
            matches = re.findall(r'<string name="([^"]+)">', content)
            for m in matches:
                keys[m] = "Content" # content doesn't matter for key checking
                
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        
    return keys

def main():
    base_dir = "app/src/main/res"
    default_path = os.path.join(base_dir, "values", "strings.xml")
    
    print(f"Reading default strings from {default_path}...")
    default_keys = get_keys_from_file(default_path)
    print(f"Found {len(default_keys)} keys in default file.")
    
    languages = [d for d in os.listdir(base_dir) if d.startswith("values-") and d != "values-night"]
    
    missing_report = {}
    
    for lang_dir in languages:
        lang_path = os.path.join(base_dir, lang_dir, "strings.xml")
        lang_keys = get_keys_from_file(lang_path)
        
        missing = [k for k in default_keys if k not in lang_keys]
        
        if missing:
            missing_report[lang_dir] = missing
            print(f"Language '{lang_dir}' is missing {len(missing)} keys.")
        else:
            print(f"Language '{lang_dir}' is complete.")

    if not missing_report:
        print("\nAll languages are complete!")
        return

    # Check if we should generate patches (I'll just print the instructions for myself mostly)
    print("\n--- Summary of missing keys ---")
    for lang, keys in missing_report.items():
        print(f"{lang}: {len(keys)} missing keys")
        # print(f"First 5: {keys[:5]}")

if __name__ == "__main__":
    main()
