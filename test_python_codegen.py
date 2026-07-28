import subprocess
import sys
import time
import urllib.request

def main():
    # 1. Fetch schema
    url = "http://localhost:8080/api-docs"
    max_retries = 20
    schema_fetched = False
    print("Waiting for Spring Boot to be ready...")
    for i in range(max_retries):
        try:
            req = urllib.request.urlopen(url, timeout=5)
            with open("schema.json", "wb") as f:
                f.write(req.read())
            print(f"Successfully downloaded schema from {url}")
            schema_fetched = True
            break
        except Exception as e:
            print(f"Attempt {i+1} failed: {e}")
            time.sleep(5)
            
    if not schema_fetched:
        print("Failed to fetch schema.")
        sys.exit(1)
        
    # 2. Run datamodel-codegen
    print("Running datamodel-codegen...")
    result = subprocess.run([
        sys.executable, "-m", "datamodel_code_generator",
        "--input", "schema.json",
        "--input-file-type", "openapi",
        "--output", "generated_models.py"
    ], capture_output=True, text=True)
    
    if result.returncode != 0:
        print("Codegen failed!")
        print(result.stderr)
        sys.exit(1)
        
    print("Codegen successful. Here is an excerpt of generated_models.py:\n")
    
    with open("generated_models.py", "r", encoding="utf-8") as f:
        content = f.read()
        
    # Extract JournalTemplateCategory
    if "class JournalTemplateCategory" in content:
        idx = content.find("class JournalTemplateCategory")
        end_idx = content.find("class ", idx + 10)
        if end_idx == -1:
            end_idx = len(content)
        print("--- GENERATED PYTHON ENUM ---")
        print(content[idx:end_idx].strip())
        print("-----------------------------\n")
        print("Test passed! Python library perfectly understood the schema.")
    else:
        print("Enum not found in generated models.")
        
if __name__ == "__main__":
    main()
