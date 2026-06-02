# Cash Entry Manager

An offline Android Kotlin app for tracking daily income and expenses.

## Features
- Complete Offline Data Storage (JSON via SharedPreferences)
- Clean, Modern Material 3 UI with Green/White theme
- Add, Edit, Delete Transactions
- Custom Ledgers (Cashbooks)
- PDF & Excel CSV Exports
- Comprehensive Dashboard & Reporting View

## Technical details
- Target SDK: 36
- Minimum SDK: 24
- Libraries: Jetpack Compose, Moshi (JSON Serialization)
- **No Room DB, No Firebase, No SQL used.**

## Installation
The generated APK can be found under the `APK_DOWNLOAD` or `.build-outputs` directory, named `app-debug.apk`. 

Send it to your Android device and open it to install. 

## Re-building
Run `./gradlew assembleDebug` to rebuild the APK from source.
