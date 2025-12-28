package Backend;

import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;


public class Terrain {
    VolatileImage background;
    BufferedImage terrain;


    public Terrain(String backgroundPath, String terrainPath) throws IOException {
        /*
         * Constructeur public pour Terrain.
         * arguments:
         * - backgroundPath: chemin vers l'image de fond (VolatileImage)
         * - terrainPath: chemin vers l'image du terrain (BufferedImage)
         */
        this.background = createVolatileImage(backgroundPath);
        this.terrain = loadBufferedImage(terrainPath);

    }

    public static BufferedImage loadBufferedImage(String path) throws IOException {
        /*
         * Méthode publique pour charger une image BufferedImage depuis un chemin donné.
         * arguments:
         * - path: chemin vers l'image (String)
         * retourne: BufferedImage chargé
         */

        BufferedImage img = null;
        // Try to load as a resource first (path should start with '/' from resources)
        URL imgUrl = Terrain.class.getResource(path);
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

    public  VolatileImage createVolatileImage(String path) throws IOException {
        /*
         * Méthode publique pour créer une VolatileImage depuis un chemin donné.
         * arguments:
         * - path: chemin vers l'image (String)
         * retourne: VolatileImage créé
         */
        BufferedImage img = loadBufferedImage(path);
        VolatileImage vImg = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleVolatileImage(img.getWidth(), img.getHeight());
        vImg.getGraphics().drawImage(img, 0, 0, null);
        return vImg;
    }

    public float getHeightBelow(float x, float y, int width , int height) {
        /*
         * Méthode publique pour obtenir la hauteur du terrain en dessous d'une position
         * donnée.
         * arguments:
         * - x: coordonnée x (int)
         * - y: coordonnée y actuelle (int)
         * retourne: hauteur du terrain en dessous (int)
         */

        int intX = (int) x;
        int endX= intX + width;
        int highestSurfaceY = terrain.getHeight();// looking for lowest y (highest surface)
        for (int col = intX; col < endX; col++) {// scan each pixel column from player width
            if (col < 0 || col >= terrain.getWidth()) {
                continue; // skip out of bounds columns
            }
            int searchStartY= (int) y; // start search at the head of character
            searchStartY = Math.max(0, searchStartY);
            for (int row = searchStartY; row < terrain.getHeight(); row++) {
                if (isPixelSolid(col, row)) { // found the surface for column
                    if (row < highestSurfaceY) {// is surface higher than previous found
                        highestSurfaceY = row;
                    }
                    break; // found the surface for this column, move to next column
                }
            }
        }

        return highestSurfaceY;
    }

    public boolean inBounds(int x, int y){
        return x >= 0 && y >= 0 && x < terrain.getWidth() && y < terrain.getHeight();
    }

    public boolean isOnGround(ActorModel character) {
        /*
         * Méthode publique pour vérifier si le personnage est au sol.
         * arguments:
         * - character: instance de src.ActorModel représentant le personnage
         * retourne: true si le personnage est au sol, false sinon
         */
        float characterBottomY = character.getY() + character.height;
        float terrainHeightAtCharacterX = getHeightBelow(character.getX(), characterBottomY,character.getWidth(), character.getHeight());
        return characterBottomY >= terrainHeightAtCharacterX;
    }

    public boolean isColliding(ActorModel character) {
        return isColliding(character.getX(), character.getY(), character.width, character.height);
    }

    public boolean isColliding(float x, float y , ActorModel character) {
        int charX = Math.round(x);
        int charY = Math.round(y);
        int width = character.getWidth();
        int height = character.getHeight();

        if (isWater(charX, charY + height)) {
            character.dies();
            return false;
        }

        // On parcourt les pixels du personnage
        for (int px = charX; px < charX + width; px++) {
            for (int py = charY; py < charY + height; py++) {
                // Vérifie les limites de la map
                if (px < 0 || px >= terrain.getWidth() || py < 0 || py >= terrain.getHeight())
                    continue;

                int pixel = terrain.getRGB(px, py);
                if ((pixel >> 24) != 0x00) { // Pixel non transparent => terrain solide
                    if(!isWater(px, py)) { // If it's solid and not water
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isColliding(Projectile p) {
        int projX = Math.round(p.getX());
        int projY = Math.round(p.getY());
        int width = p.getSize();
        int height = p.getSize();

        // On parcourt les pixels du personnage
        for (int px = projX; px < projX + width; px++) {
            for (int py = projY; py < projY + height; py++) {
                // Vérifie les limites de la map
                if (px < 0 || px >= terrain.getWidth() || py < 0 || py >= terrain.getHeight())
                    continue;

                int pixel = terrain.getRGB(px, py);
                if ((pixel >> 24) != 0x00) { // Pixel non transparent => terrain solide
                    if(!isWater(px, py)) { // If it's solid and not water
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isColliding(int x, int y , int w, int h) {
        if (isWater(x, y + h)) {
            return false;
        }
        for(int px = x; px < x + w; px++) {
            for(int py = y; py < y + h; py++) {
                // Check bounds
                if (px < 0 || px >= terrain.getWidth() || py < 0 || py >= terrain.getHeight())
                    continue;

                if (isPixelSolid(px, py)) { return true;
                }
            }
        }
        return false;
    }

    public void resolveCollision(ActorModel character) {
        // On vérifie s'il y a une collision
        if (!isColliding(character))
            return;

        // Tant qu'il est en collision, on le déplace vers le haut (pixel par pixel)
        /*while (isColliding(character)) {
            character.setY(character.getY() - 1);
        }
        */
        int y = character.getY();
        for (int j = 0; j <= character.getHeight(); j++) {
            if (isColliding(character)) {
                character.setY(character.getY() - 1);
            }
            else {
                break;
            }
        }
        if (isColliding(character)) {
            character.setY(y);
            float x = character.getPrevX();
            character.setX(x);
        }
        character.stopMoving();

        // Une fois sorti du terrain, on le place juste au-dessus
        // character.setY(character.getY() - 1); //Cette ligne cause un bug faisant flotter le personnage
    }

    public int[] isTerrain(float x, float y, float xTarget, float yTarget) {
        int [] res = {(int)x,(int)y};

        float vecX = xTarget - x;
        float vecY = yTarget - y;

        float steps = Math.max(Math.abs(vecX), Math.abs(vecY)); // Number of steps based on the largest distance
        if(steps == 0){ // If no movement
            return res; // Return original position
        }

        float xI = vecX / steps; // Increment per step
        float yI = vecY / steps; // Increment per step
        float xCurrent = x; // Current position
        float yCurrent = y; // Current position
        for(int i = 0; i <= steps ; i ++){ // Iterate through each step
            if (isPixelSolid((int)xCurrent,(int) yCurrent)) { // Check for collision at current position
                return new int[]{(int)(xCurrent), (int)(yCurrent)}; // Return collision position
            }
            xCurrent += xI; // Update current position
            yCurrent += yI; // Update current position
        }
        return res; // No collision, return target position
    }

    public boolean isWater(int x, int y){
        if (x < 0 || x >= terrain.getWidth() || y < 0 || y >= terrain.getHeight())
            return false;

        int pixel = terrain.getRGB(x, y);
        int alpha = (pixel >> 24) & 0xff;
        return alpha >=20 && alpha <= 235; // Water pixels have alpha (transparency)between 20 and 235
    }

    public void destroy(int x, int y, int radius) { //add parameter int radius >=0
        radius = Math.max(0, radius);

        ArrayList<Pair> circleCoordinates = new ArrayList<>();
        // uses symetry to calculate only one quadrant for the entire circle
        for (int x1 = x - radius; x1 <= x; x1++) {
            for (int y1 = y - radius; y1 <= y; y1++) {
                // square root too slow
                if((x1 - x) * (x1 - x) + (y1 - y) * (y1 - y) <= radius * radius){
                    int xSym = x - (x1 - x);
                    int ySym = y - (y1 - y);
                    // (x1,y1); (xSym,y1); (x1,ySym); (xSym,ySym) are in the circle

                    if (inBounds(x1,y1)){
                        circleCoordinates.add(new Pair(x1, y1));
                    }
                    if (inBounds(x1,ySym)){
                        circleCoordinates.add(new Pair(x1, ySym));
                    }
                    if (inBounds(xSym,y1)){
                        circleCoordinates.add(new Pair(xSym, y1));
                    }
                    if (inBounds(xSym,ySym)){
                        circleCoordinates.add(new Pair(xSym, ySym));
                    }
                }
            }

        }
        for (Pair p : circleCoordinates) {
            if (!isWater(p.x, p.y)) {
                terrain.setRGB(p.x,p.y,0x00);// destroy pixel in (x,y)
            }
        }
    }

    public BufferedImage getImage() {
        return terrain;
    }

    public VolatileImage getBackground() {
        return background;
    }

    public boolean isPixelSolid(int x, int y) {
        int pixel = terrain.getRGB(x, y);
        if ((pixel >> 24) == 0x00){
            return false;
        }
        return !isWater(x, y);
    }


}