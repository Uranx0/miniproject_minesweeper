package board;

import exceptions.GameOverException;
import exceptions.InvalidGameConfigurationException;
import field.Field;
import field.MineField;
import field.SafeField;

import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Board {

    //Field with board state
    private final Field[][] grid;
    private final int rows;
    private final int columns;
    private final int mineAmount;
    private int revealedCount = 0;

    // Directions for neighbors (Top-Left to Bottom-Right)
    private static final int[] DX = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] DY = {-1, 0, 1, -1, 1, -1, 0, 1};

    public Board(int rows, int columns, int mineAmount) {
        // Ensuring there is enough space for mines + 3x3 safe zone
        if (rows < 5 || columns < 5 || mineAmount >= rows * columns - 9) {
            throw new InvalidGameConfigurationException("Invalid size or mineAmount");
        }
        this.rows = rows;
        this.columns = columns;
        this.mineAmount = mineAmount;
        this.grid = new Field[rows][columns];

        initializeEmptyBoard();
    }

    // Create board filled with SafeFields
    private void initializeEmptyBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                grid[i][j] = new SafeField();
            }
        }
    }

    //TODO (Maybe implement a solver to check if board is solvable before giving it to the player)

    // Fill the board with mines and count neighborMines for SafeFields
    public void initializeBoard(int firstClickX, int firstClickY, boolean practice) {
        Random rng = new Random();
        Set<Point> safeZone = new HashSet<>();

        // Defining safeZone (firstClick cell + neighbors)
        safeZone.add(new Point(firstClickX, firstClickY));
        for (int i = 0; i < 8; i++) {
            int nx = firstClickX + DX[i];
            int ny = firstClickY + DY[i];
            if (isValid(nx, ny)) {
                safeZone.add(new Point(nx, ny));
            }
        }

        // Random MineField placement
        int minesPlaced = 0;
        while (minesPlaced < mineAmount) {
            int x = rng.nextInt(rows);
            int y = rng.nextInt(columns);

            // Skip if it's already a mine or inside the safe zone
            if (grid[x][y].isMine() || safeZone.contains(new Point(x, y))) {
                continue;
            }

            grid[x][y] = new MineField(practice);
            minesPlaced++;
        }

        // Calculating Numbers for SafeFields
        calculateNeighborMines();
    }

    // Calculate how many neighbors of a Cell are mines
    private void calculateNeighborMines() {
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                if (!grid[x][y].isMine()) {
                    int mines = countMinesAround(x, y);
                    ((SafeField) grid[x][y]).setNeighborMines(mines);
                }
            }
        }
    }

    // Count mines around a Cell
    private int countMinesAround(int x, int y) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int nx = x + DX[i];
            int ny = y + DY[i];
            if (isValid(nx, ny) && grid[nx][ny].isMine()) {
                count++;
            }
        }
        return count;
    }

    // Cell reveal logic
    public void revealCell(int x, int y) {
        if (!isValid(x, y)) return;

        Field field = grid[x][y];

        // Stop if already revealed
        if (field.isRevealed()) return;

        field.reveal();

        if (field.isMine()) {
            throw new GameOverException("Clicked on a mine");
        }

        revealedCount++;

        // Revealing neighbors automatically if Cell has 0 neighborMines
        if (field instanceof SafeField && ((SafeField) field).getNeighborMines() == 0) {
            for (int i = 0; i < 8; i++) {
                revealCell(x + DX[i], y + DY[i]);
            }
        }
    }

    // Cell flagging logic
    public void toggleFlagCell(int x, int y) {
        if (isValid(x, y)) {
            grid[x][y].toggleFlag();
        }
    }

    // Check for win sate
    public boolean checkWin() {
        // Win condition: All safe cells are revealed
        int totalCells = rows * columns;
        int safeCells = totalCells - mineAmount;
        return revealedCount == safeCells;
    }

    // Helper method to check array bounds
    private boolean isValid(int x, int y) {
        return x >= 0 && x < rows && y >= 0 && y < columns;
    }

    public Field getFieldAt(int x, int y) {
        if (!isValid(x, y)) return null;
        return grid[x][y];
    }
}
