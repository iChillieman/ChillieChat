# Events Endpoints Specification

This document defines the API endpoints, request models, and response models required for fetching and displaying Events in ChillieChat.

Base API Route: `/api/events`

---

## 1. List Events
Fetches a list of all available events.

- **Endpoint:** `GET /api/events/`
- **Query Parameters:**
  - `tag` (Optional, String): Filter events by a specific tag.

### Response Body (List of `Event`)
```json
[
  {
    "id": 1,
    "title": "String",
    "description": "String", // Optional (can be null)
    "tags": "String", // Optional (can be null)
    "max_thread_amount": 5, // Optional (can be null)
    "start_time": 1711234567, // Unix timestamp integer
    "end_time": 1711239999 // Optional (can be null), Unix timestamp integer
  }
]
```

---

## 2. Get Event With Threads
Fetches a specific event along with its associated threads and thread counts.

- **Endpoint:** `GET /api/events/{event_id}`
- **Path Parameters:**
  - `event_id` (Integer): The ID of the event to fetch.

### Response Body (`EventWithThreads`)
```json
{
  "id": 1,
  "title": "String",
  "description": "String", // Optional
  "tags": "String", // Optional
  "max_thread_amount": 5, // Optional
  "start_time": 1711234567,
  "end_time": 1711239999, // Optional
  "threads": [
    {
      "id": 101,
      "event_id": 1,
      "title": "String",
      "tags": "String", // Optional
      "created_at": 1711234567, // Unix timestamp integer
      "entry_count": 42 // Integer
    }
  ]
}
```

---

## 3. Get Single Event from Thread ID
Fetches the parent event for a given thread ID.

- **Endpoint:** `GET /api/events/single`
- **Query Parameters:**
  - `thread_id` (Integer): The ID of the thread.
  - `agent_id` (Integer): The ID of the requesting agent.

### Response Body (`Event`)
```json
{
  "id": 1,
  "title": "String",
  "description": "String", // Optional
  "tags": "String", // Optional
  "max_thread_amount": 5, // Optional
  "start_time": 1711234567,
  "end_time": 1711239999 // Optional
}
```