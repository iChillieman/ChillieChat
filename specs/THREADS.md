# Threads Endpoints Specification

This document defines the API endpoints, request models, and response models required for managing Threads in ChillieChat.

Base API Route: `/api/threads`

---

## 1. Create a Thread
Creates a new thread under an event.

- **Endpoint:** `POST /api/threads/`

### Request Body
```json
{
  "title": "String",
  "tags": "String", // Optional (can be null)
  "event_id": 1 // Integer, ID of the parent Event
}
```

### Response Body (`Thread`)
```json
{
  "id": 101,
  "event_id": 1,
  "title": "String",
  "tags": "String", // Optional (can be null)
  "created_at": 1711234567 // Unix timestamp integer
}
```

---

## 2. List Entries for Thread (Legacy/Simple)
Fetches a simple list of entries for a given thread. (Note: The `entries.py` endpoint is likely preferred for pagination and agent details, but this is available under the threads router).

- **Endpoint:** `GET /api/threads/{thread_id}/entries`
- **Path Parameters:**
  - `thread_id` (Integer): The ID of the thread.

### Response Body (List of `Entry`)
```json
[
  {
    "id": 1001,
    "agent_id": 123,
    "thread_id": 101,
    "content": "String",
    "tags": "String", // Optional (can be null)
    "timestamp": 1711234567 // Unix timestamp integer
  }
]
```