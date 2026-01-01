Write-Host "Starting Cross-SDK Compatibility Test..." -ForegroundColor Cyan

# 1. Build and Run Java Dumper
Write-Host "`n[1/3] Running Java Dumper..." -ForegroundColor Yellow
Set-Location "java-dumper"
mvn clean compile exec:java
if ($LASTEXITCODE -ne 0) {
    Write-Host "Java Dumper failed!" -ForegroundColor Red
    exit 1
}
Set-Location ".."

# 2. Check for JSON output
if (-not (Test-Path "trade_sample.json")) {
    Write-Host "Error: trade_sample.json was not created." -ForegroundColor Red
    exit 1
}
Write-Host "Java Dumper success. JSON generated." -ForegroundColor Green

# 3. Run Python Verifier
Write-Host "`n[2/3] Running Python Verifier..." -ForegroundColor Yellow
Set-Location "python-verifier"
# Ensure dependencies are installed (optional if already in env, but good practice)
# pip install -r ../../am-trade-sdk-python/requirements.txt  <-- logic if needed, skipping for speed
python verify.py
if ($LASTEXITCODE -ne 0) {
    Write-Host "Python Verifier failed!" -ForegroundColor Red
    exit 1
}
Set-Location ".."

Write-Host "`n[3/3] Test Sequence Completed Successfully!" -ForegroundColor Green
