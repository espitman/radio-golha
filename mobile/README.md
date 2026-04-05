# RadioGolha Mobile

Initial Kotlin Multiplatform mobile scaffold for the RadioGolha project.

Current scope:

- `composeApp/` KMP app module
- Android launcher activity with a Hello World screen
- iOS targets declared so shared code can grow toward a phone app on both platforms

To run it locally once Java and Android tooling are installed:

```bash
cd mobile
./gradlew :composeApp:assembleDebug
```

For Android Studio:

1. Open `mobile/`
2. Let Gradle sync
3. Run the `composeApp` Android configuration
