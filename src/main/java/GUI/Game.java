package GUI;

import board.Board;
import exceptions.GameOverException;
import score.HighScoreManager;
import score.Score;

import javax.swing.*;
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
    private boolean firstClick = true;

    public Game(int rows, int columns, int mineAmount) {
        this.rows = rows;
        this.columns = columns;
        this.mineAmount = mineAmount;
        this.highScoreEntryAmount = 10;
        this.board = new Board(rows, columns, mineAmount);
        this.buttons = new JButton[rows][columns];

        this.highScoreManager = new HighScoreManager(1000);

        // Window setup
        setTitle("Minesweeper");
        int windowScale = 50;
        setSize(columns * windowScale, (rows) * windowScale + 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Menu bar
        setJMenuBar(createMenuBar());

        // Game timer
        JLabel timeLabel = new JLabel();
        this.gameTimer = new GameTimer(timeLabel);
        add(timeLabel, BorderLayout.NORTH);

        // Button board
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
                button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
                button.setFocusPainted(false);

                buttons[i][j] = button;

                int finalI = i;
                int finalJ = j;

                // Left click (revealing)
                button.addActionListener(e -> handleLeftClick(finalI, finalJ));

                // Righ click (flagging)
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e) && buttons[finalI][finalJ].isEnabled()) {
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

    // Handling left click on buttons
    private void handleLeftClick(int x, int y) {
        if (firstClick) {
            this.board.initializeBoard(x, y);
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

        String name = JOptionPane.showInputDialog(
                this,
                "You win!\nTime: " + gameTimer.getTimeInSeconds() + "s\nEnter your name:"
        );

        if (name != null && !name.trim().isEmpty()) {
            highScoreManager.addScore(name, gameTimer.getTimeInSeconds());
            showHighscores(highScoreEntryAmount);
        }
    }

    // Handling game over state
    private void handleGameOver() {
        gameTimer.stop();
        revealAllMines();

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

    // Refreshing the button label
    private void refreshView() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                JButton btn = buttons[i][j];
                String symbol = board.getFieldAt(i, j).getDisplaySymbol();
                btn.setText(symbol);

                // Disable reveled buttons
                if (board.getFieldAt(i, j).isRevealed()) {
                    btn.setEnabled(false);

                    // Color change to disabled buttons for visibility
                    btn.setUI(new javax.swing.plaf.metal.MetalButtonUI() {
                        protected Color getDisabledTextColor() {
                            return Color.BLACK;
                        }
                    });
                }
            }
        }
    }

    // Reveal all mines on button label
    private void revealAllMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                // Show all mines
                if (board.getFieldAt(i, j).isMine()) {
                    buttons[i][j].setText("💣");
                    buttons[i][j].setBackground(Color.RED);

                    // Color change for visibility
                    buttons[i][j].setUI(new javax.swing.plaf.metal.MetalButtonUI() {
                        protected Color getDisabledTextColor() {
                            return Color.BLACK;
                        }
                    });
                }
                buttons[i][j].setEnabled(false);
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

        JOptionPane.showMessageDialog(this, scrollPane, "High Scores", JOptionPane.PLAIN_MESSAGE);
    }
}