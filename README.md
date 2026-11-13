# Make Shorty API

A small URL shortener REST API built with Spring Boot and PostgreSQL (Supabase).
You send it a long URL and it returns a short code. Opening `/<code>` redirects to the original URL.

## Tech stack

- Java 21
- Spring Boot 4 (Web MVC, Data JPA, Validation)
- PostgreSQL (hosted on Supabase)
- Docker (multi-stage build)
- Deployed on Render

## API

### `POST /shorten`

Creates a short code for a URL.

**Request**

```json
{ "url": "https://example.com/some/very/long/path" }
```

**Response `200 OK`**

```json
{ "short": "aB3xY9z" }
```

The full short link is `<API base URL>/<short>`.

**Errors `400 Bad Request`**

| `error` value          | Cause                                                      |
| ---------------------- | ---------------------------------------------------------- |
| `url is required`      | `url` is missing or empty                                  |
| `invalid url`          | `url` does not start with `http://` or `https://`, or has spaces |
| `url is too long`      | `url` is longer than 2048 characters                       |
| `invalid request body` | The body is not valid JSON                                 |

### `GET /{code}`

Redirects to the original URL.

- `302 Found` with a `Location` header pointing to the original URL
- `404 Not Found` with `{ "error": "link not found" }` when the code does not exist

### Example

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com"}'

curl -i http://localhost:8080/aB3xY9z
```

## How codes are generated

Each code is 7 random base62 characters (`a-z`, `A-Z`, `0-9`), generated with `SecureRandom`.
That gives about 3.5 trillion combinations, so codes can't be guessed by counting.
If a generated code already exists, the service tries again (up to 5 times).

## Configuration

The app reads its settings from environment variables:

| Variable               | Description                                  | Default |
| ---------------------- | -------------------------------------------- | ------- |
| `SUPABASE_JDBC_URL`    | JDBC URL of the PostgreSQL database          | —       |
| `SUPABASE_DB_USER`     | Database user                                | —       |
| `SUPABASE_DB_PASSWORD` | Database password                            | —       |
| `PORT`                 | HTTP port                                    | `8080`  |

> **Supabase tip:** use the **Session pooler** connection string
> (`jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres`, user `postgres.<project-ref>`).
> The direct connection (`db.<ref>.supabase.co`) is IPv6-only and fails on hosts without IPv6, such as Render.

## Database schema

Hibernate runs with `ddl-auto: validate`, so it does not create tables. The table must already exist:

```sql
CREATE TABLE short_urls (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(10) NOT NULL UNIQUE,
    original_url TEXT        NOT NULL,
    created_at   TIMESTAMPTZ
);
```

## Running locally

Requirements: Java 21 and a PostgreSQL database with the table above.

```bash
export SUPABASE_JDBC_URL="jdbc:postgresql://..."
export SUPABASE_DB_USER="..."
export SUPABASE_DB_PASSWORD="..."
./mvnw spring-boot:run
```

## Running with Docker

```bash
docker build -t make-shorty-api .
docker run -p 8080:8080 \
  -e SUPABASE_JDBC_URL="jdbc:postgresql://..." \
  -e SUPABASE_DB_USER="..." \
  -e SUPABASE_DB_PASSWORD="..." \
  make-shorty-api
```

The final image runs as a non-root user.

## Tests

```bash
./mvnw test
```

The tests mock the database, so they run without any database or environment variables.

## CORS

Allowed origins are set in `CorsConfig`. Update them when the frontend domain changes.
