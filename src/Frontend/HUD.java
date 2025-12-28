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
import Backend.ObjectModel;


public class HUD {
    private BufferedImage playerIcon;
    private GameEngine gameEngine;
    private LinkedList<ActorModel> turns = new LinkedList<ActorModel>();
    private int currentPlayerID = 0;
    // private Inventory inventory; (to be implemented later)

    public HUD() {
        try {
            this.playerIcon = loadHUDImage("/Images/CharactersPreview/Ver_Punk.png");// Change when implementing choosable characters
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
    public void drawKnifePreview(Graphics2D g2d, ActorModel player, float currentPower) {
    int range = 40;
    int arcAngle = 60;
    
    // Player center position
    int x = player.getX() - range + (player.getWidth() / 2);
    int y = player.getY() - range + (player.getHeight() / 2);
    
    // player facing
    int startAngle = (player.getFacing() == 1) ? -arcAngle/2 : 180 - arcAngle/2;

    // make the power gauge color vary from white to red based on power
    int red = (int) (150 + (currentPower / 100.0) * 105);
    g2d.setColor(new Color(red, 50, 50, 100)); // Transparent

    // draw the arc hitbox
    g2d.fillArc(x, y, range * 2, range * 2, -startAngle, -arcAngle);
    
    // draw the arcborder
    g2d.setColor(new Color(255, 255, 255, 200));
    g2d.drawArc(x, y, range * 2, range * 2, -startAngle, -arcAngle);
}
    public void draw(Graphics g,int panelWidth, int panelHeight){
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
        int avatarSize = 40;
        int spacing = 10;
        int totalTurnsWidth = turns.size() * (avatarSize + spacing);
        int turnStartX=(panelWidth - totalTurnsWidth) / 2;
        int turnY = 20;
        for (int i = 0; i < turns.size(); i++) {
            ActorModel player = turns.get(i);
            int currentX = turnStartX + i * (avatarSize + spacing);

            //Contour doré autour du joueur actif
            if(i == currentPlayerID){
                g2d.setColor(new Color(227, 196, 48, 255));
                g2d.setStroke(new BasicStroke(3));
                g.fillRect(currentX - 2,turnY - 2,avatarSize + 4,avatarSize + 4);
            }
            else{//Contour blanc autour de tout autre joueur
                g2d.setColor(new Color(255, 255, 255));
                g.fillRect(currentX -2,turnY - 2,avatarSize + 4,avatarSize + 4);
            }
            g2d.setColor(Color.BLACK);

            if (player.getIcon() != null) {
                g.drawImage(player.getIcon(), currentX,turnY, avatarSize, avatarSize, null);
            }
            else{
                g.fillRect(currentX,turnY,player.getWidth()*4,player.getHeight()*4);
            }
        }
        


        //Add the drawing of the inventory here once it is implemented
        
        List<ObjectModel> itemsToDisplay =(gameEngine.isTeamInventory())? gameEngine.getCurrentTeam().getTeamInventory() : gameEngine.getCurrentPlayer().getInventory() ;
        int slotSize = 50;
        int startX = (panelWidth - (8 * slotSize)) / 2; // Centred
        int yInv = panelHeight - slotSize - 20;

        for (int a = 0; a < 8; a++) {
           // draw the inventory slots
        int currentSlotX = startX + (a * slotSize);
        g2d.setColor(new Color(50, 50, 50, 200));
        g2d.fillRect(startX + (a * slotSize), yInv, slotSize, slotSize);
        if (a == gameEngine.getSelectedSlot()) {
            g2d.setColor(new Color(100, 100, 100, 255));
        }
        g2d.fillRect(currentSlotX, yInv, slotSize, slotSize);     
            if(a < itemsToDisplay.size()){
                ObjectModel item = itemsToDisplay.get(a);
                if (item.getIcon() != null) {
                    g.drawImage(item.getIcon(), currentSlotX, yInv, slotSize, slotSize, null);
                } else {
                    // Draw a placeholder if no icon is available
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(startX + (a * slotSize), yInv, slotSize, slotSize);
                }
            }
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(currentSlotX, yInv, slotSize, slotSize);

        // Wrap the permanent slots in gold
        if (a < 2) {
            g2d.setColor(new Color(255, 215, 0, 100)); // gold border
            g2d.drawRect(startX + (a * slotSize) + 2, yInv + 2, slotSize - 4, slotSize - 4);
        }
    }


    }
}
    

