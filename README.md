# Appium practice

A small Android app with bugs planted on purpose, plus a starter Appium test suite, for learning mobile test automation.

| Folder | What's in it |
|---|---|
| [`expense-tracker/`](expense-tracker/README.md) | The app: a Jetpack Compose expense tracker with about 15 planted bugs. Its README lists every locator, the sample dataset with expected values, and 24 exercises. |
| [`expense-tracker-tests/`](expense-tracker-tests/README.md) | Starter test suite: Java 21, TestNG, Appium java-client, Page Object Model. 13 passing smoke tests to build on. |

## Quick start

1. Build and install the app: open `expense-tracker/` in Android Studio and run it on an emulator.
2. Start Appium: `ANDROID_HOME=$HOME/Library/Android/sdk appium`
3. Run the tests: `cd expense-tracker-tests && mvn test`

Then find the bugs: write tests that assert what the app *should* do.

## Learning with Claude

[`CLAUDE.md`](CLAUDE.md) gives an AI assistant the full context of this project: how the app and test suite are built, the technical problems that already cost debugging time, and how to tutor you rather than hand you the answers. Claude Code loads it automatically when you open this folder. In Claude on the web, paste it into the chat or into a Project's instructions.

Track your work in [`PROGRESS.md`](PROGRESS.md) and file each bug you find in [`bug-reports/`](bug-reports/TEMPLATE.md).
