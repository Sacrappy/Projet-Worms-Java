package Frontend;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import Backend.GameEngine;
import Backend.ActorModel;
import Backend.Pair;
import Backend.Team;


public class HUD {
    private BufferedImage playerIcon;
    private GameEngine gameEngine;
    private LinkedList<ActorModel> turns = new LinkedList<ActorModel>();
    private int currentPlayerID = 0;
    // private Inventory inventory; (to be implemented later)

    public HUD() {
        try {
            this.playerIcon = loadHUDImage("/Images/Characters/CharacterTest.png");// Change when implementing choosable characters
        } catch (IOException e) {
            e.printStackTrace();
            this.playerIcon = null; // or handle error appropriately
        }
        // this.inventory
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    private BufferedImage loadHUDImage(String path) throws IOException {
        BufferedImage img = null;
        // Try to load as a resource first (path should start with '/' from resources)
        URL imgUrl = getClass().getResource(path);
        if (imgUrl != null) {
            img = ImageIO.read(imgUrl);
        } else {
            // Fallback: treat path as a file system path
            File file = new File("src"+path);
            if(!file.exists()){
                file = new File("ressources"+path);
            }
            if (file.exists()) {
                img = ImageIO.read(file);
            } 
            if (img == null) {
                throw new IOException("Image not found at resource or file path: " + path);
            }
        }
        return img;
    }

    private String timeFormat(int time){
        if (time < 60){
            return time + "s";
        }
        if (time < 3600) {
            return time/60 + "m" + time % 60 + "s";
        }
        return time/3600 + "h" + time % 60 + "m" + time % 60 + "s";
    }

    private void definePlayerID(){
        //Transforme la liste de Pair de GameEngine en liste d'ActorModel correspondants
        turns.removeAll(turns);
        for(Pair p : gameEngine.getTurnOrder()){
            if(gameEngine.getPlayerFromPair(p).isAlive()) {
                turns.add(gameEngine.getPlayerFromPair(p));
            }
        }

        //Fait bouger la liste jusqu'à ce que le 1er élément soit plus ou moins au milieu.
        int len = turns.size()/2;
        for(int i = 0; i < len; i++){
            ActorModel player = turns.removeFirst();
                turns.add(player);
        }
        currentPlayerID = len;
    }

    public void draw(Graphics g, int panelHeight){
        definePlayerID();
        if (playerIcon == null || gameEngine == null) {
            return; // Cannot draw HUD without player icon or game engine
        }
        ActorModel currentplayer = gameEngine.getCurrentPlayer();
        if (currentplayer==null) return; // do not draw HUD for a dead/ non existant player
        Graphics2D g2d = (Graphics2D) g;

        // enables antialiasing so that the circles look better
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //draw the portrait
        int portraitSize = 100;
        int padding = 10;//padding from the edges
        int x = padding;
        int y = panelHeight-portraitSize - padding;
        g2d.setColor(new Color(30,30,30,200)); 
        g2d.fillOval(x,y,portraitSize,portraitSize);
        if(playerIcon != null){
            int iconSize = (int)(portraitSize*0.7); // get the icon smaller than the circle
            int iconOffset =(portraitSize-iconSize)/2;
            g.drawImage(playerIcon,x+ iconOffset, y +iconOffset, iconSize,iconSize, null);
        }

        //make a golden border for the cirle
        g2d.setColor(new Color(200,180,120));
        g2d.setStroke(new BasicStroke(3));// width of the golden border
        g2d.drawOval(x,y, portraitSize,portraitSize);

        //draw the hp bar
        int hpBarX = x+ portraitSize + 10;
        int hpBarY = y + 10;
        int barWidth= 200 ;
        int barHeight= 10;
        g2d.setColor(new Color(80,0,0));// drawing missing HP
        g2d.fillRect(hpBarX,hpBarY,barWidth,barHeight);

        float hpRatio =(float) gameEngine.getCurrentPlayer().getHP() / gameEngine.getCurrentPlayer().getMaxHP();//ratio of current hp
        int currentHpWidth= (int) (barWidth*hpRatio);

        g2d.setColor(new Color(200,20,20));// brighter red than missing hp
        g2d.fillRect(hpBarX, hpBarY, currentHpWidth, barHeight);// covers the missing hp bar by current hp bar

        //make a golden border for the hp Bar
        g2d.setColor(new Color(200,180,120));
        g2d.setStroke(new BasicStroke(3));// width of the golden border
        g2d.drawRect(hpBarX,hpBarY, barWidth,barHeight);

        //Affichage du temps global.
        g2d.setColor(new Color(255, 255, 255, 255));
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString(timeFormat((int)GameWindow.elapsed/1000),20,30);

        //affichage du décompte du tour.
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.setColor(new Color(179, 23, 23, 255));
        if(gameEngine.getTimeRemaining()/1000 < 10) {//affichage lorsqu'il n'y a qu'un chiffre
            g.drawString(gameEngine.getTimeRemaining() / 1000 + "", gameEngine.getTerrain().getImage().getWidth() / 2 - 235, 110);
        }
        else{//affichage s'il y a au moins 2 chiffres.
            g.drawString(gameEngine.getTimeRemaining()/1000 + "", gameEngine.getTerrain().getImage().getWidth() / 2 - 245, 110);
        }

        //Affichage des tours.
        g2d.setColor(Color.BLACK);
        double x2 = gameEngine.getTerrain().getImage().getWidth()/2.0 - 12.0 * turns.size();
        y = 20;
        int x1 = 500;
        int valueAdd = (gameEngine.getTerrain().getImage().getWidth()/2) - ((turns.size())/2 + 5) * 50;
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        for(int j = 0; j < turns.size(); j++){
            xValues.add(valueAdd + j * 50);
        }
        int i = 0;
        while (i < xValues.size()) {
            ActorModel player = turns.get(i);

            //Contour doré autour du joueur actif
            if(i == currentPlayerID){
                g2d.setColor(new Color(227, 196, 48, 255));
                g.fillRect(xValues.get(i) - 2,y - 2,player.getWidth()*4 + 4,player.getHeight()*3 + 4);
            }
            else{//Contour blanc autour de tout autre joueur
                g2d.setColor(new Color(255, 255, 255));
                g.fillRect(xValues.get(i) -2,y - 2,player.getWidth()*4 + 4,player.getHeight()*3 + 4);
            }
            g2d.setColor(Color.BLACK);

            if (player.getIcon() != null) {
                g.drawImage(player.getIcon(), xValues.get(i),y, player.getWidth() *4, player.getHeight()*3, null);
            }
            else{
                g.fillRect(xValues.get(i),y,player.getWidth()*4,player.getHeight()*4);
            }
            //x1 += (int) (12 * turns.size()/2.0);
            i++;
        }



        //Add the drawing of the inventory here once it is implemented
        


    }
}
    

