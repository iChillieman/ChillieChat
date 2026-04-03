Please create an Onboarding workflow to guide new users how to use the app.

1. When the user opens the app the first time, an Onboarding Mode is triggered/ started.
2. Most of the screen goes dim, like an overlay is being placed over the Events Screen. The Settings
   Icon on the top right is spotlighted in some way, With Text displaying "Press Settings to change
   your identity"
3. When you arrive on the Settings Screen during onboarding mode, Automatically focus on the "Agent
   Name" field, and tell the user to "Enter a name".
4. Once the user leaves focus of that Agent Name, OR hasn't typed anything in 5 seconds(but typed
   SOMETHING) → Bring the User focus to the "Login / Register Publicly button"
5. Once the User presses the Login Button, and the Agent is loaded, bring the user attention to the
   Secret (Optional) Input - Tell the User "You can also make a custom secret, like a password".
   Give focus to the Secret Input.
6. When the user enters in a secret, or 5 seconds pass - Highlight the Login / Register **privately
   ** button.
7. Once the User Presses it → Wait for the Private Agent to load
8. Finally, highlight the "Logout button" - Tell the user "If you want to go back to being
   anonymous, just logout".
9. Whether the User logs out of not, Mark Onboarding Complete.

Completely Separate Dialog Tip, kinda unrelated to official Onboarding - but still an onboarding
theme:

1. Whenever the user is arriving in the EntriesScreen → Show a Dialog saying "Press
   and Hold any message to report it". That Dialog should have a "Don't Remind Me Again checkbox",
   and a confirm button. Show this dialog every single visit until the user presses "Don't Remind me
   Again" and then confirms.