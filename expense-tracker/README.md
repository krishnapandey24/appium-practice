# Expense Tracker: an Appium practice app

A small Android app (Jetpack Compose) for practising Appium automation. You can add, edit, delete, search, filter and sort expenses. It also has an Insights screen with charts, a Settings screen and a Debug tools screen for test data.

**The app has bugs in it on purpose.** Your job is to write automated tests that catch them. There are about 15, spread across every screen. Some are obvious and some only show up when you compare two screens or change the device setup.

---

## 1. Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

To build it yourself, open this folder in Android Studio and run the `app` configuration.

| | |
|---|---|
| Package | `com.practice.expensetracker` |
| Activity | `.MainActivity` |
| Min Android | 8.0 (API 26) |

## 2. Appium setup (Java + TestNG)

```java
UiAutomator2Options options = new UiAutomator2Options()
        .setPlatformName("Android")
        .setAutomationName("UiAutomator2")
        .setAppPackage("com.practice.expensetracker")
        .setAppActivity(".MainActivity")
        .setNoReset(false);          // fresh app data for each session

AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
```

## 3. How to find elements

This is a Compose app. Compose has no XML layout ids, so the app exposes **test tags as resource-ids** instead. Almost every element you need has one.

The ids have **no package prefix**: the resource-id is `add_expense_fab`, not `com.practice.expensetracker:id/add_expense_fab`. `AppiumBy.id(...)` adds that prefix, so it finds nothing. Match the raw resource-id with UiSelector instead:

```java
By fab = AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"add_expense_fab\")");
driver.findElement(fab).click();
```

**Typing into text fields:** on Compose text fields, `clear()` doesn't empty the field and `sendKeys()` adds to what's already there. To replace a value, use:

```java
driver.executeScript("mobile: replaceElementValue", Map.of(
        "elementId", ((RemoteWebElement) field).getId(),
        "text", "Coffee"));
```

**Starting Appium:** the server needs to know where the Android SDK is:

```bash
ANDROID_HOME=$HOME/Library/Android/sdk appium
```

The full list of ids is in [`TestTags.kt`](app/src/main/java/com/practice/expensetracker/ui/common/TestTags.kt). Here are the main ones:

### Expenses screen (home)
| Element | id |
|---|---|
| Add button (FAB) | `add_expense_fab` |
| "Spent this month" amount | `spent_amount` |
| Budget text / percent / progress bar | `budget_text`, `budget_percent`, `budget_progress` |
| Budget warning / over-budget banners | `budget_warning_banner`, `budget_over_banner` |
| Search box / clear search | `search_field`, `search_clear_button` |
| Period chips | `period_chip_ALL`, `period_chip_THIS_MONTH`, `period_chip_LAST_30_DAYS` |
| Category chips | `filter_chip_FOOD`, `filter_chip_TRANSPORT`, … `filter_chip_OTHER` |
| Sort button / options | `sort_button`, `sort_option_DATE_DESC`, `sort_option_DATE_ASC`, `sort_option_AMOUNT_DESC`, `sort_option_AMOUNT_ASC` |
| "Showing N expenses" | `expense_count_text` |
| The list | `expense_list` |
| One row (by database id) | `expense_item_<id>` |
| Row parts | `expense_item_title_<id>`, `expense_item_amount_<id>`, `expense_item_category_<id>`, `expense_item_date_<id>`, `expense_item_reimbursable_<id>` |
| Loading spinner | `loading_indicator` |
| Empty states | `empty_state`, `empty_state_add_button`, `empty_filter_state`, `clear_filters_button` |
| Delete dialog | `delete_dialog`, `delete_dialog_confirm`, `delete_dialog_cancel` |
| Overflow menu (⋮) | `overflow_menu_button`, `menu_load_sample`, `menu_debug` |
| Snackbar | `snackbar`, `snackbar_message`, `snackbar_action` |
| Bottom navigation | `nav_expenses`, `nav_stats`, `nav_settings` |

> Tip: to get rows without knowing their ids, use a prefix match:
> `AppiumBy.androidUIAutomator("new UiSelector().resourceIdMatches(\"expense_item_title_.*\")")`

### Add / Edit screen
| Element | id |
|---|---|
| Title / counter / error | `input_title`, `input_title_counter`, `error_title` |
| Amount / error | `input_amount`, `error_amount` |
| Category field / chosen value / error | `input_category`, `input_category_value`, `error_category` |
| Category menu options | `category_option_FOOD` … `category_option_OTHER` |
| Date field / shown value | `input_date`, `input_date_value` |
| Date picker | `date_picker_dialog`, `date_picker_ok`, `date_picker_cancel` |
| Payment method radios | `payment_option_CASH`, `payment_option_CARD`, `payment_option_UPI` |
| Note | `input_note` |
| Reimbursable checkbox | `checkbox_reimbursable` |
| Save button / saving spinner | `save_button`, `save_progress` |
| Delete (edit only) / Back | `delete_button`, `back_button` |
| Discard-changes dialog | `discard_dialog`, `discard_confirm`, `discard_cancel` |

### Insights screen
| Element | id |
|---|---|
| Period chips | `stats_period_THIS_MONTH`, `stats_period_LAST_6_MONTHS`, `stats_period_THIS_YEAR` |
| Total / count / average | `stats_total`, `stats_count`, `stats_avg_per_day` |
| Charts | `pie_chart`, `bar_chart` |
| Legend rows | `legend_item_<CATEGORY>`, `legend_amount_<CATEGORY>`, `legend_percent_<CATEGORY>` |
| Empty | `stats_empty` |

### Settings and Debug tools
| Element | id |
|---|---|
| Currency row / value / dialog / options | `settings_currency`, `settings_currency_value`, `currency_dialog`, `currency_option_INR` / `_USD` / `_EUR` |
| Budget input / save / error | `settings_budget_input`, `settings_budget_save`, `settings_budget_error` |
| Confirm-delete switch | `settings_confirm_delete_switch` |
| Open debug tools | `settings_open_debug` |
| Expense count / status message | `debug_expense_count`, `debug_status_text` |
| Generate N | `debug_count_input`, `debug_generate_button` |
| Add 1 random / load sample dataset | `debug_add_random`, `debug_load_sample` |
| Slow mode switch | `debug_slow_mode_switch` |
| Reset settings / delete all | `debug_reset_settings`, `debug_clear_all` |

## 4. Things that will trip you up (on purpose)

These show up in most real apps, so the practice app has them too:

1. **Some elements have no id.** These include the bottom-nav icons, the sort button's accessibility id (content-desc), the "Biggest expense" card on Insights, the app version in Settings, and both buttons in the "Delete all expenses?" dialog. Find them by text or with XPath, and note in your report which ones should get ids.
2. **Off-screen chips don't exist yet.** The category chips are in a horizontally scrolling lazy row. The last few (Health, Education, Other) are missing from the page source until you scroll the row.
3. **Long lists are lazy too.** After generating 100 expenses, only the visible rows exist in the page source. Scroll before searching for a row.
4. **Charts are pictures.** The pie and bar charts are drawn on a canvas. Appium can't see the slices or bars, only a summary in the chart's `content-desc`. Check the numbers through the legend instead.
5. **Slow mode.** Turn it on in Debug tools. Loading the list then takes 2.5 s and saving takes 1.5 s. Tests that use `Thread.sleep` or skip waits will start to fail. Use explicit waits (`WebDriverWait` + `ExpectedConditions`).
6. **Snackbars disappear.** "Expense added" and "Expense deleted / Undo" go away after a few seconds, so assert on them quickly.
7. **Gestures.** You can delete a row by swiping it left (`mobile: swipeGesture`). Pulling down on the list refreshes it.
8. **The keyboard can cover the Save button.** On small screens you may need to hide the keyboard or scroll.
9. **Date picker.** It's a full calendar dialog. Practise picking a specific date, and check the date shown on the form afterwards.

### Deep links (shortcuts for test setup)
```java
driver.executeScript("mobile: deepLink", Map.of(
        "url", "expensetracker://add",
        "package", "com.practice.expensetracker"));
```
| URL | Opens |
|---|---|
| `expensetracker://add` | Add expense |
| `expensetracker://expense/<id>` | Edit that expense |
| `expensetracker://stats` | Insights |
| `expensetracker://debug` | Debug tools |

### Resetting state between tests
- **UI:** Debug tools → "Replace with sample dataset", "Reset settings to defaults", "Delete all expenses".
- **Fastest:** clear app data with `driver.executeScript("mobile: clearApp", Map.of("appId", "com.practice.expensetracker"))`, then relaunch.

## 5. The sample dataset (your expected results)

Debug tools → **Replace with sample dataset** loads 16 fixed expenses. Dates are relative to the current month, so the numbers below hold whenever you run it. **These are the correct values.** If the app shows something different, you've found a bug.

| Title | Amount | Category | Payment | Date |
|---|---:|---|---|---|
| Groceries - BigBasket | 1,250.00 | Food | UPI | this month, day 2 |
| Metro card recharge | 500.00 | Transport | Card | this month, day 3 |
| Electricity bill | 2,340.50 | Bills | Card | this month, day 5 |
| Lunch with team | 860.00 | Food | Cash | this month, day 6, reimbursable |
| Movie tickets | 640.00 | Entertainment | UPI | this month, day 8 |
| Uber to airport | 1,120.00 | Transport | UPI | this month, day 10, reimbursable |
| Pharmacy | 315.75 | Health | Cash | this month, day 12 |
| Coffee | 180.00 | Food | Card | this month, day 14 |
| New headphones | 2,999.00 | Shopping | Card | this month, day 15 |
| Internet bill | 999.00 | Bills | Card | this month, day 18 |
| Gym membership | 1,500.00 | Health | UPI | this month, day 20 |
| Dinner | 95.00 | Food | Cash | this month, **last day** |
| Rent | 15,000.00 | Bills | Card | last month, day 1 |
| Books | 650.00 | Education | UPI | last month, day 9 |
| Petrol | 2,200.00 | Transport | Card | 2 months ago, day 11 |
| Birthday gift | 1,800.00 | Shopping | UPI | 2 months ago, day 22 |

**Expected numbers (default settings: ₹, budget ₹20,000):**
- Spent this month: **₹12,799.25** across **12** expenses
- All-time total: **₹32,449.25** across **16** expenses
- This month by category: Bills ₹3,339.50 · Shopping ₹2,999.00 · Food ₹2,385.00 · Health ₹1,815.75 · Transport ₹1,620.00 · Entertainment ₹640.00
- Category percentages should add up to **100%**
- Sorted by amount (high to low), the list should start with Rent, New headphones, Electricity bill

## 6. Exercises

Work through the levels in order. Use the **Page Object Model** from Level 2 onwards: one class per screen (`ExpenseListPage`, `AddExpensePage`, `InsightsPage`, `SettingsPage`, `DebugPage`).

### Level 1: basics
1. Launch the app and assert the title is "Expenses" and the empty state is shown.
2. Add an expense (title, amount, category, payment method) and assert it appears in the list with the right amount.
3. Assert the "Expense added" snackbar appears.
4. Try to save an empty form and assert all three error messages appear.
5. Navigate to every bottom-nav tab and assert each screen title.

### Level 2: forms and validation
6. Write **boundary tests** for the title (39, 40 and 41 characters).
7. Write tests for invalid amounts: empty, `0`, `1.2.3`, `1000001`. Think about what each one *should* do.
8. Edit an expense, change it, save, and check the list shows the change.
9. Edit an expense, change something, press back, and check the "Discard changes?" dialog appears. Try changing each field separately.
10. Pick a specific date with the date picker and check the date on the form and in the list.

### Level 3: lists, filters and gestures
11. Load the sample dataset and assert the "Spent this month" amount and the count.
12. Search for `coffee`, `Coffee` and `bill`. What should each one return?
13. Test every sort option against the dataset table above.
14. Select two category chips together. What should the list show?
15. Delete a row by swiping it and by using the edit screen. Test the dialog's Cancel button, the Delete button, and tapping outside the dialog.
16. Turn off "Confirm before deleting" in Settings, swipe-delete a row, tap **Undo**, and check the restored row matches the original exactly (every field).
17. Generate 50 expenses from Debug tools and assert the count goes up by 50.

### Level 4: cross-screen checks, waits and setup
18. Compare "Spent this month" on the home screen with "Total spent" on Insights (This month). They should match.
19. Check that the legend percentages add up to 100.
20. Set the budget to ₹12,000 with the sample data loaded. Which banner should show?
21. Open Insights, then switch currency to USD in Settings, then go back to Insights. Every amount should show `$`.
22. Turn on slow mode and make your suite pass with **no `Thread.sleep` anywhere**.
23. Set up every test with deep links + `mobile: clearApp` instead of tapping through the UI.
24. Change the device time zone to India (Settings → System → Date & time) and run test 10 again.

## 7. Reporting a bug

For each bug, file a report with:
- **Title:** one line, e.g. "Search is case-sensitive"
- **Steps to reproduce:** numbered
- **Expected** vs **Actual**
- **Test:** the name of the automated test that catches it
- **Severity:** High / Medium / Low

Good luck!
