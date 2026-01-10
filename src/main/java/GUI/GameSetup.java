package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameSetup extends JFrame {

    // Components we need to access later
    private final JSpinner rowSpinner;
    private final JSpinner colSpinner;
    private final JSpinner mineSpinner;
    private final JRadioButton easyBtn;
    private final JRadioButton mediumBtn;
    private final JRadioButton hardBtn;

    public GameSetup() {
        setTitle("New Game Setup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Presets panel
        JPanel presetPanel = new JPanel(new GridLayout(4, 1));
        presetPanel.setBorder(BorderFactory.createTitledBorder("Choose Difficulty"));

        ButtonGroup group = new ButtonGroup();
        easyBtn = new JRadioButton("Easy (9x9, 10 Mines)");
        mediumBtn = new JRadioButton("Medium (16x16, 40 Mines)");
        hardBtn = new JRadioButton("Hard (16x30, 99 Mines)");
        JRadioButton customBtn = new JRadioButton("Custom");

        // Grouping the buttons so only one can be selected
        group.add(easyBtn);
        group.add(mediumBtn);
        group.add(hardBtn);
        group.add(customBtn);

        presetPanel.add(easyBtn);
        presetPanel.add(mediumBtn);
        presetPanel.add(hardBtn);
        presetPanel.add(customBtn);

        // Custom input panel
        JPanel customPanel = new JPanel(new FlowLayout());
        customPanel.setBorder(BorderFactory.createTitledBorder("Custom Settings  [min 5,5,1]  [max 16,30,99]"));

        rowSpinner = createSpinner(9, 5, 16);
        colSpinner = createSpinner(9, 5, 30);
        mineSpinner = createSpinner(10, 1, 99);

        customPanel.add(new JLabel("Rows:"));
        customPanel.add(rowSpinner);
        customPanel.add(new JLabel("Cols:"));
        customPanel.add(colSpinner);
        customPanel.add(new JLabel("Mines:"));
        customPanel.add(mineSpinner);

        // Disable custom fields by default
        enableCustomFields(false);

        // ActionListeners for RadioButtons
        ActionListener presetListener = e -> enableCustomFields(false);
        easyBtn.addActionListener(presetListener);
        mediumBtn.addActionListener(presetListener);
        hardBtn.addActionListener(presetListener);

        // Enable spinners ONLY when "Custom" is clicked
        customBtn.addActionListener(e -> enableCustomFields(true));

        // Select Easy by default
        easyBtn.setSelected(true);

        // Start button
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.addActionListener(e -> startGame());

        // assembling the window
        add(presetPanel, BorderLayout.NORTH);
        add(customPanel, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        // Auto-size window
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Helper method for creating spinners quickly
    private JSpinner createSpinner(int val, int min, int max) {
        return new JSpinner(new SpinnerNumberModel(val, min, max, 1));
    }

    // Enable? custom fields
    private void enableCustomFields(boolean enable) {
        rowSpinner.setEnabled(enable);
        colSpinner.setEnabled(enable);
        mineSpinner.setEnabled(enable);
    }

    // Starting the Game
    private void startGame() {
        int r, c, m;

        if (easyBtn.isSelected()) {
            r = 9;
            c = 9;
            m = 10;
        } else if (mediumBtn.isSelected()) {
            r = 16;
            c = 16;
            m = 40;
        } else if (hardBtn.isSelected()) {
            r = 16;
            c = 30;
            m = 99;
        } else {
            // Get values from spinners
            r = (int) rowSpinner.getValue();
            c = (int) colSpinner.getValue();
            m = (int) mineSpinner.getValue();
        }

        // Check if picked amount of mines fits on the board
        if (m >= r * c) {
            JOptionPane.showMessageDialog(this, "Too many mines for board size");
        }

        this.dispose();
        new Game(r, c, m);
    }
}