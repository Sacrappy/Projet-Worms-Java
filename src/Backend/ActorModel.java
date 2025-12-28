package Backend;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActorModel {
    private static Terrain terrain;

    private int HP;
    private int maxHP;
    private int MP;
    private int maxMP;
    private float prevY;
    private float x, y;
    private float vx, vy; //vertical speed
    private float prevX;
    private final float GRAVITY = 0.4f;
    private final float IMPULSION = -6.5f; // Negative because the Y axis increases downwards
    private final int MAX_CLIMB_HEIGHT = 10; // Max height the player can climb
    private int facing;//-1 for left & 1 for right
    final int height = 20;
    final int width = 10;

    private int physicalDamage;
    private int magicDamage;
    private int speed;
    private boolean alive = true;
    private boolean jumping = false;
    private BufferedImage icon;
    //private Passive passive;
    //private Skill skill;
    private List<ObjectModel> inventory= new ArrayList<>();
    
    
    
    

    public static void setTerrain(Terrain terrain) {
        ActorModel.terrain = terrain;
    }

    public ActorModel(){
        inventory.add(new Knife());
        HP = 100;
        maxHP = 100;
        MP = 100;
        maxMP = 100;
        x = 100;
        y = 400;
        vy = 0;
        facing = 1;
        physicalDamage = 20;
        magicDamage = 20;
        speed = 1;
        try {
            icon = Terrain.loadBufferedImage("/Images/Characters/Ver_Punk.png");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ActorModel(ActorModel worm){
        HP = worm.HP;
        maxHP = worm.maxHP;
        MP = worm.MP;
        maxMP = worm.maxMP;
        x = worm.x;
        y = worm.y;
        vx = worm.vx;
        vy = worm.vy;
        facing = worm.facing;
        jumping = worm.jumping;
        alive = worm.alive;
        physicalDamage = worm.physicalDamage;
        magicDamage = worm.magicDamage;
        speed = worm.speed;
    }

    public int getHP(){
        return HP;
    }
    public void setHP(int HP){
        this.HP = Math.min(HP, maxHP);
    }
    public void heal(int healAmount){
        this.HP = Math.min(HP + healAmount, maxHP);
    }
    public int getMaxHP() {
        return maxHP;
    }
    public int getMP() {
        return MP;
    }
    public void setMP(int MP) {
        this.MP = MP;
    }
    public int getX() {
        return Math.round(x);
    }
    public void setX(float x) {
        this.x = x;
    }
    public int getY() {
        return Math.round(y);
    }
    public void setY(float y) {
        this.y = y;
    }
    public void setVy(float vy) {
        this.vy = vy;
    }
    public float getPrevX() {
        return prevX;
    }
    public int getPhysicalDamage() {
        return physicalDamage;
    }
    public void setPhysicalDamage(int physicalDamage) {
        this.physicalDamage = physicalDamage;
    }
    public int getMagicDamage() {
        return magicDamage;
    }
    public void setMagicDamage(int magicDamage) {
        this.magicDamage = magicDamage;
    }
    public int getSpeed() {
        return speed;
    }
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    public float getVx() {
        return vx;
    }
    public float getVy() {
        return vy;
    }
    public int getHeight() {
        return height;
    }
    public int getWidth() {
        return width;
    }
    public BufferedImage getIcon() {return icon;}

    public boolean isAlive() {
        return alive;
    }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    public List<ObjectModel> getInventory(){
        return inventory;
    }
    public void removeFromInventory(ObjectModel item) {
    this.inventory.remove(item);
}
    public void changeFacing(int newFacing) {
        assert(newFacing == 1 || newFacing == -1);
        this.facing = newFacing;
    }
    public int getFacing() {
        return facing;
    }
    public boolean isOnGround(Terrain terrain) {
        //Verify if the character is touching any ground
        return terrain.isOnGround(this);
    }

    public boolean isColliding(Terrain terrain) {
        return terrain.isColliding(this);
    }

    public void resolveCollision(Terrain terrain) {
        // 1. if not colliding, return
        if (!terrain.isColliding((int)x, (int)y, width, height)) {
            return;
        }

        int currentY = (int) y;
        boolean canClimb = false;
        int targetY = currentY;

        // 2. check if we can climb the obstacle
        // we check up to MAX_CLIMB_HEIGHT pixels above
        for (int i = 1; i <= MAX_CLIMB_HEIGHT; i++) {
            // if the space above is free, we can climb
            if (!terrain.isColliding((int)x, currentY - i, width, height)) {
                targetY = currentY - i;
                canClimb = true;
                break; // we found a climbable height
            }
        }

        if (canClimb) {
            // Case 1 : it's a climbable obstacle
            y = targetY;
        } else {
            // Case 2: it's not climbable
            // we move back to previous X position
            x = prevX;

            // if still colliding after moving back in X,
            // we use terrain resolution method
            if (terrain.isColliding((int)x, (int)y, width, height)) {
                terrain.resolveCollision(this);
            }
        }

    }

    public void jump(){
        //Jump without direction, depending on a gravity physics
        if(isOnGround(terrain) && isAlive()){
            vy = IMPULSION;
            jumping = true;
        }
    }

    public void moveLeft(int dist) {
        if (isAlive()) {
            vx = -speed * dist;
            facing = -1;

        }
    }

    public void moveRight(int dist) {
        if (isAlive()) {
            vx = speed * dist;
            facing = 1;
        }
    }

    public void stopMoving() {
        vx = 0;
    }

    public void stopMovingConsole(){
        while(isAlive() && !terrain.isColliding(this) && terrain.inBounds((int) x, (int) y)) {
            y += 1;
            if(terrain.isWater((int)x, (int)y)){dies();}
        }
        if(!terrain.inBounds((int)x, (int)y)){
            dies();
        }
    }

    public void updateDirection(boolean jump) {
        if(!isAlive()) return;
        float originalX = x; // save previous position
        vy += GRAVITY;
        x += vx;
        if (terrain != null) {
            // Prevent going out of bounds on the left side
            if (x < 0) { x = 0;vx = 0;} // stop horizontal movement

            if (x + height > terrain.getImage().getWidth()) {
                // Prevent going out of bounds on the right side
                x = terrain.getImage().getWidth() - height;
                vx = 0; // stop horizontal movement
            }

            // handle X axis movement first

            if (isColliding(terrain)) { // handle wall and uphill collisions
                boolean climbed = false;
                // Try to climb if moving horizontally and on ground
                for (int i = 1; i <= MAX_CLIMB_HEIGHT; i++) {
                    if (!terrain.isColliding((int)x, (int)(y - i), width, height)) {
                        y -= i;// climb up
                        climbed = true;
                        break;
                    }
                }
                if (!climbed) {
                    // revert to previous X position if cannot climb
                    x = originalX;
                }
            }
        }

        float originalY = y; // save previous Y position
        y += vy;
        if (terrain != null) {
            if (y < 0) { y = 0; vy = 0; // stop vertical movement// Prevent going out of bounds on the top side
            }
            if (y + height > terrain.getImage().getHeight()) {
                setAlive(false); // The actor dies if he falls out of the map
                return;// no need to check for ground or collision
            }

            if(vy>0){ // currently moving up
                if(isOnGround(terrain)) {
                    vy = 0; // stop vertical movement
                    float surfaceY= terrain.getHeightBelow(x, y, width, height);
                    y = surfaceY - height;
                    jumping = false;
                }
            } else if(vy<0){
                if(isColliding(terrain)) {
                    y= originalY;
                    vy=0;
                }
            }

            if (terrain.isWater((int) x, (int) y + height)) { // if touching water dies
                dies();
            }
        }
    }

    void dies(){
        setHP(0);
        alive = false;
    }

    public void target(ActorModel worm){
        int xTarget =  worm.getX();
        int yTarget = worm.getY();
        int[] pos = terrain.isTerrain(x,y,xTarget,yTarget);
        int xI = pos[0];
        int yI = pos[1];
        if (xI == xTarget && yI == yTarget) {
            magicAttack(worm,0);//Pour une attaque magique.
            terrain.destroy(xI,yI, 20);// arbitrary radius
        }
        else if (xI <= terrain.terrain.getWidth() &&  yI <= terrain.terrain.getHeight()) {
            terrain.destroy(xI, yI,20);
        }
    }

    public void sufferDamage(int damage){
        //The worm loses a damage number of HP or hits O HP if HP - damage < 0
        if(alive){
            setHP(Math.max(getHP() - damage,0));
            if (HP == 0) {alive = false;}
        }
    }

    public void magicAttack(ActorModel m, int cost){
        //This worm attacks the worm m with magic. He also loses a cost number of MP
        if(alive && m.isAlive() && MP >= cost){
            m.sufferDamage(magicDamage);
            setMP(Math.max(getMP() - cost,0));
        }
    }

    public void physicalAttack(ActorModel m){
        //This worm deals the worm "m" "PhysicalDamage" damages.
        if(alive &&  m.isAlive()){
            m.sufferDamage(physicalDamage);
        }
    }

    public void draw(Graphics g, float scaleX, float scaleY, boolean isActive) {
        if (!isAlive()) return;
        //coord calculation
        int screenX = (int)(x * scaleX);
        int screenY = (int)(y * scaleY);
        int screenWidth = (int)(width * scaleX);
        int screenHeight = (int)(height * scaleY);

        if(icon!= null){
            g.drawImage(icon, screenX, screenY, screenWidth, screenHeight, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(screenX, screenY, screenWidth, screenHeight);
        }
        g.setColor(Color.GREEN);
        g.fillRect(screenX, screenY-10,(int) (screenWidth*((float)HP/maxHP)),5); // draw HP bar above character

        if(isActive){
            g.setColor(Color.MAGENTA);
            int dotSize = 10;
            int dotX = screenX + screenWidth / 2 - dotSize / 2;
            int dotY = screenY - 25; // position above the character
            g.fillOval(dotX, dotY, dotSize, dotSize); // draw a magenta dot above active character
        }
    }

    //public void passTurn(){}
}
