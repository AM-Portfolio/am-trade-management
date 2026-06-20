# Bulk Delete Favorite Filters - curl Examples

## Delete Multiple Filters (Bulk Delete)

### curl Command
```bash
curl -X DELETE "http://localhost:8050/api/v1/filters/bulk" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "filterIds": [
      "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "c3d4e5f6-a7b8-9012-cdef-123456789012"
    ]
  }'
```

### Response Example
```json
{
  "deletedCount": 3,
  "totalRequested": 3,
  "message": "Successfully deleted 3 out of 3 filters"
}
```

## PowerShell Example

```powershell
$body = @{
    userId = "user123"
    filterIds = @(
        "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "b2c3d4e5-f6a7-8901-bcde-f12345678901",
        "c3d4e5f6-a7b8-9012-cdef-123456789012"
    )
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8050/api/v1/filters/bulk" `
  -Method Delete `
  -ContentType "application/json" `
  -Body $body
```

## Single Filter Delete (Original Endpoint - Still Available)

### curl Command
```bash
curl -X DELETE "http://localhost:8050/api/v1/filters/a1b2c3d4-e5f6-7890-abcd-ef1234567890?userId=user123" \
  -H "Content-Type: application/json"
```

### PowerShell
```powershell
$filterId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
Invoke-RestMethod -Uri "http://localhost:8050/api/v1/filters/$filterId?userId=user123" `
  -Method Delete `
  -ContentType "application/json"
```

## Notes

- **Bulk Delete**: Use `/api/v1/filters/bulk` endpoint with DELETE method
  - Pass `userId` and `filterIds` in the request body
  - Returns count of successfully deleted filters
  - Continues processing even if some filters fail (partial success)

- **Single Delete**: Use `/api/v1/filters/{filterId}` endpoint with DELETE method
  - Pass `userId` as query parameter
  - Returns 204 No Content on success, 404 if not found

- Both endpoints verify that filters belong to the specified user before deletion
- Bulk delete is transactional - uses @Transactional annotation
