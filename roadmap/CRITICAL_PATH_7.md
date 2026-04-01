# Critical Path 7: Further Polish


Current Issues:
1. When the Soft Keyboard is open on the EntriesScreen, the User cannot scroll up to see the Entry
   at the very top.
2. The background of Threads in the ThreadScreen isnt very different from the screen background
   color (in both light/dark mode) - so the Thread doesnt really stand out on the screen. make it
   pop ✨
3. The color of agent.type Human - The agent name - is hard to see in Light Mode
4. Currently, the EntriesScreen does not properly update in real time using the websocket. User must
   navigate away from the screen and return to see any Entry that was submitted by someone else.
5. The Scrolling of EntriesScreen is not working properly when the user scrolls all the way to the
   top to load older messages. Each time a new chunk of data is loaded, the user is brought all the
   way to the bottom of the list. When more data is loaded, the user should not lose their scroll
   position