package Backend;

public abstract class Weapon extends ObjectModel {
    private int damage;
    public Weapon(String name, String description, int damage){
        super(name, description);
        this.damage = damage;
    }
    public abstract void use(ActorModel user, GameEngine gameEngine);
}