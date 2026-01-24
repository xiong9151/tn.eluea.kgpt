import os
import re

def remove_duplicates_in_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    unique_keys = set()
    cleaned_lines = []
    
    # Simple regex to detect <string name="KEY">...
    # This assumes standard formatting where the tag starts on the line.
    # It might miss multi-line tags if the "name=" part is split, but that's rare in this codebase.
    key_pattern = re.compile(r'<string\s+name="([^"]+)"')

    for line in lines:
        match = key_pattern.search(line)
        if match:
            key = match.group(1)
            if key in unique_keys:
                print(f"Removing duplicate {key} in {os.path.basename(file_path)}")
                continue # Skip this line
            unique_keys.add(key)
        
        # Also check for end of file garbage if any
        if line.strip() == "</resources>" and any(l.strip() == "</resources>" for l in cleaned_lines):
             continue # Avoid double closing tags if my previous script added one

        cleaned_lines.append(line)

    # Ensure </resources> is at the end and only one
    # My previous script might have left a </resources> in the middle if I did string replacement poorly.
    # Let's verify the content structure.
    
    # Re-assemble
    content = "".join(cleaned_lines)
    
    # Fix potential double </resources> issue if my previous extraction logic was flawed
    # (My previous script did: content.replace('</resources>', '') then appended. 
    # If regex failed to match key, it might have added it again.
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

def main():
    base_dir = "app/src/main/res"
    languages = [d for d in os.listdir(base_dir) if d.startswith("values-")]
    
    for lang in languages:
        path = os.path.join(base_dir, lang, "strings.xml")
        if os.path.exists(path):
            print(f"Processing {lang}...")
            remove_duplicates_in_file(path)

if __name__ == "__main__":
    main()
