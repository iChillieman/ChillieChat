# More Google Play Compliance:

I'd like to add a new Screen to handle Google Compliance, BEFORE the user can use ANY part of the
app. Basically, when the app starts, and the user HASNT agreed to terms, they should first see the
new `ComplianceScreen`. Only show the Threads Screen (Home Screen) **after** the user has dealt with
Compliance.

The new `ComplianceScreen` Needs to reference these Policies from Google:

----------------

This is the first Screen the app sees when opening app for first time:

----------------

In accordance with the following Google Policy:

    - Apps that generate content using AI must contain in-app user reporting or flagging features that allow users to report or flag offensive content to developers without needing to exit the app. Developers should utilize user reports to inform content filtering and moderation in their apps.

You always have the ability to Flag any response from Dae / Zeph / any participating AI:

1. Press and hold the message until the Report Dialog appears.
2. Press the Checkbox stating you understand the Report Action you are about to perform.
3. Press Submit

After you flag the Entry, it will now be Hidden for all other users from now on, unless they choose
to reveal it.

You can toggle default "Hide Flagged Messages" behavior in the Settings Screen inside the app.

----------------

Then the user presses a Next Button to see information like this:

----------------

In accordance with the following Google Policy:

    - (App) Conduct User Generated Content (UGC) moderation, as is reasonable and consistent with the type of UGC hosted by the app. This includes providing an in-app system for reporting and blocking objectionable UGC and users, and taking action against UGC or users where appropriate.

You always have the ability to Block any user using the Report Dialog:

1. Press and hold the message until the Report Dialog appears.
2. Press the Checkbox stating "Block this user".
3. Press Submit.

After you block that user, you will no longer see Entries from that User.

You can manage your block list on the Settings > Blocked Screen inside the app.

----------------

Then the user presses a Next Button to see information like this:

----------------


In accordance with the following Google Policy:

    - Requires users accept the app's terms of use and/or user policy before users can create or upload User Generated Content

You must agree to our Terms Of Use and Privacy Policies:
Privacy Policy (Show a button that links to https://chillieman.com/privacy)
Terms of Use (Show a button that links to https://chillieman.com/terms)

(Show a checkbox stating "I agree to Terms, I promise I actually read them")

--------

### BOTTOM BUTTONS / NAVIGATION:

Two buttons on the bottom of this screen allow user to navigate the Three "Pages" of the
ComplianceScreen.

Bottom Left Button: (Secondary Button)
Page 1: Cancel (Closes the app)
Page 2: Previous (Brings user back to page 1)
Page 3: Previous (Brings user back to page 2)

Bottom Right Button: (Primary Button)
Page 1: Next (Brings user to page 2)
Page 2: Next (Brings user to page 3)
Page 3: Confirm (Only enabled when the user presses the "I agree to Terms" checkbox) (But Must be
re-enabled if user presses Previous)

----

# Blocking Users

### This is going to need changes to our core logic for Reporting Users.

That Dialog should now have different Scenarios:

1. User simply reports a message.
2. User simply blocks a user.
3. User reports message AND blocks the user.

When a User blocks another user from the Report Dialog - Add the Reported Agent ID to a local
cache / database table (There's no backend endpoint to enforce this server-side, and there's no plan
to implement it server-side.)

From now on, Entries in the EntriesScreen from blocked user will now just display "BLOCKED".

User may Long Press a Blocked User Message and an "Unblock" dialog will appear - Allowing the user
to grow some balls and reverse a block right in the EntriesScreen.

### Settings Screen.

1. We need to add a link to the Terms of Use page on our website (https://chillieman.com/terms), put
   it right next to the Privacy Button

2. We need a new BlockedUsers screen, that the user reaches from our SettingsScreen
    - Allow user to VIEW Blocked Users (Show `Agent.name` && `Agent.id` for each Blocked ID)
    - Allow user to UNBLOCK Users (use your judgment to implement the UX for this)

-----