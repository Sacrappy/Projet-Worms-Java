package Frontend;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
public class Menu extends JPanel {
    private GameWindow gameWindow;
    private Image backgroundImage;
    private Clip MenuMusic;
    public Menu(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        loadBackgroundImage();
        startMusic("lien vers le fichier musique");


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(30,30,30)); //Remove when background image is added


        JLabel titre = new JLabel("Worms Like");
        titre.setForeground(Color.WHITE);
        titre.setFont(new Font("Arial", Font.BOLD, 48));
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton startButton = new JButton("Start Game");
        JButton loadButton = new JButton("Load Game");
        JButton optionsButton = new JButton("Options");
        JButton exitButton = new JButton("Exit");
        
        Font policeBouton = new Font("Arial", Font.PLAIN, 24);
        
        for (JButton b : new JButton[]{startButton, loadButton, optionsButton, exitButton}) {
            b.setFont(policeBouton);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(200, 50));
        }

        add(Box.createVerticalGlue());
        add(titre);
        add(Box.createRigidArea(new Dimension(0, 40)));
        add(Box.createVerticalGlue());
        
        add(loadButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(Box.createVerticalGlue());
        
        add(startButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(Box.createVerticalGlue());
        
        add(optionsButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(Box.createVerticalGlue());
        
        add(exitButton);
        add(Box.createVerticalGlue());

        loadButton.addActionListener(e->loadGame());
        startButton.addActionListener(e->{ 
            if(MenuMusic !=null){
            MenuMusic.stop();
        }
        gameWindow.showMapSelectionScreen(); });  

        optionsButton.addActionListener(e -> showOptions() );
        exitButton.addActionListener(e -> System.exit(0));
       
         
    
}
    private void loadBackgroundImage() {
        try{
            URL imgUrl = getClass().getResource("/Images/Backgrounds/BackGround_Test.png");
            if(imgUrl!=null)
            backgroundImage = ImageIO.read(imgUrl);
            else{
                System.err.println("No image found");
                setBackground(new Color(30,30,30));
        }
    } catch (IOException e) {
            e.printStackTrace();
            setBackground(new Color(30,30,30));
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void startMusic(String musicFilePath) {
        try {
            URL musicUrl = getClass().getResource(musicFilePath);
            if (musicUrl != null) {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(musicUrl);
            MenuMusic = AudioSystem.getClip();
            MenuMusic.open(audioIn);
            MenuMusic.loop(Clip.LOOP_CONTINUOUSLY);
            MenuMusic.start();
                
            }
            else{System.err.println("Music file not found: " + musicFilePath);}
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            System.err.println("Error playing music: " + e.getMessage());
        }
}

    private void loadGame() {
        JOptionPane.showMessageDialog(this, "Load game functionality is not implemented yet.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
    }


    private void showOptions() {
        gameWindow.showOptionsScreen();
    }
}
