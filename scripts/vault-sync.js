const fs = require('fs');
const path = require('path');
const net = require('net');

/**
 * Vault Sync Utility - Aligned with am-portfolio standards
 * This script aggregates secrets from multiple Vault paths into a local .env.
 */

// Configuration
const ENV_PATH = path.join(__dirname, '..', '.env');

// Vault Server Details
let VAULT_URI = process.env.VAULT_URI || 'http://vault.munish.org';
let VAULT_TOKEN = process.env.VAULT_TOKEN;

// The standard multi-path scanning strategy
const VAULT_SUBPATHS = [
    'local/apps/api',
    'local/apps/config',
    'local/apps/database',
    'local/apps/paas',
    'local/apps/security',
    'local/app/am-trade-management' // Service-specific path
];

async function loadEnv() {
    if (!fs.existsSync(ENV_PATH)) return {};
    const content = fs.readFileSync(ENV_PATH, 'utf8');
    const env = {};
    content.split('\n').forEach(line => {
        const match = line.match(/^\s*([\w.-]+)\s*=\s*(.*)?\s*$/);
        if (match) {
            env[match[1]] = match[2] ? match[2].trim() : '';
        }
    });
    return env;
}

async function fetchFromVaultPath(subpath) {
    const fullPath = `v1/secret/data/${subpath}`;
    console.log(`[VAULT] Checking ${subpath}...`);
    try {
        const response = await fetch(`${VAULT_URI}/${fullPath}`, {
            headers: { 'X-Vault-Token': VAULT_TOKEN }
        });
        if (!response.ok) {
            if (response.status === 404) return {};
            console.warn(`[WARN] Vault access failed for ${subpath} (Status ${response.status})`);
            return {};
        }
        const json = await response.json();
        const data = json.data.data || {};
        const count = Object.keys(data).length;
        if (count > 0) console.log(`[OK] Found ${count} keys in ${subpath}`);
        return data;
    } catch (error) {
        console.warn(`[WARN] Vault connection failed for ${subpath}: ${error.message}`);
        return {};
    }
}

async function checkConnectivity(host, port, name) {
    return new Promise((resolve) => {
        const socket = new net.Socket();
        socket.setTimeout(3000);
        socket.on('connect', () => {
            console.log(`[PASS] ${name} reachable at ${host}:${port}`);
            socket.destroy();
            resolve(true);
        });
        socket.on('timeout', () => {
            console.error(`[FAIL] ${name} connection timed out at ${host}:${port}`);
            socket.destroy();
            resolve(false);
        });
        socket.on('error', (err) => {
            console.error(`[FAIL] ${name} connection failed at ${host}:${port}: ${err.message}`);
            socket.destroy();
            resolve(false);
        });
        socket.connect(port, host);
    });
}

async function sync() {
    // 1. Resolve Credentials
    const existingEnv = await loadEnv();
    VAULT_TOKEN = VAULT_TOKEN || existingEnv.VAULT_TOKEN;

    if (!VAULT_TOKEN) {
        console.error('[ERROR] VAULT_TOKEN not found in environment or .env file!');
        process.exit(1);
    }

    let aggregatedVaultSecrets = {};

    // 2. Begin multi-path scan
    console.log(`[VAULT] Connecting to ${VAULT_URI}...`);
    const vaultDataResults = await Promise.all(VAULT_SUBPATHS.map(subpath => fetchFromVaultPath(subpath)));
    vaultDataResults.forEach(data => {
        aggregatedVaultSecrets = { ...aggregatedVaultSecrets, ...data };
    });

    // 3. Merge secrets (Vault secrets override current .env)
    const finalEnv = { ...existingEnv, ...aggregatedVaultSecrets };

    // 4. Auto-generate MONGODB_URL if components are present
    if (finalEnv.MONGODB_HOST && finalEnv.MONGODB_PASSWORD && !finalEnv.MONGODB_URL) {
        const user = finalEnv.MONGODB_USERNAME || 'admin';
        finalEnv.MONGODB_URL = `mongodb://${user}:${finalEnv.MONGODB_PASSWORD}@${finalEnv.MONGODB_HOST}/?authSource=admin&directConnection=true`;
    }

    // 5. Save back to .env
    const envContent = Object.entries(finalEnv)
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([key, value]) => `${key}=${value}`)
        .join('\n');
    
    fs.writeFileSync(ENV_PATH, envContent + '\n');
    console.log(`\n[OK] Updated ${ENV_PATH} with ${Object.keys(finalEnv).length} keys total.\n`);

    // 6. Infrastructure Check
    console.log('[INFRA] Verifying connectivity...');
    
    let mongoHost = finalEnv.MONGODB_HOST || 'localhost';
    let mongoPort = 27017;
    if (finalEnv.MONGODB_URL) {
        try {
            // Simple robust parser for connection strings
            const match = finalEnv.MONGODB_URL.match(/@([^:\/]+):?(\d+)?/);
            if (match) {
                mongoHost = match[1];
                mongoPort = match[2] ? parseInt(match[2]) : 27017;
            }
        } catch (e) {}
    }

    const checks = [
        checkConnectivity(mongoHost, mongoPort, 'MongoDB'),
        checkConnectivity(finalEnv.REDIS_HOSTNAME || 'localhost', finalEnv.REDIS_PORT || 6379, 'Redis'),
        checkConnectivity((finalEnv.KAFKA_BOOTSTRAP_SERVERS || 'localhost:9092').split(':')[0], parseInt((finalEnv.KAFKA_BOOTSTRAP_SERVERS || 'localhost:9092').split(':')[1] || 9092), 'Kafka')
    ];

    const results = await Promise.all(checks);
    if (results.includes(false)) {
        console.warn('\n[!!] INFRASTRUCTURE WARNING: Verify your network/VPN or SSH tunnels.');
    } else {
        console.log('\n[SUCCESS] Environment synced and all services are reachable.');
    }
}

if (require.main === module) {
    sync().catch(err => {
        console.error('[ERROR] Sync failed:', err);
        process.exit(1);
    });
}
