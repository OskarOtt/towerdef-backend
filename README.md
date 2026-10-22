# towerdef-backend

Spring Boot leaderboard API. Run Postgres locally: `docker compose up -d`, then `./mvnw spring-boot:run`.

## API
1. `POST /api/runs` → `{runId, token}` — call when a game starts.
2. `POST /api/runs/{runId}/score` — on game over. Body:
   `{token, name (1-16 chars), wave, kills, topTowerId, topTowerLevel, achievements}` (topTowerLevel = sum of the tower's 5 upgrade levels).
   → `{rank}`. Errors: 400 invalid, 403 bad token, 409 already used, 410 expired, 422 implausible, 429 rate limited.
   A run can be submitted once; a rejected attempt also consumes it.
3. `GET /api/leaderboard?limit=10` (max 100) → ranked by wave, kills, earliest.

## Config (env)
`DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `TOWERDEF_SECRET` (**set in production**), `TOWERDEF_ALLOWED_ORIGINS`,
`TOWERDEF_TRUST_FORWARDED_FOR=true` only when behind a trusted reverse proxy (otherwise rate limiting uses the socket IP).

## Limits
Per-IP rate limits (in-memory, per minute): start 10, submit 5, read 60. Plausibility checks mirror frontend game constants; keep them in sync with `towerdef-io/src/game`. Scores from a client-side game can never be fully verified.

## Deployment
Same pipeline as dogtrainer-backend: `.github/workflows/deploy.yml` builds/tests, pushes `ghcr.io/<repo>:latest`, copies `k8s/` to the MicroK8s host and rolls out. Schema is managed by Liquibase (`db/changelog`).
Required GitHub secrets: `SSH_HOST`, `SSH_USER`, `SSH_PRIVATE_KEY`. Create once in the cluster:
`kubectl create secret generic towerdef-backend-secret --from-literal=DATABASE_URL=jdbc:postgresql://... --from-literal=DATABASE_USER=... --from-literal=DATABASE_PASSWORD=... --from-literal=TOWERDEF_SECRET=$(openssl rand -hex 32)`
Also needs the existing `ghcr-secret`. Ingress host `towerdef-api.oskott.com` and image owner `oskarott` are assumptions; adjust in `k8s/`. Runs with a single replica because rate-limit buckets are in memory.
