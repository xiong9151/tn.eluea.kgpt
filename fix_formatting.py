import os
import re

base_path = r'c:\Users\Classic\Downloads\KGPT-main (2)\KGPT-main\app\src\main\res'

def fix_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Regex to find the tag. 
    # Group 1: <string 
    # Group 2: attributes (name="...")
    # Group 3: >content</string>
    pattern = r'(<string\s+)(name="pattern_desc_custom_command"[^>]*)>(.*?)</string>'
    
    def replace(match):
        prefix = match.group(1)
        attrs = match.group(2)
        text = match.group(3)
        
        if 'formatted="false"' in attrs:
            return match.group(0)
        
        return f'{prefix}{attrs} formatted="false">{text}</string>'

    new_content = re.sub(pattern, replace, content)
    
    if new_content != content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Fixed {file_path}")

for root, dirs, files in os.walk(base_path):
    for file in files:
        if file == 'strings.xml':
            fix_file(os.path.join(root, file))
