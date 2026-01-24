import os

base_path = r'c:\Users\Classic\Downloads\KGPT-main (2)\KGPT-main\app\src\main\res'

def fix_apostrophes(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = content.replace("L'IA", r"L\'IA")
    
    if new_content != content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Fixed apostrophes in {file_path}")

fix_apostrophes(os.path.join(base_path, 'values-it', 'strings.xml'))
fix_apostrophes(os.path.join(base_path, 'values-fr', 'strings.xml'))
