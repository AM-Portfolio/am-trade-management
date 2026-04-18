import requests
import json
import os

# --- CONFIGURATION ---
VAULT_ADDR = "http://vault-local.munish.org"
VAULT_TOKEN = os.environ.get("VAULT_TOKEN") # Set this in your terminal or .env
SECRET_PATH = "local/app/am-trade-management"
ENGINE_NAME = "secret"

def create_vault_secrets():
    """
    Creates the app folder and service secrets safely.
    This action only affects the specific path defined and will NOT delete 
    or modify any other folders (like /infra).
    """
    
    # Vault KV-V2 API uses /data/ in the path for secret management
    url = f"{VAULT_ADDR}/v1/{ENGINE_NAME}/data/{SECRET_PATH}"
    
    headers = {
        "X-Vault-Token": VAULT_TOKEN,
        "Content-Type": "application/json"
    }
    
    # Define the secrets you want to initialize
    payload = {
        "data": {
            "MONGODB_URI": "mongodb://admin:SZvvtJfHlokkcZW7XHcj@100.83.141.23:27017/am-trade",
            "KAFKA_BOOTSTRAP_SERVERS": "localhost:9092",
            "REDIS_HOST": "localhost",
            "PORT": "8080"
        }
    }

    try:
        print(f"🚀 Connecting to Vault at {VAULT_ADDR}...")
        response = requests.post(url, headers=headers, data=json.dumps(payload))
        
        if response.status_code in [200, 204]:
            print(f"✅ Success! Created path: {ENGINE_NAME}/{SECRET_PATH}")
            print(f"⚠️  Note: Existing folders like 'infra/' were NOT touched.")
        else:
            print(f"❌ Error: {response.status_code}")
            print(response.text)
            
    except Exception as e:
        print(f"💥 Failed to connect: {str(e)}")

if __name__ == "__main__":
    if VAULT_TOKEN == "PASTE_YOUR_TOKEN_HERE":
        print("🛑 Please edit the script and paste your Vault Token first!")
    else:
        create_vault_secrets()
