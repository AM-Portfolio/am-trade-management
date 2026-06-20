# Favorite Filter API - PowerShell test script
# User identity comes from Authorization Bearer JWT (not userId query params).

$baseUrl = "http://localhost:8073/api/v1/filters"
$jwtToken = if ($env:JWT_TOKEN) { $env:JWT_TOKEN } else { "replace-with-your-jwt" }
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $jwtToken"
}

Write-Host "Listing filters..."
Invoke-RestMethod -Uri $baseUrl -Method Get -Headers $headers

Write-Host "Getting default filter..."
Invoke-RestMethod -Uri "$baseUrl/default" -Method Get -Headers $headers
