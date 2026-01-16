package GUI;

import score.Difficulty;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameSetup extends JFrame {

    private final JSpinner rowSpinner;
    private final JSpinner colSpinner;
    private final JSpinner mineSpinner;
    private final JRadioButton easyBtn;
    private final JRadioButton mediumBtn;
    private final JRadioButton hardBtn;
    private final JCheckBox practiceCheckBox;

    // --- THEME COLORS ---
    private static final Color BG_DARK = new Color(43, 43, 43);
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color BTN_DARK = new Color(60, 63, 65);
    private static final Color BORDER_COLOR = new Color(100, 100, 100);

    public GameSetup() {
        setTitle("New Game Setup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        getContentPane().setBackground(BG_DARK);

        // Presets panel
        JPanel presetPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        presetPanel.setBackground(BG_DARK);
        presetPanel.setBorder(createDarkTitledBorder("Choose Difficulty"));

        ButtonGroup group = new ButtonGroup();
        easyBtn = createDarkRadioButton("Easy (9x9, 10 Mines)");
        mediumBtn = createDarkRadioButton("Medium (16x16, 40 Mines)");
        hardBtn = createDarkRadioButton("Hard (16x30, 99 Mines)");
        JRadioButton customBtn = createDarkRadioButton("Custom");

        practiceCheckBox = new JCheckBox("Practice Mode?");
        practiceCheckBox.setBackground(BG_DARK);
        practiceCheckBox.setForeground(new Color(144, 238, 144));
        practiceCheckBox.setFocusPainted(false);
        practiceCheckBox.setSelected(false);

        group.add(easyBtn);
        group.add(mediumBtn);
        group.add(hardBtn);
        group.add(customBtn);

        presetPanel.add(easyBtn);
        presetPanel.add(mediumBtn);
        presetPanel.add(hardBtn);
        presetPanel.add(customBtn);
        presetPanel.add(practiceCheckBox);

        // Custom input panel
        JPanel customPanel = new JPanel(new FlowLayout());
        customPanel.setBackground(BG_DARK);
        customPanel.setBorder(createDarkTitledBorder("Custom Settings [5 - 30 : 5 - 60 : 1 - 400]"));

        rowSpinner = createDarkSpinner(16, 5, 30);
        colSpinner = createDarkSpinner(30, 5, 60);
        mineSpinner = createDarkSpinner(99, 1, 400);

        customPanel.add(createDarkLabel("Rows:"));
        customPanel.add(rowSpinner);
        customPanel.add(createDarkLabel("Cols:"));
        customPanel.add(colSpinner);
        customPanel.add(createDarkLabel("Mines:"));
        customPanel.add(mineSpinner);

        toggleCustomFields(false);

        // ActionListeners for RadioButtons
        ActionListener presetListener = e -> toggleCustomFields(false);
        easyBtn.addActionListener(presetListener);
        mediumBtn.addActionListener(presetListener);
        hardBtn.addActionListener(presetListener);

        customBtn.addActionListener(e -> toggleCustomFields(true));

        easyBtn.setSelected(true);

        // Start button
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startButton.setBackground(new Color(70, 130, 180)); // Steel Blue
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(100, 149, 237)); // Lighter Blue
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(70, 130, 180));
            }
        });

        startButton.addActionListener(e -> startGame());

        // Assembling the window
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBackground(BG_DARK);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainContainer.add(presetPanel, BorderLayout.NORTH);
        mainContainer.add(customPanel, BorderLayout.CENTER);
        mainContainer.add(startButton, BorderLayout.SOUTH);

        add(mainContainer);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JRadioButton createDarkRadioButton(String text) {
        JRadioButton btn = new JRadioButton(text);
        btn.setBackground(BG_DARK);
        btn.setForeground(TEXT_WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private JLabel createDarkLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_WHITE);
        return label;
    }

    private TitledBorder createDarkTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), title);
        border.setTitleColor(TEXT_WHITE);
        return border;
    }

    private JSpinner createDarkSpinner(int val, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(val, min, max, 1));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(BTN_DARK);
            tf.setForeground(TEXT_WHITE);
            tf.setCaretColor(TEXT_WHITE);
            tf.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }
        spinner.setBorder(BorderFactory.createEmptyBorder());

        return spinner;
    }

    private void toggleCustomFields(boolean enable) {
        rowSpinner.setEnabled(enable);
        colSpinner.setEnabled(enable);
        mineSpinner.setEnabled(enable);
    }

    private void startGame() {
        int r, c, m;
        boolean practise = practiceCheckBox.isSelected();
        Difficulty difficulty;

        if (easyBtn.isSelected()) {
            r = 9;
            c = 9;
            m = 10;
            difficulty = Difficulty.EASY;
        } else if (mediumBtn.isSelected()) {
            r = 16;
            c = 16;
            m = 40;
            difficulty = Difficulty.MEDIUM;
        } else if (hardBtn.isSelected()) {
            r = 16;
            c = 30;
            m = 99;
            difficulty = Difficulty.HARD;
        } else {
            r = (int) rowSpinner.getValue();
            c = (int) colSpinner.getValue();
            m = (int) mineSpinner.getValue();
            difficulty = Difficulty.CUSTOM;
        }

        if (m >= r * c - 9) {
            JOptionPane.showMessageDialog(this, "Too many mines for board size");
            return;
        }

        this.dispose();
        new Game(r, c, m, practise, difficulty);
    }
}