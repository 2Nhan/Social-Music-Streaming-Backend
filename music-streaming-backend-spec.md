# Music Streaming Backend Specification
## Inspired by SoundCloud

## 1. Project Context

This backend service provides APIs for a music streaming platform inspired by SoundCloud.
The system allows users to upload, stream, discover, and interact with audio content.

The backend is implemented as a **Monolithic Spring Boot Application** and must extend the **existing codebase without modifying current business logic**.

Agent must follow:
- Existing project structure (Strict Monolith package-by-layer/feature)
- Existing service patterns
- Existing naming conventions (e.g., using `Song` instead of `Track`)
- Existing dependency management

Do NOT refactor existing business logic.
New features must integrate cleanly with the current Monolithic architecture.

---

## Important Constraints (UPDATED)
1. **Existing Logic MUST NOT BE MODIFIED**: Any features or logic already implemented strictly must NOT be altered or refactored. The agent must build on top of or alongside current implementations.
2. **Temporarily Skipped Features**: The following features are currently OUT OF SCOPE and must not be implemented at this time:
   - **Search System** (Section 4.9)
   - **Discovery / Feed / Recommendations** (Section 4.10)
   - **Notifications** (Section 4.11)
   - **Play Count Tracking** (Section 4.12)
   - **Premium Subscription & Payments** (Section 4.13)
   - **Caching Strategy** (Section 5)
   
3. **MANDATORY HLS Streaming Requirement**: The project is already built entirely around **HLS (HTTP Live Streaming)**. Any further development for audio delivery MUST rely strictly on the existing HLS streaming architecture (`HlsService`, `master.m3u8` logic). Do NOT fall back to basic HTTP Range Requests or alternative video/audio delivery methods.

---

## 2. Engineering Requirements

### 2.1 Code Quality & Monolith Structure

All implementations must follow:
- Clean Code
- SOLID principles
- OOP best practices
- Clear separation of layers

**Strict Directory Structure Standard**:
The application must maintain a logically organized Monolithic structure. All new components must be placed into their respective directories according to this standard:
```text
src/main/java/com/tunhan/micsu/
├── config/       # Spring configurations, security configs, beans
├── controller/   # REST APIs (Endpoints)
├── dto/          # Data Transfer Objects (Request/Response models)
├── entity/       # JPA Entities representing DB tables
├── exception/    # Global exception handling & custom exceptions
├── mapper/       # Object mapping interfaces (e.g., MapStruct)
├── repository/   # Spring Data JPA repositories
├── security/     # JWT filters, custom authentication logic
├── service/      # Business logic interfaces
│   └── impl/     # Business logic implementations
└── utils/        # Helper classes, constants, generic functions
```

---

### 2.2 API Design Standard

All API endpoints must follow RESTful conventions.

**Base Path**
```text
/api/v1
```

**Resource naming**
```text
/users
/songs
/playlists
/comments
/likes
/follow
/search
/notifications
/stream
```

**HTTP verbs**
- `GET` -> retrieve
- `POST` -> create
- `PUT` -> update
- `DELETE` -> remove

**Response format**
All responses must follow this standard format:
```json
{
  "success": true,
  "data": {},
  "message": "",
  "timestamp": "2026-03-11T02:15:13+07:00"
}
```

### 2.3 Performance Requirements

Agent must optimize for:
- **Memory usage**: 
  - Stream audio using range requests or HLS streaming.
  - Avoid loading full files into memory. 
  - Use lazy loading where possible.
- **Response time**:
  - Implement caching for popular songs, search results, and user profiles.
  - Use pagination for all list endpoints.
  - Avoid N+1 query problems in JPA.

---

## 3. Core Domain Entities

The system supports the following main entities, inheriting from `BaseEntity` (which provides `createdAt` and `updatedAt`).

**User**
- id
- username
- email
- password
- avatarUrl
- bio
- followersCount
- followingCount
- songCount
- createdAt, updatedAt

**Song** (Replaces `Track` conceptually)
- id
- title
- description
- coverUrl
- audioUrl
- duration
- lyricsData
- favoriteCount
- viewCount
- repostCount
- visibility
- uploadedBy (User reference)
- createdAt, updatedAt

**Playlist**
- id
- name
- description
- coverImage
- createdBy
- songCount
- isPublic
- createdAt, updatedAt

**Comment**
- id
- songId
- userId
- content
- timestampInSong (position in audio)
- createdAt, updatedAt

**Like** (or Favorite)
- id
- userId
- targetType (SONG, PLAYLIST, COMMENT)
- targetId
- createdAt, updatedAt

---

## 4. Feature Analysis & API Contracts

The following core features must be implemented or extended based on SoundCloud's domain model.

### 4.0 Access Control Scope (Public vs. Secured)
The API must distinguish cleanly between public features (no token required) and secured features (token required).

**Public Features (No Login Required):**
- **Authentication**: Register, Login, Refresh token
- **Discovery**: Viewing a single Song's details and listening/streaming the audio (HLS)
- **Profiles**: Viewing a user's public profile and their public tracks
- **Playlists**: Viewing public playlists

**Secured Features (Login Required):**
- **Uploading**: Creating and managing Songs, uploading assets.
- **Interactions**: Liking songs/playlists, commenting, reposting.
- **Library/History**: Viewing personal liked songs list, creating private playlists, fetching listening history, fetching personal reposts.
- **Social**: Following other users.

### 4.1 Authentication & Authorization
**Features:** Register, Login, Refresh token, Logout, Update profile.
**APIs:**
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

### 4.2 User Profile
**Features:** View profile, edit profile, follow other users, see followers.
**APIs:**
- `GET /api/v1/users/{id}`
- `PUT /api/v1/users/{id}`
- `GET /api/v1/users/{id}/songs`
- `GET /api/v1/users/{id}/followers`
- `GET /api/v1/users/{id}/following`

### 4.3 Song Upload & Management
**Features:** Upload audio (creating HLS/streamable assets), upload cover art, edit metadata, delete song, get song details.
**APIs:**
- `POST /api/v1/songs`
- `PUT /api/v1/songs/{id}`
- `DELETE /api/v1/songs/{id}`
- `GET /api/v1/songs/{id}`

### 4.4 Audio Streaming (HLS ONLY)
**Features:** Stream audio files using the existing HLS architecture.
**APIs:**
- `GET /api/v1/songs/{songId}/hls/master.m3u8` 
- Note: Must use existing `HlsService` logic implementations. No standard file streaming allowed.

### 4.5 Likes / Favorites
**Features:** Users can like songs and playlists.
**APIs:**
- `POST /api/v1/likes`
- `DELETE /api/v1/likes/{id}`
- `GET /api/v1/users/{id}/likes`

### 4.5.1 Reposts
**Features:** Users can repost tracks. Reposts appear on the user's profile timeline.
**APIs:**
- `POST /api/v1/songs/{songId}/repost`
- `DELETE /api/v1/songs/{songId}/repost`
- `GET /api/v1/users/{id}/reposts` (List of songs the user has reposted)

### 4.6 Comments
**Features:** Comments can be attached to a specific timestamp in the song.
**APIs:**
- `POST /api/v1/songs/{songId}/comments`
- `GET /api/v1/songs/{songId}/comments`
- `DELETE /api/v1/comments/{id}`

### 4.6.1 Listening History & User Library
**Features:** Track what the user has listened to recently and maintain their list of liked songs.
**APIs:**
- `GET /api/v1/users/me/history` (Listening history)
- `GET /api/v1/users/me/likes` (Liked songs list)
- `POST /api/v1/users/me/history/{songId}` (Log a listen)

### 4.7 Playlists
**Features:** Create playlist, add song, remove song, reorder songs.
**APIs:**
- `POST /api/v1/playlists`
- `PUT /api/v1/playlists/{id}`
- `DELETE /api/v1/playlists/{id}`
- `POST /api/v1/playlists/{id}/songs`
- `DELETE /api/v1/playlists/{id}/songs/{songId}`

### 4.8 Follow System
**Features:** Follow and unfollow creators. Subscriptions to other users.
**APIs:**
- `POST /api/v1/follow/{userId}`
- `DELETE /api/v1/follow/{userId}`
- `GET /api/v1/users/{id}/followers`
- `GET /api/v1/users/{id}/following`

### 4.9 Search System (*TEMPORARILY SKIPPED*)
**Features:** Fuzzy search across songs, artists, and playlists with pagination.
**APIs:**
- `GET /api/v1/search?q=`

### 4.10 Discovery / Feed (*TEMPORARILY SKIPPED*)
**Features:** Personalized home feed (followed artists, trending songs, recent uploads).
**APIs:**
- `GET /api/v1/feed`
- `GET /api/v1/songs/trending`
- `GET /api/v1/songs/recommended`

### 4.11 Notifications (*TEMPORARILY SKIPPED*)
**Features:** Real-time or polled notifications for follows, likes, and comments.
**APIs:**
- `GET /api/v1/notifications`
- `PUT /api/v1/notifications/read/{id}`

### 4.12 Play Count Tracking (*TEMPORARILY SKIPPED*)
**Features:** Tracking views/listens securely.
**APIs:**
- `POST /api/v1/songs/{id}/play`

*Implementation requirement:* Prevent spam plays by validating unique sessions or using rate limiting algorithms.

### 4.13 Premium Subscription & Payments (*TEMPORARILY SKIPPED*)
**Features:** Unlocking premium features via payments (e.g., ad-free, high-quality audio).
**APIs:**
- `POST /api/v1/payments/checkout`
- `POST /api/v1/payments/webhook`

*Implementation requirement:* The system must ensure **transactional integrity**. Implement distributed lock strategies (e.g., Optimistic Locking with `@Version` on entities or Pessimistic Locking using database row locks/Redis locks) during the payment and fulfillment process to prevent double spending and race conditions.

---

## 5. Caching Strategy (*TEMPORARILY SKIPPED*)

Introduce caching to maintain high performance.
**Recommended Tool:** Redis
**Cache TTL Examples:**
- Trending songs -> 10 minutes
- User profile -> 5 minutes
- Search results -> 2 minutes
- Song metadata -> 1 hour

---

## 6. Pagination Standard

All list APIs must support Spring Data pagination equivalents:
- `?page=0`
- `&size=20`
- `&sort=createdAt,desc`

Response Structure (Pageable equivalent):
```json
{
 "content": [],
 "page": 0,
 "size": 20,
 "totalElements": 0
}
```

---

## 7. Security Requirements

- **JWT Authentication:** Stateless JWT validation.
- **Strict Asset Ownership Validation:** Users forms the ONLY tier of access; there is NO role-based hierarchy (e.g., no Admin vs. User distinctions). Every resource (Songs, Playlists, Comments, Profile data) must strictly validate that **only the original owner (uploader/creator) has permission to edit or delete it**.
- **Upload File Validation:** Limit file sizes, validate MIME types (audio and images).
- **Request Size Limits:** Configure application properties appropriately.

---

## 8. File Storage Strategy

Audio tracks and image covers should NOT be stored in the database.
**Requirement:** Object Storage (Cloudflare R2 with S3 compatibility)
Only the publicly accessible URLs (e.g., `audioUrl`, `coverUrl`) are stored in the database.

---

## 9. Logging & Monitoring

All backend services must implement consistent logging (SLF4J/Logback) for:
- Upload events (start, success, error)
- Streaming metrics and errors
- Authentication and security events

---

## 10. Future Scalability Extensions

The architecture must remain extensible for:
- Music Monetization & Ads.
- Podcast support.
- Live streaming.
- AI-driven recommendations.
