import os

file_path = r'c:\Users\Classic\Downloads\KGPT-main (2)\KGPT-main\app\src\main\res\values-it\strings.xml'

def fix_it_quotes():
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = content.replace(r"\'", r"\u0027")
    
    # Also fix potential bare apostrophes if any left (except inside tags)
    # This is risky with regex on XML but let's just do the known ones
    new_content = new_content.replace(r"'", r"\u0027")
    
    # Fix double escaping if we created it: \u0027 is safe.
    # Note: replacing "'" with "\u0027" might affect XML attributes like name='...' if single quotes used.
    # But strings.xml usually uses double quotes for attributes: name="...".
    # Check if there are single quoted attributes
    if "='" in content or "='" in content:
        print("Warning: Single quoted attributes detected, skipping unsafe replace")
        return

    if new_content != content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Fixed quotes in {file_path}")

fix_it_quotes()
