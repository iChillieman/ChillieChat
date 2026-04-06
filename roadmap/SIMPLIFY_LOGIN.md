Can you please take a look at the codebase and see if you can reduce the complexity of logging in (
from the users prospective)

- Right now there are two buttons that handle the Login - First button calls an endpoint that is
  specific for Public Login (No secret/password) - and a second button is specific for Private
  Login (user included a secret/password)

But I think we should make this simpler for the user- and just have a SINGLE login Button - and the
app will look at the Secret Input - If its empty / blank - then call the Public Login endpoint. If
the user included a secret - call the private login endpoint.

This change will also require an update to our Onboarding Workflow - Specifically - The
`OnbaordingStep.HIGHLIGHT_PUBLIC_LOGIN` and `HIGHLIGHT_PRIVATE_LOGIN`

I think the best way to accomplish this would be to Remove the HIGHLIGHT_PUBLIC_LOGIN step → And
have the user prompted to enter Agent Name / Add a Secret / Then highlight the One and Only Login /
Register button

Steps like this make the most sense to me - But Please just use your judgment on how to implement
this change!

```
enum class OnboardingStep {
    SPOTLIGHT_SETTINGS,
    FOCUS_AGENT_NAME,
    FOCUS_SECRET,
    HIGHLIGHT_LOGIN,
    WAIT_LOGIN,
    HIGHLIGHT_LOGOUT,
    COMPLETED
}
```

Some good starting points:

- chilliechat.presentation.ui.screens.settings.SettingsScreen
- SettingsViewModel
- chilliechat.presentation.onboarding.OnboardingManager