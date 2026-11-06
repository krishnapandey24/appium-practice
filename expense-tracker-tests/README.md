# Expense Tracker: Appium tests

Starter Appium suite (Java 21, TestNG, Appium java-client 8.5.1, Page Object Model) for the [Expense Tracker practice app](../expense-tracker/README.md).

## Run it

1. Start an emulator (or connect a phone) and install the app:
   ```bash
   adb install -r ../expense-tracker/app/build/outputs/apk/debug/app-debug.apk
   ```
2. Start Appium in its own terminal. It needs the SDK path:
   ```bash
   ANDROID_HOME=$HOME/Library/Android/sdk appium
   ```
3. Run the suite:
   ```bash
   mvn test
   ```

Reports are written to `target/surefire-reports/index.html`.

## How it's organised

```
src/test/java/com/practice/expense/
  base/BaseTest.java       one Appium session per class; wipes app data before every test
  pages/                   one class per screen (ExpenseListPage, AddExpensePage, InsightsPage, ...)
  tests/                   the tests
```

- **Every test starts from an empty app.** `BaseTest` runs `mobile: clearApp` + `mobile: activateApp` before each test.
- **Test data comes from the app's Debug tools**, opened with a deep link (`deepLink("debug")`) instead of tapping through the UI.
- **Locators:** `BasePage.tag("save_button")` finds an element by its Compose test tag. See the app README for why it uses UiSelector rather than `AppiumBy.id`.
- **No `Thread.sleep`.** Every lookup goes through `waitFor(...)` (explicit wait, 10 s).

## What's covered (13 tests)

| Class | Tests |
|---|---|
| `NavigationTest` | empty state on a fresh install, bottom navigation, deep link to Debug tools |
| `AddExpenseTest` | add an expense, empty-form errors, title boundary (40 / 41 chars) |
| `ExpenseListTest` | sample data count, sort oldest first, search, edit title, swipe to delete |
| `InsightsTest` | this month's totals for the sample dataset |

All of these pass on the current app. Finding the planted bugs is your job: add tests in `tests/` that assert what the app **should** do (use the expected numbers in the app README).
