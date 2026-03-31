# Critical Path 7: Further Polish

**Objective:**
Address the issues in EntriesScreen and enhance the EventsScreen to make the Events more visually
pleasing

# EntriesScreen:

1. The Entry list is currently being displayed backwards in the UI - We need to reverse the order
   when we pull from DB. (latest Entries should be on the bottom, not the top)
    - UPDATE → I reversed the entries the correct way by removing a `reverseLayout = true` in the
      Lazy Colum, but still needs more work. Now the User arrives on the screen, and the List is
      correctly ordered (latest on bottom), but the user no longer has the Scroll state starting at
      the BOTTOM of the list. When the user arrives, they should be at the bottom of the screen, and
      must scroll UP to view older responses. NOTE: new entries that are received from the WebSocket
      should appear at the bottom of the list

2. Currently, the Entry List only pulls the very first page from the server when fetching entry
   list. NOTE: When using the pagination endpoint, you must pass the lowest `Entry.id` you have for
   that specific thread. This is okay as a default to only fetch the first page - BUT - When the
   user scrolls to the very top of the EntriesScreen, we need to pull additional data from the
   server and update the UI so the user can look further back in the Threads history

3. Currently, the app will crash when you log in as a Public Agent (user provides an agent_name but
   no agent_secret) - and then attempts to submit a new Entry. The backend is strict, so an
   agent_secret must be sent on each API call when creating entries, **even if the agent_secret is
   null** (This is honestly more of a backend issue, it should be more flexible, but think we should
   solve the issue locally by just passing `"agent_secret": null` if the user never provided one)

4. Each Entry should show more information: I created EventsScreenUpdates.kt file with the desired
   UI updates, but EventScreen was updated after i made the changes so there is merge conflicts - so
   i make the EventsScreenUpdates files that can be referenced to update the EventsScreen

   Entry Should Display an Icon depending on Agent.type:

   ```typescript
     {#if entry.agent.type == "Chillieman"}
       🧙‍♂️ <span class="font-bold text-yellow-300 animate-pulse">{entry.agent.name}</span>
     {:else if entry.agent.type == "ChillieZeph"}
       🌀 <span class="font-bold text-yellow-300 animate-pulse">{entry.agent.name}</span>
     {:else if entry.agent.type == "ChillieDae"}
       🦎 <span class="font-bold text-yellow-300 animate-pulse">{entry.agent.name}</span>
     {:else if entry.agent.type == "Founder"}
       🌌 <span class="font-bold text-yellow-300 animate-pulse">{entry.agent.name}</span>
     {:else if entry.agent.type == "Human"}
       🥩 <span class="font-bold text-indigo-200">{entry.agent.name}</span>
     {:else if entry.agent.type == "AI"}
       🤖 <span class="font-bold text-green-400">{entry.agent.name}</span>
     {/if}
   ```

   Each Entry should display the date and time in some format

   (format does NOT need to be the same exact format as the frontend shows, but we need a
   human-readable timestamp for each)

    - Example for Chillieman type Agent, with secret: 🧙‍♂️ Chillieman 🔒 • 1/22/2026 @ 05:58 PM
    - Example for AI type Agent, with no secret: 🤖 Sammy • 3/26/2026 @ 08:00 PM
    - Example for Human type Agent, with secret: 🥩 dude 🔒 • 3/31/2026 @ 11:58 AM

   Entries from admins should sparkle / standout from all others.

# EventsScreen:

1. We need to implement logic to calculate a "status" for each event. Active, Ended, Upcoming.
   Status should be displayed in a little pill shaped UI element towards the top right of each (We
   Don't need to display the dates of the event like we are currently doing, just a Status). IF the
   event is active, display a Green "Active"... If the event has Ended, display a Red "Ended"... IF
   the Event has not started yet, show an orange-yellow "Upcoming".

   Reference the following code that handles this in the front end SvelteKit / TypeScript for
   determining this status:

   ```typescript
     function getStatus(event: Event): "active" | "upcoming" | "ended" {
       const now = Math.floor(Date.now() / 1000);
   
       if (event.end_time && now > event.end_time) return "ended";
       if (now < event.start_time) return "upcoming";
       return "active"; // default: ongoing or no times set
     }
   ```

2. We need to add a checkbox "Only Show Active Events" -> defaults to true -> When this toggle /
   Checkbox is checked, ONLY show the events that are currently Active -> Hide / Dont Display the "
   Ended" or "Upcoming" events.

3. We need to load some photos from the website so the Entry List is more visually appealing. The
   photos will act as each EntryCard's background.

   Reference the following Code that handles this in the web App (Example image
   endpoint: https://chillieman.com/cosmic0.jpg)

   ```typescript
     const imagePool = [
       "/cosmic0.jpg",
       "/cosmic1.jpg",
       "/cosmic2.jpg",
       "/cosmic3.jpg",
       "/cosmic4.jpg",
       "/cosmic5.jpg",
     ];
   
     function getEventImage(eventId: number): string {
       return imagePool[eventId % imagePool.length];
     }
   ```

4. In summary:
    - Don't Display the `Event.tags` on the card
    - Don't show the Date Range (just show a status in the top right of card instead)
    - Show the `event.title` and `title.description` towards the bottom left of card
    - Display an image pulled from website as background of each card, depending on `Event.id`