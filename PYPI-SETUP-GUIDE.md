# PyPI Publishing Setup Guide

This guide explains how to set up PyPI publishing for the `am-trade-sdk` package.

## Prerequisites

- PyPI account (create at https://pypi.org/account/register/)
- GitHub repository with write access
- GitHub CLI or direct GitHub web access

## Step 1: Create PyPI API Token

1. Go to https://pypi.org/manage/account/tokens/
2. Click **"Add API token"**
3. Give it a name: `am-trade-sdk-github-actions`
4. Scope: **Entire account** (or specific project if preferred)
5. Click **"Create token"**
6. **Copy the token** - you'll only see it once!

Token format: `pypi-AgEI...` (starts with `pypi-`)

## Step 2: Add GitHub Secret

### Using GitHub Web Interface:

1. Go to your repository: https://github.com/ssd2658/am-trade-managment
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Name: `PYPI_API_TOKEN`
5. Secret: Paste the token from Step 1
6. Click **Add secret**

### Using GitHub CLI:

```bash
gh secret set PYPI_API_TOKEN --body "pypi-AgEI..." -R ssd2658/am-trade-managment
```

## Step 3: Verify Setup

After adding the secret, trigger the workflow:

```bash
# Option 1: Push to feature branch
git push origin feature/sdk

# Option 2: Manual trigger (if workflow supports it)
gh workflow run pythonsdk.yml -R ssd2658/am-trade-managment
```

## Workflow Behavior

When you push to any branch (including `feature/sdk`), the workflow will:

1. **Build** the Python SDK (wheel + source distribution)
2. **Publish** to PyPI using the API token
3. **Wait** 30 seconds for PyPI to sync
4. **Verify** by:
   - Installing the package from PyPI
   - Testing all core modules (TradeClient, Trade, SdkConfig)
   - Displaying verification results

## Expected Workflow Output

### Successful Publication:
```
Uploading am_trade_sdk-1.0.0-py3-none-any.whl
100% ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 20.9/20.9 kB • 00:00 • 44.2 MB/s

Response from https://upload.pypi.org/legacy/:
200 OK - File already exists. See https://pypi.org/help/#file-name-reuse for more information.
```

### Verification Success:
```
[OK] All core modules imported successfully
[OK] TradeClient: <class 'sdk.client.trade_client.TradeClient'>
[OK] Trade: <class 'sdk.models.Trade'>
[OK] SdkConfig: <class 'sdk.config.SdkConfig'>

=== SDK VERIFICATION PASSED ===
Package: am-trade-sdk
Status: Successfully published to PyPI
Installation: pip install am-trade-sdk
```

## Troubleshooting

### Error: "Invalid or non-existent authentication information"

**Cause:** The `PYPI_API_TOKEN` secret is not set or is incorrect.

**Solution:**
1. Verify the secret is added: Go to Settings → Secrets → Check `PYPI_API_TOKEN` exists
2. Verify the token is correct: Regenerate if needed at https://pypi.org/manage/account/tokens/
3. Update the secret with the new token

### Error: "File already exists"

**Cause:** Package version already exists on PyPI.

**Solution:** Increment the version in `am-trade-sdk-python/pyproject.toml`:
```toml
[project]
name = "am-trade-sdk"
version = "1.0.1"  # Change from 1.0.0
```

### Error: "401 Unauthorized"

**Cause:** Invalid PyPI credentials.

**Solution:**
1. Regenerate API token at https://pypi.org/manage/account/tokens/
2. Update GitHub secret with new token

## After First Successful Publication

Once published to PyPI, users can install with:

```bash
pip install am-trade-sdk
```

Update documentation to reflect this:

```markdown
# Installation

## From PyPI (Recommended)

```bash
pip install am-trade-sdk
```

## From GitHub Releases

```bash
pip install https://github.com/ssd2658/am-trade-managment/releases/download/...
```

## From Source

```bash
git clone https://github.com/ssd2658/am-trade-managment.git
cd am-trade-sdk-python
pip install -e .
```
```

## Workflow Files

- **Publishing Workflow:** `.github/workflows/pythonsdk.yml`
  - Triggers on: Push to any branch, manual dispatch
  - Jobs:
    1. `publish-pypi` - Build and publish to PyPI
    2. `verify-pypi-publication` - Verify installation and imports

- **CI Workflow:** `.github/workflows/python-sdk-ci.yml`
  - Runs tests on every push
  - Handles encoding issues
  - Builds distribution artifacts

## References

- PyPI Help: https://pypi.org/help/
- Twine Documentation: https://twine.readthedocs.io/
- GitHub Secrets: https://docs.github.com/en/actions/security-guides/encrypted-secrets
