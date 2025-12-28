package Frontend;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GameModeSelectionPanel extends JPanel{
    private GameWindow gameWindow;

    public GameModeSelectionPanel(GameWindow gameWindow){
        this.gameWindow = gameWindow;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(50,50,50));

        JLabel titre = new JLabel("Select Game Mode");
        titre.setForeground(Color.WHITE);
        titre.setFont(new Font("Arial", Font.BOLD, 36));
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton graphicalModeButton = new JButton("Graphical Mode");
        JButton ConsoleModeButton = new JButton("Console Mode");
        JButton backButton = new JButton("Back");

        Font buttonFont = new Font("Arial", Font.PLAIN, 24);
        for (JButton b : new JButton[]{graphicalModeButton, ConsoleModeButton, backButton}) {
            b.setFont(buttonFont);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(350, 50));
            b.setContentAreaFilled(false);
            b.setForeground(Color.WHITE);
            b.setBorderPainted(false);
        }

        add(Box.createVerticalGlue());
        add(titre);
        add(Box.createRigidArea(new Dimension(0, 40)));
        add(Box.createVerticalGlue());

        add(graphicalModeButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(Box.createVerticalGlue());

        add(ConsoleModeButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(Box.createVerticalGlue());

        add(backButton);
        add(Box.createVerticalGlue());

        graphicalModeButton.addActionListener(e -> gameWindow.showGameScreen());
        ConsoleModeButton.addActionListener(e -> {
            try {
                gameWindow.startGameTextMode();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        backButton.addActionListener(e->{gameWindow.showMenuScreen();});
    }
    
}
