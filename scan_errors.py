import os
import re

base_path = r'c:\Users\Classic\Downloads\KGPT-main (2)\KGPT-main\app\src\main\res'

def check_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    for i, line in enumerate(lines):
        if '<string' in line:
            # simple check for unescaped %
            # Find all % that are NOT %% and NOT followed by valid specifier
            # Valid specifiers approx: \d+\$[sd]|s|d
            
            # Simple heuristic: if line has %, print it for manual review
            if '%' in line and 'formatted="false"' not in line:
                matches = re.finditer(r'%', line)
                valid = True
                for m in matches:
                    # check what follows %
                    rest = line[m.end():]
                    if rest.startswith('%'): continue # %% is escaped
                    if re.match(r'^(\d+\$)?(s|d|f|c|b|x)', rest): continue
                    valid = False
                    break
                
                if not valid:
                    print(f"{file_path}:{i+1}: {line.strip()}")

for root, dirs, files in os.walk(base_path):
    for file in files:
        if file == 'strings.xml':
            check_file(os.path.join(root, file))
