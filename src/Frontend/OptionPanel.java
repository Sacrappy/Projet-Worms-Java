package Frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;

public class OptionPanel extends JPanel {
    private GameWindow gameWindow;
    private Map<String, Integer> keybindings = new HashMap<>();

    private boolean listeningForKey = false;
    private JButton currentKeyButton = null;
    private String currentActionName = null;

    public OptionPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
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

        keybindings.put("Move Left", KeyEvent.VK_LEFT);
        keybindings.put("Move Right", KeyEvent.VK_RIGHT);
        keybindings.put("Jump", KeyEvent.VK_UP);
        keybindings.put("Shoot", MouseEvent.BUTTON1);
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
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10)); // 0 ligne, 2 colonnes
        panel.setBackground(new Color(70, 70, 70));

        // Les actions seront ajoutées ici, basées sur le HashMap
        for (Map.Entry<String, Integer> entry : keybindings.entrySet()) {
            String actionName = entry.getKey();
            int keyCode = entry.getValue();

            panel.add(new JLabel(actionName + " :") {
                {
                    setForeground(Color.WHITE);
                }
            });

            JButton keyButton = new JButton(KeyEvent.getKeyText(keyCode));
            keyButton.addActionListener(e -> startKeyListening(actionName, keyButton));
            panel.add(keyButton);
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

        // Utilisez directement GameWindow pour ajouter le KeyListener
        gameWindow.addKeyListener(keyListener);
        gameWindow.requestFocus(); // S'assurer que la fenêtre a le focus pour capturer l'événement
    }

    private void stopKeyListening() {
        listeningForKey = false;
        currentKeyButton.setEnabled(true);
        // Important: retirer le KeyListener pour éviter qu'il capture les touches après
        gameWindow.removeKeyListener(keyListener);
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
}
