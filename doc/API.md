# KUJI Admin API

This document lists the main API endpoints for the KUJI-Admin backend. Context path is `/api` (see `application.yml`). Backoffice/admin routes are under `/admin/**`.

Authentication: JWT (Access + Refresh tokens). The `Authorization` header should be set as `Bearer <accessToken>` for protected endpoints.

## Front-end / Player APIs

### POST /api/auth/register
Registers a new player.

Request:
{
	"email": "player@example.com",
	"password": "secret123",
	"nickname": "PlayerOne"
}

Response (200):
{
	"success": true,
	"data": { ... user object ... }
}

### POST /api/auth/login
Login for players.

Request:
{
	"email": "player@example.com",
	"password": "secret123"
}

Response (200):
{
	"accessToken": "...",
	"refreshToken": "...",
	"expiresIn": 86400,
	"user": { ... }
}

### POST /api/auth/google
Login via Google. Front end should obtain a Google ID token and send it to backend to verify and create/find user.

Request:
{
	"idToken": "<Google ID token>"
}

### POST /api/auth/refresh
Refresh the access token using a refresh token.

Request:
{
	"refreshToken": "..."
}

Response:
{
	"accessToken": "...",
	"refreshToken": "...",
	"expiresIn": 86400
}

### GET /api/user/me
Gets current player details (must be authenticated)

### POST /api/lottery/{id}/draw
Performs a draw on a lottery.
Parameters: costType=gold|bonus (default=gold)

Response (200):
{
	"id": "draw-record-id",
	"lotteryId": "...",
	"userId": "...",
	"prizeId": "...",
	"costType": "gold",
	"costAmount": 100,
	"status": "won"
}

## Admin APIs (Backoffice)
Admin endpoints are under `/admin/**` and require `ROLE_Admin` or properly assigned roles.

### POST /admin/auth/login
Admin login with username & password. Returns tokens like Player login.

### Store / Role / Menu management
(To be expanded) Roles and menu management are controlled by Admins. RBAC table `role`, `menu`, `role_menu`, and `admin_user_role` decide access and displayed menu items.

## Error handling
Standard responses are wrapped by `ApiResponse`. In case of errors, the response will be:
{
	"success": false,
	"error": { "code": "ERR_CODE", "message": "Human readable message" },
	"meta": { ... }
}

Common error codes:
- 400: Bad request
- 401: Unauthorized / invalid token
- 403: Forbidden
- 422: Business validation (e.g., insufficient funds)
- 500: Internal server error

