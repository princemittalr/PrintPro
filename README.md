# PrintPro — campus print shop billing

Java Hackathon, Problem 17. A menu-driven Java 17 console program that bills print jobs at the campus print shop counter, plus a web counter that runs the same rules in the browser.

**Team:** R. Ramcharan, Prince Mittal R, VinayKumar N Goudar, P. Lakshmi Ganesh Raj, B. Jaswanth Reddy

## What's here
| Path | What it is |
|---|---|
| `PrintPro/` | Java 17 console program (arrays only, 6 packages). See `PrintPro/README.md` for the syllabus map |
| `PrintPro-Web/index.html` | Web counter, one HTML file, no install |
| `PrintPro_Presentation.pptx` | 6-slide project deck |
| `PrintPro_Video.mp4` | 82-second project video |

## Run the console program
```powershell
cd PrintPro
javac -d out -sourcepath src src\com\printpro\app\PrintProApp.java
java -cp out com.printpro.app.PrintProApp
```
Or double-click `PrintPro\run.bat`. To run test cases 1 to 4 automatically: `PrintPro\run_tests.bat`.

## Features
- Service list with daily capacity and LOW / OK status
- Jobs with student (10%) or faculty (15%) discount, 18% GST, undo with `-1`, finish with `0`
- Job IDs (JOB1, JOB2...), tokens, odd tokens to Tray 1 and even to Tray 2
- Find old bills, end-of-day report, bills saved to `bills/`
- Checked exceptions for unknown services and over-capacity jobs; a rejected job never changes capacity
- Never crashes on bad input
- **Extras:** smart capacity offer, ready-by time from a shop queue, LOW-stock warning, capacity used %

## Test cases
| Case | Order | Result |
|---|---|---|
| 1 | Student: 2 × B/W Print, 1 × Lamination | ₹30.80 |
| 2 | Faculty: 3 × Colour Print, 2 × Poster Print | ₹270.81 |
| 3 | Student: 201 × Colour Print | CapacityExceededException, capacity unchanged |
| 4 | Service ID 999 | PrintServiceNotFoundException, program continues |
