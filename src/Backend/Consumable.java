package Backend;
public abstract class Consumable extends ObjectModel{
    public Consumable(String name, String description){
        super(name,description);
    }
    public abstract void use(ActorModel user, GameEngine gameEngine);
}