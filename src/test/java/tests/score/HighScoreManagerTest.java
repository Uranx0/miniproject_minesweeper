package tests.score;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import score.Difficulty;
import score.HighScoreManager;
import score.Score;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HighScoreManagerTest {

    private HighScoreManager manager;
    private final String TEST_FILE = "test_minesweeper_scores.dat";

    @BeforeEach
    void setUp() {
        deleteTestFile();
    }

    @AfterEach
    void cleanUp() {
        deleteTestFile();
    }

    private void deleteTestFile() {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testAddAndSortScores() {
        manager = new HighScoreManager(Difficulty.EASY, 5, TEST_FILE);

        // Adding scores in unsorted order
        manager.addScore("SlowPlayer", 999);
        manager.addScore("FastPlayer", 10);
        manager.addScore("AveragePlayer", 50);

        List<Score> top = manager.getTopScores(10);

        // Scores should be sorted by time (lowest first)
        assertEquals("FastPlayer", top.get(0).getPlayerName());
        assertEquals(10, top.get(0).getTimeInSeconds());

        assertEquals("AveragePlayer", top.get(1).getPlayerName());
        assertEquals("SlowPlayer", top.get(2).getPlayerName());
    }

    @Test
    void testMaxEntriesLimit() {
        // Creating manager that only accepts top 3 scores
        manager = new HighScoreManager(Difficulty.EASY, 3, TEST_FILE);

        manager.addScore("1st", 10);
        manager.addScore("2nd", 20);
        manager.addScore("3rd", 30);
        manager.addScore("4th", 40); // The worst score should be dropped

        List<Score> top = manager.getTopScores(10);

        assertEquals(3, top.size(), "There should only be 3 scores");
        assertEquals("1st", top.get(0).getPlayerName());
        assertEquals("3rd", top.get(2).getPlayerName());

        // Checking again if 4th is gone
        boolean contains4th = top.stream().anyMatch(s -> s.getPlayerName().equals("4th"));
        assertFalse(contains4th, "The worst score should have been trimmed");
    }

    @Test
    void testPersistence() {
        // Creating a manager and save a score
        HighScoreManager manager1 = new HighScoreManager(Difficulty.EASY, 10, TEST_FILE);
        manager1.addScore("PersistUser", 42);

        // Creating a brand-new manager instance pointing to the same file
        HighScoreManager manager2 = new HighScoreManager(Difficulty.EASY, 10, TEST_FILE);
        List<Score> scores = manager2.getTopScores(10);

        // The new manager should have loaded the data from the file
        assertFalse(scores.isEmpty(), "Should load scores from file");
        assertEquals("PersistUser", scores.get(0).getPlayerName());
        assertEquals(42, scores.get(0).getTimeInSeconds());
    }

    @Test
    void testMultipleDifficultiesInOneFile() throws IOException {

        // Saving an EASY score
        HighScoreManager easyMgr = new HighScoreManager(Difficulty.EASY, 10, TEST_FILE);
        easyMgr.addScore("EasyUser", 15);

        // Saving a HARD score (using the same file)
        HighScoreManager hardMgr = new HighScoreManager(Difficulty.HARD, 10, TEST_FILE);
        hardMgr.addScore("HardUser", 99);

        // Verifying File Content manually to ensure format "DIFFICULTY:NAME:TIME"
        List<String> lines = Files.readAllLines(Path.of(TEST_FILE));
        boolean hasEasy = lines.stream().anyMatch(line -> line.equals("EASY:EasyUser:15"));
        boolean hasHard = lines.stream().anyMatch(line -> line.equals("HARD:HardUser:99"));

        assertTrue(hasEasy, "File should contain the Easy score");
        assertTrue(hasHard, "File should contain the Hard score");

        // Verifying Separation: Easy manager should NOT see Hard scores
        // Re-loading Easy Manager
        HighScoreManager easyMgrReloaded = new HighScoreManager(Difficulty.EASY, 10, TEST_FILE);
        List<Score> easyScores = easyMgrReloaded.getTopScores(10);

        assertEquals(1, easyScores.size());
        assertEquals("EasyUser", easyScores.get(0).getPlayerName());
    }

    @Test
    void testCorruptedDataHandling() throws IOException {
        // Manually writing garbage to the file
        Files.writeString(Path.of(TEST_FILE), "ThisIsNotAValidScore\nEASY:ValidUser:10\nEASY:BrokenTime:abc");

        // Loading manager
        manager = new HighScoreManager(Difficulty.EASY, 10, TEST_FILE);
        List<Score> scores = manager.getTopScores(10);

        // Manager should have loaded the valid line and ignored the garbage without crashing
        assertEquals(1, scores.size());
        assertEquals("ValidUser", scores.get(0).getPlayerName());
    }
}