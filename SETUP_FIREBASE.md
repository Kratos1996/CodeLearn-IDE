# Firebase Setup Guide

## Step 1: Create Firebase Project
1. Go to https://console.firebase.google.com
2. Click "Add project" → name it "CodeLearnIDE"
3. Enable Google Analytics (optional)

## Step 2: Add Android App
1. Click the Android icon (➕ Add app)
2. Package name: `com.codelearn.ide`
3. Download `google-services.json`
4. Place it at: `composeApp/google-services.json`

## Step 3: Enable Authentication
1. Firebase Console → Authentication → Get Started
2. Enable **Email/Password** provider

## Step 4: Enable Realtime Database
1. Firebase Console → Realtime Database → Create Database
2. Choose a region (e.g. asia-southeast1 for India)
3. Start in **test mode** (you can add security rules later)
4. Copy your database URL (looks like: `https://YOUR_PROJECT-default-rtdb.REGION.firebasedatabase.app`)

## Step 5: Set Firebase Config
Open this file and fill in your values:
`composeApp/src/commonMain/kotlin/com/codelearn/ide/firebase/FirebaseService.kt`

```kotlin
object FirebaseConfig {
    const val API_KEY    = "YOUR_API_KEY"          // From Project Settings → Web API Key
    const val PROJECT_ID = "YOUR_PROJECT_ID"       // From Project Settings
    const val DB_URL     = "https://YOUR_PROJECT-default-rtdb.REGION.firebasedatabase.app"
}
```

Where to find API_KEY:
- Firebase Console → Project Settings (gear icon) → General → Web API Key

## Step 6: Database Security Rules (after testing)
In Firebase Console → Realtime Database → Rules, paste:
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "chat": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "challenges": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "leaderboard": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

## Step 7: Build & Run
```bash
# Android
./gradlew :composeApp:assembleDebug

# Desktop
./gradlew :composeApp:run
```

## Notes
- The app works fully OFFLINE without Firebase (all course content is built-in)
- Firebase only syncs: user profile, course progress, badges, chat, challenges
- Guest mode available: tap "Continue as Guest" on sign-in screen
