package Backend;


public class MedKit extends Consumable {
    private int healAmount;
    
    public MedKit(){
        super("MedKit", "A kit used to heal wounds.");
        this.healAmount = healAmount;
        try {
            this.icon = loadBufferedImage("/Weapons/medkit.png");
        } catch (Exception e) {
            this.icon = null;
        }
    }
    @Override
    public void use(ActorModel user, GameEngine gameEngine){
        user.heal(this.healAmount);
        if(gameEngine.isTeamInventory()){
            gameEngine.getCurrentTeam().removeFromInventory(this);
            return;
        }
        gameEngine.getCurrentPlayer().removeFromInventory(this);
    }
}
