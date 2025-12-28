package Frontend;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {

    public GameOverPanel(GameWindow gameWindow, String winnerName) {
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 30, 240)); // Dark background (semi-transparent)

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 20, 0); // spacing
        gbc.anchor = GridBagConstraints.CENTER;
        // Annoncing Winners
        gbc.gridy++;
        JLabel winnerLabel = new JLabel("Winners : " + winnerName);
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        winnerLabel.setForeground(Color.WHITE);
        add(winnerLabel, gbc);

        // Add spaces
        gbc.gridy++;
        add(Box.createRigidArea(new Dimension(0, 40)), gbc);

        //  Replay Button
        gbc.gridy++;
        JButton replayButton = createStyledButton("Play Again (Same Map)");
        replayButton.addActionListener(e -> gameWindow.replayGame());
        add(replayButton, gbc);

        // Home Menu Button
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0); // adds more space
        JButton menuButton = createStyledButton("Back to Menu");
        menuButton.addActionListener(e -> gameWindow.showMenuScreen());
        add(menuButton, gbc);
    }

    private JButton createStyledButton(String text) { //creates a button from a String
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 20));
        btn.setPreferredSize(new Dimension(300, 50));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        return btn;
    }
}