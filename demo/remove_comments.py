import re
import os
from pathlib import Path

# Directories to process
base_dir = r"c:\Users\Maxi G\Documents\GitHub\TrimminFlow-Backend\demo\src\main\java\com\trimminflow\demo"
dirs_to_process = ["service", "repository"]

for dir_name in dirs_to_process:
    dir_path = os.path.join(base_dir, dir_name)
    
    for java_file in Path(dir_path).glob("*.java"):
        with open(java_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Remove class-level single-line comments
        content = re.sub(r'^//\s*(barber management service|service management service|appointment service|auth service|business hours service|barbershop service|user service|service layer for appointment management)\s*\n', '', content, flags=re.MULTILINE)
        
        # Remove Javadoc comments (/** ... */)
        content = re.sub(r'/\*\*\s*\n\s*\*[^\n]*\n\s*\*\s*\n\s*\*[^\n]*\n\s*\*/', '', content)
        content = re.sub(r'/\*\*\s*\n\s*\*[^\n]*\n\s*\*/', '', content)
        content = re.sub(r'/\*\*\s*\n\s*\*[^\n]*\n\s*\*/\s*\n', '', content)
        
        with open(java_file, 'w', encoding='utf-8') as f:
            f.write(content)

print("Comments removed successfully")
