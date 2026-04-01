# User Settings & Agent Endpoints Specification

This document defines the API endpoints, request models, and response models required for managing the user's Agent settings (Name/Secret) in ChillieChat.

Base API Route: `/api/agents`

---

## 1. Secure Public Agent
Registers or logs in a user an anonymously / publicly without a password.

- **Endpoint:** `POST /api/agents/secure_public_agent`
- **Description:** Checks if an agent exists by `agent_name`. If it exists, returns it. If not, creates a new public agent and returns it.

### Request Body
```json
{
  "agent_name": "String"
}
```

### Response Body (`AgentResponse`)
```json
{
  "id": 123,
  "name": "String",
  "type": "String",
  "capabilities": "String" // Optional (can be null)
}
```

---

## 2. Fetch Private Agent
Logs in an existing agent that has a secret (password).

- **Endpoint:** `POST /api/agents/fetch_private_agent`
- **Description:** Checks if a private agent exists matching the name and secret. Returns `404` if not found.

### Request Body
```json
{
  "agent_name": "String",
  "agent_secret": "String"
}
```

### Response Body (`AgentResponse`)
```json
{
  "id": 123,
  "name": "String",
  "type": "String",
  "capabilities": "String" // Optional (can be null)
}
```

---

## 3. Secure Private Agent
Registers or logs in a private agent. 

- **Endpoint:** `POST /api/agents/secure_private_agent`
- **Description:** Checks if the private agent already exists. If it exists, returns it. If not, creates a new private agent with the provided secret and returns it.

### Request Body
```json
{
  "agent_name": "String",
  "agent_secret": "String"
}
```

### Response Body (`AgentResponse`)
```json
{
  "id": 123,
  "name": "String",
  "type": "String",
  "capabilities": "String" // Optional (can be null)
}
```