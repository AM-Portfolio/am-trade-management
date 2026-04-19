import requests
import os
import socket
import subprocess
import json

# --- LOAD ENVIRONMENT VARIABLES ---
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    if os.path.exists(".env"):
        with open(".env") as f:
            for line in f:
                if line.strip() and not line.startswith("#"):
                    key, value = line.strip().split("=", 1)
                    os.environ[key] = value

# --- CONFIGURATION (Tries to load from Environment first) ---
VAULT_ADDR = "http://vault.munish.org"
VAULT_TOKEN = os.environ.get("VAULT_TOKEN")

MONGODB_URI = os.environ.get("MONGODB_URI")

GITHUB_PAT = os.environ.get("GITHUB_PACKAGES_TOKEN")

INFRA_POSTS = {
    "Kafka": ("100.83.141.23", 9092),
    "Redis": ("100.83.141.23", 6379),
    "Vault UI": ("vault.munish.org", 80)
}

def check_vault():
    print("🔐 [Vault]   Checking token validity...")
    url = f"{VAULT_ADDR}/v1/auth/token/lookup-self"
    headers = {"X-Vault-Token": VAULT_TOKEN}
    try:
        r = requests.get(url, headers=headers, timeout=5)
        if r.status_code == 200:
            user = r.json()['data']['display_name']
            print(f"   ✅ SUCCESS: Logged in as {user}")
        else:
            print(f"   ❌ FAILED: Status code {r.status_code}")
    except Exception as e:
        print(f"   ❌ ERROR: Could not connect to Vault - {str(e)}")

def check_github():
    if not GITHUB_PAT:
        print("🐙 [GitHub]  SKIPPED: No Token found in .env")
        return
    print("🐙 [GitHub]  Verifying Personal Access Token...")
    url = "https://api.github.com/user"
    headers = {"Authorization": f"Bearer {GITHUB_PAT}"}
    try:
        r = requests.get(url, headers=headers, timeout=5)
        if r.status_code == 200:
            name = r.json().get('login', 'Unknown')
            print(f"   ✅ SUCCESS: Token valid for user '{name}'")
        else:
            print(f"   ❌ FAILED: {r.status_code} - {r.json().get('message')}")
    except Exception as e:
        print(f"   ❌ ERROR: Could not connect to GitHub - {str(e)}")

def check_mongodb():
    print("🍃 [MongoDB] Checking connection and pinging cluster...")
    # Use mongosh subprocess to avoid needing pymongo library
    cmd = f'mongosh "{MONGODB_URI}" --eval "db.adminCommand(\'ping\')" --quiet'
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, shell=True)
        if "ok: 1" in result.stdout:
            print("   ✅ SUCCESS: Cluster is reachable and responding to ping.")
        else:
            print(f"   ❌ FAILED: {result.stderr or result.stdout}")
    except Exception as e:
        print(f"   ❌ ERROR: mongosh execution failed - {str(e)}")

def check_network():
    for name, (host, port) in INFRA_POSTS.items():
        print(f"🌐 [Network] Checking {name} ({host}:{port})...")
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.settimeout(3)
        try:
            s.connect((host, port))
            print(f"   ✅ SUCCESS: Port {port} is OPEN.")
            s.close()
        except Exception:
            print(f"   ❌ FAILED: Port {port} is CLOSED or unreachable.")

if __name__ == "__main__":
    print("\n🚀 Starting Credential Cross-Check (READ-ONLY)\n" + "="*50)
    check_vault()
    check_github()
    check_mongodb()
    check_network()
    print("="*50 + "\n✅ Audit Complete.\n")
