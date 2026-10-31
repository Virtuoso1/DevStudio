# Home Services App (working title)

Android front end for the Home Services App: find, compare and book trusted local
electricians, plumbers and appliance technicians. Built with Kotlin and Jetpack Compose.

## Screens
- **Home**: greeting, live search, browse by trade, "Book again near you"
- **Trade list**: one consistent card pattern, colour accent per trade
- **Profile**: rating, experience, bio, credentials, BOOK and Message
- **Booking sheet**: pick a day and time, describe the issue, confirm
- **Chat**: provider header pinned while you talk
- **Bookings / Messages**: tabs in the bottom bar

## Run it
1. Open this folder in Android Studio (Koala or newer) and let Gradle sync.
2. Run on an emulator or device, Android 8.0 (API 26) or higher.

## Where things live
```
app/src/main/java/com/homeservices/app/
  data/        models, sample data (ProData), AppViewModel (in-memory bookings + chats)
  navigation/  AppNav.kt (routes + bottom bar)
  ui/theme/    colours, typography
  ui/components/ shared composables (Avatar, ProCard, RatingBadge...)
  ui/screens/  one file per screen
```

## Connecting the backend later
`ProData` and `AppViewModel` hold sample data in memory. Replace them with a repository
that calls your API; the screens only read state and call the ViewModel functions.

## Branching suggestion
`main` stays runnable. Work on `feature/<name>` branches and open pull requests.
