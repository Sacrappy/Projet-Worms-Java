package Backend;

public abstract class ProjectileWeapon extends Weapon {
    private int maxPower;
    public ProjectileWeapon(String name, String description, int damage, int maxPower){
        super(name, description, damage);
        this.maxPower = maxPower;
    }
    public abstract void fire(ActorModel user, float dirX, float dirY,float maxPower, GameEngine gameEngine);
    }

