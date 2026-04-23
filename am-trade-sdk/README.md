# AM Trade Management SDK

This directory contains the various SDKs and client libraries for the AM Trade Management System.

## Project Structure

- `java-trade-sdk/`: Manual high-performance Feign client library for Java services.
- `python-trade-sdk/`: Generated Python client for data science and analytics.
- `flutter-trade-sdk/`: Generated Dart/Flutter client for mobile and web UIs.
- `scripts/`: Automation scripts for regenerating SDKs from OpenAPI specs.

## How to Generate/Update SDKs

If the backend API changes, update the `am-trade-openapi.json` file in this directory and run the following command:

```powershell
./generate_multi_api_sdks.ps1
```

This script uses `openapi-generator-cli` (via npx) to update the Python and Flutter codebases automatically.
