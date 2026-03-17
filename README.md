# IntellectLight

A Spring Boot authentication and knowledge management service with RAG (Retrieval-Augmented Generation) capabilities, social networking features, and comprehensive content management.

## Tech Stack

| Component | Technology |
|-----------|------------|
| Runtime | Java 21, Spring Boot 3.2.4 |
| Database | MySQL 8.0+ (MyBatis) |
| Cache | Redis + Caffeine (two-tier) |
| Search | Elasticsearch 9.2.1 |
| AI/RAG | Spring AI (OpenAI, DeepSeek) |
| Auth | JWT (RSA 2048-bit) + Spring Security |
| Messaging | Kafka + Canal CDC |
| Storage | Aliyun OSS |
| Monitoring | Spring Actuator |

## Features

- **Authentication** — JWT-based auth with access/refresh tokens, verification codes (SMS/email), password reset, and login audit logging
- **Knowledge Posts** — Full lifecycle management (draft → pending → published) with OSS-backed content storage and integrity checks (ETag, SHA-256)
- **RAG Q&A** — AI-powered question answering over post content via Elasticsearch vector store and streaming SSE responses
- **Social Graph** — Follow/unfollow with bidirectional relationship tracking, paginated follower/following lists
- **Search** — Full-text search with Elasticsearch and autocomplete suggestions via completion suggester
- **Counters** — Compact SDS-format counters for likes, favorites, followers, following, and post counts with Kafka-driven aggregation
- **Hot Key Detection** — Automatic cache TTL extension based on access frequency with configurable heat levels
- **CDC Sync** — Canal captures MySQL binlog changes via the transactional outbox pattern, publishing to Kafka for Elasticsearch indexing

## Project Structure

```
src/main/java/com/tongji/
├── auth/           # Authentication, JWT, verification codes
├── cache/          # Two-tier caching (Redis + Caffeine), hot key detection
├── common/         # Error codes, exceptions, global handler, utilities
├── config/         # Elasticsearch, Redisson, thread pool configuration
├── counter/        # SDS-format counters, Kafka aggregation
├── knowpost/       # Knowledge post CRUD, RAG, AI description
├── profile/        # User profile management
├── relation/       # Social graph (follow/follower)
├── search/         # Elasticsearch search, Canal CDC consumers
├── storage/        # Aliyun OSS presigned URL generation
└── user/           # User service
```

## API Endpoints

### Authentication — `/api/v1/auth`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/send-code` | Send verification code |
| POST | `/register` | Register new user |
| POST | `/login` | Login (password or code) |
| POST | `/token/refresh` | Refresh token pair |
| POST | `/logout` | Logout and revoke token |
| POST | `/password/reset` | Reset password |
| GET | `/me` | Get current user info |

### Knowledge Posts — `/api/v1/knowposts`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/drafts` | Create new draft |
| POST | `/{id}/content/confirm` | Confirm content upload |
| PATCH | `/{id}` | Update metadata |
| POST | `/{id}/publish` | Publish post |
| PATCH | `/{id}/top` | Pin/unpin post |
| PATCH | `/{id}/visibility` | Update visibility |
| DELETE | `/{id}` | Soft delete |
| GET | `/feed` | Public feed (paginated) |
| GET | `/mine` | User's published posts |
| GET | `/detail/{id}` | Post details |
| GET | `/{id}/qa/stream` | RAG Q&A (SSE streaming) |
| POST | `/{id}/rag/reindex` | Trigger RAG reindex |
| POST | `/description/suggest` | AI-generated description |

### Search — `/api/v1/search`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Keyword search with pagination |
| GET | `/suggest` | Autocomplete suggestions |

### Relations — `/api/v1/relation`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/follow` | Follow user |
| POST | `/unfollow` | Unfollow user |
| GET | `/status` | Relation status query |
| GET | `/following` | Following list (paginated) |
| GET | `/followers` | Followers list (paginated) |
| GET | `/counter` | User dimension counters |

### Actions & Counters — `/api/v1/action`, `/api/v1/counter`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/like`, `/unlike` | Like/unlike entity |
| POST | `/favorite`, `/unfavorite` | Favorite/unfavorite entity |
| GET | `/{etype}/{eid}` | Get aggregated counts |

### Storage — `/api/v1/storage`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/presign` | Get presigned upload URL |

## Prerequisites

- Java 21
- Maven 3+
- MySQL 8.0+
- Redis
- Elasticsearch 9.2.1
- Kafka
- Canal (for CDC)
- Aliyun OSS account

## Getting Started

1. **Set up the database**

   ```bash
   mysql -u root -p < db/schema.sql
   ```

2. **Configure the application**

   Create or update `src/main/resources/application.yml` with your environment-specific settings for MySQL, Redis, Elasticsearch, Kafka, Canal, and Aliyun OSS.

3. **Build**

   ```bash
   mvn clean package
   ```

4. **Run**

   ```bash
   java -jar target/IntellectLight-1.0-SNAPSHOT.jar
   ```

   The application starts on the default Spring Boot port. Health check is available at `/actuator/health`.

## Testing

```bash
mvn test
```

## Architecture Highlights

- **Two-tier caching** — Caffeine (local, ~10-30s TTL) backed by Redis (distributed) with automatic hot key TTL extension
- **Transactional outbox** — Guarantees at-least-once event delivery from MySQL to Kafka via Canal CDC
- **SDS counters** — Compact 20-byte big-endian format packing 5 counter dimensions into a single Redis value
- **Distributed locking** — Redisson for coordinating counter rebuilds and concurrent operations
- **Stateless auth** — RSA-signed JWT tokens with no server-side session storage
