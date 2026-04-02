# Critical Path: Google Play Compliance (Reporting & Moderation)

**Objective:**
Implement user reporting functionality to comply with Google Play Developer Policies regarding AI-Generated Content. This requires backend database modifications, a new API endpoint, WebSocket broadcasts, and comprehensive Android UI logic to hide/reveal reported content.

## Phase 1: Backend Updates (nexus-fest)

1. **Database Schema:**
   - Add two new columns to the `entries` table (or model):
     - `reported_at` (Integer/Timestamp, nullable)
     - `reported_count` (Integer, default 0)

2. **API Endpoint:**
   - Create a new endpoint (e.g., `POST /api/entries/{entry_id}/report`).
   - Logic:
     - Set `reported_at` to the current timestamp (if currently null).
     - Increment `reported_count` by 1.
     - Broadcast a `ENTRY_REPORTED` event over the thread's WebSocket.

## Phase 2: Android App Updates (ChillieChat)

1. **Settings & Persistence:**
   - Add a new boolean preference in DataStore/SharedPreferences: `alwaysShowReportedMessages` (default `false`).
   - Add a toggle for this preference in the `SettingsScreen`.

2. **Reporting UI Flow:**
   - Implement a long-press (or similar interaction) on an Entry bubble in `EntriesScreen` to reveal a "Report" icon/text.
   - Tapping "Report" opens a Dialog:
     - Title: "Report Message"
     - Text: "Are you sure you want to report this message as inappropriate or offensive?"
     - Checkbox: "I Understand"
     - Buttons: "Cancel", "Submit" (Submit is disabled until the checkbox is ticked).
   - Tapping "Submit" calls the new backend reporting endpoint.

3. **WebSocket & State Handling:**
   - Update `WebSocketManager` / `EntriesViewModel` to handle the `ENTRY_REPORTED` broadcast event.
   - When received, update the specific Entry in the UI state to reflect its reported status.

4. **Viewing Reported Messages:**
   - If an Entry is reported and `alwaysShowReportedMessages` is `false`:
     - Hide the message content.
     - Display: "Message has been reported."
     - Show a "Show Anyways" button.
     - Always display a small red flag icon in the top right of the message bubble.
   - Tapping "Show Anyways" opens an "Uncensor Dialog":
     - Text: "This message was reported as inappropriate or misleading. Are you sure you want to view it?"
     - Checkbox: "Yes, I want to reveal this message"
     - Checkbox: "Always Show Reported Messages"
     - Buttons: "Cancel", "Reveal"
   - Tapping "Reveal" (with "Yes" checked) unhides that specific message for the current session.
   - If "Always Show Reported Messages" is checked and "Reveal" is tapped, update the global DataStore preference to `true` and unhide all reported messages permanently.
   - If an Entry is reported and `alwaysShowReportedMessages` is `true`:
     - Do not hide the message content.
     - Still display the small red flag icon in the top right.

**Success Criteria:**
- Users can report messages.
- Reported messages are immediately hidden for users who haven't opted-in to seeing them.
- Users can opt-in to see individual reported messages or all reported messages globally.
- The app fully satisfies Google Play's user-safety requirements for AI-generated content.