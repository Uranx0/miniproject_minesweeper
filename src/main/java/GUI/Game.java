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

    // --- THEME COLORS ---
    private static final Color BG_DARK = new Color(43, 43, 43);       // Window/Header Background
    private static final Color BTN_HIDDEN = new Color(60, 63, 65);    // Unclicked Button
    private static final Color BTN_REVEALED = new Color(32, 32, 32);  // Clicked Button
    private static final Color BORDER_COLOR = new Color(20, 20, 20);  // Dark Borders

    private static final MetalButtonUI DARK_BUTTON_UI = new MetalButtonUI() {
        @Override
        protected Color getDisabledTextColor() {
            return null;
        }

        @Override
        protected Color getSelectColor() {
            return BG_DARK;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            if (b.getForeground() != null) {
                g.setColor(b.getForeground());
            }
            super.paint(g, c);
        }
    };

    public Game(int rows, int columns, int mineAmount, boolean practice, Difficulty difficulty) {
        this.rows = rows;
        this.columns = columns;
        this.mineAmount = mineAmount;
        this.practice = practice;
        this.difficulty = difficulty;
        this.highScoreEntryAmount = 10;
        this.board = new Board(rows, columns, mineAmount);
        this.buttons = new JButton[rows][columns];

        this.highScoreManager = new HighScoreManager(difficulty, 100);

        // Window setup
        setTitle("Minesweeper");
        int windowScale = 50;
        setSize(columns * windowScale, rows * windowScale + 80);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Apply Dark Background to Frame
        getContentPane().setBackground(BG_DARK);

        // Menu bar
        setJMenuBar(createMenuBar());

        // Header panel (timer + mine counter)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setBackground(BG_DARK);

        // Mine counter (left)
        mineLabel = new JLabel("Mines: " + mineAmount);
        mineLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mineLabel.setForeground(Color.WHITE);
        mineLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Game timer (right)
        JLabel timeLabel = new JLabel("Time: 0s");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timeLabel.setForeground(Color.WHITE);
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
        menuBar.setBackground(BG_DARK);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JMenu gameMenu = new JMenu("Menu");
        gameMenu.setForeground(Color.WHITE);

        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> newGame());

        JMenuItem resetGameItem = new JMenuItem("Reset Game");
        resetGameItem.addActionListener(e -> resetGame());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        JMenuItem highScoresItem = new JMenuItem("High Scores");
        highScoresItem.setForeground(Color.WHITE);
        highScoresItem.setBackground(BG_DARK);
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
        panel.setBackground(BG_DARK);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
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
        // Force the custom Dark UI painter
        btn.setUI(DARK_BUTTON_UI);

        if (isRevealed) {
            btn.setBackground(BTN_REVEALED);
            btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        } else {
            btn.setBackground(BTN_HIDDEN);
            btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            btn.setRolloverEnabled(true);
        }
    }

    // --- NEON / PASTEL COLORS FOR DARK MODE ---
    private Color getFieldColor(String symbol) {
        return switch (symbol) {
            case "1" -> new Color(100, 149, 237);
            case "2" -> new Color(144, 238, 144);
            case "3" -> new Color(255, 102, 102);
            case "4" -> new Color(150, 150, 255);
            case "5" -> new Color(255, 179, 71);
            case "6" -> new Color(102, 205, 170);
            case "7" -> Color.WHITE;
            case "8" -> Color.LIGHT_GRAY;
            case "🚩" -> new Color(255, 69, 0);
            case "💣" -> new Color(255, 50, 50);
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
            JOptionPane.showMessageDialog(this, "You win!\nTime: " + gameTimer.getTimeInSeconds() + "s");
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
        } else if (selection == 1) {
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

    // Refreshing the game board
    private void refreshView() {
        int flagsPlaced = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                JButton btn = buttons[i][j];
                String symbol = board.getFieldAt(i, j).getDisplaySymbol();
                boolean isRevealed = board.getFieldAt(i, j).isRevealed();

                // Set text & color
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
                if (board.getFieldAt(i, j).isMine()) {
                    JButton btn = buttons[i][j];
                    btn.setText("💣");
                    btn.setForeground(Color.BLACK);
                    styleButton(btn, true);

                    if (won) {
                        btn.setBackground(new Color(50, 205, 50));
                    } else {
                        btn.setBackground(new Color(205, 50, 50));
                    }
                }
                buttons[i][j].setEnabled(false);
            }
        }
    }

    // Show highscore window
    private void showHighscores(int amount) {
        // Preparing data for JTable
        List<Score> topScores = highScoreManager.getTopScores(amount);
        String[] columnNames = {"Rank", "Name", "Time (s)"};
        Object[][] data = new Object[amount][3];

        for (int i = 0; i < amount; i++) {
            data[i][0] = i + 1;
            if (i < topScores.size()) {
                Score s = topScores.get(i);
                data[i][1] = s.getPlayerName();
                data[i][2] = s.getTimeInSeconds();
            } else {
                data[i][1] = "-----";
                data[i][2] = "--";
            }
        }

        // Creating the table
        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);

        // Dark table styling
        table.setBackground(BTN_REVEALED);
        table.setForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.getTableHeader().setBackground(BTN_HIDDEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        // Center text renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(BTN_REVEALED);
        centerRenderer.setForeground(Color.WHITE);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setPreferredSize(new Dimension(350, 200));

        // Creating custom dialog (Because JOptionPane can't change bg '_' )
        String title = "High Scores - " + difficulty.toString().toLowerCase();
        if (difficulty == Difficulty.CUSTOM) title = "High Scores (Disabled)";

        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG_DARK);

        // Content panel (To hold the text / table)
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (difficulty != Difficulty.CUSTOM) {
            contentPanel.add(scrollPane);
        } else {
            JLabel msg = new JLabel("High Scores are disabled in Custom Difficulty");
            msg.setForeground(Color.WHITE);
            contentPanel.add(msg);
        }

        // Custom "OK" button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JButton okButton = new JButton("OK");

        // Style the button to match game theme
        okButton.setFocusPainted(false);
        okButton.setBackground(BTN_HIDDEN);
        okButton.setForeground(Color.WHITE);
        okButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        // Add hover effect
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(BTN_HIDDEN);
            }
        });

        okButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(okButton);

        // Assemble and show
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}