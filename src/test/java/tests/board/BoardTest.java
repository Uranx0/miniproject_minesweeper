package tests.board;

import board.Board;
import exceptions.*;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    @Test
    void testMineCount() {
        int rows = 10;
        int cols = 10;
        int mineAmount = 15;
        Board board = new Board(rows, cols, mineAmount);
        board.initializeBoard(0,0);

        int actualMines = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board.getFieldAt(i, j).isMine()) {
                    actualMines++;
                }
            }
        }

        assertEquals(mineAmount, actualMines, "The number of mines on the board should match the configuration.");
    }

    @Test
    void testInvalidConfiguration() {
        int rows = 5;
        int cols = 5;
        int tooManyMines = 26;

        assertThrows(InvalidGameConfigurationException.class, () -> {
            new Board(rows, cols, tooManyMines);
        }, "Constructor should throw exception if mines count is greater than board size.");
    }

    @Test
    void testToggleFlag() {
        Board board = new Board(10, 10, 5);
        board.initializeBoard(0,0);
        int x = 0;
        int y = 0;

        // Flag the cell
        board.toggleFlagCell(x, y);
        boolean isFlaggedAfterFirstClick = board.getFieldAt(x, y).isFlagged();

        // Unflag the cell
        board.toggleFlagCell(x, y);
        boolean isFlaggedAfterSecondClick = board.getFieldAt(x, y).isFlagged();

        assertTrue(isFlaggedAfterFirstClick, "Field should be flagged after first toggle.");
        assertFalse(isFlaggedAfterSecondClick, "Field should be unflagged after second toggle.");
    }

    @Test
    void testRevealingMineThrowsException() {
        int rows = 5;
        int cols = 5;
        Board board = new Board(rows, cols, 10);
        board.initializeBoard(0,0);

        Point mineLocation = null;

        // Finding a location with a mine
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board.getFieldAt(i, j).isMine()) {
                    mineLocation = new Point(i, j);
                    break;
                }
            }
            if (mineLocation != null) break;
        }

        assertNotNull(mineLocation, "Board should contain at least one mine.");

        Point finalMineLocation = mineLocation;
        assertThrows(GameOverException.class, () -> {
            board.revealCell(finalMineLocation.x, finalMineLocation.y);
        }, "Revealing a mine should throw GameOverException.");
    }
}
