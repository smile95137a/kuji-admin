Draw Flow (Fixed Pool + Flip)
================================

High-level steps when a User performs a draw:

1. User clicks draw on UI; frontend sends POST /api/lottery/{lotteryId}/draw with their token.
2. API authenticates token and identifies the user.
3. The backend checks user's balance and deducts the draw cost (Gold or Bonus) using DB atomic update.
4. Backend selects a prize using the remaining inventory and weight distribution.
	- The selection calculates total weight as sum of (remaining * weight) for each prize.
	- A random number is chosen and the prize is found by cumulative weights.
	- Claiming a prize is performed with a DB update where remaining > 0; if update fails, retry.
5. Create a LotteryDrawRecord, insert PointLog, and (optionally) Order entry in DB.
6. If all prizes exhausted, mark lottery as ended (status = 0).
7. Response returned to frontend contains prize info.

Concurrency & Atomicity:
- Prize decrement is performed with a conditional DB update (decrement WHERE remaining > 0) to avoid double-claims.
- The system retries selection a few times if a concurrent claim steals the last piece.
- Consider row-level locks or optimistic transactions for high-frequency draw events.

