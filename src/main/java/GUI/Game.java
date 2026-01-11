package GUI;

import board.Board;
import exceptions.GameOverException;
import score.Difficulty;
import score.HighScoreManager;
import score.Score;

import javax.swing.*;
import javax.swing.plaf.metal.MetalButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class Game extends JFrame {

    private final int rows;
    private final int columns;
    private final int mineAmount;
    private final int highScoreEntryAmount;

    private Board board;
    private final JButton[][] buttons;
    private final HighScoreManager highScoreManager;
    private final GameTimer gameTimer;

    private JPanel buttonsPanel;
    private final JLabel mineLabel;

    private boolean firstClick = true;
    private final boolean practice;
    private final Difficulty difficulty;

    public Game(int rows, int columns, int mineAmount, boolean practice, Difficulty difficulty) {
        this.rows = rows;
        this.columns = columns;
        this.mineAmount = mineAmount;
        this.practice = practice;
        this.difficulty = difficulty;
        this.highScoreEntryAmount = 10;
        this.board = new Board(rows, columns, mineAmount);
        this.buttons = new JButton[rows][columns];

        this.highScoreManager = new HighScoreManager(difficulty, 1000);

        // Window setup
        setTitle("Minesweeper");
        int windowScale = 50;
        setSize(columns * windowScale, rows * windowScale + 80);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Menu bar
        setJMenuBar(createMenuBar());

        // Header panel (timer + mine counter)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setBackground(Color.LIGHT_GRAY);

        // Mine counter (left)
        mineLabel = new JLabel("Mines: " + mineAmount);
        mineLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mineLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Game timer (right)
        JLabel timeLabel = new JLabel("Time: 0s");
        this.gameTimer = new GameTimer(timeLabel);

        headerPanel.add(mineLabel, BorderLayout.WEST);
        headerPanel.add(timeLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        // -------------------------------------------

        // Buttons panel
        this.buttonsPanel = createButtonsPanel();
        add(buttonsPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    // Create menu bar
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Menu");

        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> newGame());

        JMenuItem resetGameItem = new JMenuItem("Reset Game");
        resetGameItem.addActionListener(e -> resetGame());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        JMenuItem highScoresItem = new JMenuItem("High Scores");
        highScoresItem.addActionListener(e -> showHighscores(highScoreEntryAmount));

        gameMenu.add(newGameItem);
        gameMenu.add(resetGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);
        menuBar.add(highScoresItem);

        return menuBar;
    }

    // Create buttons label
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(rows, columns));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {

                // Button visuals
                JButton button = new JButton();
                int fontSize = (rows > 6 && columns > 6) ? 10 : 9;
                button.setFont(new Font("Segoe UI Emoji", Font.BOLD, fontSize));
                button.setFocusPainted(false);
                styleButton(button, false);

                buttons[i][j] = button;

                int finalI = i;
                int finalJ = j;

                // Left click (revealing)
                button.addActionListener(e -> handleLeftClick(finalI, finalJ));

                // Righ click (flagging)
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e) && buttons[finalI][finalJ].isEnabled() && !firstClick) {
                            board.toggleFlagCell(finalI, finalJ);
                            refreshView();
                        }
                    }
                });

                panel.add(button);
            }
        }
        return panel;
    }

    // Helper method for styling buttons
    private void styleButton(JButton btn, boolean isRevealed) {
        // Disable the hover "glow" effect
        btn.setRolloverEnabled(false);

        if (isRevealed) {
            btn.setBackground(new Color(32, 32, 32));
            btn.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

            btn.setUI(new MetalButtonUI() {
                protected Color getDisabledTextColor() {
                    return getFieldColor(btn.getText());
                }
            });
        } else {
            btn.setBackground(new Color(48, 48, 48));
            btn.setBorder(BorderFactory.createLineBorder(new Color(32, 32, 32)));
            btn.setRolloverEnabled(true);
        }
    }

    private Color getFieldColor(String symbol) {
        return switch (symbol) {
            case "🚩" -> Color.RED;
            case "💣" -> Color.BLACK;
            default -> Color.WHITE;
        };
    }

    // Handling left click on buttons
    private void handleLeftClick(int x, int y) {
        if (firstClick) {
            this.board.initializeBoard(x, y, practice);
            gameTimer.start();
            firstClick = false;
        }

        try {
            board.revealCell(x, y);
            refreshView();

            if (board.checkWin()) {
                handleWin();
            }

        } catch (GameOverException ex) {
            handleGameOver();
        }
    }

    // Handling win state
    private void handleWin() {
        gameTimer.stop();
        revealAllMines(true);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                buttons[i][j].setEnabled(false);
            }
        }

        if (!practice && difficulty != Difficulty.CUSTOM) {
            String name = JOptionPane.showInputDialog(
                    this,
                    "You win!\nTime: " + gameTimer.getTimeInSeconds() + "s\nEnter your name:"
            );

            if (name != null && !name.trim().isEmpty()) {
                highScoreManager.addScore(name, gameTimer.getTimeInSeconds());
                showHighscores(highScoreEntryAmount);
            }
        } else {
            JOptionPane.showMessageDialog(this, "You win!\nTime: " + gameTimer.getTimeInSeconds());
        }
    }

    // Handling game over state
    private void handleGameOver() {
        gameTimer.stop();
        revealAllMines(false);

        // Game over window with option to play again
        Object[] options = {"Play Again", "Exit"};
        int selection = JOptionPane.showOptionDialog(
                this,
                "Game Over! You hit a mine.", "Game Over",
                JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE,
                null, options, options[0]
        );

        if (selection == 0) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    // Resetting the game
    private void resetGame() {
        firstClick = true;
        this.board = new Board(rows, columns, mineAmount);
        this.gameTimer.reset();

        mineLabel.setText("Mines: " + mineAmount);

        remove(buttonsPanel);
        buttonsPanel = createButtonsPanel();
        add(buttonsPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void newGame() {
        this.dispose();
        new GameSetup();
    }

    // Refreshing the button label and updating mine count
    private void refreshView() {
        int flagsPlaced = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                JButton btn = buttons[i][j];
                String symbol = board.getFieldAt(i, j).getDisplaySymbol();
                boolean isRevealed = board.getFieldAt(i, j).isRevealed();

                // Set the text color (based on symbol)
                btn.setText(symbol);
                btn.setForeground(getFieldColor(symbol));

                if (symbol.equals("🚩") || symbol.equals("💣")) {
                    flagsPlaced++;
                    styleButton(btn, false);
                } else styleButton(btn, isRevealed);

                // Disable buttons when everything around them is revealed or flagged
                btn.setEnabled(board.isEnabled(i, j));
            }
        }
        // Updating the mine count label
        int remaining = mineAmount - flagsPlaced;
        mineLabel.setText("Mines: " + remaining);
    }

    // Reveal all mines on button label
    private void revealAllMines(boolean won) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                styleButton(buttons[i][j], true);
                buttons[i][j].setEnabled(false);

                // Show all mines
                if (board.getFieldAt(i, j).isMine()) {
                    buttons[i][j].setText("💣");
                    if (won) {
                        buttons[i][j].setBackground(Color.GREEN);
                    } else {
                        buttons[i][j].setBackground(Color.RED);
                    }
                }
            }
        }
    }

    // Show highscore window
    private void showHighscores(int amount) {
        List<Score> topScores = highScoreManager.getTopScores(amount);

        // Preparing Data for JTable
        String[] columnNames = {"Rank", "Name", "Time (s)"};
        Object[][] data = new Object[amount][3];

        for (int i = 0; i < amount; i++) {
            data[i][0] = i + 1;

            if (i < topScores.size()) {
                Score s = topScores.get(i);
                data[i][1] = s.getPlayerName();
                data[i][2] = s.getTimeInSeconds();
            } else {
                // Handling empty rows
                data[i][1] = "-----";
                data[i][2] = "--";
            }
        }

        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);

        // Formatting (center text)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        // ScrollPane to put the table in
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(350, 200));

        JOptionPane.showMessageDialog(
                this,
                (difficulty != Difficulty.CUSTOM) ? scrollPane : "High Scores are disabled in Custom Difficulty",
                "High Scores - " + difficulty.toString().toLowerCase(),
                JOptionPane.PLAIN_MESSAGE
        );
    }
}