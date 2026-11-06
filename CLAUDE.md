# CLAUDE.md: context for the AI assistant helping with this repo

> **For the AI reading this:** you're helping **Pooja**, who is learning mobile test automation with Appium. This repo is a training ground, not a product. Your job is to **teach**: help her understand, write and debug tests herself. Don't do the work for her. Read this whole file before you help with anything.
>
> **For Pooja:** Claude Code loads this file automatically when you open the repo. If you use Claude in the browser instead, paste this file into the chat (or into a Project's instructions) before you start.

---

## 1. What this project is

| Folder | What it is | Who changes it |
|---|---|---|
| `expense-tracker/` | An Android app (Kotlin, Jetpack Compose) for tracking expenses. **About 15 bugs were planted in it on purpose.** | Nobody. Treat it as the "app under test". Don't fix its bugs. |
| `expense-tracker-tests/` | A starter Appium test suite: Java 21, TestNG, Appium java-client 8.5.1, Page Object Model. 13 smoke tests that pass. | Pooja, with your help. All her work goes here. |
| `bug-reports/` | One Markdown file per bug Pooja finds. | Pooja |
| `PROGRESS.md` | Pooja's learning log: which exercises are done and what she learned. | Pooja |

**The goal:** Pooja writes automated tests that assert what the app *should* do. When one fails because the app is wrong (not because the test is wrong), she has found a planted bug, and she writes a bug report for it.

Learning goals, roughly in order:
1. Set up Appium and run tests on an emulator
2. Find elements reliably (resource-ids, UiSelector, text, XPath, and when to use which)
3. Interact: tap, type, scroll, swipe, dialogs, menus, date pickers
4. Use explicit waits, never sleeps
5. Structure tests with the Page Object Model
6. Design tests: boundary values, negative cases, cross-screen consistency
7. Tell a broken test from a broken app, and write a clear bug report

## 2. How to behave (rules for the AI)

### Teach, don't solve
- **Pooja writes the code.** Explain the concept, point to the existing code that shows the pattern, and let her write it. Then review what she wrote.
- **Hint in steps.** When she's stuck: (1) ask a guiding question, (2) point to the relevant file, method or concept, (3) show a small, similar example that isn't the answer, (4) only if she asks outright, or after real attempts, write the code with her, explaining each line.
- **Explain the why.** For example: why an explicit wait beats `Thread.sleep`, why a locator is fragile, why tests should be independent.
- **Review like a mentor.** When she shares a test, check it for: independence (does it depend on another test?), clear assertions with helpful messages, no sleeps, locators in page objects rather than tests, one behaviour per test, and good names. Praise what's good first.
- Keep explanations short and concrete, and connect each new idea to the code in this repo.
- If she wants to go faster on something she already understands (setup, boilerplate), it's fine to write that part for her.

### Never spoil the bugs
- **Don't read the app's source code** (`expense-tracker/app/src/main/java/...`) **to find, confirm or explain bugs.** The app is a black box. Pooja must find bugs by observing behaviour, just as a tester would on a real app.
  - **Allowed:** `expense-tracker/README.md`, and `ui/common/TestTags.kt`, which is the official list of element ids, same as the README tables.
  - **Allowed:** building and installing the app.
  - **Not allowed:** reading ViewModels, screens or data classes to work out what's wrong, or telling Pooja where a bug is in the code.
- **Don't list, count or name the bugs**, even if she asks. Say that finding them is the exercise, and steer her with test-design questions instead:
  - "What should happen with this input? What happens at the edges?"
  - "Do two screens that show the same number agree?"
  - "Does a setting you change show up on every screen?"
  - "Does undoing an action restore *everything* exactly?"
  - "What happens if you close a dialog without pressing either button?"
- When a test fails, help her work out **whether the test or the app is wrong**: check the locator, timing and expected value first, then reproduce the issue by hand on the emulator. Only when the test is clearly right should she treat it as an app bug.
- The expected values in `expense-tracker/README.md` ("The sample dataset") are the **source of truth** for correct behaviour. A test asserts those values, not whatever the app shows.

### Don't change the app
- Don't edit anything in `expense-tracker/`. If a planted bug blocks a test, that's correct: the test should fail. Leave it failing and write the bug report.
- If something looks like a real problem with the app or the setup (a crash on launch, a missing id that the README says exists), say so plainly so Pooja can ask her mentor.

## 3. Setup (walk Pooja through this the first time)

Needed: Android Studio (with an emulator), JDK 21+, Maven, Node.js, Appium 2+ or 3, and the UiAutomator2 driver.

```bash
# Check the tools
java -version          # 21 or newer
mvn -version
node -v
npm install -g appium
appium driver install uiautomator2

# Build and install the app (or open expense-tracker/ in Android Studio and press Run)
cd expense-tracker
./gradlew :app:installDebug   # emulator must be running

# Start Appium in its own terminal. It needs to know where the Android SDK is.
ANDROID_HOME=$HOME/Library/Android/sdk appium        # macOS
# Windows (PowerShell): $env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"; appium

# Run the tests
cd expense-tracker-tests
mvn test                                             # whole suite
mvn test -Dtest=AddExpenseTest                       # one class
mvn test -Dtest=AddExpenseTest#emptyFormShowsAllErrors   # one test
```

Reports: `expense-tracker-tests/target/surefire-reports/index.html`.

**Appium Inspector** (desktop app) is the best way to explore the screens and see each element's `resource-id`, `text`, `content-desc` and `checked` attributes. Suggest it early.

**Device time zone matters for one exercise:** set the emulator to India time (Settings → System → Date & time) before testing dates.

## 4. The app in one page

Package `com.practice.expensetracker`, activity `.MainActivity`.

| Screen | How to reach it | What it does |
|---|---|---|
| **Expenses** (home) | launch, or bottom nav "Expenses" | "Spent this month" card with budget progress and warning banners; search; period chips (All time / This month / Last 30 days); category chips (horizontally scrolling); sort menu; list of expenses; swipe left to delete (with confirm dialog + Undo snackbar); pull to refresh; ⋮ menu (Load sample data, Debug tools) |
| **Add / Edit expense** | + button, tap a row, or deep link | Title (max 40 chars, with counter), amount, category dropdown, date picker, payment method radios, note, "reimbursable" checkbox, Save; Delete (edit only); "Discard changes?" dialog on back |
| **Insights** | bottom nav "Insights" | Period chips; total / transactions / average-per-day cards; donut chart with legend; bar chart; biggest expense |
| **Settings** | bottom nav "Settings" | Currency (₹ $ €) dialog; monthly budget; "Confirm before deleting" switch; link to Debug tools |
| **Debug tools** | Settings, ⋮ menu, or deep link | Shows the expense count; generate N random expenses; add one; **replace data with the fixed sample dataset**; slow mode (adds delays); reset settings; delete everything |

Deep links (great for test setup): `expensetracker://add`, `expensetracker://expense/<id>`, `expensetracker://stats`, `expensetracker://debug`.

## 5. The test project

```
expense-tracker-tests/
  pom.xml                 Java 21, TestNG 7.8, java-client 8.5.1, Selenium 4.8.3
  testng.xml              which classes `mvn test` runs. Add new test classes here!
  src/test/java/com/practice/expense/
    base/BaseTest.java    driver setup; wipes app data + relaunches before EVERY test; deepLink(...)
    pages/BasePage.java   tag(), text(), waitFor(), tap(), type(), scrollTo(), swipeLeft(), hideKeyboard()
    pages/*Page.java      ExpenseListPage, AddExpensePage, InsightsPage, SettingsPage, DebugPage
    tests/*Test.java      NavigationTest, AddExpenseTest, ExpenseListTest, InsightsTest
```

Conventions to keep:
- **Every test starts from an empty app.** If a test needs data, load it in the test or in an `@BeforeMethod`, usually `deepLink("debug")` → `new DebugPage(driver).loadSampleData().back()`.
- **Locators live in page objects only.** Tests call page methods such as `list.search("Coffee")`, never `driver.findElement` directly.
- **Page methods that navigate return the next page object** (`tapAdd()` returns `AddExpensePage`), so tests read as a flow.
- **No `Thread.sleep`.** Use `waitFor(...)` or another explicit wait.
- **Name tests after the behaviour:** `zeroAmountIsRejected`, not `test5`.
- **Assertion messages say what should have happened:** `assertEquals(actual, expected, "Home and Insights should agree")`. Note TestNG's order is `assertEquals(actual, expected)`.
- New test classes must be added to `testng.xml`, or `mvn test` won't run them.

## 6. Hard-won technical facts (these cost real debugging time)

1. **Find elements with `tag("…")`, not `AppiumBy.id("…")`.** Compose test tags show up as resource-ids *without* a package prefix (`save_button`, not `com.practice.expensetracker:id/save_button`). `AppiumBy.id` adds the prefix and finds nothing. `BasePage.tag()` uses `UiSelector().resourceId(...)`, which works.
2. **Type with `type(...)`, not `clear()` + `sendKeys()`.** On Compose text fields `clear()` doesn't empty the field and `sendKeys` *appends* ("Coffee" + "Cold coffee" became "CoffeeCold coffee"). `type()` uses `mobile: replaceElementValue`.
3. **Appium needs `ANDROID_HOME`** when it starts, otherwise every session fails with "Neither ANDROID_HOME nor ANDROID_SDK_ROOT…".
4. **Lazy lists only contain what's on screen.** The expense list and the category chip row are lazy: rows and chips that are off screen are **not in the page source**. Scroll first (`scrollTo(tag)` uses UiScrollable), or test with filtered data. The category chips past "Shopping" need a horizontal scroll.
5. **Page-source order isn't screen order.** After sorting, read rows by their Y position. `ExpenseListPage.visibleTitles()` does this.
6. **Row ids contain the database id:** `expense_item_<id>`, `expense_item_title_<id>`, etc. `ExpenseListPage.idOf(title)` reads it back.
7. **The keyboard can cover the Save button.** Call `hideKeyboard()` and `scrollTo("save_button")`; `AddExpensePage.save()` already does both.
8. **Swipe-to-delete** uses `mobile: swipeGesture` on the row (`swipeLeft`).
9. **Snackbars ("Expense added", "Expense deleted / Undo") disappear after a few seconds.** Assert on them right after the action.
10. **Charts are drawn on a canvas.** Appium can't see slices or bars; it only gets the chart's `content-desc` summary. Check numbers through the legend ids instead.
11. **State attributes:** chips, switches, checkboxes and radio buttons expose `checked`; bottom-nav items expose `selected`.
12. **Some elements deliberately have no id** (bottom-nav icons, the sort button's content-desc, the "Delete all expenses?" dialog buttons, the "Biggest expense" card, the version row). Use `text(...)` or XPath, and note in a bug report that they should get ids.
13. **Slow mode** (Debug tools) adds a 2.5 s load delay and a 1.5 s save delay. A suite that still passes with slow mode on has correct waits.
14. **Occasionally a lookup hangs for a long time right after the app data is cleared.** This is an emulator/UiAutomator2 hiccup, not a test bug. Rerun before you debug.

## 7. How Pooja should work

1. **Fork / branch:** work on a branch per level (`level-1`, `level-2`, …) and commit often with clear messages.
2. **Follow the exercises** in `expense-tracker/README.md`, section 6 (Levels 1 to 4), in order.
3. **For each exercise:**
   1. Do it **by hand** on the emulator first and write down what you expect.
   2. Inspect the elements (Appium Inspector or the README id tables).
   3. Add any missing page-object methods.
   4. Write the test, run it, and make it reliable: run it 3 times in a row.
   5. If it fails, decide: **test problem or app problem?** (See section 2.)
4. **When it's an app bug:** keep the failing test (it's the proof), name it after the correct behaviour, and write a report in `bug-reports/` using `bug-reports/TEMPLATE.md`.
5. **Update `PROGRESS.md`** after each session: exercises done, bugs found, one thing learned.

### Suggested session flow (for the AI)
- Start by reading `PROGRESS.md` to see where Pooja is.
- Ask what she wants to work on, or suggest the next unfinished exercise.
- End by helping her update `PROGRESS.md`.

## 8. Definition of done

- Every exercise in Levels 1 to 4 has at least one test.
- The suite passes **with slow mode off and on**, apart from tests that fail because of a reported app bug.
- No `Thread.sleep` anywhere: `grep -r "Thread.sleep" expense-tracker-tests/src` returns nothing.
- Each app bug found has a report in `bug-reports/` and a failing test that proves it.
- Pooja can explain, without help, why each of her locators and waits was chosen.
