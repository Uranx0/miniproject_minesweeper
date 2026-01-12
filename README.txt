================================================================================
                                 MINESWEEPER
================================================================================

A modern, dark-themed clone of the classic Minesweeper game built with Java Swing.
This project features a object-oriented design, custom game mechanics,
persistent high scores, and comprehensive unit tests.

================================================================================
                                   FEATURES
================================================================================

[ GAMEPLAY ]
* Classic Mechanics: Left-click to reveal, Right-click to flag.
* First Click Safe: The first click is guaranteed to be safe and opens an area.
* Smart Revealing: Click a revealed number to auto-reveal neighbors if the correct
  number of flags are placed.
* Smart Disabling: Cells automatically disable visually when their logic is
  complete (e.g., a "1" surrounded by 1 flag and 7 revealed cells), reducing
  visual clutter.

[ UI / UX ]
* Dark Mode: Fully custom dark theme with neon-pastel text colors for readability.
* Custom Dialogs: High Score and Game Over screens match the dark aesthetic
  (no standard white JOptionPanes).
* Responsive Feedback: Buttons simulate a "pressed" state without flashing blue.

[ DIFFICULTY ]
* Presets:
  - Easy:   9x9   (10 Mines)
  - Medium: 16x16 (40 Mines)
  - Hard:   16x30 (99 Mines)
* Custom: Configure your own Board Size (up to 16x30) and Mine Count.

[ PERSISTENCE ]
* High Scores: Saves top times for Easy, Medium, and Hard difficulties.
* Single File: All scores are stored in a managed `minesweeper_scores.dat` file.
* Limits: Automatically keeps only the top 10 scores per difficulty.

================================================================================
                                  CONTROLS
================================================================================

* Left Click: Reveal a cell.
* Right Click: Flag/Unflag a potential mine.
* Left Click on Number: (Reveal valid neighbors) if flags match the number.
* (Menu): New Game / Reset the current game / Exit the app.

================================================================================