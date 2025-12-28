package Frontend;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class OptionPanel extends JPanel {
    private GameWindow gameWindow;
    private Map<String, Integer> keybindings = new HashMap<>();

    private boolean listeningForKey = false;
    private JButton currentKeyButton = null;
    private String currentActionName = null;
    private AWTEventListener globalMouseCapture;

    public OptionPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        
        keybindings.put("Move Left", KeyEvent.VK_Q);
        keybindings.put("Move Right", KeyEvent.VK_D);
        keybindings.put("Jump", KeyEvent.VK_Z);
        keybindings.put("Pause", KeyEvent.VK_ESCAPE);
        keybindings.put("Use", MouseEvent.BUTTON1);

        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 50));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(60, 60, 60));
        tabbedPane.setForeground(Color.WHITE);

        tabbedPane.addTab("Graphics", creerPanelAffichage());
        tabbedPane.addTab("Audio", creerPanelSon());
        tabbedPane.addTab("Gameplay", creerPanelCommandes());

        add(tabbedPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Retour au Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setPreferredSize(new Dimension(150, 40));
        backButton.addActionListener(e -> gameWindow.showMenuScreen());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(50, 50, 50));
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        
    }

    private JPanel creerPanelAffichage() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBackground(new Color(70, 70, 70));
        panel.setForeground(Color.WHITE);

        // Helper pour les labels
        JLabel titleLabel = new JLabel("Résolution :") {
            {
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.PLAIN, 62));
            }
        };
        panel.add(titleLabel);

        // Sélecteur de Résolution
        String[] resolutions = { "2560x1440", "2560x1080", "1920x1200", "1920x1080", "1680x1050", "1600x900",
                "1440x900", "1366x768", "1280x800", "1280x720", "1024x768" };
        JComboBox<String> resolutionSelector = new JComboBox<>(resolutions);
        Font selectorFont = new Font("Arial", Font.PLAIN, 62);
        resolutionSelector.setFont(selectorFont);
        resolutionSelector.setSelectedItem(gameWindow.getSize().width + "x" + gameWindow.getSize().height);
        resolutionSelector.addActionListener(e -> {
            String selectedRes = (String) resolutionSelector.getSelectedItem();
            appliquerResolution(selectedRes); // CRITÈRE D'ACCEPTATION
        });
        panel.add(resolutionSelector);

        JLabel modeLabel = new JLabel("Window Mode :") {
            {
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.PLAIN, 62));
            }
        };
        panel.add(modeLabel);
        // 1. Définir les options du mode de fenêtre
    // 1. Définir les options du mode de fenêtre
    String[] windowModes = {"Windowed", "Borderless", "Full Screen"};
    JComboBox<String> windowModeComboBox = new JComboBox<>(windowModes);
    windowModeComboBox.setFont(selectorFont); // Appliquer la police

    // 2. Définir l'ActionListener (inchangé)
    ActionListener modeListener = e -> {
        String selectedMode = (String) windowModeComboBox.getSelectedItem();
        appliquerModeAffichage(selectedMode.toUpperCase().replace(" ", "")); 
    };

    windowModeComboBox.addActionListener(modeListener);

    // 3. Utiliser GridBagLayout pour l'ancrage en bas et à gauche
    // REMPLACER FlowLayout par GridBagLayout
    JPanel windowSelector = new JPanel(new GridBagLayout()); 
    windowSelector.setBackground(new Color(70, 70, 70));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.SOUTHWEST; // Ancrer en bas (SOUTH) et à gauche (WEST)
    gbc.weightx = 1.0; // Pour prendre l'espace horizontal disponible
    gbc.weighty = 1.0; // Pour prendre l'espace vertical disponible
    gbc.insets = new Insets(0, 0, 80, 0); // Ajouter une petite marge en bas (10px)

    windowSelector.add(windowModeComboBox, gbc);
    
    panel.add(windowSelector); // Ajout du windowSelector à la grille principale

        // FPS Selector
        JLabel fpsLabel = new JLabel("Framerate (FPS) :") {
            {
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.PLAIN, 62));
            }
        };
        panel.add(fpsLabel);
        Integer[] fpsOptions = { 60, 50, 30 };
        JComboBox<Integer> fpsSelector = new JComboBox<>(fpsOptions);
        fpsSelector.setFont(selectorFont);
        panel.add(fpsSelector);

        return panel;
    }

    private JPanel creerPanelSon() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBackground(new Color(70, 70, 70));
        // Sliders for volume
        panel.add(new JLabel("Master Volume :") {
            {
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.PLAIN, 62));
            }
        });
        panel.add(new JSlider(0, 100, 50));

        panel.add(new JLabel("BGM :") {
            {
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.PLAIN, 62));
            }
        });
        panel.add(new JSlider(0, 100, 50));

        panel.add(new JLabel("VFX :") {
            {
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.PLAIN, 62));
            }
        });
        panel.add(new JSlider(0, 100, 50));

        return panel;
    }

    private JPanel creerPanelCommandes() {
       JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(70, 70, 70));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 20);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel invLabel = new JLabel("Inventory Mode :");
        invLabel.setForeground(Color.WHITE);
        invLabel.setFont(labelFont);
        panel.add(invLabel, gbc);

        gbc.gridx = 1;
        String[] invOptions = { "Individual", "Team / Shared" };
        JComboBox<String> invSelector = new JComboBox<>(invOptions);
        invSelector.addActionListener(e -> {
            boolean isTeam = invSelector.getSelectedIndex() == 1;
            // Met à jour la logique dans GameEngine via GameWindow
            if(gameWindow.getGameEngine() != null) {
                gameWindow.getGameEngine().setTeamInventoryMode(isTeam);
            }
            System.out.println("Inventory Mode changed to: " + invOptions[invSelector.getSelectedIndex()]);
        });
        panel.add(invSelector, gbc);

    // sepate options
    gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        
        // Les actions seront ajoutées ici, basées sur le HashMap
        int row = 2;
        for (Map.Entry<String, Integer> entry : keybindings.entrySet()) {
            String actionName = entry.getKey();
            int keyCode = entry.getValue();

            gbc.gridx = 0; gbc.gridy = row;
            JLabel actionLabel = new JLabel(actionName + " :");
            actionLabel.setForeground(Color.WHITE);
            actionLabel.setFont(labelFont);
            panel.add(actionLabel, gbc);

            gbc.gridx = 1;
            JButton keyButton = new JButton(KeyEvent.getKeyText(keyCode));
            keyButton.setFont(new Font("Arial", Font.PLAIN, 18));
            keyButton.addActionListener(e -> startKeyListening(actionName, keyButton));
            panel.add(keyButton, gbc);
            row++;
            ;

        }

        return panel;
    }

    private void startKeyListening(String actionName, JButton button) {
        if (listeningForKey)
            return;
        listeningForKey = true;
        currentActionName = actionName;
        currentKeyButton = button;

        button.setText("Appuyez sur une touche...");
        button.setBackground(Color.YELLOW);

        // Utilisez directement GameWindow pour ajouter le KeyListener
        gameWindow.addKeyListener(keyListener);
        globalMouseCapture = event -> {
        if (event instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) event;
            if (me.getID() == MouseEvent.MOUSE_PRESSED && listeningForKey) {
                // get the mouse button
                int mouseButton = me.getButton();
                keybindings.put(currentActionName, mouseButton);
                currentKeyButton.setText("Mouse " + mouseButton);
                
                // Consume mouse event to prevent the button from consuming
                me.consume();
                
                // stop listening
                SwingUtilities.invokeLater(this::stopKeyListening);
            }
        }
    };

    // activate the global mouse listener
    Toolkit.getDefaultToolkit().addAWTEventListener(globalMouseCapture, AWTEvent.MOUSE_EVENT_MASK);
        gameWindow.requestFocus(); // S'assurer que la fenêtre a le focus pour capturer l'événement
    }

    private void stopKeyListening() {
        listeningForKey = false;
        currentKeyButton.setEnabled(true);
        // Important: retirer le KeyListener pour éviter qu'il capture les touches après
        gameWindow.removeKeyListener(keyListener);
        gameWindow.removeMouseListener(mouseListener);
        if(globalMouseCapture !=null){
        Toolkit.getDefaultToolkit().removeAWTEventListener(globalMouseCapture);
        }
        globalMouseCapture= null;
        currentKeyButton.setBackground(Color.WHITE);
        currentKeyButton = null;
        currentActionName = null;

        // Réactive le KeyListener de la GameWindow pour le jeu (si on revient au jeu)
        gameWindow.requestFocus();
    }

    private KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (listeningForKey) {
                int newKeyCode = e.getKeyCode();

                // CRITÈRE D'ACCEPTATION : Sauvegarde dans le HashMap
                keybindings.put(currentActionName, newKeyCode);

                // Mise à jour de l'UI
                currentKeyButton.setText(KeyEvent.getKeyText(newKeyCode));

                stopKeyListening();

                // Vous pouvez ajouter ici une logique pour mettre à jour la GameWindow
                // afin que le jeu utilise la nouvelle liaison de touche.
                // Ex: gameWindow.updateKeyBinding(keybindings);
            }
        }
    };
    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (listeningForKey) {
                int mouseButton = e.getButton();
                keybindings.put(currentActionName, mouseButton);
                currentKeyButton.setText("Mouse " + mouseButton);
                stopKeyListening();
            }
        }
    };

    private void appliquerResolution(String resolution) {
        String[] parts = resolution.split("x");
        if (parts.length == 2) {
            try {
                int width = Integer.parseInt(parts[0]);
                int height = Integer.parseInt(parts[1]);
                // CRITÈRE D'ACCEPTATION : Appliquer le changement immédiatement
                gameWindow.setSize(width, height);
                gameWindow.setLocationRelativeTo(null); // Recentrer
            } catch (NumberFormatException e) {
                System.err.println("Format de résolution invalide.");
            }
        }
    }

    private void appliquerModeAffichage(String mode) {

        // Obtenir le périphérique d'affichage par défaut (l'écran principal)
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        // 1. Définir le style de décoration (bordure)
        boolean isUndecorated = mode.equals("FULLSCREEN") || mode.equals("BORDERLESS");

        // Si l'état de décoration doit changer
        if (gameWindow.isUndecorated() != isUndecorated) {
            // Étapes critiques pour changer la décoration
            gameWindow.setVisible(false);
            gameWindow.dispose();
            gameWindow.setUndecorated(isUndecorated);
        }

        // --- 2. Gérer la Taille et l'État de la Fenêtre ---

        if (mode.equals("FULLSCREEN")) {
            // Quitter le mode exclusif s'il était déjà actif
            if (gd.getFullScreenWindow() == gameWindow) {
                gd.setFullScreenWindow(null);
            }

            // S'assurer que la fenêtre est en état NORMAL avant de la passer en Plein Écran
            // Exclusif
            gameWindow.setExtendedState(JFrame.NORMAL);

            // Cacher la fenêtre temporairement avant de changer le mode exclusif (bonne
            // pratique)
            gameWindow.setVisible(false);

            // Définir le plein écran exclusif (la méthode recommandée)
            gd.setFullScreenWindow(gameWindow);

        } else {
            // Si on quitte le mode Plein Écran Exclusif
            if (gd.getFullScreenWindow() == gameWindow) {
                gd.setFullScreenWindow(null); // Relâcher le contrôle exclusif
            }

            // S'assurer que l'état n'est pas maximisé avant de définir les dimensions
            gameWindow.setExtendedState(JFrame.NORMAL);

            if (mode.equals("BORDERLESS")) {

                // --- NOUVEAU: Utilisation directe des dimensions du GraphicsDevice ---
                // On utilise la configuration par défaut pour obtenir les dimensions maximales.
                // Le GraphicsConfiguration renvoie la taille et la position de l'écran.

                int screenWidth = gd.getDefaultConfiguration().getBounds().width;
                int screenHeight = gd.getDefaultConfiguration().getBounds().height;

                // Définir la taille et la position
                gameWindow.setLocation(0, 0);
                gameWindow.setSize(screenWidth, screenHeight);

            } else if (mode.equals("WINDOWED")) {
                // Revenir au mode fenêtré normal

                // Restauration de la taille par défaut ou d'une taille mémorisée
                gameWindow.setSize(gameWindow.getSize().width, gameWindow.getSize().height);
                gameWindow.setLocationRelativeTo(null); // Centrer la fenêtre
            }
        }

        // --- 3. Afficher et Restaurer le Focus ---

        // Rendre la fenêtre visible si elle ne l'était pas
        if (!gameWindow.isVisible()) {
            gameWindow.setVisible(true);
        }

        gameWindow.requestFocus();
    }
    public Map<String, Integer> getKeybindings() {
    return keybindings;
}
}
