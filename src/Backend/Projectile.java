package Backend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

public class Projectile {
    private float x, y;
    private float vx, vy;
    private float dirX, dirY;
    private final float GRAVITY = 0.8f;
    private boolean isActive = true;
    private int size;
    private final float explosionRadius;
    private final ActorModel shooter;
    private final Color projectileColor;
    // private BufferedImage projectileImage;

    public Projectile(float startX, float startY, float initialSpeed, float directionX, float directionY,
            ActorModel shooter, float explosionRadius, int explosionDamage) {
        this.x = startX;
        this.y = startY;
        float magnitude = (float) Math.sqrt(directionX * directionX + directionY * directionY);
        if (magnitude == 0)
            magnitude = 1;
        this.dirX = directionX / magnitude;
        this.dirY = directionY / magnitude;
        this.vx = this.dirX * initialSpeed;
        this.vy = this.dirY * initialSpeed;
        this.size = 8; // size of the projectile

        this.shooter = shooter;
        this.explosionRadius = explosionRadius;
        this.projectileColor = Color.RED;
        this.isActive = true;
        // try { projectileImage = ImageIO.read(new
        // File("resources/Images/projectile.png")); } catch (IOException e) {
        // e.printStackTrace(); }

    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getVx() {
        return vx;
    }

    public float getVy() {
        return vy;
    }


    public float getDirY() {
        return dirY;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getSize() {
        return size;
    }

    public ActorModel getShooter() {
        return shooter;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, size, size);
    }

    public void update() {
        if (!isActive)
            return;
        x += vx;
        y += vy;
        vy += GRAVITY;
        if(x<-100 || x>2000 || y>2000) {
            isActive = false;// to free the round
        }
    }

    public void draw(Graphics g) {
        if (!isActive)
            return;
        else {
        }
        g.setColor(projectileColor);
        g.fillRect((int) x, (int) y, size, size);
    }

    public void checkCollisionAndExplode(Terrain terrain, List<ActorModel> players) {
        if (!isActive)
            return;
        
        Rectangle projectileBounds = getBounds();

        if (terrain.isColliding(this)) {
            explode(terrain, players);
            return;
        }

        for (ActorModel player : players) {
            if (player != shooter){
                if (projectileBounds.intersects(new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight()))) {
                explode(terrain, players);
                return;
                }
            }
        }
    }

    public void draw(Graphics g, float scaleX, float scaleY) { // draws the projectile on the terrainPanel
        if (!isActive) return;
        int screenX = (int)(x * scaleX);
        int screenY = (int)(y * scaleY);
        int screenSize = (int)(size * scaleX); // assuming size is the radius
        g.setColor(projectileColor);
        g.fillOval(screenX, screenY, screenSize, screenSize);
    }

    private void explode(Terrain terrain, List<ActorModel> players) {
        this.isActive = false;
        float explosionCenterX = x + size / 2f;
        float explosionCenterY = y + size / 2f;
        terrain.destroy((int) explosionCenterX, (int) explosionCenterY, (int) explosionRadius);
        applyDamageToPlayers(explosionCenterX, explosionCenterY, players);
    }

    private void applyDamageToPlayers(float centerX, float centerY, List<ActorModel> players) {
        for (ActorModel player : players) {
            float dx = player.getX() - centerX;
            float dy = player.getY() - centerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= explosionRadius) {
                // Calcul des dégâts avec décroissance (falloff) en fonction de la distance
                float normalizedDistance = distance / explosionRadius;
                float damageFactor = 1.0f - normalizedDistance;

                // Dégâts minimum de 1
                int finalDamage = Math.max(1, (int) (shooter.getMagicDamage() * damageFactor));

                if (player != shooter) {
                    player.sufferDamage(finalDamage);
                }
            }
        }
    }
}
