# PrintPro — Campus Print Shop Billing (Java 17 console)

## Run
- **Windows:** double-click `run.bat` (or run it in cmd). Needs JDK 17+ (`javac -version`).
- **Auto-test cases 1–4:** double-click `run_tests.bat`.
- **IntelliJ / Eclipse / VS Code:** open the folder, mark `src` as the source root, run `com.printpro.app.PrintProApp`.
- Bills are also saved to `bills/JOB1.txt`, `bills/JOB2.txt`… and `bills/DAY_REPORT.txt` on exit.

## Project structure
```
src/com/printpro/
├── app/        PrintProApp (main, menu)   PrintShop (services[], jobs[], validation, report)
├── model/      PrintService   JobItem   PrintJob (implements Billable)
├── customer/   Person -> abstract Customer -> Student / Faculty
├── billing/    Billable (interface)
├── exception/  PrintServiceNotFoundException   CapacityExceededException  (checked)
└── util/       ItemStack (own array stack for undo)   InputReader (crash-proof input)
```

## Where each syllabus item is (show the mentor)
| Unit | Requirement | File → where |
|---|---|---|
| I | `PrintService[]`, `PrintJob[]` | `PrintShop` fields `services`, `jobs` |
| I | int, double, char, boolean | `PrintService.id/price`, `Customer.type` (char), `finished` (boolean) in `PrintProApp.newJob` |
| I | `final double GST = 0.18` | `PrintJob.GST` |
| I | type casting | `(int) (getDiscountRate() * 100)` in `Customer.toString` |
| II | do-while + switch | `PrintProApp.run()` |
| II | `?:` LOW/OK | `PrintService.getStatus()` |
| II | `token & 1` | `PrintJob.getTray()` |
| III | constructors + `this` | every model class; `ItemStack()` chains with `this(10)` |
| III | overloaded method | `InputReader.readInt(prompt)` and `readInt(prompt, min, max)` |
| III | static job counter | `PrintJob.jobCounter` → JOB1, JOB2… |
| III | recursive subtotal | `PrintJob.calculateSubtotal(int index)` |
| III | own stack for undo (-1) | `ItemStack` push/pop/peek; used in `PrintProApp.undoLast` |
| IV | packages | 6 packages above |
| IV | Person → abstract Customer → Student/Faculty, `super` | `customer` package |
| IV | overridden `getDiscountRate()` | `Student` 0.10, `Faculty` 0.15 |
| IV | interface | `Billable`, implemented by `PrintJob` |
| IV | final method | `Customer.calculateDiscount()` |
| IV | `toString()` | Person, Customer, PrintService, JobItem, PrintJob |
| V | checked exceptions | `exception` package (both extend `Exception`) |
| V | `throw` / `throws` | `PrintShop.findService`, `checkCapacity`, `acceptJob` |
| V | multiple catch + nested try | `PrintProApp.newJob()` |
| V | finally | `PrintProApp.newJob()` and `run()` |
| V | try-with-resources | `PrintProApp.main` (Scanner), `PrintShop.saveBill` (PrintWriter) |
| — | arrays only | no ArrayList / HashMap used anywhere (only mentioned in comments) |

## Test cases (verified)
| # | Input | Output |
|---|---|---|
| 1 | Student: 2 × 101, 1 × 105 | 29.00 → −2.90 → 26.10 → +4.70 GST = **Rs. 30.80** |
| 2 | Faculty: 3 × 102, 2 × 106 | 270.00 → −40.50 → 229.50 → +41.31 GST = **Rs. 270.81** |
| 3 | Student: 201 × 102 | `CapacityExceededException`, capacity stays 200 |
| 4 | service id 999 | `PrintServiceNotFoundException`, program continues |

## What makes PrintPro different (innovation)
| Idea | What it does | Where |
|---|---|---|
| **Smart capacity offer** | When an order is too big, PrintPro doesn't only say no: it offers what the shop can still do today ("Only 197 left. Add 197 instead?") | `PrintProApp.offerWhatIsLeft` |
| **Ready-by time** | Each service has minutes per unit; jobs queue one after another, so every bill says when the prints will be ready, fixing the deadline rush | `PrintShop.acceptJob`, `workMinutes`, `queueMinutes`; bill line "Ready by" |
| **LOW-stock warning** | Before billing, warns if this job will push a service below 10 left for the day | `PrintProApp.warnLowStock`, `PrintService.wouldTurnLow` |
| **Capacity used %** | Day report shows how much of each service's daily capacity was used | `PrintService.getUsedPercent` |
| **All-or-nothing jobs** | Validates every line first, deducts only if all pass, so a rejected job never changes capacity | `PrintShop.acceptJob` |
| **Bills saved to files** | `bills/JOB1.txt` and `bills/DAY_REPORT.txt` | `PrintShop.saveBill`, `saveReport` |
| **Web counter** | Same rules in the browser, with a receipt-printer animation and one-click test runner | `../PrintPro-Web/index.html` |

## Key design point: "a rejected job must not change any capacity"
Two layers:
1. Each line is checked as it's entered (quantity already in the cart + new quantity ≤ remaining).
2. `PrintShop.acceptJob()` is **all-or-nothing**: pass 1 checks every service, pass 2 deducts only if all passed.

## Checkpoint explain notes
- **& vs &&**: `&` is bitwise on ints (`token & 1` reads the last bit: 1 = odd) and non-short-circuit on booleans; `&&` is logical and short-circuits (skips the right side if left is false).
- **throw vs throws**: `throw` raises an exception object; `throws` in a method signature declares it might be raised.
- **Checked vs unchecked**: checked (extends `Exception`) must be caught or declared — compiler enforces it.
- **abstract class vs interface**: abstract class can hold state + constructors (Customer has `name`, `type`); interface is a pure contract (Billable).
- **final method**: can't be overridden — discount formula is fixed, only the rate varies.
- **static**: one `jobCounter` shared by all PrintJob objects, so ids never repeat.
- **Recursion**: `subtotal(i) = line[i] + subtotal(i+1)`, base case `i == length → 0`.
- **Stack (LIFO)**: last service added is the first removed on `-1`.

## Likely "surprise change" and where to edit (CP4, without AI)
| Change | Edit |
|---|---|
| New customer type (e.g. Staff 5%) | new `Staff extends Customer`; add `'T'` in `readChoice` and the ternary in `newJob` |
| Change GST / discount | `PrintJob.GST`, `Student.STUDENT_DISCOUNT`, `Faculty.FACULTY_DISCOUNT` |
| New service | add a row in `PrintShop` constructor |
| LOW threshold | `PrintService.LOW_LIMIT` |
| 3 trays | `getTray()`: `return token % 3 + 1;` |
| Bulk discount (e.g. subtotal > 500) | in `PrintJob.getDiscountAmount()` |
