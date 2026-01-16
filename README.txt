================================================================================
                                 MINESWEEPER
================================================================================

A modern, dark-themed clone of the classic Minesweeper game built with Java Swing.
This project features a object-oriented design, custom game mechanics,
persistent data management, and comprehensive unit tests.

================================================================================
                                   FEATURES
================================================================================

[ GAMEPLAY ]
* Classic Mechanics: Left-click to reveal, Right-click to flag.
* First Click Safe: The first click is guaranteed to be safe and opens an area.
* Smart Revealing: Click a revealed number to auto-reveal neighbors if the correct
  number of flags are placed.

[ VISUALS & THEME ]
* Dark Mode: A custom dark gray theme with neon/pastel text colors for readability.
* Smart Disabling: Cells automatically disable visually when their logic is
  complete (e.g., a "1" surrounded by 1 flag and 7 revealed cells), reducing
  visual clutter.
* Aspect Ratio Locking: The game grid maintains perfect square cells regardless
  of how the window is resized.
* Custom Dialogs: High Score and Game Over screens match the dark aesthetic
  (no standard white JOptionPanes).
* Responsive Feedback: Buttons simulate a "pressed" state without flashing blue.

[ DIFFICULTY ]
* Presets:
  - Easy:   9x9   (10 Mines)
  - Medium: 16x16 (40 Mines)
  - Hard:   16x30 (99 Mines)
* Custom: Configure your own Board Size (up to 30x60) and custom Mine Count.

[ PERSISTENCE ]
* Save Settings: The game remembers your last used difficulty, custom values,
  and "Practice Mode" toggle (using Java Preferences).
* High Scores: Saves top times for Easy, Medium, and Hard difficulties.
* Single File Storage: All scores are stored in a managed `minesweeper_scores.txt` file.

================================================================================
                                  CONTROLS
================================================================================

* Left Click: Reveal a cell.
* Right Click: Flag/Unflag a potential mine.
* Left Click on Number: (Reveal valid neighbors) if flags match the number.
* (Menu): New Game / Reset the current game / Exit the app.
* Resize Window: The board automatically centers and scales while keeping cells square.
* R_key & N_key keybinds for resetting and creating a new game respectively

================================================================================