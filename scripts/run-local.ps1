# Load environment variables from .env file
# Try to find .env in current or parent directory
$EnvPath = Join-Path $PSScriptRoot "../.env"
if (-not (Test-Path $EnvPath)) {
    $EnvPath = Join-Path (Get-Item $PSScriptRoot).Parent.FullName ".env"
}

if (Test-Path $EnvPath) {
    Get-Content $EnvPath | Where-Object { $_ -match "^[^#].*=" } | ForEach-Object {
        $parts = $_.Trim().Split('=', 2)
        if ($parts.Count -eq 2) {
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            # Remove quotes if present
            $value = $value -replace "^['\""]", "" -replace "['\""]$", ""
            Set-Item "Env:$name" $value
        }
    }
}

# Find the am-trade-app directory
$AppDir = Join-Path $PSScriptRoot "../am-trade-app"
if (-not (Test-Path $AppDir)) {
    $AppDir = Join-Path $pwd "am-trade-app"
}

if (Test-Path $AppDir) {
    Push-Location $AppDir
    ./../mvnw spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.jvmArguments=-Xmx512m" "-Dspring-boot.run.arguments=--server.port=8080"
    Pop-Location
} else {
    ./mvnw spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.jvmArguments=-Xmx512m" "-Dspring-boot.run.arguments=--server.port=8080"
}
