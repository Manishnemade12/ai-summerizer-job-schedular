# 🚀 SmartCache AI — Async AI Processing & Caching Engine (Go)

## 🧠 Overview

SmartCache AI is a **backend-focused, high-visibility system** that combines:

* ⚡ Redis/Valkey caching
* 🤖 AI-powered summarization
* ⚙️ Asynchronous job processing (worker pool)
* 📊 Analytics & observability

This is **not a simple AI wrapper** — it is a **scalable AI processing pipeline** similar to real production systems.

---

## 🎯 Goals

* Showcase **Go concurrency (goroutines, channels)**
* Implement **async job queue system**
* Use **Redis/Valkey for caching + state management**
* Integrate **AI meaningfully (not per request)**
* Demonstrate **system design + backend engineering**

---

## ❌ Non-Goals

* No authentication (v1)
* No complex UI
* No microservices (single service)

---

## 🏗️ System Architecture

Client → Go API → Redis (Cache + Queue) → Worker Pool → AI → Redis → Client

---

## 🔄 Core Flow (IMPORTANT)

### Request Flow:

1. User sends input (text / URL)
2. Backend generates hash key
3. Redis check:

   * ✅ HIT → return cached result
   * ❌ MISS:

     * create job
     * push to queue
     * return `processing` status

---

### Worker Flow:

1. Worker picks job from queue
2. Fetch content (if URL)
3. Call AI
4. Store result in Redis
5. Update job status → `completed`

---

## 🧩 Core Features

---

### ⚙️ Async Job Queue System (🔥 MOST IMPORTANT)

* Redis-based queue
* Background workers using goroutines
* Non-blocking API

#### Job States:

* `pending`
* `processing`
* `completed`
* `failed`

---

### 🧠 AI Processing Engine

* Generates:

  * summary (2–3 lines)
  * tags (2–4)

* Runs **only in worker (not request path)**

---

### ⚡ Smart Caching (Redis / Valkey)

* Cache key: `summary:{hash}`
* TTL-based expiry
* Avoid duplicate AI calls

---

### 📊 Analytics & Observability

Track:

* total requests
* cache hits / misses
* queue size
* processing time
* failure rate

---

### 🔁 Deduplication System

* Input → hashed
* Same input → same key
* Prevents duplicate processing

---

## 🛠️ Tech Stack

### Backend

* Go (Gin)

### Cache + Queue

* Redis / Valkey

### AI

* Gemini API

---

## 📁 Folder Structure

```
smartcache-ai/

  backend/
    cmd/
      server/
        main.go

    internal/
      api/
        handlers/
          submit.go
          status.go

      worker/
        pool.go
        job.go

      cache/
        redis.go

      ai/
        gemini.go
        prompt.go

      services/
        processor.go

      analytics/
        metrics.go

    config/
      config.go

    .env.example
    go.mod
```

---

## 🔌 API Design

---

### POST `/api/submit`

Submit text or URL for summarization

#### Request:

```json
{
  "input": "https://example.com/article"
}
```

#### Response:

```json
{
  "job_id": "abc123",
  "status": "processing"
}
```

---

### GET `/api/status/:job_id`

Check job status

```json
{
  "status": "completed",
  "summary": "AI tools are dominating modern developer workflows.",
  "tags": ["AI", "DevTools"]
}
```

---

### GET `/api/analytics`

```json
{
  "total_requests": 1200,
  "cache_hits": 800,
  "queue_size": 5,
  "avg_processing_time_ms": 320
}
```

---

## 🧠 Redis Key Design (VERY IMPORTANT)

| Purpose   | Key              | Example        |
| --------- | ---------------- | -------------- |
| Cache     | `summary:{hash}` | summary:abc123 |
| Job data  | `job:{id}`       | job:xyz789     |
| Queue     | `job_queue`      | list           |
| Analytics | `metrics:*`      | metrics:hits   |

---

## ⚙️ Environment Variables

```
PORT=8080

REDIS_URL=redis://localhost:6379

GEMINI_API_KEY=your_key

WORKER_COUNT=3

CACHE_TTL=300
```

---

## ▶️ Running Locally

### Start Redis

```
docker run -d -p 6379:6379 redis
```

### Run Backend

```
cd backend
go mod tidy
go run cmd/server/main.go
```

---

## 🧠 Key Concepts Demonstrated

* Async processing with goroutines
* Redis as cache + queue
* API performance optimization
* AI integration (decoupled)
* Job-based architecture
* Observability

---

## 🚀 Future Improvements

* WebSocket for live job updates
* Rate limiting using Redis
* Retry mechanism for failed jobs
* PostgreSQL for persistence
* AI batching

---

## 🎯 Why This Project Matters

This project demonstrates:

* Real-world backend architecture
* Concurrency handling in Go
* Production-like async systems
* Efficient AI usage
* Strong system design thinking

---

## 🧠 Final Note

This is **not just an AI project**.

It is a **scalable backend system that happens to use AI**.

---

**Think like a backend engineer. Build like one.**
