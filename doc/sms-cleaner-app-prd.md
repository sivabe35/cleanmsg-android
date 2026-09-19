# CleanMsg — Product & Technical Doc (v1)

**App name:** CleanMsg
**Package:** `com.siva.cleanmsg.app`


## 1. Problem Statement
User's Android device has 10,000+ SMS messages accumulated over years. Manually selecting and deleting them one by one is impractical. There is no easy way to see messages grouped by type (Promotional, OTP, Banking, Personal, etc.) to quickly judge what's safe to bulk-delete.

## 2. Goal
Build a fast, working **Android MVP** that:
- Classifies existing SMS inbox into categories automatically.
- Lets the user review each category and bulk-delete unwanted messages.
- Is publishable on the Play Store.
- Is portfolio-ready to demo in an interview (clean architecture, sound permission handling, real problem solved).

## 3. Platform Constraint (critical — decided)
- Android restricts **SMS deletion** to whichever app is currently the **default SMS app** (OS-level restriction since KitKat, not just a Play policy).
- Reading the full existing inbox (`content://sms`) also requires `READ_SMS`, which for a general-purpose app effectively requires default-SMS-app status to pass Play Store review.
- **Decision:** App will prompt the user to set it as the default SMS app on first launch (via `RoleManager` / `Telecom` intent on modern Android). To qualify, the app implements the minimum required components:
  - A `BroadcastReceiver` for `SMS_DELIVER`
  - A basic compose/view SMS activity (can be extremely minimal)
  - Respond to the default `ACTION_SENDTO` intents
- After cleanup, user can revert to their original default SMS app (standard, Play-accepted pattern — same approach used by SMS backup/cleaner apps).

## 4. Classification Approach (decided)
**On-device, rule/regex-based** — no ML, no cloud calls. Fast, offline, private, and simple to demo/explain in an interview.

Suggested initial categories & signal sources:
| Category | Signals |
|---|---|
| OTP / Verification | Sender ID pattern (e.g. `VM-`, `AX-`, numeric short codes), keywords: "OTP", "verification code", "one time password" |
| Banking / Transactional | Known bank sender IDs (HDFC, SBI, ICICI, etc.), keywords: "debited", "credited", "account", "balance" |
| Promotional | Keywords: "offer", "discount", "sale", "% off", sender IDs starting with common promo prefixes (e.g. `AD-`, 10-digit long codes used by marketers) |
| Personal | Fallback: doesn't match any rule, sender is a normal phone number, not a registered short code |
| Uncategorized | Anything left over |

Rules should live in a config file (JSON) so they can be extended without code changes — good talking point for "extensible architecture" in the interview.

**India-specific sender ID structure (for rule design):** DLT-registered headers are 6-character alphanumeric names prefixed by a 2-letter telecom code and a dash (e.g. `VM-HDFCBK`, `AD-MYNTRA`). The `AD-` prefix is commonly used for promotional/marketing headers per TRAI rules; banking/OTP headers use other prefixes and are better matched by the entity name itself (e.g. `HDFCBK`, `ICICIB`, `SBIINB`). Pure 4–6 digit numeric senders are typically OTP short codes. Normal 10-digit numbers are personal senders.

The full built-in rule set (bank sender IDs, OTP patterns, promotional keywords) ships as `classification_rules.json` — drop it into the app's `assets/` folder as the seeded default config referenced in the rule engine notes below.

**Rule engine — built-in + custom:**
- Built-in rules ship as a default JSON config (read-only, seeded on first install).
- User-defined rules are stored in Room (editable/deletable), each with: match type (keyword / sender-ID pattern — **regex placeholder for a future version, not built in v1**), match value, target category (default or custom).
- Evaluation order: user-defined rules are checked first (so a user can override a message that would otherwise be caught by a built-in rule), then built-in rules, then fallback to "Personal"/"Uncategorized".
- Re-running classification (e.g. after adding a new rule) should be a fast, idempotent operation — only re-tag messages, don't re-scan the whole SMS provider unless the user explicitly triggers a full re-scan.

## 5. MVP Feature Scope
**In scope (v1):**
1. Onboarding: request `READ_SMS` permission → prompt to set as default SMS app → explain why.
2. Inbox scan: read all existing SMS, run classification, store metadata + body (see body-length note below) in local DB (Room).
3. Category view: tabs/list per category with count, preview of last few messages.
4. Message list per category: sender, snippet, date, select (multi-select), delete.
5. **Sorting**: sort message list by Date (newest/oldest first), Sender (A–Z), or Length. Default: newest first.
6. **Message body preview**: show up to 200 characters of the message body in the list view (truncate with "…"); full body on tap/expand.
7. **Custom categories**: user can add a new category (name + optional icon/color) beyond the default set.
8. **Custom user-defined rules**: user can define a rule (keyword match and/or sender-ID pattern) and assign it to any category (default or custom). User rules are evaluated alongside/ahead of the built-in rule set — see engine notes below.
9. Bulk delete: select-all within a category, delete with confirmation.
10. Revert default SMS app option (settings screen).

**Out of scope for v1 (future roadmap):**
- Sending/replying to messages (only minimal stub needed to qualify as default handler).
- ML-based classification.
- Cloud backup/sync.
- iOS version.

## 6. Recommended Tech Stack
Given the goal is a fast, native, Play-Store-ready MVP with deep platform permission work (SMS provider, default-app role), **native Kotlin** is recommended over React Native for this project — default SMS app registration and `content://sms` access are platform APIs best handled natively, and it keeps the codebase simpler for a portfolio project.

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Local DB:** Room (store message metadata + classification tags)
- **Architecture:** MVVM + Repository pattern
- **Async:** Kotlin Coroutines / Flow
- **Permissions:** Android runtime permissions + `RoleManager` for default SMS app request (API 29+), with fallback intent for older versions

## 7. High-Level Architecture
```
UI (Compose Screens)
   ↓
ViewModel (per screen)
   ↓
Repository (SmsRepository)
   ↓
 ┌─────────────┬───────────────┐
 SMS ContentProvider   Classification Engine (rule config)
 (read/delete)              ↓
                        Room DB (cache + tags)
```

## 8. Screens (v1)
1. **Onboarding / Permission** — explain purpose, request permission, set default SMS app.
2. **Dashboard** — category cards with counts (e.g. "Promotional: 4,213", "OTP: 1,802", "Banking: 640", "Personal: 1,120"), including any user-added categories; "+ Add Category" entry point.
3. **Category List** — messages within a category, 200-char body preview, sort control (Date / Sender / Length), multi-select, delete.
4. **Message Detail** — full body, sender, date, timestamp, quick "reclassify" action.
5. **Manage Rules** — list of custom rules, add/edit/delete a rule (match type, match value, target category).
6. **Add/Edit Category** — name, color/icon.
7. **Settings** — revert default SMS app, re-scan inbox, about/version.

## 9. Suggested Milestones (fast MVP track)
1. Project setup + permission & default-SMS-app flow working end-to-end (biggest risk item — do first).
2. SMS read + Room caching of existing inbox.
3. Rule-based classification engine + config file.
4. Dashboard + category list UI.
5. Multi-select + delete flow.
6. Polish, app icon, Play Store listing, privacy policy (required for SMS permission apps).
7. Internal testing track → closed/production release.

## 10. Play Store Notes
- Apps requesting SMS permissions must fill out the **Permissions Declaration Form** in Play Console and justify the "core functionality" (default SMS handler qualifies).
- A **privacy policy URL** is mandatory for any app touching SMS data.
- Recommend starting on the **Internal Testing** track first to validate the policy declaration before going public — Google's SMS/Call Log review can take a few days and occasionally requires a demo video showing the permission usage.

## 11. Interview Talking Points (built into the design)
- Real-world Android platform constraint (default SMS app / OS-level delete restriction) identified and solved correctly.
- Config-driven, extensible classification engine (easy to extend without touching core logic).
- Clean MVVM + Repository architecture.
- Handles a genuinely large dataset (10k+ records) — worth mentioning batching/pagination in the UI (don't load 10k rows at once; page or lazy-load per category).

## 12. Open Questions / Future Decisions
- Exact rule list per category (needs your local bank/sender IDs — India-specific short codes).
- Message body is stored locally (never transmitted) and truncated to 200 chars in list views, full body available in Message Detail — confirm this is sufficient, or if full body should also be searchable.
- Whether to add a "review before delete" undo/trash period (soft delete) vs. immediate hard delete.
- Whether custom rules should support regex, or keep to simple keyword/sender-ID matching for v1 simplicity. **Decided: v1 ships with keyword/sender-ID matching only; schema leaves a placeholder field (`matchType`) so regex can be added later without a migration.**
