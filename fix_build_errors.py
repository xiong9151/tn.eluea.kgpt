import os
import re

base_path = r'c:\Users\Classic\Downloads\KGPT-main (2)\KGPT-main\app\src\main\res'

def fix_italian(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # specific fix for Italian
    content = content.replace("dall'IA", r"dall\'IA")
    content = content.replace("l'IA", r"l\'IA") # generic fix
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Fixed Italian apostrophes")

def fix_unknown_model(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Fix %1 / %2 to %1$s / %2$s in dialog_msg_unknown_model
    # This regex looks for the string and replaces %1 with %1$s if mostly correct
    if 'dialog_msg_unknown_model' in content:
        # We can't easily parse XML with regex safely, but for this specific string we can be aggressive
        # Pattern: <string name="dialog_msg_unknown_model">...%1...</string>
        
        # We will iterate lines to be safer
        lines = content.split('\n')
        new_lines = []
        changed = False
        for line in lines:
            if 'name="dialog_msg_unknown_model"' in line:
                # Replace %1 with %1$s if not already %1$s
                # Check for %1 not followed by $
                if re.search(r'%1[^$]', line): 
                    line = line.replace('%1', '%1$s')
                    changed = True
                if '%2' in line and not '%2$s' in line:
                     # careful not to replace %2$s again if we run multiple times, but checks handle it
                     if re.search(r'%2[^$]', line) or line.endswith('%2</string>'):
                        line = line.replace('%2', '%2$s')
                        changed = True
            
            new_lines.append(line)
        
        if changed:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write('\n'.join(new_lines))
            print(f"Fixed unknown_model in {file_path}")

fix_italian(os.path.join(base_path, 'values-it', 'strings.xml'))
fix_italian(os.path.join(base_path, 'values-fr', 'strings.xml')) # French might have L'IA

for root, dirs, files in os.walk(base_path):
    for file in files:
        if file == 'strings.xml':
            fix_unknown_model(os.path.join(root, file))
