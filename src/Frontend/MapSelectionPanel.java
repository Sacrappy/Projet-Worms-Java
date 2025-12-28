package Frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.IOException;

public class MapSelectionPanel extends JPanel {

    private GameWindow gameWindow;
    private JComboBox<String> mapComboBox;
    private JComboBox<String> backgroundComboBox;
    private JButton nextButton;
    private ImagePreviewPanel previewPanel;

    // options for maps and backgrounds
    // index 0 is default "choose" option
    private final String[] maps = {"* Choose Map *","AncientTree","Buildings","Spaceship","TheBridge","ThePillar"};
    private final String[] backgrounds = {"* Choose Backgorund *","City","Forest","Mountains","Sky1","Sky2","Space"};

    public MapSelectionPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new GridBagLayout()); // GridBagLayout for precise positioning
        setBackground(new Color(30, 30, 30)); // Background color , might be replaced by an image later

        GridBagConstraints gbc = new GridBagConstraints();
        
        // --- 1. ComboBox Configuration  (Menus déroulants) ---
        mapComboBox = new JComboBox<>(maps);
        styleComboBox(mapComboBox);

        backgroundComboBox = new JComboBox<>(backgrounds);
        styleComboBox(backgroundComboBox);

        // --- 2. Buttuon Placement ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Ancred at the top left
        gbc.insets = new Insets(50, 50, 0, 0); // All around margins
        add(new JLabel("Select Map :") {{ setForeground(Color.WHITE); }}, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 50, 0, 0);
        add(mapComboBox, gbc);

        // --- 3. Leave space for previsualisation ---
        previewPanel = new ImagePreviewPanel(300, 300);
        GridBagConstraints gbcPreview = new GridBagConstraints();
        gbcPreview.gridx = 0;
        gbcPreview.gridy = 2;
        gbcPreview.gridwidth = 2; // Spans two columns
        gbcPreview.weightx = 1.0; // Take as much horizontal space as possible
        gbcPreview.weighty = 1.0; // Take as much vertical space as possible
        gbcPreview.fill = GridBagConstraints.BOTH; // Stretches in both directions
        gbcPreview.insets = new Insets(20, 50, 20, 50); // Margins
        add(previewPanel, gbcPreview);

        // Leave space between the two selection buttons
        gbc.gridy = 2;
        gbc.weighty = 1.0; // will take as much vertical space as possible
        add(Box.createGlue(), gbc); 

        // --- 4. Placing Background selection button ---
        gbc.gridy = 3;
        gbc.weighty = 0; 
        gbc.insets = new Insets(0, 50, 0, 0);
        add(new JLabel("Selectionner le Fond :") {{ setForeground(Color.WHITE); }}, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 50, 50, 0); // Margins
        add(backgroundComboBox, gbc);


        // --- 5. Add a next button on the bottom right ---
        nextButton = new JButton("Next");
        styleButton(nextButton);
        nextButton.setEnabled(false); // Grey by default until valid selections

        GridBagConstraints gbcBtn = new GridBagConstraints();
        gbcBtn.gridx = 1; // right column
        gbcBtn.gridy = 5; // at the bottom
        gbcBtn.weightx = 1.0; // Pushes to the right
        gbcBtn.anchor = GridBagConstraints.SOUTHEAST; // anchored to bottom right
        gbcBtn.insets = new Insets(0, 0, 30, 30); // Margins
        add(nextButton, gbcBtn);


        // --- 6. Logic behind selectors (using listeners) ---
        ActionListener selectionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePreview();
                checkSelection();
            }
        };

        mapComboBox.addActionListener(selectionListener);
        backgroundComboBox.addActionListener(selectionListener);

        // Next button action
        nextButton.addActionListener(e -> {
            // Load selected options
            String selectedMap = (String) mapComboBox.getSelectedItem();
            String selectedBg = (String) backgroundComboBox.getSelectedItem();
            
            // Give the selections to the GameWindow
            gameWindow.setSelectedMapPath("/Images/Maps/" + selectedMap+".png");
            gameWindow.setSelectedBackgroundPath("/Images/Backgrounds/" + selectedBg+".png");
            
            // Go to Game mode selection panel
            gameWindow.showGameModeSelection();
        });
    }
    //Update the preview panel based on current selections
    private void updatePreview() {
        String mapName= (String) mapComboBox.getSelectedItem();
        String bgName= (String) backgroundComboBox.getSelectedItem();
        int mapIndex = mapComboBox.getSelectedIndex();
        int bgIndex = mapComboBox.getSelectedIndex();
        BufferedImage mapImg = null;
        BufferedImage bgImg = null;

        // Load map image if valid selection
        if (mapIndex > 0) { // index 0 is default "choose"
            mapImg = loadImage("/Images/Maps/" + mapName + ".png");
        }
        // Load background image if valid selection
        if (bgIndex > 0) { // index 0 is default "choose"
            bgImg = loadImage("/Images/Backgrounds/" + bgName + ".png");
        }
        // send to preview panel
        previewPanel.setImage(bgImg, mapImg);
    }
    //Helper method to load image from resources
    private BufferedImage loadImage(String path) {
        try {
            URL imgUrl = getClass().getResource(path);
            if (imgUrl != null) {
                return ImageIO.read(imgUrl);
            } else {
                System.err.println("Image not found: " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Check if both selections are valid to enable the next button
    private void checkSelection() {
        boolean mapSelected = mapComboBox.getSelectedIndex() > 0; // Index 0 is base "choose" option
        boolean bgSelected = backgroundComboBox.getSelectedIndex() > 0; // Index 0  is base option

        nextButton.setEnabled(mapSelected && bgSelected);
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setPreferredSize(new Dimension(200, 30));
        box.setBackground(Color.WHITE);
        box.setForeground(Color.BLACK);
    }
    private void styleButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(150, 50));
        // Button is greyed automatically by setEnabled(false)
    }


}   
