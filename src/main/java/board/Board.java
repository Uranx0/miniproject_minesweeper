package board;

import exceptions.GameOverException;
import exceptions.InvalidGameConfigurationException;
import field.Field;
import field.MineField;
import field.SafeField;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class Board {
    //Map with board state
    private final Map<Point, Field> grid = new HashMap<>();
    private final int rows;
    private final int columns;
    private final int mineAmount;

    public Board(int rows, int columns, int mineAmount) {
        if (rows < 5 || columns < 5 || mineAmount >= rows * columns) {
            throw new InvalidGameConfigurationException("Invalid size or mineAmount");
        }
        this.rows = rows;
        this.columns = columns;
        this.mineAmount = mineAmount;
        initializeEmptyBoard();
    }

    // Create board filled with SafeFields
    private void initializeEmptyBoard() {
        // Clearing the grid
        grid.clear();

        // Filling the grid with SafeField
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                grid.put(new Point(i, j), new SafeField());
            }
        }
    }

    //TODO
    // Make the game always winnable (minesweeper algorithm)

    // Fill the board with mines and count neighborMines for SafeFields
    public void initializeBoard(int originX, int originY, boolean practise) {
        // Random MineField placement
        HashSet<Point> mines = new HashSet<>();
        mines.add(new Point(originX, originY));

        int mineCount = 0;
        while (mineCount != mineAmount) {
            Random rng = new Random();
            int x = rng.nextInt(rows);
            int y = rng.nextInt(columns);

            if (!mines.contains(new Point(x, y))) {
                mines.add(new Point(x, y));
                mineCount++;

                grid.put(new Point(x, y), new MineField(practise));
            }
        }

        // Counting neighborMines for all SafeField's
        // Arrays with shifts for all 8 possible neighbors
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Point currentPoint = new Point(i, j);
                Field currentField = grid.get(currentPoint);

                if (!currentField.isMine()) {
                    int count = 0;

                    for (int k = 0; k < 8; k++) {
                        int nx = i + dx[k]; // neighbor x
                        int ny = j + dy[k]; // neighbor y

                        // Check if the neighbor isn't outside the Board
                        if (nx >= 0 && nx < rows && ny >= 0 && ny < columns) {
                            Field neighbor = grid.get(new Point(nx, ny));

                            if (neighbor.isMine()) count++;
                        }
                    }

                    if (currentField instanceof SafeField) {
                        ((SafeField) currentField).setNeighborMines(count);
                    }
                }
            }
        }

    }

    // Cell reveal logic
    public void revealCell(int x, int y) {
        Point point = new Point(x, y);
        Field field = grid.get(point);

        field.reveal();

        if (field.isMine()) {
            throw new GameOverException("Clicked on a mine");
        }

        // Reveal all neighbors if Cell has 0 neighborMines

        if (((SafeField) field).getNeighborMines() == 0) {
            // Arrays with shifts for all 8 possible neighbors
            int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
            int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

            for (int i = 0; i < 8; i++) {
                int nx = x + dx[i]; // neighbor x
                int ny = y + dy[i]; // neighbor y

                // Check if the neighbor isn't outside the Board
                if (nx >= 0 && nx < rows && ny >= 0 && ny < columns) {
                    if (!grid.get(new Point(nx, ny)).isRevealed()) {
                        this.revealCell(nx, ny);
                    }
                }
            }
        }
    }

    // Cell flagging logic
    public void toggleFlagCell(int x, int y) {
        Point point = new Point(x, y);
        Field field = grid.get(point);

        field.toggleFlag();
    }

    // Check for win sate
    public boolean checkWin() {
        int count = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (grid.get(new Point(i, j)).isRevealed()) {
                    count++;
                }
            }
        }

        return count == rows * columns - mineAmount;
    }

    public Field getFieldAt(int x, int y) {
        return grid.get(new Point(x, y));
    }
}
