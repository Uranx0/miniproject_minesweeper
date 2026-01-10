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

public class GUI extends JFrame {

    private final JButton[][] buttons;
    private Board board;
    private final int size;
    private final int mineAmount;
    private final GameTimer gameTimer;
    private JPanel buttonsPanel;
    private boolean firstClick = true;
    private final HighScoreManager highScoreManager;
    private final int highScoreEntryAmount;

    public GUI(int size, int mineAmount, int highScoreEntryAmount) {
        this.size = size;
        this.mineAmount = mineAmount;
        this.highScoreEntryAmount = highScoreEntryAmount;
        this.board = new Board(size, mineAmount);
        this.buttons = new JButton[size][size];

        this.highScoreManager = new HighScoreManager(1000);

        // Window setup
        setTitle("Minesweeper");
        setSize(600, 700);
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

        setVisible(true);
    }

    // Create menu bar
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Menu");

        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> resetGame());

        JMenuItem highScoresItem = new JMenuItem("High Scores");
        highScoresItem.addActionListener(e -> showHighscores(highScoreEntryAmount));

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);
        menuBar.add(highScoresItem);

        return menuBar;
    }

    // Create buttons label
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(size, size));

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                // Button visuals
                JButton button = new JButton();
                button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
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
        this.board = new Board(size, mineAmount);
        this.gameTimer.reset();

        remove(buttonsPanel);
        buttonsPanel = createButtonsPanel();
        add(buttonsPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // Refreshing the button label
    private void refreshView() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
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
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
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

    //TODO
    // Show highscore window
    private void showHighscores(int amount) {
        // 1. Get the list (Use Generics <Score>)
        List<Score> topScores = highScoreManager.getTopScores(amount);

        // 2. Prepare Data for JTable
        String[] columnNames = {"Rank", "Name", "Time (s)"};
        Object[][] data = new Object[amount][3];

        for (int i = 0; i < amount; i++) {
            // Always set the Rank
            data[i][0] = i + 1;

            // SAFETY CHECK: Ensure we don't go out of bounds if list is small
            if (i < topScores.size()) {
                Score s = topScores.get(i); // FIXED: Added (i)
                data[i][1] = s.getPlayerName();
                data[i][2] = s.getTimeInSeconds(); // Or s.getValue()
            } else {
                // Optional: Handle empty rows explicitly
                data[i][1] = "---";
                data[i][2] = "--";
            }
        }

        // 3. Create JTable
        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);

        // Formatting: Center text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        // 4. Wrap in ScrollPane & Show
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(350, 200));

        JOptionPane.showMessageDialog(this, scrollPane, "High Scores", JOptionPane.PLAIN_MESSAGE);
    }
}