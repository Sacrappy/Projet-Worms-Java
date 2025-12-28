package Backend;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public abstract class ObjectModel {
    protected String name;
    protected String description;
    protected BufferedImage icon;
    public ObjectModel(String name, String description){
        this.name= name;
        this.description = description;
    }
    public ObjectModel(){
        this.name= "Error";
        this.description = "error";
    }

    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    public BufferedImage getIcon(){ return icon;}

    public abstract void use(ActorModel user, GameEngine gameEngine);
    
    public static BufferedImage loadBufferedImage(String path) throws IOException {
    

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
}