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
import java.awt.event.*;
import java.util.List;

public class Game extends JFrame {

    private final int rows;
    private final int columns;
    private final int mineAmount;
    private final int highScoreEntryAmount;
    private final int scale;

    private Board board;
    private final JButton[][] buttons;
    private final HighScoreManager highScoreManager;
    private final GameTimer gameTimer;

    private JPanel buttonsPanel;
    private final JPanel boardWrapperPanel;
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
        this.highScoreEntryAmount = 10;
        this.scale = 20;
        this.practice = practice;
        this.difficulty = difficulty;
        this.board = new Board(rows, columns, mineAmount);
        this.buttons = new JButton[rows][columns];

        this.highScoreManager = new HighScoreManager(difficulty, 100);

        // Window setup
        setTitle("Minesweeper");
        setMinimumSize(new Dimension(columns * scale + 350, rows * scale + 300));
        setSize(new Dimension(columns * scale + 350, rows * scale + 300));
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);

        // Menu bar
        setJMenuBar(createMenuBar());

        // Header panel (timer + mine counter)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setBackground(BG_DARK);

        // R - reset & N - new game (key binds)
        InputMap inputMap = headerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = headerPanel.getActionMap();

        KeyStroke rKey = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
        inputMap.put(rKey, "resetGame");
        actionMap.put("resetGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });

        KeyStroke nKey = KeyStroke.getKeyStroke(KeyEvent.VK_N, 0);
        inputMap.put(nKey, "newGame");
        actionMap.put("newGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame();
            }
        });

        // Mine counter (left)
        mineLabel = new JLabel("Mines: " + mineAmount);
        mineLabel.setFont(new Font("Segoe UI", Font.BOLD, scale));
        mineLabel.setForeground(Color.WHITE);
        mineLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Game timer (right)
        JLabel timeLabel = new JLabel("Time: 0s");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, scale));
        timeLabel.setForeground(Color.WHITE);
        this.gameTimer = new GameTimer(timeLabel);

        headerPanel.add(mineLabel, BorderLayout.WEST);
        headerPanel.add(timeLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Buttons boardWrapper panel
        boardWrapperPanel = new JPanel(new GridBagLayout());
        boardWrapperPanel.setBackground(BG_DARK);

        buttonsPanel = createButtonsPanel();
        boardWrapperPanel.add(buttonsPanel);

        boardWrapperPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = boardWrapperPanel.getWidth();
                int h = boardWrapperPanel.getHeight();

                int cellSize = Math.min(w / columns, h / rows);

                cellSize = Math.max(cellSize, scale);

                Dimension squareSize = new Dimension(columns * cellSize, rows * cellSize);
                buttonsPanel.setPreferredSize(squareSize);

                boardWrapperPanel.revalidate();
            }
        });
        add(boardWrapperPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setResizable(true);
        setVisible(true);
    }

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

    private void styleButton(JButton btn, boolean isRevealed) {
        btn.setRolloverEnabled(false);
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

            while (name != null && name.matches(".*:.*")) {
                name = JOptionPane.showInputDialog(
                        this,
                        "Your name can't contain ':'\nTime: " + gameTimer.getTimeInSeconds() + "s\nEnter your name:"
                );
            }

            if (name != null && !name.trim().isEmpty()) {
                highScoreManager.addScore(name, gameTimer.getTimeInSeconds());
                showHighscores(highScoreEntryAmount);
            }
        } else {
            JOptionPane.showMessageDialog(this, "You win!\nTime: " + gameTimer.getTimeInSeconds() + "s");
        }
    }

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

    private void newGame() {
        this.dispose();
        new GameSetup();
    }

    private void resetGame() {
        firstClick = true;
        this.board = new Board(rows, columns, mineAmount);
        this.gameTimer.reset();
        mineLabel.setText("Mines: " + mineAmount);

        boardWrapperPanel.remove(buttonsPanel);
        buttonsPanel = createButtonsPanel();
        boardWrapperPanel.add(buttonsPanel);

        refreshBoardLayout();

        revalidate();
        repaint();
    }

    private void refreshBoardLayout() {
        if (boardWrapperPanel.getWidth() == 0 || boardWrapperPanel.getHeight() == 0) return;

        int w = boardWrapperPanel.getWidth();
        int h = boardWrapperPanel.getHeight();
        int cellSize = Math.min(w / columns, h / rows);

        cellSize = Math.max(cellSize, scale);

        Dimension squareSize = new Dimension(columns * cellSize, rows * cellSize);
        buttonsPanel.setPreferredSize(squareSize);

        boardWrapperPanel.revalidate();
        boardWrapperPanel.repaint();
    }

    private void refreshView() {
        int flagsPlaced = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                JButton btn = buttons[i][j];
                String symbol = board.getFieldAt(i, j).getDisplaySymbol();
                boolean isRevealed = board.getFieldAt(i, j).isRevealed();

                btn.setText(symbol);
                btn.setForeground(getFieldColor(symbol));

                if (symbol.equals("🚩") || symbol.equals("💣")) {
                    flagsPlaced++;
                    styleButton(btn, false);
                } else styleButton(btn, isRevealed);

                btn.setEnabled(board.isEnabled(i, j));
            }
        }
        int remaining = mineAmount - flagsPlaced;
        mineLabel.setText("Mines: " + remaining);
    }

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

    private void showHighscores(int amount) {
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

        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);

        table.setBackground(BTN_REVEALED);
        table.setForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.getTableHeader().setBackground(BTN_HIDDEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(BTN_REVEALED);
        centerRenderer.setForeground(Color.WHITE);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

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

        okButton.setFocusPainted(false);
        okButton.setBackground(BTN_HIDDEN);
        okButton.setForeground(Color.WHITE);
        okButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

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

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}