package tests.board;

import board.Board;
import exceptions.GameOverException;
import exceptions.InvalidGameConfigurationException;
import field.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;
    private final int ROWS = 9;
    private final int COLS = 9;
    private final int MINES = 10;

    @BeforeEach
    void setUp() {
        // Create a fresh board before every test
        board = new Board(ROWS, COLS, MINES);
    }

    @Test
    void testInvalidConfiguration() {
        assertThrows(
                InvalidGameConfigurationException.class,
                () -> new Board(ROWS, COLS, 0),
                "Constructor should throw exception if minesAmount is 0 or less"
        );

        assertThrows(
                InvalidGameConfigurationException.class,
                () -> new Board(ROWS, COLS, ROWS*COLS-9),
                "Constructor should throw exception if minesAmount is greater than board size"
        );
    }

    @Test
    void testBoardInitializationDimensions() {
        // Check if the grid was created with correct size
        assertNotNull(board.getFieldAt(0, 0));
        assertNotNull(board.getFieldAt(ROWS - 1, COLS - 1));
        assertNull(board.getFieldAt(ROWS, COLS), "Out of bounds should return null");
    }

    @Test
    void testFirstClickIsSafe() {
        // Initialize the board by clicking at 0,0
        board.initializeBoard(0, 0, false);

        Field startField = board.getFieldAt(0, 0);

        // The first clicked cell must NOT be a mine
        assertFalse(startField.isMine(), "First click should never be a mine");
    }

    @Test
    void testMineCount() {
        board.initializeBoard(5, 5, false);

        int mineCounter = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board.getFieldAt(i, j).isMine()) {
                    mineCounter++;
                }
            }
        }
        assertEquals(MINES, mineCounter, "Board should generate exactly " + MINES + " mines");
    }

    @Test
    void testFlagging() {
        // Flag a cell
        board.toggleFlagCell(1, 1);
        assertTrue(board.getFieldAt(1, 1).isFlagged(), "Cell should be flagged");

        // Unflag a cell
        board.toggleFlagCell(1, 1);
        assertFalse(board.getFieldAt(1, 1).isFlagged(), "Cell should be unflagged");
    }

    @Test
    void testGameOverOnMineClick() {
        board.initializeBoard(0, 0, false);

        // Find a mine location to click
        int mineX = -1, mineY = -1;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board.getFieldAt(i, j).isMine()) {
                    mineX = i;
                    mineY = j;
                    break;
                }
            }
        }

        // Assert that clicking it throws the exception
        int finalMineX = mineX;
        int finalMineY = mineY;
        assertThrows(GameOverException.class, () -> board.revealCell(finalMineX, finalMineY));
    }

    @Test
    void testWinCondition() {
        // Create a very small board: 5x5 with 1 mine
        Board smallBoard = new Board(5, 5, 1);
        // Initialize at 0,0 (safe)
        smallBoard.initializeBoard(0, 0, false);

        // Reveal ALL non-mine cells
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (!smallBoard.getFieldAt(i, j).isMine()) {
                    smallBoard.revealCell(i, j);
                }
            }
        }

        assertTrue(smallBoard.checkWin(), "Game should be won when all safe cells are revealed");
    }
}