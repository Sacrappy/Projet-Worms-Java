package Frontend;

import javax.swing.*;

import Backend.*;

import java.awt.*;
import java.net.URL;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;



public class TerrainPanel extends JPanel {
    private GameEngine gameEngine;
    private Backend.Terrain ground;
    private BufferedImage imageCharacter;
    private HUD hud;

    public TerrainPanel (){
        //imageCharacter = loadBufferedImage("/Images/Characters/CharacterTest.png");
        hud = new HUD();
    }

    public void setGameEngine(GameEngine engine){
        this.gameEngine = engine;
        if(hud != null) hud.setGameEngine(engine);
    }

    public void setTerrain(Backend.Terrain ground){
        this.ground = ground;
    }
    public Backend.Terrain getGround(){
        return this.ground;
    }

    public BufferedImage loadBufferedImage(String path) {
        /*
         * Public method to load a BufferedImage from a path given as arguments:
         */        try {
            BufferedImage img = null;
            // Try to load as a resource first (path should start with '/' from resources)
            URL imgUrl = getClass().getResource(path);
            if (imgUrl != null) {
                img = ImageIO.read(imgUrl);
            } else {
                // Fallback: treat path as a file system path
                File file = new File(path);
                if (file.exists()) {
                    img = ImageIO.read(file);
                } else {
                    throw new IOException("Image not found at resource or file path: " + path);
                }
            }
            return img;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (ground == null) return;
        // Draw the terrain
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        Image bgImage = ground.getBackground();

        if(bgImage != null){
            g.drawImage(bgImage, 0, 0, panelWidth, panelHeight, this); // Draw the terrain background

        }

        g.drawImage(ground.getImage(), 0, 0, getWidth(),getHeight(),this); // Draw the terrain foreground

        if(gameEngine != null){
            int terrainWidth = ground.getImage().getWidth();
            int terrainHeight = ground.getImage().getHeight();
            float scaleX = (float) getWidth() / terrainWidth; // convert world coord to screen coord
            float scaleY = (float)getHeight() / terrainHeight;

            for (Team team : gameEngine.getTeams()){
                for (ActorModel player : team.getTeammates()){
                    boolean isActive = (player == gameEngine.getCurrentPlayer());
                    player.draw(g, scaleX, scaleY, isActive); // draw the players
                }
            }

            Projectile proj = gameEngine.getActiveProjectile();
            if(proj != null ){
                proj.draw(g, scaleX, scaleY); // draw the projectile
            }

            if(gameEngine != null && hud !=null) {
                hud.draw(g, panelHeight);
            }
        }
    }

}
