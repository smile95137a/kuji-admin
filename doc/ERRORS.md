# Error Cases, Edge Cases and Notes

Common error responses are handled by global exception handler and wrapped by `ApiResponse`.

## Common HTTP codes & causes
- 400 Bad Request: Malformed requests, validation issues.
- 401 Unauthorized: Missing/expired/invalid JWT tokens.
- 403 Forbidden: Insufficient role or permissions.
- 404 Not Found: Resource does not exist (lottery, user, prize).
- 422 Unprocessable Entity: Business validation failure such as insufficient funds or invalid draw type.
- 500 Internal Server Error: Unexpected errors, DB issues.

## Draw-specific edge cases
- Concurrency: multiple users drawing at the same time; prize decrement and DB update should be atomic.
- Rate limiting not implemented; consider limiting draws per user or throttling requests.
- Draw when lottery has no prizes: should mark lottery as ended and return an error.
- Insufficient balance: return 422 with message 'Insufficient gold/bonus coins'.

## Admin RBAC edge cases
- Store owner vs Editor scope: ensure actions that require higher privilege are restricted (e.g., creating stores, editing roles).

